package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.service.academy.AcademyList;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestWithDrawalPledge extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (activeChar.getClan() == null) {
            activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
         } else if (activeChar.isClanLeader()) {
            activeChar.sendPacket(SystemMessageId.CLAN_LEADER_CANNOT_WITHDRAW);
         } else if (activeChar.isInCombat()) {
            activeChar.sendPacket(SystemMessageId.YOU_CANNOT_LEAVE_DURING_COMBAT);
         } else {
            Clan clan = activeChar.getClan();
            clan.removeClanMember(activeChar.getObjectId(), System.currentTimeMillis() + (long)Config.ALT_CLAN_JOIN_DAYS * 3600000L);
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WITHDRAWN_FROM_THE_CLAN);
            sm.addString(activeChar.getName());
            clan.broadcastToOnlineMembers(sm);
            SystemMessage var4 = null;
            clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(activeChar.getName()));
            activeChar.sendPacket(SystemMessageId.YOU_HAVE_WITHDRAWN_FROM_CLAN);
            activeChar.sendPacket(SystemMessageId.YOU_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN);
            AcademyList.removeAcademyFromDB(clan, activeChar.getObjectId(), false, true);
         }
      }
   }
}
