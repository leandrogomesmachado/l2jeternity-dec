package l2e.gameserver.model.actor.instance;

import l2e.commons.util.Evolve;
import l2e.gameserver.Config;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class PetManagerInstance extends MerchantInstance {
   public PetManagerInstance(int objectID, NpcTemplate template) {
      super(objectID, template);
      this.setInstanceType(GameObject.InstanceType.PetManagerInstance);
   }

   @Override
   public String getHtmlPath(int npcId, int val) {
      String pom = "";
      if (val == 0) {
         pom = "" + npcId;
      } else {
         pom = npcId + "-" + val;
      }

      return "data/html/petmanager/" + pom + ".htm";
   }

   @Override
   public void showChatWindow(Player player) {
      String filename = "data/html/petmanager/" + this.getId() + ".htm";
      if (this.getId() == 36478 && player.hasSummon()) {
         filename = "data/html/petmanager/restore-unsummonpet.htm";
      }

      NpcHtmlMessage html = new NpcHtmlMessage(1);
      html.setFile(player, player.getLang(), filename);
      if (Config.ALLOW_RENTPET && Config.LIST_PET_RENT_NPC.contains(this.getId())) {
         html.replace("_Quest", "_RentPet\">Rent Pet</a><br><a action=\"bypass -h npc_%objectId%_Quest");
      }

      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      html.replace("%npcname%", this.getName());
      player.sendPacket(html);
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      if (command.startsWith("exchange")) {
         String[] params = command.split(" ");
         int val = Integer.parseInt(params[1]);
         switch(val) {
            case 1:
               this.exchange(player, 7585, 6650);
               break;
            case 2:
               this.exchange(player, 7583, 6648);
               break;
            case 3:
               this.exchange(player, 7584, 6649);
         }
      } else if (command.startsWith("evolve")) {
         String[] params = command.split(" ");
         int val = Integer.parseInt(params[1]);
         boolean ok = false;
         switch(val) {
            case 1:
               ok = Evolve.doEvolve(player, this, 2375, 9882, 55);
               break;
            case 2:
               ok = Evolve.doEvolve(player, this, 9882, 10426, 70);
               break;
            case 3:
               ok = Evolve.doEvolve(player, this, 6648, 10311, 55);
               break;
            case 4:
               ok = Evolve.doEvolve(player, this, 6650, 10313, 55);
               break;
            case 5:
               ok = Evolve.doEvolve(player, this, 6649, 10312, 55);
         }

         if (!ok) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile(player, player.getLang(), "data/html/petmanager/evolve_no.htm");
            player.sendPacket(html);
         }
      } else if (command.startsWith("restore")) {
         String[] params = command.split(" ");
         int val = Integer.parseInt(params[1]);
         boolean ok = false;
         switch(val) {
            case 1:
               ok = Evolve.doRestore(player, this, 10307, 9882, 55);
               break;
            case 2:
               ok = Evolve.doRestore(player, this, 10611, 10426, 70);
               break;
            case 3:
               ok = Evolve.doRestore(player, this, 10308, 4422, 55);
               break;
            case 4:
               ok = Evolve.doRestore(player, this, 10309, 4423, 55);
               break;
            case 5:
               ok = Evolve.doRestore(player, this, 10310, 4424, 55);
         }

         if (!ok) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile(player, player.getLang(), "data/html/petmanager/restore_no.htm");
            player.sendPacket(html);
         }
      } else {
         super.onBypassFeedback(player, command);
      }
   }

   public final void exchange(Player player, int itemIdtake, int itemIdgive) {
      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      if (player.destroyItemByItemId("Consume", itemIdtake, 1L, this, true)) {
         player.addItem("", itemIdgive, 1L, this, true);
         html.setFile(player, player.getLang(), "data/html/petmanager/" + this.getId() + ".htm");
         player.sendPacket(html);
      } else {
         html.setFile(player, player.getLang(), "data/html/petmanager/exchange_no.htm");
         player.sendPacket(html);
      }
   }
}
