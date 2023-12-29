package l2e.gameserver.model.skills.conditions;

import java.util.ArrayList;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.stats.Env;

public class ConditionTargetNpcId extends Condition {
   private final ArrayList<Integer> _npcIds;

   public ConditionTargetNpcId(ArrayList<Integer> npcIds) {
      this._npcIds = npcIds;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getTarget() != null && env.getTarget().isNpc()) {
         return this._npcIds.contains(((Npc)env.getTarget()).getId());
      } else {
         return env.getTarget() != null && env.getTarget().isDoor() ? this._npcIds.contains(((DoorInstance)env.getTarget()).getDoorId()) : false;
      }
   }
}
