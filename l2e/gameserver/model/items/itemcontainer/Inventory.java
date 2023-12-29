package l2e.gameserver.model.items.itemcontainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.StringUtil;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ArmorSetsParser;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.handler.skillhandlers.SkillHandler;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.ArmorSetTemplate;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.type.EtcItemType;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.Stats;

public abstract class Inventory extends ItemContainer {
   protected static final Logger _log = Logger.getLogger(Inventory.class.getName());
   public static final int PAPERDOLL_UNDER = 0;
   public static final int PAPERDOLL_HEAD = 1;
   public static final int PAPERDOLL_HAIR = 2;
   public static final int PAPERDOLL_HAIR2 = 3;
   public static final int PAPERDOLL_NECK = 4;
   public static final int PAPERDOLL_RHAND = 5;
   public static final int PAPERDOLL_CHEST = 6;
   public static final int PAPERDOLL_LHAND = 7;
   public static final int PAPERDOLL_REAR = 8;
   public static final int PAPERDOLL_LEAR = 9;
   public static final int PAPERDOLL_GLOVES = 10;
   public static final int PAPERDOLL_LEGS = 11;
   public static final int PAPERDOLL_FEET = 12;
   public static final int PAPERDOLL_RFINGER = 13;
   public static final int PAPERDOLL_LFINGER = 14;
   public static final int PAPERDOLL_LBRACELET = 15;
   public static final int PAPERDOLL_RBRACELET = 16;
   public static final int PAPERDOLL_DECO1 = 17;
   public static final int PAPERDOLL_DECO2 = 18;
   public static final int PAPERDOLL_DECO3 = 19;
   public static final int PAPERDOLL_DECO4 = 20;
   public static final int PAPERDOLL_DECO5 = 21;
   public static final int PAPERDOLL_DECO6 = 22;
   public static final int PAPERDOLL_CLOAK = 23;
   public static final int PAPERDOLL_BELT = 24;
   public static final int PAPERDOLL_TOTALSLOTS = 25;
   public static final double MAX_ARMOR_WEIGHT = 12000.0;
   protected final ItemInstance[] _paperdoll = new ItemInstance[25];
   private final List<Inventory.PaperdollListener> _paperdollListeners = new ArrayList<>();
   protected int _totalWeight;
   private int _wearedMask;

   protected Inventory() {
      if (this instanceof PcInventory) {
         this.addPaperdollListener(Inventory.ArmorSetListener.getInstance());
         this.addPaperdollListener(Inventory.BowCrossRodListener.getInstance());
         this.addPaperdollListener(Inventory.ItemSkillsListener.getInstance());
         this.addPaperdollListener(Inventory.BraceletListener.getInstance());
      }

      this.addPaperdollListener(Inventory.StatsListener.getInstance());
   }

   protected abstract ItemInstance.ItemLocation getEquipLocation();

   public Inventory.ChangeRecorder newRecorder() {
      return new Inventory.ChangeRecorder(this);
   }

   public ItemInstance dropItem(String process, ItemInstance item, Player actor, Object reference) {
      if (item == null) {
         return null;
      } else {
         synchronized(item) {
            if (!this._items.contains(item)) {
               return null;
            } else {
               this.removeItem(item);
               item.setOwnerId(process, 0, actor, reference);
               item.setItemLocation(ItemInstance.ItemLocation.VOID);
               item.setLastChange(3);
               item.updateDatabase();
               this.refreshWeight();
               return item;
            }
         }
      }
   }

   public ItemInstance dropItem(String process, int objectId, long count, Player actor, Object reference) {
      ItemInstance item = this.getItemByObjectId(objectId);
      if (item == null) {
         return null;
      } else {
         synchronized(item) {
            if (!this._items.contains(item)) {
               return null;
            }

            if (item.getCount() > count) {
               item.changeCount(process, -count, actor, reference);
               item.setLastChange(2);
               item.updateDatabase();
               item = ItemsParser.getInstance().createItem(process, item.getId(), count, actor, reference);
               item.updateDatabase();
               this.refreshWeight();
               return item;
            }
         }

         return this.dropItem(process, item, actor, reference);
      }
   }

   @Override
   protected void addItem(ItemInstance item) {
      super.addItem(item);
      if (item.isEquipped()) {
         this.equipItem(item);
      }
   }

   @Override
   public boolean removeItem(ItemInstance item) {
      for(int i = 0; i < this._paperdoll.length; ++i) {
         if (this._paperdoll[i] == item) {
            this.unEquipItemInSlot(i);
         }
      }

      return super.removeItem(item);
   }

   public ItemInstance getPaperdollItem(int slot) {
      return this._paperdoll[slot];
   }

   public ItemInstance[] getPaperdollItems() {
      return this._paperdoll;
   }

   public boolean isPaperdollSlotEmpty(int slot) {
      return this._paperdoll[slot] == null;
   }

