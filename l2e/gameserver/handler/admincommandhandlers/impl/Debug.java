package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public class Debug implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_debug"};

   @Override
   public final boolean useAdminCommand(String command, Player activeChar) {
      String[] commandSplit = command.split(" ");
      if (ADMIN_COMMANDS[0].equalsIgnoreCase(commandSplit[0])) {
         GameObject target;
         if (commandSplit.length > 1) {
            target = World.getInstance().getPlayer(commandSplit[1].trim());
            if (target == null) {
               activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
               return true;
            }
         } else {
            target = activeChar.getTarget();
         }

         if (target instanceof Creature) {
            this.setDebug(activeChar, (Creature)target);
         } else {
            this.setDebug(activeChar, activeChar);
         }
      }

      return true;
   }

   @Override
   public final String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private final void setDebug(Player activeChar, Creature target) {
      if (target.isDebug()) {
         target.setDebug(null);
         activeChar.sendMessage("Stop debugging " + target.getName());
      } else {
         target.setDebug(activeChar);
         activeChar.sendMessage("Start debugging " + target.getName());
      }
   }
}
