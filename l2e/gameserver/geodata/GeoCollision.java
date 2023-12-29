package l2e.gameserver.geodata;

import l2e.commons.geometry.Shape;

public interface GeoCollision {
   Shape getShape();

   byte[][] getGeoAround();

   void setGeoAround(byte[][] var1);

   boolean isConcrete();
}
