package l2e.gameserver.handler.actionhandlers.impl;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.handler.actionhandlers.IActionHandler;
import l2e.gameserver.instancemanager.MercTicketManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;

public class ItemAction implements IActionHandler {
   @Override
   public boolean action(Player activeChar, GameObject target, boolean interact) {
      int castleId = MercTicketManager.getInstance().getTicketCastleId(((ItemInstance)target).getId());
      if (castleId > 0 && (!activeChar.isCastleLord(castleId) || activeChar.isInParty())) {
         if (activeChar.isInParty()) {
            activeChar.sendMessage("You cannot pickup mercenaries while in a party.");
         } else {
            activeChar.sendMessage("Only the castle lord can pickup mercenaries.");
         }

         activeChar.setTarget(target);
         activeChar.getAI().setIntention(CtrlIntention.IDLE);
      } else if (!activeChar.isFlying()) {
         activeChar.getAI().setIntention(CtrlIntention.PICK_UP, target);
      }

      return true;
   }

   @Override
   public GameObject.InstanceType getInstanceType() {
      return GameObject.InstanceType.ItemInstance;
   }
}
