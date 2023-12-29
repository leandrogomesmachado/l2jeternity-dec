package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestExOustFromMPCC extends GameClientPacket {
   private String _name;

   @Override
   protected void readImpl() {
      this._name = this.readS();
   }

   @Override
   protected void runImpl() {
      Player target = World.getInstance().getPlayer(this._name);
      Player activeChar = this.getClient().getActiveChar();
      if (target != null
         && target.isInParty()
         && activeChar.isInParty()
         && activeChar.getParty().isInCommandChannel()
         && target.getParty().isInCommandChannel()
         && activeChar.getParty().getCommandChannel().getLeader().equals(activeChar)
         && activeChar.getParty().getCommandChannel().equals(target.getParty().getCommandChannel())) {
         if (activeChar.equals(target)) {
            return;
         }

         target.getParty().getCommandChannel().removeParty(target.getParty());
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DISMISSED_FROM_COMMAND_CHANNEL);
         target.getParty().broadCast(sm);
         if (activeChar.getParty().isInCommandChannel()) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PARTY_DISMISSED_FROM_COMMAND_CHANNEL);
            sm.addString(target.getParty().getLeader().getName());
            activeChar.getParty().getCommandChannel().broadCast(sm);
         }
      } else {
         activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
      }
   }
}
