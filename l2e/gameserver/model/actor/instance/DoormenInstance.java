package l2e.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.data.parser.TeleLocationParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.TeleportTemplate;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class DoormenInstance extends NpcInstance {
   public DoormenInstance(int objectID, NpcTemplate template) {
      super(objectID, template);
      this.setInstanceType(GameObject.InstanceType.DoormenInstance);
   }

   @Override
   public void onBypassFeedback(Player player, String command) {
      if (command.startsWith("Chat")) {
         this.showChatWindow(player);
      } else if (command.startsWith("open_doors")) {
         if (this.isOwnerClan(player)) {
            if (this.isUnderSiege()) {
               this.cannotManageDoors(player);
            } else {
               this.openDoors(player, command);
            }
         }
      } else if (command.startsWith("close_doors")) {
         if (this.isOwnerClan(player)) {
            if (this.isUnderSiege()) {
               this.cannotManageDoors(player);
            } else {
               this.closeDoors(player, command);
            }
         }
      } else if (command.startsWith("tele")) {
         if (this.isOwnerClan(player)) {
            this.doTeleport(player, command);
         }
      } else {
         super.onBypassFeedback(player, command);
      }
   }

   @Override
   public void showChatWindow(Player player) {
      player.sendActionFailed();
      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      if (!this.isOwnerClan(player)) {
         html.setFile(player, player.getLang(), "data/html/doormen/" + this.getTemplate().getId() + "-no.htm");
      } else {
         html.setFile(player, player.getLang(), "data/html/doormen/" + this.getTemplate().getId() + ".htm");
      }

      html.replace("%objectId%", String.valueOf(this.getObjectId()));
      player.sendPacket(html);
   }

   protected void openDoors(Player player, String command) {
      StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
      st.nextToken();

      while(st.hasMoreTokens()) {
         DoorParser.getInstance().getDoor(Integer.parseInt(st.nextToken())).openMe();
      }
   }

   protected void closeDoors(Player player, String command) {
      StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
      st.nextToken();

      while(st.hasMoreTokens()) {
         DoorParser.getInstance().getDoor(Integer.parseInt(st.nextToken())).closeMe();
      }
   }

   protected void cannotManageDoors(Player player) {
      player.sendActionFailed();
      NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
      html.setFile(player, player.getLang(), "data/html/doormen/" + this.getTemplate().getId() + "-busy.htm");
      player.sendPacket(html);
   }

   protected void doTeleport(Player player, String command) {
      int whereTo = Integer.parseInt(command.substring(5).trim());
      TeleportTemplate list = TeleLocationParser.getInstance().getTemplate(whereTo);
      if (list != null) {
         if (!player.isAlikeDead()) {
            player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), false);
         }
      } else {
         _log.warning("No teleport destination with id:" + whereTo);
      }

      player.sendActionFailed();
   }

   protected boolean isOwnerClan(Player player) {
      return true;
   }

   protected boolean isUnderSiege() {
      return false;
   }
}
