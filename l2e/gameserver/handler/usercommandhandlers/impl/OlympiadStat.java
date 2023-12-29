package l2e.gameserver.handler.usercommandhandlers.impl;

import l2e.gameserver.handler.usercommandhandlers.IUserCommandHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.olympiad.Olympiad;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class OlympiadStat implements IUserCommandHandler {
   private static final int[] COMMAND_IDS = new int[]{109};

   @Override
   public boolean useUserCommand(int id, Player activeChar) {
      if (id != COMMAND_IDS[0]) {
         return false;
      } else {
         int nobleObjId = activeChar.getObjectId();
         GameObject target = activeChar.getTarget();
         if (target != null) {
            if (!target.isPlayer() || !target.getActingPlayer().isNoble()) {
               activeChar.sendPacket(SystemMessageId.NOBLESSE_ONLY);
               return false;
            }

            nobleObjId = target.getObjectId();
         } else if (!activeChar.isNoble()) {
            activeChar.sendPacket(SystemMessageId.NOBLESSE_ONLY);
            return false;
         }

         SystemMessage sm = SystemMessage.getSystemMessage(
            SystemMessageId.THE_CURRENT_RECORD_FOR_THIS_OLYMPIAD_SESSION_IS_S1_MATCHES_S2_WINS_S3_DEFEATS_YOU_HAVE_EARNED_S4_OLYMPIAD_POINTS
         );
         sm.addNumber(Olympiad.getInstance().getCompetitionDone(nobleObjId));
         sm.addNumber(Olympiad.getInstance().getCompetitionWon(nobleObjId));
         sm.addNumber(Olympiad.getInstance().getCompetitionLost(nobleObjId));
         sm.addNumber(Olympiad.getInstance().getNoblePoints(nobleObjId));
         activeChar.sendPacket(sm);
         SystemMessage sm2 = SystemMessage.getSystemMessage(
            SystemMessageId.YOU_HAVE_S1_MATCHES_REMAINING_THAT_YOU_CAN_PARTECIPATE_IN_THIS_WEEK_S2_CLASSED_S3_NON_CLASSED_S4_TEAM
         );
         sm2.addNumber(Olympiad.getInstance().getRemainingWeeklyMatches(nobleObjId));
         sm2.addNumber(Olympiad.getInstance().getRemainingWeeklyMatchesClassed(nobleObjId));
         sm2.addNumber(Olympiad.getInstance().getRemainingWeeklyMatchesNonClassed(nobleObjId));
         sm2.addNumber(Olympiad.getInstance().getRemainingWeeklyMatchesTeam(nobleObjId));
         activeChar.sendPacket(sm2);
         return true;
      }
   }

   @Override
   public int[] getUserCommandList() {
      return COMMAND_IDS;
   }
}
