package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.strings.server.ServerMessage;

public class ExpGain implements IVoicedCommandHandler {
   private static final String[] _voicedCommands = new String[]{"expon", "xpon", "expoff", "xpoff"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String target) {
      if (!Config.ALLOW_EXP_GAIN_COMMAND) {
         return false;
      } else {
         if (command.equalsIgnoreCase("expon") || command.equalsIgnoreCase("xpon")) {
            activeChar.setVar("blockedEXP@", "0");
            activeChar.sendMessage(new ServerMessage("ExpGain.EXP_GAIN_ENABLED", activeChar.getLang()).toString());
         } else if (command.equalsIgnoreCase("expoff") || command.equalsIgnoreCase("xpoff")) {
            activeChar.setVar("blockedEXP@", "1");
            activeChar.sendMessage(new ServerMessage("ExpGain.EXP_GAIN_DISABLED", activeChar.getLang()).toString());
         }

         return true;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }
}
