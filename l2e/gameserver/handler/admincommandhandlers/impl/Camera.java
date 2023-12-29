package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.SystemMessageId;

public class Camera implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_cam", "admin_camex", "admin_cam3"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (activeChar.getTarget() != null && activeChar.getTarget().isCreature()) {
         Creature target = (Creature)activeChar.getTarget();
         String[] com = command.split(" ");
         String var5 = com[0];
         switch(var5) {
            case "admin_cam":
               if (com.length != 12) {
                  activeChar.sendMessage("Usage: //cam force angle1 angle2 time range duration relYaw relPitch isWide relAngle");
                  return false;
               }

               Quest.specialCamera(
                  activeChar,
                  target,
                  Integer.parseInt(com[1]),
                  Integer.parseInt(com[2]),
                  Integer.parseInt(com[3]),
                  Integer.parseInt(com[4]),
                  Integer.parseInt(com[5]),
                  Integer.parseInt(com[6]),
                  Integer.parseInt(com[7]),
                  Integer.parseInt(com[8]),
                  Integer.parseInt(com[9]),
                  Integer.parseInt(com[10])
               );
               break;
            case "admin_camex":
               if (com.length != 10) {
                  activeChar.sendMessage("Usage: //camex force angle1 angle2 time duration relYaw relPitch isWide relAngle");
                  return false;
               }

               Quest.specialCameraEx(
                  activeChar,
                  target,
                  Integer.parseInt(com[1]),
                  Integer.parseInt(com[2]),
                  Integer.parseInt(com[3]),
                  Integer.parseInt(com[4]),
                  Integer.parseInt(com[5]),
                  Integer.parseInt(com[6]),
                  Integer.parseInt(com[7]),
                  Integer.parseInt(com[8]),
                  Integer.parseInt(com[9])
               );
               break;
            case "admin_cam3":
               if (com.length != 12) {
                  activeChar.sendMessage("Usage: //cam3 force angle1 angle2 time range duration relYaw relPitch isWide relAngle unk");
                  return false;
               }

               Quest.specialCamera3(
                  activeChar,
                  target,
                  Integer.parseInt(com[1]),
                  Integer.parseInt(com[2]),
                  Integer.parseInt(com[3]),
                  Integer.parseInt(com[4]),
                  Integer.parseInt(com[5]),
                  Integer.parseInt(com[6]),
                  Integer.parseInt(com[7]),
                  Integer.parseInt(com[8]),
                  Integer.parseInt(com[9]),
                  Integer.parseInt(com[10]),
                  Integer.parseInt(com[11])
               );
         }

         return true;
      } else {
         activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
         return false;
      }
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
