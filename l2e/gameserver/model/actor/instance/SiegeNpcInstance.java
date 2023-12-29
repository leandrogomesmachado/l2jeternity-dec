package l2e.gameserver.model.actor.instance;

import l2e.commons.util.Util;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class SiegeNpcInstance extends NpcInstance {
   public SiegeNpcInstance(int objectID, NpcTemplate template) {
      super(objectID, template);
      this.setInstanceType(GameObject.InstanceType.SiegeNpcInstance);
   }

   @Override
   public void showChatWindow(Player player) {
      this.showSiegeInfoWindow(player);
   }

   public void showSiegeInfoWindow(Player player) {
      if (this.validateCondition(player)) {
         SiegableHall hall = this.getConquerableHall();
         if (hall != null) {
            hall.showSiegeInfo(player);
         } else {
            this.getCastle().getSiege().listRegisterClan(player);
         }
      } else {
         NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
         html.setFile(player, player.getLang(), "data/html/siege/" + this.getId() + "-busy.htm");
         html.replace(
            "%castlename%", this.getConquerableHall() != null ? Util.clanHallName(player, this.getConquerableHall().getId()) : this.getCastle().getName()
         );
         html.replace("%objectId%", String.valueOf(this.getObjectId()));
         player.sendPacket(html);
         player.sendActionFailed();
      }
   }

   private boolean validateCondition(Player player) {
      if (this.getConquerableHall() != null && this.getConquerableHall().isInSiege()) {
         return false;
      } else {
         return !this.getCastle().getSiege().getIsInProgress();
      }
   }
}
