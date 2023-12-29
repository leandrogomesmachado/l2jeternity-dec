package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.StringTokenizer;
import l2e.commons.util.Util;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.ClanHallManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Clans implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_clan_info", "admin_clan_changeleader", "admin_clan_show_pending", "admin_clan_force_pending"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      StringTokenizer st = new StringTokenizer(command);
      String cmd = st.nextToken();
      switch(cmd) {
         case "admin_clan_info":
            Player player = this.getPlayer(activeChar, st);
            if (player != null) {
               Clan clan = player.getClan();
               if (clan == null) {
                  activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
                  return false;
               }

               NpcHtmlMessage html = new NpcHtmlMessage(0, 5);
               html.setFile(activeChar, activeChar.getLang(), "data/html/admin/claninfo.htm");
               html.replace("%clan_name%", clan.getName());
               html.replace("%clan_leader%", clan.getLeaderName());
               html.replace("%clan_level%", String.valueOf(clan.getLevel()));
               html.replace("%clan_has_castle%", clan.getCastleId() > 0 ? CastleManager.getInstance().getCastleById(clan.getCastleId()).getName() : "No");
               html.replace(
                  "%clan_has_clanhall%",
                  clan.getHideoutId() > 0 ? Util.clanHallName(activeChar, ClanHallManager.getInstance().getClanHallById(clan.getHideoutId()).getId()) : "No"
               );
               html.replace("%clan_has_fortress%", clan.getFortId() > 0 ? FortManager.getInstance().getFortById(clan.getFortId()).getName() : "No");
               html.replace("%clan_points%", String.valueOf(clan.getReputationScore()));
               html.replace("%clan_players_count%", String.valueOf(clan.getMembersCount()));
               html.replace("%clan_ally%", clan.getAllyId() > 0 ? clan.getAllyName() : "Not in ally");
               html.replace("%current_player_objectId%", String.valueOf(player.getObjectId()));
               html.replace("%current_player_name%", player.getName());
               activeChar.sendPacket(html);
            }
            break;
         case "admin_clan_changeleader":
            Player player = this.getPlayer(activeChar, st);
            if (player != null) {
               Clan clan = player.getClan();
               if (clan == null) {
                  activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
                  return false;
               }

               ClanMember member = clan.getClanMember(player.getObjectId());
               if (member != null) {
                  if (player.isAcademyMember()) {
                     player.sendPacket(SystemMessageId.RIGHT_CANT_TRANSFERRED_TO_ACADEMY_MEMBER);
                  } else {
                     clan.setNewLeader(member);
                  }
               }
            }
            break;
         case "admin_clan_show_pending":
            NpcHtmlMessage html = new NpcHtmlMessage(0, 5);
            html.setFile(activeChar, activeChar.getLang(), "data/html/admin/clanchanges.htm");
            StringBuilder sb = new StringBuilder();

            for(Clan clan : ClanHolder.getInstance().getClans()) {
               if (clan.getNewLeaderId() != 0) {
                  sb.append("<tr>");
                  sb.append("<td>" + clan.getName() + "</td>");
                  sb.append("<td>" + clan.getNewLeaderName() + "</td>");
                  sb.append("<td><a action=\"bypass -h admin_clan_force_pending " + clan.getId() + "\">Force</a></td>");
                  sb.append("</tr>");
               }
            }

            html.replace("%data%", sb.toString());
            activeChar.sendPacket(html);
            break;
         case "admin_clan_force_pending":
            if (st.hasMoreElements()) {
               String token = st.nextToken();
               if (Util.isDigit(token)) {
                  int clanId = Integer.parseInt(token);
                  Clan clan = ClanHolder.getInstance().getClan(clanId);
                  if (clan != null) {
                     ClanMember member = clan.getClanMember(clan.getNewLeaderId());
                     if (member != null) {
                        clan.setNewLeader(member);
                        activeChar.sendMessage("Task have been forcely executed.");
                     }
                  }
               }
            }
      }

      return true;
   }

   private Player getPlayer(Player activeChar, StringTokenizer st) {
      Player player = null;
      if (st.hasMoreTokens()) {
         String val = st.nextToken();
         if (Util.isDigit(val)) {
            player = World.getInstance().getPlayer(Integer.parseInt(val));
            if (player == null) {
               activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
               return null;
            }
         } else {
            player = World.getInstance().getPlayer(val);
            if (player == null) {
               activeChar.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
               return null;
            }
         }
      } else {
         GameObject targetObj = activeChar.getTarget();
         if (!(targetObj instanceof Player)) {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return null;
         }

         player = targetObj.getActingPlayer();
      }

      return player;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
