package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestNewVoteSystem extends GameClientPacket {
   private int _targetId;

   @Override
   protected void readImpl() {
      this._targetId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         GameObject object = activeChar.getTarget();
         if (!(object instanceof Player)) {
            if (object == null) {
               activeChar.sendPacket(SystemMessageId.SELECT_TARGET);
            } else {
               activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
            }
         } else {
            Player target = (Player)object;
            if (target.getObjectId() == this._targetId) {
               if (target == activeChar) {
                  activeChar.sendPacket(SystemMessageId.YOU_CANNOT_RECOMMEND_YOURSELF);
               } else if (activeChar.getRecommendation().getRecomLeft() <= 0) {
                  activeChar.sendPacket(SystemMessageId.YOU_CURRENTLY_DO_NOT_HAVE_ANY_RECOMMENDATIONS);
               } else if (target.getRecommendation().getRecomHave() >= 255) {
                  activeChar.sendPacket(SystemMessageId.YOUR_TARGET_NO_LONGER_RECEIVE_A_RECOMMENDATION);
               } else {
                  activeChar.getRecommendation().giveRecom(target);
                  SystemMessage sm = null;
                  sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_RECOMMENDED_C1_YOU_HAVE_S2_RECOMMENDATIONS_LEFT);
                  sm.addPcName(target);
                  sm.addNumber(activeChar.getRecommendation().getRecomLeft());
                  activeChar.sendPacket(sm);
                  sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_BEEN_RECOMMENDED_BY_C1);
                  sm.addPcName(activeChar);
                  target.sendPacket(sm);
                  sm = null;
                  activeChar.sendUserInfo(true);
                  target.broadcastCharInfo();
                  activeChar.sendVoteSystemInfo();
                  target.sendVoteSystemInfo();
               }
            }
         }
      }
   }
}
