package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.PetitionManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public class Petition implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_view_petitions", "admin_view_petition", "admin_accept_petition", "admin_reject_petition", "admin_reset_petitions", "admin_force_peti"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      int petitionId = -1;

      try {
         petitionId = Integer.parseInt(command.split(" ")[1]);
      } catch (Exception var7) {
      }

      if (command.equals("admin_view_petitions")) {
         PetitionManager.getInstance().sendPendingPetitionList(activeChar);
      } else if (command.startsWith("admin_view_petition")) {
         PetitionManager.getInstance().viewPetition(activeChar, petitionId);
      } else if (command.startsWith("admin_accept_petition")) {
         if (PetitionManager.getInstance().isPlayerInConsultation(activeChar)) {
            activeChar.sendPacket(SystemMessageId.ONLY_ONE_ACTIVE_PETITION_AT_TIME);
            return true;
         }

         if (PetitionManager.getInstance().isPetitionInProcess(petitionId)) {
            activeChar.sendPacket(SystemMessageId.PETITION_UNDER_PROCESS);
            return true;
         }

         if (!PetitionManager.getInstance().acceptPetition(activeChar, petitionId)) {
            activeChar.sendPacket(SystemMessageId.NOT_UNDER_PETITION_CONSULTATION);
         }
      } else if (command.startsWith("admin_reject_petition")) {
         if (!PetitionManager.getInstance().rejectPetition(activeChar, petitionId)) {
            activeChar.sendPacket(SystemMessageId.FAILED_CANCEL_PETITION_TRY_LATER);
         }

         PetitionManager.getInstance().sendPendingPetitionList(activeChar);
      } else if (command.equals("admin_reset_petitions")) {
         if (PetitionManager.getInstance().isPetitionInProcess()) {
            activeChar.sendPacket(SystemMessageId.PETITION_UNDER_PROCESS);
            return false;
         }

         PetitionManager.getInstance().clearPendingPetitions();
         PetitionManager.getInstance().sendPendingPetitionList(activeChar);
      } else if (command.startsWith("admin_force_peti")) {
         try {
            GameObject targetChar = activeChar.getTarget();
            if (targetChar == null || !(targetChar instanceof Player)) {
               activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
               return false;
            }

            Player targetPlayer = (Player)targetChar;
            String val = command.substring(15);
            petitionId = PetitionManager.getInstance().submitPetition(targetPlayer, val, 9);
            PetitionManager.getInstance().acceptPetition(activeChar, petitionId);
         } catch (StringIndexOutOfBoundsException var8) {
            activeChar.sendMessage("Usage: //force_peti text");
            return false;
         }
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
