package l2e.commons.geometry;

public class GeometryUtils {
   private GeometryUtils() {
   }

   public static boolean checkIfLinesIntersects(Point2D a, Point2D b, Point2D c, Point2D d) {
      return checkIfLinesIntersects(a, b, c, d, null);
   }

   public static boolean checkIfLinesIntersects(Point2D a, Point2D b, Point2D c, Point2D d, Point2D r) {
      if ((a.getX() != b.getX() || a.getY() != b.getY()) && (c.getX() != d.getX() || c.getY() != d.getY())) {
         double Bx = (double)(b.getX() - a.getX());
         double By = (double)(b.getY() - a.getY());
         double Cx = (double)(c.getX() - a.getX());
         double Cy = (double)(c.getY() - a.getY());
         double Dx = (double)(d.getX() - a.getX());
         double Dy = (double)(d.getY() - a.getY());
         double distAB = Math.sqrt(Bx * Bx + By * By);
         double theCos = Bx / distAB;
         double theSin = By / distAB;
         double newX = Cx * theCos + Cy * theSin;
         Cy = (double)((int)(Cy * theCos - Cx * theSin));
         newX = Dx * theCos + Dy * theSin;
         Dy = (double)((int)(Dy * theCos - Dx * theSin));
         if (Cy == Dy) {
            return false;
         } else {
            double ABpos = newX + (newX - newX) * Dy / (Dy - Cy);
            if (r != null) {
               r._x = (int)((double)a.getX() + ABpos * theCos);
               r._y = (int)((double)a.getY() + ABpos * theSin);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean checkIfLineSegementsIntersects(Point2D a, Point2D b, Point2D c, Point2D d) {
      return checkIfLineSegementsIntersects(a, b, c, d, null);
   }

   public static boolean checkIfLineSegementsIntersects(Point2D a, Point2D b, Point2D c, Point2D d, Point2D r) {
      if ((a.getX() != b.getX() || a.getY() != b.getY()) && (c.getX() != d.getX() || c.getY() != d.getY())) {
         if ((a.getX() != c.getX() || a.getY() != c.getY())
            && (b.getX() != c.getX() || b.getY() != c.getY())
            && (a.getX() != d.getX() || a.getY() != d.getY())
            && (b.getX() != d.getX() || b.getY() != d.getY())) {
            double Bx = (double)(b.getX() - a.getX());
            double By = (double)(b.getY() - a.getY());
            double Cx = (double)(c.getX() - a.getX());
            double Cy = (double)(c.getY() - a.getY());
            double Dx = (double)(d.getX() - a.getX());
            double Dy = (double)(d.getY() - a.getY());
            double distAB = Math.sqrt(Bx * Bx + By * By);
            double theCos = Bx / distAB;
            double theSin = By / distAB;
            double newX = Cx * theCos + Cy * theSin;
            Cy = (double)((int)(Cy * theCos - Cx * theSin));
            newX = Dx * theCos + Dy * theSin;
            Dy = (double)((int)(Dy * theCos - Dx * theSin));
            if ((!(Cy < 0.0) || !(Dy < 0.0)) && (!(Cy >= 0.0) || !(Dy >= 0.0))) {
               double ABpos = newX + (newX - newX) * Dy / (Dy - Cy);
               if (!(ABpos < 0.0) && !(ABpos > distAB)) {
                  if (r != null) {
                     r._x = (int)((double)a.getX() + ABpos * theCos);
                     r._y = (int)((double)a.getY() + ABpos * theSin);
                  }

                  return true;
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static int calculateDistance(Point2D a, Point2D b) {
      return calculateDistance(a._x, a._y, b._x, b._y);
   }

   public static int calculateDistance(Point3D a, Point3D b, boolean includeZAxis) {
      return calculateDistance(a._x, a._y, a._z, b._x, b._y, b._z, includeZAxis);
   }

   public static int calculateDistance(int x1, int y1, int x2, int y2) {
      return calculateDistance(x1, y1, 0, x2, y2, 0, false);
   }

   public static int calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis) {
      long dx = (long)(x1 - x2);
      long dy = (long)(y1 - y2);
      if (includeZAxis) {
         long dz = (long)(z1 - z2);
         return (int)Math.sqrt((double)(dx * dx + dy * dy + dz * dz));
      } else {
         return (int)Math.sqrt((double)(dx * dx + dy * dy));
      }
   }

   public static double calculateAngleFrom(Point2D a, Point2D b) {
      return calculateAngleFrom(a._x, a._y, b._x, b._y);
   }

   public static double calculateAngleFrom(int x1, int y1, int x2, int y2) {
      double angleTarget = Math.toDegrees(Math.atan2((double)(y2 - y1), (double)(x2 - x1)));
      if (angleTarget < 0.0) {
         angleTarget += 360.0;
      }

      return angleTarget;
   }

   public static Point2D applyOffset(Point2D a, Point2D b, int offset, boolean add) {
      Point2D result = new Point2D();
      if (offset <= 0) {
         result._x = a._x;
         result._y = a._y;
         return result;
      } else {
         long dx = (long)(a._x - b._x);
         long dy = (long)(a._y - b._y);
         double distance = Math.sqrt((double)(dx * dx + dy * dy));
         if (!add) {
            if (distance <= (double)offset) {
               result._x = b._x;
               result._y = b._y;
               return result;
            }
         } else {
            offset = (int)((double)offset + distance);
         }

         if (distance >= 1.0) {
            double cut = (double)offset / distance;
            result._x = a._x - (int)((double)dx * cut + 0.5);
            result._y = a._y - (int)((double)dy * cut + 0.5);
         }

         return result;
      }
   }

   public static Point2D applyOffset(int x1, int y1, int x2, int y2, int offset, boolean add) {
      return applyOffset(new Point2D(x1, y1), new Point2D(x2, y2), offset, add);
   }

   public static boolean isOnLine(Point2D a, Point2D b, int x, int y) {
      return (x - a._x) * (b._y - a._y) - (b._x - a._x) * (y - a._y) == 0;
   }

   public static Point2D getLineCenter(Point2D a, Point2D b) {
      return getLineCenter(a._x, a._y, b._x, b._y);
   }

   public static Point2D getLineCenter(int x1, int y1, int x2, int y2) {
      return new Point2D((x1 + x2) / 2, (y1 + y2) / 2);
   }

   public static Point2D getNearestPointOnCircle(Point2D center, int r, int x, int y) {
      return applyOffset(center, new Point2D(x, y), r, false);
   }

   public static Point2D getNearestPointOnLine(Point2D p1, Point2D p2, int x, int y) {
      int r1 = calculateDistance(p1._x, p1._y, x, y);
      Point2D np1 = getNearestPointOnCircle(p1, r1, p2._x, p2._y);
      int r2 = calculateDistance(p2._x, p2._y, x, y);
      Point2D np2 = getNearestPointOnCircle(p2, r2, p1._x, p1._y);
      return getLineCenter(np1, np2);
   }

   public static Point2D getNearestPointOnPolygon(Point2D[] points, int x, int y) {
      Point2D nearestPoint = new Point2D();
      if (points.length == 0) {
         nearestPoint = null;
      } else if (points.length == 1) {
         nearestPoint._x = points[0]._x;
         nearestPoint._y = points[0]._y;
      } else {
         for(int i = 1; i <= points.length; ++i) {
            Point2D p1 = points[i - 1];
            Point2D p2 = i == points.length ? points[0] : points[i];
            Point2D n = getNearestPointOnLine(p1, p2, x, y);
            if (calculateDistance(n._x, n._y, x, y) < calculateDistance(nearestPoint._x, nearestPoint._y, x, y)) {
               nearestPoint = n;
            }
         }
      }

      return nearestPoint;
   }

   public static boolean isOnPolygonPerimeter(Point2D[] points, int x, int y) {
      if (points.length == 0) {
         return false;
      } else if (points.length != 1) {
         for(int i = 1; i <= points.length; ++i) {
            Point2D p1 = points[i - 1];
            Point2D p2 = i == points.length ? points[0] : points[i];
            if (isOnLine(p1, p2, x, y)) {
               return true;
            }
         }

         return false;
      } else {
         return points[0]._x == x && points[1]._y == y;
      }
   }
}
