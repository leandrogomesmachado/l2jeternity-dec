package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.Clan;
import l2e.gameserver.model.stats.Env;

public final class ConditionPlayerHasCastle extends Condition {
   private final int _castle;

   public ConditionPlayerHasCastle(int castle) {
      this._castle = castle;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() == null) {
         return false;
      } else {
         Clan clan = env.getPlayer().getClan();
         if (clan == null) {
            return this._castle == 0;
         } else if (this._castle == -1) {
            return clan.getCastleId() > 0;
         } else {
            return clan.getCastleId() == this._castle;
         }
      }
   }
}
