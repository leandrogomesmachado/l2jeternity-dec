package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class HelpPage implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_help"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_help")) {
         try {
            String val = command.substring(11);
            showHelpPage(activeChar, val);
         } catch (StringIndexOutOfBoundsException var4) {
         }
      }

      return true;
   }

   public static void showHelpPage(Player targetChar, String filename) {
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setFile(targetChar, targetChar.getLang(), "data/html/admin/" + filename);
      targetChar.sendPacket(adminReply);
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
