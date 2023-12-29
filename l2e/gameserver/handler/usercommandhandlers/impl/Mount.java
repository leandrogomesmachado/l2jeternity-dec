package l2e.gameserver.handler.usercommandhandlers.impl;

import l2e.gameserver.handler.usercommandhandlers.IUserCommandHandler;
import l2e.gameserver.model.actor.Player;

public class Mount implements IUserCommandHandler {
   private static final int[] COMMAND_IDS = new int[]{61};

   @Override
   public synchronized boolean useUserCommand(int id, Player activeChar) {
      return id != COMMAND_IDS[0] ? false : activeChar.mountPlayer(activeChar.getSummon());
   }

   @Override
   public int[] getUserCommandList() {
      return COMMAND_IDS;
   }
}
