package l2e.gameserver.model.zone;

import java.awt.geom.Line2D;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.items.instance.ItemInstance;

public abstract class ZoneForm {
   protected static final int STEP = 10;

   public abstract boolean isInsideZone(int var1, int var2, int var3);

   public abstract boolean intersectsRectangle(int var1, int var2, int var3, int var4);

   public abstract double getDistanceToZone(int var1, int var2);

   public abstract int getLowZ();

   public abstract int getHighZ();

   protected boolean lineSegmentsIntersect(int ax1, int ay1, int ax2, int ay2, int bx1, int by1, int bx2, int by2) {
      return Line2D.linesIntersect((double)ax1, (double)ay1, (double)ax2, (double)ay2, (double)bx1, (double)by1, (double)bx2, (double)by2);
   }

   public abstract void visualizeZone(int var1);

   protected final void dropDebugItem(int itemId, int num, int x, int y, int z) {
      ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
      item.setCount((long)num);
      item.spawnMe(x, y, z + 5);
      ZoneManager.getInstance().getDebugItems().add(item);
   }

   public abstract int[] getRandomPoint();
}
