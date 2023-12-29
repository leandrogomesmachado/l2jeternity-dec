package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.actor.Player;

public final class RequestPledgeSetMemberPowerGrade extends GameClientPacket {
   private String _member;
   private int _powerGrade;

   @Override
   protected void readImpl() {
      this._member = this.readS();
      this._powerGrade = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         Clan clan = activeChar.getClan();
         if (clan != null) {
            if ((activeChar.getClanPrivileges() & 16) == 16) {
               ClanMember member = clan.getClanMember(this._member);
               if (member != null) {
                  if (member.getObjectId() != clan.getLeaderId()) {
                     if (member.getPledgeType() == -1) {
                        activeChar.sendMessage("You cannot change academy member grade");
                     } else {
                        member.setPowerGrade(this._powerGrade);
                        clan.broadcastClanStatus();
                     }
                  }
               }
            }
         }
      }
   }
}
