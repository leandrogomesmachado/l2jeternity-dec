package l2e.gameserver.model.zone.type;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;

public class WaterZone extends ZoneType {
   public WaterZone(int id) {
      super(id);
      this.addZoneId(ZoneId.WATER);
   }

   @Override
   protected void onEnter(Creature character) {
      if (character.isPlayer()) {
         Player player = character.getActingPlayer();
         if (player.isTransformed() && !player.getTransformation().canSwim()) {
            character.stopTransformation(true);
         } else {
            player.broadcastUserInfo(true);
         }
      } else if (character.isNpc()) {
         character.broadcastInfo();
      }
   }

   @Override
   protected void onExit(Creature character) {
      if (character.isPlayer()) {
         character.getActingPlayer().broadcastUserInfo(true);
      } else if (character.isNpc()) {
         character.broadcastInfo();
      }
   }

   public int getWaterMinZ() {
      return this.getZone().getLowZ();
   }

   public int getWaterZ() {
      return this.getZone().getHighZ();
   }

   public boolean canUseWaterTask() {
      return Math.abs(this.getWaterMinZ() - this.getWaterZ()) > 100;
   }
}
