package l2e.gameserver.model.entity.events.model.impl;

import java.util.HashMap;
import java.util.Map;
import l2e.commons.collections.MultiValueSet;
import l2e.commons.threading.RunnableImpl;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.model.template.FightEventPlayer;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.serverpackets.CreatureSay;

public class LastManStandingEvent extends AbstractFightEvent {
   private FightEventPlayer _winner;

   public LastManStandingEvent(MultiValueSet<String> set) {
      super(set);
   }

   @Override
   public void onKilled(Creature actor, Creature victim) {
      if (actor != null && actor.isPlayer()) {
         FightEventPlayer fActor = this.getFightEventPlayer(actor.getActingPlayer());
         if (fActor != null && victim.isPlayer()) {
            fActor.increaseKills();
            this.updatePlayerScore(fActor);
            ServerMessage msg = new ServerMessage("FightEvents.YOU_HAVE_KILL", fActor.getPlayer().getLang());
            msg.add(victim.getName());
            this.sendMessageToPlayer(fActor.getPlayer(), AbstractFightEvent.MESSAGE_TYPES.GM, msg);
         }

         actor.getActingPlayer().sendUserInfo();
      }

      if (victim.isPlayer()) {
         FightEventPlayer fVictim = this.getFightEventPlayer(victim);
         fVictim.increaseDeaths();
         if (actor != null) {
            ServerMessage msg = new ServerMessage("FightEvents.YOU_KILLED", fVictim.getPlayer().getLang());
            msg.add(actor.getName());
            this.sendMessageToPlayer(fVictim.getPlayer(), AbstractFightEvent.MESSAGE_TYPES.GM, msg);
         }

         victim.getActingPlayer().broadcastCharInfo();
         this.leaveEvent(fVictim.getPlayer(), true);
         this.checkRoundOver();
      }

      super.onKilled(actor, victim);
   }

   @Override
   public void startEvent() {
      super.startEvent();
      ThreadPoolManager.getInstance().schedule(new LastManStandingEvent.InactivityCheck(), 60000L);
   }

   @Override
   public void startRound() {
      super.startRound();
      this.checkRoundOver();
   }

   @Override
   public boolean leaveEvent(Player player, boolean teleportTown) {
      boolean result = super.leaveEvent(player, teleportTown);
      if (result) {
         this.checkRoundOver();
      }

      return result;
   }

   private boolean checkRoundOver() {
      if (this.getState() != AbstractFightEvent.EVENT_STATE.STARTED) {
         return true;
      } else {
         int alivePlayers = 0;
         FightEventPlayer aliveFPlayer = null;

         for(FightEventPlayer iFPlayer : this.getPlayers(new String[]{"fighting_players"})) {
            if (this.isPlayerActive(iFPlayer.getPlayer())) {
               ++alivePlayers;
               aliveFPlayer = iFPlayer;
            }

            if (aliveFPlayer == null && !iFPlayer.getPlayer().isDead()) {
               aliveFPlayer = iFPlayer;
            }
         }

         if (alivePlayers <= 1) {
            this._winner = aliveFPlayer;
            if (this._winner != null) {
               this._winner.increaseScore(1);
               this.announceWinnerPlayer(false, this._winner);
            }

            this.updateScreenScores();
            this.setState(AbstractFightEvent.EVENT_STATE.OVER);
            ThreadPoolManager.getInstance().schedule(new Runnable() {
               @Override
               public void run() {
                  LastManStandingEvent.this.endRound();
               }
            }, 5000L);
            if (this._winner != null) {
               for(Player player : World.getInstance().getAllPlayers()) {
                  ServerMessage msg = new ServerMessage("FightEvents.WON_LAST_HERO", player.getLang());
                  msg.add(this._winner.getPlayer().getName());
                  msg.add(player.getEventName(this.getId()));
                  player.sendPacket(new CreatureSay(0, 18, player.getEventName(this.getId()), msg.toString()));
               }
            }

            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   protected boolean inScreenShowBeScoreNotKills() {
      return false;
   }

   @Override
   protected Map<Integer, Long> giveRewardForWinningTeam(FightEventPlayer fPlayer, Map<Integer, Long> rewards, boolean atLeast1Kill) {
      if (fPlayer == null) {
         return null;
      } else {
         if (rewards == null) {
            rewards = new HashMap<>();
         }

         if (fPlayer.equals(this._winner)) {
            for(int[] item : this._rewardByWinner) {
               if (item != null && item.length == 2) {
                  if (rewards.get(item[0]) != null) {
                     long amount = rewards.get(item[0]) + (long)item[1];
                     rewards.put(item[0], amount);
                  } else {
                     rewards.put(item[0], (long)item[1]);
                  }
               }
            }
         }

         return rewards;
      }
   }

   @Override
   public String getVisibleTitle(Player player, Player viewer, String currentTitle, boolean toMe) {
      FightEventPlayer realPlayer = this.getFightEventPlayer(player);
      if (realPlayer == null) {
         return currentTitle;
      } else {
         ServerMessage msg = new ServerMessage("FightEvents.TITLE_INFO2", viewer.getLang());
         msg.add(realPlayer.getKills());
         return msg.toString();
      }
   }

   private class InactivityCheck extends RunnableImpl {
      private InactivityCheck() {
      }

      @Override
      public void runImpl() throws Exception {
         if (LastManStandingEvent.this.getState() != AbstractFightEvent.EVENT_STATE.NOT_ACTIVE) {
            LastManStandingEvent.this.checkRoundOver();
            ThreadPoolManager.getInstance().schedule(this, 60000L);
         }
      }
   }
}
