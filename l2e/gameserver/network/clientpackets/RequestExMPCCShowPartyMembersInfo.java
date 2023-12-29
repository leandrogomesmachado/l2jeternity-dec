package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExMPCCShowPartyMemberInfo;

public final class RequestExMPCCShowPartyMembersInfo extends GameClientPacket {
   private int _partyLeaderId;

   @Override
   protected void readImpl() {
      this._partyLeaderId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         Player player = World.getInstance().getPlayer(this._partyLeaderId);
         if (player != null && player.getParty() != null) {
            activeChar.sendPacket(new ExMPCCShowPartyMemberInfo(player.getParty()));
         }
      }
   }
}
