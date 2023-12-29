package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.Config;
import l2e.gameserver.data.holder.CharSummonHolder;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class ConditionPlayerCanSummon extends Condition {
   private final boolean _val;

   public ConditionPlayerCanSummon(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      Player player = env.getPlayer();
      if (player == null) {
         return false;
      } else {
         boolean canSummon = true;
         if (Config.RESTORE_SERVITOR_ON_RECONNECT && CharSummonHolder.getInstance().getServitors().containsKey(player.getObjectId())) {
            player.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
            canSummon = false;
         } else if (Config.RESTORE_PET_ON_RECONNECT && CharSummonHolder.getInstance().getPets().containsKey(player.getObjectId())) {
            player.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
            canSummon = false;
         } else if (player.hasSummon()) {
            player.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
            canSummon = false;
         } else if (player.isFlyingMounted() || player.isMounted()) {
            canSummon = false;
         }

         return this._val == canSummon;
      }
   }
}
