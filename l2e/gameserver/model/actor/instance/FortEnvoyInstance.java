package l2e.gameserver.model.actor.instance;

import l2e.commons.util.Util;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class FortEnvoyInstance extends Npc {
   public FortEnvoyInstance(int objectID, NpcTemplate template) {
      super(objectID, template);
      this.setInstanceType(GameObject.InstanceType.FortEnvoyInstance);
   }

   @Override
   public void showChatWindow(Player player) {
      Fort fortress = this.getFort();
      String filePath;
      if (!player.isClanLeader() || fortress.getId() != player.getClan().getFortId()) {
         filePath = "data/html/fortress/ambassador-not-leader.htm";
      } else if (fortress.getFortState() == 1) {
         filePath = "data/html/fortress/ambassador-rejected.htm";
      } else if (fortress.getFortState() == 2) {
         filePath = "data/html/fortress/ambassador-signed.htm";
      } else if (fortress.isBorderFortress()) {
         filePath = "data/html/fortress/ambassador-border.htm";
      } else {
         filePath = "data/html/fortress/ambassador.htm";
      }

      player.sendActionFailed();
      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      html.setFile(player, player.getLang(), filePath);
      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      html.replace("%castleName%", String.valueOf(fortress.getCastleByAmbassador(this.getId()).getName()));
      player.sendPacket(html);
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      if (command.startsWith("select ")) {
         String param = command.substring(7);
         Fort fortress = this.getFort();
         Castle castle = fortress.getCastleByAmbassador(this.getId());
         String filePath;
         if (castle.getOwnerId() == 0) {
            filePath = "data/html/fortress/ambassador-not-owned.htm";
         } else {
            int choice = Util.isDigit(param) ? Integer.parseInt(param) : 0;
            fortress.setFortState(choice, castle.getId());
            filePath = choice == 1 ? "data/html/fortress/ambassador-independent.htm" : "data/html/fortress/ambassador-signed.htm";
         }

         NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
         html.setFile(player, player.getLang(), filePath);
         html.replace("%castleName%", castle.getName());
         player.sendPacket(html);
      } else {
         super.onBypassFeedback(player, command);
      }
   }
}
