package l2e.gameserver.model.entity.events.model.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import l2e.commons.collections.MultiValueSet;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.BaseToCaptureInstance;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.model.template.FightEventPlayer;
import l2e.gameserver.model.entity.events.model.template.FightEventTeam;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.strings.server.ServerMessage;

public class BaseCaptureEvent extends AbstractFightEvent {
   private BaseCaptureEvent.CaptureBaseTeam[] _baseTeams;
   private final int[][] _rewardByCaptureBase;

   public BaseCaptureEvent(MultiValueSet<String> set) {
      super(set);
      this._rewardByCaptureBase = this.parseItemsList(set.getString("rewardByCaptureBase", null));
   }

   @Override
   public void onKilled(Creature actor, Creature victim) {
      try {
         if (actor != null && actor.isPlayer()) {
            FightEventPlayer realActor = this.getFightEventPlayer(actor.getActingPlayer());
            if (victim.isPlayer() && realActor != null) {
               realActor.increaseKills();
               this.updatePlayerScore(realActor);
               ServerMessage msg = new ServerMessage("FightEvents.YOU_HAVE_KILL", realActor.getPlayer().getLang());
               msg.add(victim.getName());
               this.sendMessageToPlayer(realActor.getPlayer(), AbstractFightEvent.MESSAGE_TYPES.GM, msg);
            }

            actor.getActingPlayer().sendUserInfo();
         }

         if (victim.isPlayer()) {
            FightEventPlayer realVictim = this.getFightEventPlayer(victim);
            realVictim.increaseDeaths();
            if (actor != null) {
               ServerMessage msg = new ServerMessage("FightEvents.YOU_KILLED", realVictim.getPlayer().getLang());
               msg.add(actor.getName());
               this.sendMessageToPlayer(realVictim.getPlayer(), AbstractFightEvent.MESSAGE_TYPES.GM, msg);
            }

            victim.getActingPlayer().broadcastCharInfo();
         }

         super.onKilled(actor, victim);
      } catch (Exception var5) {
         _log.log(Level.SEVERE, "Error on CaptureBase OnKilled!", (Throwable)var5);
      }
   }

   @Override
   public boolean canAttack(Creature target, Creature attacker) {
      if (this._state != AbstractFightEvent.EVENT_STATE.STARTED) {
         return false;
      } else {
         Player player = attacker.getActingPlayer();
         if (player == null) {
            return true;
         } else {
            if (target instanceof BaseToCaptureInstance) {
               for(BaseCaptureEvent.CaptureBaseTeam iBaseTeam : this._baseTeams) {
                  if (iBaseTeam._base != null && iBaseTeam._base.equals(target)) {
                     FightEventPlayer fPlayer = this.getFightEventPlayer(player);
                     if (fPlayer != null) {
                        if (fPlayer.getTeam().equals(iBaseTeam._team)) {
                           return false;
                        }

                        return true;
                     }
                  }
               }
            }

            if (this.isTeamed()) {
               FightEventPlayer targetFPlayer = this.getFightEventPlayer(target);
               FightEventPlayer attackerFPlayer = this.getFightEventPlayer(attacker);
               if (targetFPlayer == null || attackerFPlayer == null || targetFPlayer.getTeam().equals(attackerFPlayer.getTeam())) {
                  return false;
               }
            }

            return this.canAttackPlayers();
         }
      }
   }

   @Override
   public boolean canUseMagic(Creature target, Creature attacker, Skill skill) {
      if (this._state != AbstractFightEvent.EVENT_STATE.STARTED) {
         return false;
      } else {
         if (target instanceof BaseToCaptureInstance) {
            for(BaseCaptureEvent.CaptureBaseTeam iBaseTeam : this._baseTeams) {
               if (iBaseTeam._base != null && iBaseTeam._base.equals(target)) {
                  FightEventPlayer fPlayer = this.getFightEventPlayer(attacker);
                  if (fPlayer != null) {
                     if (fPlayer.getTeam().equals(iBaseTeam._team)) {
                        return false;
                     }

                     return true;
                  }
               }
            }
         }

         if (attacker != null && target != null) {
            if (!this.canUseSkill(attacker, target, skill)) {
               return false;
            }

            if (attacker.getObjectId() == target.getObjectId()) {
               return true;
            }
         }

         if (this.isTeamed()) {
            FightEventPlayer targetFPlayer = this.getFightEventPlayer(target);
            FightEventPlayer attackerFPlayer = this.getFightEventPlayer(attacker);
            if (targetFPlayer == null || attackerFPlayer == null || targetFPlayer.getTeam().equals(attackerFPlayer.getTeam()) && skill.isOffensive()) {
               return false;
            }
         }

         return this.canAttackPlayers();
      }
   }

