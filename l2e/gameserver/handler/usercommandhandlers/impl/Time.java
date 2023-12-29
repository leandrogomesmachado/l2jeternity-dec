package l2e.gameserver.handler.usercommandhandlers.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import l2e.gameserver.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.handler.usercommandhandlers.IUserCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Time implements IUserCommandHandler {
   private static final int[] COMMAND_IDS = new int[]{77};
   private static final SimpleDateFormat fmt = new SimpleDateFormat("H:mm.");

   @Override
   public boolean useUserCommand(int id, Player activeChar) {
      if (COMMAND_IDS[0] != id) {
         return false;
      } else {
         int t = GameTimeController.getInstance().getGameTime();
         String h = "" + t / 60 % 24;
         String m;
         if (t % 60 < 10) {
            m = "0" + t % 60;
         } else {
            m = "" + t % 60;
         }

         SystemMessage sm;
         if (GameTimeController.getInstance().isNight()) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.TIME_S1_S2_IN_THE_NIGHT);
            sm.addString(h);
            sm.addString(m);
         } else {
            sm = SystemMessage.getSystemMessage(SystemMessageId.TIME_S1_S2_IN_THE_DAY);
            sm.addString(h);
            sm.addString(m);
         }

         activeChar.sendPacket(sm);
         if (Config.DISPLAY_SERVER_TIME) {
            activeChar.sendMessage("Server time is " + fmt.format(new Date(System.currentTimeMillis())));
         }

         return true;
      }
   }

   @Override
   public int[] getUserCommandList() {
      return COMMAND_IDS;
   }
}
