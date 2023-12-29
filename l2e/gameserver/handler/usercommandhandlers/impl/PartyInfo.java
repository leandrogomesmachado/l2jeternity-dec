package l2e.gameserver.handler.usercommandhandlers.impl;

import l2e.gameserver.handler.usercommandhandlers.IUserCommandHandler;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class PartyInfo implements IUserCommandHandler {
   private static final int[] COMMAND_IDS = new int[]{81};

   @Override
   public boolean useUserCommand(int id, Player activeChar) {
      if (id != COMMAND_IDS[0]) {
         return false;
      } else {
         activeChar.sendPacket(SystemMessageId.PARTY_INFORMATION);
         if (activeChar.isInParty()) {
            Party party = activeChar.getParty();
            switch(party.getLootDistribution()) {
               case 0:
                  activeChar.sendPacket(SystemMessageId.LOOTING_FINDERS_KEEPERS);
                  break;
               case 1:
                  activeChar.sendPacket(SystemMessageId.LOOTING_RANDOM);
                  break;
               case 2:
                  activeChar.sendPacket(SystemMessageId.LOOTING_RANDOM_INCLUDE_SPOIL);
                  break;
               case 3:
                  activeChar.sendPacket(SystemMessageId.LOOTING_BY_TURN);
                  break;
               case 4:
                  activeChar.sendPacket(SystemMessageId.LOOTING_BY_TURN_INCLUDE_SPOIL);
            }

            if (!party.isLeader(activeChar)) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PARTY_LEADER_C1);
               sm.addPcName(party.getLeader());
               activeChar.sendPacket(sm);
            }

            activeChar.sendMessage("Members: " + party.getMemberCount() + "/9");
         }

         activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
         return true;
      }
   }

   @Override
   public int[] getUserCommandList() {
      return COMMAND_IDS;
   }
}
