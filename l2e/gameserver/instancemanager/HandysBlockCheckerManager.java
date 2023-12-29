package l2e.gameserver.instancemanager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.tasks.PenaltyRemoveTask;
import l2e.gameserver.model.ArenaParticipantsHolder;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.BlockCheckerEngine;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.model.olympiad.OlympiadManager;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExBlockUpSetList;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class HandysBlockCheckerManager {
   private static final ArenaParticipantsHolder[] _arenaPlayers = new ArenaParticipantsHolder[4];
   private static final Map<Integer, Integer> _arenaVotes = new HashMap<>();
   private static final Map<Integer, Boolean> _arenaStatus = new HashMap<>();
   protected static Set<Integer> _registrationPenalty = Collections.synchronizedSet(new HashSet<>());

   public synchronized int getArenaVotes(int arenaId) {
      return _arenaVotes.get(arenaId);
   }

   public synchronized void increaseArenaVotes(int arena) {
      int newVotes = _arenaVotes.get(arena) + 1;
      ArenaParticipantsHolder holder = _arenaPlayers[arena];
      if (newVotes > holder.getAllPlayers().size() / 2 && !holder.getEvent().isStarted()) {
         this.clearArenaVotes(arena);
         if (holder.getBlueTeamSize() == 0 || holder.getRedTeamSize() == 0) {
            return;
         }

         if (Config.HBCE_FAIR_PLAY) {
            holder.checkAndShuffle();
         }

         ThreadPoolManager.getInstance().execute(holder.getEvent().new StartEvent());
      } else {
         _arenaVotes.put(arena, newVotes);
      }
   }

   public synchronized void clearArenaVotes(int arena) {
      _arenaVotes.put(arena, 0);
   }

   protected HandysBlockCheckerManager() {
      _arenaStatus.put(0, false);
      _arenaStatus.put(1, false);
      _arenaStatus.put(2, false);
      _arenaStatus.put(3, false);
      _arenaVotes.put(0, 0);
      _arenaVotes.put(1, 0);
      _arenaVotes.put(2, 0);
      _arenaVotes.put(3, 0);
   }

   public ArenaParticipantsHolder getHolder(int arena) {
      return _arenaPlayers[arena];
   }

   public void startUpParticipantsQueue() {
      for(int i = 0; i < 4; ++i) {
         _arenaPlayers[i] = new ArenaParticipantsHolder(i);
      }
   }

   public boolean addPlayerToArena(Player player, int arenaId) {
      ArenaParticipantsHolder holder = _arenaPlayers[arenaId];
      synchronized(holder) {
         for(int i = 0; i < 4; ++i) {
            if (_arenaPlayers[i].getAllPlayers().contains(player)) {
               SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_MATCH_WAITING_LIST);
               msg.addCharName(player);
               player.sendPacket(msg);
               return false;
            }
         }

         if (player.isCursedWeaponEquipped()) {
            player.sendPacket(SystemMessageId.CANNOT_REGISTER_PROCESSING_CURSED_WEAPON);
            return false;
         } else if (!player.isOnEvent() && !player.isInOlympiadMode()) {
            if (OlympiadManager.getInstance().isRegistered(player)) {
               OlympiadManager.getInstance().unRegisterNoble(player);
               player.sendPacket(SystemMessageId.COLISEUM_OLYMPIAD_KRATEIS_APPLICANTS_CANNOT_PARTICIPATE);
            }

            if (_registrationPenalty.contains(player.getObjectId())) {
               player.sendPacket(SystemMessageId.CANNOT_REQUEST_REGISTRATION_10_SECS_AFTER);
               return false;
            } else {
               boolean isRed;
               if (holder.getBlueTeamSize() < holder.getRedTeamSize()) {
                  holder.addPlayer(player, 1);
                  isRed = false;
               } else {
                  holder.addPlayer(player, 0);
                  isRed = true;
               }

               holder.broadCastPacketToTeam(new ExBlockUpSetList(player, isRed, false));
               return true;
            }
         } else {
            player.sendMessage("Couldnt register you due other event participation");
            return false;
         }
      }
   }

   public void removePlayer(Player player, int arenaId, int team) {
      ArenaParticipantsHolder holder = _arenaPlayers[arenaId];
      synchronized(holder) {
         boolean isRed = team == 0;
         holder.removePlayer(player, team);
         holder.broadCastPacketToTeam(new ExBlockUpSetList(player, isRed, true));
         int teamSize = isRed ? holder.getRedTeamSize() : holder.getBlueTeamSize();
         if (teamSize == 0) {
            holder.getEvent().endEventAbnormally();
         }

         _registrationPenalty.add(player.getObjectId());
         this.schedulePenaltyRemoval(player.getObjectId());
      }
   }

   public void changePlayerToTeam(Player player, int arena, int team) {
      ArenaParticipantsHolder holder = _arenaPlayers[arena];
      synchronized(holder) {
         boolean isFromRed = holder.getRedPlayers().contains(player);
         if (isFromRed && holder.getBlueTeamSize() == 6) {
            player.sendMessage("The team is full");
         } else if (!isFromRed && holder.getRedTeamSize() == 6) {
            player.sendMessage("The team is full");
         } else {
            int futureTeam = isFromRed ? 1 : 0;
            holder.addPlayer(player, futureTeam);
            if (isFromRed) {
               holder.removePlayer(player, 0);
            } else {
               holder.removePlayer(player, 1);
            }

            holder.broadCastPacketToTeam(new ExBlockUpSetList(player, isFromRed));
         }
      }
   }

   public synchronized void clearPaticipantQueueByArenaId(int arenaId) {
      _arenaPlayers[arenaId].clearPlayers();
   }

   public boolean arenaIsBeingUsed(int arenaId) {
      return arenaId >= 0 && arenaId <= 3 ? _arenaStatus.get(arenaId) : false;
   }

   public void setArenaBeingUsed(int arenaId) {
      _arenaStatus.put(arenaId, true);
   }

   public void setArenaFree(int arenaId) {
      _arenaStatus.put(arenaId, false);
   }

   public void onDisconnect(Player player) {
      int arena = player.getBlockCheckerArena();
      int team = this.getHolder(arena).getPlayerTeam(player);
      getInstance().removePlayer(player, arena, team);
      if (player.getTeam() > 0) {
         player.stopAllEffects();
         player.setTeam(0);
         PcInventory inv = player.getInventory();
         if (inv.getItemByItemId(13787) != null) {
            long count = inv.getInventoryItemCount(13787, 0);
            inv.destroyItemByItemId("Handys Block Checker", 13787, count, player, player);
         }

         if (inv.getItemByItemId(13788) != null) {
            long count = inv.getInventoryItemCount(13788, 0);
            inv.destroyItemByItemId("Handys Block Checker", 13788, count, player, player);
         }

         player.teleToLocation(-57478, -60367, -2370, true);
      }
   }

   public static HandysBlockCheckerManager getInstance() {
      return HandysBlockCheckerManager.SingletonHolder._instance;
   }

   public void removePenalty(int objectId) {
      _registrationPenalty.remove(objectId);
   }

   private void schedulePenaltyRemoval(int objId) {
      ThreadPoolManager.getInstance().schedule(new PenaltyRemoveTask(objId), 10000L);
   }

   private static class SingletonHolder {
      protected static HandysBlockCheckerManager _instance = new HandysBlockCheckerManager();
   }
}
