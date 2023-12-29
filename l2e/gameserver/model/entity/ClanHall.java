package l2e.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.zone.type.ClanHallZone;
import l2e.gameserver.network.serverpackets.PledgeShowInfoUpdate;

public abstract class ClanHall {
   public static final Logger _log = Logger.getLogger(ClanHall.class.getName());
   private final int _clanHallId;
   private ArrayList<DoorInstance> _doors;
   private int _ownerId;
   private ClanHallZone _zone;
   protected final int _chRate = 604800000;
   protected boolean _isFree = true;
   private final Map<Integer, ClanHall.ClanHallFunction> _functions;
   public static final int FUNC_TELEPORT = 1;
   public static final int FUNC_ITEM_CREATE = 2;
   public static final int FUNC_RESTORE_HP = 3;
   public static final int FUNC_RESTORE_MP = 4;
   public static final int FUNC_RESTORE_EXP = 5;
   public static final int FUNC_SUPPORT = 6;
   public static final int FUNC_DECO_FRONTPLATEFORM = 7;
   public static final int FUNC_DECO_CURTAINS = 8;

   public ClanHall(StatsSet set) {
      this._clanHallId = set.getInteger("id");
      this._ownerId = set.getInteger("ownerId");
      this._functions = new ConcurrentHashMap<>();
      if (this._ownerId > 0) {
         Clan clan = ClanHolder.getInstance().getClan(this._ownerId);
         if (clan != null) {
            clan.setHideoutId(this.getId());
         } else {
            this.free();
         }
      }
   }

   public final int getId() {
      return this._clanHallId;
   }

   public final int getOwnerId() {
      return this._ownerId;
   }

   public final Clan getOwnerClan() {
      return ClanHolder.getInstance().getClan(this.getOwnerId());
   }

   public final ArrayList<DoorInstance> getDoors() {
      if (this._doors == null) {
         this._doors = new ArrayList<>();
      }

      return this._doors;
   }

   public final DoorInstance getDoor(int doorId) {
      if (doorId <= 0) {
         return null;
      } else {
         for(DoorInstance door : this.getDoors()) {
            if (door.getDoorId() == doorId) {
               return door;
            }
         }

         return null;
      }
   }

   public ClanHall.ClanHallFunction getFunction(int type) {
      return this._functions.get(type) != null ? this._functions.get(type) : null;
   }

   public void setZone(ClanHallZone zone) {
      this._zone = zone;
   }

   public boolean checkIfInZone(int x, int y, int z) {
      return this.getZone().isInsideZone(x, y, z);
   }

   public ClanHallZone getZone() {
      return this._zone;
   }

   public void free() {
      this._ownerId = 0;
      this._isFree = true;

      for(Integer fc : this._functions.keySet()) {
         this.removeFunction(fc);
      }

      this._functions.clear();
      this.updateDb();
   }

   public void setOwner(Clan clan) {
      if (this._ownerId <= 0 && clan != null) {
         this._ownerId = clan.getId();
         this._isFree = false;
         clan.setHideoutId(this.getId());
         clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
         this.updateDb();
      }
   }

   public void openCloseDoor(Player activeChar, int doorId, boolean open) {
      if (activeChar != null && activeChar.getClanId() == this.getOwnerId()) {
         this.openCloseDoor(doorId, open);
      }
   }

   public void openCloseDoor(int doorId, boolean open) {
      this.openCloseDoor(this.getDoor(doorId), open);
   }

   public void openCloseDoor(DoorInstance door, boolean open) {
      if (door != null) {
         if (open) {
            door.openMe();
         } else {
            door.closeMe();
         }
      }
   }

   public void openCloseDoors(Player activeChar, boolean open) {
      if (activeChar != null && activeChar.getClanId() == this.getOwnerId()) {
         this.openCloseDoors(open);
      }
   }

   public void openCloseDoors(boolean open) {
      for(DoorInstance door : this.getDoors()) {
         if (door != null) {
            if (open) {
               door.openMe();
            } else {
               door.closeMe();
            }
         }
      }
   }

   public void banishForeigners() {
      if (this._zone != null) {
         this._zone.banishForeigners(this.getOwnerId());
      } else {
         _log.log(
            Level.WARNING, this.getClass().getSimpleName() + ": Zone is null for clan hall: " + this.getId() + " " + Util.clanHallName(null, this.getId())
         );
      }
   }

