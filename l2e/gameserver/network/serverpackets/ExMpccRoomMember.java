package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;

public class ExMpccRoomMember extends GameServerPacket {
   private final int _type;
   private List<ExMpccRoomMember.MpccRoomMemberInfo> _members = Collections.emptyList();

   public ExMpccRoomMember(MatchingRoom room, Player player) {
      this._type = room.getMemberType(player);
      this._members = new ArrayList<>(room.getPlayers().size());

      for(Player member : room.getPlayers()) {
         this._members.add(new ExMpccRoomMember.MpccRoomMemberInfo(member, room.getMemberType(member)));
      }
   }

   @Override
   public void writeImpl() {
      this.writeD(this._type);
      this.writeD(this._members.size());

      for(ExMpccRoomMember.MpccRoomMemberInfo member : this._members) {
         this.writeD(member.objectId);
         this.writeS(member.name);
         this.writeD(member.level);
         this.writeD(member.classId);
         this.writeD(member.location);
         this.writeD(member.memberType);
      }
   }

   static class MpccRoomMemberInfo {
      public final int objectId;
      public final int classId;
      public final int level;
      public final int location;
      public final int memberType;
      public final String name;

      public MpccRoomMemberInfo(Player member, int type) {
         this.objectId = member.getObjectId();
         this.name = member.getName();
         this.classId = member.getClassId().ordinal();
         this.level = member.getLevel();
         this.location = MapRegionManager.getInstance().getBBs(member.getLocation());
         this.memberType = type;
      }
   }
}
