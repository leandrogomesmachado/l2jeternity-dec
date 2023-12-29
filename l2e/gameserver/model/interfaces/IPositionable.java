package l2e.gameserver.model.interfaces;

import l2e.gameserver.model.Location;

public interface IPositionable extends ILocational {
   void setX(int var1);

   void setY(int var1);

   void setZ(int var1);

   void setXYZ(int var1, int var2, int var3);

   void setXYZ(ILocational var1);

   void setHeading(int var1);

   void setLocation(Location var1);
}
