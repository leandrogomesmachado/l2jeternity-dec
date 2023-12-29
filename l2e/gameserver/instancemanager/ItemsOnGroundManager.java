package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.ItemsAutoDestroy;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.World;
import l2e.gameserver.model.items.instance.ItemInstance;

public class ItemsOnGroundManager implements Runnable {
   private static final Logger _log = Logger.getLogger(ItemsOnGroundManager.class.getName());
   protected List<ItemInstance> _items = new CopyOnWriteArrayList<>();

   protected ItemsOnGroundManager() {
      if (Config.SAVE_DROPPED_ITEM_INTERVAL > 0) {
         ThreadPoolManager.getInstance().scheduleAtFixedRate(this, (long)Config.SAVE_DROPPED_ITEM_INTERVAL, (long)Config.SAVE_DROPPED_ITEM_INTERVAL);
      }

      this.load();
   }

   private void load() {
      if (!Config.SAVE_DROPPED_ITEM && Config.CLEAR_DROPPED_ITEM_TABLE) {
         this.emptyTable();
      }

      if (Config.SAVE_DROPPED_ITEM) {
         if (Config.DESTROY_DROPPED_PLAYER_ITEM) {
            try (Connection con = DatabaseFactory.getInstance().getConnection()) {
               String str = null;
               if (!Config.DESTROY_EQUIPABLE_PLAYER_ITEM) {
                  str = "update itemsonground set drop_time=? where drop_time=-1 and equipable=0";
               } else if (Config.DESTROY_EQUIPABLE_PLAYER_ITEM) {
                  str = "update itemsonground set drop_time=? where drop_time=-1";
               }

               PreparedStatement statement = con.prepareStatement(str);
               statement.setLong(1, System.currentTimeMillis());
               statement.execute();
               statement.close();
            } catch (Exception var37) {
               _log.log(Level.SEVERE, this.getClass().getSimpleName() + ": Error while updating table ItemsOnGround " + var37.getMessage(), (Throwable)var37);
            }
         }

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("SELECT object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable FROM itemsonground");
            int count = 0;
            ResultSet rset = statement.executeQuery();

            while(rset.next()) {
               ItemInstance item = new ItemInstance(rset.getInt(1), rset.getInt(2));
               World.getInstance().addObject(item);
               if (item.isStackable() && rset.getInt(3) > 1) {
                  item.setCount((long)rset.getInt(3));
               }

               if (rset.getInt(4) > 0) {
                  item.setEnchantLevel(rset.getInt(4));
               }

               item.spawnMe(rset.getInt(5), rset.getInt(6), rset.getInt(7));
               long dropTime = rset.getLong(8);
               item.setDropTime(dropTime);
               item.setProtected(dropTime == -1L);
               this._items.add(item);
               ++count;
               if (!Config.LIST_PROTECTED_ITEMS.contains(item.getId())
                  && dropTime > -1L
                  && (Config.AUTODESTROY_ITEM_AFTER > 0 && !item.getItem().isHerb() || Config.HERB_AUTO_DESTROY_TIME > 0 && item.getItem().isHerb())) {
                  ItemsAutoDestroy.getInstance().addItem(item);
               }
            }

            rset.close();
            statement.close();
            _log.info(this.getClass().getSimpleName() + ": Loaded " + count + " items.");
         } catch (Exception var35) {
            _log.log(Level.SEVERE, this.getClass().getSimpleName() + ": Error while loading ItemsOnGround " + var35.getMessage(), (Throwable)var35);
         }

         if (Config.EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD) {
            this.emptyTable();
         }
      }
   }

   public void save(ItemInstance item) {
      if (Config.SAVE_DROPPED_ITEM) {
         this._items.add(item);
      }
   }

   public void removeObject(ItemInstance item) {
      if (Config.SAVE_DROPPED_ITEM) {
         this._items.remove(item);
      }
   }

   public void saveInDb() {
      this.run();
   }

   public void cleanUp() {
      this._items.clear();
   }

   public void emptyTable() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("DELETE FROM itemsonground");
         statement.execute();
         statement.close();
      } catch (Exception var14) {
         _log.log(Level.SEVERE, this.getClass().getSimpleName() + ": Error while cleaning table ItemsOnGround " + var14.getMessage(), (Throwable)var14);
      }
   }

   @Override
   public synchronized void run() {
      if (Config.SAVE_DROPPED_ITEM) {
         this.emptyTable();
         if (!this._items.isEmpty()) {
            try (Connection con = DatabaseFactory.getInstance().getConnection()) {
               PreparedStatement statement = con.prepareStatement(
                  "INSERT INTO itemsonground(object_id,item_id,count,enchant_level,x,y,z,drop_time,equipable) VALUES(?,?,?,?,?,?,?,?,?)"
               );

               for(ItemInstance item : this._items) {
                  if (item != null && !CursedWeaponsManager.getInstance().isCursed(item.getId())) {
                     try {
                        statement.setInt(1, item.getObjectId());
                        statement.setInt(2, item.getId());
                        statement.setLong(3, item.getCount());
                        statement.setInt(4, item.getEnchantLevel());
                        statement.setInt(5, item.getX());
                        statement.setInt(6, item.getY());
                        statement.setInt(7, item.getZ());
                        statement.setLong(8, item.isProtected() ? -1L : item.getDropTime());
                        statement.setLong(9, (long)(item.isEquipable() ? 1 : 0));
                        statement.execute();
                        statement.clearParameters();
                     } catch (Exception var16) {
                        _log.log(
                           Level.SEVERE,
                           this.getClass().getSimpleName() + ": Error while inserting into table ItemsOnGround: " + var16.getMessage(),
                           (Throwable)var16
                        );
                     }
                  }
               }

               statement.close();
            } catch (SQLException var19) {
               _log.log(Level.SEVERE, this.getClass().getSimpleName() + ": SQL error while storing items on ground: " + var19.getMessage(), (Throwable)var19);
            }
         }
      }
   }

   public static final ItemsOnGroundManager getInstance() {
      return ItemsOnGroundManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final ItemsOnGroundManager _instance = new ItemsOnGroundManager();
   }
}
