package l2e.gameserver.geodata.utils;

import java.awt.Color;
import l2e.gameserver.Config;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExServerPrimitive;

public class GeodataUtils {
   public static final byte EAST = 1;
   public static final byte WEST = 2;
   public static final byte SOUTH = 4;
   public static final byte NORTH = 8;
   public static final byte NSWE_ALL = 15;
   public static final byte NSWE_NONE = 0;

   public static void debug2DLine(Player player, int x, int y, int tx, int ty, int z) {
      int gx = GeoEngine.getGeoX(x);
      int gy = GeoEngine.getGeoY(y);
      int tgx = GeoEngine.getGeoX(tx);
      int tgy = GeoEngine.getGeoY(ty);
      ExServerPrimitive prim = new ExServerPrimitive("Debug2DLine", x, y, z);
      prim.addLine(Color.BLUE, GeoEngine.getWorldX(gx), GeoEngine.getWorldY(gy), z, GeoEngine.getWorldX(tgx), GeoEngine.getWorldY(tgy), z);
      LinePointIterator iter = new LinePointIterator(gx, gy, tgx, tgy);

      while(iter.next()) {
         int wx = GeoEngine.getWorldX(iter.x());
         int wy = GeoEngine.getWorldY(iter.y());
         prim.addPoint(Color.RED, wx, wy, z);
      }

      player.sendPacket(prim);
   }

   public static void debug3DLine(Player player, int x, int y, int z, int tx, int ty, int tz) {
      int gx = GeoEngine.getGeoX(x);
      int gy = GeoEngine.getGeoY(y);
      int tgx = GeoEngine.getGeoX(tx);
      int tgy = GeoEngine.getGeoY(ty);
      ExServerPrimitive prim = new ExServerPrimitive("Debug3DLine", x, y, z);
      prim.addLine(Color.BLUE, GeoEngine.getWorldX(gx), GeoEngine.getWorldY(gy), z, GeoEngine.getWorldX(tgx), GeoEngine.getWorldY(tgy), tz);
      LinePointIterator3D iter = new LinePointIterator3D(gx, gy, z, tgx, tgy, tz);
      iter.next();
      int prevX = iter.x();
      int prevY = iter.y();
      int wx = GeoEngine.getWorldX(prevX);
      int wy = GeoEngine.getWorldY(prevY);
      int wz = iter.z();
      prim.addPoint(Color.RED, wx, wy, wz);

      while(iter.next()) {
         int curX = iter.x();
         int curY = iter.y();
         if (curX != prevX || curY != prevY) {
            wx = GeoEngine.getWorldX(curX);
            wy = GeoEngine.getWorldY(curY);
            wz = iter.z();
            prim.addPoint(Color.RED, wx, wy, wz);
            prevX = curX;
            prevY = curY;
         }
      }

      player.sendPacket(prim);
   }

   private static Color getDirectionColor(int x, int y, int z, int geoIndex, byte NSWE) {
      return (GeoEngine.getNSWE(x, y, z, geoIndex) & NSWE) != 0 ? Color.GREEN : Color.RED;
   }

   public static void debugGrid(Player player, int geoRadius) {
      if (geoRadius < 0) {
         throw new IllegalArgumentException("geoRadius < 0");
      } else {
         int blocksPerPacket = 10;
         int iBlock = 10;
         int iPacket = 0;
         ExServerPrimitive exsp = null;
         Location playerGeoLoc = player.getLocation().clone().world2geo();

         for(int dx = -geoRadius; dx <= geoRadius; ++dx) {
            for(int dy = -geoRadius; dy <= geoRadius; ++dy) {
               if (iBlock >= 10) {
                  iBlock = 0;
                  if (exsp != null) {
                     ++iPacket;
                     player.sendPacket(exsp);
                  }

                  exsp = new ExServerPrimitive("DebugGrid_" + iPacket, player.getX(), player.getY(), -16000);
               }

               if (exsp == null) {
                  throw new IllegalStateException();
               }

               int gx = playerGeoLoc.getX() + dx;
               int gy = playerGeoLoc.getY() + dy;
               int geoIndex = player.getGeoIndex();
               Location worldLoc = new Location(gx, gy, playerGeoLoc.getZ() + Config.MIN_LAYER_HEIGHT).geo2world();
               int x = worldLoc.getX();
               int y = worldLoc.getY();
               int z = GeoEngine.getHeight(worldLoc, geoIndex);
               Color col = getDirectionColor(x, y, z, geoIndex, (byte)8);
               exsp.addLine("", col, true, x - 1, y - 7, z, x + 1, y - 7, z);
               exsp.addLine("N", col, true, x - 2, y - 6, z, x + 2, y - 6, z);
               exsp.addLine("", col, true, x - 3, y - 5, z, x + 3, y - 5, z);
               exsp.addLine("", col, true, x - 4, y - 4, z, x + 4, y - 4, z);
               col = getDirectionColor(x, y, z, geoIndex, (byte)1);
               exsp.addLine("", col, true, x + 7, y - 1, z, x + 7, y + 1, z);
               exsp.addLine("E", col, true, x + 6, y - 2, z, x + 6, y + 2, z);
               exsp.addLine("", col, true, x + 5, y - 3, z, x + 5, y + 3, z);
               exsp.addLine("", col, true, x + 4, y - 4, z, x + 4, y + 4, z);
               col = getDirectionColor(x, y, z, geoIndex, (byte)4);
               exsp.addLine("", col, true, x - 1, y + 7, z, x + 1, y + 7, z);
               exsp.addLine("S", col, true, x - 2, y + 6, z, x + 2, y + 6, z);
               exsp.addLine("", col, true, x - 3, y + 5, z, x + 3, y + 5, z);
               exsp.addLine("", col, true, x - 4, y + 4, z, x + 4, y + 4, z);
               col = getDirectionColor(x, y, z, geoIndex, (byte)2);
               exsp.addLine("", col, true, x - 7, y - 1, z, x - 7, y + 1, z);
               exsp.addLine("W", col, true, x - 6, y - 2, z, x - 6, y + 2, z);
               exsp.addLine("", col, true, x - 5, y - 3, z, x - 5, y + 3, z);
               exsp.addLine("", col, true, x - 4, y - 4, z, x - 4, y + 4, z);
               ++iBlock;
            }
         }

         player.sendPacket(exsp);
      }
   }
}
