package l2e.gameserver.model.entity.events.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.collections.MultiValueSet;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.model.template.FightEventPlayer;
import l2e.gameserver.model.entity.events.model.template.FightEventTeam;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.strings.server.ServerMessage;

public class KoreanStyleEvent extends AbstractFightEvent {
   private final long MAX_FIGHT_TIME = 90000L;
   private final FightEventPlayer[] _fightingPlayers;
   private final int[] _lastTeamChosenSpawn;
   private long _lastKill;
   private final boolean _winnerByDamage;
   private ScheduledFuture<?> _fightTask;

   public KoreanStyleEvent(MultiValueSet<String> set) {
      super(set);
      this._winnerByDamage = set.getBool("winnerByDamage", true);
      this._lastKill = 0L;
      this._fightingPlayers = new FightEventPlayer[2];
      this._lastTeamChosenSpawn = new int[]{0, 0};
   }

   @Override
   public void onKilled(Creature actor, Creature victim) {
      if (actor != null && actor.isPlayer()) {
         FightEventPlayer realActor = this.getFightEventPlayer(actor.getActingPlayer());
         if (victim.isPlayer() && realActor != null) {
            realActor.increaseKills();
            this.updatePlayerScore(realActor);
            this.updateScreenScores();
            ServerMessage msg = new ServerMessage("FightEvents.YOU_HAVE_KILL", realActor.getPlayer().getLang());
            msg.add(victim.getName());
            this.sendMessageToPlayer(realActor.getPlayer(), AbstractFightEvent.MESSAGE_TYPES.GM, msg);
         }

         actor.getActingPlayer().sendUserInfo();
      }

      if (victim.isPlayer()) {
         if (victim.getSummon() != null && !victim.getSummon().isDead()) {
            victim.getSummon().doDie(actor);
         }

         FightEventPlayer realVictim = this.getFightEventPlayer(victim);
         realVictim.increaseDeaths();
         if (actor != null) {
            ServerMessage msg = new ServerMessage("FightEvents.YOU_KILLED", realVictim.getPlayer().getLang());
            msg.add(actor.getName());
            this.sendMessageToPlayer(realVictim.getPlayer(), AbstractFightEvent.MESSAGE_TYPES.GM, msg);
         }

         victim.getActingPlayer().broadcastCharInfo();
         this._lastKill = System.currentTimeMillis();
      }

      this.checkFightingPlayers();
      super.onKilled(actor, victim);
   }

   @Override
   public void onDamage(Creature actor, Creature victim, double damage) {
      if (actor != null && actor.isPlayable()) {
         FightEventPlayer realActor = this.getFightEventPlayer(actor.getActingPlayer());
         if (victim.isPlayer() && realActor != null) {
            realActor.increaseDamage(damage);
         }
      }

      super.onDamage(actor, victim, damage);
   }

   @Override
   public void loggedOut(Player player) {
      super.loggedOut(player);

      for(FightEventPlayer fPlayer : this._fightingPlayers) {
         if (fPlayer != null && fPlayer.getPlayer() != null && fPlayer.getPlayer().equals(player)) {
            this.checkFightingPlayers();
         }
      }
   }

   @Override
   public boolean leaveEvent(Player player, boolean teleportTown) {
      super.leaveEvent(player, teleportTown);
      Effect eInvis = player.getFirstEffect(EffectType.INVINCIBLE);
      if (eInvis != null) {
         eInvis.exit();
      }

      player.startHealBlocked(false);
      player.setIsInvul(false);

      try {
         if (player.isRooted()) {
            player.startRooted(false);
         }
      } catch (IllegalStateException var8) {
      }

      player.stopAbnormalEffect(AbnormalEffect.ROOT);
      if (player.getSummon() != null) {
         if (player.getSummon().isRooted()) {
            player.getSummon().startRooted(false);
         }

         player.getSummon().stopAbnormalEffect(AbnormalEffect.ROOT);
      }

      if (this.getState() != AbstractFightEvent.EVENT_STATE.STARTED) {
         return true;
      } else {
         for(FightEventPlayer fPlayer : this._fightingPlayers) {
            if (fPlayer != null && fPlayer.getPlayer() != null && fPlayer.getPlayer().equals(player)) {
               this.checkFightingPlayers();
            }
         }

         return true;
      }
   }

