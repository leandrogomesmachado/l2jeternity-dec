package l2e.gameserver.model.actor.instance;

import l2e.gameserver.ai.character.CharacterAI;
import l2e.gameserver.ai.npc.Minions;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;

public class ControllableMobInstance extends MonsterInstance {
   private boolean _isInvul;

   @Override
   public boolean isAggressive() {
      return true;
   }

   @Override
   public int getAggroRange() {
      return 500;
   }

   public ControllableMobInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.ControllableMobInstance);
   }

   @Override
   public CharacterAI initAI() {
      return new Minions(this);
   }

   @Override
   public void detachAI() {
   }

   @Override
   public boolean isInvul() {
      return this._isInvul;
   }

   public void setInvul(boolean isInvul) {
      this._isInvul = isInvul;
   }

   @Override
   protected void onDeath(Creature killer) {
      this.setAI(null);
      super.onDeath(killer);
   }
}
