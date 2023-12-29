package l2e.gameserver.handler.chathandlers.impl;

import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.handler.chathandlers.IChatHandler;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import l2e.gameserver.model.BlockedList;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class ChatAll implements IChatHandler {
   private static Logger _log = Logger.getLogger(ChatAll.class.getName());
   private static final int[] COMMAND_IDS = new int[]{0};

   @Override
   public void handleChat(int type, Player activeChar, String params, String text, boolean blockBroadCast) {
      boolean vcd_used = false;
      if (text.startsWith(".")) {
         StringTokenizer st = new StringTokenizer(text);
         String command = "";
         IVoicedCommandHandler vch;
         if (st.countTokens() > 1) {
            command = st.nextToken().substring(1);
            params = text.substring(command.length() + 2);
            vch = VoicedCommandHandler.getInstance().getHandler(command);
         } else {
            command = text.substring(1);
            if (Config.DEBUG) {
               _log.info("Command: " + command);
            }

            vch = VoicedCommandHandler.getInstance().getHandler(command);
         }

         if (vch != null) {
            vch.useVoicedCommand(command, activeChar, params);
            vcd_used = true;
         } else {
            if (Config.DEBUG) {
               _log.warning("No handler registered for bypass '" + command + "'");
            }

            vcd_used = false;
         }
      }

      if (!vcd_used) {
         if (activeChar.isChatBanned() && Util.contains(Config.BAN_CHAT_CHANNELS, type)) {
            activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
            return;
         }

         if (text.matches("\\.{1}[^\\.]+")) {
            activeChar.sendPacket(SystemMessageId.INCORRECT_SYNTAX);
         } else {
            CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getAppearance().getVisibleName(), text);
            Collection<Player> plrs = null;
            List var13;
            if (activeChar.isInFightEvent()) {
               var13 = activeChar.getFightEvent().getAllFightingPlayers();
            } else {
               var13 = World.getInstance().getAroundPlayers(activeChar);
            }

            for(Player player : var13) {
               if (player != null
                  && activeChar.isInsideRadius(player, 1250, false, true)
                  && !BlockedList.isBlocked(player, activeChar)
                  && !blockBroadCast
                  && (!activeChar.isInFightEvent() || player != activeChar)) {
                  player.sendPacket(cs);
               }
            }

            activeChar.sendPacket(cs);
         }
      }
   }

   @Override
   public int[] getChatTypeList() {
      return COMMAND_IDS;
   }
}
