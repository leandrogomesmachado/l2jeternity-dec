package l2e.gameserver.geodata;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import l2e.commons.lang.ArrayUtils;
import l2e.commons.text.StrTable;
import l2e.gameserver.Config;
import l2e.gameserver.model.Location;

public class PathFindBuffers {
   public static final int MIN_MAP_SIZE = 64;
   public static final int STEP_MAP_SIZE = 32;
   public static final int MAX_MAP_SIZE = 512;
   private static TIntObjectHashMap<PathFindBuffers.PathFindBuffer[]> buffers = new TIntObjectHashMap<>();
   private static int[] sizes = new int[0];
   private static Lock lock = new ReentrantLock();

   private static PathFindBuffers.PathFindBuffer create(int mapSize) {
      lock.lock();

      PathFindBuffers.PathFindBuffer var3;
      try {
         PathFindBuffers.PathFindBuffer[] buff = (PathFindBuffers.PathFindBuffer[])buffers.get(mapSize);
         PathFindBuffers.PathFindBuffer buffer;
         if (buff != null) {
            buff = ArrayUtils.add(buff, buffer = new PathFindBuffers.PathFindBuffer(mapSize));
         } else {
            buff = new PathFindBuffers.PathFindBuffer[]{buffer = new PathFindBuffers.PathFindBuffer(mapSize)};
            sizes = org.apache.commons.lang3.ArrayUtils.add(sizes, mapSize);
            Arrays.sort(sizes);
         }

         buffers.put(mapSize, buff);
         buffer.inUse = true;
         var3 = buffer;
      } finally {
         lock.unlock();
      }

      return var3;
   }

   private static PathFindBuffers.PathFindBuffer get(int mapSize) {
      lock.lock();

      try {
         PathFindBuffers.PathFindBuffer[] buff = (PathFindBuffers.PathFindBuffer[])buffers.get(mapSize);

         for(PathFindBuffers.PathFindBuffer buffer : buff) {
            if (!buffer.inUse) {
               buffer.inUse = true;
               return buffer;
            }
         }

         return null;
      } finally {
         lock.unlock();
      }
   }

   public static PathFindBuffers.PathFindBuffer alloc(int mapSize) {
      if (mapSize > 512) {
         return null;
      } else {
         mapSize += 32;
         if (mapSize < 64) {
            mapSize = 64;
         }

         PathFindBuffers.PathFindBuffer buffer = null;

         for(int i = 0; i < sizes.length; ++i) {
            if (sizes[i] >= mapSize) {
               mapSize = sizes[i];
               buffer = get(mapSize);
               break;
            }
         }

         if (buffer == null) {
            for(int size = 64; size < 512; size += 32) {
               if (size >= mapSize) {
                  buffer = create(size);
                  break;
               }
            }
         }

         return buffer;
      }
   }

   public static void recycle(PathFindBuffers.PathFindBuffer buffer) {
      lock.lock();

      try {
         buffer.inUse = false;
      } finally {
         lock.unlock();
      }
   }

   public static StrTable getStats() {
      StrTable table = new StrTable("PathFind Buffers Stats");
      lock.lock();

      try {
         long totalUses = 0L;
         long totalPlayable = 0L;
         long totalTime = 0L;
         int index = 0;

         for(int size : sizes) {
            ++index;
            int count = 0;
            long uses = 0L;
            long playable = 0L;
            long itrs = 0L;
            long success = 0L;
            long overtime = 0L;
            long time = 0L;

            for(PathFindBuffers.PathFindBuffer buff : buffers.get(size)) {
               ++count;
               uses += buff.totalUses;
               playable += buff.playableUses;
               success += buff.successUses;
               overtime += buff.overtimeUses;
               time += buff.totalTime / 1000000L;
               itrs += buff.totalItr;
            }

            totalUses += uses;
            totalPlayable += playable;
            totalTime += time;
            table.set(index, "Size", size);
            table.set(index, "Count", count);
            table.set(index, "Uses (success%)", uses + "(" + String.format("%2.2f", uses > 0L ? (double)success * 100.0 / (double)uses : 0.0) + "%)");
            table.set(index, "Uses, playble", playable + "(" + String.format("%2.2f", uses > 0L ? (double)playable * 100.0 / (double)uses : 0.0) + "%)");
            table.set(index, "Uses, overtime", overtime + "(" + String.format("%2.2f", uses > 0L ? (double)overtime * 100.0 / (double)uses : 0.0) + "%)");
            table.set(index, "Iter., avg", uses > 0L ? itrs / uses : 0L);
            table.set(index, "Time, avg (ms)", String.format("%1.3f", uses > 0L ? (double)time / (double)uses : 0.0));
         }

         table.addTitle("Uses, total / playable  : " + totalUses + " / " + totalPlayable);
         table.addTitle(
            "Uses, total time / avg (ms) : " + totalTime + " / " + String.format("%1.3f", totalUses > 0L ? (double)totalTime / (double)totalUses : 0.0)
         );
      } finally {
         lock.unlock();
      }

      return table;
   }