   protected void loadFunctions() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM clanhall_functions WHERE hall_id = ?");
         statement.setInt(1, this.getId());
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            this._functions
               .put(
                  rs.getInt("type"),
                  new ClanHall.ClanHallFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0, rs.getLong("rate"), rs.getLong("endTime"), true)
               );
         }

         rs.close();
         statement.close();
      } catch (Exception var15) {
         _log.log(Level.SEVERE, "Exception: ClanHall.loadFunctions(): " + var15.getMessage(), (Throwable)var15);
      }
   }

   public void removeFunction(int functionType) {
      this._functions.remove(functionType);

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("DELETE FROM clanhall_functions WHERE hall_id=? AND type=?");
         statement.setInt(1, this.getId());
         statement.setInt(2, functionType);
         statement.execute();
         statement.close();
      } catch (Exception var15) {
         _log.log(Level.SEVERE, "Exception: ClanHall.removeFunctions(int functionType): " + var15.getMessage(), (Throwable)var15);
      }
   }

   public boolean updateFunctions(Player player, int type, int lvl, int lease, long rate, boolean addNew) {
      if (player == null) {
         return false;
      } else if (lease > 0 && !player.destroyItemByItemId("Consume", 57, (long)lease, null, true)) {
         return false;
      } else {
         if (addNew) {
            this._functions.put(type, new ClanHall.ClanHallFunction(type, lvl, lease, 0, rate, 0L, false));
         } else if (lvl == 0 && lease == 0) {
            this.removeFunction(type);
         } else {
            int diffLease = lease - this._functions.get(type).getLease();
            if (diffLease > 0) {
               this._functions.remove(type);
               this._functions.put(type, new ClanHall.ClanHallFunction(type, lvl, lease, 0, rate, -1L, false));
            } else {
               this._functions.get(type).setLease(lease);
               this._functions.get(type).setLvl(lvl);
               this._functions.get(type).dbSave();
            }
         }

         return true;
      }
   }

   public int getGrade() {
      return 0;
   }

   public long getPaidUntil() {
      return 0L;
   }

   public int getLease() {
      return 0;
   }

   public boolean isSiegableHall() {
      return false;
   }

   public boolean isFree() {
      return this._isFree;
   }

   public abstract void updateDb();

   public class ClanHallFunction {
      private final int _type;
      private int _lvl;
      protected int _fee;
      protected int _tempFee;
      private final long _rate;
      private long _endDate;
      protected boolean _inDebt;
      public boolean _cwh;

      public ClanHallFunction(int type, int lvl, int lease, int tempLease, long rate, long time, boolean cwh) {
         this._type = type;
         this._lvl = lvl;
         this._fee = lease;
         this._tempFee = tempLease;
         this._rate = rate;
         this._endDate = time;
         this.initializeTask(cwh);
      }

      public int getType() {
         return this._type;
      }

      public int getLvl() {
         return this._lvl;
      }

      public int getLease() {
         return this._fee;
      }

      public long getRate() {
         return this._rate;
      }

      public long getEndTime() {
         return this._endDate;
      }

      public void setLvl(int lvl) {
         this._lvl = lvl;
      }

      public void setLease(int lease) {
         this._fee = lease;
      }

      public void setEndTime(long time) {
         this._endDate = time;
      }

      private void initializeTask(boolean cwh) {
         if (!ClanHall.this._isFree) {
            long currentTime = System.currentTimeMillis();
            if (this._endDate > currentTime) {
               ThreadPoolManager.getInstance().schedule(new ClanHall.ClanHallFunction.FunctionTask(cwh), this._endDate - currentTime);
            } else {
               ThreadPoolManager.getInstance().schedule(new ClanHall.ClanHallFunction.FunctionTask(cwh), 0L);
            }
         }
      }

      public void dbSave() {
         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement(
               "REPLACE INTO clanhall_functions (hall_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)"
            );
            statement.setInt(1, ClanHall.this.getId());
            statement.setInt(2, this.getType());
            statement.setInt(3, this.getLvl());
            statement.setInt(4, this.getLease());
            statement.setLong(5, this.getRate());
            statement.setLong(6, this.getEndTime());
            statement.execute();
            statement.close();
         } catch (Exception var14) {
            ClanHall._log
               .log(
                  Level.SEVERE,
                  "Exception: ClanHall.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + var14.getMessage(),
                  (Throwable)var14
               );
         }
      }

      private class FunctionTask implements Runnable {
         public FunctionTask(boolean cwh) {
            ClanHallFunction.this._cwh = cwh;
         }

         @Override
         public void run() {
            try {
               if (ClanHall.this._isFree) {
                  return;
               }

               if (ClanHolder.getInstance().getClan(ClanHall.this.getOwnerId()).getWarehouse().getAdena() < (long)ClanHallFunction.this._fee
                  && ClanHallFunction.this._cwh) {
                  ClanHall.this.removeFunction(ClanHallFunction.this.getType());
               } else {
                  int fee = ClanHallFunction.this._fee;
                  if (ClanHallFunction.this.getEndTime() == -1L) {
                     fee = ClanHallFunction.this._tempFee;
                  }

                  ClanHallFunction.this.setEndTime(System.currentTimeMillis() + ClanHallFunction.this.getRate());
                  ClanHallFunction.this.dbSave();
                  if (ClanHallFunction.this._cwh) {
                     ClanHolder.getInstance()
                        .getClan(ClanHall.this.getOwnerId())
                        .getWarehouse()
                        .destroyItemByItemId("CH_function_fee", 57, (long)fee, null, null);
                  }

                  ThreadPoolManager.getInstance().schedule(ClanHallFunction.this.new FunctionTask(true), ClanHallFunction.this.getRate());
               }
            } catch (Exception var2) {
               ClanHall._log.log(Level.SEVERE, "", (Throwable)var2);
            }
         }
      }
   }
}
