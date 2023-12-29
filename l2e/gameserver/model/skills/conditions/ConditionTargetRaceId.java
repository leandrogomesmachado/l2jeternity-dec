package l2e.gameserver.model.skills.conditions;

import java.util.ArrayList;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.stats.Env;

public class ConditionTargetRaceId extends Condition {
   private final ArrayList<Integer> _raceIds;

   public ConditionTargetRaceId(ArrayList<Integer> raceId) {
      this._raceIds = raceId;
   }

   @Override
   public boolean testImpl(Env env) {
      return !(env.getTarget() instanceof Npc) ? false : this._raceIds.contains(((Npc)env.getTarget()).getTemplate().getRace().ordinal() + 1);
   }
}
