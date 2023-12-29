package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.StringTokenizer;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Hellbound implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_hellbound_setlevel", "admin_hellbound"};

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (activeChar == null) {
         return false;
      } else if (command.startsWith(ADMIN_COMMANDS[0])) {
         try {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int level = Integer.parseInt(st.nextToken());
            if (level >= 0 && level <= 11) {
               HellboundManager.getInstance().setLevel(level);
               activeChar.sendMessage("Hellbound level set to " + level);
               return true;
            } else {
               throw new NumberFormatException();
            }
         } catch (Exception var5) {
            activeChar.sendMessage("Usage: //hellbound_setlevel 0-11");
            return false;
         }
      } else if (command.startsWith(ADMIN_COMMANDS[1])) {
         this.showMenu(activeChar);
         return true;
      } else {
         return false;
      }
   }

   private void showMenu(Player activeChar) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(activeChar, activeChar.getLang(), "data/html/admin/hellbound.htm");
      html.replace("%hbstage%", String.valueOf(HellboundManager.getInstance().getLevel()));
      html.replace("%trust%", String.valueOf(HellboundManager.getInstance().getTrust()));
      html.replace("%maxtrust%", String.valueOf(HellboundManager.getInstance().getMaxTrust()));
      html.replace("%mintrust%", String.valueOf(HellboundManager.getInstance().getMinTrust()));
      activeChar.sendPacket(html);
   }
}
