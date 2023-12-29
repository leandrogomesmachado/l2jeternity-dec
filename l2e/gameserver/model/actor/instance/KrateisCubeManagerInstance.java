package l2e.gameserver.model.actor.instance;

import l2e.gameserver.instancemanager.KrateisCubeManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.network.SystemMessageId;

public class KrateisCubeManagerInstance extends NpcInstance {
   public KrateisCubeManagerInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      if (command.startsWith("Register")) {
         if ((double)player.getInventoryLimit() * 0.8 <= (double)player.getInventory().getSize()) {
            player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
            this.showChatWindow(player, "data/html/krateisCube/32503-9.htm");
            return;
         }

         int cmdChoice = Integer.parseInt(command.substring(9, 10).trim());
         switch(cmdChoice) {
            case 1:
               if (player.getLevel() < 70 || player.getLevel() > 75) {
                  this.showChatWindow(player, "data/html/krateisCube/32503-7.htm");
                  return;
               }
               break;
            case 2:
               if (player.getLevel() < 76 || player.getLevel() > 79) {
                  this.showChatWindow(player, "data/html/krateisCube/32503-7.htm");
                  return;
               }
               break;
            case 3:
               if (player.getLevel() < 80) {
                  this.showChatWindow(player, "data/html/krateisCube/32503-7.htm");
                  return;
               }
            case 4:
               if (player.getLevel() < 70) {
                  this.showChatWindow(player, "data/html/krateisCube/32503-10.htm");
                  return;
               }
         }

         if (!KrateisCubeManager.getInstance().isTimeToRegister()) {
            this.showChatWindow(player, "data/html/krateisCube/32503-8.htm");
            return;
         }

         if (KrateisCubeManager.getInstance().registerPlayer(player)) {
            this.showChatWindow(player, "data/html/krateisCube/32503-4.htm");
         } else {
            this.showChatWindow(player, "data/html/krateisCube/32503-5.htm");
         }
      } else {
         if (command.startsWith("Cancel")) {
            KrateisCubeManager.getInstance().removePlayer(player);
            this.showChatWindow(player, "data/html/krateisCube/32503-6.htm");
            return;
         }

         if (command.startsWith("TeleportToFI")) {
            player.teleToLocation(-59193, -56893, -2034, true);
            Summon pet = player.getSummon();
            if (pet != null) {
               pet.teleToLocation(-59193, -56893, -2034, true);
            }

            return;
         }

         if (command.startsWith("TeleportIn")) {
            KrateisCubeManager.getInstance().teleportPlayerIn(player);
            return;
         }

         super.onBypassFeedback(player, command);
      }
   }

   @Override
   public String getHtmlPath(int npcId, int val) {
      String pom = "";
      if (val == 0) {
         pom = "" + npcId;
      } else {
         pom = npcId + "-" + val;
      }

      return "data/html/krateisCube/" + pom + ".htm";
   }
}
