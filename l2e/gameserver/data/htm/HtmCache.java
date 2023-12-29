package l2e.gameserver.data.htm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.commons.util.file.filter.HTMLFilter;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;

public class HtmCache {
   private static final Logger _log = Logger.getLogger(HtmCache.class.getName());
   private static final HTMLFilter htmlFilter = new HTMLFilter();
   private static final Map<String, String> _cache = (Map<String, String>)(Config.LAZY_CACHE ? new ConcurrentHashMap<>() : new HashMap<>());
   private int _loadedFiles;
   private long _bytesBuffLen;

   protected HtmCache() {
      this.reload();
   }

   public void reload() {
      this.reload(Config.DATAPACK_ROOT);
   }

   public void reload(File f) {
      if (!Config.LAZY_CACHE) {
         _log.info("Html cache start...");
         this.parseDir(f);
         _log.info("Cache[HTML]: " + String.format("%.3f", this.getMemoryUsage()) + " megabytes on " + this.getLoadedFiles() + " files loaded");
      } else {
         _cache.clear();
         this._loadedFiles = 0;
         this._bytesBuffLen = 0L;
         _log.info("Cache[HTML]: Running lazy cache");
      }
   }

   public void reloadPath(File f) {
      this.parseDir(f);
      _log.info("Cache[HTML]: Reloaded specified path.");
   }

   public double getMemoryUsage() {
      return (double)((float)this._bytesBuffLen / 1048576.0F);
   }

   public int getLoadedFiles() {
      return this._loadedFiles;
   }

   private void parseDir(File dir) {
      File[] files = dir.listFiles();

      for(File file : files) {
         if (!file.isDirectory()) {
            this.loadFile(file);
         } else {
            this.parseDir(file);
         }
      }
   }

   public String loadFile(File file) {
      if (!htmlFilter.accept(file)) {
         return null;
      } else {
         String relpath = Util.getRelativePath(Config.DATAPACK_ROOT, file);
         String content = null;

         try (
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
         ) {
            int bytes = bis.available();
            byte[] raw = new byte[bytes];
            bis.read(raw);
            content = new String(raw, "UTF-8");
            String oldContent = _cache.get(relpath);
            if (oldContent == null) {
               this._bytesBuffLen += (long)bytes;
               ++this._loadedFiles;
            } else {
               this._bytesBuffLen = this._bytesBuffLen - (long)oldContent.length() + (long)bytes;
            }

            _cache.put(relpath, content);
         } catch (Exception var38) {
            _log.log(Level.WARNING, "Problem with htm file " + var38.getMessage(), (Throwable)var38);
         }

         return content;
      }
   }

   public String getHtmForce(Player player, String prefix, String path) {
      String content = this.getHtm(player, prefix, path);
      if (content == null) {
         content = "<html><body>My text is missing:<br>" + path + "</body></html>";
         _log.warning("Cache[HTML]: Missing HTML page: " + path);
      }

      return content;
   }

   public String getHtm(Player player, String prefix, String path) {
      String oriPath = path;
      if (prefix != null && !prefix.equalsIgnoreCase("en") && path.contains("html/")) {
         path = path.replace("html/", "html-" + prefix + "/");
      }

      String content = getInstance().getHtm(player, path);
      if (content == null && !oriPath.equals(path)) {
         content = getInstance().getHtm(player, oriPath);
      }

      return content;
   }

   public String getHtm(Player player, String path) {
      if (path != null && !path.isEmpty()) {
         String content = _cache.get(path);
         if (Config.LAZY_CACHE && content == null) {
            content = this.loadFile(new File(Config.DATAPACK_ROOT, path));
         }

         if (player.isGM() && content != null) {
            String link = path.replace("data/", "");
            player.sendMessage("HTML: " + link);
         }

         return content;
      } else {
         return "";
      }
   }

   public boolean contains(String path) {
      return _cache.containsKey(path);
   }

   public boolean isLoadable(String path) {
      return htmlFilter.accept(new File(path));
   }

   public static HtmCache getInstance() {
      return HtmCache.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final HtmCache _instance = new HtmCache();
   }
}
