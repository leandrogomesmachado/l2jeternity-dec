package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.logging.Logger;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.handler.admincommandhandlers.AdminCommandHandler;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.actor.Player;

public class Summons implements IAdminCommandHandler {
   private static final Logger _log = Logger.getLogger(Summons.class.getName());
   public static final String[] ADMIN_COMMANDS = new String[]{"admin_summon"};

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      int count = 1;
      String[] data = command.split(" ");

      int id;
      try {
         id = Integer.parseInt(data[1]);
         if (data.length > 2) {
            count = Integer.parseInt(data[2]);
         }
      } catch (NumberFormatException var8) {
         activeChar.sendMessage("Incorrect format for command 'summon'");
         return false;
      }

      if (id < 1000000) {
         String subCommand = "admin_create_item";
         if (!AdminParser.getInstance().hasAccess(subCommand, activeChar.getAccessLevel())) {
            activeChar.sendMessage("You don't have the access right to use this command!");
            _log.warning("Character " + activeChar.getName() + " tryed to use admin command " + subCommand + ", but have no access to it!");
            return false;
         }

         IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(subCommand);
         ach.useAdminCommand(subCommand + " " + id + " " + count, activeChar);
      } else {
         String subCommand = "admin_spawn_once";
         if (!AdminParser.getInstance().hasAccess(subCommand, activeChar.getAccessLevel())) {
            activeChar.sendMessage("You don't have the access right to use this command!");
            _log.warning("Character " + activeChar.getName() + " tryed to use admin command " + subCommand + ", but have no access to it!");
            return false;
         }

         IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(subCommand);
         activeChar.sendMessage("This is only a temporary spawn.  The mob(s) will NOT respawn.");
         id -= 1000000;
         ach.useAdminCommand(subCommand + " " + id + " " + count, activeChar);
      }

      return true;
   }
}
