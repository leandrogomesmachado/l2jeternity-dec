package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.StringTokenizer;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.entity.Reflection;

public class Instances implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_setinstance", "admin_ghoston", "admin_ghostoff", "admin_createinstance", "admin_destroyinstance", "admin_listinstances"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      StringTokenizer st = new StringTokenizer(command);
      st.nextToken();
      if (command.startsWith("admin_createinstance")) {
         String[] parts = command.split(" ");
         if (parts.length == 3) {
            try {
               int id = Integer.parseInt(parts[1]);
               if (id < 300000 && ReflectionManager.getInstance().createReflectionFromTemplate(id, parts[2])) {
                  activeChar.sendMessage("Instance created.");
               } else {
                  activeChar.sendMessage("Failed to create instance.");
               }

               return true;
            } catch (Exception var8) {
               activeChar.sendMessage("Failed loading: " + parts[1] + " " + parts[2]);
               return false;
            }
         }

         activeChar.sendMessage("Example: //createinstance <id> <templatefile> - ids => 300000 are reserved for dynamic instances");
      } else if (command.startsWith("admin_listinstances")) {
         for(Reflection temp : ReflectionManager.getInstance().getReflections().values()) {
            activeChar.sendMessage("Id: " + temp.getId() + " Name: " + temp.getName());
         }
      } else if (command.startsWith("admin_setinstance")) {
         try {
            int val = Integer.parseInt(st.nextToken());
            if (ReflectionManager.getInstance().getReflection(val) == null) {
               activeChar.sendMessage("Instance " + val + " doesnt exist.");
               return false;
            }

            GameObject target = activeChar.getTarget();
            if (target != null && !(target instanceof Summon)) {
               target.setReflectionId(val);
               if (target instanceof Player) {
                  Player player = (Player)target;
                  player.sendMessage("Admin set your instance to:" + val);
                  player.teleToLocation(player.getX(), player.getY(), player.getZ(), true);
               }

               activeChar.sendMessage("Moved " + target.getName() + " to instance " + target.getReflectionId() + ".");
               return true;
            }

            activeChar.sendMessage("Incorrect target.");
            return false;
         } catch (Exception var9) {
            activeChar.sendMessage("Use //setinstance id");
         }
      } else if (command.startsWith("admin_destroyinstance")) {
         try {
            int val = Integer.parseInt(st.nextToken());
            ReflectionManager.getInstance().destroyReflection(val);
            activeChar.sendMessage("Instance destroyed");
         } catch (Exception var7) {
            activeChar.sendMessage("Use //destroyinstance id");
         }
      } else if (command.startsWith("admin_ghoston")) {
         activeChar.getAppearance().setGhostMode(true);
         activeChar.sendMessage("Ghost mode enabled");
         activeChar.broadcastUserInfo(true);
         activeChar.decayMe();
         activeChar.spawnMe();
      } else if (command.startsWith("admin_ghostoff")) {
         activeChar.getAppearance().setGhostMode(false);
         activeChar.sendMessage("Ghost mode disabled");
         activeChar.broadcastUserInfo(true);
         activeChar.decayMe();
         activeChar.spawnMe();
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
