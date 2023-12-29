package l2e.gameserver.model.actor.instance;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ai.character.CharacterAI;
import l2e.gameserver.ai.guard.FortGuardAI;
import l2e.gameserver.ai.guard.GuardAI;
import l2e.gameserver.ai.guard.SpecialGuardAI;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.reward.RewardList;
import l2e.gameserver.model.reward.RewardType;

public class DefenderInstance extends Attackable {
   private Castle _castle = null;
   private Fort _fort = null;
   private SiegableHall _hall = null;

   public DefenderInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.DefenderInstance);
      this.setIsSiegeGuard(true);
   }

   @Override
   public CharacterAI initAI() {
      if (this.getConquerableHall() == null && this.getCastle(10000L) == null) {
         return new FortGuardAI(this);
      } else {
         return (CharacterAI)(this.getCastle(10000L) != null ? new GuardAI(this) : new SpecialGuardAI(this));
      }
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      if (!(attacker instanceof Playable)) {
         return false;
      } else {
         Player player = attacker.getActingPlayer();
         if (this._fort != null && this._fort.getZone().isActive()
            || this._castle != null && this._castle.getZone().isActive()
            || this._hall != null && this._hall.getSiegeZone().isActive()) {
            int activeSiegeId = this._fort != null
               ? this._fort.getId()
               : (this._castle != null ? this._castle.getId() : (this._hall != null ? this._hall.getId() : 0));
            if (player != null
               && (
                  player.getSiegeState() == 2 && !player.isRegisteredOnThisSiegeField(activeSiegeId)
                     || player.getSiegeState() == 1 && !TerritoryWarManager.getInstance().isAllyField(player, activeSiegeId)
                     || player.getSiegeState() == 0
               )) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   protected void onDeath(Creature killer) {
      if (killer != null && killer.isPlayer()) {
         killer.getActingPlayer().getCounters().addAchivementInfo("siegeGuardKiller", this.getId(), -1L, false, false, false);
      }

      if (killer != null && (killer.isSummon() || killer.isPlayer()) && Config.EPAULETTE_ONLY_FOR_REG) {
         Player player = killer.isSummon() ? ((Summon)killer).getOwner() : killer.getActingPlayer();
         if (player == null) {
            super.onDeath(killer);
            return;
         }

         int activeSiegeId = this._fort != null
            ? this._fort.getId()
            : (this._castle != null ? this._castle.getId() : (this._hall != null ? this._hall.getId() : 0));
         if (TerritoryWarManager.getInstance().isTWInProgress()
               && player.getSiegeState() == 1
               && !TerritoryWarManager.getInstance().isAllyField(player, activeSiegeId)
            || player.getClan() != null
               && (
                  this._fort != null && this._fort.getId() > 0 && this._fort.getSiege().checkIsAttacker(player.getClan())
                     || this._castle != null && this._castle.getId() > 0 && this._castle.getSiege().checkIsAttacker(player.getClan())
               )) {
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
   public boolean hasRandomAnimation() {
      return false;
   }

   @Override
   public void returnHome() {
      if (!(this.getWalkSpeed() <= 0.0)) {
         if (this.getSpawn() != null) {
            if (!this.isInsideRadius(this.getSpawn().getX(), this.getSpawn().getY(), 40, false)) {
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
      }
   }

   @Override
   public void onSpawn() {
      super.onSpawn();
      this._fort = FortManager.getInstance().getFort(this.getX(), this.getY(), this.getZ());
      this._castle = CastleManager.getInstance().getCastle(this.getX(), this.getY(), this.getZ());
      this._hall = this.getConquerableHall();
      if (this._fort == null && this._castle == null && this._hall == null) {
         _log.warning(
            "DefenderInstance spawned outside of Fortress, Castle or Siegable hall Zone! NpcId: "
               + this.getId()
               + " x="
               + this.getX()
               + " y="
               + this.getY()
               + " z="
               + this.getZ()
         );
      }
   }

   @Override
   public void onAction(Player player, boolean interact) {
      if (!this.canTarget(player)) {
         player.sendActionFailed();
      } else {
         if (this != player.getTarget()) {
            if (Config.DEBUG) {
               _log.info("new target selected:" + this.getObjectId());
            }

            player.setTarget(this);
         } else if (interact) {
            if (this.isAutoAttackable(player) && !this.isAlikeDead() && Math.abs(player.getZ() - this.getZ()) < 600) {
               player.getAI().setIntention(CtrlIntention.ATTACK, this);
            }

            if (!this.isAutoAttackable(player) && !this.canInteract(player)) {
               player.getAI().setIntention(CtrlIntention.INTERACT, this);
            }
         }

         player.sendActionFailed();
      }
   }

   @Override
   public void addDamageHate(Creature attacker, int damage, int aggro) {
      if (attacker != null) {
         if (!(attacker instanceof DefenderInstance)) {
            if (damage == 0 && aggro <= 1 && attacker instanceof Playable) {
               Player player = attacker.getActingPlayer();
               if (this._fort != null && this._fort.getZone().isActive()
                  || this._castle != null && this._castle.getZone().isActive()
                  || this._hall != null && this._hall.getSiegeZone().isActive()) {
                  int activeSiegeId = this._fort != null
                     ? this._fort.getId()
                     : (this._castle != null ? this._castle.getId() : (this._hall != null ? this._hall.getId() : 0));
                  if (player != null
                     && (
                        player.getSiegeState() == 2 && player.isRegisteredOnThisSiegeField(activeSiegeId)
                           || player.getSiegeState() == 1 && TerritoryWarManager.getInstance().isAllyField(player, activeSiegeId)
                     )) {
                     return;
                  }
               }
            }

            super.addDamageHate(attacker, damage, aggro);
         }
      }
   }
}
