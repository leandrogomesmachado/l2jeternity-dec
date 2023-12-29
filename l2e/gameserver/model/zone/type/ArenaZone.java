package l2e.gameserver.model.zone.type;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.SystemMessageId;

public class ArenaZone extends ZoneType {
   public ArenaZone(int id) {
      super(id);
      this.addZoneId(ZoneId.PVP);
   }

   @Override
   protected void onEnter(Creature character) {
      if (character.isPlayer() && !character.isInsideZone(ZoneId.PVP, this)) {
         character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
      }
   }

   @Override
   protected void onExit(Creature character) {
      if (character.isPlayer() && !character.isInsideZone(ZoneId.PVP, this)) {
         character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
      }
   }
}
