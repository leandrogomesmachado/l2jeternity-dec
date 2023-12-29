package l2e.gameserver.model.entity.events.model.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import l2e.commons.collections.MultiValueSet;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.model.object.CTFCombatFlagObject;
import l2e.gameserver.model.entity.events.model.template.FightEventPlayer;
import l2e.gameserver.model.entity.events.model.template.FightEventTeam;
import l2e.gameserver.model.strings.server.ServerMessage;

public class CaptureTheFlagEvent extends AbstractFightEvent {
   private CaptureTheFlagEvent.CaptureFlagTeam[] _flagTeams;
   private final int[][] _rewardByCaptureFlag;

   public CaptureTheFlagEvent(MultiValueSet<String> set) {
      super(set);
      this._rewardByCaptureFlag = this.parseItemsList(set.getString("rewardByCaptureFlag", null));
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
            CaptureTheFlagEvent.CaptureFlagTeam flagTeam = this.getTeam(realVictim.getTeam());
            if (flagTeam != null && flagTeam._thisTeamHolder != null && flagTeam._thisTeamHolder._playerHolding.equals(realVictim)) {
               CaptureTheFlagEvent.CaptureFlagHolder holdingTeam = flagTeam._thisTeamHolder;
               if (holdingTeam != null && realVictim.equals(holdingTeam._playerHolding)) {
                  holdingTeam._enemyFlagHoldByPlayer.despawnObject();
               }

               this.spawnFlag(this.getTeam(flagTeam._thisTeamHolder._teamFlagOwner));
               flagTeam._thisTeamHolder = null;
            }
         }

