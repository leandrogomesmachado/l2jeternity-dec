package l2e.gameserver.model;

import java.awt.Polygon;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.spawn.Spawner;

public final class DimensionalRiftRoom {
   private final byte _type;
   private final byte _room;
   private final int _xMin;
   private final int _xMax;
   private final int _yMin;
   private final int _yMax;
   private final int _zMin;
   private final int _zMax;
   private final int[] _teleportCoords;
   private final Shape _s;
   private final boolean _isBossRoom;
   private final List<Spawner> _roomSpawns = new ArrayList<>();
   protected final List<Npc> _roomMobs = new ArrayList<>();
   private boolean _partyInside = false;

   public DimensionalRiftRoom(byte type, byte room, int xMin, int xMax, int yMin, int yMax, int zMin, int zMax, int xT, int yT, int zT, boolean isBossRoom) {
      this._type = type;
      this._room = room;
      this._xMin = xMin + 128;
      this._xMax = xMax - 128;
      this._yMin = yMin + 128;
      this._yMax = yMax - 128;
      this._zMin = zMin;
      this._zMax = zMax;
      this._teleportCoords = new int[]{xT, yT, zT};
      this._isBossRoom = isBossRoom;
      this._s = new Polygon(new int[]{xMin, xMax, xMax, xMin}, new int[]{yMin, yMin, yMax, yMax}, 4);
   }

   public byte getType() {
      return this._type;
   }

   public byte getRoom() {
      return this._room;
   }

   public int getRandomX() {
      return Rnd.get(this._xMin, this._xMax);
   }

   public int getRandomY() {
      return Rnd.get(this._yMin, this._yMax);
   }

   public int[] getTeleportCoorinates() {
      return this._teleportCoords;
   }

   public boolean checkIfInZone(int x, int y, int z) {
      return this._s.contains((double)x, (double)y) && z >= this._zMin && z <= this._zMax;
   }

   public boolean isBossRoom() {
      return this._isBossRoom;
   }

   public List<Spawner> getSpawns() {
      return this._roomSpawns;
   }

   public void spawn() {
      for(Spawner spawn : this._roomSpawns) {
         spawn.doSpawn();
         spawn.startRespawn();
      }
   }

   public DimensionalRiftRoom unspawn() {
      for(Spawner spawn : this._roomSpawns) {
         spawn.stopRespawn();
         if (spawn.getLastSpawn() != null) {
            spawn.getLastSpawn().deleteMe();
         }
      }

      return this;
   }

   public boolean isPartyInside() {
      return this._partyInside;
   }

   public void setPartyInside(boolean partyInside) {
      this._partyInside = partyInside;
   }
}
