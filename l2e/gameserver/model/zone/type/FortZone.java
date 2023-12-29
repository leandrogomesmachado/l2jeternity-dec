package l2e.gameserver.model.zone.type;

import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneRespawn;

public class FortZone extends ZoneRespawn {
   private int _fortId;

   public FortZone(int id) {
      super(id);
      this.addZoneId(ZoneId.FORT);
   }

   @Override
   public void setParameter(String name, String value) {
      if (name.equals("fortId")) {
         this._fortId = Integer.parseInt(value);
      } else {
         super.setParameter(name, value);
      }
   }

   @Override
   protected void onEnter(Creature character) {
   }

   @Override
   protected void onExit(Creature character) {
   }

   public void updateZoneStatusForCharactersInside() {
   }

   public void banishForeigners(int owningClanId) {
      TeleportWhereType type = TeleportWhereType.FORTRESS_BANISH;

      for(Player temp : this.getPlayersInside()) {
         if (temp.getClanId() != owningClanId || owningClanId == 0) {
            temp.teleToLocation(type, true);
         }
      }
   }

   public int getFortId() {
      return this._fortId;
   }
}
