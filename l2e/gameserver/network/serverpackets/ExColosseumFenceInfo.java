package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.ColosseumFence;

public class ExColosseumFenceInfo extends GameServerPacket {
   private final ColosseumFence _fence;

   public ExColosseumFenceInfo(ColosseumFence fence) {
      this._fence = fence;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._fence.getObjectId());
      this.writeD(this._fence.getFenceState().ordinal());
      this.writeD(this._fence.getX());
      this.writeD(this._fence.getY());
      this.writeD(this._fence.getZ());
      this.writeD(this._fence.getFenceWidth());
      this.writeD(this._fence.getFenceHeight());
   }
}
