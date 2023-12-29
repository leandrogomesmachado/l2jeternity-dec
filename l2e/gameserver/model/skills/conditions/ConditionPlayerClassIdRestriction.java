package l2e.gameserver.model.skills.conditions;

import java.util.List;
import l2e.gameserver.model.stats.Env;

public class ConditionPlayerClassIdRestriction extends Condition {
   private final List<Integer> _classIds;

   public ConditionPlayerClassIdRestriction(List<Integer> classId) {
      this._classIds = classId;
   }

   @Override
   public boolean testImpl(Env env) {
      return env.getPlayer() != null
         && (
            env.getPlayer().isSubClassActive()
               ? this._classIds.contains(env.getPlayer().getActiveClass())
               : this._classIds.contains(env.getPlayer().getClassId().getId())
         );
   }
}
