package l2e.gameserver.handler.chathandlers.impl;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.handler.chathandlers.IChatHandler;
import l2e.gameserver.instancemanager.PetitionManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public class ChatPetition implements IChatHandler {
   private static final int[] COMMAND_IDS = new int[]{6, 7};

   @Override
   public void handleChat(int type, Player activeChar, String target, String text, boolean blockBroadCast) {
      if (activeChar.isChatBanned() && Util.contains(Config.BAN_CHAT_CHANNELS, type)) {
         activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
      } else if (!PetitionManager.getInstance().isPlayerInConsultation(activeChar)) {
         activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_IN_PETITION_CHAT);
      } else {
         PetitionManager.getInstance().sendActivePetitionMessage(activeChar, text);
      }
   }

   @Override
   public int[] getChatTypeList() {
      return COMMAND_IDS;
   }
}
