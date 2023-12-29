package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.instancemanager.PetitionManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.petition.PetitionMainGroup;
import l2e.gameserver.model.petition.PetitionSection;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestPetition extends GameClientPacket {
   private String _content;
   private int _type;

   @Override
   protected void readImpl() {
      this._content = this.readS();
      this._type = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (!AdminParser.getInstance().isGmOnline(true)) {
            activeChar.sendPacket(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW);
         } else if (!PetitionManager.getInstance().isPetitioningAllowed()) {
            activeChar.sendPacket(SystemMessageId.GAME_CLIENT_UNABLE_TO_CONNECT_TO_PETITION_SERVER);
         } else if (PetitionManager.getInstance().isPlayerPetitionPending(activeChar)) {
            activeChar.sendPacket(SystemMessageId.ONLY_ONE_ACTIVE_PETITION_AT_TIME);
         } else if (PetitionManager.getInstance().getPendingPetitionCount() == Config.MAX_PETITIONS_PENDING) {
            activeChar.sendPacket(SystemMessageId.PETITION_SYSTEM_CURRENT_UNAVAILABLE);
         } else {
            int totalPetitions = PetitionManager.getInstance().getPlayerTotalPetitionCount(activeChar) + 1;
            if (totalPetitions > Config.MAX_PETITIONS_PER_PLAYER) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.WE_HAVE_RECEIVED_S1_PETITIONS_TODAY);
               sm.addNumber(totalPetitions);
               activeChar.sendPacket(sm);
               SystemMessage var7 = null;
            } else if (this._content.length() > 255) {
               activeChar.sendPacket(SystemMessageId.PETITION_MAX_CHARS_255);
            } else {
               if (Config.NEW_PETITIONING_SYSTEM) {
                  PetitionMainGroup group = activeChar.getPetitionGroup();
                  if (group == null) {
                     return;
                  }

                  PetitionSection section = group.getSubGroup(this._type);
                  if (section == null) {
                     return;
                  }
               }

               int petitionId = PetitionManager.getInstance().submitPetition(activeChar, this._content, this._type);
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PETITION_ACCEPTED_RECENT_NO_S1);
               sm.addNumber(petitionId);
               activeChar.sendPacket(sm);
               sm = SystemMessage.getSystemMessage(SystemMessageId.SUBMITTED_YOU_S1_TH_PETITION_S2_LEFT);
               sm.addNumber(totalPetitions);
               sm.addNumber(Config.MAX_PETITIONS_PER_PLAYER - totalPetitions);
               activeChar.sendPacket(sm);
               sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PETITION_ON_WAITING_LIST);
               sm.addNumber(PetitionManager.getInstance().getPendingPetitionCount());
               activeChar.sendPacket(sm);
               SystemMessage var11 = null;
            }
         }
      }
   }
}
