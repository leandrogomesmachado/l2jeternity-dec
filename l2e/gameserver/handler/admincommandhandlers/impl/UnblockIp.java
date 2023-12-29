package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.logging.Logger;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.actor.Player;

public class UnblockIp implements IAdminCommandHandler {
   private static final Logger _log = Logger.getLogger(UnblockIp.class.getName());
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_unblockip"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_unblockip ")) {
         try {
            String ipAddress = command.substring(16);
            if (this.unblockIp(ipAddress, activeChar)) {
               activeChar.sendMessage("Removed IP " + ipAddress + " from blocklist!");
            }
         } catch (StringIndexOutOfBoundsException var4) {
            activeChar.sendMessage("Usage: //unblockip <ip>");
         }
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private boolean unblockIp(String ipAddress, Player activeChar) {
      _log.warning("IP removed by GM " + activeChar.getName());
      return true;
   }
}
