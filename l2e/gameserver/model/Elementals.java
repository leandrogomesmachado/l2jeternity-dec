package l2e.gameserver.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.funcs.FuncAdd;
import l2e.gameserver.model.skills.funcs.LambdaConst;
import l2e.gameserver.model.stats.Stats;

public final class Elementals {
   private static final Map<Integer, Elementals.ElementalItems> TABLE = new HashMap<>();
   public static final byte NONE = -1;
   public static final byte FIRE = 0;
   public static final byte WATER = 1;
   public static final byte WIND = 2;
   public static final byte EARTH = 3;
   public static final byte HOLY = 4;
   public static final byte DARK = 5;
   public static final int FIRST_WEAPON_BONUS = 20;
   public static final int NEXT_WEAPON_BONUS = 5;
   public static final int ARMOR_BONUS = 6;
   public static final int[] WEAPON_VALUES;
   public static final int[] ARMOR_VALUES;
   private Elementals.ElementalStatBoni _boni = null;
   private byte _element = -1;
   private int _value = 0;

   public static byte getItemElement(int itemId) {
      Elementals.ElementalItems item = TABLE.get(itemId);
      return item != null ? item._element : -1;
   }

   public static Elementals.ElementalItems getItemElemental(int itemId) {
      return TABLE.get(itemId);
   }

   public static int getMaxElementLevel(int itemId) {
      Elementals.ElementalItems item = TABLE.get(itemId);
      return item != null ? item._type._maxLevel : -1;
   }

   public static byte getElementById(int id) {
      switch(id) {
         case 0:
            return 0;
         case 1:
            return 1;
         case 2:
            return 2;
         case 3:
            return 3;
         case 4:
            return 4;
         case 5:
            return 5;
         default:
            return -1;
      }
   }

   public static byte getReverseElement(byte element) {
      switch(element) {
         case 0:
            return 1;
         case 1:
            return 0;
         case 2:
            return 3;
         case 3:
            return 2;
         case 4:
            return 5;
         case 5:
            return 4;
         default:
            return -1;
      }
   }

   public static String getElementName(byte element) {
      switch(element) {
         case 0:
            return "Fire";
         case 1:
            return "Water";
         case 2:
            return "Wind";
         case 3:
            return "Earth";
         case 4:
            return "Holy";
         case 5:
            return "Dark";
         default:
            return "None";
      }
   }

   public static byte getElementId(String name) {
      String tmp = name.toLowerCase();
      if (tmp.equals("fire")) {
         return 0;
      } else if (tmp.equals("water")) {
         return 1;
      } else if (tmp.equals("wind")) {
         return 2;
      } else if (tmp.equals("earth")) {
         return 3;
      } else if (tmp.equals("dark")) {
         return 5;
      } else {
         return (byte)(tmp.equals("holy") ? 4 : -1);
      }
   }

   public static byte getOppositeElement(byte element) {
      return (byte)(element % 2 == 0 ? element + 1 : element - 1);
   }

   public byte getElement() {
      return this._element;
   }

   public byte getFire() {
      return 0;
   }

   public byte getWater() {
      return 1;
   }

   public byte getWind() {
      return 2;
   }

   public byte getEarth() {
      return 3;
   }

   public int getHoly() {
      return 4;
   }

   public int getUnholy() {
      return 5;
   }

   public void setElement(byte type) {
      this._element = type;
      this._boni.setElement(type);
   }

   public int getValue() {
      return this._value;
   }

   public void setValue(int val) {
      this._value = val;
      this._boni.setValue(val);
   }

   @Override
   public String toString() {
      return getElementName(this._element) + " +" + this._value;
   }

   public Elementals(byte type, int value) {
      this._element = type;
      this._value = value;
      this._boni = new Elementals.ElementalStatBoni(this._element, this._value);
   }

   public void applyBonus(Player player, boolean isArmor) {
      this._boni.applyBonus(player, isArmor);
   }

   public void removeBonus(Player player) {
      this._boni.removeBonus(player);
   }

   public void updateBonus(Player player, boolean isArmor) {
      this._boni.removeBonus(player);
      this._boni.applyBonus(player, isArmor);
   }

   static {
      for(Elementals.ElementalItems item : Elementals.ElementalItems.values()) {
         TABLE.put(item._itemId, item);
      }

      WEAPON_VALUES = new int[]{0, 25, 75, 150, 175, 225, 300, 325, 375, 450, 475, 525, 600, Integer.MAX_VALUE};
      ARMOR_VALUES = new int[]{0, 12, 30, 60, 72, 90, 120, 132, 150, 180, 192, 210, 240, Integer.MAX_VALUE};
   }

   public static enum Elemental {
      FIRE(0),
      WATER(1),
      WIND(2),
      EARTH(3),
      HOLY(4),
      UNHOLY(5),
      NONE(-1);

      public static final Elementals.Elemental[] VALUES = Arrays.copyOf(values(), 6);
      private int _id;

      private Elemental(int id) {
         this._id = id;
      }

      public int getId() {
         return this._id;
      }
   }

