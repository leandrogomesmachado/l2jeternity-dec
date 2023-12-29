package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;

public class Debug implements IVoicedCommandHandler {
   private static final String[] VOICED_COMMANDS = new String[]{"debug"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String params) {
      if (Config.DEBUG_VOICE_COMMAND && AdminParser.getInstance().hasAccess(command, activeChar.getAccessLevel())) {
         if (VOICED_COMMANDS[0].equalsIgnoreCase(command)) {
            if (activeChar.isDebug()) {
               activeChar.setDebug(null);
               activeChar.sendMessage("Debugging disabled.");
            } else {
               activeChar.setDebug(activeChar);
               activeChar.sendMessage("Debugging enabled.");
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return VOICED_COMMANDS;
   }
}
