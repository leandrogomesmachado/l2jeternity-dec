package l2e.gameserver.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import l2e.commons.collections.JoinedIterator;
import l2e.gameserver.Config;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.interfaces.IL2Procedure;
import l2e.gameserver.model.matching.MatchingRoom;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.ExCloseMPCC;
import l2e.gameserver.network.serverpackets.ExMPCCPartyInfoUpdate;
import l2e.gameserver.network.serverpackets.ExOpenMPCC;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class CommandChannel implements PlayerGroup {
   public static final int STRATEGY_GUIDE_ID = 8871;
   public static final int CLAN_IMPERIUM_ID = 391;
   private final List<Party> _commandChannelParties = new CopyOnWriteArrayList<>();
   private Player _commandChannelLeader;
   private int _commandChannelLvl;
   private int _reflectionId;
   private MatchingRoom _matchingRoom;

   public CommandChannel(Player leader) {
      this._commandChannelLeader = leader;
      this._commandChannelParties.add(leader.getParty());
      this._commandChannelLvl = leader.getParty().getLevel();
      leader.getParty().setCommandChannel(this);
      this.broadCast(ExOpenMPCC.STATIC);
   }

   public void addParty(Party party) {
      if (party != null) {
         this.broadCast(new ExMPCCPartyInfoUpdate(party, 1));
         this._commandChannelParties.add(party);
         this.refreshLevel();
         party.setCommandChannel(this);

         for(Player member : party.getMembers()) {
            member.sendPacket(ExOpenMPCC.STATIC);
            if (this._matchingRoom != null && member == party.getLeader()) {
               this._matchingRoom.addMember(party.getLeader());
               party.getLeader().setMatchingRoomWindowOpened(true);
               party.getLeader().sendPacket(this._matchingRoom.infoRoomPacket(), this._matchingRoom.membersPacket(party.getLeader()));
               this._matchingRoom.broadcastPlayerUpdate(party.getLeader());
            }
         }

         if (Config.PARTY_RANK_AUTO_OPEN) {
            for(Player member : this.getMembers()) {
               if (member != null) {
                  IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler("rank");
                  if (vch != null) {
                     vch.useVoicedCommand("rank", member, null);
                  }
               }
            }
         }
      }
   }

   public void removeParty(Party party) {
      this._commandChannelParties.remove(party);
      this.refreshLevel();
      party.setCommandChannel(null);
      party.broadCast(ExCloseMPCC.STATIC);
      int instanceId = this.getReflectionId();
      if (instanceId != 0) {
         Reflection ref = ReflectionManager.getInstance().getReflection(instanceId);
         if (ref != null) {
            for(Player player : party.getMembers()) {
               if (player != null && player.getReflectionId() != 0) {
                  player.setReflectionId(0);
                  player.teleToLocation(ref.getReturnLoc(), true);
               }
            }
         }
      }

      if (this._commandChannelParties.size() < 2) {
         this.disbandChannel();
      } else {
         this.broadCast(new ExMPCCPartyInfoUpdate(party, 0));
         if (this._matchingRoom != null) {
            this._matchingRoom.removeMember(party.getLeader(), false);
            this._matchingRoom.broadcastPlayerUpdate(party.getLeader());
         }
      }
   }

   public void disbandChannel() {
      this.broadCast(SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_DISBANDED));

      for(Party party : this._commandChannelParties) {
         party.setCommandChannel(null);
         party.broadCast(ExCloseMPCC.STATIC);
      }

      if (this.isInReflection()) {
         Reflection inst = ReflectionManager.getInstance().getReflection(this.getReflectionId());
         if (inst != null) {
            inst.setDuration(60000);
            inst.setEmptyDestroyTime(0L);
         }

         this.setReflectionId(0);
      }

      if (this._matchingRoom != null) {
         this._matchingRoom.disband();
      }

      this._commandChannelParties.clear();
      this._commandChannelLeader = null;
   }

   @Override
   public int getMemberCount() {
      int count = 0;

      for(Party party : this._commandChannelParties) {
         count += party.getMemberCount();
      }

      return count;
   }

   @Override
   public void broadCast(GameServerPacket... gsp) {
      for(Party party : this._commandChannelParties) {
         party.broadCast(gsp);
      }
   }

   public void broadcastToChannelPartyLeaders(GameServerPacket gsp) {
      for(Party party : this._commandChannelParties) {
         Player leader = party.getLeader();
         if (leader != null) {
            leader.sendPacket(gsp);
         }
      }
   }

   public List<Party> getPartys() {
      return this._commandChannelParties;
   }

   @Override
   public Player getGroupLeader() {
      return this.getLeader();
   }

   @Override
   public Iterator<Player> iterator() {
      List<Iterator<Player>> iterators = new ArrayList<>(this._commandChannelParties.size());

      for(Party p : this.getPartys()) {
         iterators.add(p.getMembers().iterator());
      }

      return new JoinedIterator<>(iterators);
   }

   public int getLevel() {
      return this._commandChannelLvl;
   }

   public void setLeader(Player newLeader) {
      this._commandChannelLeader = newLeader;
      this.broadCast(SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_LEADER_NOW_C1).addPcName(newLeader));
   }

   public Player getLeader() {
      return this._commandChannelLeader;
   }

   public int getLeaderObjectId() {
      return this._commandChannelLeader.getObjectId();
   }

   private void refreshLevel() {
      this._commandChannelLvl = 0;

      for(Party pty : this._commandChannelParties) {
         if (pty.getLevel() > this._commandChannelLvl) {
            this._commandChannelLvl = pty.getLevel();
         }
      }
   }

   public boolean isInReflection() {
      return this._reflectionId != 0;
   }

   public void setReflectionId(int reflectionId) {
      this._reflectionId = reflectionId;
   }

   public int getReflectionId() {
      return this._reflectionId;
   }

   public MatchingRoom getMatchingRoom() {
      return this._matchingRoom;
   }

   public void setMatchingRoom(MatchingRoom matchingRoom) {
      this._matchingRoom = matchingRoom;
   }

   public static Player checkAndAskToCreateChannel(Player player, Player target) {
      if (player.isOutOfControl()) {
         player.sendActionFailed();
         return null;
      } else if (player.isProcessingRequest()) {
         player.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
         return null;
      } else if (!player.isInParty() || player.getParty().getLeader() != player) {
         player.sendPacket(SystemMessageId.CANNOT_INVITE_TO_COMMAND_CHANNEL);
         return null;
      } else if (target != null && player != target && target.isInParty() && player.getParty() != target.getParty()) {
         if (target.isInParty() && !target.getParty().isLeader(target)) {
            target = target.getParty().getLeader();
         }

         if (target == null) {
            player.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
            return null;
         } else if (target.getParty().isInCommandChannel()) {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_ALREADY_MEMBER_OF_COMMAND_CHANNEL).addPcName(target));
            return null;
         } else if (target.isProcessingRequest()) {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addPcName(target));
            return null;
         } else {
            return !checkCreationByClanCondition(player) ? null : target;
         }
      } else {
         player.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
         return null;
      }
   }

   public static boolean checkCreationByClanCondition(Player creator) {
      if (creator != null && creator.isInParty() && creator.getParty().isLeader(creator) && creator.getPledgeClass() >= Config.RANK_CLASS_FOR_CC) {
         boolean haveSkill = creator.getSkillLevel(391) > 0;
         boolean haveItem = creator.getInventory().getItemByItemId(8871) != null;
         if (!haveSkill && !haveItem) {
            creator.sendPacket(SystemMessageId.COMMAND_CHANNEL_ONLY_BY_LEVEL_5_CLAN_LEADER_PARTY_LEADER);
            return false;
         } else {
            return true;
         }
      } else {
         creator.sendPacket(SystemMessageId.COMMAND_CHANNEL_ONLY_BY_LEVEL_5_CLAN_LEADER_PARTY_LEADER);
         return false;
      }
   }

   public boolean meetRaidWarCondition(GameObject obj) {
      if (obj instanceof Creature && ((Creature)obj).isRaid()) {
         return this.getMemberCount() >= Config.LOOT_RAIDS_PRIVILEGE_CC_SIZE;
      } else {
         return false;
      }
   }

   public List<Player> getMembers() {
      List<Player> members = new LinkedList<>();

      for(Party party : this.getPartys()) {
         members.addAll(party.getMembers());
      }

      return members;
   }

   public boolean forEachMember(IL2Procedure<Player> procedure) {
      if (this._commandChannelParties != null && !this._commandChannelParties.isEmpty()) {
         for(Party party : this._commandChannelParties) {
            if (!party.forEachMember(procedure)) {
               return false;
            }
         }
      }

      return true;
   }

   public void broadcastCreatureSay(final CreatureSay msg, final Player broadcaster) {
      this.forEachMember(new IL2Procedure<Player>() {
         public boolean execute(Player member) {
            if (member != null && !BlockedList.isBlocked(member, broadcaster)) {
               member.sendPacket(msg);
            }

            return true;
         }
      });
   }

   public boolean isLeader(Player player) {
      return this.getLeaderObjectId() == player.getObjectId();
   }
}
