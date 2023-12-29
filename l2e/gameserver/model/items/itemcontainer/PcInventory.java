package l2e.gameserver.model.items.itemcontainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.DressArmorParser;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.listener.events.AddToInventoryEvent;
import l2e.gameserver.listener.events.ItemDestroyEvent;
import l2e.gameserver.listener.events.ItemDropEvent;
import l2e.gameserver.listener.events.ItemTransferEvent;
import l2e.gameserver.listener.player.ItemTracker;
import l2e.gameserver.model.TradeItem;
import l2e.gameserver.model.TradeList;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.DressArmorTemplate;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.StatusUpdate;

public class PcInventory extends Inventory {
   private static final Logger _log = Logger.getLogger(PcInventory.class.getName());
   public static final int ADENA_ID = 57;
   public static final int ANCIENT_ADENA_ID = 5575;
   public static final long MAX_ADENA = Config.MAX_ADENA;
   private final Player _owner;
   private ItemInstance _adena;
   private ItemInstance _ancientAdena;
   private boolean _mustShowDressMe = false;
   private int[] _blockItems = null;
   private int _questSlots;
   private final Object _lock;
   private int _blockMode = -1;
   private static List<ItemTracker> itemTrackers = new LinkedList<>();

   public PcInventory(Player owner) {
      this._owner = owner;
      this._lock = new Object();
   }

   public Player getOwner() {
      return this._owner;
   }

   @Override
   protected ItemInstance.ItemLocation getBaseLocation() {
      return ItemInstance.ItemLocation.INVENTORY;
   }

   @Override
   protected ItemInstance.ItemLocation getEquipLocation() {
      return ItemInstance.ItemLocation.PAPERDOLL;
   }

   public ItemInstance getAdenaInstance() {
      return this._adena;
   }

   @Override
   public long getAdena() {
      return this._adena != null ? this._adena.getCount() : 0L;
   }

   public ItemInstance getAncientAdenaInstance() {
      return this._ancientAdena;
   }

   public long getAncientAdena() {
      return this._ancientAdena != null ? this._ancientAdena.getCount() : 0L;
   }

   public ItemInstance[] getUniqueItems(boolean allowAdena, boolean allowAncientAdena) {
      return this.getUniqueItems(allowAdena, allowAncientAdena, true);
   }

   public ItemInstance[] getUniqueItems(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable) {
      List<ItemInstance> list = new LinkedList<>();

      for(ItemInstance item : this._items) {
         if (item != null && !item.isTimeLimitedItem() && (allowAdena || item.getId() != 57) && (allowAncientAdena || item.getId() != 5575)) {
            boolean isDuplicate = false;

            for(ItemInstance litem : list) {
               if (litem.getId() == item.getId()) {
                  isDuplicate = true;
                  break;
               }
            }

            if (!isDuplicate && (!onlyAvailable || item != null && item.isSellable() && item.isAvailable(this.getOwner(), false, false))) {
               list.add(item);
            }
         }
      }

      return list.toArray(new ItemInstance[list.size()]);
   }

   public ItemInstance[] getUniqueItemsByEnchantLevel(boolean allowAdena, boolean allowAncientAdena) {
      return this.getUniqueItemsByEnchantLevel(allowAdena, allowAncientAdena, true);
   }

   public ItemInstance[] getUniqueItemsByEnchantLevel(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable) {
      List<ItemInstance> list = new LinkedList<>();

      for(ItemInstance item : this._items) {
         if (item != null && !item.isTimeLimitedItem() && (allowAdena || item.getId() != 57) && (allowAncientAdena || item.getId() != 5575)) {
            boolean isDuplicate = false;

            for(ItemInstance litem : list) {
               if (litem.getId() == item.getId() && litem.getEnchantLevel() == item.getEnchantLevel()) {
                  isDuplicate = true;
                  break;
               }
            }

            if (!isDuplicate && (!onlyAvailable || item.isSellable() && item.isAvailable(this.getOwner(), false, false))) {
               list.add(item);
            }
         }
      }

      return list.toArray(new ItemInstance[list.size()]);
   }

   public ItemInstance[] getAllItemsByItemId(int itemId) {
      return this.getAllItemsByItemId(itemId, true);
   }

