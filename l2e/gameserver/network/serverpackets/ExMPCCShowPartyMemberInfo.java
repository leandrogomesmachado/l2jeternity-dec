package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;

public class ExMPCCShowPartyMemberInfo extends GameServerPacket {
   private final Party _party;

   public ExMPCCShowPartyMemberInfo(Party party) {
      this._party = party;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._party.getMemberCount());

      for(Player pc : this._party.getMembers()) {
         this.writeS(pc.getName());
         this.writeD(pc.getObjectId());
         this.writeD(pc.getClassId().getId());
      }
   }
}
