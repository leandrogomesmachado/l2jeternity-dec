package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.strings.server.ServerMessage;

public class HideTraders implements IVoicedCommandHandler {
   private static final String[] _voicedCommands = new String[]{"notraders"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String target) {
      if (!Config.ALLOW_HIDE_TRADERS_COMMAND) {
         return false;
      } else {
         if (activeChar.getNotShowTraders()) {
            activeChar.setVar("useHideTraders@", "0");
            activeChar.restorePrivateStores();
            activeChar.sendMessage(new ServerMessage("HideTraders.DISABLED", activeChar.getLang()).toString());
         } else {
            activeChar.setVar("useHideTraders@", "1");
            activeChar.hidePrivateStores();
            activeChar.sendMessage(new ServerMessage("HideTraders.ENABLED", activeChar.getLang()).toString());
         }

         return true;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }
}
