package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.handler.admincommandhandlers.impl.model.ViewerUtils;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class LogsViewer implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_logsviewer", "admin_startViewer", "admin_stopViewer", "admin_viewLog"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      if (command.startsWith("admin_logsviewer")) {
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/logsViewer.htm");
         activeChar.sendPacket(adminhtm);
         return true;
      } else {
         String file = command.split(" ")[1];
         if (command.startsWith("admin_viewLog")) {
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/logsViewer.htm");
            activeChar.sendPacket(adminhtm);
            ViewerUtils.sendCbWindow(activeChar, file);
            return true;
         } else if (command.startsWith("admin_startViewer")) {
            ViewerUtils.startLogViewer(activeChar, file);
            return true;
         } else if (command.startsWith("admin_stopViewer")) {
            ViewerUtils.stopLogViewer(activeChar, file);
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
