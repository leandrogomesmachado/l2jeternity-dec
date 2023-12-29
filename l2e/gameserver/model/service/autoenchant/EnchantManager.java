package l2e.gameserver.model.service.autoenchant;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class EnchantManager {
   private static EnchantManager instance;
   private final String mainColor = "BCBCBC";
   private final String fieldColor = "B59A75";
   private final String unknown = this.setColor("?", "B59A75");

   public static EnchantManager getInstance() {
      return instance == null ? (instance = new EnchantManager()) : instance;
   }

   private String setColor(String text, String color) {
      return "<font color=" + color + ">" + text + "</font>";
   }

   private String setColor(int value, String color) {
      return this.setColor("" + value, color);
   }

   private String setCenter(String text) {
      return "<center>" + text + "</center>";
   }

   private String setButton(String bypass, String name, int width, int height) {
      return "<button width="
         + width
         + " height="
         + height
         + " back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h ."
         + bypass
         + "\" value=\""
         + name
         + "\">";
   }

   private String setPressButton(String bypass, String name, int width, int height) {
      return "<button width="
         + width
         + " height="
         + height
         + " back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h ."
         + bypass
         + "\" value=\""
         + name
         + "\">";
   }

   private String setTextBox(String name, int width, int height) {
      return "<edit var=\"" + name + "\" width=" + width + " height=" + height + ">";
   }

   private String setIcon(String src) {
      return this.setIcon(src, 32, 32);
   }

   private String setIcon(String src, int width, int height) {
      return "<img src=\"" + src + "\" width=" + width + " height=" + height + ">";
   }

   public void showMainPage(Player player) {
      String page = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/autoenchant/enchant.htm");
      page = this.setTargetItem(player, player.getEnchantParams().targetItem, page);
      page = this.setEnchantItem(player, player.getEnchantParams().upgradeItem, page);
      page = this.setConfiguration(player, page);
      this.show(page, player);
   }

   public void showItemChoosePage(Player player, int item_type, int sort_type, int page_number) {
      String page = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/autoenchant/enchant_item_choose.htm");
      page = this.parseItemChoosePage(player, page, item_type, sort_type, page_number);
      this.show(page, player);
   }

   public void showResultPage(Player player, EnchantType type, Map<String, Integer> result) {
      switch(type) {
         case SCROLL: {
            String page = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/autoenchant/enchant_scroll_result.htm");
            page = page.replaceFirst(
               "%crystallized%",
               result.get("crystallized") == 1
                  ? "" + ServerStorage.getInstance().getString(player.getLang(), "Enchant.YES1") + ""
                  : "" + ServerStorage.getInstance().getString(player.getLang(), "Enchant.NO1") + ""
            );
            page = page.replaceFirst("%enchant%", "" + result.get("enchant"));
            page = page.replaceFirst("%max_enchant%", "" + result.get("maxenchant"));
            page = page.replaceFirst("%scrolls%", "" + result.get("scrolls"));
            page = page.replaceFirst("%common_scrolls%", "" + result.get("commonscrolls"));
            page = page.replaceFirst("%chance%", "" + (double)result.get("chance").intValue() / 100.0);
            page = page.replaceFirst(
               "%success%",
               result.get("success") == 1
                  ? "<font name=\"hs12\" color=\"00c500\">" + ServerStorage.getInstance().getString(player.getLang(), "Enchant.SUCCESS") + "</font>"
                  : "<font name=\"hs12\" color=\"c50000\">" + ServerStorage.getInstance().getString(player.getLang(), "Enchant.FAIL") + "</font>"
            );
            this.show(page, player);
            break;
         }
         case ATTRIBUTE: {
            String page = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/autoenchant/enchant_attribute_result.htm");
            page = page.replaceFirst("%enchant%", "" + result.get("enchant"));
            page = page.replaceFirst("%stones%", "" + result.get("stones"));
            page = page.replaceFirst("%crystals%", "" + result.get("crystals"));
            page = page.replaceFirst("%chance%", "" + (double)result.get("chance").intValue() / 100.0);
            page = page.replaceFirst(
               "%success%",
               result.get("success") == 1
                  ? "<font name=\"hs12\" color=\"00c500\">" + ServerStorage.getInstance().getString(player.getLang(), "Enchant.SUCCESS") + "</font>"
                  : "<font name=\"hs12\" color=\"c50000\">" + ServerStorage.getInstance().getString(player.getLang(), "Enchant.FAIL") + "</font>"
            );
            this.show(page, player);
         }
      }
   }

   private void show(String text, Player player) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setHtml(player, text);
      player.sendPacket(html);
   }

   public void showHelpPage(Player player) {
      String page = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/mods/autoenchant/enchant_help.htm");
      this.show(page, player);
   }

   private String setTargetItem(Player player, ItemInstance targetItem, String page) {
      if (targetItem == null) {
         page = page.replaceFirst("%item_icon%", this.setIcon("icon.weapon_long_sword_i00"));
         page = page.replaceFirst("%item_name%", this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.SELECT_ITEM"), "BCBCBC"));
         page = page.replaceFirst("%item_enchant_lvl%", this.unknown);
         page = page.replaceFirst("%item_att%", this.unknown);
         page = page.replaceFirst("%item_att_lvl%", this.unknown);
         return page.replaceFirst("%item_button%", this.setButton("", ServerStorage.getInstance().getString(player.getLang(), "Enchant.SELECTED"), 55, 32));
      } else {
         page = page.replaceFirst("%item_icon%", this.setIcon(targetItem.getItem().getIcon()));
         page = page.replaceFirst("%item_name%", this.setColor(player.getItemName(targetItem.getItem()), "BCBCBC"));
         page = page.replaceFirst("%item_enchant_lvl%", "" + this.setColor("+" + targetItem.getEnchantLevel(), "B59A75"));
         page = page.replaceFirst(
            "%item_button%",
            this.setButton("item_change 0-" + targetItem.getObjectId(), ServerStorage.getInstance().getString(player.getLang(), "Enchant.SELECTED"), 55, 32)
         );
         return this.setAttribute(player, targetItem, page);
      }
   }

   private String setEnchantItem(Player player, ItemInstance enchantItem, String page) {
      if (enchantItem == null) {
         page = page.replaceFirst("%ench_icon%", this.setIcon("icon.etc_scroll_of_enchant_weapon_i01"));
         page = page.replaceFirst("%ench_name%", this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.SELECT_ITEMS"), "BCBCBC"));
         page = page.replaceFirst("%ench_blessed_field%", this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.BLESSED"), "BCBCBC"));
         page = page.replaceFirst("%ench_blessed%", this.unknown);
         page = page.replaceFirst("%ench_type%", this.unknown);
         page = page.replaceFirst("%ench_count%", this.unknown);
         return page.replaceFirst("%ench_button%", this.setButton("", ServerStorage.getInstance().getString(player.getLang(), "Enchant.SELECTED"), 55, 32));
      } else {
         page = page.replaceFirst("%ench_icon%", this.setIcon(enchantItem.getItem().getIcon()));
         page = page.replaceFirst("%ench_name%", this.setColor(player.getItemName(enchantItem.getItem()), "BCBCBC"));
         if (EnchantUtils.getInstance().isAttribute(enchantItem)) {
            page = page.replaceFirst(
               "%ench_blessed_field%", this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ELEMENT"), "BCBCBC")
            );
            page = page.replaceFirst("%ench_blessed%", this.setAttributeById(player, enchantItem.getId()));
            page = page.replaceFirst(
               "%ench_type%",
               this.setColor(
                  EnchantUtils.getInstance().isAttributeCrystal(enchantItem)
                     ? ServerStorage.getInstance().getString(player.getLang(), "Enchant.CRYSTAL")
                     : ServerStorage.getInstance().getString(player.getLang(), "Enchant.STONE"),
                  "B59A75"
               )
            );
         }

         page = page.replaceFirst("%ench_blessed_field%", this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.BLESSED"), "BCBCBC"));
         page = page.replaceFirst(
            "%ench_blessed%",
            EnchantUtils.getInstance().isBlessed(enchantItem)
               ? "" + ServerStorage.getInstance().getString(player.getLang(), "Enchant.YES") + ""
               : "" + ServerStorage.getInstance().getString(player.getLang(), "Enchant.NO") + ""
         );
         page = page.replaceFirst(
            "%ench_type%",
            this.setColor(
               EnchantUtils.getInstance().isArmorScroll(enchantItem)
                  ? ServerStorage.getInstance().getString(player.getLang(), "Enchant.ARMOR")
                  : ServerStorage.getInstance().getString(player.getLang(), "Enchant.WEAPON"),
               "BCBCBC"
            )
         );
         page = page.replaceFirst("%ench_count%", this.setColor("" + enchantItem.getCount(), "B59A75"));
         return page.replaceFirst(
            "%ench_button%",
            this.setButton("item_change 1-" + enchantItem.getObjectId(), ServerStorage.getInstance().getString(player.getLang(), "Enchant.SELECTED"), 55, 32)
         );
      }
   }

   private String setConfiguration(Player player, String page) {
      page = page.replaceFirst(
         "%common_scrolls_for_safe%",
         player.getEnchantParams().isUseCommonScrollWhenSafe
            ? "" + ServerStorage.getInstance().getString(player.getLang(), "Enchant.YES") + ""
            : "" + ServerStorage.getInstance().getString(player.getLang(), "Enchant.NO") + ""
      );
      if (player.getEnchantParams().isUseCommonScrollWhenSafe) {
         page = page.replaceFirst("%common_scrolls_for_safe_button%", this.setButton("common_for_safe 0", "Off", 30, 22));
      } else {
         page = page.replaceFirst("%common_scrolls_for_safe_button%", this.setButton("common_for_safe 1", "On", 30, 22));
      }

      ItemInstance enchantItem = player.getEnchantParams().upgradeItem;
      if (enchantItem == null) {
         page = page.replaceFirst("%max_enchant%", this.unknown);
         page = page.replaceFirst("%upgrade_item_limit%", this.unknown);
         page = page.replaceFirst("%item_limit_button%", this.setButton("", ServerStorage.getInstance().getString(player.getLang(), "Enchant.CHANGE"), 62, 22));
         return page.replaceFirst(
            "%max_enchant_button%", this.setButton("", ServerStorage.getInstance().getString(player.getLang(), "Enchant.CHANGE"), 62, 22)
         );
      } else {
         if (!player.getEnchantParams().isChangingMaxEnchant) {
            page = page.replaceFirst(
               "%max_enchant_button%", this.setButton("max_enchant", ServerStorage.getInstance().getString(player.getLang(), "Enchant.CHANGE"), 62, 22)
            );
            if (EnchantUtils.getInstance().isAttribute(enchantItem)) {
               page = page.replaceFirst("%max_enchant%", this.setColor(player.getEnchantParams().maxEnchantAtt, "B59A75"));
            } else {
               page = page.replaceFirst("%max_enchant%", this.setColor("+" + player.getEnchantParams().maxEnchant, "B59A75"));
            }
         } else {
            page = page.replaceFirst("%max_enchant%", this.setTextBox("max_enchant", 38, 12));
            page = page.replaceFirst(
               "%max_enchant_button%",
               Matcher.quoteReplacement(
                  this.setButton("max_enchant $max_enchant", ServerStorage.getInstance().getString(player.getLang(), "Enchant.TODO"), 62, 22)
               )
            );
         }

         if (!player.getEnchantParams().isChangingUpgradeItemLimit) {
            page = page.replaceFirst("%upgrade_item_limit%", this.setColor(player.getEnchantParams().upgradeItemLimit, "B59A75"));
            page = page.replaceFirst(
               "%item_limit_button%", this.setButton("item_limit", ServerStorage.getInstance().getString(player.getLang(), "Enchant.CHANGE"), 62, 22)
            );
         } else {
            page = page.replaceFirst("%upgrade_item_limit%", this.setTextBox("upgrade_item_limit", 38, 12));
            page = page.replaceFirst(
               "%item_limit_button%",
               Matcher.quoteReplacement(
                  this.setButton("item_limit $upgrade_item_limit", ServerStorage.getInstance().getString(player.getLang(), "Enchant.TODO"), 62, 22)
               )
            );
         }

         return page;
      }
   }

   private String setAttribute(Player player, ItemInstance item, String page) {
      String attr = "";
      int power = 0;
      if (!item.isWeapon() && !item.isArmor()) {
         if (item.getItem().getElemental((byte)0) != null) {
            attr = attr + this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "f72c31");
            power += item.getItem().getElemental((byte)0).getValue();
         } else if (item.getItem().getElemental((byte)1) != null) {
            attr = attr + this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "1892ef");
            power += item.getItem().getElemental((byte)1).getValue();
         }

         if (item.getItem().getElemental((byte)2) != null) {
            attr = attr + this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "7bbebd");
            power += item.getItem().getElemental((byte)2).getValue();
         } else if (item.getItem().getElemental((byte)3) != null) {
            attr = attr + this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "298a08");
            power += item.getItem().getElemental((byte)3).getValue();
         }

         if (item.getItem().getElemental((byte)4) != null) {
            attr = attr + this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "dedfde");
            power += item.getItem().getElemental((byte)4).getValue();
         } else if (item.getItem().getElemental((byte)5) != null) {
            attr = attr + this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "9533b1");
            power += item.getItem().getElemental((byte)5).getValue();
         }
      } else if (item.getElementals() != null) {
         if (item.isWeapon()) {
            if (item.getElementals()[0].getElement() == item.getElementals()[0].getFire()) {
               attr = attr + this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "f72c31");
               power += item.getElementals()[0].getValue();
            }

            if (item.getElementals()[0].getElement() == item.getElementals()[0].getWater()) {
               attr = attr + this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "1892ef");
               power += item.getElementals()[0].getValue();
            }

            if (item.getElementals()[0].getElement() == item.getElementals()[0].getWind()) {
               attr = attr + this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "7bbebd");
               power += item.getElementals()[0].getValue();
            }

            if (item.getElementals()[0].getElement() == item.getElementals()[0].getEarth()) {
               attr = attr + this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "298a08");
               power += item.getElementals()[0].getValue();
            }

            if (item.getElementals()[0].getElement() == item.getElementals()[0].getHoly()) {
               attr = attr + this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "dedfde");
               power += item.getElementals()[0].getValue();
            }

            if (item.getElementals()[0].getElement() == item.getElementals()[0].getUnholy()) {
               attr = attr + this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "9533b1");
               power += item.getElementals()[0].getValue();
            }
         } else if (item.isArmor()) {
            for(Elementals elm : item.getElementals()) {
               if (elm.getElement() == elm.getFire()) {
                  attr = attr + this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "f72c31");
                  power += elm.getValue();
               }

               if (elm.getElement() == elm.getWater()) {
                  attr = attr + this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "1892ef");
                  power += elm.getValue();
               }

               if (elm.getElement() == elm.getWind()) {
                  attr = attr + this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "7bbebd");
                  power += elm.getValue();
               }

               if (elm.getElement() == elm.getEarth()) {
                  attr = attr + this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "298a08");
                  power += elm.getValue();
               }

               if (elm.getElement() == elm.getHoly()) {
                  attr = attr + this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "dedfde");
                  power += elm.getValue();
               }

               if (elm.getElement() == elm.getUnholy()) {
                  attr = attr + this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "9533b1");
                  power += elm.getValue();
               }
            }
         }
      }

      if (attr.equals("")) {
         attr = this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.NO1"), "B59A75");
      }

      page = page.replaceFirst("%item_att%", attr);
      return page.replaceFirst("%item_att_lvl%", "" + power);
   }

   private String setAttributeById(Player player, int id) {
      if (id == 9546 || id == 9552) {
         return this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "f72c31");
      } else if (id == 9547 || id == 9553) {
         return this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "1892ef");
      } else if (id == 9548 || id == 9554) {
         return this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "298a08");
      } else if (id == 9549 || id == 9555) {
         return this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "7bbebd");
      } else if (id == 9550 || id == 9556) {
         return this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "9533b1");
      } else {
         return id != 9551 && id != 9557 ? "" : this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.ICON"), "dedfde");
      }
   }

   private String getPageButtons(int count, int type, int sort_type, int activePage) {
      String buttons = "<br><center><table><tr>";

      for(int i = 1; i <= count; ++i) {
         if (i == activePage) {
            buttons = buttons + "<td>" + this.setPressButton("item_choose " + type + "-" + sort_type + "-" + i, "" + i, 22, 22) + "</td>";
         } else {
            buttons = buttons + "<td>" + this.setButton("item_choose " + type + "-" + sort_type + "-" + i, "" + i, 22, 22) + "</td>";
         }
      }

      return buttons + "</tr></table></center>";
   }

   private String parseItemChoosePage(Player player, String page, int itemType, int sort_type, int page_index) {
      String content;
      StringBuilder template;
      List<ItemInstance> items;
      int itemsOnPage = 3;
      content = "";
      template = new StringBuilder(
         HtmCache.getInstance()
            .getHtm(
               player,
               player.getLang(),
               itemType == 0 ? "data/html/mods/autoenchant/enchant_item_obj.htm" : "data/html/mods/autoenchant/enchant_upgrade_item_obj.htm"
            )
      );
      items = EnchantUtils.getInstance().getWeapon(player);
      label62:
      switch(itemType) {
         case 0:
            switch(sort_type) {
               case 0:
                  items = EnchantUtils.getInstance().getWeapon(player);
                  break label62;
               case 1:
                  items = EnchantUtils.getInstance().getArmor(player);
                  break label62;
               case 2:
                  items = EnchantUtils.getInstance().getJewelry(player);
               default:
                  break label62;
            }
         case 1:
            if (sort_type == 0 && !Config.ENCHANT_ALLOW_SCROLLS) {
               ++sort_type;
            }

            if (sort_type == 1 && !Config.ENCHANT_ALLOW_ATTRIBUTE) {
               if (Config.ENCHANT_ALLOW_SCROLLS) {
                  --sort_type;
               } else {
                  ++sort_type;
               }
            }

            switch(sort_type) {
               case 0:
                  items = EnchantUtils.getInstance().getScrolls(player);
                  break;
               case 1:
                  items = EnchantUtils.getInstance().getAtributes(player);
            }
      }

      content = content + this.getMenuButtons(player, sort_type, itemType);
      StringBuilder parsed_items = new StringBuilder();
      int page_count = (items.size() + 3) / 3;
      if (items.size() > 3) {
         if (page_index > page_count) {
            page_index = page_count;
         }

         int i = page_index * 3 - 3;

         for(int startIdx = i; i < items.size() && i < startIdx + 3; ++i) {
            if (itemType == 0) {
               parsed_items.append(this.setTargetItem(player, items.get(i), template.toString()));
            } else {
               parsed_items.append(this.setEnchantItem(player, items.get(i), template.toString()));
            }
         }

         parsed_items.append((CharSequence)(new StringBuilder(this.getPageButtons(page_count, itemType, sort_type, page_index))));
      } else {
         for(ItemInstance item : items) {
            if (itemType == 0) {
               parsed_items.append((CharSequence)(new StringBuilder(this.setTargetItem(player, item, template.toString()))));
            } else {
               parsed_items.append((CharSequence)(new StringBuilder(this.setEnchantItem(player, item, template.toString()))));
            }
         }
      }

      if (parsed_items.toString().equals("")) {
         parsed_items.append(
            (CharSequence)(new StringBuilder(
               this.setCenter(this.setColor(ServerStorage.getInstance().getString(player.getLang(), "Enchant.NO_AVALIABLE"), "BCBCBC"))
            ))
         );
      }

      content = content + parsed_items;
      return page.replaceFirst("%content%", content);
   }

   private String getMenuButtons(Player player, int activeButton, int item_type) {
      String buttons = "<center><table border=0><tr>";
      int summaryWidth = 240;
      int height = 25;
      String[][] itemButtons = new String[][]{
         {
               ServerStorage.getInstance().getString(player.getLang(), "Enchant.WEAPON"),
               ServerStorage.getInstance().getString(player.getLang(), "Enchant.ARMOR"),
               ServerStorage.getInstance().getString(player.getLang(), "Enchant.JEWEL")
         },
         {
               Config.ENCHANT_ALLOW_SCROLLS ? ServerStorage.getInstance().getString(player.getLang(), "Enchant.ENCHANT_SCROLL") : "unallowed",
               Config.ENCHANT_ALLOW_ATTRIBUTE ? ServerStorage.getInstance().getString(player.getLang(), "Enchant.ENCHANT_ATT") : "unallowed"
         }
      };

      for(int i = 0; i < itemButtons[item_type].length; ++i) {
         if (!itemButtons[item_type][i].equals("unallowed")) {
            if (i == activeButton) {
               buttons = buttons
                  + "<td>"
                  + this.setPressButton("item_choose " + item_type + "-" + i + "-1", itemButtons[item_type][i], 240 / itemButtons[item_type].length, 25)
                  + "</td>";
            } else {
               buttons = buttons
                  + "<td>"
                  + this.setButton("item_choose " + item_type + "-" + i + "-1", itemButtons[item_type][i], 240 / itemButtons[item_type].length, 25)
                  + "</td>";
            }
         }
      }

      return buttons + "</tr></table></center>";
   }
}
