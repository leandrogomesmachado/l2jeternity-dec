package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.entity.Castle;

public class DoorControl implements IAdminCommandHandler {
   private static DoorParser _DoorParser = DoorParser.getInstance();
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_open", "admin_close", "admin_openall", "admin_closeall"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      try {
         if (command.startsWith("admin_open ")) {
            int doorId = Integer.parseInt(command.substring(11));
            if (_DoorParser.getDoor(doorId) != null) {
               _DoorParser.getDoor(doorId).openMe();
            } else {
               for(Castle castle : CastleManager.getInstance().getCastles()) {
                  if (castle.getDoor(doorId) != null) {
                     castle.getDoor(doorId).openMe();
                  }
               }
            }
         } else if (command.startsWith("admin_close ")) {
            int doorId = Integer.parseInt(command.substring(12));
            if (_DoorParser.getDoor(doorId) != null) {
               _DoorParser.getDoor(doorId).closeMe();
            } else {
               for(Castle castle : CastleManager.getInstance().getCastles()) {
                  if (castle.getDoor(doorId) != null) {
                     castle.getDoor(doorId).closeMe();
                  }
               }
            }
         }

         if (command.equals("admin_closeall")) {
            for(DoorInstance door : _DoorParser.getDoors()) {
               door.closeMe();
            }

            for(Castle castle : CastleManager.getInstance().getCastles()) {
               for(DoorInstance door : castle.getDoors()) {
                  door.closeMe();
               }
            }
         }

         if (command.equals("admin_openall")) {
            for(DoorInstance door : _DoorParser.getDoors()) {
               door.openMe();
            }

            for(Castle castle : CastleManager.getInstance().getCastles()) {
               for(DoorInstance door : castle.getDoors()) {
                  door.openMe();
               }
            }
         }

         if (command.equals("admin_open")) {
            GameObject target = activeChar.getTarget();
            if (target instanceof DoorInstance) {
               ((DoorInstance)target).openMe();
            } else {
               activeChar.sendMessage("Incorrect target.");
            }
         }

         if (command.equals("admin_close")) {
            GameObject target = activeChar.getTarget();
            if (target instanceof DoorInstance) {
               ((DoorInstance)target).closeMe();
            } else {
               activeChar.sendMessage("Incorrect target.");
            }
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
