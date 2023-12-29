package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;

public class ExMPCCPartyInfoUpdate extends GameServerPacket {
   private final Party _party;
   Player _leader;
   private final int _mode;
   private final int _count;

   public ExMPCCPartyInfoUpdate(Party party, int mode) {
      this._party = party;
      this._mode = mode;
      this._count = this._party.getMemberCount();
      this._leader = this._party.getLeader();
   }

   @Override
   protected void writeImpl() {
      this.writeS(this._leader.getName());
      this.writeD(this._leader.getObjectId());
      this.writeD(this._count);
      this.writeD(this._mode);
   }
}
