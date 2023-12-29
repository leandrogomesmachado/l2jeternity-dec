package l2e.gameserver.handler.chathandlers.impl;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.handler.chathandlers.IChatHandler;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.model.BlockedList;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class ChatTrade implements IChatHandler {
   private static final int[] COMMAND_IDS = new int[]{8};

   @Override
   public void handleChat(int type, Player activeChar, String target, String text, boolean blockBroadCast) {
      if (activeChar.isChatBanned() && Util.contains(Config.BAN_CHAT_CHANNELS, type)) {
         activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
      } else {
         CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
         if (blockBroadCast) {
            activeChar.sendPacket(cs);
         } else {
            if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("on") || Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("gm") && activeChar.isGM()) {
               int region = MapRegionManager.getInstance().getMapRegionLocId(activeChar);

               for(Player player : World.getInstance().getAllPlayers()) {
                  if (region == MapRegionManager.getInstance().getMapRegionLocId(player)
                     && !BlockedList.isBlocked(player, activeChar)
                     && player.getReflectionId() == activeChar.getReflectionId()) {
                     player.sendPacket(cs);
                  }
               }
            } else if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("global")) {
               if (!activeChar.canOverrideCond(PcCondOverride.CHAT_CONDITIONS) && !activeChar.checkFloodProtection("TRADECHAT", "trade_chat")) {
                  activeChar.sendMessage("Do not spam trade channel.");
                  return;
               }

               for(Player player : World.getInstance().getAllPlayers()) {
                  if (!BlockedList.isBlocked(player, activeChar)) {
                     player.sendPacket(cs);
                  }
               }
            }
         }
      }
   }

   @Override
   public int[] getChatTypeList() {
      return COMMAND_IDS;
   }
}
