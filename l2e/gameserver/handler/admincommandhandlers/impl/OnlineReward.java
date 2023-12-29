package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.OnlineRewardManager;
import l2e.gameserver.model.actor.Player;

public class OnlineReward implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_onlinerewards"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_onlinereward") && OnlineRewardManager.getInstance().reloadRewards()) {
         activeChar.sendMessage("Online Rewards Reloaded!");
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