   public static enum ElementalItemType {
      Stone(3),
      Roughore(3),
      Crystal(6),
      Jewel(9),
      Energy(12);

      public int _maxLevel;

      private ElementalItemType(int maxLvl) {
         this._maxLevel = maxLvl;
      }
   }

   public static enum ElementalItems {
      fireStone((byte)0, 9546, Elementals.ElementalItemType.Stone),
      waterStone((byte)1, 9547, Elementals.ElementalItemType.Stone),
      windStone((byte)2, 9549, Elementals.ElementalItemType.Stone),
      earthStone((byte)3, 9548, Elementals.ElementalItemType.Stone),
      divineStone((byte)4, 9551, Elementals.ElementalItemType.Stone),
      darkStone((byte)5, 9550, Elementals.ElementalItemType.Stone),
      fireRoughtore((byte)0, 10521, Elementals.ElementalItemType.Roughore),
      waterRoughtore((byte)1, 10522, Elementals.ElementalItemType.Roughore),
      windRoughtore((byte)2, 10524, Elementals.ElementalItemType.Roughore),
      earthRoughtore((byte)3, 10523, Elementals.ElementalItemType.Roughore),
      divineRoughtore((byte)4, 10526, Elementals.ElementalItemType.Roughore),
      darkRoughtore((byte)5, 10525, Elementals.ElementalItemType.Roughore),
      fireCrystal((byte)0, 9552, Elementals.ElementalItemType.Crystal),
      waterCrystal((byte)1, 9553, Elementals.ElementalItemType.Crystal),
      windCrystal((byte)2, 9555, Elementals.ElementalItemType.Crystal),
      earthCrystal((byte)3, 9554, Elementals.ElementalItemType.Crystal),
      divineCrystal((byte)4, 9557, Elementals.ElementalItemType.Crystal),
      darkCrystal((byte)5, 9556, Elementals.ElementalItemType.Crystal),
      fireJewel((byte)0, 9558, Elementals.ElementalItemType.Jewel),
      waterJewel((byte)1, 9559, Elementals.ElementalItemType.Jewel),
      windJewel((byte)2, 9561, Elementals.ElementalItemType.Jewel),
      earthJewel((byte)3, 9560, Elementals.ElementalItemType.Jewel),
      divineJewel((byte)4, 9563, Elementals.ElementalItemType.Jewel),
      darkJewel((byte)5, 9562, Elementals.ElementalItemType.Jewel),
      fireEnergy((byte)0, 9564, Elementals.ElementalItemType.Energy),
      waterEnergy((byte)1, 9565, Elementals.ElementalItemType.Energy),
      windEnergy((byte)2, 9567, Elementals.ElementalItemType.Energy),
      earthEnergy((byte)3, 9566, Elementals.ElementalItemType.Energy),
      divineEnergy((byte)4, 9569, Elementals.ElementalItemType.Energy),
      darkEnergy((byte)5, 9568, Elementals.ElementalItemType.Energy);

      public byte _element;
      public int _itemId;
      public Elementals.ElementalItemType _type;

      private ElementalItems(byte element, int itemId, Elementals.ElementalItemType type) {
         this._element = element;
         this._itemId = itemId;
         this._type = type;
      }
   }

   public static class ElementalStatBoni {
      private byte _elementalType;
      private int _elementalValue;
      private boolean _active;

      public ElementalStatBoni(byte type, int value) {
         this._elementalType = type;
         this._elementalValue = value;
         this._active = false;
      }

      public void applyBonus(Player player, boolean isArmor) {
         if (!this._active) {
            switch(this._elementalType) {
               case 0:
                  player.addStatFunc(new FuncAdd(isArmor ? Stats.FIRE_RES : Stats.FIRE_POWER, 64, this, new LambdaConst((double)this._elementalValue)));
                  break;
               case 1:
                  player.addStatFunc(new FuncAdd(isArmor ? Stats.WATER_RES : Stats.WATER_POWER, 64, this, new LambdaConst((double)this._elementalValue)));
                  break;
               case 2:
                  player.addStatFunc(new FuncAdd(isArmor ? Stats.WIND_RES : Stats.WIND_POWER, 64, this, new LambdaConst((double)this._elementalValue)));
                  break;
               case 3:
                  player.addStatFunc(new FuncAdd(isArmor ? Stats.EARTH_RES : Stats.EARTH_POWER, 64, this, new LambdaConst((double)this._elementalValue)));
                  break;
               case 4:
                  player.addStatFunc(new FuncAdd(isArmor ? Stats.HOLY_RES : Stats.HOLY_POWER, 64, this, new LambdaConst((double)this._elementalValue)));
                  break;
               case 5:
                  player.addStatFunc(new FuncAdd(isArmor ? Stats.DARK_RES : Stats.DARK_POWER, 64, this, new LambdaConst((double)this._elementalValue)));
            }

            this._active = true;
         }
      }

      public void removeBonus(Player player) {
         if (this._active) {
            player.removeStatsOwner(this);
            this._active = false;
         }
      }

      public void setValue(int val) {
         this._elementalValue = val;
      }

      public void setElement(byte type) {
         this._elementalType = type;
      }
   }
}
