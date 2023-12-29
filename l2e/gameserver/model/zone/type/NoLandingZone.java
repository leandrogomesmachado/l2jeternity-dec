package l2e.gameserver.model.zone.type;

import l2e.gameserver.model.MountType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.SystemMessageId;

public class NoLandingZone extends ZoneType {
   private int dismountDelay = 5;

   public NoLandingZone(int id) {
      super(id);
      this.addZoneId(ZoneId.NO_LANDING);
   }

   @Override
   public void setParameter(String name, String value) {
      if (name.equals("dismountDelay")) {
         this.dismountDelay = Integer.parseInt(value);
      } else {
         super.setParameter(name, value);
      }
   }

   @Override
   protected void onEnter(Creature character) {
      if (character.isPlayer() && character.getActingPlayer().getMountType() == MountType.WYVERN) {
         character.sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);
         character.getActingPlayer().enteredNoLanding(this.dismountDelay);
      }
   }

   @Override
   protected void onExit(Creature character) {
      if (character.isPlayer() && character.getActingPlayer().getMountType() == MountType.WYVERN) {
         character.getActingPlayer().exitedNoLanding();
      }
   }
}
