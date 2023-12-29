package l2e.gameserver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.SevenSignsFestival;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import l2e.gameserver.instancemanager.DuelManager;
import l2e.gameserver.instancemanager.MatchingRoomManager;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.ServitorInstance;
import l2e.gameserver.model.actor.templates.player.ranking.PartyTemplate;
import l2e.gameserver.model.entity.DimensionalRift;
import l2e.gameserver.model.entity.underground_coliseum.UCTeam;
import l2e.gameserver.model.entity.underground_coliseum.UCWaiting;
import l2e.gameserver.model.interfaces.IL2Procedure;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.matching.MatchingRoom;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.ExAskModifyPartyLooting;
import l2e.gameserver.network.serverpackets.ExCloseMPCC;
import l2e.gameserver.network.serverpackets.ExOpenMPCC;
import l2e.gameserver.network.serverpackets.ExPVPMatchRecord;
import l2e.gameserver.network.serverpackets.ExPartyPetWindowAdd;
import l2e.gameserver.network.serverpackets.ExPartyPetWindowDelete;
import l2e.gameserver.network.serverpackets.ExReplyHandOverPartyMaster;
import l2e.gameserver.network.serverpackets.ExSetPartyLooting;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.PartyMemberPosition;
import l2e.gameserver.network.serverpackets.PartySmallWindowAdd;
import l2e.gameserver.network.serverpackets.PartySmallWindowAll;
import l2e.gameserver.network.serverpackets.PartySmallWindowDelete;
import l2e.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import l2e.gameserver.network.serverpackets.SetDismissParty;
import l2e.gameserver.network.serverpackets.SetOustPartyMember;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Party implements PlayerGroup {
   private static final Logger _log = Logger.getLogger(Party.class.getName());
   private static final double[] BONUS_EXP_SP = new double[]{1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 2.0, 2.1, 2.2};
   private static final int[] LOOT_SYSSTRINGS = new int[]{487, 488, 798, 799, 800};
   private static final int PARTY_POSITION_BROADCAST_DELAY = 12000;
   public static final byte ITEM_LOOTER = 0;
   public static final byte ITEM_RANDOM = 1;
   public static final byte ITEM_RANDOM_SPOIL = 2;
   public static final byte ITEM_ORDER = 3;
   public static final byte ITEM_ORDER_SPOIL = 4;
   private final List<Player> _members;
   private final Map<Integer, PartyTemplate> _ranking;
   private boolean _pendingInvitation = false;
   private long _pendingInviteTimeout;
   private int _partyLvl = 0;
   private int _itemDistribution = 0;
   private int _itemLastLoot = 0;
   private CommandChannel _commandChannel = null;
   private DimensionalRift _dr;
   private byte _requestChangeLoot = -1;
   private List<Integer> _changeLootAnswers = null;
   protected long _requestChangeLootTimer = 0L;
   private Future<?> _checkTask = null;
   private Future<?> _positionBroadcastTask = null;
   protected PartyMemberPosition _positionPacket;
   private boolean _disbanding = false;
   private Object _ucState = null;
   private double _rateXp;
   private double _rateSp;
   private double _rateFishing;
   private double _dropSiege;
   private double _dropElementStones;
   private double _dropSealStones;
   private double _questRewardRate;
   private double _questDropRate;
   private double _dropAdena;
   private double _dropItems;
   private double _dropRaids;
   private double _dropEpics;
   private double _dropSpoil;

   public void recalculatePartyData() {
      double rateXp = 0.0;
      double rateSp = 0.0;
      double rateFishing = 0.0;
      double dropSiege = 0.0;
      double dropElementStones = 0.0;
      double dropSealStones = 0.0;
      double questRewardRate = 0.0;
      double questDropRate = 0.0;
      double dropAdena = 0.0;
      double dropItems = 0.0;
      double dropRaids = 0.0;
      double dropEpics = 0.0;
      double dropSpoil = 0.0;
      int count = 0;

      for(Player member : this._members) {
         if (member != null) {
            ++count;
            rateXp += member.getPremiumBonus().getRateXp();
            rateSp += member.getPremiumBonus().getRateSp();
            rateFishing += member.getPremiumBonus().getFishingRate();
            dropSiege += member.getPremiumBonus().getDropSiege();
            dropElementStones += member.getPremiumBonus().getDropElementStones();
            dropSealStones += member.getPremiumBonus().getDropSealStones();
            questRewardRate += member.getPremiumBonus().getQuestRewardRate();
            questDropRate += member.getPremiumBonus().getQuestDropRate();
            dropAdena += member.getPremiumBonus().getDropAdena();
            dropItems += member.getPremiumBonus().getDropItems();
            dropRaids += member.getPremiumBonus().getDropRaids();
            dropEpics += member.getPremiumBonus().getDropEpics();
            dropSpoil += member.getPremiumBonus().getDropSpoil();
         }
      }

      this._rateXp = Math.max(1.0, rateXp / (double)count);
      this._rateSp = Math.max(1.0, rateSp / (double)count);
      this._rateFishing = Math.max(1.0, rateFishing / (double)count);
      this._dropSiege = Math.max(1.0, dropSiege / (double)count);
      this._dropElementStones = Math.max(1.0, dropElementStones / (double)count);
      this._dropSealStones = Math.max(1.0, dropSealStones / (double)count);
      this._questRewardRate = Math.max(1.0, questRewardRate / (double)count);
      this._questDropRate = Math.max(1.0, questDropRate / (double)count);
      this._dropAdena = Math.max(1.0, dropAdena / (double)count);
      this._dropItems = Math.max(1.0, dropItems / (double)count);
      this._dropRaids = Math.max(1.0, dropRaids / (double)count);
      this._dropEpics = Math.max(1.0, dropEpics / (double)count);
      this._dropSpoil = Math.max(1.0, dropSpoil / (double)count);
   }

   public Party(Player leader, int itemDistribution) {
      this._members = new CopyOnWriteArrayList<>();
      this._members.add(leader);
      this._partyLvl = leader.getLevel();
      this._rateXp = leader.getPremiumBonus().getRateXp();
      this._rateSp = leader.getPremiumBonus().getRateSp();
      this._rateFishing = leader.getPremiumBonus().getFishingRate();
      this._dropSiege = leader.getPremiumBonus().getDropSiege();
      this._dropElementStones = leader.getPremiumBonus().getDropElementStones();
      this._dropSealStones = leader.getPremiumBonus().getDropSealStones();
      this._questRewardRate = leader.getPremiumBonus().getQuestRewardRate();
      this._questDropRate = leader.getPremiumBonus().getQuestDropRate();
      this._dropAdena = leader.getPremiumBonus().getDropAdena();
      this._dropItems = leader.getPremiumBonus().getDropItems();
      this._dropRaids = leader.getPremiumBonus().getDropRaids();
      this._dropEpics = leader.getPremiumBonus().getDropEpics();
      this._dropSpoil = leader.getPremiumBonus().getDropSpoil();
      this._itemDistribution = itemDistribution;
      this._ranking = new ConcurrentHashMap<>();
      if (Config.ALLOW_PARTY_RANK_COMMAND) {
         this._ranking.put(leader.getObjectId(), new PartyTemplate());
      }
   }

   public boolean getPendingInvitation() {
      return this._pendingInvitation;
   }

   public void setPendingInvitation(boolean val) {
      this._pendingInvitation = val;
      this._pendingInviteTimeout = (long)(GameTimeController.getInstance().getGameTicks() + 150);
   }

   public boolean isInvitationRequestExpired() {
      return this._pendingInviteTimeout <= (long)GameTimeController.getInstance().getGameTicks();
   }

   private Player getCheckedRandomMember(int itemId, Creature target) {
      List<Player> availableMembers = new ArrayList<>();

      for(Player member : this.getMembers()) {
         if (!member.isDead() && member.getInventory().validateCapacityByItemId(itemId) && Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true)) {
            availableMembers.add(member);
         }
      }

      return !availableMembers.isEmpty() ? availableMembers.get(Rnd.get(availableMembers.size())) : null;
   }

   private Player getCheckedNextLooter(int ItemId, Creature target) {
      for(int i = 0; i < this.getMemberCount(); ++i) {
         if (++this._itemLastLoot >= this.getMemberCount()) {
            this._itemLastLoot = 0;
         }

         try {
            Player member = this.getMembers().get(this._itemLastLoot);
            if (member != null
               && !member.isDead()
               && member.getInventory().validateCapacityByItemId(ItemId)
               && Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true)) {
               return member;
            }
         } catch (Exception var6) {
         }
      }

      return null;
   }

   private Player getActualLooter(Player player, int ItemId, boolean spoil, Creature target) {
      Player looter = player;
      switch(this._itemDistribution) {
         case 1:
            if (!spoil) {
               looter = this.getCheckedRandomMember(ItemId, target);
            }
            break;
         case 2:
            looter = this.getCheckedRandomMember(ItemId, target);
            break;
         case 3:
            if (!spoil) {
               looter = this.getCheckedNextLooter(ItemId, target);
            }
            break;
         case 4:
            looter = this.getCheckedNextLooter(ItemId, target);
      }

      if (looter == null) {
         looter = player;
      }

      return looter;
   }

   @Deprecated
   public void broadcastToPartyMembers(GameServerPacket packet) {
      this.broadCast(packet);
   }

   public void broadcastToPartyMembersNewLeader() {
      for(Player member : this.getMembers()) {
         if (member != null) {
            member.sendPacket(PartySmallWindowDeleteAll.STATIC_PACKET);
            member.sendPacket(new PartySmallWindowAll(member, this));
            member.broadcastUserInfo(true);
         }
      }
   }

   public void broadcastToPartyMembers(Player player, GameServerPacket msg) {
      for(Player member : this.getMembers()) {
         if (member != null && member.getObjectId() != player.getObjectId()) {
            member.sendPacket(msg);
         }
      }
   }

   public void addPartyMember(Player player) {
      if (!this.getMembers().contains(player)) {
         if (this._requestChangeLoot != -1) {
            this.finishLootRequest(false);
         }

         player.sendPacket(new PartySmallWindowAll(player, this));

         for(Player pMember : this.getMembers()) {
            if (pMember != null && pMember.hasSummon()) {
               player.sendPacket(new ExPartyPetWindowAdd(pMember.getSummon()));
            }
         }

         SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_JOINED_S1_PARTY);
         msg.addString(this.getLeader().getName());
         player.sendPacket(msg);
         msg = SystemMessage.getSystemMessage(SystemMessageId.C1_JOINED_PARTY);
         msg.addString(player.getName());
         this.broadCast(msg);
         this.broadCast(new PartySmallWindowAdd(player, this));
         if (player.hasSummon()) {
            this.broadCast(new ExPartyPetWindowAdd(player.getSummon()));
         }

         this.getMembers().add(player);
         if (player.getLevel() > this._partyLvl) {
            this._partyLvl = player.getLevel();
         }

         this.recalculatePartyData();
         if (Config.ALLOW_PARTY_RANK_COMMAND) {
            this._ranking.put(player.getObjectId(), new PartyTemplate());
            if (Config.PARTY_RANK_AUTO_OPEN) {
               IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler("rank");
               if (vch != null) {
                  vch.useVoicedCommand("rank", player, null);
                  if (this.getMemberCount() == 2) {
                     vch.useVoicedCommand("rank", this.getLeader(), null);
                  }
               }
            }
         }

         for(Player member : this.getMembers()) {
            if (member != null) {
               member.updateEffectIcons(true);
               Summon summon = member.getSummon();
               member.broadcastUserInfo(true);
               if (summon != null) {
                  summon.updateEffectIcons();
               }
            }
         }

         if (this.isInDimensionalRift()) {
            this._dr.partyMemberInvited();
         }

         if (this.isInCommandChannel()) {
            player.sendPacket(ExOpenMPCC.STATIC);
         }

         MatchingRoom currentRoom = player.getMatchingRoom();
         MatchingRoom room = this.getLeader().getMatchingRoom();
         if (currentRoom != null && currentRoom != room) {
            currentRoom.removeMember(player, false);
         }

         if (room != null && room.getType() == MatchingRoom.PARTY_MATCHING) {
            room.addMemberForce(player);
         } else {
            MatchingRoomManager.getInstance().removeFromWaitingList(player);
         }

         if (this._positionBroadcastTask == null) {
            this._positionBroadcastTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Party.PositionBroadcast(), 6000L, 12000L);
         }
      }
   }

   public void removePartyMember(String name, Party.messageType type) {
      this.removePartyMember(this.getPlayerByName(name), type);
   }

   public void removePartyMember(Player player, Party.messageType type) {
      if (this.getMembers().contains(player)) {
         boolean isLeader = this.isLeader(player);
         if (!this._disbanding) {
            if (this.getMembers().size() == 2 || isLeader && !Config.ALT_LEAVE_PARTY_LEADER && type != Party.messageType.Disconnected) {
               this.disbandParty();
               return;
            }

            if (player.getUCState() != 0) {
               player.sendPacket(new ExPVPMatchRecord(2, 0));
            }
         }

         this.getMembers().remove(player);
         if (player.getUCState() != 0) {
            player.setTeam(0);
            player.cleanUCStats();
            player.setUCState(0);
            if (player.isDead()) {
               UCTeam.resPlayer(player);
            }

            if (player.getSaveLoc() != null) {
               player.teleToLocation(player.getSaveLoc(), true);
            } else {
               player.teleToLocation(TeleportWhereType.TOWN, true);
            }
         }

         if (Config.ALLOW_PARTY_RANK_COMMAND) {
            this._ranking.remove(player.getObjectId());
         }

         this.recalculatePartyLevel();
         this.recalculatePartyData();
         if (player.isFestivalParticipant()) {
            SevenSignsFestival.getInstance().updateParticipants(player, this);
         }

         if (player.isInDuel()) {
            DuelManager.getInstance().onRemoveFromParty(player);
         }

         try {
            if (player.getFusionSkill() != null) {
               player.abortCast();
            }

            for(Creature character : World.getInstance().getAroundCharacters(player)) {
               if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == player) {
                  character.abortCast();
               }
            }
         } catch (Exception var9) {
            _log.log(Level.WARNING, "", (Throwable)var9);
         }

         if (type == Party.messageType.Expelled) {
            player.sendPacket(SetOustPartyMember.STATIC_PACKET);
            SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_EXPELLED_FROM_PARTY);
            msg.addString(player.getName());
            this.broadCast(msg);
         } else if (type == Party.messageType.Left || type == Party.messageType.Disconnected) {
            player.sendPacket(SystemMessageId.YOU_LEFT_PARTY);
            SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_LEFT_PARTY);
            msg.addString(player.getName());
            this.broadCast(msg);
         }

         player.sendPacket(PartySmallWindowDeleteAll.STATIC_PACKET);
         player.setParty(null);
         this.broadCast(new PartySmallWindowDelete(player));
         if (player.hasSummon()) {
            this.broadCast(new ExPartyPetWindowDelete(player.getSummon()));
         }

         player.broadcastUserInfo(true);
         if (this.isInDimensionalRift()) {
            this._dr.partyMemberExited(player);
         }

         if (this.isInCommandChannel()) {
            player.sendPacket(new ExCloseMPCC());
         }

         MatchingRoom room = this.getLeader() != null ? this.getLeader().getMatchingRoom() : null;
         if (room != null && room.getType() == MatchingRoom.PARTY_MATCHING) {
            if (isLeader) {
               room.disband();
            } else {
               room.removeMember(player, false);
            }
         }

         if (isLeader && this.getMembers().size() > 1 && (Config.ALT_LEAVE_PARTY_LEADER || type == Party.messageType.Disconnected)) {
            SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_BECOME_A_PARTY_LEADER);
            msg.addString(this.getLeader().getName());
            this.broadCast(msg);
            this.broadcastToPartyMembersNewLeader();
            if (this.isInCommandChannel() && this._commandChannel.getLeader() == player) {
               this._commandChannel.setLeader(this.getLeader());
            }
         } else if (this.getMembers().size() == 1) {
            if (this.isInCommandChannel()) {
               if (this.getCommandChannel().getLeader().getObjectId() == this.getLeader().getObjectId()) {
                  this.getCommandChannel().disbandChannel();
               } else {
                  this.getCommandChannel().removeParty(this);
               }
            }

            Player pl = this.getLeader();
            if (pl != null) {
               pl.setParty(null);
               if (pl.isInDuel()) {
                  DuelManager.getInstance().onRemoveFromParty(pl);
               }
            }

            try {
               if (this._checkTask != null) {
                  this._checkTask.cancel(true);
                  this._checkTask = null;
               }

               if (this._positionBroadcastTask != null) {
                  this._positionBroadcastTask.cancel(false);
                  this._positionBroadcastTask = null;
               }
            } catch (Exception var8) {
            }

            this._members.clear();
            this._ranking.clear();
         }
      }
   }

   public void disbandParty() {
      this._disbanding = true;
      this.checkUCStatus();
      if (this._members != null) {
         this.broadCast(SetDismissParty.STATIC_PACKET);

         for(Player member : this._members) {
            if (member != null) {
               this.removePartyMember(member, Party.messageType.None);
            }
         }
      }
   }

   public void changePartyLeader(String name) {
      this.setLeader(this.getPlayerByName(name));
   }

   public boolean isLeader(Player player) {
      return this.getLeader() == player;
   }

   public void setLeader(Player player) {
      if (player != null && !player.isInDuel()) {
         if (this.getMembers().contains(player)) {
            if (this.isLeader(player)) {
               player.sendPacket(SystemMessageId.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF);
            } else {
               Player temp = this.getLeader();
               int p1 = this.getMembers().indexOf(player);
               this.getMembers().set(0, player);
               this.getMembers().set(p1, temp);
               temp.sendPacket(ExReplyHandOverPartyMaster.FALSE);
               player.sendPacket(ExReplyHandOverPartyMaster.TRUE);
               SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_BECOME_A_PARTY_LEADER);
               msg.addString(this.getLeader().getName());
               this.broadCast(msg);
               this.broadcastToPartyMembersNewLeader();
               if (this.isInCommandChannel() && this._commandChannel.getLeader() == temp) {
                  this._commandChannel.setLeader(this.getLeader());
               }

               MatchingRoom room = this.getLeader().getMatchingRoom();
               if (room != null && room.getType() == MatchingRoom.PARTY_MATCHING) {
                  room.setLeader(this.getLeader());
               }
            }
         } else {
            player.sendPacket(SystemMessageId.YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER);
         }
      }
   }

   private Player getPlayerByName(String name) {
      for(Player member : this.getMembers()) {
         if (member.getName().equalsIgnoreCase(name)) {
            return member;
         }
      }

      return null;
   }

   public void distributeItem(Player player, ItemInstance item) {
      if (item.getId() == 57) {
         this.distributeAdena(player, item.getCount(), player);
         ItemsParser.getInstance().destroyItem("Party", item, player, null);
      } else if (BotFunctions.getInstance().isAutoDropEnable(player)) {
         player.getParty().getLeader().addItem("Party", item, player.getParty().getLeader(), true);
      } else {
         Player target = this.getActualLooter(player, item.getId(), false, player);
         target.addItem("Party", item, player, true);
         if (item.getCount() > 1L) {
            SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_OBTAINED_S3_S2);
            msg.addString(target.getName());
            msg.addItemName(item);
            msg.addItemNumber(item.getCount());
            this.broadcastToPartyMembers(target, msg);
         } else {
            SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_OBTAINED_S2);
            msg.addString(target.getName());
            msg.addItemName(item);
            this.broadcastToPartyMembers(target, msg);
         }
      }
   }

   public void distributeItem(Player player, int itemId, long itemCount, boolean spoil, Attackable target) {
      if (itemId == 57) {
         this.distributeAdena(player, itemCount, target);
      } else if (BotFunctions.getInstance().isAutoDropEnable(player) && !spoil) {
         player.getParty().getLeader().addItem("Party", itemId, itemCount, player.getParty().getLeader(), true);
      } else if (BotFunctions.getInstance().isAutoSpoilEnable(player) && spoil) {
         player.getParty().getLeader().addItem("Party", itemId, itemCount, player.getParty().getLeader(), true);
      } else {
         Player looter = this.getActualLooter(player, itemId, spoil, target);
         looter.addItem(spoil ? "Sweeper Party" : "Party", itemId, itemCount, target, true);
         if (itemCount > 1L) {
            SystemMessage msg = spoil
               ? SystemMessage.getSystemMessage(SystemMessageId.C1_SWEEPED_UP_S3_S2)
               : SystemMessage.getSystemMessage(SystemMessageId.C1_OBTAINED_S3_S2);
            msg.addString(looter.getName());
            msg.addItemName(itemId);
            msg.addItemNumber(itemCount);
            this.broadcastToPartyMembers(looter, msg);
         } else {
            SystemMessage msg = spoil
               ? SystemMessage.getSystemMessage(SystemMessageId.C1_SWEEPED_UP_S2)
               : SystemMessage.getSystemMessage(SystemMessageId.C1_OBTAINED_S2);
            msg.addString(looter.getName());
            msg.addItemName(itemId);
            this.broadcastToPartyMembers(looter, msg);
         }
      }
   }

   public void distributeAdena(Player player, long adena, Creature target) {
      if (BotFunctions.getInstance().isAutoDropEnable(player)) {
         player.getParty().getLeader().addAdena("Party", adena, player.getParty().getLeader(), true);
      } else {
         Map<Player, AtomicLong> toReward = new HashMap<>(9);

         for(Player member : this.getMembers()) {
            if (Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true)) {
               toReward.put(member, new AtomicLong());
            }
         }

         if (!toReward.isEmpty()) {
            long leftOver = adena % (long)toReward.size();
            long count = adena / (long)toReward.size();
            if (count > 0L) {
               for(AtomicLong member : toReward.values()) {
                  member.addAndGet(count);
               }
            }

            if (leftOver > 0L) {
               List<Player> keys = new ArrayList<>(toReward.keySet());

               while(leftOver-- > 0L) {
                  Collections.shuffle(keys);
                  toReward.get(keys.get(0)).incrementAndGet();
               }
            }

            for(Entry<Player, AtomicLong> member : toReward.entrySet()) {
               if (member.getValue().get() > 0L) {
                  if (member.getKey().getInventory().getAdenaInstance() != null) {
                     member.getKey().addAdena("Party", member.getValue().get(), player, true);
                  } else {
                     member.getKey().addItem("Party", 57, member.getValue().get(), player, true);
                  }
               }
            }
         }
      }
   }

   public void distributeXpAndSp(
      long xpReward_pr, int spReward_pr, long xpReward, int spReward, List<Player> rewardedMembers, int topLvl, int partyDmg, Attackable target
   ) {
      List<Player> validMembers = this.getValidMembers(rewardedMembers, topLvl);
      xpReward = (long)((double)xpReward * this.getExpBonus(validMembers.size()));
      spReward = (int)((double)spReward * this.getSpBonus(validMembers.size()));
      xpReward_pr = (long)((double)xpReward_pr * this.getExpBonus(validMembers.size()));
      spReward_pr = (int)((double)spReward_pr * this.getSpBonus(validMembers.size()));
      int sqLevelSum = 0;

      for(Player member : validMembers) {
         sqLevelSum += member.getLevel() * member.getLevel();
      }

      double vitalityPoints = target.getVitalityPoints(partyDmg) * Config.RATE_PARTY_XP / (double)validMembers.size();
      boolean useVitalityRate = target.useVitalityRate();

      for(Player member : rewardedMembers) {
         if (!member.isDead()) {
            if (validMembers.contains(member)) {
               float penalty = member.hasServitor() ? ((ServitorInstance)member.getSummon()).getExpPenalty() : 0.0F;
               double sqLevel = (double)(member.getLevel() * member.getLevel());
               double preCalculation = sqLevel / (double)sqLevelSum * (double)(1.0F - penalty);
               long addexp;
               int addsp;
               if (member.hasPremiumBonus()) {
                  addexp = Math.round(member.calcStat(Stats.EXPSP_RATE, (double)xpReward_pr * preCalculation, null, null));
                  addsp = (int)member.calcStat(Stats.EXPSP_RATE, (double)spReward_pr * preCalculation, null, null);
               } else {
                  addexp = Math.round(member.calcStat(Stats.EXPSP_RATE, (double)xpReward * preCalculation, null, null));
                  addsp = (int)member.calcStat(Stats.EXPSP_RATE, (double)spReward * preCalculation, null, null);
               }

               addexp = this.calculateExpSpPartyCutoff(member.getActingPlayer(), topLvl, addexp, addsp, useVitalityRate);
               if (addexp > 0L) {
                  member.updateVitalityPoints(vitalityPoints, true, false);
               }
            } else {
               member.addExpAndSp(0L, 0);
            }
         }
      }
   }

   private final long calculateExpSpPartyCutoff(Player player, int topLvl, long addExp, int addSp, boolean vit) {
      double xp = (double)addExp;
      double sp = (double)addSp;
      if (player.getPremiumBonus().isPersonal()) {
         xp *= player.getPremiumBonus().getRateXp();
         sp *= player.getPremiumBonus().getRateSp();
      }

      xp *= player.getRExp();
      sp *= player.getRSp();
      if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("highfive")) {
         int i = 0;
         int lvlDiff = topLvl - player.getLevel();

         for(int[] gap : Config.PARTY_XP_CUTOFF_GAPS) {
            if (lvlDiff >= gap[0] && lvlDiff <= gap[1]) {
               xp = xp * (double)Config.PARTY_XP_CUTOFF_GAP_PERCENTS[i] / 100.0;
               sp = sp * (double)Config.PARTY_XP_CUTOFF_GAP_PERCENTS[i] / 100.0;
               xp = xp > 9.223372E18F ? 9.223372E18F : xp;
               sp = sp > 2.147483647E9 ? 2.147483647E9 : sp;
               if (xp < 0.0) {
                  xp = 0.0;
               }

               if (sp < 0.0) {
                  sp = 0.0;
               }

               player.addExpAndSp((long)xp, (int)sp, vit);
               break;
            }

            ++i;
         }
      } else {
         xp = xp > 9.223372E18F ? 9.223372E18F : xp;
         sp = sp > 2.147483647E9 ? 2.147483647E9 : sp;
         if (xp < 0.0) {
            xp = 0.0;
         }

         if (sp < 0.0) {
            sp = 0.0;
         }

         player.addExpAndSp((long)xp, (int)sp, vit);
      }

      return (long)xp;
   }

   public void recalculatePartyLevel() {
      int newLevel = 0;

      for(Player member : this.getMembers()) {
         if (member == null) {
            this.getMembers().remove(member);
         } else if (member.getLevel() > newLevel) {
            newLevel = member.getLevel();
         }
      }

      this._partyLvl = newLevel;
   }

   private List<Player> getValidMembers(List<Player> members, int topLvl) {
      List<Player> validMembers = new ArrayList<>();
      if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level")) {
         for(Player member : members) {
            if (topLvl - member.getLevel() <= Config.PARTY_XP_CUTOFF_LEVEL) {
               validMembers.add(member);
            }
         }
      } else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("percentage")) {
         int sqLevelSum = 0;

         for(Player member : members) {
            sqLevelSum += member.getLevel() * member.getLevel();
         }

         for(Player member : members) {
            int sqLevel = member.getLevel() * member.getLevel();
            if ((double)(sqLevel * 100) >= (double)sqLevelSum * Config.PARTY_XP_CUTOFF_PERCENT) {
               validMembers.add(member);
            }
         }
      } else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("auto")) {
         int sqLevelSum = 0;

         for(Player member : members) {
            sqLevelSum += member.getLevel() * member.getLevel();
         }

         int i = members.size() - 1;
         if (i < 1) {
            return members;
         }

         if (i >= BONUS_EXP_SP.length) {
            i = BONUS_EXP_SP.length - 1;
         }

         for(Player member : members) {
            int sqLevel = member.getLevel() * member.getLevel();
            if (sqLevel >= sqLevelSum / (members.size() * members.size())) {
               validMembers.add(member);
            }
         }
      } else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("highfive")) {
         validMembers.addAll(members);
      } else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("none")) {
         validMembers.addAll(members);
      }

      return validMembers;
   }

   private double getBaseExpSpBonus(int membersCount) {
      int i = membersCount - 1;
      if (i < 1) {
         return 1.0;
      } else {
         if (i >= BONUS_EXP_SP.length) {
            i = BONUS_EXP_SP.length - 1;
         }

         return BONUS_EXP_SP[i];
      }
   }

   private double getExpBonus(int membersCount) {
      return membersCount < 2 ? this.getBaseExpSpBonus(membersCount) : this.getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_XP;
   }

   private double getSpBonus(int membersCount) {
      return membersCount < 2 ? this.getBaseExpSpBonus(membersCount) : this.getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_SP;
   }

   public int getLevel() {
      return this._partyLvl;
   }

   public int getLootDistribution() {
      return this._itemDistribution;
   }

   public boolean isInCommandChannel() {
      return this._commandChannel != null;
   }

   public CommandChannel getCommandChannel() {
      return this._commandChannel;
   }

   public void setCommandChannel(CommandChannel channel) {
      this._commandChannel = channel;
   }

   public boolean isInDimensionalRift() {
      return this._dr != null;
   }

   public void setDimensionalRift(DimensionalRift dr) {
      this._dr = dr;
   }

   public DimensionalRift getDimensionalRift() {
      return this._dr;
   }

   public Player getLeader() {
      return this._members != null && !this._members.isEmpty() ? this._members.get(0) : null;
   }

   public void requestLootChange(byte type) {
      if (this._requestChangeLoot != -1) {
         if (System.currentTimeMillis() <= this._requestChangeLootTimer) {
            return;
         }

         this.finishLootRequest(false);
      }

      this._requestChangeLoot = type;
      int additionalTime = 45000;
      this._requestChangeLootTimer = System.currentTimeMillis() + 45000L;
      this._changeLootAnswers = new ArrayList<>();
      this._checkTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Party.ChangeLootCheck(), 46000L, 5000L);
      this.broadcastToPartyMembers(this.getLeader(), new ExAskModifyPartyLooting(this.getLeader().getName(), type));
      SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REQUESTING_APPROVAL_CHANGE_PARTY_LOOT_S1);
      sm.addSystemString(LOOT_SYSSTRINGS[type]);
      this.getLeader().sendPacket(sm);
   }

   public synchronized void answerLootChangeRequest(Player member, boolean answer) {
      if (this._requestChangeLoot != -1) {
         if (!this._changeLootAnswers.contains(member.getObjectId())) {
            if (!answer) {
               this.finishLootRequest(false);
            } else {
               this._changeLootAnswers.add(member.getObjectId());
               if (this._changeLootAnswers.size() >= this.getMemberCount() - 1) {
                  this.finishLootRequest(true);
               }
            }
         }
      }
   }

   protected synchronized void finishLootRequest(boolean success) {
      if (this._requestChangeLoot != -1) {
         if (this._checkTask != null) {
            this._checkTask.cancel(false);
            this._checkTask = null;
         }

         if (success) {
            this.broadCast(new ExSetPartyLooting(1, this._requestChangeLoot));
            this._itemDistribution = this._requestChangeLoot;
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PARTY_LOOT_CHANGED_S1);
            sm.addSystemString(LOOT_SYSSTRINGS[this._requestChangeLoot]);
            this.broadCast(sm);
         } else {
            this.broadCast(new ExSetPartyLooting(0, (byte)0));
            this.broadCast(SystemMessage.getSystemMessage(SystemMessageId.PARTY_LOOT_CHANGE_CANCELLED));
         }

         this._requestChangeLoot = -1;
         this._changeLootAnswers.clear();
         this._requestChangeLootTimer = 0L;
      }
   }

   public List<Player> getMembers() {
      return this._members;
   }

   public Object getUCState() {
      return this._ucState;
   }

   public void setUCState(Object uc) {
      this._ucState = uc;
   }

   @Override
   public Player getGroupLeader() {
      return this.getLeader();
   }

   @Override
   public Iterator<Player> iterator() {
      return this._members.iterator();
   }

   @Override
   public void broadCast(GameServerPacket... msg) {
      if (this._members != null && !this._members.isEmpty()) {
         for(Player member : this._members) {
            if (member != null) {
               member.sendPacket(msg);
            }
         }
      }
   }

   @Override
   public int getMemberCount() {
      return this._members.size();
   }

   public boolean forEachMember(IL2Procedure<Player> procedure) {
      for(Player player : this.getMembers()) {
         if (!procedure.execute(player)) {
            return false;
         }
      }

      return true;
   }

   public boolean containsPlayer(Player player) {
      return this.getMembers().contains(player);
   }

   public int getLeaderObjectId() {
      return this.getLeader() != null ? this.getLeader().getObjectId() : 0;
   }

   public void broadcastString(String text) {
      this.broadCast(SystemMessage.sendString(text));
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

   public Map<Integer, PartyTemplate> getRanking() {
      return this._ranking;
   }

   public PartyTemplate getMemberRank(Player player) {
      return this._ranking.get(player.getObjectId());
   }

   public double getRateXp() {
      return this._rateXp;
   }

   public double getRateSp() {
      return this._rateSp;
   }

   public double getQuestRewardRate() {
      return this._questRewardRate;
   }

   public double getQuestDropRate() {
      return this._questDropRate;
   }

   public double getDropAdena() {
      return this._dropAdena;
   }

   public double getDropItems() {
      return this._dropItems;
   }

   public double getDropSpoil() {
      return this._dropSpoil;
   }

   public double getDropSiege() {
      return this._dropSiege;
   }

   public double getDropElementStones() {
      return this._dropElementStones;
   }

   public double getFishingRate() {
      return this._rateFishing;
   }

   public double getDropRaids() {
      return this._dropRaids;
   }

   public double getDropEpics() {
      return this._dropEpics;
   }

   public double getDropSealStones() {
      return this._dropSealStones;
   }

   private void checkUCStatus() {
      if (this.getUCState() != null) {
         if (this.getUCState() instanceof UCWaiting) {
            UCWaiting waiting = (UCWaiting)this.getUCState();
            waiting.setParty(false);
            waiting.clean();
         } else if (this.getUCState() instanceof UCTeam) {
            UCTeam team = (UCTeam)this.getUCState();
            UCTeam otherTeam = team.getOtherTeam();
            team.setStatus((byte)2);
            otherTeam.setStatus((byte)1);
            team.getBaseArena().runTaskNow();
         }
      }
   }

   protected class ChangeLootCheck implements Runnable {
      @Override
      public void run() {
         if (System.currentTimeMillis() > Party.this._requestChangeLootTimer) {
            Party.this.finishLootRequest(false);
         }
      }
   }

   protected class PositionBroadcast implements Runnable {
      @Override
      public void run() {
         if (Party.this._positionPacket == null) {
            Party.this._positionPacket = new PartyMemberPosition(Party.this);
         } else {
            Party.this._positionPacket.reuse(Party.this);
         }

         Party.this.broadCast(Party.this._positionPacket);
      }
   }

   public static enum messageType {
      Expelled,
      Left,
      None,
      Disconnected;
   }
}