   static {
      TIntIntHashMap config = new TIntIntHashMap();

      for(String e : Config.PATHFIND_BUFFERS.split(";")) {
         String[] k;
         if (!e.isEmpty() && (k = e.split("x")).length == 2) {
            config.put(Integer.valueOf(k[1]), Integer.valueOf(k[0]));
         }
      }

      TIntIntIterator itr = config.iterator();

      while(itr.hasNext()) {
         itr.advance();
         int size = itr.key();
         int count = itr.value();
         PathFindBuffers.PathFindBuffer[] buff = new PathFindBuffers.PathFindBuffer[count];

         for(int i = 0; i < count; ++i) {
            buff[i] = new PathFindBuffers.PathFindBuffer(size);
         }

         buffers.put(size, buff);
      }

      sizes = config.keys();
      Arrays.sort(sizes);
   }

   public static class GeoNode implements Comparable<PathFindBuffers.GeoNode> {
      public static final int NONE = 0;
      public static final int OPENED = 1;
      public static final int CLOSED = -1;
      public int x;
      public int y;
      public short z;
      public short nswe = -1;
      public float totalCost;
      public float costFromStart;
      public float costToEnd;
      public int state;
      public PathFindBuffers.GeoNode parent;

      public PathFindBuffers.GeoNode set(int x, int y, short z) {
         this.x = x;
         this.y = y;
         this.z = z;
         return this;
      }

      public boolean isSet() {
         return this.nswe != -1;
      }

      public void free() {
         this.nswe = -1;
         this.costFromStart = 0.0F;
         this.totalCost = 0.0F;
         this.costToEnd = 0.0F;
         this.parent = null;
         this.state = 0;
      }

      public Location getLoc() {
         return new Location(this.x, this.y, this.z);
      }

      @Override
      public String toString() {
         return "[" + this.x + "," + this.y + "," + this.z + "] f: " + this.totalCost;
      }

      public int compareTo(PathFindBuffers.GeoNode o) {
         if (this.totalCost > o.totalCost) {
            return 1;
         } else {
            return this.totalCost < o.totalCost ? -1 : 0;
         }
      }
   }

   public static class PathFindBuffer {
      final int mapSize;
      final PathFindBuffers.GeoNode[][] nodes;
      final Queue<PathFindBuffers.GeoNode> open;
      int offsetX;
      int offsetY;
      boolean inUse;
      long totalUses;
      long successUses;
      long overtimeUses;
      long playableUses;
      long totalTime;
      long totalItr;

      public PathFindBuffer(int mapSize) {
         this.open = new PriorityQueue<>(mapSize);
         this.mapSize = mapSize;
         this.nodes = new PathFindBuffers.GeoNode[mapSize][mapSize];

         for(int i = 0; i < this.nodes.length; ++i) {
            for(int j = 0; j < this.nodes[i].length; ++j) {
               this.nodes[i][j] = new PathFindBuffers.GeoNode();
            }
         }
      }

      public void free() {
         this.open.clear();

         for(int i = 0; i < this.nodes.length; ++i) {
            for(int j = 0; j < this.nodes[i].length; ++j) {
               this.nodes[i][j].free();
            }
         }
      }
   }
}
