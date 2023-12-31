package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.data.holder.SpawnHolder;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.RaidBossSpawnManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.spawn.Spawner;

public class Delete implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_delete"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.equals("admin_delete")) {
         this.handleDelete(activeChar);
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void handleDelete(Player activeChar) {
      GameObject obj = activeChar.getTarget();
      if (obj instanceof Npc) {
         Npc target = (Npc)obj;
         Spawner spawn = target.getSpawn();
         if (spawn != null) {
            spawn.stopRespawn();
            if (RaidBossSpawnManager.getInstance().isDefined(spawn.getId())) {
               RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
            } else {
               SpawnHolder.getInstance().deleteSpawn(spawn, true);
            }
         }

         target.deleteMe();
         activeChar.sendMessage("Deleted " + target.getName() + " from " + target.getObjectId() + ".");
      } else {
         activeChar.sendMessage("Incorrect target.");
      }
   }
}
