package l2e.gameserver.model.actor.instance;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class TerritoryWardInstance extends Attackable {
   public TerritoryWardInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.disableCoreAI(true);
   }

   @Override
   public boolean isAutoAttackable(Creature attacker) {
      if (this.isInvul()) {
         return false;
      } else if (this.getCastle() != null && this.getCastle().getZone().isActive()) {
         Player actingPlayer = attacker.getActingPlayer();
         if (actingPlayer == null) {
            return false;
         } else if (actingPlayer.getSiegeSide() == 0) {
            return false;
         } else {
            return !TerritoryWarManager.getInstance().isAllyField(actingPlayer, this.getCastle().getId());
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean hasRandomAnimation() {
      return false;
   }

   @Override
   public void onSpawn() {
      super.onSpawn();
      if (this.getCastle() == null) {
         _log.warning("TerritoryWardInstance(" + this.getName() + ") spawned outside Castle Zone!");
      }
   }

   @Override
   public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, Skill skill) {
      if (skill == null && TerritoryWarManager.getInstance().isTWInProgress()) {
         Player actingPlayer = attacker.getActingPlayer();
         if (actingPlayer != null) {
            if (!actingPlayer.isCombatFlagEquipped()) {
               if (actingPlayer.getSiegeSide() != 0) {
                  if (this.getCastle() != null) {
                     if (!TerritoryWarManager.getInstance().isAllyField(actingPlayer, this.getCastle().getId())) {
                        super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void reduceCurrentHpByDOT(double i, Creature attacker, Skill skill) {
   }

   @Override
   protected void onDeath(Creature killer) {
      if (this.getCastle() != null && TerritoryWarManager.getInstance().isTWInProgress()) {
         if (killer.isPlayer()) {
            if (((Player)killer).getSiegeSide() > 0 && !((Player)killer).isCombatFlagEquipped()) {
               ((Player)killer).addItem("Pickup", this.getId() - 23012, 1L, null, false);
            } else {
               TerritoryWarManager.getInstance().getTerritoryWard(this.getId() - 36491).spawnMe();
            }

            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_S1_WARD_HAS_BEEN_DESTROYED_C2_HAS_THE_WARD);
            sm.addString(this.getName().replaceAll(" Ward", ""));
            sm.addPcName((Player)killer);
            TerritoryWarManager.getInstance().announceToParticipants(sm, 0, 0);
         } else {
            TerritoryWarManager.getInstance().getTerritoryWard(this.getId() - 36491).spawnMe();
         }

         this.decayMe();
         super.onDeath(killer);
      } else {
         super.onDeath(killer);
      }
   }

   @Override
   public void onForcedAttack(Player player) {
      this.onAction(player);
   }

   @Override
   public void onAction(Player player, boolean interact) {
      if (player != null && this.canTarget(player)) {
         if (this != player.getTarget()) {
            player.setTarget(this);
         } else if (interact) {
            if (this.isAutoAttackable(player) && Math.abs(player.getZ() - this.getZ()) < 100) {
               player.getAI().setIntention(CtrlIntention.ATTACK, this);
            } else {
               player.sendActionFailed();
            }
         }
      }
   }
}
