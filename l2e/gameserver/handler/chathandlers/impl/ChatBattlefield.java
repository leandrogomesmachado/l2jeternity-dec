package l2e.gameserver.handler.chathandlers.impl;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.handler.chathandlers.IChatHandler;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class ChatBattlefield implements IChatHandler {
   private static final int[] COMMAND_IDS = new int[]{20};

   @Override
   public void handleChat(int type, Player activeChar, String target, String text, boolean blockBroadCast) {
      if (blockBroadCast) {
         activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text));
      } else if (activeChar.isInFightEvent()) {
         CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);

         for(Player player : activeChar.getFightEvent().getMyTeamFightingPlayers(activeChar)) {
            player.sendPacket(cs);
         }
      } else {
         if (TerritoryWarManager.getInstance().isTWChannelOpen() && activeChar.getSiegeSide() > 0) {
            if (activeChar.isChatBanned() && Util.contains(Config.BAN_CHAT_CHANNELS, type)) {
               activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
               return;
            }

            CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);

            for(Player player : World.getInstance().getAllPlayers()) {
               if (player.getSiegeSide() == activeChar.getSiegeSide()) {
                  player.sendPacket(cs);
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