   public ItemInstance[] getAllItemsByItemId(int itemId, boolean includeEquipped) {
      List<ItemInstance> list = new LinkedList<>();

      for(ItemInstance item : this._items) {
         if (item != null && item.getId() == itemId && (includeEquipped || !item.isEquipped())) {
            list.add(item);
         }
      }

      return list.toArray(new ItemInstance[list.size()]);
   }

   public ItemInstance[] getAllItemsByItemId(int itemId, int enchantment) {
      return this.getAllItemsByItemId(itemId, enchantment, true);
   }

   public ItemInstance[] getAllItemsByItemId(int itemId, int enchantment, boolean includeEquipped) {
      List<ItemInstance> list = new LinkedList<>();

      for(ItemInstance item : this._items) {
         if (item != null && item.getId() == itemId && item.getEnchantLevel() == enchantment && (includeEquipped || !item.isEquipped())) {
            list.add(item);
         }
      }

      return list.toArray(new ItemInstance[list.size()]);
   }

   public ItemInstance[] getAvailableItems(boolean allowAdena, boolean allowNonTradeable, boolean feightable) {
      List<ItemInstance> list = new LinkedList<>();

      for(ItemInstance item : this._items) {
         if (item != null && item.isAvailable(this.getOwner(), allowAdena, allowNonTradeable) && this.canManipulateWithItemId(item.getId())) {
            if (feightable) {
               if (item.getItemLocation() == ItemInstance.ItemLocation.INVENTORY && item.isFreightable()) {
                  list.add(item);
               }
            } else {
               list.add(item);
            }
         }
      }

      return list.toArray(new ItemInstance[list.size()]);
   }

   public ItemInstance[] getAugmentedItems() {
      List<ItemInstance> list = new LinkedList<>();

      for(ItemInstance item : this._items) {
         if (item != null && item.isAugmented()) {
            list.add(item);
         }
      }

      return list.toArray(new ItemInstance[list.size()]);
   }

   public ItemInstance[] getElementItems() {
      List<ItemInstance> list = new LinkedList<>();

      for(ItemInstance item : this._items) {
         if (item != null && item.getElementals() != null) {
            list.add(item);
         }
      }

      return list.toArray(new ItemInstance[list.size()]);
   }

   public TradeItem[] getAvailableItems(TradeList tradeList) {
      List<TradeItem> list = new LinkedList<>();

      for(ItemInstance item : this._items) {
         if (item != null && item.isAvailable(this.getOwner(), false, false)) {
            TradeItem adjItem = tradeList.adjustAvailableItem(item);
            if (adjItem != null) {
               boolean found = false;
               if (!adjItem.getItem().isStackable()) {
                  for(TradeItem temp : this.getOwner().getSellList().getItems()) {
                     if (temp.getObjectId() == adjItem.getObjectId()) {
                        found = true;
                        break;
                     }
                  }
               }

               if (!found) {
                  list.add(adjItem);
               }
            }
         }
      }

      return list.toArray(new TradeItem[list.size()]);
   }

   public void adjustAvailableItem(TradeItem item) {
      boolean notAllEquipped = false;

      for(ItemInstance adjItem : this.getItemsByItemId(item.getItem().getId())) {
         if (!adjItem.isEquipable()) {
            notAllEquipped |= true;
            break;
         }

         if (!adjItem.isEquipped()
            && adjItem.getEnchantLevel() == item.getEnchant()
            && adjItem.getAttackElementType() == item.getAttackElementType()
            && adjItem.getAttackElementPower() == item.getAttackElementPower()) {
            boolean checkAtt = true;
            byte i = 0;

            while(true) {
               if (i < 6) {
                  if (adjItem.getElementDefAttr(i) == item.getElementDefAttr(i)) {
                     ++i;
                     continue;
                  }

                  checkAtt = false;
               }

               if (checkAtt) {
                  notAllEquipped |= true;
               }
               break;
            }
         }
      }

      if (!notAllEquipped) {
         item.setCount(0L);
      } else {
         ItemInstance adjItem = this.getItemByItemId(item.getItem().getId());
         item.setObjectId(adjItem.getObjectId());
         item.setEnchant(adjItem.getEnchantLevel());
         item.setAttackElementType(adjItem.getAttackElementType());
         item.setAttackElementPower(adjItem.getAttackElementPower());

         for(byte i = 0; i < 6; ++i) {
            item.setElemDefAttr(i, adjItem.getElementDefAttr(i));
         }

         if (adjItem.getCount() < item.getCount()) {
            item.setCount(adjItem.getCount());
         }
      }
   }

