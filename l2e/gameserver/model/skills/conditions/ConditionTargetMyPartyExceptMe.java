package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ConditionTargetMyPartyExceptMe extends Condition {
   private final boolean _val;

   public ConditionTargetMyPartyExceptMe(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      boolean isPartyMember = true;
      Player player = env.getPlayer();
      Creature target = env.getTarget();
      if (player == null || target == null || !target.isPlayer()) {
         isPartyMember = false;
      } else if (player == target) {
         player.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
         isPartyMember = false;
      } else if (!player.isInSameParty(target.getActingPlayer())) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
         sm.addSkillName(env.getSkill());
         player.sendPacket(sm);
         isPartyMember = false;
      }

      return this._val == isPartyMember;
   }
}
