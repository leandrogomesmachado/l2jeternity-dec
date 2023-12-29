package l2e.gameserver.network.clientpackets;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import l2e.commons.util.Broadcast;
import l2e.gameserver.Config;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CastleSiegeInfo;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class RequestSetCastleSiegeTime extends GameClientPacket {
   private int _castleId;
   private long _time;

   @Override
   protected void readImpl() {
      this._castleId = this.readD();
      this._time = (long)this.readD();
      this._time *= 1000L;
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      Castle castle = CastleManager.getInstance().getCastleById(this._castleId);
      if (activeChar != null && castle != null) {
         if (castle.getOwnerId() > 0 && castle.getOwnerId() != activeChar.getClanId()) {
            _log.log(
               Level.WARNING,
               this.getType()
                  + ": activeChar: "
                  + activeChar
                  + " castle: "
                  + castle
                  + " castleId: "
                  + this._castleId
                  + " is trying to change siege date of not his own castle!"
            );
         } else if (!activeChar.isClanLeader()) {
            _log.log(
               Level.WARNING,
               this.getType()
                  + ": activeChar: "
                  + activeChar
                  + " castle: "
                  + castle
                  + " castleId: "
                  + this._castleId
                  + " is trying to change siege date but is not clan leader!"
            );
         } else {
            if (!castle.getIsTimeRegistrationOver() && !castle.getIsTimeRegistrationOver()) {
               if (isSiegeTimeValid(castle.getSiegeDate().getTimeInMillis(), this._time)) {
                  castle.getSiegeDate().setTimeInMillis(this._time);
                  castle.setIsTimeRegistrationOver(true);
                  castle.getSiege().saveSiegeDate();
                  SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S1_ANNOUNCED_SIEGE_TIME);
                  msg.addCastleId(this._castleId);
                  Broadcast.toAllOnlinePlayers(msg);
                  activeChar.sendPacket(new CastleSiegeInfo(castle));
               } else {
                  _log.log(
                     Level.WARNING,
                     this.getType()
                        + ": activeChar: "
                        + activeChar
                        + " castle: "
                        + castle
                        + " castleId: "
                        + this._castleId
                        + " is trying to an invalid time ("
                        + new Date(this._time)
                        + " !"
                  );
               }
            } else {
               _log.log(
                  Level.WARNING,
                  this.getType()
                     + ": activeChar: "
                     + activeChar
                     + " castle: "
                     + castle
                     + " castleId: "
                     + this._castleId
                     + " is trying to change siege date but currently not possible!"
               );
            }
         }
      } else {
         _log.log(Level.WARNING, this.getType() + ": activeChar: " + activeChar + " castle: " + castle + " castleId: " + this._castleId);
      }
   }

   private static boolean isSiegeTimeValid(long siegeDate, long choosenDate) {
      Calendar cal1 = Calendar.getInstance();
      cal1.setTimeInMillis(siegeDate);
      cal1.set(12, 0);
      cal1.set(13, 0);
      Calendar cal2 = Calendar.getInstance();
      cal2.setTimeInMillis(choosenDate);

      for(int hour : Config.SIEGE_HOUR_LIST) {
         cal1.set(11, hour);
         if (isEqual(cal1, cal2, 1, 2, 5, 10, 12, 13)) {
            return true;
         }
      }

      return false;
   }

   private static boolean isEqual(Calendar cal1, Calendar cal2, int... fields) {
      for(int field : fields) {
         if (cal1.get(field) != cal2.get(field)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public String getType() {
      return this.getClass().getSimpleName();
   }
}
