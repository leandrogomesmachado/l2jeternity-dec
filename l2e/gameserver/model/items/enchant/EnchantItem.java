package l2e.gameserver.model.items.enchant;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.stats.StatsSet;

public class EnchantItem {
   protected static final Logger _log = Logger.getLogger(EnchantItem.class.getName());
   private final int _id;
   private final boolean _isWeapon;
   private final int _grade;
   private final int _maxEnchantLevel;
   private final double _bonusRate;
   private List<Integer> _itemIds;

   public EnchantItem(StatsSet set) {
      this._id = set.getInteger("id");
      this._isWeapon = set.getBool("isWeapon", true);
      this._grade = ItemsParser._crystalTypes.get(set.getString("targetGrade", "none"));
      this._maxEnchantLevel = set.getInteger("maxEnchant", 65535);
      this._bonusRate = set.getDouble("bonusRate", 0.0);
   }

   public final int getId() {
      return this._id;
   }

   public final double getBonusRate() {
      return this._bonusRate;
   }

   public void addItem(int id) {
      if (this._itemIds == null) {
         this._itemIds = new ArrayList<>();
      }

      this._itemIds.add(id);
   }

   public final boolean isValid(ItemInstance enchantItem) {
      if (enchantItem == null) {
         return false;
      } else if (enchantItem.isEnchantable() == 0) {
         return false;
      } else if (!this.isValidItemType(enchantItem.getItem().getType2())) {
         return false;
      } else if (this._maxEnchantLevel != 0 && enchantItem.getEnchantLevel() >= this._maxEnchantLevel) {
         return false;
      } else if (this._grade != enchantItem.getItem().getItemGradeSPlus()) {
         return false;
      } else {
         return this._itemIds == null || this._itemIds.contains(enchantItem.getId());
      }
   }

   private boolean isValidItemType(int type2) {
      if (type2 == 0) {
         return this._isWeapon;
      } else if (type2 != 1 && type2 != 2) {
         return false;
      } else {
         return !this._isWeapon;
      }
   }
}
