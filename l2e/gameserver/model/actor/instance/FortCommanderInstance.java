package l2e.gameserver.model.actor.instance;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.character.CharacterAI;
import l2e.gameserver.ai.guard.FortGuardAI;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.reward.RewardList;
import l2e.gameserver.model.reward.RewardType;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.SpawnFortSiege;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class FortCommanderInstance extends DefenderInstance {
   private boolean _canTalk;

   public FortCommanderInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.FortCommanderInstance);
      this.setIsSiegeGuard(true);
      this._canTalk = true;
   }

   @Override
   public CharacterAI initAI() {
      return new FortGuardAI(this);
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      if (attacker != null && attacker.isPlayer()) {
         return this.getFort() != null
            && this.getFort().getId() > 0
            && this.getFort().getSiege().getIsInProgress()
            && !this.getFort().getSiege().checkIsDefender(((Player)attacker).getClan());
      } else {
         return false;
      }
   }

   @Override
   public void addDamageHate(Creature attacker, int damage, int aggro) {
      if (attacker != null) {
         if (!(attacker instanceof FortCommanderInstance)) {
            super.addDamageHate(attacker, damage, aggro);
         }
      }
   }

   @Override
   protected void onDeath(Creature killer) {
      if (this.getFort().getSiege().getIsInProgress()) {
         this.getFort().getSiege().killedCommander(this);
      }

      if (killer != null && (killer.isSummon() || killer.isPlayer()) && Config.EPAULETTE_ONLY_FOR_REG) {
         Player player = killer.isSummon() ? ((Summon)killer).getOwner() : killer.getActingPlayer();
         if (player == null) {
            super.onDeath(killer);
            return;
         }

         if (player.getClan() != null && this.getFort() != null && this.getFort().getId() > 0 && this.getFort().getSiege().checkIsAttacker(player.getClan())) {
            Map<Player, Attackable.RewardInfo> rewards = new ConcurrentHashMap<>();
            Player maxDealer = null;
            int maxDamage = 0;

            for(Attackable.AggroInfo info : this.getAggroList().values()) {
               if (info != null) {
                  Player attacker = info.getAttacker().getActingPlayer();
                  if (attacker != null) {
                     int damage = info.getDamage();
                     if (damage > 1 && Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, attacker, true)) {
                        Attackable.RewardInfo reward = rewards.get(attacker);
                        if (reward == null) {
                           reward = new Attackable.RewardInfo(attacker, damage);
                           rewards.put(attacker, reward);
                        } else {
                           reward.addDamage(damage);
                        }

                        if (reward.getDamage() > maxDamage) {
                           maxDealer = attacker;
                           maxDamage = reward.getDamage();
                        }
                     }
                  }
               }
            }

            for(Entry<RewardType, RewardList> entry : this.getTemplate().getRewards().entrySet()) {
               this.rollRewards(entry, killer, (Creature)(maxDealer != null && maxDealer.isOnline() ? maxDealer : killer));
            }
         }
      }

      super.onDeath(killer);
   }

   @Override
   public void returnHome() {
      if (!this.isInsideRadius(this.getSpawn().getX(), this.getSpawn().getY(), 200, false)) {
         if (Config.DEBUG) {
            _log.info(this.getObjectId() + ": moving home");
         }

         this.setisReturningToSpawnPoint(true);
         this.clearAggroList();
         if (this.hasAI()) {
            this.getAI().setIntention(CtrlIntention.MOVING, this.getSpawn().getLocation());
         }
      }
   }

   @Override
   public final void addDamage(Creature attacker, int damage, Skill skill) {
      Spawner spawn = this.getSpawn();
      if (spawn != null && this.canTalk()) {
         for(SpawnFortSiege spawn2 : FortSiegeManager.getInstance().getCommanderSpawnList(this.getFort().getId())) {
            if (spawn2.getId() == spawn.getId()) {
               NpcStringId npcString = null;
               switch(spawn2.getId()) {
                  case 1:
                     npcString = NpcStringId.ATTACKING_THE_ENEMYS_REINFORCEMENTS_IS_NECESSARY_TIME_TO_DIE;
                     break;
                  case 2:
                     if (attacker instanceof Summon) {
                        attacker = ((Summon)attacker).getOwner();
                     }

                     npcString = NpcStringId.EVERYONE_CONCENTRATE_YOUR_ATTACKS_ON_S1_SHOW_THE_ENEMY_YOUR_RESOLVE;
                     break;
                  case 3:
                     npcString = NpcStringId.SPIRIT_OF_FIRE_UNLEASH_YOUR_POWER_BURN_THE_ENEMY;
               }

               if (npcString != null) {
                  NpcSay ns = new NpcSay(this.getObjectId(), 23, this.getId(), npcString);
                  if (npcString.getParamCount() == 1) {
                     ns.addStringParameter(attacker.getName());
                  }

                  this.broadcastPacket(ns);
                  this.setCanTalk(false);
                  ThreadPoolManager.getInstance().schedule(new FortCommanderInstance.ScheduleTalkTask(), 10000L);
               }
            }
         }
      }

      super.addDamage(attacker, damage, skill);
   }

   void setCanTalk(boolean val) {
      this._canTalk = val;
   }

   private boolean canTalk() {
      return this._canTalk;
   }

   @Override
   public boolean hasRandomAnimation() {
      return false;
   }

   private class ScheduleTalkTask implements Runnable {
      public ScheduleTalkTask() {
      }

      @Override
      public void run() {
         FortCommanderInstance.this.setCanTalk(true);
      }
   }
}
