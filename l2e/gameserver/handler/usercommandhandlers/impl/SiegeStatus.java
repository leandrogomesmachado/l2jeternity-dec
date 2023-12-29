package l2e.gameserver.handler.usercommandhandlers.impl;

import l2e.gameserver.handler.usercommandhandlers.IUserCommandHandler;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Siege;
import l2e.gameserver.model.zone.type.SiegeZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class SiegeStatus implements IUserCommandHandler {
   private static final int[] COMMAND_IDS = new int[]{99};
   private static final String INSIDE_SIEGE_ZONE = "Castle Siege in Progress";
   private static final String OUTSIDE_SIEGE_ZONE = "No Castle Siege Area";

   @Override
   public boolean useUserCommand(int id, Player activeChar) {
      if (id != COMMAND_IDS[0]) {
         return false;
      } else if (activeChar.isNoble() && activeChar.isClanLeader()) {
         for(Siege siege : SiegeManager.getInstance().getSieges()) {
            if (siege.getIsInProgress()) {
               Clan clan = activeChar.getClan();
               if (siege.checkIsAttacker(clan) || siege.checkIsDefender(clan)) {
                  SiegeZone siegeZone = siege.getCastle().getZone();
                  StringBuilder sb = new StringBuilder();

                  for(Player member : clan.getOnlineMembers(0)) {
                     sb.append("<tr><td width=170>");
                     sb.append(member.getName());
                     sb.append("</td><td width=100>");
                     sb.append(siegeZone.isInsideZone(member) ? "Castle Siege in Progress" : "No Castle Siege Area");
                     sb.append("</td></tr>");
                  }

                  NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
                  html.setFile(activeChar, activeChar.getLang(), "data/html/siege/siege_status.htm");
                  html.replace("%kill_count%", (long)clan.getSiegeKills());
                  html.replace("%death_count%", (long)clan.getSiegeDeaths());
                  html.replace("%member_list%", sb.toString());
                  activeChar.sendPacket(html);
                  return true;
               }
            }
         }

         activeChar.sendPacket(SystemMessageId.ONLY_NOBLESSE_LEADER_CAN_VIEW_SIEGE_STATUS_WINDOW);
         return false;
      } else {
         activeChar.sendPacket(SystemMessageId.ONLY_NOBLESSE_LEADER_CAN_VIEW_SIEGE_STATUS_WINDOW);
         return false;
      }
   }

   @Override
   public int[] getUserCommandList() {
      return COMMAND_IDS;
   }
}
