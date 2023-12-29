package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.Augmentation;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.ItemRecovery;
import l2e.gameserver.model.items.instance.ItemInstance;

public class ItemRecoveryManager {
   private static final Logger _log = Logger.getLogger(ItemRecoveryManager.class.getName());
   private final Map<Integer, List<ItemRecovery>> _itemList = new ConcurrentHashMap<>();

   public ItemRecoveryManager() {
      this._itemList.clear();
      this.load();
   }

   public void load() {
      int count = 0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM item_recovery");
         ResultSet rset = statement.executeQuery();
      ) {
         while(rset.next()) {
            if (rset.getLong("time") < System.currentTimeMillis()) {
               this.deleteItem(rset.getInt("object_id"));
            } else {
               ItemRecovery item = new ItemRecovery();
               item.setCharId(rset.getInt("charId"));
               item.setItemId(rset.getInt("item_id"));
               item.setObjectId(rset.getInt("object_id"));
               item.setCount(rset.getLong("count"));
               item.setEnchantLevel(rset.getInt("enchant_level"));
               item.setAugmentationId(rset.getInt("augmentation"));
               item.setElementals(rset.getString("elementals"));
               item.setTime(rset.getLong("time"));
               if (!this._itemList.containsKey(item.getCharId())) {
                  this._itemList.put(item.getCharId(), new ArrayList<>());
               }

               this._itemList.get(item.getCharId()).add(item);
               ++count;
            }
         }
      } catch (Exception var61) {
         _log.warning(this.getClass().getSimpleName() + ": Could not load recovery items: " + var61.getMessage());
      }

      _log.info(this.getClass().getSimpleName() + ": Loaded " + count + " delete items.");
   }

   public List<ItemRecovery> getAllRemoveItems(int playerObjId) {
      if (this._itemList != null && !this._itemList.isEmpty() && this._itemList.containsKey(playerObjId)) {
         for(ItemRecovery item : this._itemList.get(playerObjId)) {
            if (item != null && item.getTime() < System.currentTimeMillis()) {
               this.deleteItem(item.getObjectId());
               this._itemList.get(playerObjId).remove(item);
            }
         }

         return this._itemList.get(playerObjId);
      } else {
         return null;
      }
   }

   private void deleteItem(int objId) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM item_recovery WHERE object_id=?");
      ) {
         statement.setInt(1, objId);
         statement.execute();
      } catch (Exception var34) {
         _log.warning(this.getClass().getSimpleName() + ": Could not delete recovery item: " + var34.getMessage());
      }
   }

   public boolean recoveryItem(int objectId, Player player) {
      if (!this._itemList.containsKey(player.getObjectId())) {
         return false;
      } else {
         List<ItemRecovery> items = this.getAllRemoveItems(player.getObjectId());
         if (items != null && !items.isEmpty()) {
            for(ItemRecovery item : items) {
               if (item != null && item.getObjectId() == objectId) {
                  ItemInstance itemRecovery = new ItemInstance(IdFactory.getInstance().getNextId(), item.getItemId());
                  if (item.getEnchantLevel() != 0) {
                     itemRecovery.setEnchantLevel(item.getEnchantLevel());
                  }

                  if (item.getAugmentationId() != -1) {
                     itemRecovery.setAugmentation(new Augmentation(item.getAugmentationId()));
                  }

                  if (item.getElementals() != null && !item.getElementals().isEmpty()) {
                     String[] elements = item.getElementals().split(";");

                     for(String el : elements) {
                        String[] element = el.split(":");
                        if (element != null) {
                           itemRecovery.setElementAttr(Byte.parseByte(element[0]), Integer.parseInt(element[1]));
                        }
                     }
                  }

                  itemRecovery.setCount(item.getCount());
                  player.addItem("Recovery Item", itemRecovery, player, true);
                  this.deleteItem(item.getObjectId());
                  this._itemList.get(player.getObjectId()).remove(item);
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      }
   }

   public void saveToRecoveryItem(Player player, ItemInstance item, long count) {
      ItemRecovery itemRec = new ItemRecovery();
      itemRec.setCharId(player.getObjectId());
      itemRec.setItemId(item.getId());
      itemRec.setObjectId(IdFactory.getInstance().getNextId());
      itemRec.setCount(count);
      itemRec.setEnchantLevel(item.getEnchantLevel());
      itemRec.setAugmentationId(item.getAugmentation() != null ? item.getAugmentation().getAttributes() : -1);
      String elements = "";
      if (item.getElementals() != null) {
         for(Elementals elm : item.getElementals()) {
            elements = elements + "" + elm.getElement() + ":" + elm.getValue() + ";";
         }
      }

      itemRec.setElementals(elements);
      itemRec.setTime(System.currentTimeMillis() + (long)Config.RECOVERY_ITEMS_HOURS * 3600000L);

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement(
            "INSERT INTO item_recovery (`charId`, `item_id`, `object_id`, `count`, `enchant_level`, `augmentation`, `elementals`, `time`) VALUES (?,?,?,?,?,?,?,?) "
         );
      ) {
         ps.setInt(1, itemRec.getCharId());
         ps.setInt(2, itemRec.getItemId());
         ps.setInt(3, itemRec.getObjectId());
         ps.setLong(4, itemRec.getCount());
         ps.setInt(5, itemRec.getEnchantLevel());
         ps.setInt(6, itemRec.getAugmentationId());
         ps.setString(7, itemRec.getElementals());
         ps.setLong(8, itemRec.getTime());
         ps.executeUpdate();
         if (!this._itemList.containsKey(itemRec.getCharId())) {
            this._itemList.put(itemRec.getCharId(), new ArrayList<>());
         }

         this._itemList.get(itemRec.getCharId()).add(itemRec);
      } catch (SQLException var39) {
         _log.warning(this.getClass().getSimpleName() + ": Could not save recovery item: " + var39.getMessage());
      }
   }

   public static ItemRecoveryManager getInstance() {
      return ItemRecoveryManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final ItemRecoveryManager _instance = new ItemRecoveryManager();
   }
}
