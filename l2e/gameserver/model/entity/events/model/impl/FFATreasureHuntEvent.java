package l2e.gameserver.model.entity.events.model.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import l2e.commons.collections.MultiValueSet;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.model.template.FightEventPlayer;
import l2e.gameserver.model.strings.server.ServerMessage;

public class FFATreasureHuntEvent extends AbstractFightEvent {
   private static final int CHEST_ID = 53000;
   private final int[][] _rewardByOpenChest;
   private final int _scoreForKilledPlayer;
   private final int _scoreForChest;
   private final long _timeForRespawningChest;
   private final int _numberOfChests;
   private final Collection<Npc> _spawnedChests;

   public FFATreasureHuntEvent(MultiValueSet<String> set) {
      super(set);
      this._rewardByOpenChest = this.parseItemsList(set.getString("rewardByOpenChest", null));
      this._scoreForKilledPlayer = set.getInteger("scoreForKilledPlayer");
      this._scoreForChest = set.getInteger("scoreForChest");
      this._timeForRespawningChest = set.getLong("timeForRespawningChest");
      this._numberOfChests = set.getInteger("numberOfChests");
      this._spawnedChests = new CopyOnWriteArrayList<>();
   }

   @Override
   public void onKilled(Creature actor, Creature victim) {
      if (actor != null && actor.isPlayer()) {
         FightEventPlayer realActor = this.getFightEventPlayer(actor.getActingPlayer());
         if (realActor != null) {
            if (victim.isPlayer()) {
               realActor.increaseKills();
               realActor.increaseScore(this._scoreForKilledPlayer);
               this.updatePlayerScore(realActor);
               ServerMessage msg = new ServerMessage("FightEvents.YOU_HAVE_KILL", realActor.getPlayer().getLang());
               msg.add(victim.getName());
               this.sendMessageToPlayer(realActor.getPlayer(), AbstractFightEvent.MESSAGE_TYPES.GM, msg);
            }

            actor.getActingPlayer().sendUserInfo();
         }
      }

      if (victim.isPlayer()) {
         FightEventPlayer realVictim = this.getFightEventPlayer(victim);
         if (realVictim != null) {
            realVictim.increaseDeaths();
            if (actor != null) {
               ServerMessage msg = new ServerMessage("FightEvents.YOU_KILLED", realVictim.getPlayer().getLang());
               msg.add(actor.getName());
               this.sendMessageToPlayer(realVictim.getPlayer(), AbstractFightEvent.MESSAGE_TYPES.GM, msg);
            }

            victim.getActingPlayer().broadcastCharInfo();
         }
      }

      super.onKilled(actor, victim);
   }

   private void spawnChest() {
      this._spawnedChests.add(this.chooseLocAndSpawnNpc(53000, this.getMap().getKeyLocations(), 0, true));
   }

   @Override
   public void startRound() {
      super.startRound();

      for(int i = 0; i < this._numberOfChests; ++i) {
         this.spawnChest();
      }
   }

   @Override
   public void stopEvent() {
      super.stopEvent();

      for(Npc chest : this._spawnedChests) {
         if (chest != null && !chest.isDead()) {
            chest.deleteMe();
         }
      }

      this._spawnedChests.clear();
   }

   public boolean openTreasure(Player player, Npc npc) {
      FightEventPlayer fPlayer = this.getFightEventPlayer(player);
      if (fPlayer == null) {
         return false;
      } else if (this.getState() != AbstractFightEvent.EVENT_STATE.STARTED) {
         return false;
      } else {
         fPlayer.increaseEventSpecificScore("chest");
         fPlayer.increaseScore(this._scoreForChest);
         this.updatePlayerScore(fPlayer);
         player.broadcastCharInfo();
         ThreadPoolManager.getInstance().schedule(new FFATreasureHuntEvent.SpawnChest(this), this._timeForRespawningChest * 1000L);
         this._spawnedChests.remove(npc);
         return true;
      }
   }

   @Override
   protected void giveItemRewardsForPlayer(FightEventPlayer fPlayer, Map<Integer, Long> rewards, boolean isTopKiller) {
      if (fPlayer != null) {
         if (rewards == null) {
            rewards = new HashMap<>();
         }

         if (this._rewardByOpenChest != null && this._rewardByOpenChest.length > 0) {
            for(int[] item : this._rewardByOpenChest) {
               if (item != null && item.length == 2) {
                  if (rewards.containsKey(item[0])) {
                     long amount = rewards.get(item[0]) + (long)(item[1] * fPlayer.getEventSpecificScore("chest"));
                     rewards.put(item[0], amount);
                  } else {
                     rewards.put(item[0], (long)(item[1] * fPlayer.getEventSpecificScore("chest")));
                  }
               }
            }
         }

         super.giveItemRewardsForPlayer(fPlayer, rewards, isTopKiller);
      }
   }

   @Override
   public String getVisibleTitle(Player player, Player viewer, String currentTitle, boolean toMe) {
      FightEventPlayer fPlayer = this.getFightEventPlayer(player);
      if (fPlayer == null) {
         return currentTitle;
      } else {
         ServerMessage msg = new ServerMessage("FightEvents.TITLE_INFO1", viewer.getLang());
         msg.add(fPlayer.getEventSpecificScore("chest"));
         msg.add(fPlayer.getKills());
         return msg.toString();
      }
   }

   private static class SpawnChest implements Runnable {
      private final FFATreasureHuntEvent event;

      private SpawnChest(FFATreasureHuntEvent event) {
         this.event = event;
      }

      @Override
      public void run() {
         if (this.event.getState() != AbstractFightEvent.EVENT_STATE.NOT_ACTIVE) {
            this.event.spawnChest();
         }
      }
   }
}
