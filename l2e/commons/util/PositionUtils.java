package l2e.commons.util;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;

public class PositionUtils {
   protected static final double HEADINGS_IN_PI = 10430.378350470453;
   private static final int MAX_ANGLE = 360;
   private static final double FRONT_MAX_ANGLE = 100.0;
   private static final double BACK_MAX_ANGLE = 40.0;

   public static PositionUtils.TargetDirection getDirectionTo(Creature target, Creature attacker) {
      if (target == null || attacker == null) {
         return PositionUtils.TargetDirection.NONE;
      } else if (isBehind(target, attacker)) {
         return PositionUtils.TargetDirection.BEHIND;
      } else {
         return isInFrontOf(target, attacker) ? PositionUtils.TargetDirection.FRONT : PositionUtils.TargetDirection.SIDE;
      }
   }

   public static boolean isInFrontOf(Creature target, Creature attacker) {
      if (target == null) {
         return false;
      } else {
         double angleTarget = calculateAngleFrom(target, attacker);
         double angleChar = convertHeadingToDegree(target.getHeading());
         double angleDiff = angleChar - angleTarget;
         if (angleDiff <= -260.0) {
            angleDiff += 360.0;
         }

         if (angleDiff >= 260.0) {
            angleDiff -= 360.0;
         }

         return Math.abs(angleDiff) <= 100.0;
      }
   }

   public static boolean isBehind(Creature target, Creature attacker) {
      if (target == null) {
         return false;
      } else {
         double angleChar = calculateAngleFrom(attacker, target);
         double angleTarget = convertHeadingToDegree(target.getHeading());
         double angleDiff = angleChar - angleTarget;
         if (angleDiff <= -320.0) {
            angleDiff += 360.0;
         }

         if (angleDiff >= 320.0) {
            angleDiff -= 360.0;
         }

         return Math.abs(angleDiff) <= 40.0;
      }
   }

   public static boolean isFacing(Creature attacker, GameObject target, int maxAngle) {
      if (target == null) {
         return false;
      } else {
         double maxAngleDiff = (double)(maxAngle / 2);
         double angleTarget = calculateAngleFrom(attacker, target);
         double angleChar = convertHeadingToDegree(attacker.getHeading());
         double angleDiff = angleChar - angleTarget;
         if (angleDiff <= -360.0 + maxAngleDiff) {
            angleDiff += 360.0;
         }

         if (angleDiff >= 360.0 - maxAngleDiff) {
            angleDiff -= 360.0;
         }

         return Math.abs(angleDiff) <= maxAngleDiff;
      }
   }

   public static int calculateHeadingFrom(GameObject obj1, GameObject obj2) {
      return calculateHeadingFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
   }

   public static int calculateHeadingFrom(int obj1X, int obj1Y, int obj2X, int obj2Y) {
      double angleTarget = Math.toDegrees(Math.atan2((double)(obj2Y - obj1Y), (double)(obj2X - obj1X)));
      if (angleTarget < 0.0) {
         angleTarget += 360.0;
      }

      return (int)(angleTarget * 182.044444444);
   }

   public static double calculateAngleFrom(GameObject obj1, GameObject obj2) {
      return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
   }

   public static double calculateAngleFrom(int obj1X, int obj1Y, int obj2X, int obj2Y) {
      double angleTarget = Math.toDegrees(Math.atan2((double)(obj2Y - obj1Y), (double)(obj2X - obj1X)));
      if (angleTarget < 0.0) {
         angleTarget += 360.0;
      }

      return angleTarget;
   }

   public static boolean checkIfInRange(int range, int x1, int y1, int x2, int y2) {
      return checkIfInRange(range, x1, y1, 0, x2, y2, 0, false);
   }

   public static boolean checkIfInRange(int range, int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis) {
      long dx = (long)(x1 - x2);
      long dy = (long)(y1 - y2);
      if (includeZAxis) {
         long dz = (long)(z1 - z2);
         return dx * dx + dy * dy + dz * dz <= (long)(range * range);
      } else {
         return dx * dx + dy * dy <= (long)(range * range);
      }
   }

   public static boolean checkIfInRange(int range, GameObject obj1, GameObject obj2, boolean includeZAxis) {
      return obj1 != null && obj2 != null
         ? checkIfInRange(range, obj1.getX(), obj1.getY(), obj1.getZ(), obj2.getX(), obj2.getY(), obj2.getZ(), includeZAxis)
         : false;
   }

   public static double convertHeadingToDegree(int heading) {
      return (double)heading / 182.044444444;
   }

   public static double convertHeadingToRadian(int heading) {
      return Math.toRadians(convertHeadingToDegree(heading) - 90.0);
   }

   public static int convertDegreeToClientHeading(double degree) {
      if (degree < 0.0) {
         degree += 360.0;
      }

      return (int)(degree * 182.044444444);
   }

   public static double calculateDistance(int x1, int y1, int z1, int x2, int y2) {
      return calculateDistance(x1, y1, 0, x2, y2, 0, false);
   }

   public static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis) {
      long dx = (long)(x1 - x2);
      long dy = (long)(y1 - y2);
      if (includeZAxis) {
         long dz = (long)(z1 - z2);
         return Math.sqrt((double)(dx * dx + dy * dy + dz * dz));
      } else {
         return Math.sqrt((double)(dx * dx + dy * dy));
      }
   }

   public static double calculateDistance(GameObject obj1, GameObject obj2, boolean includeZAxis) {
      return obj1 != null && obj2 != null
         ? calculateDistance(obj1.getX(), obj1.getY(), obj1.getZ(), obj2.getX(), obj2.getY(), obj2.getZ(), includeZAxis)
         : 2.147483647E9;
   }

   public static double getDistance(GameObject a1, GameObject a2) {
      return getDistance(a1.getX(), a2.getY(), a2.getX(), a2.getY());
   }

   public static double getDistance(Location loc1, Location loc2) {
      return getDistance(loc1.getX(), loc1.getY(), loc2.getX(), loc2.getY());
   }

   public static double getDistance(int x1, int y1, int x2, int y2) {
      return Math.hypot((double)(x1 - x2), (double)(y1 - y2));
   }

   public static int getHeadingTo(GameObject actor, GameObject target) {
      return actor != null && target != null && target != actor ? getHeadingTo(actor.getLocation(), target.getLocation()) : -1;
   }

   public static int getHeadingTo(Location actor, Location target) {
      if (actor != null && target != null && !target.equals(actor)) {
         int dx = target.getX() - actor.getX();
         int dy = target.getY() - actor.getY();
         int heading = target.getHeading() - (int)(Math.atan2((double)(-dy), (double)(-dx)) * 10430.378350470453 + 32768.0);
         if (heading < 0) {
            heading = heading + 1 + Integer.MAX_VALUE & 65535;
         } else if (heading > 65535) {
            heading &= 65535;
         }

         return heading;
      } else {
         return -1;
      }
   }

   public static enum TargetDirection {
      NONE,
      FRONT,
      SIDE,
      BEHIND;
   }
}
