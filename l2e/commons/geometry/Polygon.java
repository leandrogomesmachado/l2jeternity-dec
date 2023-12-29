package l2e.commons.geometry;

import l2e.commons.lang.ArrayUtils;

public class Polygon extends AbstractShape {
   protected Point2D[] points = Point2D.EMPTY_ARRAY;

   public Polygon add(int x, int y) {
      this.add(new Point2D(x, y));
      return this;
   }

   public Polygon add(Point2D p) {
      if (this.points.length == 0) {
         this.min._y = p.getY();
         this.min._x = p.getX();
         this.max._x = p.getX();
         this.max._y = p.getY();
      } else {
         this.min._y = Math.min(this.min.getY(), p.getY());
         this.min._x = Math.min(this.min.getX(), p.getX());
         this.max._x = Math.max(this.max.getX(), p.getX());
         this.max._y = Math.max(this.max.getY(), p.getY());
      }

      this.points = ArrayUtils.add(this.points, p);
      return this;
   }

   public Polygon setZmax(int z) {
      this.max._z = z;
      return this;
   }

   public Polygon setZmin(int z) {
      this.min._z = z;
      return this;
   }

   @Override
   public boolean isInside(int x, int y) {
      if (x >= this.min.getX() && x <= this.max.getX() && y >= this.min.getY() && y <= this.max.getY()) {
         int hits = 0;
         int npoints = this.points.length;
         Point2D last = this.points[npoints - 1];

         for(int i = 0; i < npoints; ++i) {
            Point2D cur;
            cur = this.points[i];
            label67:
            if (cur.getY() != last.getY()) {
               int leftx;
               if (cur.getX() < last.getX()) {
                  if (x >= last.getX()) {
                     break label67;
                  }

                  leftx = cur.getX();
               } else {
                  if (x >= cur.getX()) {
                     break label67;
                  }

                  leftx = last.getX();
               }

               double test1;
               double test2;
               if (cur.getY() < last.getY()) {
                  if (y < cur.getY() || y >= last.getY()) {
                     break label67;
                  }

                  if (x < leftx) {
                     ++hits;
                     break label67;
                  }

                  test1 = (double)(x - cur.getX());
                  test2 = (double)(y - cur.getY());
               } else {
                  if (y < last.getY() || y >= cur.getY()) {
                     break label67;
                  }

                  if (x < leftx) {
                     ++hits;
                     break label67;
                  }

                  test1 = (double)(x - last.getX());
                  test2 = (double)(y - last.getY());
               }

               if (test1 < test2 / (double)(last.getY() - cur.getY()) * (double)(last.getX() - cur.getX())) {
                  ++hits;
               }
            }

            last = cur;
         }

         return (hits & 1) != 0;
      } else {
         return false;
      }
   }

   public boolean validate() {
      if (this.points.length < 3) {
         return false;
      } else {
         if (this.points.length > 3) {
            for(int i = 1; i < this.points.length; ++i) {
               int ii = i + 1 < this.points.length ? i + 1 : 0;

               for(int n = i; n < this.points.length; ++n) {
                  if (Math.abs(n - i) > 1) {
                     int nn = n + 1 < this.points.length ? n + 1 : 0;
                     if (GeometryUtils.checkIfLineSegementsIntersects(this.points[i], this.points[ii], this.points[n], this.points[nn])) {
                        return false;
                     }
                  }
               }
            }
         }

         return true;
      }
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("[");

      for(int i = 0; i < this.points.length; ++i) {
         sb.append(this.points[i]);
         if (i < this.points.length - 1) {
            sb.append(",");
         }
      }

      sb.append("]");
      return sb.toString();
   }
}
