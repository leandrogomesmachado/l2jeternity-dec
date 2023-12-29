package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ControllableMobInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.taskmanager.DecayTaskManager;

public class Res implements IAdminCommandHandler {
   private static Logger _log = Logger.getLogger(Res.class.getName());
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_res", "admin_res_monster"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_res ")) {
         this.handleRes(activeChar, command.split(" ")[1]);
      } else if (command.equals("admin_res")) {
         this.handleRes(activeChar);
      } else if (command.startsWith("admin_res_monster ")) {
         this.handleNonPlayerRes(activeChar, command.split(" ")[1]);
      } else if (command.equals("admin_res_monster")) {
         this.handleNonPlayerRes(activeChar);
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void handleRes(Player activeChar) {
      this.handleRes(activeChar, null);
   }

   private void handleRes(Player activeChar, String resParam) {
      GameObject obj = activeChar.getTarget();
      if (resParam != null) {
         Player plyr = World.getInstance().getPlayer(resParam);
         if (plyr == null) {
            try {
               int radius = Integer.parseInt(resParam);

               for(Player knownPlayer : World.getInstance().getAroundPlayers(activeChar, radius, 200)) {
                  this.doResurrect(knownPlayer);
               }

               activeChar.sendMessage("Resurrected all players within a " + radius + " unit radius.");
               return;
            } catch (NumberFormatException var8) {
               activeChar.sendMessage("Enter a valid player name or radius.");
               return;
            }
         }

         obj = plyr;
      }

      if (obj == null) {
         obj = activeChar;
      }

      if (obj instanceof ControllableMobInstance) {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      } else {
         this.doResurrect((Creature)obj);
         if (Config.DEBUG) {
            _log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") resurrected character " + obj.getObjectId());
         }
      }
   }

   private void handleNonPlayerRes(Player activeChar) {
      this.handleNonPlayerRes(activeChar, "");
   }

   private void handleNonPlayerRes(Player activeChar, String radiusStr) {
      GameObject obj = activeChar.getTarget();

      try {
         int radius = 0;
         if (!radiusStr.isEmpty()) {
            radius = Integer.parseInt(radiusStr);

            for(Creature knownChar : World.getInstance().getAroundCharacters(activeChar, radius, 200)) {
               if (!(knownChar instanceof Player) && !(knownChar instanceof ControllableMobInstance)) {
                  this.doResurrect(knownChar);
               }
            }

            activeChar.sendMessage("Resurrected all non-players within a " + radius + " unit radius.");
         }
      } catch (NumberFormatException var7) {
         activeChar.sendMessage("Enter a valid radius.");
         return;
      }

      if (!(obj instanceof Player) && !(obj instanceof ControllableMobInstance)) {
         this.doResurrect((Creature)obj);
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      }
   }

   private void doResurrect(Creature targetChar) {
      if (targetChar.isDead()) {
         if (targetChar instanceof Player) {
            ((Player)targetChar).restoreExp(100.0);
         } else {
            DecayTaskManager.getInstance().cancel(targetChar);
         }

         targetChar.doRevive();
      }
   }
}
