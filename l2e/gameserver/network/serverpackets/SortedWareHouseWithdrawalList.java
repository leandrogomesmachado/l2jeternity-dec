package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import l2e.gameserver.data.parser.RecipeParser;
import l2e.gameserver.model.RecipeList;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.WarehouseItem;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.type.EtcItemType;
import l2e.gameserver.network.ServerPacketOpcodes;

public class SortedWareHouseWithdrawalList extends GameServerPacket {
   public static final int PRIVATE = 1;
   public static final int CLAN = 2;
   public static final int CASTLE = 3;
   public static final int FREIGHT = 4;
   private Player _activeChar;
   private long _playerAdena;
   private List<WarehouseItem> _objects = new ArrayList<>();
   private int _whType;
   private byte _sortorder;
   private SortedWareHouseWithdrawalList.WarehouseListType _itemtype;
   public static final byte A2Z = 1;
   public static final byte Z2A = -1;
   public static final byte GRADE = 2;
   public static final byte LEVEL = 3;
   public static final byte TYPE = 4;
   public static final byte WEAR = 5;
   public static final int MAX_SORT_LIST_ITEMS = 300;

   @Override
   protected ServerPacketOpcodes getOpcodes() {
      return ServerPacketOpcodes.WareHouseWithdrawList;
   }

   public SortedWareHouseWithdrawalList(Player player, int type, SortedWareHouseWithdrawalList.WarehouseListType itemtype, byte sortorder) {
      this._activeChar = player;
      this._whType = type;
      this._itemtype = itemtype;
      this._sortorder = sortorder;
      this._playerAdena = this._activeChar.getAdena();
      if (this._activeChar.getActiveWarehouse() == null) {
         _log.warning("error while sending withdraw request to: " + this._activeChar.getName());
      } else {
         switch(this._itemtype) {
            case WEAPON:
               this._objects = this.createWeaponList(this._activeChar.getActiveWarehouse().getItems());
               break;
            case ARMOR:
               this._objects = this.createArmorList(this._activeChar.getActiveWarehouse().getItems());
               break;
            case ETCITEM:
               this._objects = this.createEtcItemList(this._activeChar.getActiveWarehouse().getItems());
               break;
            case MATERIAL:
               this._objects = this.createMatList(this._activeChar.getActiveWarehouse().getItems());
               break;
            case RECIPE:
               this._objects = this.createRecipeList(this._activeChar.getActiveWarehouse().getItems());
               break;
            case AMULETT:
               this._objects = this.createAmulettList(this._activeChar.getActiveWarehouse().getItems());
               break;
            case SPELLBOOK:
               this._objects = this.createSpellbookList(this._activeChar.getActiveWarehouse().getItems());
               break;
            case CONSUMABLE:
               this._objects = this.createConsumableList(this._activeChar.getActiveWarehouse().getItems());
               break;
            case SHOT:
               this._objects = this.createShotList(this._activeChar.getActiveWarehouse().getItems());
               break;
            case SCROLL:
               this._objects = this.createScrollList(this._activeChar.getActiveWarehouse().getItems());
               break;
            case SEED:
               this._objects = this.createSeedList(this._activeChar.getActiveWarehouse().getItems());
               break;
            case OTHER:
               this._objects = this.createOtherList(this._activeChar.getActiveWarehouse().getItems());
               break;
            case ALL:
            default:
               this._objects = this.createAllList(this._activeChar.getActiveWarehouse().getItems());
         }

         try {
            switch(this._sortorder) {
               case -1:
               case 1:
                  Collections.sort(this._objects, new SortedWareHouseWithdrawalList.WarehouseItemNameComparator(this._sortorder));
               case 0:
               default:
                  break;
               case 2:
                  if (this._itemtype == SortedWareHouseWithdrawalList.WarehouseListType.ARMOR
                     || this._itemtype == SortedWareHouseWithdrawalList.WarehouseListType.WEAPON) {
                     Collections.sort(this._objects, new SortedWareHouseWithdrawalList.WarehouseItemNameComparator((byte)1));
                     Collections.sort(this._objects, new SortedWareHouseWithdrawalList.WarehouseItemGradeComparator((byte)1));
                  }
                  break;
               case 3:
                  if (this._itemtype == SortedWareHouseWithdrawalList.WarehouseListType.RECIPE) {
                     Collections.sort(this._objects, new SortedWareHouseWithdrawalList.WarehouseItemNameComparator((byte)1));
                     Collections.sort(this._objects, new SortedWareHouseWithdrawalList.WarehouseItemRecipeComparator(1));
                  }
                  break;
               case 4:
                  if (this._itemtype == SortedWareHouseWithdrawalList.WarehouseListType.MATERIAL) {
                     Collections.sort(this._objects, new SortedWareHouseWithdrawalList.WarehouseItemNameComparator((byte)1));
                     Collections.sort(this._objects, new SortedWareHouseWithdrawalList.WarehouseItemTypeComparator((byte)1));
                  }
                  break;
               case 5:
                  if (this._itemtype == SortedWareHouseWithdrawalList.WarehouseListType.ARMOR) {
                     Collections.sort(this._objects, new SortedWareHouseWithdrawalList.WarehouseItemNameComparator((byte)1));
                     Collections.sort(this._objects, new SortedWareHouseWithdrawalList.WarehouseItemBodypartComparator((byte)1));
                  }
            }
         } catch (Exception var6) {
         }
      }
   }

