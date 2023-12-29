package l2e.gameserver.handler.targethandlers.impl;

import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;

public class FlagPole implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      if (!activeChar.isPlayer()) {
         return EMPTY_TARGET_LIST;
      } else {
         Player player = activeChar.getActingPlayer();
         Fort fort = FortManager.getInstance().getFort(player);
         return player.getClan() != null && fort != null && player.checkIfOkToCastFlagDisplay(fort, true, skill, activeChar.getTarget())
            ? new GameObject[]{activeChar.getTarget()}
            : EMPTY_TARGET_LIST;
      }
   }

   @Override
   public Enum<TargetType> getTargetType() {
      return TargetType.FLAGPOLE;
   }
}
