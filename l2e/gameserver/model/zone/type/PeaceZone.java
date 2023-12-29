package l2e.gameserver.model.zone.type;

import l2e.gameserver.Config;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;

public class PeaceZone extends ZoneType {
   public PeaceZone(int id) {
      super(id);
      if (Config.PEACE_ZONE_MODE != 2) {
         this.addZoneId(ZoneId.PEACE);
      }
   }

   @Override
   protected void onEnter(Creature character) {
      Player player = character.getActingPlayer();
      if (character.isPlayer()) {
         if (player.isCombatFlagEquipped() && TerritoryWarManager.getInstance().isTWInProgress()) {
            TerritoryWarManager.getInstance().dropCombatFlag(player, false, true);
         }

         if (player.getSiegeState() != 0 && Config.PEACE_ZONE_MODE == 1) {
            return;
         }
      }

      if (Config.PEACE_ZONE_MODE != 2 && character.isPlayer()) {
         if (player != null) {
            player.getRecommendation().stopRecBonus();
            if (player.getNevitSystem().isActive()) {
               player.getNevitSystem().stopAdventTask(true);
            }
         }

         if (Config.SPEED_UP_RUN && player != null) {
            player.broadcastUserInfo(true);
         }
      }
   }

   @Override
   protected void onExit(Creature character) {
      if (Config.SPEED_UP_RUN && character.isPlayer()) {
         character.getActingPlayer().broadcastUserInfo(true);
      }
   }
}
