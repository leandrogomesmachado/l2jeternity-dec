package l2e.gameserver.handler.usercommandhandlers.impl;

import l2e.gameserver.handler.usercommandhandlers.IUserCommandHandler;
import l2e.gameserver.model.CommandChannel;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ChannelDelete implements IUserCommandHandler {
   private static final int[] COMMAND_IDS = new int[]{93};

   @Override
   public boolean useUserCommand(int id, Player activeChar) {
      if (id != COMMAND_IDS[0]) {
         return false;
      } else if (activeChar.isInParty()
         && activeChar.getParty().isLeader(activeChar)
         && activeChar.getParty().isInCommandChannel()
         && activeChar.getParty().getCommandChannel().getLeader().equals(activeChar)) {
         CommandChannel channel = activeChar.getParty().getCommandChannel();
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_DISBANDED);
         channel.broadCast(sm);
         channel.disbandChannel();
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
