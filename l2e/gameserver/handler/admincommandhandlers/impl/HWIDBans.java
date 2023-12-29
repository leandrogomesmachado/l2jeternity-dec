package l2e.gameserver.handler.admincommandhandlers.impl;

import java.sql.SQLException;
import java.util.StringTokenizer;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import org.strixplatform.StrixPlatform;
import org.strixplatform.logging.Log;
import org.strixplatform.managers.ClientBanManager;
import org.strixplatform.utils.BannedHWIDInfo;

public class HWIDBans implements IAdminCommandHandler {
   private static String[] _adminCommands = new String[]{"admin_hwid_ban", "admin_unban_hwid"};

   @Override
   public boolean useAdminCommand(String command, Player player) {
      StringTokenizer st = new StringTokenizer(command, " ");
      st.nextToken();
      if (command.equalsIgnoreCase("admin_hwid_ban")) {
         this.showMenu(player);
         return true;
      } else {
         if (command.startsWith("admin_hwid_ban")) {
            if (!StrixPlatform.getInstance().isPlatformEnabled()) {
               player.sendMessage("Strix Guard Disabled!");
               this.showMenu(player);
               return true;
            }

            Player targetPlayer = null;
            if (player.getTarget() != null) {
               targetPlayer = player.getTarget().getActingPlayer();
            } else {
               String playeraName = st.nextToken();
               targetPlayer = World.getInstance().getPlayer(playeraName);
            }

            if (targetPlayer != null) {
               try {
                  Long time = Long.parseLong(st.nextToken());
                  String reason = st.nextToken();
                  BannedHWIDInfo bhi = new BannedHWIDInfo(
                     targetPlayer.getClient().getStrixClientData().getClientHWID(), System.currentTimeMillis() + time * 60L * 1000L, reason, player.getName()
                  );
                  ClientBanManager.getInstance().tryToStoreBan(bhi);
                  String bannedOut = "Player [Name:{"
                     + targetPlayer.getName()
                     + "}HWID:{"
                     + targetPlayer.getClient().getStrixClientData().getClientHWID()
                     + "}] banned on ["
                     + time
                     + "] minutes from ["
                     + reason
                     + "] reason.";
                  player.sendMessage(bannedOut);
                  Log.audit(bannedOut);
                  targetPlayer.sendMessage("You banned on [" + time + "] minutes. Reason: " + reason);
                  targetPlayer.logout();
               } catch (Exception var10) {
                  if (var10 instanceof SQLException) {
                     player.sendMessage("Unable to store ban in database. Please check Strix-Platform error log!");
                     Log.error("Exception on GM trying store ban. Exception: " + var10.getLocalizedMessage());
                  } else {
                     player.sendMessage("Command syntax: //hwid_ban PLAYER_NAME(or target) TIME(in minutes) REASON(255 max)");
                  }
               }
            } else {
               player.sendMessage("Unable to find this player.");
            }

            this.showMenu(player);
         } else if (command.startsWith("admin_unban_hwid")) {
            if (!StrixPlatform.getInstance().isPlatformEnabled()) {
               player.sendMessage("Strix Guard Disabled!");
               this.showMenu(player);
               return true;
            }

            String playeraHWID = st.nextToken();
            if (playeraHWID != null && playeraHWID.length() == 32) {
               try {
                  ClientBanManager.getInstance().tryToDeleteBan(playeraHWID);
                  player.sendMessage("Player unbaned and delete from database.");
               } catch (Exception var9) {
                  if (var9 instanceof SQLException) {
                     player.sendMessage("Unable to delete ban from database. Please check Strix-Platform error log!");
                     Log.error("Exception on GM trying delete ban. Exception: " + var9.getLocalizedMessage());
                  } else {
                     player.sendMessage("Command syntax: //unban_hwid HWID_STRING(size 32)");
                  }
               }
            } else {
               player.sendMessage("Command syntax: //unban_hwid HWID_STRING(size 32)");
            }

            this.showMenu(player);
         }

         return true;
      }
   }

   private void showMenu(Player activeChar) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(activeChar, activeChar.getLang(), "data/html/admin/hwidban.htm");
      activeChar.sendPacket(html);
   }

   @Override
   public String[] getAdminCommandList() {
      return _adminCommands;
   }
}