   public static int getPaperdollIndex(int slot) {
      switch(slot) {
         case 1:
            return 0;
         case 2:
            return 8;
         case 4:
         case 6:
            return 9;
         case 8:
            return 4;
         case 16:
         case 48:
            return 13;
         case 32:
            return 14;
         case 64:
            return 1;
         case 128:
         case 16384:
            return 5;
         case 256:
            return 7;
         case 512:
            return 10;
         case 1024:
         case 32768:
         case 131072:
            return 6;
         case 2048:
            return 11;
         case 4096:
            return 12;
         case 8192:
            return 23;
         case 65536:
         case 524288:
            return 2;
         case 262144:
            return 3;
         case 1048576:
            return 16;
         case 2097152:
            return 15;
         case 4194304:
            return 17;
         case 268435456:
            return 24;
         default:
            return -1;
      }
   }

   public ItemInstance getPaperdollItemByL2ItemId(int slot) {
      int index = getPaperdollIndex(slot);
      return index == -1 ? null : this._paperdoll[index];
   }

   public int getPaperdollItemId(int slot) {
      ItemInstance item = this._paperdoll[slot];
      return item != null ? item.getId() : 0;
   }

   public int getPaperdollVisualItemId(int slot) {
      ItemInstance item = this._paperdoll[slot];
      if (item != null) {
         return item.getId();
      } else {
         if (slot == 2) {
            item = this._paperdoll[3];
            if (item != null) {
               return item.getId();
            }
         }

         return 0;
      }
   }

   public int getPaperdollItemDisplayId(int slot) {
      ItemInstance item = this._paperdoll[slot];
      return item != null ? item.getDisplayId() : 0;
   }

   public int getPaperdollAugmentationId(int slot) {
      ItemInstance item = this._paperdoll[slot];
      return item != null && item.getAugmentation() != null ? item.getAugmentation().getAugmentationId() : 0;
   }

   public int getPaperdollObjectId(int slot) {
      ItemInstance item = this._paperdoll[slot];
      return item != null ? item.getObjectId() : 0;
   }

   public synchronized void addPaperdollListener(Inventory.PaperdollListener listener) {
      assert !this._paperdollListeners.contains(listener);

      this._paperdollListeners.add(listener);
   }

   public synchronized void removePaperdollListener(Inventory.PaperdollListener listener) {
      this._paperdollListeners.remove(listener);
   }

   public synchronized ItemInstance setPaperdollItem(int slot, ItemInstance item) {
      ItemInstance old = this._paperdoll[slot];
      if (old != item) {
         if (old != null) {
            this._paperdoll[slot] = null;
            old.setItemLocation(this.getBaseLocation());
            old.setLastChange(2);
            int mask = 0;

            for(int i = 0; i < 25; ++i) {
               ItemInstance pi = this._paperdoll[i];
               if (pi != null) {
                  mask |= pi.getItem().getItemMask();
               }
            }

            this._wearedMask = mask;

            for(Inventory.PaperdollListener listener : this._paperdollListeners) {
               if (listener != null) {
                  listener.notifyUnequiped(slot, old, this);
               }
            }

            old.updateDatabase();
         }

         if (item != null) {
            this._paperdoll[slot] = item;
            item.setItemLocation(this.getEquipLocation(), slot);
            item.setLastChange(2);
            this._wearedMask |= item.getItem().getItemMask();

            for(Inventory.PaperdollListener listener : this._paperdollListeners) {
               if (listener != null) {
                  listener.notifyEquiped(slot, item, this);
               }
            }

            item.updateDatabase();
         }
      }

      return old;
   }

   public int getWearedMask() {
      return this._wearedMask;
   }

   public int getSlotFromItem(ItemInstance item) {
      int slot = -1;
      int location = item.getLocationSlot();
      switch(location) {
         case 0:
            slot = 1;
            break;
         case 1:
            slot = 64;
            break;
         case 2:
            slot = 65536;
            break;
         case 3:
            slot = 262144;
            break;
         case 4:
            slot = 8;
            break;
         case 5:
            slot = 128;
            break;
         case 6:
            slot = item.getItem().getBodyPart();
            break;
         case 7:
            slot = 256;
            break;
         case 8:
            slot = 2;
            break;
         case 9:
            slot = 4;
            break;
         case 10:
            slot = 512;
            break;
         case 11:
            slot = 2048;
            break;
         case 12:
            slot = 4096;
            break;
         case 13:
            slot = 16;
            break;
         case 14:
            slot = 32;
            break;
         case 15:
            slot = 2097152;
            break;
         case 16:
            slot = 1048576;
            break;
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
            slot = 4194304;
            break;
         case 23:
            slot = 8192;
            break;
         case 24:
            slot = 268435456;
      }

      return slot;
   }

   public void unEquipItem(ItemInstance item) {
      if (item.isEquipped()) {
         this.unEquipItemInBodySlot(item.getItem().getBodyPart());
      }
   }

