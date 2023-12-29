package l2e.gameserver.model.skills.conditions;

import java.util.ArrayList;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.stats.Env;

public final class ConditionPlayerHasClanHall extends Condition {
   private final ArrayList<Integer> _clanHall;

   public ConditionPlayerHasClanHall(ArrayList<Integer> clanHall) {
      this._clanHall = clanHall;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() == null) {
         return false;
      } else {
         Clan clan = env.getPlayer().getClan();
         if (clan != null) {
            if (this._clanHall.size() == 1 && this._clanHall.get(0) == -1) {
               return clan.getHideoutId() > 0;
            } else {
               return this._clanHall.contains(clan.getHideoutId());
            }
         } else {
            return this._clanHall.size() == 1 && this._clanHall.get(0) == 0;
         }
      }
   }
}
