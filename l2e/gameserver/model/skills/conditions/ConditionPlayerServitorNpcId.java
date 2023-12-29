package l2e.gameserver.model.skills.conditions;

import java.util.List;
import l2e.gameserver.model.stats.Env;

public class ConditionPlayerServitorNpcId extends Condition {
   private final List<Integer> _npcIds;

   public ConditionPlayerServitorNpcId(List<Integer> npcIds) {
      if (npcIds.size() == 1 && npcIds.get(0) == 0) {
         this._npcIds = null;
      } else {
         this._npcIds = npcIds;
      }
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() != null && env.getPlayer().hasSummon()) {
         return this._npcIds == null || this._npcIds.contains(env.getPlayer().getSummon().getId());
      } else {
         return false;
      }
   }
}