   public ItemInstance[] unEquipItemInBodySlotAndRecord(int slot) {
      Inventory.ChangeRecorder recorder = this.newRecorder();

      try {
         this.unEquipItemInBodySlot(slot);
      } finally {
         this.removePaperdollListener(recorder);
      }

      return recorder.getChangedItems();
   }

   public ItemInstance unEquipItemInSlot(int pdollSlot) {
      return this.setPaperdollItem(pdollSlot, null);
   }

   public ItemInstance[] unEquipItemInSlotAndRecord(int slot) {
      Inventory.ChangeRecorder recorder = this.newRecorder();

      try {
         this.unEquipItemInSlot(slot);
         if (this.getOwner().isPlayer()) {
            ((Player)this.getOwner()).refreshExpertisePenalty();
         }
      } finally {
         this.removePaperdollListener(recorder);
      }

      return recorder.getChangedItems();
   }

   public ItemInstance unEquipItemInBodySlot(int slot) {
      if (Config.DEBUG) {
         _log.info(Inventory.class.getSimpleName() + ": Unequip body slot:" + slot);
      }

      int pdollSlot = -1;
      switch(slot) {
         case 1:
            pdollSlot = 0;
            break;
         case 2:
            pdollSlot = 8;
            break;
         case 4:
            pdollSlot = 9;
            break;
         case 8:
            pdollSlot = 4;
            break;
         case 16:
            pdollSlot = 13;
            break;
         case 32:
            pdollSlot = 14;
            break;
         case 64:
            pdollSlot = 1;
            break;
         case 128:
         case 16384:
            pdollSlot = 5;
            break;
         case 256:
            pdollSlot = 7;
            break;
         case 512:
            pdollSlot = 10;
            break;
         case 1024:
         case 32768:
         case 131072:
            pdollSlot = 6;
            break;
         case 2048:
            pdollSlot = 11;
            break;
         case 4096:
            pdollSlot = 12;
            break;
         case 8192:
            pdollSlot = 23;
            break;
         case 65536:
            pdollSlot = 2;
            break;
         case 262144:
            pdollSlot = 3;
            break;
         case 524288:
            this.setPaperdollItem(2, null);
            pdollSlot = 2;
            break;
         case 1048576:
            pdollSlot = 16;
            break;
         case 2097152:
            pdollSlot = 15;
            break;
         case 4194304:
            pdollSlot = 17;
            break;
         case 268435456:
            pdollSlot = 24;
            break;
         default:
            _log.info("Unhandled slot type: " + slot);
            _log.info(StringUtil.getTraceString(Thread.currentThread().getStackTrace()));
      }

      if (pdollSlot >= 0) {
         ItemInstance old = this.setPaperdollItem(pdollSlot, null);
         if (old != null && this.getOwner().isPlayer()) {
            ((Player)this.getOwner()).refreshExpertisePenalty();
         }

         return old;
      } else {
         return null;
      }
   }

   public ItemInstance[] equipItemAndRecord(ItemInstance item) {
      Inventory.ChangeRecorder recorder = this.newRecorder();

      try {
         this.equipItem(item);
      } finally {
         this.removePaperdollListener(recorder);
      }

      return recorder.getChangedItems();
   }

