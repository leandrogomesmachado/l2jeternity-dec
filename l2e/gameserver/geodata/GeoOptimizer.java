package l2e.gameserver.geodata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import l2e.gameserver.Config;

public class GeoOptimizer {
   private static final Logger _log = Logger.getLogger(GeoOptimizer.class.getName());
   public static int[][][] checkSums;
   private static final byte version = 1;

   public static GeoOptimizer.BlockLink[] loadBlockMatches(String fileName) {
      File f = new File(Config.DATAPACK_ROOT, fileName);
      if (!f.exists()) {
         return null;
      } else {
         try {
            FileChannel roChannel = new RandomAccessFile(f, "r").getChannel();
            int count = (int)((roChannel.size() - 1L) / 6L);
            ByteBuffer buffer = roChannel.map(MapMode.READ_ONLY, 0L, roChannel.size());
            roChannel.close();
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            if (buffer.get() != 1) {
               return null;
            } else {
               GeoOptimizer.BlockLink[] links = new GeoOptimizer.BlockLink[count];

               for(int i = 0; i < links.length; ++i) {
                  links[i] = new GeoOptimizer.BlockLink(buffer.getShort(), buffer.get(), buffer.get(), buffer.getShort());
               }

               return links;
            }
         } catch (FileNotFoundException var7) {
            _log.log(Level.SEVERE, "Block Matches File not Found! ", (Throwable)var7);
            return null;
         } catch (IOException var8) {
            _log.log(Level.SEVERE, "Error while loading Block Matches! ", (Throwable)var8);
            return null;
         }
      }
   }

   public static class BlockLink {
      public final int blockIndex;
      public final int linkBlockIndex;
      public final byte linkMapX;
      public final byte linkMapY;

      public BlockLink(short _blockIndex, byte _linkMapX, byte _linkMapY, short _linkBlockIndex) {
         this.blockIndex = _blockIndex & '\uffff';
         this.linkMapX = _linkMapX;
         this.linkMapY = _linkMapY;
         this.linkBlockIndex = _linkBlockIndex & '\uffff';
      }

      public BlockLink(int _blockIndex, byte _linkMapX, byte _linkMapY, int _linkBlockIndex) {
         this.blockIndex = _blockIndex & 65535;
         this.linkMapX = _linkMapX;
         this.linkMapY = _linkMapY;
         this.linkBlockIndex = _linkBlockIndex & 65535;
      }
   }

   public static class CheckSumLoader implements Runnable {
      private final int geoX;
      private final int geoY;
      private final int rx;
      private final int ry;
      private final byte[][][] region;
      private final String fileName;

      public CheckSumLoader(int _geoX, int _geoY, byte[][][] _region) {
         this.geoX = _geoX;
         this.geoY = _geoY;
         this.rx = this.geoX + 11;
         this.ry = _geoY + 10;
         this.region = _region;
         this.fileName = "geodata/checksum/" + this.rx + "_" + this.ry + ".crc";
      }

      private boolean loadFromFile() {
         File GeoCrc = new File(Config.DATAPACK_ROOT, this.fileName);
         if (!GeoCrc.exists()) {
            return false;
         } else {
            try {
               FileChannel roChannel = new RandomAccessFile(GeoCrc, "r").getChannel();
               if (roChannel.size() != 262144L) {
                  roChannel.close();
                  return false;
               } else {
                  ByteBuffer buffer = roChannel.map(MapMode.READ_ONLY, 0L, roChannel.size());
                  roChannel.close();
                  buffer.order(ByteOrder.LITTLE_ENDIAN);
                  int[] _checkSums = new int[65536];

                  for(int i = 0; i < 65536; ++i) {
                     _checkSums[i] = buffer.getInt();
                  }

                  GeoOptimizer.checkSums[this.geoX][this.geoY] = _checkSums;
                  return true;
               }
            } catch (FileNotFoundException var6) {
               GeoOptimizer._log.log(Level.SEVERE, "Geodata File not Found! ", (Throwable)var6);
               return false;
            } catch (IOException var7) {
               GeoOptimizer._log.log(Level.SEVERE, "Error while loading Geodata File", (Throwable)var7);
               return false;
            }
         }
      }

      private void saveToFile() {
         GeoOptimizer._log.info("Saving checksums to: " + this.fileName);

         try {
            File f = new File(Config.DATAPACK_ROOT, this.fileName);
            if (f.exists()) {
               f.delete();
            }

            FileChannel wChannel = new RandomAccessFile(f, "rw").getChannel();
            ByteBuffer buffer = wChannel.map(MapMode.READ_WRITE, 0L, 262144L);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            int[] _checkSums = GeoOptimizer.checkSums[this.geoX][this.geoY];

            for(int i = 0; i < 65536; ++i) {
               buffer.putInt(_checkSums[i]);
            }

            wChannel.close();
         } catch (FileNotFoundException var6) {
            GeoOptimizer._log.log(Level.SEVERE, "Geodata file not Found! ", (Throwable)var6);
         } catch (IOException var7) {
            GeoOptimizer._log.log(Level.SEVERE, "Error while loading Geodata File", (Throwable)var7);
         }
      }

