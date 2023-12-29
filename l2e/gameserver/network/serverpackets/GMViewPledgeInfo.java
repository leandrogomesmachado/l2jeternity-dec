package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.actor.Player;

public class GMViewPledgeInfo extends GameServerPacket {
   private final Clan _clan;
   private final Player _activeChar;

   public GMViewPledgeInfo(Clan clan, Player activeChar) {
      this._clan = clan;
      this._activeChar = activeChar;
   }

   @Override
   protected final void writeImpl() {
      this.writeS(this._activeChar.getName());
      this.writeD(this._clan.getId());
      this.writeD(0);
      this.writeS(this._clan.getName());
      this.writeS(this._clan.getLeaderName());
      this.writeD(this._clan.getCrestId());
      this.writeD(this._clan.getLevel());
      this.writeD(this._clan.getCastleId());
      this.writeD(this._clan.getHideoutId());
      this.writeD(this._clan.getFortId());
      this.writeD(this._clan.getRank());
      this.writeD(this._clan.getReputationScore());
      this.writeD(0);
      this.writeD(0);
      this.writeD(this._clan.getAllyId());
      this.writeS(this._clan.getAllyName());
      this.writeD(this._clan.getAllyCrestId());
      this.writeD(this._clan.isAtWar() ? 1 : 0);
      this.writeD(0);
      this.writeD(this._clan.getMembers().length);

      for(ClanMember member : this._clan.getMembers()) {
         if (member != null) {
            this.writeS(member.getName());
            this.writeD(member.getLevel());
            this.writeD(member.getClassId());
            this.writeD(member.getSex() ? 1 : 0);
            this.writeD(member.getRaceOrdinal());
            this.writeD(member.isOnline() ? member.getObjectId() : 0);
            this.writeD(member.getSponsor() != 0 ? 1 : 0);
         }
      }
   }
}
