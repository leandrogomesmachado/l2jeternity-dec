package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.underground_coliseum.UCTeam;
import l2e.gameserver.model.skills.Skill;

public class UCTowerInstance extends Npc {
   private UCTeam _team;

   public UCTowerInstance(UCTeam team, int objectId, NpcTemplate template) {
      super(objectId, template);
      this._team = team;
   }

   @Override
   public boolean canBeAttacked() {
      return true;
   }

   @Override
   public boolean isAutoAttackable(Creature creature) {
      return true;
   }

   @Override
   public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, Skill skill) {
      if (attacker.getTeam() != this.getTeam()) {
         if (damage < this.getStatus().getCurrentHp()) {
            this.getStatus().setCurrentHp(this.getStatus().getCurrentHp() - damage);
         } else {
            this.doDie(attacker);
         }
      }
   }

   @Override
   protected void onDeath(Creature killer) {
      if (this._team != null) {
         this._team.deleteTower();
         this._team = null;
      }

      super.onDeath(killer);
   }

   @Override
   public int getTeam() {
      return this._team.getIndex() + 1;
   }
}
