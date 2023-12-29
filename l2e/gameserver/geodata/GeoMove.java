package l2e.gameserver.geodata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import l2e.gameserver.Config;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExShowTrace;

public class GeoMove {
   public static List<Location> findPath(int x, int y, int z, int tx, int ty, int tz, GameObject obj, boolean showTrace, int geoIndex) {
      if (Math.abs(z - tz) > (obj.isNpc() ? 1024 : 256)) {
         return Collections.emptyList();
      } else {
         z = GeoEngine.getHeight(x, y, z, geoIndex);
         tz = GeoEngine.getHeight(tx, ty, tz, geoIndex);
         PathFind n = new PathFind(x, y, z, tx, ty, tz, obj, geoIndex);
         if (n.getPath() != null && !n.getPath().isEmpty()) {
            List<Location> targetRecorder = new ArrayList<>(n.getPath().size() + 2);
            targetRecorder.add(new Location(x, y, z));

            for(Location p : n.getPath()) {
               targetRecorder.add(p.geo2world());
            }

            targetRecorder.add(new Location(tx, ty, tz));
            if (Config.PATH_CLEAN) {
               pathClean(targetRecorder, geoIndex);
            }

            if (showTrace && obj.isPlayer() && ((Player)obj).getVarB("trace")) {
               Player player = (Player)obj;
               ExShowTrace trace = new ExShowTrace();
               int i = 0;

               for(Location loc : targetRecorder) {
                  ++i;
                  if (i != 1 && i != targetRecorder.size()) {
                     trace.addTrace(loc.getX(), loc.getY(), loc.getZ() + 15, 30000);
                  }
               }

               player.sendPacket(trace);
            }

            if (targetRecorder.size() > 0) {
               targetRecorder.remove(0);
            }

            return targetRecorder;
         } else {
            return Collections.emptyList();
         }
      }
   }

   private static void pathClean(List<Location> path, int geoIndex) {
      int size = path.size();
      if (size > 2) {
         for(int i = 2; i < size; ++i) {
            Location p3 = path.get(i);
            Location p2 = path.get(i - 1);
            Location p1 = path.get(i - 2);
            if (p1.equals(p2) || p3.equals(p2) || IsPointInLine(p1.getX(), p1.getY(), p3.getX(), p3.getY(), p2)) {
               path.remove(i - 1);
               --size;
               i = Math.max(2, i - 2);
            }
         }
      }

      for(int current = 0; current < path.size() - 2; ++current) {
         Location one = path.get(current);

         for(int sub = current + 2; sub < path.size(); ++sub) {
            Location two = path.get(sub);
            if (one.equals(two) || GeoEngine.canMoveWithCollision(one.getX(), one.getY(), one.getZ(), two.getX(), two.getY(), two.getZ(), geoIndex)) {
               while(current + 1 < sub) {
                  path.remove(current + 1);
                  --sub;
               }
            }
         }
      }
   }

   private static boolean IsPointInLine(int x1, int y1, int x2, int y2, Location P) {
      if ((x1 != x2 || x2 != P.getX()) && (y1 != y2 || y2 != P.getY())) {
         return (x1 - P.getX()) * (y1 - P.getY()) == (P.getX() - x2) * (P.getY() - y2);
      } else {
         return true;
      }
   }
}
