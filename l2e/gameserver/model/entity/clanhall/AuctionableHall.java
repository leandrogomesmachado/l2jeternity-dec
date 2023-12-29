package l2e.gameserver.model.entity.clanhall;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.AuctionManager;
import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.entity.ClanHall;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class AuctionableHall extends ClanHall {
   protected long _paidUntil;
   private final int _grade;
   protected boolean _paid;
   private final int _lease;
   protected final int _chRate = 604800000;

   public AuctionableHall(StatsSet set) {
      super(set);
      this._paidUntil = set.getLong("paidUntil");
      this._grade = set.getInteger("grade");
      this._paid = set.getBool("paid");
      this._lease = set.getInteger("lease");
      if (this.getOwnerId() != 0) {
         this._isFree = false;
         this.initialyzeTask(false);
         this.loadFunctions();
      }
   }

   public final boolean getPaid() {
      return this._paid;
   }

   @Override
   public final int getLease() {
      return this._lease;
   }

   @Override
   public final long getPaidUntil() {
      return this._paidUntil;
   }

   @Override
   public final int getGrade() {
      return this._grade;
   }

   @Override
   public final void free() {
      super.free();
      this._paidUntil = 0L;
      this._paid = false;
   }

   @Override
   public final void setOwner(Clan clan) {
      super.setOwner(clan);
      this._paidUntil = System.currentTimeMillis();
      this.initialyzeTask(true);
   }

   private final void initialyzeTask(boolean forced) {
      long currentTime = System.currentTimeMillis();
      if (this._paidUntil > currentTime) {
         ThreadPoolManager.getInstance().schedule(new AuctionableHall.FeeTask(), this._paidUntil - currentTime);
      } else if (!this._paid && !forced) {
         if (System.currentTimeMillis() + 86400000L <= this._paidUntil + 604800000L) {
            ThreadPoolManager.getInstance().schedule(new AuctionableHall.FeeTask(), System.currentTimeMillis() + 86400000L);
         } else {
            ThreadPoolManager.getInstance().schedule(new AuctionableHall.FeeTask(), this._paidUntil + 604800000L - System.currentTimeMillis());
         }
      } else {
         ThreadPoolManager.getInstance().schedule(new AuctionableHall.FeeTask(), 0L);
      }
   }

   @Override
   public final void updateDb() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE clanhall SET ownerId=?, paidUntil=?, paid=? WHERE id=?");
      ) {
         statement.setInt(1, this.getOwnerId());
         statement.setLong(2, this.getPaidUntil());
         statement.setInt(3, this.getPaid() ? 1 : 0);
         statement.setInt(4, this.getId());
         statement.execute();
      } catch (Exception var33) {
         _log.log(Level.WARNING, "Exception: updateOwnerInDB(Clan clan): " + var33.getMessage(), (Throwable)var33);
      }
   }

   protected class FeeTask implements Runnable {
      @Override
      public void run() {
         try {
            long _time = System.currentTimeMillis();
            if (AuctionableHall.this.isFree()) {
               return;
            }

            if (AuctionableHall.this._paidUntil > _time) {
               ThreadPoolManager.getInstance().schedule(AuctionableHall.this.new FeeTask(), AuctionableHall.this._paidUntil - _time);
               return;
            }

            Clan Clan = ClanHolder.getInstance().getClan(AuctionableHall.this.getOwnerId());
            if (ClanHolder.getInstance().getClan(AuctionableHall.this.getOwnerId()).getWarehouse().getAdena() >= (long)AuctionableHall.this.getLease()) {
               if (AuctionableHall.this._paidUntil != 0L) {
                  while(AuctionableHall.this._paidUntil <= _time) {
                     AuctionableHall.this._paidUntil += 604800000L;
                  }
               } else {
                  AuctionableHall.this._paidUntil = _time + 604800000L;
               }

               ClanHolder.getInstance()
                  .getClan(AuctionableHall.this.getOwnerId())
                  .getWarehouse()
                  .destroyItemByItemId("CH_rental_fee", 57, (long)AuctionableHall.this.getLease(), null, null);
               ThreadPoolManager.getInstance().schedule(AuctionableHall.this.new FeeTask(), AuctionableHall.this._paidUntil - _time);
               AuctionableHall.this._paid = true;
               AuctionableHall.this.updateDb();
            } else {
               AuctionableHall.this._paid = false;
               if (_time > AuctionableHall.this._paidUntil + 604800000L) {
                  if (ClanHallManager.getInstance().loaded()) {
                     AuctionManager.getInstance().initNPC(AuctionableHall.this.getId());
                     ClanHallManager.getInstance().setFree(AuctionableHall.this.getId());
                     Clan.broadcastToOnlineMembers(
                        SystemMessage.getSystemMessage(
                           SystemMessageId.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED
                        )
                     );
                  } else {
                     ThreadPoolManager.getInstance().schedule(AuctionableHall.this.new FeeTask(), 3000L);
                  }
               } else {
                  AuctionableHall.this.updateDb();
                  SystemMessage sm = SystemMessage.getSystemMessage(
                     SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW
                  );
                  sm.addNumber(AuctionableHall.this.getLease());
                  Clan.broadcastToOnlineMembers(sm);
                  if (_time + 86400000L <= AuctionableHall.this._paidUntil + 604800000L) {
                     ThreadPoolManager.getInstance().schedule(AuctionableHall.this.new FeeTask(), _time + 86400000L);
                  } else {
                     ThreadPoolManager.getInstance().schedule(AuctionableHall.this.new FeeTask(), AuctionableHall.this._paidUntil + 604800000L - _time);
                  }
               }
            }
         } catch (Exception var5) {
            ClanHall._log.log(Level.SEVERE, "", (Throwable)var5);
         }
      }
   }
}
