package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.instance.DoorInstance;

public final class DoorStatusUpdate extends GameServerPacket {
   private final DoorInstance _door;

   public DoorStatusUpdate(DoorInstance door) {
      this._door = door;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._door.getObjectId());
      this.writeD(this._door.getOpen() ? 0 : 1);
      this.writeD(this._door.getDamage());
      this.writeD(this._door.isEnemy() ? 1 : 0);
      this.writeD(this._door.getDoorId());
      this.writeD((int)this._door.getCurrentHp());
      this.writeD((int)this._door.getMaxHp());
   }
}
