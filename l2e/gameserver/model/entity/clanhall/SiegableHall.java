package l2e.gameserver.model.entity.clanhall;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.logging.Level;
import l2e.commons.util.Util;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.SiegeClan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.entity.ClanHall;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.zone.type.SiegableHallZone;
import l2e.gameserver.model.zone.type.SiegeZone;
import l2e.gameserver.network.serverpackets.CastleSiegeInfo;

public final class SiegableHall extends ClanHall {
   private static final String SQL_SAVE = "UPDATE siegable_clanhall SET ownerId=?, nextSiege=? WHERE id=?";
   private final int _grade;
   private Calendar _nextSiege;
   private final long _siegeLength;
   private final int[] _scheduleConfig = new int[]{7, 0, 0, 12, 0};
   private SiegeStatus _status = SiegeStatus.REGISTERING;
   private SiegeZone _siegeZone;
   private ClanHallSiegeEngine _siege;

   public SiegableHall(StatsSet set) {
      super(set);
      this._grade = set.getInteger("grade");
      this._siegeLength = set.getLong("siegeLenght");
      if (this.getOwnerId() != 0) {
         this.loadFunctions();
      }

      String[] rawSchConfig = set.getString("scheduleConfig").split(";");
      if (rawSchConfig.length == 5) {
         for(int i = 0; i < 5; ++i) {
            try {
               this._scheduleConfig[i] = Integer.parseInt(rawSchConfig[i]);
            } catch (Exception var5) {
               _log.warning("SiegableHall - " + Util.clanHallName(null, this.getId()) + ": Wrong schedule_config parameters!");
            }
         }
      } else {
         _log.warning(Util.clanHallName(null, this.getId()) + ": Wrong schedule_config value in siegable_halls table, using default (7 days)");
      }

      this._nextSiege = Calendar.getInstance();
      long nextSiege = set.getLong("nextSiege");
      if (nextSiege - System.currentTimeMillis() < 0L) {
         this.updateNextSiege();
      } else {
         this._nextSiege.setTimeInMillis(nextSiege);
      }
   }

   public void spawnDoor() {
      this.spawnDoor(false);
   }

   public void spawnDoor(boolean isDoorWeak) {
      for(DoorInstance door : this.getDoors()) {
         if (door.isDead()) {
            door.doRevive();
            if (isDoorWeak) {
               door.setCurrentHp(door.getMaxHp() / 2.0);
            } else {
               door.setCurrentHp(door.getMaxHp());
            }
         }

         if (door.getOpen()) {
            door.closeMe();
         }
      }
   }

   @Override
   public final void updateDb() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE siegable_clanhall SET ownerId=?, nextSiege=? WHERE id=?");
      ) {
         statement.setInt(1, this.getOwnerId());
         statement.setLong(2, this.getNextSiegeTime());
         statement.setInt(3, this.getId());
         statement.execute();
      } catch (Exception var33) {
         _log.log(Level.WARNING, "Exception: SiegableHall.updateDb(): " + var33.getMessage(), (Throwable)var33);
      }
   }

   public final void setSiege(ClanHallSiegeEngine siegable) {
      this._siege = siegable;
      this._siegeZone.setSiegeInstance(siegable);
   }

   public final ClanHallSiegeEngine getSiege() {
      return this._siege;
   }

   public final Calendar getSiegeDate() {
      return this._nextSiege;
   }

   public final long getNextSiegeTime() {
      return this._nextSiege.getTimeInMillis();
   }

   public long getSiegeLenght() {
      return this._siegeLength;
   }

   public final void setNextSiegeDate(long date) {
      this._nextSiege.setTimeInMillis(date);
   }

   public final void setNextSiegeDate(Calendar c) {
      this._nextSiege = c;
   }

   public final void updateNextSiege() {
      Calendar c = Calendar.getInstance();
      c.add(6, this._scheduleConfig[0]);
      c.add(2, this._scheduleConfig[1]);
      c.add(1, this._scheduleConfig[2]);
      c.set(11, this._scheduleConfig[3]);
      c.set(12, this._scheduleConfig[4]);
      c.set(13, 0);
      this.setNextSiegeDate(c);
      this.updateDb();
   }

   public final void addAttacker(Clan clan) {
      if (this.getSiege() != null) {
         this.getSiege().getAttackers().put(clan.getId(), new SiegeClan(clan.getId(), SiegeClan.SiegeClanType.ATTACKER));
      }
   }

   public final void removeAttacker(Clan clan) {
      if (this.getSiege() != null) {
         this.getSiege().getAttackers().remove(clan.getId());
      }
   }

   public final boolean isRegistered(Clan clan) {
      return this.getSiege() == null ? false : this.getSiege().checkIsAttacker(clan);
   }

   public SiegeStatus getSiegeStatus() {
      return this._status;
   }

   public final boolean isRegistering() {
      return this._status == SiegeStatus.REGISTERING;
   }

   public final boolean isInSiege() {
      return this._status == SiegeStatus.RUNNING;
   }

   public final boolean isWaitingBattle() {
      return this._status == SiegeStatus.WAITING_BATTLE;
   }

   public final void updateSiegeStatus(SiegeStatus status) {
      this._status = status;
   }

   public final SiegeZone getSiegeZone() {
      return this._siegeZone;
   }

   public final void setSiegeZone(SiegeZone zone) {
      this._siegeZone = zone;
   }

   public final void updateSiegeZone(boolean active) {
      this._siegeZone.setIsActive(active);
   }

   public final void showSiegeInfo(Player player) {
      player.sendPacket(new CastleSiegeInfo(this));
   }

   @Override
   public final int getGrade() {
      return this._grade;
   }

   @Override
   public final boolean isSiegableHall() {
      return true;
   }

   public SiegableHallZone getZone() {
      return (SiegableHallZone)super.getZone();
   }
}
