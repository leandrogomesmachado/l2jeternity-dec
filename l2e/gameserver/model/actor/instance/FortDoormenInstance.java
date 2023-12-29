package l2e.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class FortDoormenInstance extends DoormenInstance {
   public FortDoormenInstance(int objectID, NpcTemplate template) {
      super(objectID, template);
      this.setInstanceType(GameObject.InstanceType.FortDoormenInstance);
   }

   @Override
   public void showChatWindow(Player player) {
      player.sendActionFailed();
      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      if (!this.isOwnerClan(player)) {
         html.setFile(player, player.getLang(), "data/html/doormen/" + this.getTemplate().getId() + "-no.htm");
      } else if (this.isUnderSiege()) {
         html.setFile(player, player.getLang(), "data/html/doormen/" + this.getTemplate().getId() + "-busy.htm");
      } else {
         html.setFile(player, player.getLang(), "data/html/doormen/" + this.getTemplate().getId() + ".htm");
      }

      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      player.sendPacket(html);
   }

   @Override
   protected final void openDoors(Player player, String command) {
      StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
      st.nextToken();

      while(st.hasMoreTokens()) {
         this.getFort().openDoor(player, Integer.parseInt(st.nextToken()));
      }
   }

   @Override
   protected final void closeDoors(Player player, String command) {
      StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
      st.nextToken();

      while(st.hasMoreTokens()) {
         this.getFort().closeDoor(player, Integer.parseInt(st.nextToken()));
      }
   }

   @Override
   protected final boolean isOwnerClan(Player player) {
      return player.getClan() != null
         && this.getFort() != null
         && this.getFort().getOwnerClan() != null
         && player.getClanId() == this.getFort().getOwnerClan().getId();
   }

   @Override
   protected final boolean isUnderSiege() {
      return this.getFort().getZone().isActive();
   }
}
