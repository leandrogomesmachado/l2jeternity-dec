package l2e.gameserver.handler.actionhandlers.impl;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.handler.actionhandlers.IActionHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.holders.DoorRequestHolder;
import l2e.gameserver.network.serverpackets.ConfirmDlg;

public class DoorAction implements IActionHandler {
   @Override
   public boolean action(Player activeChar, GameObject target, boolean interact) {
      if (activeChar.getTarget() != target) {
         activeChar.setTarget(target);
      } else if (interact) {
         DoorInstance door = (DoorInstance)target;
         if (target.isAutoAttackable(activeChar)) {
            if (Math.abs(activeChar.getZ() - target.getZ()) < 400) {
               activeChar.getAI().setIntention(CtrlIntention.ATTACK, target);
            }
         } else if (activeChar.getClan() != null && door.getClanHall() != null && activeChar.getClanId() == door.getClanHall().getOwnerId()) {
            if (!door.isInsideRadius(activeChar, 150, false, false)) {
               activeChar.getAI().setIntention(CtrlIntention.INTERACT, target);
            } else if (!door.getClanHall().isSiegableHall() || !((SiegableHall)door.getClanHall()).isInSiege()) {
               activeChar.addScript(new DoorRequestHolder(door));
               if (!door.getOpen()) {
                  activeChar.sendPacket(new ConfirmDlg(1140));
               } else {
                  activeChar.sendPacket(new ConfirmDlg(1141));
               }
            }
         } else if (activeChar.getClan() != null
            && ((DoorInstance)target).getFort() != null
            && activeChar.getClan() == ((DoorInstance)target).getFort().getOwnerClan()
            && ((DoorInstance)target).isOpenableBySkill()
            && !((DoorInstance)target).getFort().getSiege().getIsInProgress()) {
            if (!((Creature)target).isInsideRadius(activeChar, 150, false, false)) {
               activeChar.getAI().setIntention(CtrlIntention.INTERACT, target);
            } else {
               activeChar.addScript(new DoorRequestHolder((DoorInstance)target));
               if (!((DoorInstance)target).getOpen()) {
                  activeChar.sendPacket(new ConfirmDlg(1140));
               } else {
                  activeChar.sendPacket(new ConfirmDlg(1141));
               }
            }
         }
      }

      return true;
   }

   @Override
   public GameObject.InstanceType getInstanceType() {
      return GameObject.InstanceType.DoorInstance;
   }
}
