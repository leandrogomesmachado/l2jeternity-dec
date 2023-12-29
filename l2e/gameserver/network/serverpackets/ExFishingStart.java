package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public class ExFishingStart extends GameServerPacket {
   private final Creature _activeChar;
   private final int _x;
   private final int _y;
   private final int _z;
   private final int _fishType;
   private final boolean _isNightLure;

   public ExFishingStart(Creature character, int fishType, int x, int y, int z, boolean isNightLure) {
      this._activeChar = character;
      this._fishType = fishType;
      this._x = x;
      this._y = y;
      this._z = z;
      this._isNightLure = isNightLure;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._activeChar.getObjectId());
      this.writeD(this._fishType);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
      this.writeC(this._isNightLure ? 1 : 0);
      this.writeC(1);
   }
}
