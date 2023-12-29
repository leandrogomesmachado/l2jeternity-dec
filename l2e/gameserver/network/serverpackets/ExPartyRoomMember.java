package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;

public class ExPartyRoomMember extends GameServerPacket {
   private final int _type;
   private List<ExPartyRoomMember.PartyRoomMemberInfo> _members = Collections.emptyList();

   public ExPartyRoomMember(MatchingRoom room, Player activeChar) {
      this._type = room.getMemberType(activeChar);
      this._members = new ArrayList<>(room.getPlayers().size());

      for(Player $member : room.getPlayers()) {
         this._members.add(new ExPartyRoomMember.PartyRoomMemberInfo($member, room.getMemberType($member)));
      }
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._type);
      this.writeD(this._members.size());

      for(ExPartyRoomMember.PartyRoomMemberInfo member_info : this._members) {
         this.writeD(member_info.objectId);
         this.writeS(member_info.name);
         this.writeD(member_info.classId);
         this.writeD(member_info.level);
         this.writeD(member_info.location);
         this.writeD(member_info.memberType);
         this.writeD(member_info.instanceReuses.size());

         for(int i : member_info.instanceReuses) {
            this.writeD(i);
         }
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