   public static byte getOrder(String order) {
      if (order == null) {
         return 1;
      } else if (order.startsWith("A2Z")) {
         return 1;
      } else if (order.startsWith("Z2A")) {
         return -1;
      } else if (order.startsWith("GRADE")) {
         return 2;
      } else if (order.startsWith("TYPE")) {
         return 4;
      } else if (order.startsWith("WEAR")) {
         return 5;
      } else {
         try {
            return Byte.parseByte(order);
         } catch (NumberFormatException var2) {
            return 1;
         }
      }
   }

   private List<WarehouseItem> createWeaponList(ItemInstance[] _items) {
      List<WarehouseItem> _list = new ArrayList<>();

      for(ItemInstance item : _items) {
         if ((
               item.isWeapon()
                  || item.getItem().getType2() == 0
                  || item.isEtcItem() && item.getItemType() == EtcItemType.ARROW
                  || item.getItem().getType2() == 4
            )
            && _list.size() < 300) {
            _list.add(new WarehouseItem(item));
         }
      }

      return _list;
   }

   private List<WarehouseItem> createArmorList(ItemInstance[] _items) {
      List<WarehouseItem> _list = new ArrayList<>();

      for(ItemInstance item : _items) {
         if ((item.isArmor() || item.getItem().getType2() == 4) && _list.size() < 300) {
            _list.add(new WarehouseItem(item));
         }
      }

      return _list;
   }

   private List<WarehouseItem> createEtcItemList(ItemInstance[] _items) {
      List<WarehouseItem> _list = new ArrayList<>();

      for(ItemInstance item : _items) {
         if ((item.isEtcItem() || item.getItem().getType2() == 4) && _list.size() < 300) {
            _list.add(new WarehouseItem(item));
         }
      }

      return _list;
   }

   private List<WarehouseItem> createMatList(ItemInstance[] _items) {
      List<WarehouseItem> _list = new ArrayList<>();

      for(ItemInstance item : _items) {
         if ((item.isEtcItem() && item.getEtcItem().getItemType() == EtcItemType.MATERIAL || item.getItem().getType2() == 4) && _list.size() < 300) {
            _list.add(new WarehouseItem(item));
         }
      }

      return _list;
   }

   private List<WarehouseItem> createRecipeList(ItemInstance[] _items) {
      List<WarehouseItem> _list = new ArrayList<>();

      for(ItemInstance item : _items) {
         if ((item.isEtcItem() && item.getEtcItem().getItemType() == EtcItemType.RECIPE || item.getItem().getType2() == 4) && _list.size() < 300) {
            _list.add(new WarehouseItem(item));
         }
      }

      return _list;
   }

   private List<WarehouseItem> createAmulettList(ItemInstance[] _items) {
      List<WarehouseItem> _list = new ArrayList<>();

      for(ItemInstance item : _items) {
         if ((item.isEtcItem() && item.getItem().getNameEn().toUpperCase().startsWith("AMULET") || item.getItem().getType2() == 4) && _list.size() < 300) {
            _list.add(new WarehouseItem(item));
         }
      }

      return _list;
   }

   private List<WarehouseItem> createSpellbookList(ItemInstance[] _items) {
      List<WarehouseItem> _list = new ArrayList<>();

      for(ItemInstance item : _items) {
         if ((item.isEtcItem() && !item.getItem().getNameEn().toUpperCase().startsWith("AMULET") || item.getItem().getType2() == 4) && _list.size() < 300) {
            _list.add(new WarehouseItem(item));
         }
      }

      return _list;
   }

