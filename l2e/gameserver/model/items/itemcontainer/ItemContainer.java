package l2e.gameserver.model.items.itemcontainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.math.SafeMath;
import l2e.gameserver.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.instance.ItemInstance;

public abstract class ItemContainer {
   protected static final Logger _log = Logger.getLogger(ItemContainer.class.getName());
   protected final List<ItemInstance> _items = new CopyOnWriteArrayList<>();

   protected ItemContainer() {
   }

   protected abstract Creature getOwner();

   protected abstract ItemInstance.ItemLocation getBaseLocation();

   public String getName() {
      return "ItemContainer";
   }

   public int getOwnerId() {
      return this.getOwner() == null ? 0 : this.getOwner().getObjectId();
   }

   public int getSize() {
      return this._items.size();
   }

   public ItemInstance[] getItems() {
      return this._items.toArray(new ItemInstance[this._items.size()]);
   }

   public ItemInstance getItemByItemId(int itemId) {
      for(ItemInstance item : this._items) {
         if (item != null && item.getId() == itemId) {
            return item;
         }
      }

      return null;
   }

   public boolean haveItemsCountNotEquip(int itemId, long count) {
      long amount = 0L;

      for(ItemInstance item : this._items) {
         if (item != null && item.getId() == itemId && (!item.isEquipable() || !item.isEquipped())) {
            if (item.isEquipable()) {
               if (++amount >= count) {
                  break;
               }
            } else {
               amount = item.getCount();
            }
         }
      }

      return amount >= count;
   }

   public List<ItemInstance> getItemsByItemId(int itemId) {
      List<ItemInstance> returnList = new LinkedList<>();

      for(ItemInstance item : this._items) {
         if (item != null && item.getId() == itemId) {
            returnList.add(item);
         }
      }

      return returnList;
   }

   public ItemInstance getItemByItemId(int itemId, ItemInstance itemToIgnore) {
      for(ItemInstance item : this._items) {
         if (item != null && item.getId() == itemId && !item.equals(itemToIgnore)) {
            return item;
         }
      }

      return null;
   }

   public ItemInstance getItemByObjectId(int objectId) {
      for(ItemInstance item : this._items) {
         if (item != null && item.getObjectId() == objectId) {
            return item;
         }
      }

      return null;
   }

   public long getInventoryItemCount(int itemId, int enchantLevel) {
      return this.getInventoryItemCount(itemId, enchantLevel, true);
   }

   public long getInventoryItemCount(int itemId, int enchantLevel, boolean includeEquipped) {
      long count = 0L;

      for(ItemInstance item : this._items) {
         if (item.getId() == itemId && (item.getEnchantLevel() == enchantLevel || enchantLevel < 0) && (includeEquipped || !item.isEquipped())) {
            if (item.isStackable()) {
               count = item.getCount();
            } else {
               ++count;
            }
         }
      }

      return count;
   }

   public ItemInstance addItem(ItemInstance item, String owner, String log) {
      if (item == null) {
         return null;
      } else if (item.getCount() < 1L) {
         return null;
      } else {
         ItemInstance result = null;
         if (this.getItemByObjectId(item.getObjectId()) != null) {
            return null;
         } else {
            if (item.isStackable()) {
               int itemId = item.getId();
               result = this.getItemByItemId(itemId);
               if (result != null) {
                  synchronized(result) {
                     result.setCount(SafeMath.addAndLimit(item.getCount(), result.getCount()));
                     result.updateDatabase();
                     this.removeItem(item);
                  }
               }
            }

            if (result == null) {
               this._items.add(item);
               result = item;
               this.addItem(item);
            }

            return result;
         }
      }
   }

   public ItemInstance addItem(String process, ItemInstance item, Player actor, Object reference) {
      ItemInstance olditem = this.getItemByItemId(item.getId());
      if (olditem != null && olditem.isStackable()) {
         long count = item.getCount();
         olditem.changeCount(process, count, actor, reference);
         olditem.setLastChange(2);
         ItemsParser.getInstance().destroyItem(process, item, actor, reference);
         item.updateDatabase();
         item = olditem;
         if (olditem.getId() != 57 || !((double)count < 10000.0 * Config.RATE_DROP_ADENA)) {
            olditem.updateDatabase();
         } else if (GameTimeController.getInstance().getGameTicks() % 5 == 0) {
            olditem.updateDatabase();
         }
      } else {
         item.setOwnerId(process, this.getOwnerId(), actor, reference);
         item.setItemLocation(this.getBaseLocation());
         item.setLastChange(1);
         this.addItem(item);
         item.updateDatabase();
      }

      if (actor != null && item != null) {
         ItemInstance newItem = actor.getInventory().getItemByItemId(item.getId());
         if (newItem != null && newItem.isEtcItem()) {
            actor.checkToEquipArrows(newItem);
         }
      }

      this.refreshWeight();
      return item;
   }

