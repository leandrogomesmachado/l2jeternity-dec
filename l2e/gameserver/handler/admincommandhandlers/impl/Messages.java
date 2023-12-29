package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.commons.util.Util;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Messages implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_msg", "admin_msgx"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_msg ")) {
         try {
            activeChar.sendPacket(SystemMessage.getSystemMessage(Integer.parseInt(command.substring(10).trim())));
            return true;
         } catch (Exception var12) {
            activeChar.sendMessage("Command format: //msg <SYSTEM_MSG_ID>");
         }
      } else if (command.startsWith("admin_msgx ")) {
         String[] tokens = command.split(" ");
         if (tokens.length <= 2 || !Util.isDigit(tokens[1])) {
            activeChar.sendMessage("Command format: //msgx <SYSTEM_MSG_ID> [item:Id] [skill:Id] [npc:Id] [zone:x,y,x] [castle:Id] [str:'text']");
            return false;
         }

         SystemMessage sm = SystemMessage.getSystemMessage(Integer.parseInt(tokens[1]));
         int lastPos = 0;

         for(int i = 2; i < tokens.length; ++i) {
            try {
               String val = tokens[i];
               if (val.startsWith("item:")) {
                  sm.addItemName(Integer.parseInt(val.substring(5)));
               } else if (val.startsWith("skill:")) {
                  sm.addSkillName(Integer.parseInt(val.substring(6)));
               } else if (val.startsWith("npc:")) {
                  sm.addNpcName(Integer.parseInt(val.substring(4)));
               } else if (val.startsWith("zone:")) {
                  int x = Integer.parseInt(val.substring(5, val.indexOf(",")));
                  int y = Integer.parseInt(val.substring(val.indexOf(",") + 1, val.lastIndexOf(",")));
                  int z = Integer.parseInt(val.substring(val.lastIndexOf(",") + 1, val.length()));
                  sm.addZoneName(x, y, z);
               } else if (val.startsWith("castle:")) {
                  sm.addCastleId(Integer.parseInt(val.substring(7)));
               } else if (val.startsWith("str:")) {
                  int pos = command.indexOf("'", lastPos + 1);
                  lastPos = command.indexOf("'", pos + 1);
                  sm.addString(command.substring(pos + 1, lastPos));
               }
            } catch (Exception var11) {
               activeChar.sendMessage("Exception: " + var11.getMessage());
            }
         }

         activeChar.sendPacket(sm);
      }

      return false;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
