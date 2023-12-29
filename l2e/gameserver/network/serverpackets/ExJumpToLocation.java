package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Location;

public class ExJumpToLocation extends GameServerPacket {
   private final int _objectId;
   private final Location _current;
   private final Location _destination;

   public ExJumpToLocation(int objectId, Location from, Location to) {
      this._objectId = objectId;
      this._current = from;
      this._destination = to;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objectId);
      this.writeD(this._destination.getX());
      this.writeD(this._destination.getY());
      this.writeD(this._destination.getZ());
      this.writeD(this._current.getX());
      this.writeD(this._current.getY());
      this.writeD(this._current.getZ());
   }
}
