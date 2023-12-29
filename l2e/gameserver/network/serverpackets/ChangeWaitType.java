package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public class ChangeWaitType extends GameServerPacket {
   private final int _charObjId;
   private final int _moveType;
   private final int _x;
   private final int _y;
   private final int _z;
   public static final int WT_SITTING = 0;
   public static final int WT_STANDING = 1;
   public static final int WT_START_FAKEDEATH = 2;
   public static final int WT_STOP_FAKEDEATH = 3;

   public ChangeWaitType(Creature character, int newMoveType) {
      this._charObjId = character.getObjectId();
      this._moveType = newMoveType;
      this._x = character.getX();
      this._y = character.getY();
      this._z = character.getZ();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._charObjId);
      this.writeD(this._moveType);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
   }
}
