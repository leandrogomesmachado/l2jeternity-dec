package l2e.gameserver.handler.usercommandhandlers.impl;

import l2e.gameserver.handler.usercommandhandlers.IUserCommandHandler;
import l2e.gameserver.model.CommandChannel;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ChannelLeave implements IUserCommandHandler {
   private static final int[] COMMAND_IDS = new int[]{96};

   @Override
   public boolean useUserCommand(int id, Player activeChar) {
      if (id != COMMAND_IDS[0]) {
         return false;
      } else if (!activeChar.isInParty() || !activeChar.getParty().isLeader(activeChar)) {
         activeChar.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_LEAVE_CHANNEL);
         return false;
      } else if (activeChar.getParty().isInCommandChannel()) {
         CommandChannel channel = activeChar.getParty().getCommandChannel();
         Party party = activeChar.getParty();
         channel.removeParty(party);
         party.getLeader().sendPacket(SystemMessageId.LEFT_COMMAND_CHANNEL);
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PARTY_LEFT_COMMAND_CHANNEL);
         sm.addPcName(party.getLeader());
         channel.broadCast(sm);
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
