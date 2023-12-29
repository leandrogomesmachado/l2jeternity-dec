package l2e.gameserver.handler.chathandlers.impl;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.handler.chathandlers.IChatHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class ChatClan implements IChatHandler {
   private static final int[] COMMAND_IDS = new int[]{4};

   @Override
   public void handleChat(int type, Player activeChar, String target, String text, boolean blockBroadCast) {
      if (activeChar.getClan() != null) {
         if (activeChar.isChatBanned() && Util.contains(Config.BAN_CHAT_CHANNELS, type)) {
            activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
            return;
         }

         if (blockBroadCast) {
            activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text));
            return;
         }

         CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
         activeChar.getClan().broadcastCSToOnlineMembers(cs, activeChar);
      }
   }

   @Override
   public int[] getChatTypeList() {
      return COMMAND_IDS;
   }
}
