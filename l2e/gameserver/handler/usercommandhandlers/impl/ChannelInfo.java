package l2e.gameserver.handler.usercommandhandlers.impl;

import l2e.gameserver.handler.usercommandhandlers.IUserCommandHandler;
import l2e.gameserver.model.CommandChannel;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExMultiPartyCommandChannelInfo;

public class ChannelInfo implements IUserCommandHandler {
   private static final int[] COMMAND_IDS = new int[]{97};

   @Override
   public boolean useUserCommand(int id, Player activeChar) {
      if (id != COMMAND_IDS[0]) {
         return false;
      } else if (activeChar.getParty() != null && activeChar.getParty().getCommandChannel() != null) {
         CommandChannel channel = activeChar.getParty().getCommandChannel();
         activeChar.sendPacket(new ExMultiPartyCommandChannelInfo(channel));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public int[] getUserCommandList() {
      return COMMAND_IDS;
   }
}