   private List<WarehouseItem> createConsumableList(ItemInstance[] _items) {
      List<WarehouseItem> _list = new ArrayList<>();

      for(ItemInstance item : _items) {
         if ((
               item.isEtcItem() && (item.getEtcItem().getItemType() == EtcItemType.SCROLL || item.getEtcItem().getItemType() == EtcItemType.SHOT)
                  || item.getItem().getType2() == 4
            )
            && _list.size() < 300) {
            _list.add(new WarehouseItem(item));
         }
      }

      return _list;
   }

   private List<WarehouseItem> createShotList(ItemInstance[] _items) {
      List<WarehouseItem> _list = new ArrayList<>();

      for(ItemInstance item : _items) {
         if ((item.isEtcItem() && item.getEtcItem().getItemType() == EtcItemType.SHOT || item.getItem().getType2() == 4) && _list.size() < 300) {
            _list.add(new WarehouseItem(item));
         }
      }

      return _list;
   }

   private List<WarehouseItem> createScrollList(ItemInstance[] _items) {
      List<WarehouseItem> _list = new ArrayList<>();

      for(ItemInstance item : _items) {
         if ((item.isEtcItem() && item.getEtcItem().getItemType() == EtcItemType.SCROLL || item.getItem().getType2() == 4) && _list.size() < 300) {
            _list.add(new WarehouseItem(item));
         }
      }

      return _list;
   }

   private List<WarehouseItem> createSeedList(ItemInstance[] _items) {
      List<WarehouseItem> _list = new ArrayList<>();

      for(ItemInstance item : _items) {
         if ((item.isEtcItem() && item.getEtcItem().getItemType() == EtcItemType.SEED || item.getItem().getType2() == 4) && _list.size() < 300) {
            _list.add(new WarehouseItem(item));
         }
      }

      return _list;
   }

   private List<WarehouseItem> createOtherList(ItemInstance[] _items) {
      List<WarehouseItem> _list = new ArrayList<>();

      for(ItemInstance item : _items) {
         if ((
               item.isEtcItem()
                     && item.getEtcItem().getItemType() != EtcItemType.MATERIAL
                     && item.getEtcItem().getItemType() != EtcItemType.RECIPE
                     && item.getEtcItem().getItemType() != EtcItemType.SCROLL
                     && item.getEtcItem().getItemType() != EtcItemType.SHOT
                  || item.getItem().getType2() == 4
            )
            && _list.size() < 300) {
            _list.add(new WarehouseItem(item));
         }
      }

      return _list;
   }

   private List<WarehouseItem> createAllList(ItemInstance[] _items) {
      List<WarehouseItem> _list = new ArrayList<>();

      for(ItemInstance item : _items) {
         if (_list.size() < 300) {
            _list.add(new WarehouseItem(item));
         }
      }

      return _list;
   }

   @Override
   protected final void writeImpl() {
      this.writeH(this._whType);
      this.writeQ(this._playerAdena);
      this.writeH(this._objects.size());

      for(WarehouseItem item : this._objects) {
         this.writeD(item.getObjectId());
         this.writeD(item.getItem().getDisplayId());
         this.writeD(item.getLocationSlot());
         this.writeQ(item.getCount());
         this.writeH(item.getItem().getType2());
         this.writeH(item.getCustomType1());
         this.writeH(0);
         this.writeD(item.getItem().getBodyPart());
         this.writeH(item.getEnchantLevel());
         this.writeH(item.getCustomType2());
         if (item.isAugmented()) {
            this.writeD(item.getAugmentationId());
         } else {
            this.writeD(0);
         }

         this.writeD(item.getMana());
         this.writeD(item.getTime());
         this.writeH(item.getAttackElementType());
         this.writeH(item.getAttackElementPower());

         for(byte i = 0; i < 6; ++i) {
            this.writeH(item.getElementDefAttr(i));
         }

         for(int op : item.getEnchantOptions()) {
            this.writeH(op);
         }

         this.writeD(item.getObjectId());
      }
   }

   private static class WarehouseItemBodypartComparator implements Comparator<WarehouseItem> {
      private byte order = 0;

      protected WarehouseItemBodypartComparator(byte sortOrder) {
         this.order = sortOrder;
      }

      public int compare(WarehouseItem o1, WarehouseItem o2) {
         if (o1.getType2() == 4 && o2.getType2() != 4) {
            return this.order == 1 ? -1 : 1;
         } else if (o2.getType2() == 4 && o1.getType2() != 4) {
            return this.order == 1 ? 1 : -1;
         } else {
            Integer i1 = o1.getBodyPart();
            Integer i2 = o2.getBodyPart();
            return this.order == 1 ? i1.compareTo(i2) : i2.compareTo(i1);
         }
      }
   }

