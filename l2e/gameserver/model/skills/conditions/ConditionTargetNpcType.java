package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.stats.Env;

public class ConditionTargetNpcType extends Condition {
   private final GameObject.InstanceType[] _npcType;

   public ConditionTargetNpcType(GameObject.InstanceType[] type) {
      this._npcType = type;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getTarget() == null ? false : env.getTarget().getInstanceType().isTypes(this._npcType);
   }
}
