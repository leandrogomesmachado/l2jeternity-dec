package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class FortSiegeNpcInstance extends Npc {
   public FortSiegeNpcInstance(int objectID, NpcTemplate template) {
      super(objectID, template);
      this.setInstanceType(GameObject.InstanceType.FortSiegeNpcInstance);
   }

   @Override
   public void showChatWindow(Player player, int val) {
      player.sendActionFailed();
      String filename;
      if (val == 0) {
         filename = "data/html/fortress/merchant.htm";
      } else {
         filename = "data/html/fortress/merchant-" + val + ".htm";
      }

      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      html.setFile(player, player.getLang(), filename);
      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      html.replace("%npcId%", String.valueOf(this.getId()));
      if (this.getFort().getOwnerClan() != null) {
         html.replace("%clanname%", this.getFort().getOwnerClan().getName());
      } else {
         html.replace("%clanname%", "NPC");
      }

      player.sendPacket(html);
   }

   @Override
   public boolean hasRandomAnimation() {
      return false;
   }
}
