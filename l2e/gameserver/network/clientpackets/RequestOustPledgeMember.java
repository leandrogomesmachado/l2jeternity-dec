package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.service.academy.AcademyList;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestOustPledgeMember extends GameClientPacket {
   private String _target;

   @Override
   protected void readImpl() {
      this._target = this.readS();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (activeChar.getClan() == null) {
            activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
         } else if ((activeChar.getClanPrivileges() & 64) != 64) {
            activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
         } else if (activeChar.getName().equalsIgnoreCase(this._target)) {
            activeChar.sendPacket(SystemMessageId.YOU_CANNOT_DISMISS_YOURSELF);
         } else {
            Clan clan = activeChar.getClan();
            ClanMember member = clan.getClanMember(this._target);
            if (member == null) {
               _log.warning("Target (" + this._target + ") is not member of the clan");
            } else if (member.isOnline() && member.getPlayerInstance().isInCombat()) {
               activeChar.sendPacket(SystemMessageId.CLAN_MEMBER_CANNOT_BE_DISMISSED_DURING_COMBAT);
            } else if (AcademyList.isAcademyChar(member.getObjectId()) && !activeChar.isClanLeader()) {
               activeChar.sendPacket(
                  new CreatureSay(activeChar.getObjectId(), 20, "Academy", "Only the clan leader can kick characters recruit by Academy Board Service.")
               );
            } else {
               AcademyList.removeAcademyFromDB(clan, member.getObjectId(), false, false);
               clan.removeClanMember(member.getObjectId(), System.currentTimeMillis() + (long)Config.ALT_CLAN_JOIN_DAYS * 3600000L);
               clan.setCharPenaltyExpiryTime(System.currentTimeMillis() + (long)Config.ALT_CLAN_JOIN_DAYS * 3600000L);
               clan.updateClanInDB();
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED);
               sm.addString(member.getName());
               clan.broadcastToOnlineMembers(sm);
               SystemMessage var6 = null;
               activeChar.sendPacket(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_EXPELLING_CLAN_MEMBER);
               activeChar.sendPacket(SystemMessageId.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER);
               clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(this._target));
               if (member.isOnline()) {
                  Player player = member.getPlayerInstance();
                  player.sendPacket(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED);
               }
            }
         }
      }
   }
}
