package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.ClanMember;

public class PledgeReceivePowerInfo extends GameServerPacket {
   private final ClanMember _member;

   public PledgeReceivePowerInfo(ClanMember member) {
      this._member = member;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._member.getPowerGrade());
      this.writeS(this._member.getName());
      this.writeD(this._member.getClan().getRankPrivs(this._member.getPowerGrade()));
   }
}
