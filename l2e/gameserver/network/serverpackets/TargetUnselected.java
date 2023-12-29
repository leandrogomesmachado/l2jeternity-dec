package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public class TargetUnselected extends GameServerPacket {
   private final int _targetObjId;
   private final int _x;
   private final int _y;
   private final int _z;

   public TargetUnselected(Creature character) {
      this._targetObjId = character.getObjectId();
      this._x = character.getX();
      this._y = character.getY();
      this._z = character.getZ();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._targetObjId);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
      this.writeD(0);
   }
}
