package l2e.gameserver.handler.actionhandlers.impl;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.handler.actionhandlers.IActionHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.network.SystemMessageId;

public class PlayerAction implements IActionHandler {
   @Override
   public boolean action(Player activeChar, GameObject target, boolean interact) {
      for(AbstractFightEvent e : activeChar.getFightEvents()) {
         if (e != null && !e.canAction((Creature)target, activeChar)) {
            return false;
         }
      }

      if (!AerialCleftEvent.getInstance().onAction(activeChar, target.getObjectId())) {
         return false;
      } else if (activeChar.isOutOfControl()) {
         return false;
      } else if (activeChar.isLockedTarget() && activeChar.getLockedTarget() != target) {
         activeChar.sendPacket(SystemMessageId.FAILED_CHANGE_TARGET);
         return false;
      } else if (activeChar.getTarget() != target) {
         activeChar.setTarget(target);
         return false;
      } else {
         if (interact) {
            Player player = target.getActingPlayer();
            if (player.getPrivateStoreType() != 0) {
               activeChar.getAI().setIntention(CtrlIntention.INTERACT, player);
            } else if (target.isAutoAttackable(activeChar)) {
               if ((!player.isCursedWeaponEquipped() || activeChar.getLevel() >= 21) && (!activeChar.isCursedWeaponEquipped() || player.getLevel() >= 21)) {
                  if (GeoEngine.canSeeTarget(activeChar, player, player.isFlying())) {
                     activeChar.getAI().setIntention(CtrlIntention.ATTACK, player);
                  } else {
                     activeChar.getAI().setIntention(CtrlIntention.MOVING, player.getLocation());
                  }

                  activeChar.onActionRequest();
               } else {
                  activeChar.sendActionFailed();
               }
            } else {
               activeChar.sendActionFailed();
               if (GeoEngine.canSeeTarget(activeChar, player, player.isFlying())) {
                  activeChar.getAI().setIntention(CtrlIntention.FOLLOW, player);
               } else {
                  activeChar.getAI().setIntention(CtrlIntention.MOVING, player.getLocation());
               }
            }
         }

         return true;
      }
   }

   @Override
   public GameObject.InstanceType getInstanceType() {
      return GameObject.InstanceType.Player;
   }
}
