package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;

public class ExGetOnAirShip extends GameServerPacket {
   private final int _playerId;
   private final int _airShipId;
   private final Location _pos;

   public ExGetOnAirShip(Player player, Creature ship) {
      this._playerId = player.getObjectId();
      this._airShipId = ship.getObjectId();
      this._pos = player.getInVehiclePosition();
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._playerId);
      this.writeD(this._airShipId);
      this.writeD(this._pos.getX());
      this.writeD(this._pos.getY());
      this.writeD(this._pos.getZ());
   }
}
