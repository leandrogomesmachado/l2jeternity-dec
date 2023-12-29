package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.instance.DoorInstance;

public final class DoorInfo extends GameServerPacket {
   private final int _objId;
   private final int _doorId;
   private final int _viewHp;

   public DoorInfo(DoorInstance door) {
      this._objId = door.getObjectId();
      this._doorId = door.getDoorId();
      this._viewHp = door.getIsShowHp() ? 1 : 0;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objId);
      this.writeD(this._doorId);
      this.writeD(this._viewHp);
   }
}
