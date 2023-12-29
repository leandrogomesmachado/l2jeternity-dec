package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.matching.MatchingRoom;

public class ExMpccRoomInfo extends GameServerPacket {
   private final int _index;
   private final int _memberSize;
   private final int _minLevel;
   private final int _maxLevel;
   private final int _lootType;
   private final int _locationId;
   private final String _topic;

   public ExMpccRoomInfo(MatchingRoom matching) {
      this._index = matching.getId();
      this._locationId = matching.getLocationId();
      this._topic = matching.getTopic();
      this._minLevel = matching.getMinLevel();
      this._maxLevel = matching.getMaxLevel();
      this._memberSize = matching.getMaxMembersSize();
      this._lootType = matching.getLootType();
   }

   @Override
   public void writeImpl() {
      this.writeD(this._index);
      this.writeD(this._memberSize);
      this.writeD(this._minLevel);
      this.writeD(this._maxLevel);
      this.writeD(this._lootType);
      this.writeD(this._locationId);
      this.writeS(this._topic);
   }
}
