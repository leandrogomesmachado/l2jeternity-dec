package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;

public final class Ride extends GameServerPacket {
   private final int _objectId;
   private final int _mounted;
   private final int _rideType;
   private final int _rideNpcId;
   private final Location _loc;

   public Ride(Player player) {
      this._objectId = player.getObjectId();
      this._mounted = player.isMounted() ? 1 : 0;
      this._rideType = player.getMountType().ordinal();
      this._rideNpcId = player.getMountNpcId() + 1000000;
      this._loc = player.getLocation();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objectId);
      this.writeD(this._mounted);
      this.writeD(this._rideType);
      this.writeD(this._rideNpcId);
      this.writeD(this._loc.getX());
      this.writeD(this._loc.getY());
      this.writeD(this._loc.getZ());
   }
}