      private void gen() {
         GeoOptimizer._log.info("Generating checksums for " + this.rx + "_" + this.ry);
         int[] _checkSums = new int[65536];
         CRC32 crc32 = new CRC32();

         for(int i = 0; i < 65536; ++i) {
            crc32.update(this.region[i][0]);
            _checkSums[i] = (int)(~crc32.getValue());
            crc32.reset();
         }

         GeoOptimizer.checkSums[this.geoX][this.geoY] = _checkSums;
      }

      @Override
      public void run() {
         if (!this.loadFromFile()) {
            this.gen();
            this.saveToFile();
         }
      }
   }

   public static class GeoBlocksMatchFinder implements Runnable {
      private final int geoX;
      private final int geoY;
      private final int rx;
      private final int ry;
      private final int maxScanRegions;
      private final String fileName;

      public GeoBlocksMatchFinder(int _geoX, int _geoY, int _maxScanRegions) {
         this.geoX = _geoX;
         this.geoY = _geoY;
         this.rx = this.geoX + 11;
         this.ry = this.geoY + 10;
         this.maxScanRegions = _maxScanRegions;
         this.fileName = "geodata/matches/" + this.rx + "_" + this.ry + ".matches";
      }

      private boolean exists() {
         return new File(Config.DATAPACK_ROOT, this.fileName).exists();
      }

      private void saveToFile(GeoOptimizer.BlockLink[] links) {
         GeoOptimizer._log.info("Saving matches to: " + this.fileName);

         try {
            File f = new File(Config.DATAPACK_ROOT, this.fileName);
            if (f.exists()) {
               f.delete();
            }

            FileChannel wChannel = new RandomAccessFile(f, "rw").getChannel();
            ByteBuffer buffer = wChannel.map(MapMode.READ_WRITE, 0L, (long)(links.length * 6 + 1));
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.put((byte)1);

            for(int i = 0; i < links.length; ++i) {
               buffer.putShort((short)links[i].blockIndex);
               buffer.put(links[i].linkMapX);
               buffer.put(links[i].linkMapY);
               buffer.putShort((short)links[i].linkBlockIndex);
            }

            wChannel.close();
         } catch (FileNotFoundException var6) {
            GeoOptimizer._log.log(Level.SEVERE, "Geodata File not found while saving! ", (Throwable)var6);
         } catch (IOException var7) {
            GeoOptimizer._log.log(Level.SEVERE, "Error while Saving Geodata File! ", (Throwable)var7);
         }
      }

      private void calcMatches(int[] curr_checkSums, int mapX, int mapY, List<GeoOptimizer.BlockLink> putlinks, boolean[] notready) {
         int[] next_checkSums = GeoOptimizer.checkSums[mapX][mapY];
         if (next_checkSums != null) {
            for(int blockIdx = 0; blockIdx < 65536; ++blockIdx) {
               if (notready[blockIdx]) {
                  int startIdx2 = next_checkSums == curr_checkSums ? blockIdx + 1 : 0;

                  for(int blockIdx2 = startIdx2; blockIdx2 < 65536; ++blockIdx2) {
                     if (curr_checkSums[blockIdx] == next_checkSums[blockIdx2]
                        && GeoEngine.compareGeoBlocks(this.geoX, this.geoY, blockIdx, mapX, mapY, blockIdx2)) {
                        putlinks.add(new GeoOptimizer.BlockLink(blockIdx, (byte)mapX, (byte)mapY, blockIdx2));
                        notready[blockIdx] = false;
                        break;
                     }
                  }
               }
            }
         }
      }

      private GeoOptimizer.BlockLink[] gen() {
         GeoOptimizer._log.info("Searching matches for " + this.rx + "_" + this.ry);
         long started = System.currentTimeMillis();
         boolean[] notready = new boolean[65536];

         for(int i = 0; i < 65536; ++i) {
            notready[i] = true;
         }

         List<GeoOptimizer.BlockLink> links = new ArrayList<>();
         int[] _checkSums = GeoOptimizer.checkSums[this.geoX][this.geoY];
         int n = 0;

         for(int mapX = this.geoX; mapX < 16; ++mapX) {
            int startgeoY = mapX == this.geoX ? this.geoY : 0;

            for(int mapY = startgeoY; mapY < 17; ++mapY) {
               this.calcMatches(_checkSums, mapX, mapY, links, notready);
               if (this.maxScanRegions > 0 && this.maxScanRegions == ++n) {
                  return links.toArray(new GeoOptimizer.BlockLink[links.size()]);
               }
            }
         }

         started = System.currentTimeMillis() - started;
         GeoOptimizer._log.info("Founded " + links.size() + " matches for " + this.rx + "_" + this.ry + " in " + (float)started / 1000.0F + "s");
         return links.toArray(new GeoOptimizer.BlockLink[links.size()]);
      }

      @Override
      public void run() {
         if (!this.exists()) {
            GeoOptimizer.BlockLink[] links = this.gen();
            this.saveToFile(links);
         }
      }
   }
}
