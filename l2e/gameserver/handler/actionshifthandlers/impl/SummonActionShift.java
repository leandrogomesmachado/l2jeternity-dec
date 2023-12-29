package l2e.gameserver.handler.actionshifthandlers.impl;

import l2e.gameserver.handler.actionhandlers.IActionHandler;
import l2e.gameserver.handler.admincommandhandlers.AdminCommandHandler;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;

public class SummonActionShift implements IActionHandler {
   @Override
   public boolean action(Player activeChar, GameObject target, boolean interact) {
      if (activeChar.isGM()) {
         if (activeChar.getTarget() != target) {
            activeChar.setTarget(target);
         }

         IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler("admin_summon_info");
         if (ach != null) {
            ach.useAdminCommand("admin_summon_info", activeChar);
         }
      }

      return true;
   }

   @Override
   public GameObject.InstanceType getInstanceType() {
      return GameObject.InstanceType.Summon;
   }
}
