package l2e.gameserver.handler.actionhandlers.impl;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.handler.actionhandlers.IActionHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;

public class ArtefactAction implements IActionHandler {
   @Override
   public boolean action(Player activeChar, GameObject target, boolean interact) {
      if (!((Npc)target).canTarget(activeChar)) {
         return false;
      } else {
         if (activeChar.getTarget() != target) {
            activeChar.setTarget(target);
         } else if (interact && !((Npc)target).canInteract(activeChar)) {
            activeChar.getAI().setIntention(CtrlIntention.INTERACT, target);
         }

         return true;
      }
   }

   @Override
   public GameObject.InstanceType getInstanceType() {
      return GameObject.InstanceType.ArtefactInstance;
   }
}