   public void equipItem(ItemInstance item) {
      if (!this.getOwner().isPlayer() || ((Player)this.getOwner()).getPrivateStoreType() == 0) {
         if (this.getOwner().isPlayer()) {
            Player player = (Player)this.getOwner();
            if (!player.canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && !player.isHero() && item.isHeroItem()) {
               return;
            }
         }

         int targetSlot = item.getItem().getBodyPart();
         ItemInstance formal = this.getPaperdollItem(6);
         label88:
         if (item.getId() == 21163 || formal == null || formal.getItem().getBodyPart() != 131072) {
            switch(targetSlot) {
               case 1:
                  this.setPaperdollItem(0, item);
                  break;
               case 2:
               case 4:
               case 6:
                  if (this._paperdoll[9] == null) {
                     this.setPaperdollItem(9, item);
                  } else if (this._paperdoll[8] == null) {
                     this.setPaperdollItem(8, item);
                  } else {
                     this.setPaperdollItem(9, item);
                  }
                  break;
               case 8:
                  this.setPaperdollItem(4, item);
                  break;
               case 16:
               case 32:
               case 48:
                  if (this._paperdoll[14] == null) {
                     this.setPaperdollItem(14, item);
                  } else if (this._paperdoll[13] == null) {
                     this.setPaperdollItem(13, item);
                  } else {
                     this.setPaperdollItem(14, item);
                  }
                  break;
               case 64:
                  this.setPaperdollItem(1, item);
                  break;
               case 128:
                  this.setPaperdollItem(5, item);
                  break;
               case 256:
                  ItemInstance rh = this.getPaperdollItem(5);
                  if (rh != null
                     && rh.getItem().getBodyPart() == 16384
                     && (rh.getItemType() != WeaponType.BOW || item.getItemType() != EtcItemType.ARROW)
                     && (rh.getItemType() != WeaponType.CROSSBOW || item.getItemType() != EtcItemType.BOLT)
                     && (rh.getItemType() != WeaponType.FISHINGROD || item.getItemType() != EtcItemType.LURE)) {
                     this.setPaperdollItem(5, null);
                  }

                  this.setPaperdollItem(7, item);
                  break;
               case 512:
                  this.setPaperdollItem(10, item);
                  break;
               case 1024:
                  this.setPaperdollItem(6, item);
                  break;
               case 2048:
                  ItemInstance chest = this.getPaperdollItem(6);
                  if (chest != null && chest.getItem().getBodyPart() == 32768) {
                     this.setPaperdollItem(6, null);
                  }

                  this.setPaperdollItem(11, item);
                  break;
               case 4096:
                  this.setPaperdollItem(12, item);
                  break;
               case 8192:
                  this.setPaperdollItem(23, item);
                  break;
               case 16384:
                  this.setPaperdollItem(7, null);
                  this.setPaperdollItem(5, item);
                  break;
               case 32768:
                  this.setPaperdollItem(11, null);
                  this.setPaperdollItem(6, item);
                  break;
               case 65536:
                  ItemInstance hair = this.getPaperdollItem(2);
                  if (hair != null && hair.getItem().getBodyPart() == 524288) {
                     this.setPaperdollItem(3, null);
                  } else {
                     this.setPaperdollItem(2, null);
                  }

                  this.setPaperdollItem(2, item);
                  break;
               case 131072:
                  this.setPaperdollItem(11, null);
                  this.setPaperdollItem(7, null);
                  this.setPaperdollItem(5, null);
                  this.setPaperdollItem(5, null);
                  this.setPaperdollItem(7, null);
                  this.setPaperdollItem(1, null);
                  this.setPaperdollItem(12, null);
                  this.setPaperdollItem(10, null);
                  this.setPaperdollItem(6, item);
                  break;
               case 262144:
                  ItemInstance hair2 = this.getPaperdollItem(2);
                  if (hair2 != null && hair2.getItem().getBodyPart() == 524288) {
                     this.setPaperdollItem(2, null);
                  } else {
                     this.setPaperdollItem(3, null);
                  }

                  this.setPaperdollItem(3, item);
                  break;
               case 524288:
                  this.setPaperdollItem(3, null);
                  this.setPaperdollItem(2, item);
                  break;
               case 1048576:
                  this.setPaperdollItem(16, item);
                  break;
               case 2097152:
                  this.setPaperdollItem(15, item);
                  break;
               case 4194304:
                  this.equipTalisman(item);
                  break;
               case 268435456:
                  this.setPaperdollItem(24, item);
                  break;
               default:
                  _log.warning("Unknown body slot " + targetSlot + " for Item ID:" + item.getId());
            }
         } else {
            if (formal.getItem().isCostume()) {
               switch(targetSlot) {
                  case 64:
                  case 512:
                  case 2048:
                  case 4096:
                     return;
               }
            } else {
               switch(targetSlot) {
                  case 64:
                  case 128:
                  case 256:
                  case 512:
                  case 2048:
                  case 4096:
                  case 16384:
                     return;
               }
            }
            break label88;
         }
      }
   }

   @Override
   protected void refreshWeight() {
      long weight = 0L;

      for(ItemInstance item : this._items) {
         if (item != null && item.getItem() != null) {
            weight += (long)item.getItem().getWeight() * item.getCount();
         }
      }

      this._totalWeight = Math.max((int)Math.min(weight - (long)this.getOwner().getBonusWeightPenalty(), 2147483647L), 0);
   }

   public int getTotalWeight() {
      return this._totalWeight;
   }

   public ItemInstance findArrowForBow(Item bow) {
      if (bow == null) {
         return null;
      } else {
         ItemInstance arrow = null;

         for(ItemInstance item : this.getItems()) {
            if (item.isEtcItem() && item.getItem().getItemGradeSPlus() == bow.getItemGradeSPlus() && item.getEtcItem().getItemType() == EtcItemType.ARROW) {
               arrow = item;
               break;
            }
         }

         return arrow;
      }
   }

   public ItemInstance findBoltForCrossBow(Item crossbow) {
      ItemInstance bolt = null;

      for(ItemInstance item : this.getItems()) {
         if (item.isEtcItem() && item.getItem().getItemGradeSPlus() == crossbow.getItemGradeSPlus() && item.getEtcItem().getItemType() == EtcItemType.BOLT) {
            bolt = item;
            break;
         }
      }

      return bolt;
   }

