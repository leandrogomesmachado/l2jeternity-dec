package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.CommandChannel;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExAskJoinMPCC;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestExAskJoinMPCC extends GameClientPacket {
   private String _name;

   @Override
   protected void readImpl() {
      this._name = this.readS();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         Player target = World.getInstance().getPlayer(this._name);
         if (target == null) {
            activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
         } else {
            Player resultTarget = CommandChannel.checkAndAskToCreateChannel(activeChar, target);
            Party activeParty = activeChar.getParty();
            if (resultTarget != null) {
               if (activeParty.isInCommandChannel()) {
                  if (activeParty.getCommandChannel().getLeader() != activeChar) {
                     activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_TO_COMMAND_CHANNEL);
                     return;
                  }

                  activeChar.onTransactionRequest(target);
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_CONFIRM_FROM_C1);
                  sm.addString(activeChar.getName());
                  target.sendPacket(sm);
                  target.sendPacket(new ExAskJoinMPCC(activeChar.getName()));
               } else {
                  activeChar.onTransactionRequest(target);
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_CONFIRM_FROM_C1);
                  sm.addString(activeChar.getName());
                  target.sendPacket(sm);
                  target.sendPacket(new ExAskJoinMPCC(activeChar.getName()));
               }
            }
         }
      }
   }
}
