package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.handler.communityhandlers.CommunityBoardHandler;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.model.actor.Player;

public class Balancer implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_balancer"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.equals("admin_balancer")) {
         ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler("_bbs_balancer");
         if (handler != null) {
            handler.onBypassCommand("_bbs_balancer", activeChar);
         }
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
