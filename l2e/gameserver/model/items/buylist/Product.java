package l2e.gameserver.model.items.buylist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.templates.items.Item;

public final class Product {
   private static final Logger _log = Logger.getLogger(Product.class.getName());
   private final int _buyListId;
   private final Item _item;
   private final long _price;
   private final long _restockDelay;
   private final long _maxCount;
   private AtomicLong _count = null;
   private ScheduledFuture<?> _restockTask = null;

   public Product(int buyListId, Item item, long price, long restockDelay, long maxCount) {
      this._buyListId = buyListId;
      this._item = item;
      this._price = price;
      this._restockDelay = restockDelay * 60000L;
      this._maxCount = maxCount;
      if (this.hasLimitedStock()) {
         this._count = new AtomicLong(maxCount);
      }
   }

   public int getBuyListId() {
      return this._buyListId;
   }

   public Item getItem() {
      return this._item;
   }

   public int getId() {
      return this.getItem().getId();
   }

   public long getPrice() {
      return this._price < 0L ? (long)this.getItem().getReferencePrice() : this._price;
   }

   public long getRestockDelay() {
      return this._restockDelay;
   }

   public long getMaxCount() {
      return this._maxCount;
   }

   public long getCount() {
      if (this._count == null) {
         return 0L;
      } else {
         long count = this._count.get();
         return count > 0L ? count : 0L;
      }
   }

   public void setCount(long currentCount) {
      if (this._count == null) {
         this._count = new AtomicLong();
      }

      this._count.set(currentCount);
   }

   public boolean decreaseCount(long val) {
      if (this._count == null) {
         return false;
      } else {
         if (this._restockTask == null || this._restockTask.isDone()) {
            this._restockTask = ThreadPoolManager.getInstance().schedule(new Product.RestockTask(), this.getRestockDelay());
         }

         boolean result = this._count.addAndGet(-val) >= 0L;
         this.save();
         return result;
      }
   }

   public boolean hasLimitedStock() {
      return this.getMaxCount() > -1L;
   }

   public void restartRestockTask(long nextRestockTime) {
      long remainTime = nextRestockTime - System.currentTimeMillis();
      if (remainTime > 0L) {
         this._restockTask = ThreadPoolManager.getInstance().schedule(new Product.RestockTask(), remainTime);
      } else {
         this.restock();
      }
   }

   public void restock() {
      this.setCount(this.getMaxCount());
      this.save();
   }

   private void save() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "INSERT INTO `buylists`(`buylist_id`, `item_id`, `count`, `next_restock_time`) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE `count` = ?, `next_restock_time` = ?"
         );
      ) {
         statement.setInt(1, this.getBuyListId());
         statement.setInt(2, this.getId());
         statement.setLong(3, this.getCount());
         statement.setLong(5, this.getCount());
         if (this._restockTask != null && this._restockTask.getDelay(TimeUnit.MILLISECONDS) > 0L) {
            long nextRestockTime = System.currentTimeMillis() + this._restockTask.getDelay(TimeUnit.MILLISECONDS);
            statement.setLong(4, nextRestockTime);
            statement.setLong(6, nextRestockTime);
         } else {
            statement.setLong(4, 0L);
            statement.setLong(6, 0L);
         }

         statement.executeUpdate();
      } catch (Exception var34) {
         _log.log(Level.WARNING, "Failed to save Product buylist_id:" + this.getBuyListId() + " item_id:" + this.getId(), (Throwable)var34);
      }
   }

   protected final class RestockTask implements Runnable {
      @Override
      public void run() {
         Product.this.restock();
      }
   }
}
