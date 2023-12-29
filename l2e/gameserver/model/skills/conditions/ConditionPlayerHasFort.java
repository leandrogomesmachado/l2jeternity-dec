package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.Clan;
import l2e.gameserver.model.stats.Env;

public final class ConditionPlayerHasFort extends Condition {
   private final int _fort;

   public ConditionPlayerHasFort(int fort) {
      this._fort = fort;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() == null) {
         return false;
      } else {
         Clan clan = env.getPlayer().getClan();
         if (clan == null) {
            return this._fort == 0;
         } else if (this._fort == -1) {
            return clan.getFortId() > 0;
         } else {
            return clan.getFortId() == this._fort;
         }
      }
   }
}
