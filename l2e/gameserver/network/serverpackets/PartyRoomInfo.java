package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.matching.MatchingRoom;

public class PartyRoomInfo extends GameServerPacket {
   private final int _id;
   private final int _minLevel;
   private final int _maxLevel;
   private final int _lootDist;
   private final int _maxMembers;
   private final int _location;
   private final String _title;

   public PartyRoomInfo(MatchingRoom room) {
      this._id = room.getId();
      this._minLevel = room.getMinLevel();
      this._maxLevel = room.getMaxLevel();
      this._lootDist = room.getLootType();
      this._maxMembers = room.getMaxMembersSize();
      this._location = room.getLocationId();
      this._title = room.getTopic();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._id);
      this.writeD(this._maxMembers);
      this.writeD(this._minLevel);
      this.writeD(this._maxLevel);
      this.writeD(this._lootDist);
      this.writeD(this._location);
      this.writeS(this._title);
   }
}
