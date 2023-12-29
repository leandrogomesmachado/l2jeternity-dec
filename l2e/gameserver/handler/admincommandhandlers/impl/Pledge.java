package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.GMViewPledgeInfo;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Pledge implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_pledge", "admin_pledge_info"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      GameObject target = activeChar.getTarget();
      Player player = null;
      if (target instanceof Player) {
         player = (Player)target;
         String name = player.getName();
         if (command.equals("admin_pledge_info")) {
            this.showMainPage(activeChar);
            return true;
         } else {
            if (command.startsWith("admin_pledge")) {
               String action = null;
               String parameter = null;
               StringTokenizer st = new StringTokenizer(command);

               try {
                  st.nextToken();
                  action = st.nextToken();
                  parameter = st.nextToken();
               } catch (NoSuchElementException var13) {
                  return false;
               }

               if (action.equals("create")) {
                  long cet = player.getClanCreateExpiryTime();
                  player.setClanCreateExpiryTime(0L);
                  Clan clan = ClanHolder.getInstance().createClan(player, parameter);
                  if (clan != null) {
                     activeChar.sendMessage("Clan " + parameter + " created. Leader: " + player.getName());
                  } else {
                     player.setClanCreateExpiryTime(cet);
                     activeChar.sendMessage("There was a problem while creating the clan.");
                  }
               } else {
                  if (!player.isClanLeader()) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER);
                     sm.addString(name);
                     activeChar.sendPacket(sm);
                     this.showMainPage(activeChar);
                     return false;
                  }

                  if (action.equals("dismiss")) {
                     ClanHolder.getInstance().destroyClan(player.getClanId());
                     Clan clan = player.getClan();
                     if (clan == null) {
                        activeChar.sendMessage("Clan disbanded.");
                     } else {
                        activeChar.sendMessage("There was a problem while destroying the clan.");
                     }
                  } else if (action.equals("info")) {
                     activeChar.sendPacket(new GMViewPledgeInfo(player.getClan(), player));
                  } else if (parameter == null) {
                     activeChar.sendMessage("Usage: //pledge <setlevel|rep> <number>");
                  } else if (action.equals("setlevel")) {
                     int level = Integer.parseInt(parameter);
                     if (level >= 0 && level < 12) {
                        player.getClan().changeLevel(level, true);
                        activeChar.sendMessage("You set level " + level + " for clan " + player.getClan().getName());
                     } else {
                        activeChar.sendMessage("Level incorrect.");
                     }
                  } else if (action.startsWith("rep")) {
                     try {
                        int points = Integer.parseInt(parameter);
                        Clan clan = player.getClan();
                        if (clan.getLevel() < 5) {
                           activeChar.sendMessage("Only clans of level 5 or above may receive reputation points.");
                           this.showMainPage(activeChar);
                           return false;
                        }

                        clan.addReputationScore(points, true);
                        activeChar.sendMessage(
                           "You "
                              + (points > 0 ? "add " : "remove ")
                              + Math.abs(points)
                              + " points "
                              + (points > 0 ? "to " : "from ")
                              + clan.getName()
                              + "'s reputation. Their current score is "
                              + clan.getReputationScore()
                        );
                     } catch (Exception var12) {
                        activeChar.sendMessage("Usage: //pledge <rep> <number>");
                     }
                  }
               }
            }

            this.showMainPage(activeChar);
            return true;
         }
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
         this.showMainPage(activeChar);
         return false;
      }
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void showMainPage(Player activeChar) {
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/pledgeinfo.htm");
      activeChar.sendPacket(adminhtm);
   }
}
