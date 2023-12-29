package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.List;
import java.util.StringTokenizer;
import l2e.commons.util.StringUtil;
import l2e.commons.util.Util;
import l2e.gameserver.Announcements;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.taskmanager.AutoAnnounceTaskManager;

public class Announcement implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_list_announcements",
      "admin_list_critannouncements",
      "admin_reload_announcements",
      "admin_announce_announcements",
      "admin_add_announcement",
      "admin_del_announcement",
      "admin_add_critannouncement",
      "admin_del_critannouncement",
      "admin_announce",
      "admin_critannounce",
      "admin_announce_menu",
      "admin_critannounce_menu",
      "admin_list_autoann",
      "admin_reload_autoann",
      "admin_add_autoann",
      "admin_del_autoann"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      if (command.equals("admin_list_announcements")) {
         Announcements.getInstance().listAnnouncements(activeChar);
      } else if (command.equals("admin_list_critannouncements")) {
         Announcements.getInstance().listCritAnnouncements(activeChar);
      } else if (command.equals("admin_reload_announcements")) {
         Announcements.getInstance().loadAnnouncements();
         Announcements.getInstance().listAnnouncements(activeChar);
      } else if (command.startsWith("admin_announce_menu")) {
         if (Config.GM_ANNOUNCER_NAME && command.length() > 20) {
            command = command + " (" + activeChar.getName() + ")";
         }

         Announcements.getInstance().handleAnnounce(command, 20, false);
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/gm_menu.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.startsWith("admin_critannounce_menu")) {
         try {
            command = command.substring(24);
            if (Config.GM_CRITANNOUNCER_NAME && command.length() > 0) {
               command = activeChar.getName() + ": " + command;
            }

            Announcements.getInstance().handleAnnounce(command, 0, true);
         } catch (StringIndexOutOfBoundsException var18) {
         }

         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/gm_menu.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.equals("admin_announce_announcements")) {
         for(Player player : World.getInstance().getAllPlayers()) {
            Announcements.getInstance().showAnnouncements(player);
         }

         Announcements.getInstance().listAnnouncements(activeChar);
      } else if (command.startsWith("admin_add_announcement")) {
         if (!command.equals("admin_add_announcement")) {
            try {
               String val = command.substring(23);
               Announcements.getInstance().addAnnouncement(val);
               Announcements.getInstance().listAnnouncements(activeChar);
            } catch (StringIndexOutOfBoundsException var17) {
            }
         }
      } else if (command.startsWith("admin_add_critannouncement")) {
         if (!command.equals("admin_add_critannouncement")) {
            try {
               String val = command.substring(27);
               Announcements.getInstance().addCritAnnouncement(val);
               Announcements.getInstance().listCritAnnouncements(activeChar);
            } catch (StringIndexOutOfBoundsException var16) {
            }
         }
      } else if (command.startsWith("admin_del_announcement")) {
         try {
            int val = Integer.parseInt(command.substring(23));
            Announcements.getInstance().delAnnouncement(val);
            Announcements.getInstance().listAnnouncements(activeChar);
         } catch (StringIndexOutOfBoundsException var15) {
         }
      } else if (command.startsWith("admin_del_critannouncement")) {
         try {
            int val = Integer.parseInt(command.substring(27));
            Announcements.getInstance().delCritAnnouncement(val);
            Announcements.getInstance().listCritAnnouncements(activeChar);
         } catch (StringIndexOutOfBoundsException var14) {
         }
      } else if (command.startsWith("admin_announce")) {
         if (Config.GM_ANNOUNCER_NAME && command.length() > 15) {
            command = command + " (" + activeChar.getName() + ")";
         }

         Announcements.getInstance().handleAnnounce(command, 15, false);
      } else if (command.startsWith("admin_critannounce")) {
         try {
            command = command.substring(19);
            if (Config.GM_CRITANNOUNCER_NAME && command.length() > 0) {
               command = activeChar.getName() + ": " + command;
            }

            Announcements.getInstance().handleAnnounce(command, 0, true);
         } catch (StringIndexOutOfBoundsException var13) {
         }
      } else if (command.startsWith("admin_list_autoann")) {
         this.listAutoAnnouncements(activeChar);
      } else if (command.startsWith("admin_reload_autoann")) {
         AutoAnnounceTaskManager.getInstance().restore();
         activeChar.sendMessage("AutoAnnouncement Reloaded.");
         this.listAutoAnnouncements(activeChar);
      } else if (command.startsWith("admin_add_autoann")) {
         StringTokenizer st = new StringTokenizer(command);
         st.nextToken();
         if (!st.hasMoreTokens()) {
            activeChar.sendMessage("Not enough parameters for adding autoannounce!");
            return false;
         }

         String token = st.nextToken();
         if (!token.equals("-1") && !Util.isDigit(token)) {
            activeChar.sendMessage("Not a valid initial value!");
            return false;
         }

         long initial = Long.parseLong(token);
         if (!st.hasMoreTokens()) {
            activeChar.sendMessage("Not enough parameters for adding autoannounce!");
            return false;
         }

         token = st.nextToken();
         if (!Util.isDigit(token)) {
            activeChar.sendMessage("Not a valid delay value!");
            return false;
         }

         long delay = Long.parseLong(token);
         if (!st.hasMoreTokens()) {
            activeChar.sendMessage("Not enough parameters for adding autoannounce!");
            return false;
         }

         token = st.nextToken();
         if (!Util.isDigit(token)) {
            activeChar.sendMessage("Not a valid repeat value!");
            return false;
         }

         int repeat = Integer.parseInt(token);
         if (!st.hasMoreTokens()) {
            activeChar.sendMessage("Not enough parameters for adding autoannounce!");
            return false;
         }

         boolean isCritical = Boolean.valueOf(st.nextToken());
         if (!st.hasMoreTokens()) {
            activeChar.sendMessage("Not enough parameters for adding autoannounce!");
            return false;
         }

         StringBuilder memo = new StringBuilder();

         while(st.hasMoreTokens()) {
            memo.append(st.nextToken());
            memo.append(" ");
         }

         AutoAnnounceTaskManager.getInstance().addAutoAnnounce(initial * 1000L, delay * 1000L, repeat, memo.toString().trim(), isCritical);
         this.listAutoAnnouncements(activeChar);
      } else if (command.startsWith("admin_del_autoann")) {
         StringTokenizer st = new StringTokenizer(command);
         st.nextToken();
         if (!st.hasMoreTokens()) {
            activeChar.sendMessage("Not enough parameters for deleting autoannounce!");
            return false;
         }

         String token = st.nextToken();
         if (!Util.isDigit(token)) {
            activeChar.sendMessage("Not a valid auto announce Id value!");
            return false;
         }

         AutoAnnounceTaskManager.getInstance().deleteAutoAnnounce(Integer.parseInt(token));
         this.listAutoAnnouncements(activeChar);
      }

      return true;
   }

   private void listAutoAnnouncements(Player activeChar) {
      String content = HtmCache.getInstance().getHtmForce(activeChar, activeChar.getLang(), "data/html/admin/autoannounce.htm");
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setHtml(activeChar, content);
      StringBuilder replyMSG = StringUtil.startAppend(500, "<br>");
      List<AutoAnnounceTaskManager.AutoAnnouncement> autoannouncements = AutoAnnounceTaskManager.getInstance().getAutoAnnouncements();

      for(int i = 0; i < autoannouncements.size(); ++i) {
         AutoAnnounceTaskManager.AutoAnnouncement autoann = autoannouncements.get(i);
         StringBuilder memo2 = new StringBuilder();

         for(String memo0 : autoann.getMemo()) {
            memo2.append(memo0);
            memo2.append("/n");
         }

         replyMSG.append("<table width=260><tr><td width=220><font color=\"" + (autoann.isCritical() ? "00FCFC" : "7FFCFC") + "\">");
         replyMSG.append(memo2.toString().trim());
         replyMSG.append("</font></td><td width=40><button value=\"Delete\" action=\"bypass -h admin_del_autoann ");
         replyMSG.append(i);
         replyMSG.append("\" width=60 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>");
      }

      adminReply.replace("%announces%", replyMSG.toString());
      activeChar.sendPacket(adminReply);
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
