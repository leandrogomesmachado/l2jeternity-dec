package l2e.gameserver.model.zone.type;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.ZoneRespawn;

public class ResidenceTeleportZone extends ZoneRespawn {
   private int _residenceId;

   public ResidenceTeleportZone(int id) {
      super(id);
      this.addZoneId(ZoneId.NO_SUMMON_FRIEND);
   }

   @Override
   public void setParameter(String name, String value) {
      if (name.equals("residenceId")) {
         this._residenceId = Integer.parseInt(value);
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

   public void oustAllPlayers() {
      for(Player player : this.getPlayersInside()) {
         if (player != null && player.isOnline()) {
            player.teleToLocation(this.getSpawnLoc(), 200, true);
         }
      }
   }

   public int getResidenceId() {
      return this._residenceId;
   }
}
