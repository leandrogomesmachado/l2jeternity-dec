package l2e.gameserver.model.matching;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.instancemanager.MatchingRoomManager;
import l2e.gameserver.model.PlayerGroup;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.SystemMessage;

public abstract class MatchingRoom implements PlayerGroup {
   public static int PARTY_MATCHING = 0;
   public static int CC_MATCHING = 1;
   public static int WAIT_PLAYER = 0;
   public static int ROOM_MASTER = 1;
   public static int PARTY_MEMBER = 2;
   public static int UNION_LEADER = 3;
   public static int UNION_PARTY = 4;
   public static int WAIT_PARTY = 5;
   public static int WAIT_NORMAL = 6;
   private final int _id;
   private int _minLevel;
   private int _maxLevel;
   private int _maxMemberSize;
   private int _lootType;
   private String _topic;
   protected Player _leader;
   protected Set<Player> _members = new CopyOnWriteArraySet<>();

   public MatchingRoom(Player leader, int minLevel, int maxLevel, int maxMemberSize, int lootType, String topic) {
      this._leader = leader;
      this._id = MatchingRoomManager.getInstance().addMatchingRoom(this);
      this._minLevel = minLevel;
      this._maxLevel = maxLevel;
      this._maxMemberSize = maxMemberSize;
      this._lootType = lootType;
      this._topic = topic;
      this.addMember0(leader, null, true);
   }

   public boolean addMember(Player player) {
      if (this._members.contains(player)) {
         return true;
      } else if (player.getLevel() >= this.getMinLevel() && player.getLevel() <= this.getMaxLevel() && this.getPlayers().size() < this.getMaxMembersSize()) {
         return this.addMember0(player, SystemMessage.getSystemMessage(this.enterMessage()).addPcName(player), true);
      } else {
         player.sendPacket(this.notValidMessage());
         return false;
      }
   }

   public boolean addMemberForce(Player player) {
      if (this._members.contains(player)) {
         return true;
      } else if (this.getPlayers().size() >= this.getMaxMembersSize()) {
         player.sendPacket(this.notValidMessage());
         return false;
      } else {
         return this.addMember0(player, SystemMessage.getSystemMessage(this.enterMessage()).addPcName(player), false);
      }
   }

   private boolean addMember0(Player player, GameServerPacket p, boolean sendInfo) {
      this._members.add(player);
      player.setMatchingRoom(this);

      for(Player member : this) {
         if (member != player) {
            member.sendPacket(p, this.addMemberPacket(member, player));
         }
      }

      MatchingRoomManager.getInstance().removeFromWaitingList(player);
      if (sendInfo) {
         player.setMatchingRoomWindowOpened(true);
         player.sendPacket(this.infoRoomPacket(), this.membersPacket(player));
      }

      return true;
   }

   public void removeMember(Player member, boolean oust) {
      if (member != null) {
         if (this._members.remove(member)) {
            member.setMatchingRoom(null);
            if (this._members.isEmpty()) {
               this.disband();
            } else {
               GameServerPacket infoPacket = this.infoRoomPacket();
               SystemMessageId exitMessage0 = this.exitMessage(true, oust);
               GameServerPacket exitMessage = exitMessage0 != null ? SystemMessage.getSystemMessage(exitMessage0).addPcName(member) : null;

               for(Player player : this) {
                  player.sendPacket(infoPacket, this.removeMemberPacket(player, member), exitMessage);
               }
            }

            member.sendPacket(this.closeRoomPacket(), this.exitMessage(false, oust));
            member.setMatchingRoomWindowOpened(false);
            member.broadcastCharInfo();
         }
      }
   }

   public void broadcastPlayerUpdate(Player player) {
      for(Player member : this) {
         if (member.isMatchingRoomWindowOpened()) {
            member.sendPacket(this.updateMemberPacket(member, player));
         }
      }
   }

   public void disband() {
      for(Player player : this) {
         if (player.isMatchingRoomWindowOpened()) {
            player.sendPacket(this.closeRoomMessage());
            player.sendPacket(this.closeRoomPacket());
         }

         player.setMatchingRoom(null);
         player.broadcastCharInfo();
      }

      this._members.clear();
      MatchingRoomManager.getInstance().removeMatchingRoom(this);
   }

   public void setLeader(Player leader) {
      this._leader = leader;
      if (!this._members.contains(leader)) {
         this.addMember0(leader, null, true);
      } else {
         if (!leader.isMatchingRoomWindowOpened()) {
            leader.setMatchingRoomWindowOpened(true);
            leader.sendPacket(this.infoRoomPacket(), this.membersPacket(leader));
         }

         SystemMessageId changeLeaderMessage = this.changeLeaderMessage();

         for(Player member : this) {
            if (member.isMatchingRoomWindowOpened()) {
               member.sendPacket(this.updateMemberPacket(member, leader), changeLeaderMessage);
            }
         }
      }
   }

   public abstract SystemMessageId notValidMessage();

   public abstract SystemMessageId enterMessage();

   public abstract SystemMessageId exitMessage(boolean var1, boolean var2);

   public abstract SystemMessageId closeRoomMessage();

   public abstract SystemMessageId changeLeaderMessage();

   public abstract GameServerPacket closeRoomPacket();

   public abstract GameServerPacket infoRoomPacket();

   public abstract GameServerPacket addMemberPacket(Player var1, Player var2);

   public abstract GameServerPacket removeMemberPacket(Player var1, Player var2);

   public abstract GameServerPacket updateMemberPacket(Player var1, Player var2);

   public abstract GameServerPacket membersPacket(Player var1);

   public abstract int getType();

   public abstract int getMemberType(Player var1);

   @Override
   public void broadCast(GameServerPacket... arg) {
      for(Player player : this) {
         player.sendPacket(arg);
      }
   }

   public int getId() {
      return this._id;
   }

   public int getMinLevel() {
      return this._minLevel;
   }

   public int getMaxLevel() {
      return this._maxLevel;
   }

   public String getTopic() {
      return this._topic;
   }

   public int getMaxMembersSize() {
      return this._maxMemberSize;
   }

   public int getLocationId() {
      return MapRegionManager.getInstance().getBBs(this._leader.getLocation());
   }

   public Player getLeader() {
      return this._leader;
   }

   public Collection<Player> getPlayers() {
      return this._members;
   }

   public int getLootType() {
      return this._lootType;
   }

   @Override
   public int getMemberCount() {
      return this.getPlayers().size();
   }

   @Override
   public Player getGroupLeader() {
      return this.getLeader();
   }

   @Override
   public Iterator<Player> iterator() {
      return this._members.iterator();
   }

   public void setMinLevel(int minLevel) {
      this._minLevel = minLevel;
   }

   public void setMaxLevel(int maxLevel) {
      this._maxLevel = maxLevel;
   }

   public void setTopic(String topic) {
      this._topic = topic;
   }

   public void setMaxMemberSize(int maxMemberSize) {
      this._maxMemberSize = maxMemberSize;
   }

   public void setLootType(int lootType) {
      this._lootType = lootType;
   }
}
