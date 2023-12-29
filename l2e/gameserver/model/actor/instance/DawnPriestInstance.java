package l2e.gameserver.model.actor.instance;

import l2e.gameserver.SevenSigns;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class DawnPriestInstance extends SignsPriestInstance {
   public DawnPriestInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.DawnPriestInstance);
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      if (command.startsWith("Chat")) {
         this.showChatWindow(player);
      } else {
         super.onBypassFeedback(player, command);
      }
   }

   @Override
   public void showChatWindow(Player player) {
      player.sendActionFailed();
      String filename = "data/html/seven_signs/";
      int sealGnosisOwner = SevenSigns.getInstance().getSealOwner(2);
      int playerCabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
      boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
      boolean isCompResultsPeriod = SevenSigns.getInstance().isCompResultsPeriod();
      int recruitPeriod = SevenSigns.getInstance().getCurrentPeriod();
      int compWinner = SevenSigns.getInstance().getCabalHighestScore();
      switch(playerCabal) {
         case 1:
            if (isSealValidationPeriod) {
               filename = filename + "dawn_priest_3a.htm";
            } else {
               filename = filename + "dawn_priest_3b.htm";
            }
            break;
         case 2:
            if (isCompResultsPeriod) {
               filename = filename + "dawn_priest_5.htm";
            } else if (recruitPeriod == 0) {
               filename = filename + "dawn_priest_6.htm";
            } else if (isSealValidationPeriod) {
               if (compWinner == 2) {
                  if (compWinner != sealGnosisOwner) {
                     filename = filename + "dawn_priest_2c.htm";
                  } else {
                     filename = filename + "dawn_priest_2a.htm";
                  }
               } else if (compWinner == 0) {
                  filename = filename + "dawn_priest_2d.htm";
               } else {
                  filename = filename + "dawn_priest_2b.htm";
               }
            } else {
               filename = filename + "dawn_priest_1b.htm";
            }
            break;
         default:
            if (isCompResultsPeriod) {
               filename = filename + "dawn_priest_5.htm";
            } else if (recruitPeriod == 0) {
               filename = filename + "dawn_priest_6.htm";
            } else if (isSealValidationPeriod) {
               if (compWinner == 2) {
                  filename = filename + "dawn_priest_4.htm";
               } else if (compWinner == 0) {
                  filename = filename + "dawn_priest_2d.htm";
               } else {
                  filename = filename + "dawn_priest_2b.htm";
               }
            } else {
               filename = filename + "dawn_priest_1a.htm";
            }
      }

      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      html.setFile(player, player.getLang(), filename);
      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      player.sendPacket(html);
   }
}
