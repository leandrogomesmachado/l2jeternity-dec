package l2e.gameserver.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.RadarControl;

public final class Radar {
   private final Player _player;
   private final List<Radar.RadarMarker> _markers = new CopyOnWriteArrayList<>();

   public Radar(Player player) {
      this._player = player;
   }

   public void addMarker(int x, int y, int z) {
      Radar.RadarMarker newMarker = new Radar.RadarMarker(x, y, z);
      this._markers.add(newMarker);
      this._player.sendPacket(new RadarControl(2, 2, x, y, z));
      this._player.sendPacket(new RadarControl(0, 1, x, y, z));
   }

   public void removeMarker(int x, int y, int z) {
      Radar.RadarMarker newMarker = new Radar.RadarMarker(x, y, z);
      this._markers.remove(newMarker);
      this._player.sendPacket(new RadarControl(1, 1, x, y, z));
   }

   public void removeAllMarkers() {
      for(Radar.RadarMarker tempMarker : this._markers) {
         this._player.sendPacket(new RadarControl(2, 2, tempMarker._x, tempMarker._y, tempMarker._z));
      }

      this._markers.clear();
   }

   public void loadMarkers() {
      this._player.sendPacket(new RadarControl(2, 2, this._player.getX(), this._player.getY(), this._player.getZ()));

      for(Radar.RadarMarker tempMarker : this._markers) {
         this._player.sendPacket(new RadarControl(0, 1, tempMarker._x, tempMarker._y, tempMarker._z));
      }
   }

   public static class RadarMarker {
      public int _type;
      public int _x;
      public int _y;
      public int _z;

      public RadarMarker(int type, int x, int y, int z) {
         this._type = type;
         this._x = x;
         this._y = y;
         this._z = z;
      }

      public RadarMarker(int x, int y, int z) {
         this._type = 1;
         this._x = x;
         this._y = y;
         this._z = z;
      }

      @Override
      public int hashCode() {
         int prime = 31;
         int result = 1;
         result = 31 * result + this._type;
         result = 31 * result + this._x;
         result = 31 * result + this._y;
         return 31 * result + this._z;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (!(obj instanceof Radar.RadarMarker)) {
            return false;
         } else {
            Radar.RadarMarker other = (Radar.RadarMarker)obj;
            return this._type == other._type && this._x == other._x && this._y == other._y && this._z == other._z;
         }
      }
   }
}
