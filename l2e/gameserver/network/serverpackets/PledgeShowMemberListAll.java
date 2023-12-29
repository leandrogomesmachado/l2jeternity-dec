package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.actor.Player;

public class PledgeShowMemberListAll extends GameServerPacket {
   private final Clan _clan;
   private final Player _activeChar;
   private final ClanMember[] _members;
   private int _pledgeType;

   public PledgeShowMemberListAll(Clan clan, Player activeChar) {
      this._clan = clan;
      this._activeChar = activeChar;
      this._members = this._clan.getMembers();
   }

   @Override
   protected final void writeImpl() {
      this._pledgeType = 0;
      this.writePledge(0);

      for(Clan.SubPledge subPledge : this._clan.getAllSubPledges()) {
         this._activeChar.sendPacket(new PledgeReceiveSubPledgeCreated(subPledge, this._clan));
      }

      for(ClanMember m : this._members) {
         if (m.getPledgeType() != 0) {
            this._activeChar.sendPacket(new PledgeShowMemberListAdd(m));
         }
      }

      this._activeChar.sendUserInfo();
   }

   void writePledge(int mainOrSubpledge) {
      this.writeD(mainOrSubpledge);
      this.writeD(this._clan.getId());
      this.writeD(this._pledgeType);
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
      this.writeD(this._clan.getSubPledgeMembersCount(this._pledgeType));

      for(ClanMember m : this._members) {
         if (m.getPledgeType() == this._pledgeType) {
            this.writeS(m.getName());
            this.writeD(m.getLevel());
            this.writeD(m.getClassId());
            Player player;
            if ((player = m.getPlayerInstance()) != null) {
               this.writeD(player.getAppearance().getSex() ? 1 : 0);
               this.writeD(player.getRace().ordinal());
            } else {
               this.writeD(1);
               this.writeD(1);
            }

            this.writeD(m.isOnline() ? m.getObjectId() : 0);
            this.writeD(m.getSponsor() != 0 ? 1 : 0);
         }
      }
   }
}
