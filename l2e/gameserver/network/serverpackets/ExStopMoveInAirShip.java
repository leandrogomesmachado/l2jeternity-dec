package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;

public class ExStopMoveInAirShip extends GameServerPacket {
   private final Player _activeChar;
   private final int _shipObjId;
   private final int x;
   private final int y;
   private final int z;
   private final int h;

   public ExStopMoveInAirShip(Player player, int shipObjId) {
      this._activeChar = player;
      this._shipObjId = shipObjId;
      this.x = player.getInVehiclePosition().getX();
      this.y = player.getInVehiclePosition().getY();
      this.z = player.getInVehiclePosition().getZ();
      this.h = player.getHeading();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._activeChar.getObjectId());
      this.writeD(this._shipObjId);
      this.writeD(this.x);
      this.writeD(this.y);
      this.writeD(this.z);
      this.writeD(this.h);
   }
}