         super.onKilled(actor, victim);
      } catch (Exception var6) {
         _log.log(Level.SEVERE, "Error on CaptureTheFlag OnKilled!", (Throwable)var6);
      }
   }

   @Override
   public void startEvent() {
      try {
         super.startEvent();
         this._flagTeams = new CaptureTheFlagEvent.CaptureFlagTeam[this.getTeams().size()];
         int i = 0;

         for(FightEventTeam team : this.getTeams()) {
            CaptureTheFlagEvent.CaptureFlagTeam flagTeam = new CaptureTheFlagEvent.CaptureFlagTeam();
            flagTeam._team = team;
            flagTeam._holder = this.spawnNpc(53003, this.getFlagHolderSpawnLocation(team), 0, false);
            this.spawnFlag(flagTeam);
            this._flagTeams[i] = flagTeam;
            ++i;
         }
      } catch (Exception var5) {
         _log.log(Level.SEVERE, "Error on CaptureTheFlag startEvent!", (Throwable)var5);
      }
   }

   @Override
   public void stopEvent() {
      try {
         super.stopEvent();

         for(CaptureTheFlagEvent.CaptureFlagTeam iFlagTeam : this._flagTeams) {
            if (iFlagTeam._flag != null) {
               iFlagTeam._flag.deleteMe();
            }

            if (iFlagTeam._holder != null) {
               iFlagTeam._holder.deleteMe();
            }

            if (iFlagTeam._thisTeamHolder != null && iFlagTeam._thisTeamHolder._enemyFlagHoldByPlayer != null) {
               iFlagTeam._thisTeamHolder._enemyFlagHoldByPlayer.despawnObject();
            }
         }

         this._flagTeams = null;
      } catch (Exception var5) {
         _log.log(Level.SEVERE, "Error on CaptureTheFlag stopEvent!", (Throwable)var5);
      }
   }

   public boolean tryToTakeFlag(Player player, Npc flag) {
      try {
         FightEventPlayer fPlayer = this.getFightEventPlayer(player);
         if (fPlayer == null) {
            return false;
         } else if (this.getState() != AbstractFightEvent.EVENT_STATE.STARTED) {
            return false;
         } else {
            CaptureTheFlagEvent.CaptureFlagTeam flagTeam = null;

            for(CaptureTheFlagEvent.CaptureFlagTeam iFlagTeam : this._flagTeams) {
               if (iFlagTeam._flag != null && iFlagTeam._flag.equals(flag)) {
                  flagTeam = iFlagTeam;
               }
            }

            if (fPlayer.getTeam().equals(flagTeam._team)) {
               this.giveFlagBack(fPlayer, flagTeam);
               return false;
            } else {
               return this.getEnemyFlag(fPlayer, flagTeam);
            }
         }
      } catch (Exception var9) {
         _log.log(Level.SEVERE, "Error on CaptureTheFlag tryToTakeFlag!", (Throwable)var9);
         return false;
      }
   }

   public void talkedWithFlagHolder(Player player, Npc holder) {
      try {
         FightEventPlayer fPlayer = this.getFightEventPlayer(player);
         if (fPlayer == null) {
            return;
         }

         if (this.getState() != AbstractFightEvent.EVENT_STATE.STARTED) {
            return;
         }

         CaptureTheFlagEvent.CaptureFlagTeam flagTeam = null;

         for(CaptureTheFlagEvent.CaptureFlagTeam iFlagTeam : this._flagTeams) {
            if (iFlagTeam._holder != null && iFlagTeam._holder.equals(holder)) {
               flagTeam = iFlagTeam;
            }
         }

         if (fPlayer.getTeam().equals(flagTeam._team)) {
            this.giveFlagBack(fPlayer, flagTeam);
         }
      } catch (Exception var9) {
         _log.log(Level.SEVERE, "Error on CaptureTheFlag talkedWithFlagHolder!", (Throwable)var9);
      }
   }

   private boolean getEnemyFlag(FightEventPlayer fPlayer, CaptureTheFlagEvent.CaptureFlagTeam enemyFlagTeam) {
      try {
         CaptureTheFlagEvent.CaptureFlagTeam goodTeam = this.getTeam(fPlayer.getTeam());
         Player player = fPlayer.getPlayer();
         if (enemyFlagTeam._flag == null) {
            return false;
         } else {
            enemyFlagTeam._flag.deleteMe();
            enemyFlagTeam._flag = null;
            CTFCombatFlagObject flag = new CTFCombatFlagObject();
            flag.spawnObject(player);
            CaptureTheFlagEvent.CaptureFlagHolder holder = new CaptureTheFlagEvent.CaptureFlagHolder();
            holder._enemyFlagHoldByPlayer = flag;
            holder._playerHolding = fPlayer;
            holder._teamFlagOwner = enemyFlagTeam._team;
            goodTeam._thisTeamHolder = holder;
            this.sendMessageToTeam(enemyFlagTeam._team, AbstractFightEvent.MESSAGE_TYPES.CRITICAL, "FightEvents.STOLEN_FLAG");

            for(FightEventPlayer iFPlayer : goodTeam._team.getPlayers()) {
               ServerMessage msg = new ServerMessage("FightEvents.PLAYER_STOLEN", iFPlayer.getPlayer().getLang());
               msg.add(new ServerMessage("FightEvents." + enemyFlagTeam._team.getName() + "", iFPlayer.getPlayer().getLang()).toString());
               this.sendMessageToPlayer(iFPlayer.getPlayer(), AbstractFightEvent.MESSAGE_TYPES.CRITICAL, msg);
            }

            return true;
         }
      } catch (Exception var10) {
         _log.log(Level.SEVERE, "Error on CaptureTheFlag talkedWithFlagHolder!", (Throwable)var10);
         return false;
      }
   }

   private CaptureTheFlagEvent.CaptureFlagTeam getTeam(FightEventTeam team) {
      if (team == null) {
         return null;
      } else {
         try {
            for(CaptureTheFlagEvent.CaptureFlagTeam iFlagTeam : this._flagTeams) {
               if (iFlagTeam._team != null && iFlagTeam._team.equals(team)) {
                  return iFlagTeam;
               }
            }

            return null;
         } catch (Exception var6) {
            _log.log(Level.SEVERE, "Error on CaptureTheFlag getTeam!", (Throwable)var6);
            return null;
         }
      }
   }

   private void giveFlagBack(FightEventPlayer fPlayer, CaptureTheFlagEvent.CaptureFlagTeam flagTeam) {
      try {
         CaptureTheFlagEvent.CaptureFlagHolder holdingTeam = flagTeam._thisTeamHolder;
         if (holdingTeam != null && fPlayer.equals(holdingTeam._playerHolding)) {
            holdingTeam._enemyFlagHoldByPlayer.despawnObject();
            this.spawnFlag(this.getTeam(holdingTeam._teamFlagOwner));
            flagTeam._thisTeamHolder = null;
            flagTeam._team.incScore(1);
            this.updateScreenScores();

            for(FightEventTeam team : this.getTeams()) {
               if (!team.equals(flagTeam._team)) {
                  this.sendMessageToTeam(holdingTeam._teamFlagOwner, AbstractFightEvent.MESSAGE_TYPES.CRITICAL, "FightEvents.GAIN_STORE", flagTeam._team);
               }
            }

            this.sendMessageToTeam(flagTeam._team, AbstractFightEvent.MESSAGE_TYPES.CRITICAL, "FightEvents.YOU_GAIN");
            fPlayer.increaseEventSpecificScore("capture");
         }
      } catch (Exception var6) {
         _log.log(Level.SEVERE, "Error on CaptureTheFlag giveFlagBack!", (Throwable)var6);
      }
   }

   private Location getFlagHolderSpawnLocation(FightEventTeam team) {
      return this.getMap().getKeyLocations()[team.getIndex() - 1];
   }

   private void spawnFlag(CaptureTheFlagEvent.CaptureFlagTeam flagTeam) {
      try {
         Npc flag = this.spawnNpc(53000 + flagTeam._team.getIndex(), this.getFlagHolderSpawnLocation(flagTeam._team), 0, false);
         flagTeam._flag = flag;
      } catch (Exception var3) {
         _log.log(Level.SEVERE, "Error on CaptureTheFlag spawnFlag!", (Throwable)var3);
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
   public void loggedOut(Player player) {
      FightEventPlayer fPlayer = this.getFightEventPlayer(player);
      if (fPlayer != null) {
         CaptureTheFlagEvent.CaptureFlagTeam flagTeam = this.getTeam(fPlayer.getTeam());
         if (flagTeam != null && flagTeam._thisTeamHolder != null && flagTeam._thisTeamHolder._playerHolding.equals(fPlayer)) {
            CaptureTheFlagEvent.CaptureFlagHolder holdingTeam = flagTeam._thisTeamHolder;
            if (holdingTeam != null && fPlayer.equals(holdingTeam._playerHolding)) {
               holdingTeam._enemyFlagHoldByPlayer.despawnObject();
            }

            this.spawnFlag(this.getTeam(flagTeam._thisTeamHolder._teamFlagOwner));
            flagTeam._thisTeamHolder = null;
         }
      }

      super.loggedOut(player);
   }

   @Override
   protected void giveItemRewardsForPlayer(FightEventPlayer fPlayer, Map<Integer, Long> rewards, boolean isTopKiller) {
      if (fPlayer != null) {
         if (rewards == null) {
            rewards = new HashMap<>();
         }

         if (this._rewardByCaptureFlag != null && this._rewardByCaptureFlag.length > 0) {
            for(int[] item : this._rewardByCaptureFlag) {
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

   private class CaptureFlagHolder {
      private FightEventPlayer _playerHolding;
      private CTFCombatFlagObject _enemyFlagHoldByPlayer;
      private FightEventTeam _teamFlagOwner;

      private CaptureFlagHolder() {
      }
   }

   private class CaptureFlagTeam {
      private FightEventTeam _team;
      private Npc _holder;
      private Npc _flag;
      private CaptureTheFlagEvent.CaptureFlagHolder _thisTeamHolder;

      private CaptureFlagTeam() {
      }
   }
}
