package l2e.gameserver.model.actor.instance;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public final class ObservationInstance extends Npc {
   public ObservationInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.ObservationInstance);
   }

   @Override
   public void showChatWindow(Player player, int val) {
      String filename = null;
      if (!this.isInsideRadius(-79884, 86529, 50, true)
         && !this.isInsideRadius(-78858, 111358, 50, true)
         && !this.isInsideRadius(-76973, 87136, 50, true)
         && !this.isInsideRadius(-75850, 111968, 50, true)) {
         if (val == 0) {
            filename = "data/html/observation/" + this.getId() + ".htm";
         } else {
            filename = "data/html/observation/" + this.getId() + "-" + val + ".htm";
         }
      } else if (val == 0) {
         filename = "data/html/observation/" + this.getId() + "-Oracle.htm";
      } else {
         filename = "data/html/observation/" + this.getId() + "-Oracle-" + val + ".htm";
      }

      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      html.setFile(player, player.getLang(), filename);
      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      player.sendPacket(html);
   }
}
