package l2e.gameserver.network.serverpackets;

import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.SiegeClan;
import l2e.gameserver.model.entity.Castle;

public final class CastleSiegeDefenderList extends GameServerPacket {
   private final Castle _castle;

   public CastleSiegeDefenderList(Castle castle) {
      this._castle = castle;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._castle.getId());
      this.writeD(0);
      this.writeD(1);
      this.writeD(0);
      int size = this._castle.getSiege().getDefenderClans().size() + this._castle.getSiege().getDefenderWaitingClans().size();
      if (size > 0) {
         this.writeD(size);
         this.writeD(size);

         for(SiegeClan siegeclan : this._castle.getSiege().getDefenderClans()) {
            Clan clan = ClanHolder.getInstance().getClan(siegeclan.getClanId());
            if (clan != null) {
               this.writeD(clan.getId());
               this.writeS(clan.getName());
               this.writeS(clan.getLeaderName());
               this.writeD(clan.getCrestId());
               this.writeD(0);
               switch(siegeclan.getType()) {
                  case OWNER:
                     this.writeD(1);
                     break;
                  case DEFENDER_PENDING:
                     this.writeD(2);
                     break;
                  case DEFENDER:
                     this.writeD(3);
                     break;
                  default:
                     this.writeD(0);
               }

               this.writeD(clan.getAllyId());
               this.writeS(clan.getAllyName());
               this.writeS("");
               this.writeD(clan.getAllyCrestId());
            }
         }

         for(SiegeClan siegeclan : this._castle.getSiege().getDefenderWaitingClans()) {
            Clan clan = ClanHolder.getInstance().getClan(siegeclan.getClanId());
            this.writeD(clan.getId());
            this.writeS(clan.getName());
            this.writeS(clan.getLeaderName());
            this.writeD(clan.getCrestId());
            this.writeD(0);
            this.writeD(2);
            this.writeD(clan.getAllyId());
            this.writeS(clan.getAllyName());
            this.writeS("");
            this.writeD(clan.getAllyCrestId());
         }
      } else {
         this.writeD(0);
         this.writeD(0);
      }
   }
}