   public ItemInstance addWaheHouseItem(String process, ItemInstance item, Player actor, Object reference) {
      ItemInstance olditem = this.getItemByItemId(item.getId());
      if (olditem != null && olditem.isStackable()) {
         long count = item.getCount();
         olditem.changeCount(process, count, actor, reference);
         olditem.setLastChange(2);
         ItemsParser.getInstance().destroyItem(process, item, actor, reference);
         item.updateDatabase();
         item = olditem;
         if (olditem.getId() != 57 || !((double)count < 10000.0 * Config.RATE_DROP_ADENA)) {
            olditem.updateDatabase();
         } else if (GameTimeController.getInstance().getGameTicks() % 5 == 0) {
            olditem.updateDatabase();
         }
      } else {
         item.setOwnerId(process, this.getOwnerId(), actor, reference);
         item.setItemLocation(this.getBaseLocation());
         item.setLastChange(1);
         this.addItem(item);
         item.updateDatabase();
      }

      this.refreshWeight();
      return item;
   }

   public ItemInstance addItem(String process, int itemId, long count, Player actor, Object reference) {
      ItemInstance item = this.getItemByItemId(itemId);
      if (item != null && item.isStackable()) {
         item.changeCount(process, count, actor, reference);
         item.setLastChange(2);
         double adenaRate = Config.RATE_DROP_ADENA;
         if (itemId != 57 || !((double)count < 10000.0 * adenaRate)) {
            item.updateDatabase();
         } else if (GameTimeController.getInstance().getGameTicks() % 5 == 0) {
            item.updateDatabase();
         }
      } else {
         for(int i = 0; (long)i < count; ++i) {
            Item template = ItemsParser.getInstance().getTemplate(itemId);
            if (template == null) {
               _log.log(Level.WARNING, (actor != null ? "[" + actor.getName() + "] " : "") + "Invalid ItemId requested: ", itemId);
               return null;
            }

            item = ItemsParser.getInstance().createItem(process, itemId, template.isStackable() ? count : 1L, actor, reference);
            item.setOwnerId(this.getOwnerId());
            item.setItemLocation(this.getBaseLocation());
            item.setLastChange(1);
            this.addItem(item);
            item.updateDatabase();
            if (template.isStackable() || !Config.MULTIPLE_ITEM_DROP) {
               break;
            }
         }
      }

      if (actor != null && item != null) {
         ItemInstance newItem = actor.getInventory().getItemByItemId(item.getId());
         if (newItem != null && newItem.isEtcItem()) {
            actor.checkToEquipArrows(newItem);
         }
      }

      this.refreshWeight();
      return item;
   }

   public ItemInstance addWareHouseItem(String process, int itemId, long count, Player actor, Object reference) {
      ItemInstance item = this.getItemByItemId(itemId);
      if (item != null && item.isStackable()) {
         item.changeCount(process, count, actor, reference);
         item.setLastChange(2);
         double adenaRate = Config.RATE_DROP_ADENA;
         if (itemId != 57 || !((double)count < 10000.0 * adenaRate)) {
            item.updateDatabase();
         } else if (GameTimeController.getInstance().getGameTicks() % 5 == 0) {
            item.updateDatabase();
         }
      } else {
         for(int i = 0; (long)i < count; ++i) {
            Item template = ItemsParser.getInstance().getTemplate(itemId);
            if (template == null) {
               _log.log(Level.WARNING, (actor != null ? "[" + actor.getName() + "] " : "") + "Invalid ItemId requested: ", itemId);
               return null;
            }

            item = ItemsParser.getInstance().createItem(process, itemId, template.isStackable() ? count : 1L, actor, reference);
            item.setOwnerId(this.getOwnerId());
            item.setItemLocation(this.getBaseLocation());
            item.setLastChange(1);
            this.addItem(item);
            item.updateDatabase();
            if (template.isStackable() || !Config.MULTIPLE_ITEM_DROP) {
               break;
            }
         }
      }

      this.refreshWeight();
      return item;
   }

   public ItemInstance transferItem(String process, int objectId, long count, ItemContainer target, Player actor, Object reference) {
      if (target == null) {
         return null;
      } else {
         ItemInstance sourceitem = this.getItemByObjectId(objectId);
         if (sourceitem == null) {
            return null;
         } else {
            ItemInstance targetitem = sourceitem.isStackable() ? target.getItemByItemId(sourceitem.getId()) : null;
            synchronized(sourceitem) {
               if (this.getItemByObjectId(objectId) != sourceitem) {
                  return null;
               } else {
                  if (count > sourceitem.getCount()) {
                     count = sourceitem.getCount();
                  }

                  if (sourceitem.getCount() == count && targetitem == null) {
                     this.removeItem(sourceitem);
                     target.addItem(process, sourceitem, actor, reference);
                     targetitem = sourceitem;
                  } else {
                     if (sourceitem.getCount() > count) {
                        sourceitem.changeCount(process, -count, actor, reference);
                     } else {
                        this.removeItem(sourceitem);
                        ItemsParser.getInstance().destroyItem(process, sourceitem, actor, reference);
                     }

                     if (targetitem != null) {
                        targetitem.changeCount(process, count, actor, reference);
                     } else {
                        targetitem = target.addItem(process, sourceitem.getId(), count, actor, reference);
                     }
                  }

                  sourceitem.updateDatabase(true);
                  if (targetitem != sourceitem && targetitem != null) {
                     targetitem.updateDatabase();
                  }

                  if (sourceitem.isAugmented()) {
                     sourceitem.getAugmentation().removeBonus(actor);
                  }

                  this.refreshWeight();
                  target.refreshWeight();
                  return targetitem;
               }
            }
         }
      }
   }

