package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class ConditionPlayerCanTransform extends Condition {
   private final boolean _val;

   public ConditionPlayerCanTransform(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      boolean canTransform = true;
      Player player = env.getPlayer();
      if (player == null || player.isAlikeDead() || player.isCursedWeaponEquipped()) {
         canTransform = false;
      } else if (player.isSitting()) {
         player.sendPacket(SystemMessageId.CANNOT_TRANSFORM_WHILE_SITTING);
         canTransform = false;
      } else if (player.isTransformed() || player.isInStance()) {
         player.sendPacket(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
         canTransform = false;
      } else if (player.isInWater()) {
         player.sendPacket(SystemMessageId.YOU_CANNOT_POLYMORPH_INTO_THE_DESIRED_FORM_IN_WATER);
         canTransform = false;
      } else if (player.isFlyingMounted() || player.isMounted()) {
         player.sendPacket(SystemMessageId.YOU_CANNOT_POLYMORPH_WHILE_RIDING_A_PET);
         canTransform = false;
      }

      return this._val == canTransform;
   }
}
