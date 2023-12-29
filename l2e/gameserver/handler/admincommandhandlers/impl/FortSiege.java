package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.List;
import java.util.StringTokenizer;
import l2e.commons.util.StringUtil;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class FortSiege implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_fortsiege",
      "admin_add_fortattacker",
      "admin_list_fortsiege_clans",
      "admin_clear_fortsiege_list",
      "admin_spawn_fortdoors",
      "admin_endfortsiege",
      "admin_startfortsiege",
      "admin_setfort",
      "admin_removefort"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      StringTokenizer st = new StringTokenizer(command, " ");
      command = st.nextToken();
      Fort fort = null;
      int fortId = 0;
      if (st.hasMoreTokens()) {
         fortId = Integer.parseInt(st.nextToken());
         fort = FortManager.getInstance().getFortById(fortId);
      }

      if (fort != null && fortId != 0) {
         GameObject target = activeChar.getTarget();
         Player player = null;
         if (target instanceof Player) {
            player = (Player)target;
         }

         if (command.equalsIgnoreCase("admin_add_fortattacker")) {
            if (player == null) {
               activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            } else if (fort.getSiege().checkIfCanRegister(player)) {
               fort.getSiege().registerAttacker(player, true);
            }
         } else if (command.equalsIgnoreCase("admin_clear_fortsiege_list")) {
            fort.getSiege().clearSiegeClan();
         } else if (command.equalsIgnoreCase("admin_endfortsiege")) {
            fort.getSiege().endSiege();
         } else if (command.equalsIgnoreCase("admin_list_fortsiege_clans")) {
            activeChar.sendMessage("Not implemented yet.");
         } else if (command.equalsIgnoreCase("admin_setfort")) {
            if (player != null && player.getClan() != null) {
               fort.setOwner(player.getClan(), false);
            } else {
               activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            }
         } else if (command.equalsIgnoreCase("admin_removefort")) {
            Clan clan = fort.getOwnerClan();
            if (clan != null) {
               fort.removeOwner(true);
            } else {
               activeChar.sendMessage("Unable to remove fort");
            }
         } else if (command.equalsIgnoreCase("admin_spawn_fortdoors")) {
            fort.resetDoors();
         } else if (command.equalsIgnoreCase("admin_startfortsiege")) {
            fort.getSiege().startSiege();
         }

         this.showFortSiegePage(activeChar, fort);
      } else {
         this.showFortSelectPage(activeChar);
      }

      return true;
   }

   private void showFortSelectPage(Player activeChar) {
      int i = 0;
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/forts.htm");
      List<Fort> forts = FortManager.getInstance().getForts();
      StringBuilder cList = new StringBuilder(forts.size() * 100);

      for(Fort fort : forts) {
         if (fort != null) {
            StringUtil.append(
               cList,
               "<td fixwidth=90><a action=\"bypass -h admin_fortsiege ",
               String.valueOf(fort.getId()),
               "\">",
               fort.getName(),
               " id: ",
               String.valueOf(fort.getId()),
               "</a></td>"
            );
            ++i;
         }

         if (i > 2) {
            cList.append("</tr><tr>");
            i = 0;
         }
      }

      adminReply.replace("%forts%", cList.toString());
      activeChar.sendPacket(adminReply);
   }

   private void showFortSiegePage(Player activeChar, Fort fort) {
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/fort.htm");
      adminReply.replace("%fortName%", fort.getName());
      adminReply.replace("%fortId%", String.valueOf(fort.getId()));
      activeChar.sendPacket(adminReply);
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
