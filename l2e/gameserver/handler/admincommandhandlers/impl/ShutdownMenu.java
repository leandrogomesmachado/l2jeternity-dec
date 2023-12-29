package l2e.gameserver.handler.admincommandhandlers.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.Shutdown;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class ShutdownMenu implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_server_shutdown", "admin_server_restart", "admin_server_abort"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_server_shutdown")) {
         try {
            String val = command.substring(22);
            if (Util.isDigit(val)) {
               this.serverShutdown(activeChar, Integer.valueOf(val), false);
            } else {
               activeChar.sendMessage("Usage: //server_shutdown <seconds>");
               this.sendHtmlForm(activeChar);
            }
         } catch (StringIndexOutOfBoundsException var5) {
            this.sendHtmlForm(activeChar);
         }
      } else if (command.startsWith("admin_server_restart")) {
         try {
            String val = command.substring(21);
            if (Util.isDigit(val)) {
               this.serverShutdown(activeChar, Integer.parseInt(val), true);
            } else {
               activeChar.sendMessage("Usage: //server_restart <seconds>");
               this.sendHtmlForm(activeChar);
            }
         } catch (StringIndexOutOfBoundsException var4) {
            this.sendHtmlForm(activeChar);
         }
      } else if (command.startsWith("admin_server_abort")) {
         this.serverAbort(activeChar);
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void sendHtmlForm(Player activeChar) {
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      int t = GameTimeController.getInstance().getGameTime();
      int h = t / 60;
      int m = t % 60;
      SimpleDateFormat format = new SimpleDateFormat("h:mm a");
      Calendar cal = Calendar.getInstance();
      cal.set(11, h);
      cal.set(12, m);
      adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/shutdown.htm");
      adminReply.replace("%count%", String.valueOf(World.getInstance().getAllPlayers().size()));
      adminReply.replace("%used%", String.valueOf(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
      adminReply.replace("%xp%", String.valueOf(Config.RATE_XP_BY_LVL[activeChar.getLevel()] * activeChar.getPremiumBonus().getRateXp()));
      adminReply.replace("%sp%", String.valueOf(Config.RATE_SP_BY_LVL[activeChar.getLevel()] * activeChar.getPremiumBonus().getRateSp()));
      adminReply.replace("%adena%", String.valueOf(Config.RATE_DROP_ADENA * activeChar.getPremiumBonus().getDropAdena()));
      adminReply.replace("%drop%", String.valueOf(Config.RATE_DROP_ITEMS));
      adminReply.replace("%time%", String.valueOf(format.format(cal.getTime())));
      activeChar.sendPacket(adminReply);
   }

   private void serverShutdown(Player activeChar, int seconds, boolean restart) {
      Shutdown.getInstance().startShutdown(activeChar, seconds, restart);
   }

   private void serverAbort(Player activeChar) {
      Shutdown.getInstance().abort(activeChar);
   }
}
