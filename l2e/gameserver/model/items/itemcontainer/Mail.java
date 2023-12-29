package l2e.gameserver.model.items.itemcontainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;

public class Mail extends ItemContainer {
   private final int _ownerId;
   private int _messageId;

   public Mail(int objectId, int messageId) {
      this._ownerId = objectId;
      this._messageId = messageId;
   }

   @Override
   public String getName() {
      return "Mail";
   }

   public Player getOwner() {
      return null;
   }

   @Override
   public ItemInstance.ItemLocation getBaseLocation() {
      return ItemInstance.ItemLocation.MAIL;
   }

   public int getMessageId() {
      return this._messageId;
   }

   public void setNewMessageId(int messageId) {
      this._messageId = messageId;

      for(ItemInstance item : this._items) {
         if (item != null) {
            item.setItemLocation(this.getBaseLocation(), messageId);
         }
      }

      this.updateDatabase();
   }

   @Override
   protected void addItem(ItemInstance item) {
      super.addItem(item);
      item.setItemLocation(this.getBaseLocation(), this._messageId);
   }

   @Override
   public void updateDatabase() {
      for(ItemInstance item : this._items) {
         if (item != null) {
            item.updateDatabase(true);
         }
      }
   }

   @Override
   public void restore() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time, visual_itemId, agathion_energy FROM items WHERE owner_id=? AND loc=? AND loc_data=?"
         );
      ) {
         statement.setInt(1, this.getOwnerId());
         statement.setString(2, this.getBaseLocation().name());
         statement.setInt(3, this.getMessageId());

         try (ResultSet inv = statement.executeQuery()) {
            while(inv.next()) {
               ItemInstance item = ItemInstance.restoreFromDb(this.getOwnerId(), inv);
               if (item != null) {
                  World.getInstance().addObject(item);
                  if (item.isStackable() && this.getItemByItemId(item.getId()) != null) {
                     this.addItem("Restore", item, null, null);
                  } else {
                     this.addItem(item);
                  }
               }
            }
         }
      } catch (Exception var59) {
         _log.log(Level.WARNING, "could not restore container:", (Throwable)var59);
      }
   }

   @Override
   public int getOwnerId() {
      return this._ownerId;
   }
}