   public void addAdena(String process, long count, Player actor, Object reference) {
      if (count > 0L) {
         this.addItem(process, 57, count, actor, reference);
      }
   }

   public boolean reduceAdena(String process, long count, Player actor, Object reference) {
      if (count > 0L) {
         return this.destroyItemByItemId(process, 57, count, actor, reference) != null;
      } else {
         return false;
      }
   }

   public void addAncientAdena(String process, long count, Player actor, Object reference) {
      if (count > 0L) {
         this.addItem(process, 5575, count, actor, reference);
      }
   }

   public boolean reduceAncientAdena(String process, long count, Player actor, Object reference) {
      if (count > 0L) {
         return this.destroyItemByItemId(process, 5575, count, actor, reference) != null;
      } else {
         return false;
      }
   }

   @Override
   public ItemInstance addItem(String process, ItemInstance item, Player actor, Object reference) {
      item = super.addItem(process, item, actor, reference);
      if (item != null && item.getId() == 57 && !item.equals(this._adena)) {
         this._adena = item;
      }

      if (item != null && item.getId() == 5575 && !item.equals(this._ancientAdena)) {
         this._ancientAdena = item;
      }

      this.fireTrackerEvents(PcInventory.TrackerEvent.ADD_TO_INVENTORY, actor, item, null);
      return item;
   }

   @Override
   public ItemInstance addItem(String process, int itemId, long count, Player actor, Object reference) {
      ItemInstance item = super.addItem(process, itemId, count, actor, reference);
      if (item != null && item.getId() == 57 && !item.equals(this._adena)) {
         this._adena = item;
      }

      if (item != null && item.getId() == 5575 && !item.equals(this._ancientAdena)) {
         this._ancientAdena = item;
      }

      if (item != null && actor != null) {
         if (!Config.FORCE_INVENTORY_UPDATE) {
            InventoryUpdate playerIU = new InventoryUpdate();
            playerIU.addItem(item);
            actor.sendPacket(playerIU);
         } else {
            actor.sendItemList(false);
         }

         StatusUpdate su = new StatusUpdate(actor);
         su.addAttribute(14, actor.getCurrentLoad());
         actor.sendPacket(su);
         this.fireTrackerEvents(PcInventory.TrackerEvent.ADD_TO_INVENTORY, actor, item, null);
      }

      return item;
   }

   @Override
   public ItemInstance transferItem(String process, int objectId, long count, ItemContainer target, Player actor, Object reference) {
      ItemInstance item = super.transferItem(process, objectId, count, target, actor, reference);
      if (this._adena != null && (this._adena.getCount() <= 0L || this._adena.getOwnerId() != this.getOwnerId())) {
         this._adena = null;
      }

      if (this._ancientAdena != null && (this._ancientAdena.getCount() <= 0L || this._ancientAdena.getOwnerId() != this.getOwnerId())) {
         this._ancientAdena = null;
      }

      this.fireTrackerEvents(PcInventory.TrackerEvent.TRANSFER, actor, item, target);
      return item;
   }

   @Override
   public ItemInstance destroyItem(String process, ItemInstance item, Player actor, Object reference) {
      if (item.getVisualItemId() > 0) {
         DressArmorTemplate dress = DressArmorParser.getInstance().getArmorByPartId(item.getVisualItemId());
         if (dress != null) {
            for(ItemInstance invItem : this.getItems()) {
               if (invItem.getObjectId() != item.getObjectId()
                  && invItem.getVisualItemId() >= 1
                  && (
                     invItem.getVisualItemId() == dress.getChest()
                        || invItem.getVisualItemId() == dress.getLegs()
                        || invItem.getVisualItemId() == dress.getGloves()
                        || invItem.getVisualItemId() == dress.getFeet()
                  )) {
                  invItem.setVisualItemId(0);
                  InventoryUpdate iu = new InventoryUpdate();
                  iu.addItem(invItem);
                  iu.addModifiedItem(invItem);
                  this._owner.sendPacket(iu);
               }
            }

            this._owner.getInventory().addItem("ExchangersBBS", dress.getPriceId(), dress.getPriceCount(), this._owner, true);
            _log.info("You have destroyed a part of a dressMe set, for that you will be refunded with the original price, so you can make it again");
         }
      }

      return this.destroyItem(process, item, item.getCount(), actor, reference);
   }

