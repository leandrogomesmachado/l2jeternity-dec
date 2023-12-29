package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.data.parser.ReflectionParser;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.player.CharacterVariable;
import l2e.gameserver.model.stats.Env;

public class ConditionPlayerReflectionEntry extends Condition {
   private final int _type;
   private final int _attempts;

   public ConditionPlayerReflectionEntry(int type, int attempts) {
      this._type = type;
      this._attempts = attempts;
   }

   @Override
   public boolean testImpl(Env env) {
      Player player = env.getPlayer();
      if (player == null) {
         return false;
      } else {
         CharacterVariable var = player.getVariable("reflectionEntry_" + this._type + "");
         if (var != null) {
            if (Integer.parseInt(var.getValue()) < this._attempts && !var.isExpired()) {
               return player.getReflectionId() == 0 && this.canResetReflection(player);
            } else {
               return var.isExpired() && player.getReflectionId() == 0 && this.canResetReflection(player);
            }
         } else {
            return player.getReflectionId() == 0 && this.canResetReflection(player);
         }
      }
   }

   private boolean canResetReflection(Player player) {
      for(int i : ReflectionParser.getInstance().getSharedReuseInstanceIdsByGroup(this._type)) {
         if (System.currentTimeMillis() < ReflectionManager.getInstance().getReflectionTime(player.getObjectId(), i)) {
            return true;
         }
      }

      return false;
   }
}
