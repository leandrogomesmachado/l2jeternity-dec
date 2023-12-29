package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.instancemanager.PetitionManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestPetitionCancel extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (PetitionManager.getInstance().isPlayerInConsultation(activeChar)) {
            if (activeChar.isGM()) {
               PetitionManager.getInstance().endActivePetition(activeChar);
            } else {
               activeChar.sendPacket(SystemMessageId.PETITION_UNDER_PROCESS);
            }
         } else if (PetitionManager.getInstance().isPlayerPetitionPending(activeChar)) {
            if (PetitionManager.getInstance().cancelActivePetition(activeChar)) {
               int numRemaining = Config.MAX_PETITIONS_PER_PLAYER - PetitionManager.getInstance().getPlayerTotalPetitionCount(activeChar);
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PETITION_CANCELED_SUBMIT_S1_MORE_TODAY);
               sm.addString(String.valueOf(numRemaining));
               activeChar.sendPacket(sm);
               SystemMessage var5 = null;
               String msgContent = activeChar.getName() + " has canceled a pending petition.";
               AdminParser.getInstance().broadcastToGMs(new CreatureSay(activeChar.getObjectId(), 17, "Petition System", msgContent));
            } else {
               activeChar.sendPacket(SystemMessageId.FAILED_CANCEL_PETITION_TRY_LATER);
            }
         } else {
            activeChar.sendPacket(SystemMessageId.PETITION_NOT_SUBMITTED);
         }
      }
   }
}
