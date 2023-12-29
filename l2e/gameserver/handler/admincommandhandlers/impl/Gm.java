package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.logging.Logger;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.actor.Player;

public class Gm implements IAdminCommandHandler {
   private static Logger _log = Logger.getLogger(Gm.class.getName());
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_gm"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.equals("admin_gm") && activeChar.isGM()) {
         AdminParser.getInstance().deleteGm(activeChar);
         activeChar.setAccessLevel(0);
         activeChar.sendMessage("You no longer have GM status.");
         _log.info("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") turned his GM status off");
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