   private static class WarehouseItemGradeComparator implements Comparator<WarehouseItem> {
      byte order = 0;

      protected WarehouseItemGradeComparator(byte sortOrder) {
         this.order = sortOrder;
      }

      public int compare(WarehouseItem o1, WarehouseItem o2) {
         if (o1.getType2() == 4 && o2.getType2() != 4) {
            return this.order == 1 ? -1 : 1;
         } else if (o2.getType2() == 4 && o1.getType2() != 4) {
            return this.order == 1 ? 1 : -1;
         } else {
            Integer i1 = o1.getItemGrade();
            Integer i2 = o2.getItemGrade();
            return this.order == 1 ? i1.compareTo(i2) : i2.compareTo(i1);
         }
      }
   }

   private static class WarehouseItemNameComparator implements Comparator<WarehouseItem> {
      private byte order = 0;

      protected WarehouseItemNameComparator(byte sortOrder) {
         this.order = sortOrder;
      }

      public int compare(WarehouseItem o1, WarehouseItem o2) {
         if (o1.getType2() == 4 && o2.getType2() != 4) {
            return this.order == 1 ? -1 : 1;
         } else if (o2.getType2() == 4 && o1.getType2() != 4) {
            return this.order == 1 ? 1 : -1;
         } else {
            String s1 = o1.getName();
            String s2 = o2.getName();
            return this.order == 1 ? s1.compareTo(s2) : s2.compareTo(s1);
         }
      }
   }

   private static class WarehouseItemRecipeComparator implements Comparator<WarehouseItem> {
      private int order = 0;
      private RecipeParser rd = null;

      protected WarehouseItemRecipeComparator(int sortOrder) {
         this.order = sortOrder;
         this.rd = RecipeParser.getInstance();
      }

      public int compare(WarehouseItem o1, WarehouseItem o2) {
         if (o1.getType2() == 4 && o2.getType2() != 4) {
            return this.order == 1 ? -1 : 1;
         } else if (o2.getType2() == 4 && o1.getType2() != 4) {
            return this.order == 1 ? 1 : -1;
         } else if (o1.isEtcItem() && o1.getItemType() == EtcItemType.RECIPE && o2.isEtcItem() && o2.getItemType() == EtcItemType.RECIPE) {
            try {
               RecipeList rp1 = this.rd.getRecipeByItemId(o1.getId());
               RecipeList rp2 = this.rd.getRecipeByItemId(o2.getId());
               if (rp1 == null) {
                  return this.order == 1 ? 1 : -1;
               } else if (rp2 == null) {
                  return this.order == 1 ? -1 : 1;
               } else {
                  Integer i1 = rp1.getLevel();
                  Integer i2 = rp2.getLevel();
                  return this.order == 1 ? i1.compareTo(i2) : i2.compareTo(i1);
               }
            } catch (Exception var7) {
               return 0;
            }
         } else {
            String s1 = o1.getName();
            String s2 = o2.getName();
            return this.order == 1 ? s1.compareTo(s2) : s2.compareTo(s1);
         }
      }
   }

   private static class WarehouseItemTypeComparator implements Comparator<WarehouseItem> {
      byte order = 0;

      protected WarehouseItemTypeComparator(byte sortOrder) {
         this.order = sortOrder;
      }

      public int compare(WarehouseItem o1, WarehouseItem o2) {
         if (o1.getType2() == 4 && o2.getType2() != 4) {
            return this.order == 1 ? -1 : 1;
         } else if (o2.getType2() == 4 && o1.getType2() != 4) {
            return this.order == 1 ? 1 : -1;
         } else {
            try {
               Integer i1 = o1.getItem().getMaterialType();
               Integer i2 = o2.getItem().getMaterialType();
               return this.order == 1 ? i1.compareTo(i2) : i2.compareTo(i1);
            } catch (Exception var5) {
               return 0;
            }
         }
      }
   }

   public static enum WarehouseListType {
      WEAPON,
      ARMOR,
      ETCITEM,
      MATERIAL,
      RECIPE,
      AMULETT,
      SPELLBOOK,
      SHOT,
      SCROLL,
      CONSUMABLE,
      SEED,
      POTION,
      QUEST,
      PET,
      OTHER,
      ALL;
   }
}
