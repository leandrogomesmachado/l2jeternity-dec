package l2e.gameserver.model.entity.underground_coliseum;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;

public class UCPoint {
   private final Location _loc;
   private final List<DoorInstance> _doors;
   private final List<Player> _players = new ArrayList<>();

   public UCPoint(List<DoorInstance> doors, Location loc) {
      this._doors = doors;
      this._loc = loc;
   }

   public void teleportPlayer(Player player) {
      if (player != null) {
         player.setSaveLoc(player.getLocation());
         if (player.isDead()) {
            UCTeam.resPlayer(player);
         }

         Location pos = Location.findPointToStay(this._loc, 350, player.getGeoIndex(), true);
         player.teleToLocation(pos, true);
         this._players.add(player);
      }
   }

   public void actionDoors(boolean open) {
      if (!this._doors.isEmpty()) {
         for(DoorInstance door : this._doors) {
            if (open) {
               door.openMe();
            } else {
               door.closeMe();
            }
         }
      }
   }

   public Location getLocation() {
      return this._loc;
   }

   public List<Player> getPlayers() {
      return this._players;
   }

   public boolean checkPlayer(Player player) {
      if (this._players.contains(player)) {
         this.actionDoors(true);

         for(Player pl : this._players) {
            if (pl != null) {
               pl.setUCState(2);
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
