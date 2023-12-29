package l2e.gameserver.handler.usercommandhandlers.impl;

import java.util.Calendar;
import l2e.gameserver.handler.usercommandhandlers.IUserCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class MyBirthday implements IUserCommandHandler {
   private static final int[] COMMAND_IDS = new int[]{126};

   @Override
   public boolean useUserCommand(int id, Player activeChar) {
      if (id != COMMAND_IDS[0]) {
         return false;
      } else {
         Calendar date = activeChar.getCreateDate();
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_BIRTHDAY_IS_S3_S4_S2);
         sm.addPcName(activeChar);
         sm.addString(Integer.toString(date.get(1)));
         sm.addString(Integer.toString(date.get(2) + 1));
         sm.addString(Integer.toString(date.get(5)));
         activeChar.sendPacket(sm);
         return true;
      }
   }

   @Override
   public int[] getUserCommandList() {
      return COMMAND_IDS;
   }
}