   @Override
   public void restore() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement(
            "SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time, visual_itemId, agathion_energy FROM items WHERE owner_id=? AND (loc=? OR loc=?) ORDER BY loc_data"
         );
         statement.setInt(1, this.getOwnerId());
         statement.setString(2, this.getBaseLocation().name());
         statement.setString(3, this.getEquipLocation().name());
         ResultSet inv = statement.executeQuery();

         while(inv.next()) {
            ItemInstance item = ItemInstance.restoreFromDb(this.getOwnerId(), inv);
            if (item != null) {
               if (this.getOwner().isPlayer()) {
                  Player player = (Player)this.getOwner();
                  if (!player.canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && !player.isHero() && item.isHeroItem()) {
                     item.setItemLocation(ItemInstance.ItemLocation.INVENTORY);
                  }
               }

               World.getInstance().addObject(item);
               if (item.isStackable() && this.getItemByItemId(item.getId()) != null) {
                  this.addItem("Restore", item, this.getOwner().getActingPlayer(), null);
               } else {
                  this.addItem(item);
               }
            }
         }

         inv.close();
         statement.close();
         this.refreshWeight();
      } catch (Exception var17) {
         _log.log(Level.WARNING, "Could not restore inventory: " + var17.getMessage(), (Throwable)var17);
      }
   }

   public int getMaxTalismanCount() {
      return (int)this.getOwner().getStat().calcStat(Stats.TALISMAN_SLOTS, 0.0, null, null);
   }

   private void equipTalisman(ItemInstance item) {
      if (this.getMaxTalismanCount() != 0) {
         for(int i = 17; i < 17 + this.getMaxTalismanCount(); ++i) {
            if (this._paperdoll[i] != null && this.getPaperdollItemId(i) == item.getId()) {
               this.setPaperdollItem(i, item);
               return;
            }
         }

         for(int i = 17; i < 17 + this.getMaxTalismanCount(); ++i) {
            if (this._paperdoll[i] == null) {
               this.setPaperdollItem(i, item);
               return;
            }
         }

         this.setPaperdollItem(17, item);
      }
   }

   public int getCloakStatus() {
      return (int)this.getOwner().getStat().calcStat(Stats.CLOAK_SLOT, 0.0, null, null);
   }

   public int getHeroStatus() {
      return (int)this.getOwner().getStat().calcStat(Stats.HERO_STATUS, 0.0, null, null);
   }

   public void reloadEquippedItems() {
      for(ItemInstance item : this._paperdoll) {
         if (item != null) {
            int slot = item.getLocationSlot();

            for(Inventory.PaperdollListener listener : this._paperdollListeners) {
               if (listener != null) {
                  listener.notifyUnequiped(slot, item, this);
                  listener.notifyEquiped(slot, item, this);
               }
            }
         }
      }
   }

   private static final class ArmorSetListener implements Inventory.PaperdollListener {
      private static Inventory.ArmorSetListener instance = new Inventory.ArmorSetListener();

      public static Inventory.ArmorSetListener getInstance() {
         return instance;
      }

      @Override
      public void notifyEquiped(int slot, ItemInstance item, Inventory inventory) {
         if (inventory.getOwner().isPlayer()) {
            Player player = (Player)inventory.getOwner();
            ItemInstance chestItem = inventory.getPaperdollItem(6);
            if (chestItem != null) {
               if (player.getInventory().hasAllDressMeItemsEquipped()) {
                  player.getInventory().setMustShowDressMe(true);
                  player.broadcastCharInfo();
               }

               if (ArmorSetsParser.getInstance().isArmorSet(chestItem.getId())) {
                  ArmorSetTemplate armorSet = ArmorSetsParser.getInstance().getSet(chestItem.getId());
                  boolean update = false;
                  boolean updateTimeStamp = false;
                  if (armorSet.containItem(slot, item.getId())) {
                     if (armorSet.containAll(player)) {
                        List<SkillHolder> skills = armorSet.getSkills();
                        if (skills != null) {
                           for(SkillHolder holder : skills) {
                              Skill itemSkill = holder.getSkill();
                              if (itemSkill != null) {
                                 player.addSkill(itemSkill, false);
                                 if (itemSkill.isActive()) {
                                    if (!player.hasSkillReuse(itemSkill.getReuseHashCode())) {
                                       int equipDelay = item.getEquipReuseDelay();
                                       if (equipDelay > 0) {
                                          player.addTimeStamp(itemSkill, (long)equipDelay);
                                          player.disableSkill(itemSkill, (long)equipDelay);
                                       }
                                    }

                                    updateTimeStamp = true;
                                 }

                                 update = true;
                              } else {
                                 Inventory._log.warning("Inventory.ArmorSetListener: Incorrect skill: " + holder + ".");
                              }
                           }
                        }

                        if (armorSet.containShield(player)) {
                           for(SkillHolder holder : armorSet.getShieldSkillId()) {
                              if (holder.getSkill() != null) {
                                 player.addSkill(holder.getSkill(), false);
                                 update = true;
                              } else {
                                 Inventory._log.warning("Inventory.ArmorSetListener: Incorrect skill: " + holder + ".");
                              }
                           }
                        }

                        if (armorSet.isEnchanted6(player)) {
                           for(SkillHolder holder : armorSet.getEnchant6skillId()) {
                              if (holder.getSkill() != null) {
                                 player.addSkill(holder.getSkill(), false);
                                 update = true;
                              } else {
                                 Inventory._log.warning("Inventory.ArmorSetListener: Incorrect skill: " + holder + ".");
                              }
                           }
                        }

                        if (!armorSet.getEnchantByLevel().isEmpty()) {
                           for(int enchLvl : armorSet.getEnchantByLevel().keySet()) {
                              if (armorSet.isEnchantedByLevel(player, enchLvl)) {
                                 SkillHolder holder = armorSet.getEnchantByLevel().get(enchLvl);
                                 if (holder.getSkill() != null) {
                                    player.addSkill(holder.getSkill(), false);
                                    update = true;
                                 } else {
                                    Inventory._log.warning("Inventory.ArmorSetListener: Incorrect skill: " + holder + ".");
                                 }
                              }
                           }
                        }
                     }
                  } else if (armorSet.containShield(item.getId())) {
                     for(SkillHolder holder : armorSet.getShieldSkillId()) {
                        if (holder.getSkill() != null) {
                           player.addSkill(holder.getSkill(), false);
                           update = true;
                        } else {
                           Inventory._log.warning("Inventory.ArmorSetListener: Incorrect skill: " + holder + ".");
                        }
                     }
                  }

                  if (update) {
                     player.sendSkillList(updateTimeStamp);
                  }
               }
            }
         }
      }

      @Override
      public void notifyUnequiped(int slot, ItemInstance item, Inventory inventory) {
         if (inventory.getOwner().isPlayer()) {
            Player player = (Player)inventory.getOwner();
            boolean remove = false;
            List<SkillHolder> skills = null;
            List<SkillHolder> shieldSkill = null;
            List<SkillHolder> skillId6 = null;
            Map<Integer, SkillHolder> skillIdByLevel = null;
            if (player.getInventory().mustShowDressMe() && !player.getInventory().hasAllDressMeItemsEquipped()) {
               player.getInventory().setMustShowDressMe(false);
               player.broadcastCharInfo();
            }

            if (slot == 6) {
               if (!ArmorSetsParser.getInstance().isArmorSet(item.getId())) {
                  return;
               }

               ArmorSetTemplate armorSet = ArmorSetsParser.getInstance().getSet(item.getId());
               remove = true;
               skills = armorSet.getSkills();
               shieldSkill = armorSet.getShieldSkillId();
               skillId6 = armorSet.getEnchant6skillId();
               skillIdByLevel = armorSet.getEnchantByLevel();
            } else {
               ItemInstance chestItem = inventory.getPaperdollItem(6);
               if (chestItem == null) {
                  return;
               }

               ArmorSetTemplate armorSet = ArmorSetsParser.getInstance().getSet(chestItem.getId());
               if (armorSet == null) {
                  return;
               }

               if (armorSet.containItem(slot, item.getId())) {
                  remove = true;
                  skills = armorSet.getSkills();
                  shieldSkill = armorSet.getShieldSkillId();
                  skillId6 = armorSet.getEnchant6skillId();
                  skillIdByLevel = armorSet.getEnchantByLevel();
               } else if (armorSet.containShield(item.getId())) {
                  remove = true;
                  shieldSkill = armorSet.getShieldSkillId();
               }
            }

            if (remove) {
               if (skills != null) {
                  for(SkillHolder holder : skills) {
                     Skill itemSkill = holder.getSkill();
                     if (itemSkill != null) {
                        player.removeSkill(itemSkill, false, itemSkill.isPassive());
                     } else {
                        Inventory._log.warning("Inventory.ArmorSetListener: Incorrect skill: " + holder + ".");
                     }
                  }
               }

               if (shieldSkill != null) {
                  for(SkillHolder holder : shieldSkill) {
                     Skill itemSkill = holder.getSkill();
                     if (itemSkill != null) {
                        player.removeSkill(itemSkill, false, itemSkill.isPassive());
                     } else {
                        Inventory._log.warning("Inventory.ArmorSetListener: Incorrect skill: " + holder + ".");
                     }
                  }
               }

               if (skillId6 != null) {
                  for(SkillHolder holder : skillId6) {
                     Skill itemSkill = holder.getSkill();
                     if (itemSkill != null) {
                        player.removeSkill(itemSkill, false, itemSkill.isPassive());
                     } else {
                        Inventory._log.warning("Inventory.ArmorSetListener: Incorrect skill: " + holder + ".");
                     }
                  }
               }

               if (skillIdByLevel != null && !skillIdByLevel.isEmpty()) {
                  for(int enchLvl : skillIdByLevel.keySet()) {
                     Skill itemSkill = skillIdByLevel.get(enchLvl).getSkill();
                     if (itemSkill != null) {
                        player.removeSkill(itemSkill, false, itemSkill.isPassive());
                     } else {
                        Inventory._log.warning("Inventory.ArmorSetListener: Incorrect skill: " + skillIdByLevel.get(enchLvl) + ".");
                     }
                  }
               }

               player.checkItemRestriction();
               player.sendSkillList(false);
            }
         }
      }
   }

   private static final class BowCrossRodListener implements Inventory.PaperdollListener {
      private static Inventory.BowCrossRodListener instance = new Inventory.BowCrossRodListener();

      public static Inventory.BowCrossRodListener getInstance() {
         return instance;
      }

      @Override
      public void notifyUnequiped(int slot, ItemInstance item, Inventory inventory) {
         if (slot == 5) {
            if (item.getItemType() == WeaponType.BOW) {
               ItemInstance arrow = inventory.getPaperdollItem(7);
               if (arrow != null) {
                  inventory.setPaperdollItem(7, null);
               }
            } else if (item.getItemType() == WeaponType.CROSSBOW) {
               ItemInstance bolts = inventory.getPaperdollItem(7);
               if (bolts != null) {
                  inventory.setPaperdollItem(7, null);
               }
            } else if (item.getItemType() == WeaponType.FISHINGROD) {
               ItemInstance lure = inventory.getPaperdollItem(7);
               if (lure != null) {
                  inventory.setPaperdollItem(7, null);
               }
            }
         }
      }

      @Override
      public void notifyEquiped(int slot, ItemInstance item, Inventory inventory) {
         if (slot == 5) {
            if (item.getItemType() == WeaponType.BOW) {
               ItemInstance arrow = inventory.findArrowForBow(item.getItem());
               if (arrow != null) {
                  inventory.setPaperdollItem(7, arrow);
               }
            } else if (item.getItemType() == WeaponType.CROSSBOW) {
               ItemInstance bolts = inventory.findBoltForCrossBow(item.getItem());
               if (bolts != null) {
                  inventory.setPaperdollItem(7, bolts);
               }
            }
         }
      }
   }

   private static final class BraceletListener implements Inventory.PaperdollListener {
      private static Inventory.BraceletListener instance = new Inventory.BraceletListener();

      public static Inventory.BraceletListener getInstance() {
         return instance;
      }

      @Override
      public void notifyUnequiped(int slot, ItemInstance item, Inventory inventory) {
         if (item.getItem().getBodyPart() == 1048576) {
            inventory.unEquipItemInSlot(17);
            inventory.unEquipItemInSlot(18);
            inventory.unEquipItemInSlot(19);
            inventory.unEquipItemInSlot(20);
            inventory.unEquipItemInSlot(21);
            inventory.unEquipItemInSlot(22);
         }
      }

      @Override
      public void notifyEquiped(int slot, ItemInstance item, Inventory inventory) {
         if (inventory.getOwner() instanceof Player) {
            Player player = (Player)inventory.getOwner();
            if (!item.getItem().isAccessory() && !item.getItem().isTalisman() && !item.getItem().isBracelet()) {
               player.broadcastCharInfo();
            } else {
               player.sendUserInfo(true);
            }
         }
      }
   }

   private static final class ChangeRecorder implements Inventory.PaperdollListener {
      private final Inventory _inventory;
      private final List<ItemInstance> _changed;

      ChangeRecorder(Inventory inventory) {
         this._inventory = inventory;
         this._changed = new ArrayList<>();
         this._inventory.addPaperdollListener(this);
      }

      @Override
      public void notifyEquiped(int slot, ItemInstance item, Inventory inventory) {
         if (!this._changed.contains(item)) {
            this._changed.add(item);
         }
      }

      @Override
      public void notifyUnequiped(int slot, ItemInstance item, Inventory inventory) {
         if (!this._changed.contains(item)) {
            this._changed.add(item);
         }
      }

      public ItemInstance[] getChangedItems() {
         return this._changed.toArray(new ItemInstance[this._changed.size()]);
      }
   }

   private static final class ItemSkillsListener implements Inventory.PaperdollListener {
      private static Inventory.ItemSkillsListener instance = new Inventory.ItemSkillsListener();

      public static Inventory.ItemSkillsListener getInstance() {
         return instance;
      }

      @Override
      public void notifyUnequiped(int slot, ItemInstance item, Inventory inventory) {
         if (inventory.getOwner() instanceof Player) {
            Player player = (Player)inventory.getOwner();
            Item it = item.getItem();
            boolean update = false;
            boolean updateTimeStamp = false;
            if (item.isAugmented()) {
               item.getAugmentation().removeBonus(player);
            }

            item.unChargeAllShots();
            item.removeElementAttrBonus(player);
            if (item.getEnchantLevel() >= 4) {
               Skill enchant4Skill = it.getEnchant4Skill();
               if (enchant4Skill != null) {
                  player.removeSkill(enchant4Skill, false, enchant4Skill.isPassive());
                  update = true;
               }
            }

            item.clearEnchantStats();
            SkillHolder[] skills = it.getSkills();
            if (skills != null) {
               for(SkillHolder skillInfo : skills) {
                  if (skillInfo != null) {
                     Skill itemSkill = skillInfo.getSkill();
                     if (itemSkill != null) {
                        player.removeSkill(itemSkill, false, itemSkill.isPassive());
                        update = true;
                     } else {
                        Inventory._log.warning("Inventory.ItemSkillsListener.Weapon: Incorrect skill: " + skillInfo + ".");
                     }
                  }
               }
            }

            if (item.isArmor()) {
               for(ItemInstance itm : inventory.getItems()) {
                  if (itm.isEquipped() && itm.getItem().getSkills() != null) {
                     for(SkillHolder sk : itm.getItem().getSkills()) {
                        if (player.getSkillLevel(sk.getId()) == -1) {
                           Skill itemSkill = sk.getSkill();
                           if (itemSkill != null) {
                              player.addSkill(itemSkill, false);
                              if (itemSkill.isActive()) {
                                 if (!player.hasSkillReuse(itemSkill.getReuseHashCode())) {
                                    int equipDelay = item.getEquipReuseDelay();
                                    if (equipDelay > 0) {
                                       player.addTimeStamp(itemSkill, (long)equipDelay);
                                       player.disableSkill(itemSkill, (long)equipDelay);
                                    }
                                 }

                                 updateTimeStamp = true;
                              }

                              update = true;
                           }
                        }
                     }
                  }
               }
            }

            if (item.isShadowItem()) {
               item.stopManaConsumeTask();
            }

            Skill unequipSkill = it.getUnequipSkill();
            if (unequipSkill != null) {
               ISkillHandler handler = SkillHandler.getInstance().getHandler(unequipSkill.getSkillType());
               Player[] targets = new Player[]{player};
               if (handler != null) {
                  handler.useSkill(player, unequipSkill, targets);
               } else {
                  unequipSkill.useSkill(player, targets);
               }
            }

            if (update) {
               player.sendSkillList(updateTimeStamp);
            }
         }
      }

      @Override
      public void notifyEquiped(int slot, ItemInstance item, Inventory inventory) {
         if (inventory.getOwner() instanceof Player) {
            Player player = (Player)inventory.getOwner();
            Item it = item.getItem();
            boolean update = false;
            boolean updateTimeStamp = false;
            if (item.isAugmented()) {
               item.getAugmentation().applyBonus(player);
            }

            item.rechargeShots(true, true);
            item.updateElementAttrBonus(player);
            if (item.getEnchantLevel() >= 4) {
               Skill enchant4Skill = it.getEnchant4Skill();
               if (enchant4Skill != null) {
                  player.addSkill(enchant4Skill, false);
                  update = true;
               }
            }

            item.applyEnchantStats();
            SkillHolder[] skills = it.getSkills();
            if (skills != null) {
               for(SkillHolder skillInfo : skills) {
                  if (skillInfo != null) {
                     Skill itemSkill = skillInfo.getSkill();
                     if (itemSkill != null) {
                        player.addSkill(itemSkill, false);
                        if (itemSkill.isActive()) {
                           if (!player.hasSkillReuse(itemSkill.getReuseHashCode())) {
                              int equipDelay = item.getEquipReuseDelay();
                              if (equipDelay > 0) {
                                 player.addTimeStamp(itemSkill, (long)equipDelay);
                                 player.disableSkill(itemSkill, (long)equipDelay);
                              }
                           }

                           updateTimeStamp = true;
                        }

                        update = true;
                     } else {
                        Inventory._log.warning("Inventory.ItemSkillsListener.Weapon: Incorrect skill: " + skillInfo + ".");
                     }
                  }
               }
            }

            if (update) {
               player.sendSkillList(updateTimeStamp);
            }
         }
      }
   }

   public interface PaperdollListener {
      void notifyEquiped(int var1, ItemInstance var2, Inventory var3);

      void notifyUnequiped(int var1, ItemInstance var2, Inventory var3);
   }

   private static final class StatsListener implements Inventory.PaperdollListener {
      private static Inventory.StatsListener instance = new Inventory.StatsListener();

      public static Inventory.StatsListener getInstance() {
         return instance;
      }

      @Override
      public void notifyUnequiped(int slot, ItemInstance item, Inventory inventory) {
         inventory.getOwner().removeStatsOwner(item);
      }

      @Override
      public void notifyEquiped(int slot, ItemInstance item, Inventory inventory) {
         inventory.getOwner().addStatFuncs(item.getStatFuncs(inventory.getOwner()));
      }
   }
}
