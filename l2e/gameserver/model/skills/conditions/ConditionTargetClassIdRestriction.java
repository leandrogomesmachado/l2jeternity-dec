package l2e.gameserver.model.skills.conditions;

import java.util.ArrayList;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.stats.Env;

public class ConditionTargetClassIdRestriction extends Condition {
   private final ArrayList<Integer> _classIds;

   public ConditionTargetClassIdRestriction(ArrayList<Integer> classId) {
      this._classIds = classId;
   }

   @Override
   public boolean testImpl(Env env) {
      return !env.getTarget().isPlayer() ? false : this._classIds.contains(((Player)env.getTarget()).getClassId().getId());
   }
}
