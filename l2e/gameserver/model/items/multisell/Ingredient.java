package l2e.gameserver.model.items.multisell;

import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.Augmentation;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.actor.templates.items.Armor;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.stats.StatsSet;

public class Ingredient {
   private int _itemId;
   private long _itemCount;
   private final int _enchantmentLevel;
   private final int _timeLimit;
   private Augmentation _augmentation = null;
   private Elementals[] _elementals = null;
   private boolean _isTaxIngredient;
   private boolean _maintainIngredient;
   private Item _template = null;
   private ItemInfo _itemInfo = null;

   public Ingredient(StatsSet set) {
      this(
         set.getInteger("id"),
         set.getLong("count"),
         set.getInteger("enchantmentLevel", 0),
         set.getInteger("timeLimit", -1),
         generateAugmentation(set.getString("augmentation", null), set.getInteger("id")),
         generateElementals(set.getString("elementals", null), set.getInteger("id")),
         set.getBool("isTaxIngredient", false),
         set.getBool("maintainIngredient", false)
      );
   }

   public Ingredient(
      int itemId,
      long itemCount,
      int enchantmentLevel,
      int timeLimit,
      Augmentation augmentation,
      Elementals[] elementals,
      boolean isTaxIngredient,
      boolean maintainIngredient
   ) {
      this._itemId = itemId;
      this._itemCount = itemCount;
      this._enchantmentLevel = enchantmentLevel;
      this._timeLimit = timeLimit;
      this._isTaxIngredient = isTaxIngredient;
      this._maintainIngredient = maintainIngredient;
      if (this._itemId > 0) {
         this._template = ItemsParser.getInstance().getTemplate(this._itemId);
      }

      this._augmentation = augmentation;
      this._elementals = elementals;
   }

   private static Augmentation generateAugmentation(String augmentation, int itemId) {
      if (augmentation != null) {
         String[] aug = augmentation.split(":");
         Item template = ItemsParser.getInstance().getTemplate(itemId);
         return template.isWeapon()
               && template.getCrystalType() != 0
               && template.getCrystalType() != 1
               && !template.isHeroItem()
               && template.getId() != 13752
               && template.getId() != 13753
               && template.getId() != 13754
               && template.getId() != 13755
            ? new Augmentation((Integer.parseInt(aug[0]) << 16) + Integer.parseInt(aug[1]))
            : null;
      } else {
         return null;
      }
   }

   private static Elementals[] generateElementals(String elementals, int itemId) {
      Elementals[] elementalss = null;
      if (elementals != null) {
         Item template = ItemsParser.getInstance().getTemplate(itemId);
         String[] elements = elementals.split(";");
         if (template.isWeapon()) {
            if (template.isCommonItem() || !template.isElementable() || template.getCrystalType() < 5) {
               return null;
            }

            String[] element = elements[0].split(":");
            if (element != null) {
               int value = Integer.parseInt(element[1]);
               if (value > 300) {
                  value = 300;
               }

               if (elementalss == null) {
                  elementalss = new Elementals[]{new Elementals(Byte.parseByte(element[0]), value)};
               }
            }
         } else if (template.isArmor()) {
            if (elements.length > 3) {
               return null;
            }

            if (template.isCommonItem() || !template.isElementable() || template.getCrystalType() < 5 || template.getBodyPart() == 256) {
               return null;
            }

            for(String el : elements) {
               String[] element = el.split(":");
               if (element != null) {
                  byte et = Elementals.getReverseElement(Byte.parseByte(element[0]));
                  int value = Integer.parseInt(element[1]);
                  if (value > 120) {
                     value = 120;
                  }

                  if (elementalss == null) {
                     elementalss = new Elementals[]{new Elementals(et, value)};
                  } else {
                     for(Elementals elm : elementalss) {
                        if (elm.getElement() == Elementals.getReverseElement(et)) {
                        }
                     }

                     Elementals elm = new Elementals(et, value);
                     Elementals[] array = new Elementals[elementalss.length + 1];
                     System.arraycopy(elementalss, 0, array, 0, elementalss.length);
                     array[elementalss.length] = elm;
                     elementalss = array;
                  }
               }
            }
         }
      }

      return elementalss;
   }

