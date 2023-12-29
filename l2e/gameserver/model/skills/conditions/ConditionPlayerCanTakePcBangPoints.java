package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ConditionPlayerCanTakePcBangPoints extends Condition {
   private final boolean _val;

   public ConditionPlayerCanTakePcBangPoints(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      boolean canTakePoints = true;
      Player player = env.getPlayer();
      if (player == null) {
         canTakePoints = false;
      } else if (player.getPcBangPoints() >= Config.MAX_PC_BANG_POINTS) {
         player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_MAXMIMUM_ACCUMULATION_ALLOWED_OF_PC_CAFE_POINTS_HAS_BEEN_EXCEEDED));
         canTakePoints = false;
      }

      return this._val == canTakePoints;
   }
}
