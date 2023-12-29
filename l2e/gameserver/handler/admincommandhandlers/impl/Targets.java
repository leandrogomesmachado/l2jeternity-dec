package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;

public class Targets implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_target"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_target")) {
         this.handleTarget(command, activeChar);
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void handleTarget(String command, Player activeChar) {
      try {
         String targetName = command.substring(13);
         Player player = World.getInstance().getPlayer(targetName);
         if (player != null) {
            player.onAction(activeChar);
         } else {
            activeChar.sendMessage("Player " + targetName + " not found");
         }
      } catch (IndexOutOfBoundsException var5) {
         activeChar.sendMessage("Please specify correct name.");
      }
   }
}
