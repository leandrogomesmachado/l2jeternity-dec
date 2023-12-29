package l2e.gameserver.handler.voicedcommandhandlers.impl;

import java.util.List;
import l2e.commons.util.TimeUtils;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.instancemanager.ItemRecoveryManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.ItemRecovery;

public class RecoveryItem implements IVoicedCommandHandler {
   private static final String[] _voicedCommands = new String[]{"recovery", "itemRecovery"};

   @Override
   public boolean useVoicedCommand(String command, Player player, String args) {
      if (!Config.ALLOW_RECOVERY_ITEMS) {
         return false;
      } else {
         if (command.equals("recovery")) {
            List<ItemRecovery> itemList = ItemRecoveryManager.getInstance().getAllRemoveItems(player.getObjectId());
            if (itemList == null || itemList.isEmpty()) {
               player.sendMessage("Your delete item list if empty!");
               return false;
            }

            String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/recovetyItem/index.htm");
            String template = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/recovetyItem/template.htm");
            String block = "";
            String list = "";
            if (args == null) {
               args = "1";
            }

            String[] param = args.split(" ");
            int page = param[0].length() > 0 ? Integer.parseInt(param[0]) : 1;
            int perpage = 7;
            int counter = 0;
            boolean isThereNextPage = itemList.size() > 7;

            for(int i = (page - 1) * 7; i < itemList.size(); ++i) {
               ItemRecovery itemRec = itemList.get(i);
               if (itemRec != null) {
                  String name = Util.getItemName(player, itemRec.getItemId());
                  if (name.length() > 30) {
                     name = name.substring(0, 30) + ".";
                  }

                  block = template.replace("{bypass}", "bypass -h .itemRecovery " + itemRec.getObjectId() + "_" + page);
                  block = block.replace("{name}", name);
                  block = block.replace("{enchant}", itemRec.getEnchantLevel() > 0 ? "+" + itemRec.getEnchantLevel() + "" : "");
                  block = block.replace("{count}", String.valueOf(itemRec.getCount()));
                  block = block.replace(
                     "{time}", String.valueOf(TimeUtils.formatTime(player, (int)((itemRec.getTime() - System.currentTimeMillis()) / 1000L), false))
                  );
                  block = block.replace("{icon}", Util.getItemIcon(itemRec.getItemId()));
                  list = list + block;
               }

               if (++counter >= 7) {
                  break;
               }
            }

            int count = (int)Math.ceil((double)itemList.size() / 7.0);
            html = html.replace("{list}", list);
            html = html.replace("{navigation}", Util.getNavigationBlock(count, page, itemList.size(), 7, isThereNextPage, ".recovery %s"));
            Util.setHtml(html, player);
         } else if (command.equals("itemRecovery")) {
            if (args == null) {
               return false;
            }

            String[] subStr = args.split(" ")[0].split("_");
            int objId = Integer.parseInt(subStr[0]);
            int page = Integer.parseInt(subStr[1]);
            if (!ItemRecoveryManager.getInstance().recoveryItem(objId, player)) {
               player.sendMessage("Item does not belong to you or time has expired!");
            }

            this.useVoicedCommand("recovery", player, String.valueOf(page));
         }

         return true;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }
}
