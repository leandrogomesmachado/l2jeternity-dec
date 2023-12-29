package l2e.gameserver.handler.actionhandlers.impl;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.handler.actionhandlers.IActionHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.PetStatusShow;

public class PetAction implements IActionHandler {
   @Override
   public boolean action(Player activeChar, GameObject target, boolean interact) {
      if (activeChar.isLockedTarget() && activeChar.getLockedTarget() != target) {
         activeChar.sendPacket(SystemMessageId.FAILED_CHANGE_TARGET);
         return false;
      } else {
         boolean isOwner = activeChar.getObjectId() == ((PetInstance)target).getOwner().getObjectId();
         if (isOwner && activeChar != ((PetInstance)target).getOwner()) {
            ((PetInstance)target).updateRefOwner(activeChar);
         }

         if (activeChar.getTarget() != target) {
            activeChar.setTarget(target);
         } else if (interact) {
            if (!target.isAutoAttackable(activeChar) || isOwner) {
               if (!((Creature)target).isInsideRadius(activeChar, 150, false, false)) {
                  if (GeoEngine.canSeeTarget(activeChar, target, false)) {
                     activeChar.getAI().setIntention(CtrlIntention.INTERACT, target);
                     activeChar.onActionRequest();
                  }
               } else {
                  if (isOwner) {
                     activeChar.sendPacket(new PetStatusShow((PetInstance)target));
                  }

                  activeChar.updateNotMoveUntil();
               }
            } else if (GeoEngine.canSeeTarget(activeChar, target, false)) {
               activeChar.getAI().setIntention(CtrlIntention.ATTACK, target);
               activeChar.onActionRequest();
            }
         }

         return true;
      }
   }

   @Override
   public GameObject.InstanceType getInstanceType() {
      return GameObject.InstanceType.PetInstance;
   }
}
