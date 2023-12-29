package l2e.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import l2e.gameserver.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.data.parser.TeleLocationParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.TeleportTemplate;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class DungeonGatekeeperInstance extends Npc {
   public DungeonGatekeeperInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.DungeonGatekeeperInstance);
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      player.sendActionFailed();
      StringTokenizer st = new StringTokenizer(command, " ");
      String actualCommand = st.nextToken();
      String filename = "data/html/seven_signs/";
      int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(1);
      int sealGnosisOwner = SevenSigns.getInstance().getSealOwner(2);
      int playerCabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
      boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
      int compWinner = SevenSigns.getInstance().getCabalHighestScore();
      if (actualCommand.startsWith("necro")) {
         boolean canPort = true;
         if (!Config.ALLOW_UNLIM_ENTER_CATACOMBS) {
            if (!isSealValidationPeriod) {
               if (playerCabal == 0) {
                  canPort = false;
               }
            } else if (compWinner != 2 || playerCabal == 2 && sealAvariceOwner == 2) {
               if (compWinner != 1 || playerCabal == 1 && sealAvariceOwner == 1) {
                  if (compWinner == 0 && playerCabal != 0) {
                     canPort = true;
                  } else if (playerCabal == 0) {
                     canPort = false;
                  }
               } else {
                  player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
                  canPort = false;
               }
            } else {
               player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
               canPort = false;
            }
         }

         if (!canPort) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            filename = filename + "necro_no.htm";
            html.setFile(player, player.getLang(), filename);
            player.sendPacket(html);
         } else {
            this.doTeleport(player, Integer.parseInt(st.nextToken()));
            player.setIsIn7sDungeon(true);
         }
      } else if (actualCommand.startsWith("cata")) {
         boolean canPort = true;
         if (!Config.ALLOW_UNLIM_ENTER_CATACOMBS) {
            if (!isSealValidationPeriod) {
               if (playerCabal == 0) {
                  canPort = false;
               }
            } else if (compWinner != 2 || playerCabal == 2 && sealGnosisOwner == 2) {
               if (compWinner != 1 || playerCabal == 1 && sealGnosisOwner == 1) {
                  if (compWinner == 0 && playerCabal != 0) {
                     canPort = true;
                  } else if (playerCabal == 0) {
                     canPort = false;
                  }
               } else {
                  player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
                  canPort = false;
               }
            } else {
               player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
               canPort = false;
            }
         }

         if (!canPort) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            filename = filename + "cata_no.htm";
            html.setFile(player, player.getLang(), filename);
            player.sendPacket(html);
         } else {
            this.doTeleport(player, Integer.parseInt(st.nextToken()));
            player.setIsIn7sDungeon(true);
         }
      } else if (actualCommand.startsWith("exit")) {
         this.doTeleport(player, Integer.parseInt(st.nextToken()));
         player.setIsIn7sDungeon(false);
      } else if (actualCommand.startsWith("goto")) {
         this.doTeleport(player, Integer.parseInt(st.nextToken()));
      } else {
         super.onBypassFeedback(player, command);
      }
   }

   private void doTeleport(Player player, int val) {
      TeleportTemplate list = TeleLocationParser.getInstance().getTemplate(val);
      if (list != null) {
         if (player.isAlikeDead()) {
            return;
         }

         player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
      } else {
         _log.warning("No teleport destination with id:" + val);
      }

      player.sendActionFailed();
   }

   @Override
   public String getHtmlPath(int npcId, int val) {
      String pom = "";
      if (val == 0) {
         pom = "" + npcId;
      } else {
         pom = npcId + "-" + val;
      }

      return "data/html/teleporter/" + pom + ".htm";
   }
}
