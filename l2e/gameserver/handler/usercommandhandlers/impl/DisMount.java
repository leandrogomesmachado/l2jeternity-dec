package l2e.gameserver.handler.usercommandhandlers.impl;

import l2e.gameserver.handler.usercommandhandlers.IUserCommandHandler;
import l2e.gameserver.model.actor.Player;

public class DisMount implements IUserCommandHandler {
   private static final int[] COMMAND_IDS = new int[]{62};

   @Override
   public synchronized boolean useUserCommand(int id, Player activeChar) {
      if (id != COMMAND_IDS[0]) {
         return false;
      } else {
         if (activeChar.isRentedPet()) {
            activeChar.stopRentPet();
         } else if (activeChar.isMounted()) {
            activeChar.dismount();
         }

         return true;
      }
   }

   @Override
   public int[] getUserCommandList() {
      return COMMAND_IDS;
   }
}