   public Ingredient getCopy() {
      return new Ingredient(
         this._itemId,
         this._itemCount,
         this._enchantmentLevel,
         this._timeLimit,
         this._augmentation,
         this._elementals,
         this._isTaxIngredient,
         this._maintainIngredient
      );
   }

   public final Item getTemplate() {
      return this._template;
   }

   public final void setItemInfo(ItemInstance item) {
      this._itemInfo = new ItemInfo(item);
   }

   public final void setItemInfo(ItemInfo info) {
      this._itemInfo = info;
   }

   public final ItemInfo getItemInfo() {
      return this._itemInfo;
   }

   public final int getEnchantLevel() {
      return this._itemInfo == null ? this._enchantmentLevel : this._itemInfo.getEnchantLevel();
   }

   public final int getTimeLimit() {
      return this._itemInfo == null ? this._timeLimit : this._itemInfo.getTimeLimit();
   }

   public final int getTime() {
      return (int)(this._timeLimit > 0 ? System.currentTimeMillis() + (long)(this._timeLimit * 60) * 1000L : -1L);
   }

   public final int getAugmentationId() {
      return this._itemInfo == null ? (this._augmentation != null ? this._augmentation.getAugmentationId() : 0) : this._itemInfo.getAugmentId();
   }

   public final void setItemId(int itemId) {
      this._itemId = itemId;
   }

   public final int getId() {
      return this._itemId;
   }

   public final void setCount(long itemCount) {
      this._itemCount = itemCount;
   }

   public final long getCount() {
      return this._itemCount;
   }

   public final void setIsTaxIngredient(boolean isTaxIngredient) {
      this._isTaxIngredient = isTaxIngredient;
   }

   public final boolean isTaxIngredient() {
      return this._isTaxIngredient;
   }

   public final void setMaintainIngredient(boolean maintainIngredient) {
      this._maintainIngredient = maintainIngredient;
   }

   public final boolean getMaintainIngredient() {
      return this._maintainIngredient;
   }

   public final boolean isStackable() {
      return this._template == null ? true : this._template.isStackable();
   }

   public final boolean isArmorOrWeapon() {
      return this._template == null ? false : this._template instanceof Armor || this._template instanceof Weapon;
   }

   public final int getWeight() {
      return this._template == null ? 0 : this._template.getWeight();
   }

   public Elementals[] getElementals() {
      return this._elementals;
   }

   public byte getAttackElementType() {
      if (this._itemInfo == null) {
         if (this._template != null) {
            if (!this._template.isWeapon()) {
               return 0;
            }

            if (this._template.getElementals() != null) {
               return this._template.getElementals()[0].getElement();
            }

            if (this._elementals != null) {
               return this._elementals[0].getElement();
            }
         }

         return 0;
      } else {
         this._itemInfo.getElementId();
         return 0;
      }
   }

   public int getAttackElementPower() {
      if (this._itemInfo == null) {
         if (this._template != null) {
            if (!this._template.isWeapon()) {
               return 0;
            }

            if (this._template.getElementals() != null) {
               return this._template.getElementals()[0].getValue();
            }

            if (this._elementals != null) {
               return this._elementals[0].getValue();
            }
         }

         return 0;
      } else {
         return this._itemInfo.getElementPower();
      }
   }

   public int getElementDefAttr(byte element) {
      if (this._itemInfo == null) {
         if (this._template != null) {
            if (!this._template.isArmor()) {
               return 0;
            }

            if (this._template.getElementals() != null) {
               Elementals elm = this._template.getElemental(element);
               if (elm != null) {
                  return elm.getValue();
               }
            } else if (this._elementals != null) {
               Elementals elm = this.getElemental(element);
               if (elm != null) {
                  return elm.getValue();
               }
            }
         }

         return 0;
      } else {
         return this._itemInfo.getElementals()[element];
      }
   }

   public Elementals getElemental(byte attribute) {
      if (this._elementals == null) {
         return null;
      } else {
         for(Elementals elm : this._elementals) {
            if (elm.getElement() == attribute) {
               return elm;
            }
         }

         return null;
      }
   }
}
