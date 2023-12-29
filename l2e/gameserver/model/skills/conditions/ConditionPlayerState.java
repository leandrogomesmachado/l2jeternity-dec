package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.PlayerState;
import l2e.gameserver.model.stats.Env;

public class ConditionPlayerState extends Condition {
   private final PlayerState _check;
   private final boolean _required;

   public ConditionPlayerState(PlayerState check, boolean required) {
      this._check = check;
      this._required = required;
   }

   @Override
   public boolean testImpl(Env env) {
      Creature character = env.getCharacter();
      Player player = env.getPlayer();
      switch(this._check) {
         case RESTING:
            if (player != null) {
               return player.isSitting() == this._required;
            }

            return !this._required;
         case MOVING:
            return character.isMoving() == this._required;
         case RUNNING:
            return character.isRunning() == this._required;
         case STANDING:
            if (player == null) {
               return this._required != character.isMoving();
            }

            return this._required != (player.isSitting() || player.isMoving());
         case FLYING:
            return character.isFlying() == this._required;
         case BEHIND:
            return character.isBehindTarget() == this._required;
         case FRONT:
            return character.isInFrontOfTarget() == this._required;
         case CHAOTIC:
            if (player != null) {
               return player.getKarma() > 0 == this._required;
            }

            return !this._required;
         case OLYMPIAD:
            if (player != null) {
               return player.isInOlympiadMode() == this._required;
            }

            return !this._required;
         default:
            return !this._required;
      }
   }
}
