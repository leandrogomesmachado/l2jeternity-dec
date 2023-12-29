package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class AerialCleft implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_aerial_cleft", "admin_cleft_start", "admin_cleft_stop", "admin_cleft_open_reg", "admin_cleft_clean_time"
   };

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (activeChar == null) {
         return false;
      } else if (command.startsWith(ADMIN_COMMANDS[0])) {
         this.showMenu(activeChar);
         return true;
      } else if (command.startsWith("admin_cleft_start")) {
         if (AerialCleftEvent.getInstance().forcedEventStart()) {
            activeChar.sendMessage("Aerial Cleft started!");
         } else {
            activeChar.sendMessage("Problem with starting Aerial Cleft.");
         }

         this.showMenu(activeChar);
         return true;
      } else if (command.startsWith("admin_cleft_stop")) {
         if (AerialCleftEvent.getInstance().forcedEventStop()) {
            activeChar.sendMessage("Aerial Cleft stoped!");
         } else {
            activeChar.sendMessage("Problem with stoping Aerial Cleft.");
         }

         this.showMenu(activeChar);
         return true;
      } else if (command.startsWith("admin_cleft_open_reg")) {
         if (AerialCleftEvent.getInstance().openRegistration()) {
            activeChar.sendMessage("Open registration for Aerial Cleft.");
         } else {
            activeChar.sendMessage("Warning! Aerial Cleft in progress.");
         }

         this.showMenu(activeChar);
         return true;
      } else if (command.startsWith("admin_cleft_clean_time")) {
         if (AerialCleftEvent.getInstance().cleanUpTime()) {
            activeChar.sendMessage("Clean up reload time for Aerial Cleft.");
         } else {
            activeChar.sendMessage("Warning! Aerial Cleft in progress.");
         }

         this.showMenu(activeChar);
         return true;
      } else {
         return false;
      }
   }

   private void showMenu(Player activeChar) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(activeChar, activeChar.getLang(), "data/html/admin/gracia.htm");
      activeChar.sendPacket(html);
   }
}
