package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public class VehicleStarted extends GameServerPacket {
   private final int _objectId;
   private final int _state;

   public VehicleStarted(Creature boat, int state) {
      this._objectId = boat.getObjectId();
      this._state = state;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._objectId);
      this.writeD(this._state);
   }
}
