package l2e.gameserver.handler.bypasshandlers.impl;

import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;

public class VoiceCommand implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"voice"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (command.length() > 7 && command.charAt(6) == '.') {
         int endOfCommand = command.indexOf(" ", 7);
         String vc;
         String vparams;
         if (endOfCommand > 0) {
            vc = command.substring(7, endOfCommand).trim();
            vparams = command.substring(endOfCommand).trim();
         } else {
            vc = command.substring(7).trim();
            vparams = null;
         }

         if (vc.length() > 0) {
            IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler(vc);
            if (vch != null) {
               return vch.useVoicedCommand(vc, activeChar, vparams);
            }
         }
      }

      return false;
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
