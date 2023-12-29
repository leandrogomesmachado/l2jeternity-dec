package l2e.gameserver.handler.communityhandlers.impl.model;

import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.FoundationParser;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.strings.server.ServerStorage;

public class ForgeElement {
   public static String[] generateAttribution(ItemInstance item, int slot, Player player, boolean hasBonus) {
      String[] data = new String[4];
      String noicon = "icon.NOIMAGE";
      String slotclose = "L2UI_CT1.ItemWindow_DF_SlotBox_Disable";
      String dot = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.EMPTY") + "";
      String immposible = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ATTR_POSIBLE") + "";
      String maxenchant = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.CANT_ATTR") + "";
      String heronot = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.CANT_HERO_ITEM_ATTR") + "";
      String picenchant = "l2ui_ch3.multisell_plusicon";
      if (item != null) {
         String name = player.getItemName(item.getItem());
         if (name.length() > 24) {
            name = name.substring(0, 24) + "...";
         }

         data[0] = item.getItem().getIcon();
         data[1] = name + " " + (item.getEnchantLevel() > 0 ? "+" + item.getEnchantLevel() : "");
         if (itemCheckGrade(hasBonus, item)) {
            if (item.isHeroItem()) {
               data[2] = heronot;
               data[3] = "L2UI_CT1.ItemWindow_DF_SlotBox_Disable";
            } else if ((
                  !item.isArmor()
                     || item.getElementals() == null
                     || item.getElementals().length < 3
                     || item.getElementals()[0].getValue() < Config.BBS_FORGE_ARMOR_ATTRIBUTE_MAX
               )
               && (!item.isWeapon() || item.getElementals() == null || item.getElementals()[0].getValue() < Config.BBS_FORGE_WEAPON_ATTRIBUTE_MAX)
               && !item.getItem().isAccessory()
               && !item.getItem().isShield()) {
               data[2] = "<button action=\"bypass _bbsforge:attribute:item:"
                  + slot
                  + "\" value=\""
                  + ""
                  + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.INSERT_ATTRIBUTE")
                  + ""
                  + "\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
               data[3] = "l2ui_ch3.multisell_plusicon";
            } else {
               data[2] = maxenchant;
               data[3] = "L2UI_CT1.ItemWindow_DF_SlotBox_Disable";
            }
         } else {
            data[2] = immposible;
            data[3] = "L2UI_CT1.ItemWindow_DF_SlotBox_Disable";
         }
      } else {
         data[0] = "icon.NOIMAGE";
         data[1] = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.SLOT_NOT_CLOSED_" + slot + "") + "";
         data[2] = dot;
         data[3] = "L2UI_CT1.ItemWindow_DF_SlotBox_Disable";
      }

      return data;
   }

   public static String[] generateEnchant(ItemInstance item, int max, int slot, Player player) {
      String[] data = new String[4];
      String noicon = "icon.NOIMAGE";
      String slotclose = "L2UI_CT1.ItemWindow_DF_SlotBox_Disable";
      String dot = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.EMPTY") + "";
      String maxenchant = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ENCHANT_MAX") + "";
      String picenchant = "l2ui_ch3.multisell_plusicon";
      if (item != null) {
         String name = player.getItemName(item.getItem());
         if (name.length() > 24) {
            name = name.substring(0, 24) + "...";
         }

         data[0] = item.getItem().getIcon();
         data[1] = name + " " + (item.getEnchantLevel() > 0 ? "+" + item.getEnchantLevel() : "");
         if (!item.getItem().isArrow()) {
            if (item.getEnchantLevel() >= max) {
               data[2] = maxenchant;
               data[3] = "L2UI_CT1.ItemWindow_DF_SlotBox_Disable";
            } else {
               data[2] = "<button action=\"bypass _bbsforge:enchant:item:"
                  + slot
                  + "\" value=\""
                  + ""
                  + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.ENCHANT")
                  + ""
                  + "\"width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
               data[3] = "l2ui_ch3.multisell_plusicon";
            }
         } else {
            data[2] = dot;
            data[3] = "L2UI_CT1.ItemWindow_DF_SlotBox_Disable";
         }
      } else {
         data[0] = "icon.NOIMAGE";
         data[1] = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.SLOT_NOT_CLOSED_" + slot + "") + "";
         data[2] = dot;
         data[3] = "L2UI_CT1.ItemWindow_DF_SlotBox_Disable";
      }

      return data;
   }

   public static String[] generateFoundation(ItemInstance item, int slot, Player player) {
      String[] data = new String[4];
      String noicon = "icon.NOIMAGE";
      String slotclose = "L2UI_CT1.ItemWindow_DF_SlotBox_Disable";
      String dot = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.EMPTY") + "";
      String no = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.CANT_CHANGE") + "";
      String picenchant = "l2ui_ch3.multisell_plusicon";
      if (item != null) {
         String name = player.getItemName(item.getItem());
         if (name.length() > 24) {
            name = name.substring(0, 24) + "...";
         }

         data[0] = item.getItem().getIcon();
         data[1] = name + " " + (item.getEnchantLevel() > 0 ? "+" + item.getEnchantLevel() : "");
         if (!item.getItem().isArrow()) {
            int found = FoundationParser.getInstance().getFoundation(item.getId());
            if (found == -1) {
               data[2] = no;
               data[3] = "L2UI_CT1.ItemWindow_DF_SlotBox_Disable";
            } else {
               data[2] = "<button action=\"bypass _bbsforge:foundation:item:"
                  + slot
                  + "\" value=\""
                  + ""
                  + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.EXCHANGE")
                  + ""
                  + "\"width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\">";
               data[3] = "l2ui_ch3.multisell_plusicon";
            }
         } else {
            data[2] = dot;
            data[3] = "L2UI_CT1.ItemWindow_DF_SlotBox_Disable";
         }
      } else {
         data[0] = "icon.NOIMAGE";
         data[1] = "" + ServerStorage.getInstance().getString(player.getLang(), "ServiceBBS.SLOT_NOT_CLOSED_" + slot + "") + "";
         data[2] = dot;
         data[3] = "L2UI_CT1.ItemWindow_DF_SlotBox_Disable";
      }

      return data;
   }

   public static String page(Player player) {
      return HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/forge/page_template.htm");
   }

   public static boolean itemCheckGrade(boolean hasBonus, ItemInstance item) {
      int grade = item.getItem().getCrystalType();
      switch(grade) {
         case 5:
            return hasBonus;
         case 6:
            return hasBonus;
         case 7:
            return hasBonus;
         default:
            return false;
      }
   }

   public static boolean canEnchantArmorAttribute(int attr, ItemInstance item) {
      byte opositeElement = Elementals.getOppositeElement((byte)attr);
      if (item.getElementals() != null) {
         for(Elementals elm : item.getElementals()) {
            if (elm.getElement() == opositeElement) {
               return false;
            }
         }
      }

      return true;
   }
}