   @Override
   public ItemInstance destroyItem(String process, ItemInstance item, long count, Player actor, Object reference) {
      item = super.destroyItem(process, item, count, actor, reference);
      if (this._adena != null && this._adena.getCount() <= 0L) {
         this._adena = null;
      }

      if (this._ancientAdena != null && this._ancientAdena.getCount() <= 0L) {
         this._ancientAdena = null;
      }

      this.fireTrackerEvents(PcInventory.TrackerEvent.DESTROY, actor, item, null);
      return item;
   }

   @Override
   public ItemInstance destroyItem(String process, int objectId, long count, Player actor, Object reference) {
      ItemInstance item = this.getItemByObjectId(objectId);
      return item == null ? null : this.destroyItem(process, item, count, actor, reference);
   }

   @Override
   public ItemInstance destroyItemByItemId(String process, int itemId, long count, Player actor, Object reference) {
      ItemInstance item = this.getItemByItemId(itemId);
      return item == null ? null : this.destroyItem(process, item, count, actor, reference);
   }

   @Override
   public ItemInstance dropItem(String process, ItemInstance item, Player actor, Object reference) {
      item = super.dropItem(process, item, actor, reference);
      if (this._adena != null && (this._adena.getCount() <= 0L || this._adena.getOwnerId() != this.getOwnerId())) {
         this._adena = null;
      }

      if (this._ancientAdena != null && (this._ancientAdena.getCount() <= 0L || this._ancientAdena.getOwnerId() != this.getOwnerId())) {
         this._ancientAdena = null;
      }

      this.fireTrackerEvents(PcInventory.TrackerEvent.DROP, actor, item, null);
      return item;
   }

   @Override
   public ItemInstance dropItem(String process, int objectId, long count, Player actor, Object reference) {
      ItemInstance item = super.dropItem(process, objectId, count, actor, reference);
      if (this._adena != null && (this._adena.getCount() <= 0L || this._adena.getOwnerId() != this.getOwnerId())) {
         this._adena = null;
      }

      if (this._ancientAdena != null && (this._ancientAdena.getCount() <= 0L || this._ancientAdena.getOwnerId() != this.getOwnerId())) {
         this._ancientAdena = null;
      }

      this.fireTrackerEvents(PcInventory.TrackerEvent.DROP, actor, item, null);
      return item;
   }

   @Override
   public boolean removeItem(ItemInstance item) {
      this.getOwner().removeItemFromShortCut(item.getObjectId());
      if (item.getObjectId() == this.getOwner().getActiveEnchantItemId()) {
         this.getOwner().setActiveEnchantItemId(-1);
      }

      if (item.getId() == 57) {
         this._adena = null;
      } else if (item.getId() == 5575) {
         this._ancientAdena = null;
      }

      if (item.isQuestItem()) {
         synchronized(this._lock) {
            --this._questSlots;
            if (this._questSlots < 0) {
               this._questSlots = 0;
               _log.warning(this + ": QuestInventory size < 0!");
            }
         }
      }

      return super.removeItem(item);
   }

   @Override
   public void refreshWeight() {
      super.refreshWeight();
      this.getOwner().refreshOverloaded();
   }

   @Override
   public void restore() {
      super.restore();
      this._adena = this.getItemByItemId(57);
      this._ancientAdena = this.getItemByItemId(5575);
   }

