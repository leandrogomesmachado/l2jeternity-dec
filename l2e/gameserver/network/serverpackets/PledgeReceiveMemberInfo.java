package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.ClanMember;

public class PledgeReceiveMemberInfo extends GameServerPacket {
   private final ClanMember _member;

   public PledgeReceiveMemberInfo(ClanMember member) {
      this._member = member;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._member.getPledgeType());
      this.writeS(this._member.getName());
      this.writeS(this._member.getTitle());
      this.writeD(this._member.getPowerGrade());
      if (this._member.getPledgeType() != 0) {
         this.writeS(this._member.getClan().getSubPledge(this._member.getPledgeType()).getName());
      } else {
         this.writeS(this._member.getClan().getName());
      }

      this.writeS(this._member.getApprenticeOrSponsorName());
   }
}
