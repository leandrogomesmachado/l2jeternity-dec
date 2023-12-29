package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.service.autoenchant.EnchantManager;
import l2e.gameserver.model.service.autoenchant.EnchantUtils;
import l2e.gameserver.model.strings.server.ServerMessage;

public class Enchant implements IVoicedCommandHandler {
   private static String[] commands = new String[]{
      "enchant", "max_enchant", "item_limit", "common_for_safe", "begin_enchant", "item_choose", "item_change", "enchant_help"
   };

   @Override
   public boolean useVoicedCommand(String command, Player player, String params) {
      if (player != null && Config.ALLOW_ENCHANT_SERVICE) {
         if (command.equals("enchant")) {
            EnchantManager.getInstance().showMainPage(player);
         } else if (command.equals("max_enchant")) {
            if (params != null && !params.isEmpty()) {
               try {
                  int userMax = Integer.parseInt(params.split("-")[0]);
                  ItemInstance enchant = player.getEnchantParams().upgradeItem;
                  ItemInstance targetItem = player.getEnchantParams().targetItem;
                  if (!EnchantUtils.getInstance().isAttribute(enchant)) {
                     int configMax;
                     if (targetItem.isWeapon()) {
                        configMax = Config.ENCHANT_MAX_WEAPON;
                     } else if (targetItem.isArmor()) {
                        configMax = Config.ENCHANT_MAX_ARMOR;
                     } else {
                        if (!targetItem.isJewel()) {
                           return false;
                        }

                        configMax = Config.ENCHANT_MAX_JEWELRY;
                     }

                     if (userMax < 1) {
                        userMax = 1;
                     }

                     if (userMax > configMax) {
                        userMax = configMax;
                     }

                     player.getEnchantParams().maxEnchant = userMax;
                     player.getEnchantParams().isChangingMaxEnchant = false;
                  } else {
                     if (userMax < 1) {
                        userMax = 1;
                     }

                     if (targetItem.isJewel()) {
                        return false;
                     }

                     if (targetItem.isArmor() && userMax > 120) {
                        userMax = 120;
                     }

                     if (targetItem.isWeapon() && userMax > 300) {
                        userMax = 300;
                     }

                     player.getEnchantParams().maxEnchantAtt = userMax;
                     player.getEnchantParams().isChangingMaxEnchant = false;
                  }
               } catch (Exception var9) {
               }
            } else {
               player.getEnchantParams().isChangingMaxEnchant = true;
            }

            EnchantManager.getInstance().showMainPage(player);
         } else if (command.equals("item_limit")) {
            if (params != null && !params.isEmpty()) {
               try {
                  int userLimit = Integer.parseInt(params.split("-")[0]);
                  ItemInstance enchant = player.getEnchantParams().upgradeItem;
                  if (userLimit < 1) {
                     userLimit = 1;
                  }

                  if ((long)userLimit > enchant.getCount()) {
                     userLimit = (int)enchant.getCount();
                  }

                  if (userLimit > Config.ENCHANT_MAX_ITEM_LIMIT) {
                     userLimit = Config.ENCHANT_MAX_ITEM_LIMIT;
                  }

                  player.getEnchantParams().upgradeItemLimit = userLimit;
                  player.getEnchantParams().isChangingUpgradeItemLimit = false;
               } catch (Exception var8) {
               }
            } else {
               player.getEnchantParams().isChangingUpgradeItemLimit = true;
            }

            EnchantManager.getInstance().showMainPage(player);
         } else if (command.equals("common_for_safe")) {
            if (params == null || params.isEmpty()) {
               return false;
            }

            int safe = Integer.parseInt(params.split("-")[0]);
            if (safe == 0) {
               player.getEnchantParams().isUseCommonScrollWhenSafe = false;
            } else {
               if (safe != 1) {
                  return false;
               }

               player.getEnchantParams().isUseCommonScrollWhenSafe = true;
            }

            EnchantManager.getInstance().showMainPage(player);
         } else if (command.equals("item_choose")) {
            if (params == null || params.isEmpty()) {
               return false;
            }

            String[] arr = params.split("-");
            if (arr.length < 3) {
               return false;
            }

            EnchantManager.getInstance().showItemChoosePage(player, Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
         } else if (command.equals("item_change")) {
            if (params == null || params.isEmpty()) {
               return false;
            }

            String[] arr = params.split("-");
            if (arr.length < 2) {
               return false;
            }

            if (arr[0].equals("0")) {
               player.getEnchantParams().targetItem = player.getInventory().getItemByObjectId(Integer.parseInt(arr[1]));
            } else {
               player.getEnchantParams().upgradeItem = player.getInventory().getItemByObjectId(Integer.parseInt(arr[1]));
            }

            EnchantManager.getInstance().showMainPage(player);
         } else if (command.equals("begin_enchant")) {
            if (Config.ENCHANT_SERVICE_ONLY_FOR_PREMIUM && !player.hasPremiumBonus()) {
               player.sendMessage(new ServerMessage("Enchant.ONLY_PREMIUM", player.getLang()).toString());
               return false;
            }

            ItemInstance consumeItem = player.getInventory().getItemByItemId(Config.ENCHANT_CONSUME_ITEM);
            if (Config.ENCHANT_CONSUME_ITEM != 0 && (consumeItem == null || consumeItem.getCount() < (long)Config.ENCHANT_CONSUME_ITEM_COUNT)) {
               Item template = ItemsParser.getInstance().getTemplate(Config.ENCHANT_CONSUME_ITEM);
               ServerMessage msg = new ServerMessage("Enchant.NEED_ITEMS", player.getLang());
               msg.add(Config.ENCHANT_CONSUME_ITEM_COUNT);
               msg.add(player.getItemName(template));
               player.sendMessage(msg.toString());
               return false;
            }

            EnchantUtils.getInstance().enchant(player);
         } else if (command.equals("enchant_help")) {
            EnchantManager.getInstance().showHelpPage(player);
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return commands;
   }
}