   @Override
   public void startEvent() {
      try {
         super.startEvent();
         this._baseTeams = new BaseCaptureEvent.CaptureBaseTeam[this.getTeams().size()];
         int i = 0;

         for(FightEventTeam team : this.getTeams()) {
            BaseCaptureEvent.CaptureBaseTeam baseTeam = new BaseCaptureEvent.CaptureBaseTeam();
            baseTeam._team = team;
            this.spawnBase(baseTeam);
            this._baseTeams[i] = baseTeam;
            ++i;
         }
      } catch (Exception var5) {
         _log.log(Level.SEVERE, "Error on CaptureBase startEvent!", (Throwable)var5);
      }
   }

   @Override
   public void stopEvent() {
      try {
         super.stopEvent();

         for(BaseCaptureEvent.CaptureBaseTeam iBaseTeam : this._baseTeams) {
            if (iBaseTeam._base != null) {
               iBaseTeam._base.deleteMe();
            }
         }

         this._baseTeams = null;
      } catch (Exception var5) {
         _log.log(Level.SEVERE, "Error on CaptureBase stopEvent!", (Throwable)var5);
      }
   }

   public void destroyBase(Player player, Npc holder) {
      try {
         FightEventPlayer fPlayer = this.getFightEventPlayer(player);
         if (fPlayer == null) {
            return;
         }

         if (this.getState() != AbstractFightEvent.EVENT_STATE.STARTED) {
            return;
         }

         BaseCaptureEvent.CaptureBaseTeam baseTeam = null;

         for(BaseCaptureEvent.CaptureBaseTeam iBaseTeam : this._baseTeams) {
            if (iBaseTeam._base != null && iBaseTeam._base.equals(holder)) {
               baseTeam = iBaseTeam;
            }
         }

         if (!fPlayer.getTeam().equals(baseTeam._team)) {
            fPlayer.getTeam().incScore(1);
            this.updateScreenScores();

            for(FightEventTeam team : this.getTeams()) {
               if (!team.equals(baseTeam._team)) {
                  this.sendMessageToTeam(fPlayer.getTeam(), AbstractFightEvent.MESSAGE_TYPES.CRITICAL, "FightEvents.TEAM_WON_EVENT", fPlayer.getTeam());
               }
            }

            this.sendMessageToTeam(fPlayer.getTeam(), AbstractFightEvent.MESSAGE_TYPES.CRITICAL, "FightEvents.CAPTURE_BASE");
            fPlayer.increaseEventSpecificScore("capture");
            this.stopEvent();
         }
      } catch (Exception var9) {
         _log.log(Level.SEVERE, "Error on CaptureBase destroyBase!", (Throwable)var9);
      }
   }

   private Location getBaseSpawnLocation(FightEventTeam team) {
      return this.getMap().getKeyLocations()[team.getIndex() - 1];
   }

   private void spawnBase(BaseCaptureEvent.CaptureBaseTeam baseTeam) {
      try {
         Npc base = this.spawnNpc(53003 + baseTeam._team.getIndex(), this.getBaseSpawnLocation(baseTeam._team), 0, false);
         baseTeam._base = base;
      } catch (Exception var3) {
         _log.log(Level.SEVERE, "Error on CaptureBase spawnBase!", (Throwable)var3);
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

   @Override
   protected void giveItemRewardsForPlayer(FightEventPlayer fPlayer, Map<Integer, Long> rewards, boolean isTopKiller) {
      if (fPlayer != null) {
         if (rewards == null) {
            rewards = new HashMap<>();
         }

         if (this._rewardByCaptureBase != null && this._rewardByCaptureBase.length > 0) {
            for(int[] item : this._rewardByCaptureBase) {
               if (item != null && item.length == 2) {
                  if (rewards.containsKey(item[0])) {
                     long amount = rewards.get(item[0]) + (long)(item[1] * fPlayer.getTeam().getScore());
                     rewards.put(item[0], amount);
                  } else {
                     rewards.put(item[0], (long)(item[1] * fPlayer.getTeam().getScore()));
                  }
               }
            }
         }

         super.giveItemRewardsForPlayer(fPlayer, rewards, isTopKiller);
      }
   }

   private class CaptureBaseTeam {
      private FightEventTeam _team;
      private Npc _base;

      private CaptureBaseTeam() {
      }
   }
}
