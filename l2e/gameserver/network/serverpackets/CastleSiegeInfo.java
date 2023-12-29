package l2e.gameserver.network.serverpackets;

import java.util.Calendar;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.ClanHall;

public class CastleSiegeInfo extends GameServerPacket {
   private Castle _castle;
   private ClanHall _hall;

   public CastleSiegeInfo(Castle castle) {
      this._castle = castle;
   }

   public CastleSiegeInfo(ClanHall hall) {
      this._hall = hall;
   }

   @Override
   protected final void writeImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (this._castle != null) {
            this.writeD(this._castle.getId());
            int ownerId = this._castle.getOwnerId();
            this.writeD(ownerId == activeChar.getClanId() && activeChar.isClanLeader() ? 1 : 0);
            this.writeD(ownerId);
            if (ownerId > 0) {
               Clan owner = ClanHolder.getInstance().getClan(ownerId);
               if (owner != null) {
                  this.writeS(owner.getName());
                  this.writeS(owner.getLeaderName());
                  this.writeD(owner.getAllyId());
                  this.writeS(owner.getAllyName());
               } else {
                  _log.warning("Null owner for castle: " + this._castle.getName());
               }
            } else {
               this.writeS("");
               this.writeS("");
               this.writeD(0);
               this.writeS("");
            }

            this.writeD((int)(System.currentTimeMillis() / 1000L));
            if (!this._castle.getIsTimeRegistrationOver() && activeChar.isClanLeader() && activeChar.getClanId() == this._castle.getOwnerId()) {
               Calendar cal = Calendar.getInstance();
               cal.setTimeInMillis(this._castle.getSiegeDate().getTimeInMillis());
               cal.set(12, 0);
               cal.set(13, 0);
               this.writeD(0);
               this.writeD(Config.SIEGE_HOUR_LIST.size());

               for(int hour : Config.SIEGE_HOUR_LIST) {
                  cal.set(11, hour);
                  this.writeD((int)(cal.getTimeInMillis() / 1000L));
               }
            } else {
               this.writeD((int)(this._castle.getSiegeDate().getTimeInMillis() / 1000L));
               this.writeD(0);
            }
         } else {
            this.writeD(this._hall.getId());
            int ownerId = this._hall.getOwnerId();
            this.writeD(ownerId == activeChar.getClanId() && activeChar.isClanLeader() ? 1 : 0);
            this.writeD(ownerId);
            if (ownerId > 0) {
               Clan owner = ClanHolder.getInstance().getClan(ownerId);
               if (owner != null) {
                  this.writeS(owner.getName());
                  this.writeS(owner.getLeaderName());
                  this.writeD(owner.getAllyId());
                  this.writeS(owner.getAllyName());
               } else {
                  _log.warning("Null owner for siegable hall: " + Util.clanHallName(null, this._hall.getId()));
               }
            } else {
               this.writeS("");
               this.writeS("");
               this.writeD(0);
               this.writeS("");
            }

            this.writeD((int)(Calendar.getInstance().getTimeInMillis() / 1000L));
            this.writeD((int)(CHSiegeManager.getInstance().getSiegableHall(this._hall.getId()).getNextSiegeTime() / 1000L));
            this.writeD(0);
         }
      }
   }
}
