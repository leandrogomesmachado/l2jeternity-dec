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

public class ChatHeroVoice implements IChatHandler {
   private static final int[] COMMAND_IDS = new int[]{17};

   @Override
   public void handleChat(int type, Player activeChar, String target, String text, boolean blockBroadCast) {
      if (activeChar.isHero() || activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS)) {
         if (activeChar.isChatBanned() && Util.contains(Config.BAN_CHAT_CHANNELS, type)) {
            activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
            return;
         }

         if (!activeChar.checkFloodProtection("HEROCHAT", "hero_chat")) {
            activeChar.sendMessage("Action failed. Heroes are only able to speak in the global channel once every 10 seconds.");
            return;
         }

         CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
         if (blockBroadCast) {
            activeChar.sendPacket(cs);
            return;
         }

         for(Player player : World.getInstance().getAllPlayers()) {
            if (player != null && !BlockedList.isBlocked(player, activeChar)) {
               player.sendPacket(cs);
            }
         }
      }
   }

   @Override
   public int[] getChatTypeList() {
      return COMMAND_IDS;
   }
}
