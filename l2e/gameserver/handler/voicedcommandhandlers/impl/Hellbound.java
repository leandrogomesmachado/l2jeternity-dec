package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.actor.Player;

public class Hellbound implements IVoicedCommandHandler {
   private static final String[] VOICED_COMMANDS = new String[]{"hellbound"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String params) {
      if (!Config.HELLBOUND_STATUS) {
         return false;
      } else if (HellboundManager.getInstance().isLocked()) {
         activeChar.sendMessage("Hellbound is currently locked.");
         return true;
      } else {
         int maxTrust = HellboundManager.getInstance().getMaxTrust();
         activeChar.sendMessage(
            "Hellbound level: "
               + HellboundManager.getInstance().getLevel()
               + " trust: "
               + HellboundManager.getInstance().getTrust()
               + (maxTrust > 0 ? "/" + maxTrust : "")
         );
         return true;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return VOICED_COMMANDS;
   }
}
