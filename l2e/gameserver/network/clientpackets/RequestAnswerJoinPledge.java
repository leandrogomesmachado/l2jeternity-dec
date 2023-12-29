package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.JoinPledge;
import l2e.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListAdd;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListAll;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestAnswerJoinPledge extends GameClientPacket {
   private int _answer;

   @Override
   protected void readImpl() {
      this._answer = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         Player requestor = activeChar.getRequest().getPartner();
         if (requestor != null) {
            if (this._answer == 0) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_NOT_RESPOND_TO_S1_CLAN_INVITATION);
               sm.addString(requestor.getName());
               activeChar.sendPacket(sm);
               SystemMessage var6 = null;
               var6 = SystemMessage.getSystemMessage(SystemMessageId.S1_DID_NOT_RESPOND_TO_CLAN_INVITATION);
               var6.addString(activeChar.getName());
               requestor.sendPacket(var6);
               var6 = null;
            } else {
               if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinPledge)) {
                  return;
               }

               RequestJoinPledge requestPacket = (RequestJoinPledge)requestor.getRequest().getRequestPacket();
               Clan clan = requestor.getClan();
               if (clan.checkClanJoinCondition(requestor, activeChar, requestPacket.getPledgeType())) {
                  activeChar.sendPacket(new JoinPledge(requestor.getClanId()));
                  activeChar.setPledgeType(requestPacket.getPledgeType());
                  if (requestPacket.getPledgeType() == -1) {
                     activeChar.setPowerGrade(9);
                     activeChar.setLvlJoinedAcademy(activeChar.getLevel());
                  } else {
                     activeChar.setPowerGrade(6);
                  }

                  clan.addClanMember(activeChar);
                  activeChar.setClanPrivileges(activeChar.getClan().getRankPrivs(activeChar.getPowerGrade()));
                  activeChar.sendPacket(SystemMessageId.ENTERED_THE_CLAN);
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_JOINED_CLAN);
                  sm.addString(activeChar.getName());
                  clan.broadcastToOnlineMembers(sm);
                  SystemMessage var10 = null;
                  if (activeChar.getClan().getCastleId() > 0) {
                     CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).giveResidentialSkills(activeChar);
                  }

                  if (activeChar.getClan().getFortId() > 0) {
                     FortManager.getInstance().getFortByOwner(activeChar.getClan()).giveResidentialSkills(activeChar);
                  }

                  activeChar.sendSkillList(false);
                  clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(activeChar), activeChar);
                  clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
                  activeChar.sendPacket(new PledgeShowMemberListAll(clan, activeChar));
                  activeChar.setClanJoinExpiryTime(0L);
                  activeChar.broadcastCharInfo();
               }
            }

            activeChar.getRequest().onRequestResponse();
         }
      }
   }
}
