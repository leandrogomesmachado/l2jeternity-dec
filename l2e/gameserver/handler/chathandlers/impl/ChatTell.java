package l2e.gameserver.handler.chathandlers.impl;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.handler.chathandlers.IChatHandler;
import l2e.gameserver.model.BlockedList;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class ChatTell implements IChatHandler {
   private static final int[] COMMAND_IDS = new int[]{2};

   @Override
   public void handleChat(int type, Player activeChar, String target, String text, boolean blockBroadCast) {
      if (activeChar.isChatBanned() && Util.contains(Config.BAN_CHAT_CHANNELS, type)) {
         activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
      } else if (Config.JAIL_DISABLE_CHAT && activeChar.isJailed() && !activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS)) {
         activeChar.sendPacket(SystemMessageId.CHATTING_PROHIBITED);
      } else if (target != null) {
         CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
         Player receiver = null;
         receiver = World.getInstance().getPlayer(target);
         if (receiver != null && !receiver.isSilenceMode(activeChar.getObjectId())) {
            if (Config.JAIL_DISABLE_CHAT && receiver.isJailed() && !activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS)) {
               activeChar.sendMessage("Player is in jail.");
               return;
            }

            if (receiver.isChatBanned()) {
               activeChar.sendPacket(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
               return;
            }

            if ((receiver.getClient() == null || receiver.getClient().isDetached()) && !receiver.isFakePlayer()) {
               activeChar.sendMessage("Player is in offline mode.");
               return;
            }

            if (!BlockedList.isBlocked(receiver, activeChar)) {
               if (Config.SILENCE_MODE_EXCLUDE && activeChar.isSilenceMode()) {
                  activeChar.addSilenceModeExcluded(receiver.getObjectId());
               }

               if (!blockBroadCast) {
                  receiver.sendPacket(cs);
               }

               activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), type, "->" + receiver.getName(), text));
            } else {
               activeChar.sendPacket(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
            }
         } else {
            activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
         }
      }
   }

   @Override
   public int[] getChatTypeList() {
      return COMMAND_IDS;
   }
}
