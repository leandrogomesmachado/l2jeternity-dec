package l2e.gameserver.model.skills.conditions;

import java.util.ArrayList;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.stats.Env;

public class ConditionPlayerInstanceId extends Condition {
   private final ArrayList<Integer> _instanceIds;

   public ConditionPlayerInstanceId(ArrayList<Integer> instanceIds) {
      this._instanceIds = instanceIds;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() == null) {
         return false;
      } else {
         int instanceId = env.getCharacter().getReflectionId();
         if (instanceId <= 0) {
            return false;
         } else {
            ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(env.getPlayer());
            return world != null && world.getReflectionId() == instanceId ? this._instanceIds.contains(world.getTemplateId()) : false;
         }
      }
   }
}