   public ItemInstance destroyItem(String process, ItemInstance item, Player actor, Object reference) {
      return this.destroyItem(process, item, item.getCount(), actor, reference);
   }

   public ItemInstance destroyItem(String process, ItemInstance item, long count, Player actor, Object reference) {
      synchronized(item) {
         if (item.getCount() > count) {
            item.changeCount(process, -count, actor, reference);
            item.setLastChange(2);
            if (process != null || GameTimeController.getInstance().getGameTicks() % 10 == 0) {
               item.updateDatabase();
            }

            this.refreshWeight();
         } else {
            if (item.getCount() < count) {
               return null;
            }

            boolean removed = this.removeItem(item);
            if (!removed) {
               return null;
            }

            ItemsParser.getInstance().destroyItem(process, item, actor, reference);
            item.updateDatabase();
            this.refreshWeight();
         }

         return item;
      }
   }

   public ItemInstance destroyItem(String process, int objectId, long count, Player actor, Object reference) {
      ItemInstance item = this.getItemByObjectId(objectId);
      return item == null ? null : this.destroyItem(process, item, count, actor, reference);
   }

   public ItemInstance destroyItemByItemId(String process, int itemId, long count, Player actor, Object reference) {
      ItemInstance item = this.getItemByItemId(itemId);
      return item == null ? null : this.destroyItem(process, item, count, actor, reference);
   }

   public boolean destroyItemByItemId(int itemId, long count, Player owner, String log) {
      ItemInstance item;
      if ((item = this.getItemByItemId(itemId)) == null) {
         return false;
      } else {
         synchronized(item) {
            this.destroyItem(log, item, count, owner, null);
            return true;
         }
      }
   }

   public ItemInstance destroyItemByObjectId(int objectId, long count, Player owner, Object reference) {
      ItemInstance item;
      if ((item = this.getItemByObjectId(objectId)) == null) {
         return null;
      } else {
         synchronized(item) {
            return this.destroyItem("Remove", item, count, owner, reference);
         }
      }
   }

   public void destroyAllItems(String process, Player actor, Object reference) {
      for(ItemInstance item : this._items) {
         if (item != null) {
            this.destroyItem(process, item, actor, reference);
         }
      }
   }

   public long getAdena() {
      long count = 0L;

      for(ItemInstance item : this._items) {
         if (item != null && item.getId() == 57) {
            return item.getCount();
         }
      }

      return count;
   }

   protected void addItem(ItemInstance item) {
      this._items.add(item);
   }

   public boolean removeItem(ItemInstance item) {
      return this._items.remove(item);
   }

   protected void refreshWeight() {
   }

   public void deleteMe() {
      if (this.getOwner() != null) {
         for(ItemInstance item : this._items) {
            if (item != null) {
               item.updateDatabase(true);
               item.deleteMe();
               World.getInstance().removeObject(item);
            }
         }
      }

      this._items.clear();
   }

   public void updateDatabase() {
      if (this.getOwner() != null) {
         for(ItemInstance item : this._items) {
            if (item != null) {
               item.updateDatabase(true);
            }
         }
      }
   }

   public void restore() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time, visual_itemId, agathion_energy FROM items WHERE owner_id=? AND (loc=?)"
         );
      ) {
         statement.setInt(1, this.getOwnerId());
         statement.setString(2, this.getBaseLocation().name());

         try (ResultSet inv = statement.executeQuery()) {
            while(inv.next()) {
               ItemInstance item = ItemInstance.restoreFromDb(this.getOwnerId(), inv);
               if (item != null) {
                  World.getInstance().addObject(item);
                  Player owner = this.getOwner() == null ? null : this.getOwner().getActingPlayer();
                  if (item.isStackable() && this.getItemByItemId(item.getId()) != null) {
                     this.addItem("Restore", item, owner, null);
                  } else {
                     this.addItem(item);
                  }
               }
            }
         }

         this.refreshWeight();
      } catch (Exception var60) {
         _log.log(Level.WARNING, "could not restore container:", (Throwable)var60);
      }
   }

   public boolean validateCapacity(long slots) {
      return true;
   }

   public boolean validateWeight(long weight) {
      return true;
   }

   public boolean validateCapacityByItemId(int itemId, long count) {
      Item template = ItemsParser.getInstance().getTemplate(itemId);
      return template == null || (template.isStackable() ? this.validateCapacity(1L) : this.validateCapacity(count));
   }

   public boolean validateWeightByItemId(int itemId, long count) {
      Item template = ItemsParser.getInstance().getTemplate(itemId);
      return template == null || this.validateWeight((long)template.getWeight() * count);
   }

   public boolean destroyItemByItemId(int itemId, long count, String log) {
      return this.getOwner().isPlayer() ? this.destroyItemByItemId(itemId, count, this.getOwner().getActingPlayer(), log) : false;
   }
}
