package l2e.gameserver.handler.admincommandhandlers.impl;

import java.io.File;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.actor.Player;

public class Cache implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_cache_htm_rebuild", "admin_cache_htm_reload", "admin_cache_reload_path", "admin_cache_reload_file"
   };

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_cache_htm_rebuild") || command.equals("admin_cache_htm_reload")) {
         HtmCache.getInstance().reload(Config.DATAPACK_ROOT);
         activeChar.sendMessage(
            "Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " MB on " + HtmCache.getInstance().getLoadedFiles() + " file(s) loaded."
         );
      } else if (command.startsWith("admin_cache_reload_path ")) {
         try {
            String path = command.split(" ")[1];
            HtmCache.getInstance().reloadPath(new File(Config.DATAPACK_ROOT, path));
            activeChar.sendMessage(
               "Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " MB in " + HtmCache.getInstance().getLoadedFiles() + " file(s) loaded."
            );
         } catch (Exception var5) {
            activeChar.sendMessage("Usage: //cache_reload_path <path>");
         }
      } else if (command.startsWith("admin_cache_reload_file ")) {
         try {
            String path = command.split(" ")[1];
            if (HtmCache.getInstance().loadFile(new File(Config.DATAPACK_ROOT, path)) != null) {
               activeChar.sendMessage("Cache[HTML]: file was loaded");
            } else {
               activeChar.sendMessage("Cache[HTML]: file can't be loaded");
            }
         } catch (Exception var4) {
            activeChar.sendMessage("Usage: //cache_reload_file <relative_path/file>");
         }
      }

      return true;
   }
}
