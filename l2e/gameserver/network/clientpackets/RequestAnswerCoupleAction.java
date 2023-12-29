package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExRotation;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class RequestAnswerCoupleAction extends GameClientPacket {
   private int _charObjId;
   private int _actionId;
   private int _answer;

   @Override
   protected void readImpl() {
      this._actionId = this.readD();
      this._answer = this.readD();
      this._charObjId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getActiveChar();
      if (activeChar != null) {
         activeChar.isntAfk();
         Player target = World.getInstance().getPlayer(this._charObjId);
         if (target != null) {
            if (target.getMultiSocialTarget() == activeChar.getObjectId() && target.getMultiSociaAction() == this._actionId) {
               if (this._answer == 0) {
                  target.sendPacket(SystemMessageId.COUPLE_ACTION_DENIED);
               } else if (this._answer == 1) {
                  int distance = (int)Math.sqrt(activeChar.getPlanDistanceSq(target));
                  if (distance > 125 || distance < 15 || activeChar.getObjectId() == target.getObjectId()) {
                     this.sendPacket(SystemMessageId.TARGET_DO_NOT_MEET_LOC_REQUIREMENTS);
                     target.sendPacket(SystemMessageId.TARGET_DO_NOT_MEET_LOC_REQUIREMENTS);
                     return;
                  }

                  int heading = Util.calculateHeadingFrom(activeChar, target);
                  activeChar.broadcastPacket(new ExRotation(activeChar.getObjectId(), heading));
                  activeChar.setHeading(heading);
                  heading = Util.calculateHeadingFrom(target, activeChar);
                  target.setHeading(heading);
                  target.broadcastPacket(new ExRotation(target.getObjectId(), heading));
                  activeChar.broadcastPacket(new l2e.gameserver.network.serverpackets.SocialAction(activeChar.getObjectId(), this._actionId));
                  target.broadcastPacket(new l2e.gameserver.network.serverpackets.SocialAction(this._charObjId, this._actionId));
               } else if (this._answer == -1) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_SET_TO_REFUSE_COUPLE_ACTIONS);
                  sm.addPcName(activeChar);
                  target.sendPacket(sm);
               }

               target.setMultiSocialAction(0, 0);
            }
         }
      }
   }
}
