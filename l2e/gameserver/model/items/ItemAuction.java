package l2e.gameserver.model.items;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.ItemContainer;

public class ItemAuction extends ItemContainer {
   private static final Logger _log = Logger.getLogger(ItemAuction.class.getName());
   private static ItemAuction _instance;

   protected ItemAuction() {
      this.restore();
   }

   public void deleteItemFromList(ItemInstance item) {
      this._items.remove(item);
   }

   @Override
   public String getName() {
      return "Auction";
   }

   public Player getOwner() {
      return null;
   }

   @Override
   public ItemInstance addItem(String process, ItemInstance item, Player owner, Object reference) {
      if (item == null) {
         return null;
      } else if (item.getCount() < 1L) {
         return null;
      } else {
         ItemInstance result = null;
         this._items.add(item);
         item.setLastChange(1);
         this.addItem(item);
         return item;
      }
   }

   public ItemInstance addFullItem(ItemInstance item) {
      if (item == null) {
         return null;
      } else if (item.getCount() < 1L) {
         return null;
      } else {
         ItemInstance result = null;
         this._items.add(item);
         item.setLastChange(2);
         this.addItem(item);
         return item;
      }
   }

   @Override
   public void addItem(ItemInstance item) {
      item.setItemLocation(this.getBaseLocation());
      item.updateDatabase(true);
   }

   @Override
   public ItemInstance.ItemLocation getBaseLocation() {
      return ItemInstance.ItemLocation.AUCTION;
   }

   @Override
   public ItemInstance getItemByObjectId(int objectId) {
      for(int i = 0; i < this._items.size(); ++i) {
         ItemInstance item = this._items.get(i);
         if (item.getObjectId() == objectId) {
            return item;
         }
      }

      return null;
   }

   @Override
   public void updateDatabase() {
      for(ItemInstance item : this._items) {
         if (item != null) {
            item.updateDatabase(true);
         }
      }
   }

   public void updateItem(int objectId, int newOwnerId) {
      ItemInstance item;
      if ((item = this.getItemByObjectId(objectId)) == null) {
         _log.warning("item is null in auction storage, obj id:" + objectId);
      } else {
         synchronized(item) {
            item.setOwnerId(newOwnerId);
            item.setItemLocation(ItemInstance.ItemLocation.INVENTORY);
            item.setLastChange(2);
            item.updateDatabase();
            this.deleteItemFromList(item);
         }
      }
   }

   public void changeCount(ItemInstance item, long count) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE items SET count=? WHERE object_id = ?");
      ) {
         statement.setLong(1, count);
         statement.setInt(2, item.getObjectId());
         statement.executeUpdate();
         this.deleteItemFromList(item);
         synchronized(item) {
            item.setCount(count);
            item.setLastChange(2);
            item.updateDatabase();
            this._items.add(item);
         }
      } catch (Exception var40) {
         _log.log(Level.SEVERE, "Could not update item " + this + " in DB: Reason: " + var40.getMessage(), (Throwable)var40);
      }

      item.updateDatabase(true);
   }

   public void removeItemFromDb(int objectId) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE object_id = ?");
         statement.setInt(1, objectId);
         statement.executeUpdate();
         statement.close();
         statement = con.prepareStatement("DELETE FROM item_attributes WHERE itemId = ?");
         statement.setInt(1, objectId);
         statement.executeUpdate();
         statement.close();
         statement = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ?");
         statement.setInt(1, objectId);
         statement.executeUpdate();
         statement.close();
      } catch (Exception var15) {
         _log.log(Level.SEVERE, "Could not delete item " + this + " in DB: " + var15.getMessage(), (Throwable)var15);
      }
   }

   @Override
   public void restore() {
      this.getItemsByLocation(ItemInstance.ItemLocation.AUCTION);
   }

   protected void getItemsByLocation(ItemInstance.ItemLocation loc) {
      this._items.clear();
      ItemInstance inst = null;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT object_id, item_id, count, enchant_level, custom_type1, custom_type2, visual_itemId FROM items WHERE loc = ?"
         );
      ) {
         statement.setString(1, loc.name());

         try (ResultSet rset = statement.executeQuery()) {
            for(; rset.next(); this._items.add(inst)) {
               int objectId = rset.getInt(1);
               int item_id = rset.getInt("item_id");
               long count = rset.getLong("count");
               int enchant_level = rset.getInt("enchant_level");
               int custom_type1 = rset.getInt("custom_type1");
               int custom_type2 = rset.getInt("custom_type2");
               int visual_itemId = rset.getInt("visual_itemId");
               Item item = ItemsParser.getInstance().getTemplate(item_id);
               if (item == null) {
                  _log.severe("Item item_id=" + item_id + " not known, object_id=" + objectId);
                  return;
               }

               inst = new ItemInstance(objectId, item);
               inst.setCount(count);
               inst.setEnchantLevel(enchant_level);
               inst.setCustomType1(custom_type1);
               inst.setCustomType2(custom_type2);
               inst.setVisualItemId(visual_itemId);
               if (inst.isEquipable()) {
                  inst.restoreAttributes();
               }
            }
         }
      } catch (Exception var76) {
         _log.log(Level.SEVERE, "Error while restore items from loc:" + loc.toString(), (Throwable)var76);
      }
   }

   public static ItemAuction getInstance() {
      if (_instance == null) {
         _instance = new ItemAuction();
      }

      return _instance;
   }
}
