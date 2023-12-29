package l2e.commons.geometry;

public interface Shape {
   boolean isInside(int var1, int var2);

   boolean isInside(int var1, int var2, int var3);

   int getXmax();

   int getXmin();

   int getYmax();

   int getYmin();

   int getZmax();

   int getZmin();
}
