package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.commons.util.Util;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;

public class SetVCmd implements IVoicedCommandHandler {
   private static final String[] VOICED_COMMANDS = new String[]{"set name", "set home", "set group"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String params) {
      if (command.equals("set")) {
         GameObject target = activeChar.getTarget();
         if (target == null || !target.isPlayer()) {
            return false;
         }

         Player player = activeChar.getTarget().getActingPlayer();
         if (activeChar.getClan() == null || player.getClan() == null || activeChar.getClan().getId() != player.getClan().getId()) {
            return false;
         }

         if (params.startsWith("privileges")) {
            String val = params.substring(11);
            if (!Util.isDigit(val)) {
               return false;
            }

            int n = Integer.parseInt(val);
            if (activeChar.getClanPrivileges() <= n && !activeChar.isClanLeader()) {
               return false;
            }

            player.setClanPrivileges(n);
            activeChar.sendMessage("Your clan privileges have been set to " + n + " by " + activeChar.getName() + ".");
         } else if (params.startsWith("title")) {
         }
      }

      return true;
   }

   @Override
   public String[] getVoicedCommandList() {
      return VOICED_COMMANDS;
   }
}
