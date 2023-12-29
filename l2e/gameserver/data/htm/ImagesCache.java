package l2e.gameserver.data.htm;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import l2e.gameserver.Config;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.mods.DDSConverter;
import l2e.gameserver.network.serverpackets.PledgeCrest;
import org.apache.commons.io.FilenameUtils;

public class ImagesCache {
   private static Logger _log = Logger.getLogger(ImagesCache.class.getName());
   private static final int[] SIZES = new int[]{1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024};
   private static final int MAX_SIZE = SIZES[SIZES.length - 1];
   public static final Pattern HTML_PATTERN = Pattern.compile("%image:(.*?)%", 32);
   private final Map<Integer, byte[]> _images = new HashMap<>();
   private final Map<String, Integer> _imagesId = new HashMap<>();

   public ImagesCache() {
      this.loadImages();
   }

   private void loadImages() {
      Map<Integer, File> imagesToLoad = getImagesToLoad();

      for(Entry<Integer, File> image : imagesToLoad.entrySet()) {
         File file = image.getValue();
         byte[] data = DDSConverter.convertToDDS(file).array();
         this._images.put(image.getKey(), data);
         this._imagesId.put(file.getName().toLowerCase(), image.getKey());
      }

      _log.info("Cache[Images]: Loaded " + imagesToLoad.size() + " images!");
   }

   private static Map<Integer, File> getImagesToLoad() {
      Map<Integer, File> files = new HashMap<>();
      File folder = new File(Config.DATAPACK_ROOT + "/data/images");
      if (!folder.exists()) {
         _log.log(Level.WARNING, "Path \"./data/images\" doesn't exist!", (Throwable)(new FileNotFoundException()));
         return files;
      } else {
         for(File file : folder.listFiles()) {
            for(File newFile : file.isDirectory() ? file.listFiles() : new File[]{file}) {
               if (checkImageFormat(newFile)) {
                  newFile = resizeImage(newFile);
                  int id = -1;

                  try {
                     String name = FilenameUtils.getBaseName(newFile.getName());
                     id = Integer.parseInt(name);
                  } catch (Exception var12) {
                     id = IdFactory.getInstance().getNextId();
                  }

                  if (id != -1) {
                     files.put(id, newFile);
                  }
               }
            }
         }

         return files;
      }
   }

   private static File resizeImage(File file) {
      BufferedImage image;
      try {
         image = ImageIO.read(file);
      } catch (IOException var13) {
         _log.log(Level.WARNING, "ImagesChache: Error while resizing " + file.getName() + " image.", (Throwable)var13);
         return null;
      }

      if (image == null) {
         return null;
      } else {
         int width = image.getWidth();
         int height = image.getHeight();
         boolean resizeWidth = true;
         if (width > MAX_SIZE) {
            image = image.getSubimage(0, 0, MAX_SIZE, height);
            resizeWidth = false;
         }

         boolean resizeHeight = true;
         if (height > MAX_SIZE) {
            image = image.getSubimage(0, 0, width, MAX_SIZE);
            resizeHeight = false;
         }

         int resizedWidth = width;
         if (resizeWidth) {
            for(int size : SIZES) {
               if (size >= width) {
                  resizedWidth = size;
                  break;
               }
            }
         }

         int resizedHeight = height;
         if (resizeHeight) {
            for(int size : SIZES) {
               if (size >= height) {
                  resizedHeight = size;
                  break;
               }
            }
         }

         if (resizedWidth != width || resizedHeight != height) {
            for(int x = 0; x < resizedWidth; ++x) {
               for(int y = 0; y < resizedHeight; ++y) {
                  image.setRGB(x, y, Color.BLACK.getRGB());
               }
            }

            String filename = file.getName();
            String format = filename.substring(filename.lastIndexOf("."));

            try {
               ImageIO.write(image, format, file);
            } catch (IOException var12) {
               _log.log(Level.WARNING, "ImagesChache: Error while resizing " + file.getName() + " image.", (Throwable)var12);
               return null;
            }
         }

         return file;
      }
   }

   public void sendImageToPlayer(Player player, int imageId) {
      if (Config.ALLOW_SENDING_IMAGES) {
         if (!player.wasImageLoaded(imageId)) {
            player.addLoadedImage(imageId);
            if (this._images.containsKey(imageId)) {
               player.sendPacket(new PledgeCrest(imageId, this._images.get(imageId)));
            }
         }
      }
   }

   private static boolean checkImageFormat(File file) {
      String filename = file.getName();
      int dotPos = filename.lastIndexOf(".");
      String format = filename.substring(dotPos);
      return format.equalsIgnoreCase(".jpg") || format.equalsIgnoreCase(".png") || format.equalsIgnoreCase(".bmp");
   }

   public static ImagesCache getInstance() {
      return ImagesCache.ImagesCacheHolder.instance;
   }

   private static class ImagesCacheHolder {
      protected static final ImagesCache instance = new ImagesCache();
   }
}
