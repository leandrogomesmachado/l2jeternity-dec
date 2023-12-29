package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public class ExGetOffAirShip extends GameServerPacket {
   private final int _playerId;
   private final int _airShipId;
   private final int _x;
   private final int _y;
   private final int _z;

   public ExGetOffAirShip(Creature player, Creature ship, int x, int y, int z) {
      this._playerId = player.getObjectId();
      this._airShipId = ship.getObjectId();
      this._x = x;
      this._y = y;
      this._z = z;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._playerId);
      this.writeD(this._airShipId);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
   }
}