   public static int[][] restoreVisibleInventory(int objectId) {
      int[][] paperdoll = new int[31][3];

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement2 = con.prepareStatement("SELECT object_id,item_id,loc_data,enchant_level FROM items WHERE owner_id=? AND loc='PAPERDOLL'");
      ) {
         statement2.setInt(1, objectId);

         try (ResultSet invdata = statement2.executeQuery()) {
            while(invdata.next()) {
               int slot = invdata.getInt("loc_data");
               paperdoll[slot][0] = invdata.getInt("object_id");
               paperdoll[slot][1] = invdata.getInt("item_id");
               paperdoll[slot][2] = invdata.getInt("enchant_level");
            }
         }
      } catch (Exception var60) {
         _log.log(Level.WARNING, "Could not restore inventory: " + var60.getMessage(), (Throwable)var60);
      }

      return paperdoll;
   }

   public boolean checkInventorySlotsAndWeight(List<Item> itemList, boolean sendMessage, boolean sendSkillMessage) {
      int lootWeight = 0;
      int requiredSlots = 0;
      if (itemList != null) {
         for(Item item : itemList) {
            if (!item.isStackable() || this.getInventoryItemCount(item.getId(), -1) <= 0L) {
               ++requiredSlots;
            }

            lootWeight += item.getWeight();
         }
      }

      boolean inventoryStatusOK = this.validateCapacity((long)requiredSlots) && this.validateWeight((long)lootWeight);
      if (!inventoryStatusOK && sendMessage) {
         this._owner.sendPacket(SystemMessageId.SLOTS_FULL);
         if (sendSkillMessage) {
            this._owner.sendPacket(SystemMessageId.WEIGHT_EXCEEDED_SKILL_UNAVAILABLE);
         }
      }

      return inventoryStatusOK;
   }

   public boolean validateCapacity(ItemInstance item) {
      return (!item.isStackable() || this.getItemByItemId(item.getId()) == null) && !item.getItem().isHerb()
         ? this.validateCapacity(1L, item.isQuestItem())
         : true;
   }

   public boolean validateCapacityByItemId(int itemId) {
      int slots = 0;
      ItemInstance invItem = this.getItemByItemId(itemId);
      if (invItem == null || !invItem.isStackable()) {
         ++slots;
      }

      return this.validateCapacity((long)slots, ItemsParser.getInstance().getTemplate(itemId).isQuestItem());
   }

   @Override
   public boolean validateCapacity(long slots) {
      return this.validateCapacity(slots, false);
   }

   public boolean validateCapacity(long slots, boolean questItem) {
      if (!questItem) {
         return (long)(this._items.size() - this._questSlots) + slots <= (long)this._owner.getInventoryLimit();
      } else {
         return (long)this._questSlots + slots <= (long)this._owner.getQuestInventoryLimit();
      }
   }

   @Override
   public boolean validateWeight(long weight) {
      if (this._owner.isGM() && this._owner.getDietMode() && this._owner.getAccessLevel().allowTransaction()) {
         return true;
      } else {
         return (long)this._totalWeight + weight <= (long)this._owner.getMaxLoad();
      }
   }

   public void setInventoryBlock(int[] items, int mode) {
      this._blockMode = mode;
      this._blockItems = items;
      this._owner.sendItemList(false);
   }

   public void unblock() {
      this._blockMode = -1;
      this._blockItems = null;
      this._owner.sendItemList(false);
   }

   public boolean hasInventoryBlock() {
      return this._blockMode > -1 && this._blockItems != null && this._blockItems.length > 0;
   }

   public void blockAllItems() {
      this.setInventoryBlock(new int[]{ItemsParser.getInstance().getArraySize() + 2}, 1);
   }

   public int getBlockMode() {
      return this._blockMode;
   }

   public int[] getBlockItems() {
      return this._blockItems;
   }

   public boolean canManipulateWithItemId(int itemId) {
      return (this._blockMode != 0 || !Util.contains(this._blockItems, itemId)) && (this._blockMode != 1 || Util.contains(this._blockItems, itemId));
   }

   @Override
   public void addItem(ItemInstance item) {
      if (item.isQuestItem()) {
         synchronized(this._lock) {
            ++this._questSlots;
         }
      }

      super.addItem(item);
   }

   public int getSize(boolean quest) {
      return quest ? this._questSlots : this.getSize() - this._questSlots;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "[" + this._owner + "]";
   }

   public void applyItemSkills() {
      for(ItemInstance item : this._items) {
         item.giveSkillsToOwner();
         item.applyEnchantStats();
      }
   }

   public void checkRuneSkills() {
      for(ItemInstance item : this._items) {
         if (item != null && item.hasPassiveSkills()) {
            item.giveSkillsToOwner();
         }
      }
   }

   private void fireTrackerEvents(PcInventory.TrackerEvent tEvent, Player actor, ItemInstance item, ItemContainer target) {
      if (item != null && actor != null && !itemTrackers.isEmpty()) {
         switch(tEvent) {
            case ADD_TO_INVENTORY:
               AddToInventoryEvent event = new AddToInventoryEvent();
               event.setItem(item);
               event.setPlayer(actor);

               for(ItemTracker tracker : itemTrackers) {
                  if (tracker.containsItemId(item.getId())) {
                     tracker.onAddToInventory(event);
                  }
               }

               return;
            case DROP:
               ItemDropEvent event = new ItemDropEvent();
               event.setItem(item);
               event.setDropper(actor);
               event.setLocation(actor.getLocation());

               for(ItemTracker tracker : itemTrackers) {
                  if (tracker.containsItemId(item.getId())) {
                     tracker.onDrop(event);
                  }
               }

               return;
            case DESTROY:
               ItemDestroyEvent event = new ItemDestroyEvent();
               event.setItem(item);
               event.setPlayer(actor);

               for(ItemTracker tracker : itemTrackers) {
                  if (tracker.containsItemId(item.getId())) {
                     tracker.onDestroy(event);
                  }
               }

               return;
            case TRANSFER:
               if (target != null) {
                  ItemTransferEvent event = new ItemTransferEvent();
                  event.setItem(item);
                  event.setPlayer(actor);
                  event.setTarget(target);

                  for(ItemTracker tracker : itemTrackers) {
                     if (tracker.containsItemId(item.getId())) {
                        tracker.onTransfer(event);
                     }
                  }
               }

               return;
         }
      }
   }

   public static void addItemTracker(ItemTracker tracker) {
      if (!itemTrackers.contains(tracker)) {
         itemTrackers.add(tracker);
      }
   }

   public static void removeItemTracker(ItemTracker tracker) {
      itemTrackers.remove(tracker);
   }

   @Override
   public int getPaperdollVisualItemId(int slot) {
      ItemInstance item = this.getPaperdollItem(slot);
      if (item != null) {
         switch(slot) {
            case 6:
            case 10:
            case 11:
            case 12:
               if (this.mustShowDressMe()) {
                  int visualItemId = item.getVisualItemId();
                  if (visualItemId == -1) {
                     return 0;
                  }

                  if (visualItemId != 0) {
                     return visualItemId;
                  }
               }
               break;
            case 7:
            case 8:
            case 9:
            default:
               int visualItemId = item.getVisualItemId();
               if (visualItemId == -1) {
                  return 0;
               }

               if (visualItemId != 0) {
                  return visualItemId;
               }
         }

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

   public void setMustShowDressMe(boolean val) {
      this._mustShowDressMe = val;
   }

   public boolean mustShowDressMe() {
      return this._mustShowDressMe;
   }

   public boolean hasAllDressMeItemsEquipped() {
      ItemInstance chestItem = this.getPaperdollItem(6);
      ItemInstance legsItem = this.getPaperdollItem(11);
      ItemInstance glovesItem = this.getPaperdollItem(10);
      ItemInstance feetItem = this.getPaperdollItem(12);
      if (chestItem != null && glovesItem != null && feetItem != null) {
         return legsItem != null || chestItem.getItem().getBodyPart() == 32768;
      } else {
         return false;
      }
   }

   public ItemInstance addItem(ItemInstance item, String log) {
      return this.addItem(item, this._owner.toString(), log);
   }

   @Override
   public boolean destroyItemByItemId(int itemId, long count, String log) {
      return this.destroyItemByItemId(itemId, count, this._owner, log);
   }

   private static enum TrackerEvent {
      DROP,
      ADD_TO_INVENTORY,
      DESTROY,
      TRANSFER;
   }
}
