package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestPledgeSetAcademyMaster extends GameClientPacket {
   private String _currPlayerName;
   private int _set;
   private String _targetPlayerName;

   @Override
   protected void readImpl() {
      this._set = this.readD();
      this._currPlayerName = this.readS();
      this._targetPlayerName = this.readS();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      Clan clan = activeChar.getClan();
      if (clan != null) {
         if ((activeChar.getClanPrivileges() & 256) != 256) {
            activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_DISMISS_AN_APPRENTICE);
         } else {
            ClanMember currentMember = clan.getClanMember(this._currPlayerName);
            ClanMember targetMember = clan.getClanMember(this._targetPlayerName);
            if (currentMember != null && targetMember != null) {
               ClanMember apprenticeMember;
               ClanMember sponsorMember;
               if (currentMember.getPledgeType() == -1) {
                  apprenticeMember = currentMember;
                  sponsorMember = targetMember;
               } else {
                  apprenticeMember = targetMember;
                  sponsorMember = currentMember;
               }

               Player apprentice = apprenticeMember.getPlayerInstance();
               Player sponsor = sponsorMember.getPlayerInstance();
               SystemMessage sm = null;
               if (this._set == 0) {
                  if (apprentice != null) {
                     apprentice.setSponsor(0);
                  } else {
                     apprenticeMember.setApprenticeAndSponsor(0, 0);
                  }

                  if (sponsor != null) {
                     sponsor.setApprentice(0);
                  } else {
                     sponsorMember.setApprenticeAndSponsor(0, 0);
                  }

                  apprenticeMember.saveApprenticeAndSponsor(0, 0);
                  sponsorMember.saveApprenticeAndSponsor(0, 0);
                  sm = SystemMessage.getSystemMessage(SystemMessageId.S2_CLAN_MEMBER_C1_APPRENTICE_HAS_BEEN_REMOVED);
               } else {
                  if (apprenticeMember.getSponsor() != 0
                     || sponsorMember.getApprentice() != 0
                     || apprenticeMember.getApprentice() != 0
                     || sponsorMember.getSponsor() != 0) {
                     activeChar.sendMessage("Remove previous connections first.");
                     return;
                  }

                  if (apprentice != null) {
                     apprentice.setSponsor(sponsorMember.getObjectId());
                  } else {
                     apprenticeMember.setApprenticeAndSponsor(0, sponsorMember.getObjectId());
                  }

                  if (sponsor != null) {
                     sponsor.setApprentice(apprenticeMember.getObjectId());
                  } else {
                     sponsorMember.setApprenticeAndSponsor(apprenticeMember.getObjectId(), 0);
                  }

                  apprenticeMember.saveApprenticeAndSponsor(0, sponsorMember.getObjectId());
                  sponsorMember.saveApprenticeAndSponsor(apprenticeMember.getObjectId(), 0);
                  sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HAS_BEEN_DESIGNATED_AS_APPRENTICE_OF_CLAN_MEMBER_S1);
               }

               sm.addString(sponsorMember.getName());
               sm.addString(apprenticeMember.getName());
               if (sponsor != activeChar && sponsor != apprentice) {
                  activeChar.sendPacket(sm);
               }

               if (sponsor != null) {
                  sponsor.sendPacket(sm);
               }

               if (apprentice != null) {
                  apprentice.sendPacket(sm);
               }
            }
         }
      }
   }
}
