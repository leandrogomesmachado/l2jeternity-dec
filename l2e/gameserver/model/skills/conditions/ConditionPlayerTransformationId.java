package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.stats.Env;

public class ConditionPlayerTransformationId extends Condition {
   private final int _id;

   public ConditionPlayerTransformationId(int id) {
      this._id = id;
   }

   @Override
   public boolean testImpl(Env env) {
      Player player = env.getPlayer();
      if (player == null) {
         return false;
      } else if (this._id == -1) {
         return player.isTransformed();
      } else {
         return player.getTransformationId() == this._id;
      }
   }
}
