package l2e.gameserver.handler.actionhandlers.impl;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.handler.actionhandlers.IActionHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.PetStatusShow;

public class SummonAction implements IActionHandler {
   @Override
   public boolean action(Player activeChar, GameObject target, boolean interact) {
      if (activeChar.isLockedTarget() && activeChar.getLockedTarget() != target) {
         activeChar.sendPacket(SystemMessageId.FAILED_CHANGE_TARGET);
         return false;
      } else {
         if (activeChar == ((Summon)target).getOwner() && activeChar.getTarget() == target) {
            activeChar.sendPacket(new PetStatusShow((Summon)target));
            activeChar.updateNotMoveUntil();
            activeChar.sendActionFailed();
         } else if (activeChar.getTarget() != target) {
            activeChar.setTarget(target);
         } else if (interact) {
            if (target.isAutoAttackable(activeChar)) {
               if (GeoEngine.canSeeTarget(activeChar, target, false)) {
                  activeChar.getAI().setIntention(CtrlIntention.ATTACK, target);
                  activeChar.onActionRequest();
               }
            } else {
               activeChar.sendActionFailed();
               if (((Summon)target).isInsideRadius(activeChar, 150, false, false)) {
                  activeChar.updateNotMoveUntil();
               } else if (GeoEngine.canSeeTarget(activeChar, target, false)) {
                  activeChar.getAI().setIntention(CtrlIntention.FOLLOW, target);
               }
            }
         }

         return true;
      }
   }

   @Override
   public GameObject.InstanceType getInstanceType() {
      return GameObject.InstanceType.Summon;
   }
}
