package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;

public class ExManagePartyRoomMember extends GameServerPacket {
   private final int _type;
   private final ExManagePartyRoomMember.PartyRoomMemberInfo _memberInfo;

   public ExManagePartyRoomMember(Player player, MatchingRoom room, int type) {
      this._type = type;
      this._memberInfo = new ExManagePartyRoomMember.PartyRoomMemberInfo(player, room.getMemberType(player));
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._type);
      this.writeD(this._memberInfo.objectId);
      this.writeS(this._memberInfo.name);
      this.writeD(this._memberInfo.classId);
      this.writeD(this._memberInfo.level);
      this.writeD(this._memberInfo.location);
      this.writeD(this._memberInfo.memberType);
      this.writeD(this._memberInfo.instanceReuses.size());

      for(int i : this._memberInfo.instanceReuses) {
         this.writeD(i);
      }
   }

   static class PartyRoomMemberInfo {
      public final int objectId;
      public final int classId;
      public final int level;
      public final int location;
      public final int memberType;
      public final String name;
      public final List<Integer> instanceReuses;

      public PartyRoomMemberInfo(Player member, int type) {
         this.objectId = member.getObjectId();
         this.name = member.getName();
         this.classId = member.getClassId().ordinal();
         this.level = member.getLevel();
         this.location = MapRegionManager.getInstance().getBBs(member.getLocation());
         this.memberType = type;
         this.instanceReuses = ReflectionManager.getInstance().getLockedReflectionList(member.getObjectId());
      }
   }
}
