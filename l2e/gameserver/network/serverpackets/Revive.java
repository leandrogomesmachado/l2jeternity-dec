package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.GameObject;

public final class Revive extends GameServerPacket {
   private final int _objectId;

   public Revive(GameObject obj) {
      this._objectId = obj.getObjectId();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objectId);
   }
}
