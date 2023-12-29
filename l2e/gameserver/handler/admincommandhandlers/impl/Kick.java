package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.StringTokenizer;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;

public class Kick implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_kick", "admin_kick_non_gm"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_kick")) {
         StringTokenizer st = new StringTokenizer(command);
         if (st.countTokens() > 1) {
            st.nextToken();
            String player = st.nextToken();
            Player plyr = World.getInstance().getPlayer(player);
            if (plyr != null) {
               if (plyr.isInOfflineMode()) {
                  plyr.unsetVar("offline");
                  plyr.unsetVar("storemode");
               }

               if (plyr.isSellingBuffs()) {
                  plyr.unsetVar("offlineBuff");
               }

               plyr.kick();
               activeChar.sendMessage("You kicked " + plyr.getName() + " from the game.");
            }
         }
      }

      if (command.startsWith("admin_kick_non_gm")) {
         int counter = 0;

         for(Player player : World.getInstance().getAllPlayers()) {
            if (!player.isGM()) {
               ++counter;
               if (player.isInOfflineMode()) {
                  player.unsetVar("offline");
                  player.unsetVar("storemode");
               }

               if (player.isSellingBuffs()) {
                  player.unsetVar("offlineBuff");
               }

               player.logout();
            }
         }

         activeChar.sendMessage("Kicked " + counter + " players");
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
