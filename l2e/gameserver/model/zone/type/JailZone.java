package l2e.gameserver.model.zone.type;

import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.tasks.player.TeleportTask;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.SystemMessageId;

public class JailZone extends ZoneType {
   private static final Location JAIL_IN_LOC = new Location(-114356, -249645, -2984);
   private static final Location JAIL_OUT_LOC = new Location(17836, 170178, -3507);

   public JailZone(int id) {
      super(id);
      this.addZoneId(ZoneId.JAIL);
      this.addZoneId(ZoneId.NO_SUMMON_FRIEND);
      if (Config.JAIL_IS_PVP) {
         this.addZoneId(ZoneId.PVP);
      }
   }

   @Override
   protected void onEnter(Creature character) {
      if (character.isPlayer() && Config.JAIL_IS_PVP) {
         character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
      }
   }

   @Override
   protected void onExit(Creature character) {
      if (character.isPlayer()) {
         Player player = character.getActingPlayer();
         if (Config.JAIL_IS_PVP) {
            character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
         }

         if (player.isJailed()) {
            ThreadPoolManager.getInstance().schedule(new TeleportTask(player, JAIL_IN_LOC), 2000L);
            character.sendMessage("You cannot cheat your way out of here. You must wait until your jail time is over.");
         }
      }
   }

   public static Location getLocationIn() {
      return JAIL_IN_LOC;
   }

   public static Location getLocationOut() {
      return JAIL_OUT_LOC;
   }
}
