package l2e.gameserver.handler.actionhandlers.impl;

import l2e.gameserver.handler.actionhandlers.IActionHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public class TrapAction implements IActionHandler {
   @Override
   public boolean action(Player activeChar, GameObject target, boolean interact) {
      if (activeChar.isLockedTarget() && activeChar.getLockedTarget() != target) {
         activeChar.sendPacket(SystemMessageId.FAILED_CHANGE_TARGET);
         return false;
      } else {
         activeChar.setTarget(target);
         return true;
      }
   }

   @Override
   public GameObject.InstanceType getInstanceType() {
      return GameObject.InstanceType.TrapInstance;
   }
}
