package l2e.gameserver.instancemanager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.commons.util.Broadcast;
import l2e.gameserver.Config;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.CursedWeapon;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DefenderInstance;
import l2e.gameserver.model.actor.instance.FeedableBeastInstance;
import l2e.gameserver.model.actor.instance.FestivalMonsterInstance;
import l2e.gameserver.model.actor.instance.FortCommanderInstance;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.actor.instance.GuardInstance;
import l2e.gameserver.model.actor.instance.RiftInvaderInstance;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CursedWeaponsManager {
   private static final Logger _log = Logger.getLogger(CursedWeaponsManager.class.getName());
   private Map<Integer, CursedWeapon> _cursedWeapons;

   protected CursedWeaponsManager() {
      this.init();
   }

   private void init() {
      this._cursedWeapons = new HashMap<>();
      if (Config.ALLOW_CURSED_WEAPONS) {
         this.load();
         this.restore();
         this.controlPlayers();
         _log.info(this.getClass().getSimpleName() + ": Loaded : " + this._cursedWeapons.size() + " cursed weapon(s).");
      }
   }

   public final void reload() {
      this.init();
   }

   private final void load() {
      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         File file = new File(Config.DATAPACK_ROOT + "/data/stats/items/cursedWeapons.xml");
         if (!file.exists()) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Couldn't find data/stats/items/" + file.getName());
         } else {
            Document doc = factory.newDocumentBuilder().parse(file);

            for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
               if ("list".equalsIgnoreCase(n.getNodeName())) {
                  for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                     if ("item".equalsIgnoreCase(d.getNodeName())) {
                        NamedNodeMap attrs = d.getAttributes();
                        int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                        int skillId = Integer.parseInt(attrs.getNamedItem("skillId").getNodeValue());
                        String name = attrs.getNamedItem("name").getNodeValue();
                        CursedWeapon cw = new CursedWeapon(id, skillId, name);

                        for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                           if ("dropRate".equalsIgnoreCase(cd.getNodeName())) {
                              attrs = cd.getAttributes();
                              int val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
                              cw.setDropRate(val);
                           } else if ("duration".equalsIgnoreCase(cd.getNodeName())) {
                              attrs = cd.getAttributes();
                              int val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
                              cw.setDuration(val);
                           } else if ("durationLost".equalsIgnoreCase(cd.getNodeName())) {
                              attrs = cd.getAttributes();
                              int val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
                              cw.setDurationLost(val);
                           } else if ("disapearChance".equalsIgnoreCase(cd.getNodeName())) {
                              attrs = cd.getAttributes();
                              int val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
                              cw.setDisapearChance(val);
                           } else if ("stageKills".equalsIgnoreCase(cd.getNodeName())) {
                              attrs = cd.getAttributes();
                              int val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
                              cw.setStageKills(val);
                           }
                        }

                        this._cursedWeapons.put(id, cw);
                     }
                  }
               }
            }
         }
      } catch (Exception var13) {
         _log.log(Level.SEVERE, "Error parsing cursed weapons file.", (Throwable)var13);
      }
   }

   private final void restore() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT itemId, charId, playerKarma, playerPkKills, nbKills, endTime FROM cursed_weapons");
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            int itemId = rset.getInt("itemId");
            int playerId = rset.getInt("charId");
            int playerKarma = rset.getInt("playerKarma");
            int playerPkKills = rset.getInt("playerPkKills");
            int nbKills = rset.getInt("nbKills");
            long endTime = rset.getLong("endTime");
            CursedWeapon cw = this._cursedWeapons.get(itemId);
            cw.setPlayerId(playerId);
            cw.setPlayerKarma(playerKarma);
            cw.setPlayerPkKills(playerPkKills);
            cw.setNbKills(nbKills);
            cw.setEndTime(endTime);
            cw.reActivate();
         }

         rset.close();
         statement.close();
      } catch (Exception var23) {
         _log.log(Level.WARNING, "Could not restore CursedWeapons data: " + var23.getMessage(), (Throwable)var23);
      }
   }

   private final void controlPlayers() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT owner_id FROM items WHERE item_id=?");
      ) {
         for(CursedWeapon cw : this._cursedWeapons.values()) {
            if (!cw.isActivated()) {
               int itemId = cw.getItemId();
               statement.setInt(1, itemId);

               try (ResultSet rset = statement.executeQuery()) {
                  if (rset.next()) {
                     int playerId = rset.getInt("owner_id");
                     _log.info("PROBLEM : Player " + playerId + " owns the cursed weapon " + itemId + " but he shouldn't.");

                     try (PreparedStatement delete = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?")) {
                        delete.setInt(1, playerId);
                        delete.setInt(2, itemId);
                        if (delete.executeUpdate() != 1) {
                           _log.warning("Error while deleting cursed weapon " + itemId + " from userId " + playerId);
                        }
                     }

                     try (PreparedStatement update = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE charId=?")) {
                        update.setInt(1, cw.getPlayerKarma());
                        update.setInt(2, cw.getPlayerPkKills());
                        update.setInt(3, playerId);
                        if (update.executeUpdate() != 1) {
                           _log.warning("Error while updating karma & pkkills for userId " + cw.getPlayerId());
                        }
                     }

                     removeFromDb(itemId);
                  }
               }

               statement.clearParameters();
            }
         }
      } catch (Exception var134) {
         _log.log(Level.WARNING, "Could not check CursedWeapons data: " + var134.getMessage(), (Throwable)var134);
      }
   }

   public synchronized void checkDrop(Attackable attackable, Player player) {
      if (!(attackable instanceof DefenderInstance)
         && !(attackable instanceof RiftInvaderInstance)
         && !(attackable instanceof FestivalMonsterInstance)
         && !(attackable instanceof GuardInstance)
         && !(attackable instanceof GrandBossInstance)
         && !(attackable instanceof FeedableBeastInstance)
         && !(attackable instanceof FortCommanderInstance)) {
         for(CursedWeapon cw : this._cursedWeapons.values()) {
            if (!cw.isActive() && cw.checkDrop(attackable, player)) {
               break;
            }
         }
      }
   }

   public void activate(Player player, ItemInstance item) {
      CursedWeapon cw = this._cursedWeapons.get(item.getId());
      if (player.isCursedWeaponEquipped()) {
         CursedWeapon cw2 = this._cursedWeapons.get(player.getCursedWeaponEquippedId());
         cw2.setNbKills(cw2.getStageKills() - 1);
         cw2.increaseKills();
         cw.setPlayer(player);
         cw.endOfLife();
      } else {
         cw.activate(player, item);
      }
   }

   public void drop(int itemId, Creature killer) {
      CursedWeapon cw = this._cursedWeapons.get(itemId);
      cw.dropIt(killer);
   }

   public void increaseKills(int itemId) {
      CursedWeapon cw = this._cursedWeapons.get(itemId);
      cw.increaseKills();
   }

   public int getLevel(int itemId) {
      CursedWeapon cw = this._cursedWeapons.get(itemId);
      return cw.getLevel();
   }

   public static void announce(SystemMessage sm) {
      Broadcast.toAllOnlinePlayers(sm);
   }

   public void checkPlayer(Player player) {
      if (player != null) {
         for(CursedWeapon cw : this._cursedWeapons.values()) {
            if (cw.isActivated() && player.getObjectId() == cw.getPlayerId()) {
               cw.setPlayer(player);
               cw.setItem(player.getInventory().getItemByItemId(cw.getItemId()));
               cw.giveSkill();
               player.setCursedWeaponEquippedId(cw.getItemId());
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
               sm.addString(cw.getName());
               sm.addNumber((int)((cw.getEndTime() - System.currentTimeMillis()) / 60000L));
               player.sendPacket(sm);
            }
         }
      }
   }

   public int checkOwnsWeaponId(int ownerId) {
      for(CursedWeapon cw : this._cursedWeapons.values()) {
         if (cw.isActivated() && ownerId == cw.getPlayerId()) {
            return cw.getItemId();
         }
      }

      return -1;
   }

   public static void removeFromDb(int itemId) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");
         statement.setInt(1, itemId);
         statement.executeUpdate();
         statement.close();
      } catch (SQLException var14) {
         _log.log(Level.SEVERE, "CursedWeaponsManager: Failed to remove data: " + var14.getMessage(), (Throwable)var14);
      }
   }

   public void saveData() {
      for(CursedWeapon cw : this._cursedWeapons.values()) {
         cw.saveData();
      }
   }

   public boolean isCursed(int itemId) {
      return this._cursedWeapons.containsKey(itemId);
   }

   public Collection<CursedWeapon> getCursedWeapons() {
      return this._cursedWeapons.values();
   }

   public Set<Integer> getCursedWeaponsIds() {
      return this._cursedWeapons.keySet();
   }

   public CursedWeapon getCursedWeapon(int itemId) {
      return this._cursedWeapons.get(itemId);
   }

   public void givePassive(int itemId) {
      try {
         this._cursedWeapons.get(itemId).giveSkill();
      } catch (Exception var3) {
      }
   }

   public static final CursedWeaponsManager getInstance() {
      return CursedWeaponsManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CursedWeaponsManager _instance = new CursedWeaponsManager();
   }
}
