package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.Calendar;
import java.util.StringTokenizer;
import l2e.commons.util.StringUtil;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.TerritoryWard;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class TerritoryWar implements IAdminCommandHandler {
   private static final String[] _adminCommands = new String[]{
      "admin_territory_war",
      "admin_territory_war_time",
      "admin_territory_war_start",
      "admin_territory_war_end",
      "admin_territory_wards_list",
      "admin_territory_list",
      "admin_territory_set_lord",
      "admin_territory_remove_lord"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      StringTokenizer st = new StringTokenizer(command);
      command = st.nextToken();
      if (command.equals("admin_territory_war")) {
         this.showMainPage(activeChar);
      } else if (command.equalsIgnoreCase("admin_territory_war_time")) {
         if (st.hasMoreTokens()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(TerritoryWarManager.getInstance().getTWStartTimeInMillis());
            String val = st.nextToken();
            if ("month".equals(val)) {
               int month = cal.get(2) + Integer.parseInt(st.nextToken());
               if (cal.getActualMinimum(2) > month || cal.getActualMaximum(2) < month) {
                  activeChar.sendMessage(
                     "Unable to change Siege Date - Incorrect month value only " + cal.getActualMinimum(2) + "-" + cal.getActualMaximum(2) + " is accepted!"
                  );
                  return false;
               }

               cal.set(2, month);
            } else if ("day".equals(val)) {
               int day = Integer.parseInt(st.nextToken());
               if (cal.getActualMinimum(5) > day || cal.getActualMaximum(5) < day) {
                  activeChar.sendMessage(
                     "Unable to change Siege Date - Incorrect day value only " + cal.getActualMinimum(5) + "-" + cal.getActualMaximum(5) + " is accepted!"
                  );
                  return false;
               }

               cal.set(5, day);
            } else if ("hour".equals(val)) {
               int hour = Integer.parseInt(st.nextToken());
               if (cal.getActualMinimum(11) > hour || cal.getActualMaximum(11) < hour) {
                  activeChar.sendMessage(
                     "Unable to change Siege Date - Incorrect hour value only " + cal.getActualMinimum(11) + "-" + cal.getActualMaximum(11) + " is accepted!"
                  );
                  return false;
               }

               cal.set(11, hour);
            } else if ("min".equals(val)) {
               int min = Integer.parseInt(st.nextToken());
               if (cal.getActualMinimum(12) > min || cal.getActualMaximum(12) < min) {
                  activeChar.sendMessage(
                     "Unable to change Siege Date - Incorrect minute value only "
                        + cal.getActualMinimum(12)
                        + "-"
                        + cal.getActualMaximum(12)
                        + " is accepted!"
                  );
                  return false;
               }

               cal.set(12, min);
            }

            if (cal.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
               activeChar.sendMessage("Unable to change TW Date!");
            } else if (cal.getTimeInMillis() != TerritoryWarManager.getInstance().getTWStartTimeInMillis()) {
               Quest twQuest = QuestManager.getInstance().getQuest(TerritoryWarManager.qn);
               if (twQuest != null) {
                  twQuest.onAdvEvent("setTWDate " + cal.getTimeInMillis(), null, null);
               } else {
                  activeChar.sendMessage("Missing Territory War Quest!");
               }
            }
         }

         this.showSiegeTimePage(activeChar);
      } else if (command.equalsIgnoreCase("admin_territory_war_start")) {
         Quest twQuest = QuestManager.getInstance().getQuest(TerritoryWarManager.qn);
         if (twQuest != null) {
            twQuest.onAdvEvent("setTWDate " + Calendar.getInstance().getTimeInMillis(), null, null);
         } else {
            activeChar.sendMessage("Missing Territory War Quest!");
         }
      } else if (command.equalsIgnoreCase("admin_territory_war_end")) {
         Quest twQuest = QuestManager.getInstance().getQuest(TerritoryWarManager.qn);
         if (twQuest != null) {
            twQuest.onAdvEvent("setTWDate " + (Calendar.getInstance().getTimeInMillis() - TerritoryWarManager.WARLENGTH), null, null);
         } else {
            activeChar.sendMessage("Missing Territory War Quest!");
         }
      } else if (command.equalsIgnoreCase("admin_territory_wards_list")) {
         NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(1, 1);
         StringBuilder sb = new StringBuilder();
         sb.append("<html><title>Territory War</title><body><br><center><font color=\"LEVEL\">Active Wards List:</font></center>");
         if (TerritoryWarManager.getInstance().isTWInProgress()) {
            for(TerritoryWard ward : TerritoryWarManager.getInstance().getAllTerritoryWards()) {
               if (ward.getNpc() != null) {
                  sb.append("<table width=270><tr>");
                  sb.append("<td width=135 ALIGN=\"LEFT\">" + ward.getNpc().getName() + "</td>");
                  sb.append(
                     "<td width=135 ALIGN=\"RIGHT\"><button value=\"TeleTo\" action=\"bypass -h admin_move_to "
                        + ward.getNpc().getX()
                        + " "
                        + ward.getNpc().getY()
                        + " "
                        + ward.getNpc().getZ()
                        + "\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>"
                  );
                  sb.append("</tr></table>");
               } else if (ward.getPlayer() != null) {
                  sb.append("<table width=270><tr>");
                  sb.append(
                     "<td width=135 ALIGN=\"LEFT\">"
                        + activeChar.getItemName(ward.getPlayer().getActiveWeaponInstance().getItem())
                        + " - "
                        + ward.getPlayer().getName()
                        + "</td>"
                  );
                  sb.append(
                     "<td width=135 ALIGN=\"RIGHT\"><button value=\"TeleTo\" action=\"bypass -h admin_move_to "
                        + ward.getPlayer().getX()
                        + " "
                        + ward.getPlayer().getY()
                        + " "
                        + ward.getPlayer().getZ()
                        + "\" width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>"
                  );
                  sb.append("</tr></table>");
               }
            }

            sb.append(
               "<br><center><button value=\"Back\" action=\"bypass -h admin_territory_war\" width=50 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>"
            );
            npcHtmlMessage.setHtml(activeChar, sb.toString());
            activeChar.sendPacket(npcHtmlMessage);
         } else {
            sb.append("<br><br><center>The Ward List is empty!<br>TW has probably NOT started!");
            sb.append(
               "<br><button value=\"Back\" action=\"bypass -h admin_territory_war\" width=50 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>"
            );
            npcHtmlMessage.setHtml(activeChar, sb.toString());
            activeChar.sendPacket(npcHtmlMessage);
         }
      } else if (command.equalsIgnoreCase("admin_territory_list")) {
         this.showTerritories(activeChar);
      } else if (command.startsWith("admin_territory_set_lord")) {
         GameObject target = activeChar.getTarget();
         if (target != null && target.isPlayer()) {
            String terId = "";
            if (st.hasMoreTokens()) {
               Player player = (Player)target;
               terId = st.nextToken();

               for(TerritoryWarManager.Territory teritory : TerritoryWarManager.getInstance().getAllTerritories()) {
                  if (teritory != null && teritory.getTerritoryId() == Integer.parseInt(terId)) {
                     if (teritory.getLordObjectId() == 0) {
                        if (player.getClanId() > 0 && player.isClanLeader() && player.getClan().getCastleId() > 0) {
                           if (player.getClan().getCastleId() == teritory.getCastleId()) {
                              teritory.changeOwner(player.getClan());
                           } else {
                              activeChar.sendMessage("Only clan that owns castle of this territory can become its Lord!");
                           }
                        } else {
                           activeChar.sendMessage("Only clan leader can become Lord!");
                        }
                     } else {
                        activeChar.sendMessage("The territory already has its Lord!");
                     }
                  }
               }
            }
         } else {
            activeChar.sendMessage("Select clan leader, which clan owns this castle territory!");
         }

         this.showTerritories(activeChar);
      } else if (command.startsWith("admin_territory_remove_lord")) {
         String terId = "";
         if (st.hasMoreTokens()) {
            terId = st.nextToken();

            for(TerritoryWarManager.Territory teritory : TerritoryWarManager.getInstance().getAllTerritories()) {
               if (teritory != null && teritory.getTerritoryId() == Integer.parseInt(terId)) {
                  if (teritory.getLordObjectId() > 0) {
                     teritory.changeOwner(null);
                  } else {
                     activeChar.sendMessage("The territory has no Lord!");
                  }
               }
            }
         }

         this.showTerritories(activeChar);
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return _adminCommands;
   }

   private void showTerritories(Player activeChar) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(activeChar, activeChar.getLang(), "data/html/admin/territory-list.htm");
      StringBuilder cList = new StringBuilder(500);
      StringUtil.append(
         cList,
         "<table width=280><tr>",
         "<td width=100><font color=\"c1b33a\">Territory</font></td><td width=60><font color=\"c1b33a\">Status</font></td><td width=120><center>Action</center></td>",
         "</tr></table><img src=\"L2UI.squaregray\" width=\"280\" height=\"1\">"
      );

      for(TerritoryWarManager.Territory teritory : TerritoryWarManager.getInstance().getAllTerritories()) {
         if (teritory != null) {
            String info = "";
            String action;
            if (teritory.getLordObjectId() != 0) {
               action = "<button value=\"Remove Lord\" action=\"bypass -h admin_territory_remove_lord "
                  + teritory.getTerritoryId()
                  + "\" width=100 height=24 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
               info = "<font color=\"FF0000\">Control</font>";
            } else {
               action = "<button value=\"Proclaim Lord\" action=\"bypass -h admin_territory_set_lord "
                  + teritory.getTerritoryId()
                  + "\" width=100 height=24 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
               info = "<font color=\"00FF00\">Empty</font>";
            }

            StringUtil.append(
               cList,
               "<table width=280><tr><td width=100>"
                  + CastleManager.getInstance().getCastleById(teritory.getCastleId()).getName()
                  + "</td><td width=60>"
                  + info
                  + "</td>",
               "<td width=120><center>" + action + "</center></td>",
               "</tr></table><img src=\"L2UI.squaregray\" width=\"280\" height=\"1\">"
            );
         }
      }

      html.replace("%LIST%", cList.toString());
      activeChar.sendPacket(html);
   }

   private void showSiegeTimePage(Player activeChar) {
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/territorywartime.htm");
      adminReply.replace("%time%", TerritoryWarManager.getInstance().getTWStart().getTime().toString());
      activeChar.sendPacket(adminReply);
   }

   private void showMainPage(Player activeChar) {
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/territorywar.htm");
      activeChar.sendPacket(adminhtm);
   }
}
