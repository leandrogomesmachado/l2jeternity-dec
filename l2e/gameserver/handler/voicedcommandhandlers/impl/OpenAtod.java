package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.commons.util.Rnd;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;

public class OpenAtod implements IVoicedCommandHandler {
   private static final String[] _voicedCommands = new String[]{"openatod"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String params) {
      if (command.equalsIgnoreCase("openatod")) {
         if (params == null) {
            activeChar.sendMessage("Usage: .openatod [num]");
         } else {
            int num = 0;

            try {
               num = Integer.parseInt(params);
            } catch (NumberFormatException var10) {
               activeChar.sendMessage("You must enter a number. Usage: .openatod [num]");
               return false;
            }

            if (num == 0) {
               return false;
            }

            if (activeChar.getInventory().getInventoryItemCount(9599, 0) >= (long)num) {
               int a = 0;
               int b = 0;
               int c = 0;

               for(int i = 0; i < num; ++i) {
                  int rnd = Rnd.get(100);
                  if (rnd <= 100 && rnd > 44) {
                     ++a;
                  } else if (rnd <= 44 && rnd > 14) {
                     ++b;
                  } else if (rnd <= 14) {
                     ++c;
                  }
               }

               if (activeChar.destroyItemByItemId("ATOD", 9599, (long)(a + b + c), null, true)) {
                  if (a > 0) {
                     activeChar.addItem("ATOD", 9600, (long)a, null, true);
                  }

                  if (b > 0) {
                     activeChar.addItem("ATOD", 9601, (long)b, null, true);
                  }

                  if (c > 0) {
                     activeChar.addItem("ATOD", 9602, (long)c, null, true);
                  }
               } else {
                  activeChar.sendMessage("You do not have enough tomes.");
               }
            } else {
               activeChar.sendMessage("You do not have enough tomes.");
            }
         }
      }

      return false;
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }
}
