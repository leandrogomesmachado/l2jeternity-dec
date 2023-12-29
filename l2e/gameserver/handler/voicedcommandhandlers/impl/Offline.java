package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.listener.OfflineAnswerListener;
import l2e.gameserver.model.strings.server.ServerMessage;

public class Offline implements IVoicedCommandHandler {
   private static final String[] VOICED_COMMANDS = new String[]{"offline"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String params) {
      if (!Config.OFFLINE_TRADE_ENABLE) {
         return false;
      } else if (!activeChar.canOfflineMode(activeChar, true)) {
         activeChar.sendMessage(new ServerMessage("Community.ALL_DISABLE", activeChar.getLang()).toString());
         return false;
      } else {
         activeChar.sendConfirmDlg(new OfflineAnswerListener(activeChar), 15000, new ServerMessage("Offline.CHOCICE", activeChar.getLang()).toString());
         return true;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return VOICED_COMMANDS;
   }
}
