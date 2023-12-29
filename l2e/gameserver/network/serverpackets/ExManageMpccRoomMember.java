package l2e.gameserver.network.serverpackets;

import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;

public class ExManageMpccRoomMember extends GameServerPacket {
   public static int ADD_MEMBER = 0;
   public static int UPDATE_MEMBER = 1;
   public static int REMOVE_MEMBER = 2;
   private final int _type;
   private final ExManageMpccRoomMember.MpccRoomMemberInfo _memberInfo;

   public ExManageMpccRoomMember(int type, MatchingRoom room, Player target) {
      this._type = type;
      this._memberInfo = new ExManageMpccRoomMember.MpccRoomMemberInfo(target, room.getMemberType(target));
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._type);
      this.writeD(this._memberInfo.objectId);
      this.writeS(this._memberInfo.name);
      this.writeD(this._memberInfo.level);
      this.writeD(this._memberInfo.classId);
      this.writeD(this._memberInfo.location);
      this.writeD(this._memberInfo.memberType);
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
