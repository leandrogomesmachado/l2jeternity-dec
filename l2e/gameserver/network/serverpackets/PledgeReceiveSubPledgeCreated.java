package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Clan;

public class PledgeReceiveSubPledgeCreated extends GameServerPacket {
   private final Clan.SubPledge _subPledge;
   private final Clan _clan;

   public PledgeReceiveSubPledgeCreated(Clan.SubPledge subPledge, Clan clan) {
      this._subPledge = subPledge;
      this._clan = clan;
   }

   @Override
   protected void writeImpl() {
      this.writeD(1);
      this.writeD(this._subPledge.getId());
      this.writeS(this._subPledge.getName());
      this.writeS(this.getLeaderName());
   }

   private String getLeaderName() {
      int LeaderId = this._subPledge.getLeaderId();
      if (this._subPledge.getId() == -1 || LeaderId == 0) {
         return "";
      } else if (this._clan.getClanMember(LeaderId) == null) {
         _log.warning("SubPledgeLeader: " + LeaderId + " is missing from clan: " + this._clan.getName() + "[" + this._clan.getId() + "]");
         return "";
      } else {
         return this._clan.getClanMember(LeaderId).getName();
      }
   }
}
