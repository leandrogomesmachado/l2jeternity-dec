package l2e.scripts.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.TimeUtils;
import l2e.gameserver.Announcements;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractWorldEvent;
import l2e.gameserver.model.entity.events.model.template.WorldEventSpawn;
import l2e.gameserver.model.entity.events.model.template.WorldEventTemplate;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Hitman extends AbstractWorldEvent {
   private static boolean _isActive = false;
   private WorldEventTemplate _template = null;
   private ScheduledFuture<?> _startTask = null;
   private ScheduledFuture<?> _stopTask = null;
   private final List<Npc> _npcList = new ArrayList<>();
   protected static Map<Integer, Hitman.PlayerToAssasinate> _targets;
   private static Map<String, Integer> _currency;
   private static DecimalFormat f = new DecimalFormat(",##0,000");
   private final int MIN_MAX_CLEAN_RATE = Config.HITMAN_SAVE_TARGET * 60000;

   public Hitman(String name, String descr) {
      super(name, descr);
      this._template = this.parseSettings(this.getName());
      if (this._template != null && !_isActive) {
         long expireTime = this.restoreStatus(this.getName());
         if (expireTime <= System.currentTimeMillis() && expireTime != -1L) {
            this.checkTimerTask(this.calcEventTime(this._template), true);
         } else {
            this.eventStart(expireTime == -1L ? -1L : expireTime - System.currentTimeMillis());
         }
      }
   }

   @Override
   public boolean isEventActive() {
      return _isActive;
   }

   @Override
   public WorldEventTemplate getEventTemplate() {
      return this._template;
   }

   @Override
   public boolean eventStart(long totalTime) {
      if (!_isActive && totalTime != 0L) {
         if (this._startTask != null) {
            this._startTask.cancel(false);
            this._startTask = null;
         }

         _isActive = true;
         List<WorldEventSpawn> spawnList = this._template.getSpawnList();
         if (spawnList != null && !spawnList.isEmpty()) {
            for(WorldEventSpawn spawn : spawnList) {
               this._npcList
                  .add(
                     addSpawn(
                        spawn.getNpcId(),
                        spawn.getLocation().getX(),
                        spawn.getLocation().getY(),
                        spawn.getLocation().getZ(),
                        spawn.getLocation().getHeading(),
                        false,
                        0L
                     )
                  );
            }
         }

         _targets = this.load();
         _currency = this.loadCurrency();
         ThreadPoolManager.getInstance().scheduleAtFixedRate(new Hitman.AISystem(), (long)this.MIN_MAX_CLEAN_RATE, (long)this.MIN_MAX_CLEAN_RATE);
         ServerMessage msg = new ServerMessage("EventHitman.START", true);
         Announcements.getInstance().announceToAll(msg);
         this.updateStatus(this.getName(), totalTime == -1L ? -1L : totalTime + System.currentTimeMillis(), true);
         if (totalTime > 0L) {
            this._stopTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
               @Override
               public void run() {
                  Hitman.this.eventStop();
               }
            }, totalTime);
            _log.info("Event " + this._template.getName() + " will end in: " + TimeUtils.toSimpleFormat(System.currentTimeMillis() + totalTime));
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean eventStop() {
      if (!_isActive) {
         return false;
      } else {
         if (this._stopTask != null) {
            this._stopTask.cancel(false);
            this._stopTask = null;
         }

         _isActive = false;
         if (!this._npcList.isEmpty()) {
            for(Npc _npc : this._npcList) {
               if (_npc != null) {
                  _npc.deleteMe();
               }
            }
         }

         this._npcList.clear();
         ServerMessage msg = new ServerMessage("EventHitman.STOP", true);
         Announcements.getInstance().announceToAll(msg);
         this.updateStatus(this.getName(), 0L, false);
         this.checkTimerTask(this.calcEventTime(this._template), false);
         return true;
      }
   }

   private Map<String, Integer> loadCurrency() {
      Map<String, Integer> currency = new HashMap<>();

      try {
         for(Integer itemId : Config.HITMAN_CURRENCY) {
            currency.put(getCurrencyName(itemId).trim().replaceAll(" ", "_"), itemId);
         }

         return currency;
      } catch (Exception var4) {
         return new HashMap<>();
      }
   }

   public static Map<String, Integer> getCurrencys() {
      return _currency;
   }

   public static Integer getCurrencyId(String name) {
      return _currency.get(name);
   }

   public static String getCurrencyName(Integer itemId) {
      return ItemsParser.getInstance().getTemplate(itemId).getNameEn();
   }

   private Map<Integer, Hitman.PlayerToAssasinate> load() {
      Map<Integer, Hitman.PlayerToAssasinate> map = new HashMap<>();

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement st = con.prepareStatement("SELECT targetId, clientId, target_name, itemId, bounty, pending_delete FROM hitman_list");
         ResultSet rs = st.executeQuery();

         while(rs.next()) {
            int targetId = rs.getInt("targetId");
            int clientId = rs.getInt("clientId");
            String target_name = rs.getString("target_name");
            int itemId = rs.getInt("itemId");
            Long bounty = rs.getLong("bounty");
            boolean pending = rs.getInt("pending_delete") == 1;
            if (pending) {
               removeTarget(targetId, false);
            } else {
               map.put(targetId, new Hitman.PlayerToAssasinate(targetId, clientId, itemId, bounty, target_name));
            }
         }

         _log.info("Event " + this._template.getName() + ": Loaded - " + map.size() + " Assassination Target(s)...");
         return map;
      } catch (Exception var22) {
         _log.warning("Hitman: " + var22.getCause());
         return new HashMap<>();
      }
   }

   public static void onDeath(Player assassin, Player target) {
      if (_targets.containsKey(target.getObjectId())) {
         int assassinClan = assassin.getClanId();
         int assassinAlly = assassin.getAllyId();
         if (Config.HITMAN_SAME_TEAM && (assassinClan != 0 && assassinClan == target.getClanId() || assassinAlly != 0 && assassinAlly == target.getAllyId())) {
            assassin.sendMessage(new ServerMessage("Hitman.CONSIDERED", assassin.getLang()).toString());
            assassin.sendMessage(new ServerMessage("Hitman.SAME_CLAN", assassin.getLang()).toString());
            return;
         }

         Hitman.PlayerToAssasinate pta = _targets.get(target.getObjectId());
         String name = getOfflineData(null, pta.getClientId())[1];
         Player client = World.getInstance().getPlayer(name);
         ServerMessage msg1 = new ServerMessage("Hitman.RECEIVE_REWARD", target.getLang());
         msg1.add(assassin.getName());
         target.sendMessage(msg1.toString());
         if (client != null) {
            ServerMessage msg = new ServerMessage("Hitman.ASSASIN_REQUEST", client.getLang());
            msg.add(target.getName());
            client.sendMessage(msg.toString());
            client.removeHitmanTarget(target.getObjectId());
         }

         assassin.sendMessage(new ServerMessage("Hitman.MURDER", assassin.getLang()).toString());
         rewardAssassin(assassin, target, pta.getItemId(), pta.getBounty());
         removeTarget(pta.getObjectId(), true);
      }
   }

   private static void rewardAssassin(Player activeChar, Player target, int itemId, Long bounty) {
      PcInventory inv = activeChar.getInventory();
      if (ItemsParser.getInstance().createDummyItem(itemId).isStackable()) {
         inv.addItem("Hitman", itemId, bounty, activeChar, target);
         SystemMessage systemMessage;
         if (bounty > 1L) {
            systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
            systemMessage.addItemName(itemId);
            systemMessage.addItemNumber(bounty);
         } else {
            systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
            systemMessage.addItemName(itemId);
         }

         activeChar.sendPacket(systemMessage);
      } else {
         for(int i = 0; (long)i < bounty; ++i) {
            inv.addItem("Hitman", itemId, 1L, activeChar, target);
            SystemMessage systemMessage = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
            systemMessage.addItemName(itemId);
            activeChar.sendPacket(systemMessage);
         }
      }
   }

   public static void getEnterWorld(Player activeChar) {
      if (_targets.containsKey(activeChar.getObjectId())) {
         activeChar.sendMessage(new ServerMessage("Hitman.ASK_MURDER", activeChar.getLang()).toString());
      }

      if (!activeChar.getHitmanTargets().isEmpty()) {
         for(int charId : activeChar.getHitmanTargets()) {
            if (!_targets.containsKey(charId)) {
               ServerMessage msg = new ServerMessage("Hitman.TARGET_ELIMINATE", activeChar.getLang());
               msg.add(CharNameHolder.getInstance().getNameById(charId));
               activeChar.sendMessage(msg.toString());
               activeChar.removeHitmanTarget(charId);
            } else {
               ServerMessage msg = new ServerMessage("Hitman.TARGET_STILL", activeChar.getLang());
               msg.add(CharNameHolder.getInstance().getNameById(charId));
               activeChar.sendMessage(msg.toString());
            }
         }
      }
   }

   public void save() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         for(Hitman.PlayerToAssasinate pta : _targets.values()) {
            PreparedStatement st = con.prepareStatement(
               "REPLACE INTO hitman_list (targetId,clientId,target_name,itemId,bounty,pending_delete) VALUES (?,?,?,?,?,?)"
            );
            st.setInt(1, pta.getObjectId());
            st.setInt(2, pta.getClientId());
            st.setString(3, pta.getName());
            st.setInt(4, pta.getItemId());
            st.setLong(5, pta.getBounty());
            st.setInt(6, pta.isPendingDelete() ? 1 : 0);
            st.execute();
            st.close();
         }
      } catch (Exception var16) {
         _log.warning("Hitman: " + var16);
      }
   }

   public static void putHitOn(Player client, String playerName, Long bounty, Integer itemId) {
      Player player = World.getInstance().getPlayer(playerName);
      if (client.getHitmanTargets().size() >= Config.HITMAN_TARGETS_LIMIT) {
         client.sendMessage(new ServerMessage("Hitman.OUR_CLIENT", client.getLang()).toString());
      } else if (client.getInventory().getInventoryItemCount(itemId, -1) < bounty) {
         client.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
      } else {
         if (player == null && CharNameHolder.getInstance().doesCharNameExist(playerName)) {
            Integer targetId = Integer.parseInt(getOfflineData(playerName, 0)[0]);
            if (_targets.containsKey(targetId)) {
               client.sendMessage(new ServerMessage("Hitman.ALREADY_HIT", client.getLang()).toString());
               return;
            }

            _targets.put(targetId, new Hitman.PlayerToAssasinate(targetId, client.getObjectId(), itemId, bounty, playerName));
            client.destroyItemByItemId("Hitman", itemId, bounty, client, true);
            client.addHitmanTarget(targetId);
            if (Config.HITMAN_ANNOUNCE) {
               ServerMessage msg = new ServerMessage("Hitman.ANNOUNCE_PAID", true);
               msg.add(client.getName());
               msg.add(bounty > 999L ? f.format(bounty) : bounty);
               msg.add(getCurrencyName(itemId));
               msg.add(playerName);
               Announcements.getInstance().announceToAll(msg);
            }
         } else if (player != null && CharNameHolder.getInstance().doesCharNameExist(playerName)) {
            if (_targets.containsKey(player.getObjectId())) {
               client.sendMessage(new ServerMessage("Hitman.ALREADY_HIT", client.getLang()).toString());
               return;
            }

            player.sendMessage(new ServerMessage("Hitman.HIT_YOU", client.getLang()).toString());
            _targets.put(player.getObjectId(), new Hitman.PlayerToAssasinate(player, client.getObjectId(), itemId, bounty));
            client.destroyItemByItemId("Hitman", itemId, bounty, client, true);
            client.addHitmanTarget(player.getObjectId());
            if (Config.HITMAN_ANNOUNCE) {
               ServerMessage msg = new ServerMessage("Hitman.ANNOUNCE_PAID", true);
               msg.add(client.getName());
               msg.add(bounty > 999L ? f.format(bounty) : bounty);
               msg.add(getCurrencyName(itemId));
               msg.add(playerName);
               Announcements.getInstance().announceToAll(msg);
            }
         } else {
            client.sendMessage(new ServerMessage("Hitman.NAME_INVALID", client.getLang()).toString());
         }
      }
   }

   public static void removeTarget(int obId, boolean live) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement st = con.prepareStatement("DELETE FROM hitman_list WHERE targetId = ?");
         st.setInt(1, obId);
         st.execute();
         if (live) {
            _targets.remove(obId);
         }
      } catch (Exception var15) {
         _log.warning("Hitman: " + var15);
      }
   }

   public static void cancelAssasination(String name, Player client) {
      Player target = World.getInstance().getPlayer(name);
      if (client.getHitmanTargets().isEmpty()) {
         client.sendMessage(new ServerMessage("Hitman.DONT_OWN", client.getLang()).toString());
      } else {
         if (target == null) {
            int tgtObjectId = CharNameHolder.getInstance().getIdByName(name);
            if (tgtObjectId > 0) {
               boolean found = false;

               for(int objId : client.getHitmanTargets()) {
                  if (objId == tgtObjectId) {
                     found = true;
                     break;
                  }
               }

               Hitman.PlayerToAssasinate pta = _targets.get(tgtObjectId);
               if (!found || !_targets.containsKey(pta.getObjectId())) {
                  client.sendMessage(new ServerMessage("Hitman.NO_HIT", client.getLang()).toString());
               } else if (pta.getClientId() == client.getObjectId()) {
                  client.removeHitmanTarget(pta.getObjectId());
                  removeTarget(pta.getObjectId(), true);
                  client.sendMessage(new ServerMessage("Hitman.CANCEL_HIT", client.getLang()).toString());
               } else {
                  client.sendMessage(new ServerMessage("Hitman.NO_ACTUAL_TARGET", client.getLang()).toString());
               }
            } else {
               client.sendMessage(new ServerMessage("Hitman.NAME_INVALID", client.getLang()).toString());
            }
         } else {
            boolean found = false;

            for(int objId : client.getHitmanTargets()) {
               if (objId == target.getObjectId()) {
                  found = true;
                  break;
               }
            }

            Hitman.PlayerToAssasinate pta = _targets.get(target.getObjectId());
            if (!found || !_targets.containsKey(pta.getObjectId())) {
               client.sendMessage(new ServerMessage("Hitman.NO_HIT", client.getLang()).toString());
            } else if (pta.getClientId() == client.getObjectId()) {
               client.removeHitmanTarget(pta.getObjectId());
               removeTarget(pta.getObjectId(), true);
               client.sendMessage(new ServerMessage("Hitman.CANCEL_HIT", client.getLang()).toString());
               target.sendMessage(new ServerMessage("Hitman.YOUR_HIT_CANCEL", target.getLang()).toString());
            } else {
               client.sendMessage(new ServerMessage("Hitman.NO_ACTUAL_TARGET", client.getLang()).toString());
            }
         }
      }
   }

   public static String[] getOfflineData(String name, int objId) {
      String[] set = new String[2];

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement st = con.prepareStatement(
            objId > 0 ? "SELECT charId, char_name FROM characters WHERE charId = ?" : "SELECT charId, char_name FROM characters WHERE char_name = ?"
         );
         if (objId > 0) {
            st.setInt(1, objId);
         } else {
            st.setString(1, name);
         }

         ResultSet rs;
         for(rs = st.executeQuery(); rs.next(); set[1] = rs.getString("char_name")) {
            set[0] = String.valueOf(rs.getInt("charId"));
         }

         st.close();
         rs.close();
      } catch (Exception var17) {
         _log.warning("Hitman: " + var17);
      }

      return set;
   }

   public static boolean exists(int objId) {
      return _targets.containsKey(objId);
   }

   public static Hitman.PlayerToAssasinate getTarget(int objId) {
      return _targets.get(objId);
   }

   public static Map<Integer, Hitman.PlayerToAssasinate> getTargets() {
      return _targets;
   }

   public static Map<Integer, Hitman.PlayerToAssasinate> getTargetsOnline() {
      Map<Integer, Hitman.PlayerToAssasinate> online = new HashMap<>();

      for(Integer objId : _targets.keySet()) {
         Hitman.PlayerToAssasinate pta = _targets.get(objId);
         if (pta.isOnline() && !pta.isPendingDelete()) {
            online.put(objId, pta);
         }
      }

      return online;
   }

   public void set_targets(Map<Integer, Hitman.PlayerToAssasinate> targets) {
      _targets = targets;
   }

   public static boolean getActive() {
      return _isActive;
   }

   @Override
   public void startTimerTask(long time, final boolean checkZero) {
      if (this._startTask != null) {
         this._startTask.cancel(false);
         this._startTask = null;
      }

      this._startTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
         @Override
         public void run() {
            Hitman.this.eventStart(checkZero ? -1L : (long)(Hitman.this._template.getPeriod() * 3600000));
         }
      }, time - System.currentTimeMillis());
      _log.info("Event " + this._template.getName() + " will start in: " + TimeUtils.toSimpleFormat(time));
   }

   @Override
   public boolean isReloaded() {
      if (this.isEventActive()) {
         return false;
      } else {
         this._template = this.parseSettings(this.getName());
         if (this._template == null) {
            return false;
         } else {
            long expireTime = this.restoreStatus(this.getName());
            if (expireTime <= System.currentTimeMillis() && expireTime != -1L) {
               this.checkTimerTask(this.calcEventTime(this._template), true);
            } else {
               this.eventStart(expireTime == -1L ? -1L : expireTime - System.currentTimeMillis());
            }

            return true;
         }
      }
   }

   public static void main(String[] args) {
      new Hitman(Hitman.class.getSimpleName(), "events");
   }

   public class AISystem implements Runnable {
      @Override
      public void run() {
         for(Hitman.PlayerToAssasinate target : Hitman._targets.values()) {
            if (target.isPendingDelete()) {
               Hitman.removeTarget(target.getObjectId(), true);
            }
         }

         Hitman.this.save();
      }
   }

   public static class PlayerToAssasinate {
      private int _objectId;
      private int _clientId;
      private String _name;
      private int _itemId;
      private Long _bounty;
      private boolean _online;
      private boolean _pendingDelete;

      public PlayerToAssasinate(Player target, int clientId, int itemId, Long bounty) {
         this._objectId = target.getObjectId();
         this._clientId = clientId;
         this._name = target.getName();
         this._itemId = itemId;
         this._bounty = bounty;
         this._online = target.isOnline();
      }

      public PlayerToAssasinate(int objectId, int clientId, int itemId, Long bounty, String name) {
         this._objectId = objectId;
         this._clientId = clientId;
         this._name = name;
         this._itemId = itemId;
         this._bounty = bounty;
         this._online = false;
      }

      public void setObjectId(int objectId) {
         this._objectId = objectId;
      }

      public int getObjectId() {
         return this._objectId;
      }

      public void setName(String name) {
         this._name = name;
      }

      public String getName() {
         return this._name;
      }

      public void setItemId(int itemId) {
         this._itemId = itemId;
      }

      public int getItemId() {
         return this._itemId;
      }

      public String getItemName(Player player) {
         return player.getItemName(ItemsParser.getInstance().getTemplate(this.getItemId()));
      }

      public void setBounty(Long vol) {
         this._bounty = vol;
      }

      public void incBountyBy(Long vol) {
         this._bounty = this._bounty + vol;
      }

      public void decBountyBy(Long vol) {
         this._bounty = this._bounty - vol;
      }

      public long getBounty() {
         return this._bounty;
      }

      public void setOnline(boolean online) {
         this._online = online;
      }

      public boolean isOnline() {
         return this._online;
      }

      public void setClientId(int clientId) {
         this._clientId = clientId;
      }

      public int getClientId() {
         return this._clientId;
      }

      public void setPendingDelete(boolean pendingDelete) {
         this._pendingDelete = pendingDelete;
      }

      public boolean isPendingDelete() {
         return this._pendingDelete;
      }
   }
}
