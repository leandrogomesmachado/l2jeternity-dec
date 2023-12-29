package l2e.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.zone.ZoneId;

public final class SiegableHallZone extends ClanHallZone {
   private List<Location> _challengerLocations;

   public SiegableHallZone(int id) {
      super(id);
      this.addZoneId(ZoneId.CLAN_HALL);
   }

   @Override
   public void parseLoc(int x, int y, int z, String type) {
      if (type != null && type.equals("challenger")) {
         if (this._challengerLocations == null) {
            this._challengerLocations = new ArrayList<>();
         }

         this._challengerLocations.add(new Location(x, y, z));
      } else {
         super.parseLoc(x, y, z, type);
      }
   }

   public List<Location> getChallengerSpawns() {
      return this._challengerLocations;
   }

   public void banishNonSiegeParticipants() {
      TeleportWhereType type = TeleportWhereType.CLANHALL_BANISH;

      for(Player player : this.getPlayersInside()) {
         if (player != null && player.isInHideoutSiege()) {
            player.teleToLocation(type, true);
         }
      }
   }
}
