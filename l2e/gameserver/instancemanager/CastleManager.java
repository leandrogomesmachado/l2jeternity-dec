package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.InstanceListManager;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.items.instance.ItemInstance;

public final class CastleManager implements InstanceListManager {
   protected static final Logger _log = Logger.getLogger(CastleManager.class.getName());
   private final List<Castle> _castles = new ArrayList<>();
   private static final int[] _castleCirclets = new int[]{0, 6838, 6835, 6839, 6837, 6840, 6834, 6836, 8182, 8183};

   public static final CastleManager getInstance() {
      return CastleManager.SingletonHolder._instance;
   }

   protected CastleManager() {
   }

   public final int findNearestCastleIndex(GameObject obj) {
      return this.findNearestCastleIndex(obj, Long.MAX_VALUE);
   }

   public final int findNearestCastleIndex(GameObject obj, long maxDistance) {
      int index = this.getCastleIndex(obj);
      if (index < 0) {
         for(int i = 0; i < this.getCastles().size(); ++i) {
            Castle castle = this.getCastles().get(i);
            if (castle != null) {
               double distance = castle.getDistance(obj);
               if ((double)maxDistance > distance) {
                  maxDistance = (long)distance;
                  index = i;
               }
            }
         }
      }

      return index;
   }

   public final Castle getCastleById(int castleId) {
      for(Castle temp : this.getCastles()) {
         if (temp.getId() == castleId) {
            return temp;
         }
      }

      return null;
   }

   public final Castle getCastleByOwner(Clan clan) {
      for(Castle temp : this.getCastles()) {
         if (temp.getOwnerId() == clan.getId()) {
            return temp;
         }
      }

      return null;
   }

   public final Castle getCastle(String name) {
      for(Castle temp : this.getCastles()) {
         if (temp.getName().equalsIgnoreCase(name.trim())) {
            return temp;
         }
      }

      return null;
   }

   public final Castle getCastle(int x, int y, int z) {
      for(Castle temp : this.getCastles()) {
         if (temp.checkIfInZone(x, y, z)) {
            return temp;
         }
      }

      return null;
   }

   public final Castle getCastle(GameObject activeObject) {
      return this.getCastle(activeObject.getX(), activeObject.getY(), activeObject.getZ());
   }

   public final int getCastleIndex(int castleId) {
      for(int i = 0; i < this.getCastles().size(); ++i) {
         Castle castle = this.getCastles().get(i);
         if (castle != null && castle.getId() == castleId) {
            return i;
         }
      }

      return -1;
   }

   public final int getCastleIndex(GameObject activeObject) {
      return this.getCastleIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
   }

   public final int getCastleIndex(int x, int y, int z) {
      for(int i = 0; i < this.getCastles().size(); ++i) {
         Castle castle = this.getCastles().get(i);
         if (castle != null && castle.checkIfInZone(x, y, z)) {
            return i;
         }
      }

      return -1;
   }

   public final List<Castle> getCastles() {
      return this._castles;
   }

   public final void validateTaxes(int sealStrifeOwner) {
      int maxTax;
      switch(sealStrifeOwner) {
         case 1:
            maxTax = 5;
            break;
         case 2:
            maxTax = 25;
            break;
         default:
            maxTax = 15;
      }

      for(Castle castle : this._castles) {
         if (castle.getTaxPercent() > maxTax) {
            castle.setTaxPercent(maxTax);
         }
      }
   }

   public int getCirclet() {
      return this.getCircletByCastleId(1);
   }

   public int getCircletByCastleId(int castleId) {
      return castleId > 0 && castleId < 10 ? _castleCirclets[castleId] : 0;
   }

   public void removeCirclet(Clan clan, int castleId) {
      for(ClanMember member : clan.getMembers()) {
         this.removeCirclet(member, castleId);
      }
   }

   public void removeCirclet(ClanMember member, int castleId) {
      if (member != null) {
         Player player = member.getPlayerInstance();
         int circletId = this.getCircletByCastleId(castleId);
         if (circletId != 0) {
            if (player != null) {
               try {
                  ItemInstance circlet = player.getInventory().getItemByItemId(circletId);
                  if (circlet != null) {
                     if (circlet.isEquipped()) {
                        player.getInventory().unEquipItemInSlot(circlet.getLocationSlot());
                     }

                     player.destroyItemByItemId("CastleCircletRemoval", circletId, 1L, player, true);
                  }

                  return;
               } catch (NullPointerException var20) {
               }
            }

            try (Connection con = DatabaseFactory.getInstance().getConnection()) {
               PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE owner_id = ? and item_id = ?");
               statement.setInt(1, member.getObjectId());
               statement.setInt(2, circletId);
               statement.execute();
               statement.close();
            } catch (Exception var19) {
               _log.log(Level.WARNING, "Failed to remove castle circlets offline for player " + member.getName() + ": " + var19.getMessage(), (Throwable)var19);
            }
         }
      }
   }

   @Override
   public void loadInstances() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT id FROM castle ORDER BY id");
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            this.getCastles().add(new Castle(rs.getInt("id")));
         }

         rs.close();
         statement.close();
         _log.info(this.getClass().getSimpleName() + ": Loaded: " + this.getCastles().size() + " castles");
      } catch (Exception var15) {
         _log.log(Level.WARNING, "Exception: loadCastleData(): " + var15.getMessage(), (Throwable)var15);
      }
   }

   @Override
   public void updateReferences() {
   }

   @Override
   public void activateInstances() {
      for(Castle castle : this._castles) {
         castle.activateInstance();
      }
   }

   private static class SingletonHolder {
      protected static final CastleManager _instance = new CastleManager();
   }
}