   @Override
   public void startEvent() {
      super.startEvent();

      for(FightEventPlayer fPlayer : this.getPlayers(new String[]{"fighting_players", "registered_players"})) {
         Player player = fPlayer.getPlayer();
         if (player.isDead()) {
            player.doRevive();
         }

         if (player.isFakeDeathNow()) {
            player.stopFakeDeath(true);
         }

         player.sitDownNow();
         player.resetDisabledSkills();
         player.resetReuse();
         player.sendSkillList(true);
         if (player.getSummon() != null) {
            player.getSummon().startAbnormalEffect(AbnormalEffect.ROOT);
         }
      }
   }

   @Override
   public void startRound() {
      super.startRound();
      this.checkFightingPlayers();
      this._lastKill = System.currentTimeMillis();
      if (this._fightTask != null) {
         this._fightTask.cancel(false);
         this._fightTask = null;
      }

      this._fightTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new KoreanStyleEvent.CheckFightersInactive(), 1000L, 5000L);
   }

   @Override
   public void endRound() {
      super.endRound();
      super.unrootPlayers();
   }

   @Override
   public void stopEvent() {
      super.stopEvent();
      if (this._fightTask != null) {
         this._fightTask.cancel(false);
         this._fightTask = null;
      }
   }

   private void checkFightingPlayers() {
      if (this.getState() != AbstractFightEvent.EVENT_STATE.OVER && this.getState() != AbstractFightEvent.EVENT_STATE.NOT_ACTIVE) {
         boolean changed = false;

         for(int i = 0; i < this._fightingPlayers.length; ++i) {
            FightEventPlayer oldPlayer = this._fightingPlayers[i];
            if (oldPlayer == null || !this.isPlayerActive(oldPlayer.getPlayer()) || this.getFightEventPlayer(oldPlayer.getPlayer()) == null) {
               if (oldPlayer != null && !oldPlayer.getPlayer().isDead()) {
                  oldPlayer.getPlayer().doDie(null);
                  oldPlayer.setDamage(0.0);
                  return;
               }

               FightEventPlayer newPlayer = this.chooseNewPlayer(i + 1);
               if (newPlayer == null) {
                  for(FightEventTeam team : this.getTeams()) {
                     if (team.getIndex() != i + 1) {
                        team.incScore(1);
                     }
                  }

                  this.endRound();
                  return;
               }

               newPlayer.getPlayer().isntAfk();
               this._fightingPlayers[i] = newPlayer;
               changed = true;
            }
         }

         if (changed) {
            for(FightEventPlayer iFPlayer : this.getPlayers(new String[]{"fighting_players"})) {
               if (iFPlayer != null) {
                  ServerMessage message = new ServerMessage("FightEvents.PL_VS_PL", iFPlayer.getPlayer().getLang());
                  message.add(this._fightingPlayers[0].getPlayer().getName());
                  message.add(this._fightingPlayers[1].getPlayer().getName());
                  this.sendMessageToPlayer(iFPlayer.getPlayer(), AbstractFightEvent.MESSAGE_TYPES.SCREEN_BIG, message);
               }
            }

            this.preparePlayers();
         }
      }
   }

   private FightEventPlayer chooseNewPlayer(int teamIndex) {
      List<FightEventPlayer> alivePlayersFromTeam = new ArrayList<>();

      for(FightEventPlayer fPlayer : this.getPlayers(new String[]{"fighting_players"})) {
         if (fPlayer.getPlayer().isSitting() && fPlayer.getTeam().getIndex() == teamIndex) {
            alivePlayersFromTeam.add(fPlayer);
         }
      }

      if (alivePlayersFromTeam.isEmpty()) {
         return null;
      } else {
         return alivePlayersFromTeam.size() == 1 ? alivePlayersFromTeam.get(0) : Rnd.get(alivePlayersFromTeam);
      }
   }

   private void preparePlayers() {
      for(int i = 0; i < this._fightingPlayers.length; ++i) {
         FightEventPlayer fPlayer = this._fightingPlayers[i];
         Player player = fPlayer.getPlayer();
         if (player.isBlocked()) {
            player.unblock();
         }

         player.standUp();
         player.isntAfk();
         player.resetDisabledSkills();
         player.resetReuse();
         player.sendSkillList(true);
         healFull(player);
         if (player.getSummon() instanceof PetInstance) {
            player.getSummon().unSummon(player);
         }

         if (player.getSummon() != null && !player.getSummon().isDead()) {
            healFull(player.getSummon());
         }

         Effect eInvis = player.getFirstEffect(EffectType.INVINCIBLE);
         if (eInvis != null) {
            eInvis.exit();
         }

         player.startHealBlocked(false);
         player.setIsInvul(false);
         fPlayer.setLastDamageTime();
         Location loc = this.getMap().getKeyLocations()[i];
         player.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), true);
         this.rootPlayer(player);
         player.broadcastUserInfo(true);
         player.sendMessage(new ServerMessage("FightEvents.YOU_HAVE_10SEC", player.getLang()).toString());
      }

      ThreadPoolManager.getInstance().schedule(new Runnable() {
         @Override
         public void run() {
            for(int i = 0; i < KoreanStyleEvent.this._fightingPlayers.length; ++i) {
               FightEventPlayer fPlayer = KoreanStyleEvent.this._fightingPlayers[i];
               Player player = fPlayer.getPlayer();
               if (player.isRooted()) {
                  player.startRooted(false);
               }

               player.stopAbnormalEffect(AbnormalEffect.ROOT);
               KoreanStyleEvent.healFull(player);
               if (player.getSummon() instanceof PetInstance) {
                  player.getSummon().unSummon(player);
               }

               if (player.getSummon() != null && !player.getSummon().isDead()) {
                  KoreanStyleEvent.healFull(player.getSummon());
                  if (player.getSummon().isRooted()) {
                     player.getSummon().startRooted(false);
                  }

                  player.getSummon().stopAbnormalEffect(AbnormalEffect.ROOT);
               }
            }
         }
      }, 10000L);
   }

   private static void healFull(Playable playable) {
      cleanse(playable);
      playable.setCurrentHp(playable.getMaxHp());
      playable.setCurrentMp(playable.getMaxMp());
      playable.setCurrentCp(playable.getMaxCp());
   }

   private static void cleanse(Playable playable) {
      try {
         for(Effect e : playable.getAllEffects()) {
            if (e.getSkill().isOffensive() && e.getSkill().canBeDispeled()) {
               e.exit();
            }
         }
      } catch (IllegalStateException var5) {
      }
   }

   @Override
   public boolean canAttack(Creature target, Creature attacker) {
      if (this.getState() != AbstractFightEvent.EVENT_STATE.STARTED) {
         return false;
      } else if (target == null || !target.isPlayable() || attacker == null || !attacker.isPlayable()) {
         return false;
      } else {
         return this.isFighting(target) && this.isFighting(attacker);
      }
   }

   @Override
   public boolean canUseMagic(Creature target, Creature attacker, Skill skill) {
      if (this.getState() != AbstractFightEvent.EVENT_STATE.STARTED) {
         return false;
      } else if (target == null || !target.isPlayable() || attacker == null || !attacker.isPlayable()) {
         return false;
      } else if (attacker != null && target != null && !this.canUseSkill(attacker, target, skill)) {
         return false;
      } else {
         return this.isFighting(target) && this.isFighting(attacker);
      }
   }

   private boolean isFighting(Creature actor) {
      for(FightEventPlayer fPlayer : this._fightingPlayers) {
         if (fPlayer != null && fPlayer.getPlayer() != null && fPlayer.getPlayer().equals(actor.getActingPlayer())) {
            return true;
         }
      }

      return false;
   }

   private Player calcWinnerByKills(long currentTime) {
      Player playerToKill = null;

      for(FightEventPlayer fPlayer : this._fightingPlayers) {
         if (fPlayer != null && fPlayer.getPlayer() != null && fPlayer.getPlayer().getClient().isDetached()) {
            playerToKill = fPlayer.getPlayer();
            break;
         }
      }

      if (playerToKill == null && this._fightingPlayers[0] != null && this._fightingPlayers[1] != null) {
         if (this._fightingPlayers[0].getKills() == 0 && this._fightingPlayers[1].getKills() == 0) {
            return null;
         }

         if (this._fightingPlayers[0].getKills() > this._fightingPlayers[1].getKills()) {
            playerToKill = this._fightingPlayers[1].getPlayer();
         } else if (this._fightingPlayers[0].getKills() < this._fightingPlayers[1].getKills()) {
            playerToKill = this._fightingPlayers[0].getPlayer();
         }
      }

      return playerToKill;
   }

   private Player calcWinnerByDamage(long currentTime) {
      double playerMinDamage = Double.MAX_VALUE;
      Player playerToKill = null;

      for(FightEventPlayer fPlayer : this._fightingPlayers) {
         if (fPlayer != null && fPlayer.getPlayer() != null) {
            if (fPlayer.getPlayer().getClient().isDetached()) {
               playerToKill = fPlayer.getPlayer();
               playerMinDamage = -100.0;
            } else if (currentTime - fPlayer.getPlayer().getLastNotAfkTime() > 8000L) {
               playerToKill = fPlayer.getPlayer();
               playerMinDamage = -1.0;
            } else if (fPlayer.getDamage() < playerMinDamage) {
               playerToKill = fPlayer.getPlayer();
               playerMinDamage = fPlayer.getDamage();
            }
         }
      }

      return playerToKill;
   }

   @Override
   protected Location getSinglePlayerSpawnLocation(FightEventPlayer fPlayer) {
      Location[] spawnLocations = (Location[])this.getMap().getTeamSpawns().get(fPlayer.getTeam().getIndex());
      int ordinalTeamIndex = fPlayer.getTeam().getIndex() - 1;
      int lastSpawnIndex = this._lastTeamChosenSpawn[ordinalTeamIndex];
      if (++lastSpawnIndex >= spawnLocations.length) {
         lastSpawnIndex = 0;
      }

      this._lastTeamChosenSpawn[ordinalTeamIndex] = lastSpawnIndex;
      return spawnLocations[lastSpawnIndex];
   }

   @Override
   protected Map<Integer, Long> giveRewardForWinningTeam(FightEventPlayer fPlayer, Map<Integer, Long> rewards, boolean atLeast1Kill) {
      return super.giveRewardForWinningTeam(fPlayer, rewards, false);
   }

   @Override
   protected void handleAfk(FightEventPlayer fPlayer, boolean setAsAfk) {
   }

   @Override
   protected void unrootPlayers() {
   }

   @Override
   protected boolean inScreenShowBeScoreNotKills() {
      return false;
   }

   @Override
   protected boolean inScreenShowBeTeamNotInvidual() {
      return false;
   }

   @Override
   protected boolean isAfkTimerStopped(Player player) {
      return player.isSitting() || super.isAfkTimerStopped(player);
   }

   @Override
   public boolean canStandUp(Player player) {
      for(FightEventPlayer fPlayer : this._fightingPlayers) {
         if (fPlayer != null && fPlayer.getPlayer().equals(player)) {
            return true;
         }
      }

      return false;
   }

   @Override
   protected List<List<Player>> spreadTeamInPartys(FightEventTeam team) {
      return Collections.emptyList();
   }

   @Override
   protected void createParty(List<Player> listOfPlayers) {
   }

   private class CheckFightersInactive implements Runnable {
      private CheckFightersInactive() {
      }

      @Override
      public void run() {
         if (KoreanStyleEvent.this.getState() == AbstractFightEvent.EVENT_STATE.STARTED) {
            long currentTime = System.currentTimeMillis();

            for(FightEventPlayer fPlayer : KoreanStyleEvent.this._fightingPlayers) {
               if (fPlayer != null && fPlayer.getPlayer() != null && fPlayer.getLastDamageTime() < currentTime - 120000L) {
                  fPlayer.getPlayer().doDie(null);
               }
            }

            if (KoreanStyleEvent.this._lastKill + 90000L < currentTime) {
               Player playerToKill = null;
               if (KoreanStyleEvent.this._winnerByDamage) {
                  playerToKill = KoreanStyleEvent.this.calcWinnerByDamage(currentTime);
               } else {
                  playerToKill = KoreanStyleEvent.this.calcWinnerByKills(currentTime);
                  if (playerToKill == null) {
                     playerToKill = KoreanStyleEvent.this.calcWinnerByDamage(currentTime);
                  }
               }

               if (playerToKill != null) {
                  playerToKill.doDie(null);
               }
            }
         }
      }
   }
}
