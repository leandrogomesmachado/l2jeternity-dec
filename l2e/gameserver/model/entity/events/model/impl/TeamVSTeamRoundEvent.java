package l2e.gameserver.model.entity.events.model.impl;

import l2e.commons.collections.MultiValueSet;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.model.template.FightEventPlayer;
import l2e.gameserver.model.entity.events.model.template.FightEventTeam;
import l2e.gameserver.model.strings.server.ServerMessage;

public class TeamVSTeamRoundEvent extends AbstractFightEvent {
   private static final long MAX_FIGHT_TIME = 300000L;
   protected long _lastKill = 0L;

   public TeamVSTeamRoundEvent(MultiValueSet<String> set) {
      super(set);
   }

   @Override
   public void onKilled(Creature actor, Creature victim) {
      if (actor != null && actor.isPlayer()) {
         FightEventPlayer realActor = this.getFightEventPlayer(actor.getActingPlayer());
         if (victim.isPlayer() && realActor != null) {
            realActor.increaseKills();
            realActor.getTeam().incScore(1);
            this.updatePlayerScore(realActor);
            this.updateScreenScores();
            ServerMessage msg = new ServerMessage("FightEvents.YOU_HAVE_KILL", realActor.getPlayer().getLang());
            msg.add(victim.getName());
            this.sendMessageToPlayer(realActor.getPlayer(), AbstractFightEvent.MESSAGE_TYPES.GM, msg);
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

            this.checkTeamPlayers(realVictim);
            victim.getActingPlayer().broadcastCharInfo();
         }

         this._lastKill = System.currentTimeMillis();
      }

      super.onKilled(actor, victim);
   }

   @Override
   public void startRound() {
      super.startRound();
      this._lastKill = System.currentTimeMillis();
      ThreadPoolManager.getInstance().schedule(new TeamVSTeamRoundEvent.CheckFightersInactive(this), 5000L);
   }

   @Override
   public void endRound() {
      this._state = AbstractFightEvent.EVENT_STATE.OVER;
      if (!this.isLastRound()) {
         this.sendMessageToFighting(AbstractFightEvent.MESSAGE_TYPES.SCREEN_BIG, "FightEvents.ROUND_IS_OVER", false, String.valueOf(this.getCurrentRound()));
      } else {
         this.sendMessageToFighting(AbstractFightEvent.MESSAGE_TYPES.SCREEN_BIG, "FightEvents.EVENT_IS_OVER", false);
      }

      if (!this.isLastRound()) {
         for(FightEventTeam team : this.getTeams()) {
            team.setSpawnLoc(null);
         }

         ThreadPoolManager.getInstance().schedule(() -> {
            for(FightEventPlayer iFPlayerxx : this.getPlayers(new String[]{"fighting_players"})) {
               this.teleportSinglePlayer(iFPlayerxx, false, true);
            }

            this.startNewTimer(true, 0, "startRoundTimer", new Object[]{TIME_PREPARATION_BETWEEN_NEXT_ROUNDS});
         }, (long)(TIME_AFTER_ROUND_END_TO_RETURN_SPAWN * 1000));
      } else {
         this.ressAndHealPlayers();
         ThreadPoolManager.getInstance().schedule(() -> this.stopEvent(), 10000L);
         if (this.isTeamed()) {
            this.announceWinnerTeam(true, null);
         } else {
            this.announceWinnerPlayer(true, null);
         }
      }

      for(FightEventPlayer iFPlayer : this.getPlayers(new String[]{"fighting_players"})) {
         iFPlayer.getPlayer().broadcastUserInfo(true);
      }
   }

   private void checkTeamPlayers(FightEventPlayer player) {
      boolean allDeath = true;

      for(FightEventPlayer fPlayer : player.getTeam().getPlayers()) {
         if (fPlayer != null && !fPlayer.getPlayer().isDead()) {
            allDeath = false;
            break;
         }
      }

      if (allDeath) {
         this.endRound();
      }
   }

   @Override
   public String getVisibleTitle(Player player, Player viewer, String currentTitle, boolean toMe) {
      FightEventPlayer fPlayer = this.getFightEventPlayer(player);
      if (fPlayer == null) {
         return currentTitle;
      } else {
         ServerMessage msg = new ServerMessage("FightEvents.TITLE_INFO", viewer.getLang());
         msg.add(fPlayer.getKills());
         msg.add(fPlayer.getDeaths());
         return msg.toString();
      }
   }

   protected static class CheckFightersInactive implements Runnable {
      private final TeamVSTeamRoundEvent _activeEvent;

      public CheckFightersInactive(TeamVSTeamRoundEvent event) {
         this._activeEvent = event;
      }

      @Override
      public void run() {
         if (this._activeEvent.getState() == AbstractFightEvent.EVENT_STATE.STARTED) {
            long currentTime = System.currentTimeMillis();
            if (this._activeEvent._lastKill + 300000L < currentTime) {
               for(FightEventPlayer fPlayer : this._activeEvent.getPlayers(new String[]{"fighting_players"})) {
                  if (fPlayer != null && fPlayer.getPlayer() != null && !fPlayer.getPlayer().isDead()) {
                     fPlayer.getPlayer().doDie(null);
                  }
               }

               this._activeEvent.endRound();
            }

            ThreadPoolManager.getInstance().schedule(this, 5000L);
         }
      }
   }
}
