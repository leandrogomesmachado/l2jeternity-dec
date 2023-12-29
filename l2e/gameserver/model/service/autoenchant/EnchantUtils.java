package l2e.gameserver.model.service.autoenchant;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.type.EtcItemType;
import l2e.gameserver.model.strings.server.ServerMessage;

public class EnchantUtils {
   private static EnchantUtils instance;

   public static EnchantUtils getInstance() {
      return instance == null ? (instance = new EnchantUtils()) : instance;
   }

   public boolean isAttributeStone(ItemInstance item) {
      int[] stones = new int[]{9547, 9548, 9549, 9550, 9551, 9546};

      for(int id : stones) {
         if (id == item.getId()) {
            return true;
         }
      }

      return false;
   }

   public boolean isAttributeCrystal(ItemInstance item) {
      int[] crystals = new int[]{9552, 9553, 9554, 9555, 9556, 9557};

      for(int id : crystals) {
         if (id == item.getId()) {
            return true;
         }
      }

      return false;
   }

   public boolean isAttribute(ItemInstance item) {
      return this.isAttributeCrystal(item) || this.isAttributeStone(item);
   }

   public boolean isEnchantScroll(ItemInstance item) {
      if (item.getEtcItem() == null) {
         return false;
      } else {
         return item.getEtcItem().getHandlerName() == null ? false : item.getEtcItem().getHandlerName().equals("EnchantScrolls");
      }
   }

   public boolean isBlessed(ItemInstance item) {
      return item.getItem().getItemType() == EtcItemType.BLESS_SCRL_ENCHANT_WP || item.getItem().getItemType() == EtcItemType.BLESS_SCRL_ENCHANT_AM;
   }

   public boolean isArmorScroll(ItemInstance item) {
      return item.getItem().getItemType() == EtcItemType.SCRL_ENCHANT_AM || item.getItem().getItemType() == EtcItemType.BLESS_SCRL_ENCHANT_AM;
   }

   public List<ItemInstance> getWeapon(Player player) {
      List<ItemInstance> weapon = new ArrayList<>();

      for(ItemInstance item : player.getInventory().getItems()) {
         if (item.isWeapon() && item.getItem().getCrystalType() != 0 && item.isEnchantable() != 0) {
            weapon.add(item);
         }
      }

      return weapon;
   }

   public List<ItemInstance> getArmor(Player player) {
      List<ItemInstance> armor = new ArrayList<>();

      for(ItemInstance item : player.getInventory().getItems()) {
         if ((item.isArmor() || item.getItem().getBodyPart() == 25 && Config.ENCHANT_ALLOW_BELTS)
            && item.getItem().getCrystalType() != 0
            && item.isEnchantable() != 0) {
            armor.add(item);
         }
      }

      return armor;
   }

   public List<ItemInstance> getJewelry(Player player) {
      List<ItemInstance> jewelry = new ArrayList<>();

      for(ItemInstance item : player.getInventory().getItems()) {
         if (item.isJewel() && item.getItem().getCrystalType() != 0 && item.isEnchantable() != 0) {
            jewelry.add(item);
         }
      }

      return jewelry;
   }

   public List<ItemInstance> getAtributes(Player player) {
      List<ItemInstance> stones = new ArrayList<>();

      for(ItemInstance item : player.getInventory().getItems()) {
         if (this.isAttribute(item)) {
            stones.add(item);
         }
      }

      return stones;
   }

   public List<ItemInstance> getScrolls(Player player) {
      List<ItemInstance> scrolls = new ArrayList<>();

      for(ItemInstance item : player.getInventory().getItems()) {
         if (this.isEnchantScroll(item)) {
            scrolls.add(item);
         }
      }

      return scrolls;
   }

   public void enchant(Player player) {
      if (player != null) {
         ItemInstance upgradeItem = player.getEnchantParams().upgradeItem;
         if (upgradeItem == null) {
            player.sendMessage(new ServerMessage("Enchant.SELECT_SCROLL", player.getLang()).toString());
         } else if (player.getEnchantParams().targetItem == null) {
            player.sendMessage(new ServerMessage("Enchant.SELECT_SCROLL", player.getLang()).toString());
         } else {
            if (this.isEnchantScroll(upgradeItem)) {
               ThreadPoolManager.getInstance().execute(new EnchantByScrollTask(player));
            } else if (this.isAttribute(upgradeItem)) {
               ThreadPoolManager.getInstance().execute(new EnchantByAttributeTask(player));
            }
         }
      }
   }

   public ItemInstance getUnsafeEnchantScroll(Player player, ItemInstance item) {
      int scrollId = 0;
      ItemInstance scroll = null;
      int type = item.getItem().getCrystalType();
      if (item.isWeapon()) {
         if (type == 1) {
            scrollId = 955;
         } else if (type == 2) {
            scrollId = 951;
         } else if (type == 3) {
            scrollId = 947;
         } else if (type == 4) {
            scrollId = 729;
         } else if (type == 5 || type == 6 || type == 7) {
            scrollId = 959;
         }
      } else if (item.isArmor() || item.isJewel()) {
         if (type == 1) {
            scrollId = 956;
         } else if (type == 2) {
            scrollId = 952;
         } else if (type == 3) {
            scrollId = 948;
         } else if (type == 4) {
            scrollId = 730;
         } else if (type == 5 || type == 6 || type == 7) {
            scrollId = 960;
         }
      }

      if (scrollId != 0) {
         scroll = player.getInventory().getItemByItemId(scrollId);
      }

      return scroll;
   }
}
