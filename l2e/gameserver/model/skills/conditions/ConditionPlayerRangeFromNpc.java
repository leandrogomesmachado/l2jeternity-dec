package l2e.gameserver.model.skills.conditions;

import l2e.commons.util.Util;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.stats.Env;

public class ConditionPlayerRangeFromNpc extends Condition {
   private final int[] _npcIds;
   private final int _radius;
   private final boolean _val;

   public ConditionPlayerRangeFromNpc(int[] npcIds, int radius, boolean val) {
      this._npcIds = npcIds;
      this._radius = radius;
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      boolean existNpc = false;
      if (this._npcIds != null && this._npcIds.length > 0 && this._radius > 0) {
         for(Npc target : World.getInstance().getAroundNpc(env.getCharacter(), this._radius, 200)) {
            if (Util.contains(this._npcIds, target.getId())) {
               existNpc = true;
               break;
            }
         }
      }

      return existNpc == this._val;
   }
}
