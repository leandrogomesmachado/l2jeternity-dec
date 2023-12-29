package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;

public class Disconnect implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_character_disconnect"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.equals("admin_character_disconnect")) {
         this.disconnectCharacter(activeChar);
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void disconnectCharacter(Player activeChar) {
      GameObject target = activeChar.getTarget();
      Player player = null;
      if (target instanceof Player) {
         player = (Player)target;
         if (player == activeChar) {
            activeChar.sendMessage("You cannot logout your own character.");
         } else {
            activeChar.sendMessage("Character " + player.getName() + " disconnected from server.");
            player.logout();
         }
      }
   }
}
