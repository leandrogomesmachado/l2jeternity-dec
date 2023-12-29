package l2e.gameserver.network.serverpackets;

import java.util.Collection;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.SiegeClan;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.clanhall.SiegableHall;

public final class CastleSiegeAttackerList extends GameServerPacket {
   private Castle _castle;
   private SiegableHall _hall;

   public CastleSiegeAttackerList(Castle castle) {
      this._castle = castle;
   }

   public CastleSiegeAttackerList(SiegableHall hall) {
      this._hall = hall;
   }

   @Override
   protected final void writeImpl() {
      if (this._castle != null) {
         this.writeD(this._castle.getId());
         this.writeD(0);
         this.writeD(1);
         this.writeD(0);
         int size = this._castle.getSiege().getAttackerClans().size();
         if (size > 0) {
            this.writeD(size);
            this.writeD(size);

            for(SiegeClan siegeclan : this._castle.getSiege().getAttackerClans()) {
               Clan clan = ClanHolder.getInstance().getClan(siegeclan.getClanId());
               if (clan != null) {
                  this.writeD(clan.getId());
                  this.writeS(clan.getName());
                  this.writeS(clan.getLeaderName());
                  this.writeD(clan.getCrestId());
                  this.writeD(0);
                  this.writeD(clan.getAllyId());
                  this.writeS(clan.getAllyName());
                  this.writeS("");
                  this.writeD(clan.getAllyCrestId());
               }
            }
         } else {
            this.writeD(0);
            this.writeD(0);
         }
      } else {
         this.writeD(this._hall.getId());
         this.writeD(0);
         this.writeD(1);
         this.writeD(0);
         Collection<SiegeClan> attackers = this._hall.getSiege().getAttackerClans();
         int size = attackers.size();
         if (size > 0) {
            this.writeD(size);
            this.writeD(size);

            for(SiegeClan sClan : attackers) {
               Clan clan = ClanHolder.getInstance().getClan(sClan.getClanId());
               if (clan != null) {
                  this.writeD(clan.getId());
                  this.writeS(clan.getName());
                  this.writeS(clan.getLeaderName());
                  this.writeD(clan.getCrestId());
                  this.writeD(0);
                  this.writeD(clan.getAllyId());
                  this.writeS(clan.getAllyName());
                  this.writeS("");
                  this.writeD(clan.getAllyCrestId());
               }
            }
         } else {
            this.writeD(0);
            this.writeD(0);
         }
      }
   }
}
