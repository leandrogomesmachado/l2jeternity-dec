package l2e.gameserver.model;

import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.PetInstance;

public class DropProtection implements Runnable {
   private volatile boolean _isProtected = false;
   private Player _owner = null;
   private ScheduledFuture<?> _task = null;
   private static final long PROTECTED_MILLIS_TIME = (long)Config.NPC_DROP_PROTECTION * 1000L;
   private static final long RAID_LOOT_MILLIS_TIME = (long)Config.RAID_DROP_PROTECTION * 1000L;

   @Override
   public synchronized void run() {
      this._isProtected = false;
      this._owner = null;
      this._task = null;
   }

   public boolean isProtected() {
      return this._isProtected;
   }

   public Player getOwner() {
      return this._owner;
   }

   public synchronized boolean tryPickUp(Player actor) {
      if (!this._isProtected) {
         return true;
      } else if (this._owner == actor) {
         return true;
      } else {
         return this._owner.getParty() != null && this._owner.getParty() == actor.getParty();
      }
   }

   public boolean tryPickUp(PetInstance pet) {
      return this.tryPickUp(pet.getOwner());
   }

   public synchronized void unprotect() {
      if (this._task != null) {
         this._task.cancel(false);
      }

      this._isProtected = false;
      this._owner = null;
      this._task = null;
   }

   public synchronized void protect(Player player, boolean isRaid) {
      this.unprotect();
      long protectTime = isRaid ? RAID_LOOT_MILLIS_TIME : PROTECTED_MILLIS_TIME;
      if (protectTime > 0L) {
         this._isProtected = true;
         if ((this._owner = player) == null) {
            throw new NullPointerException("Trying to protect dropped item to null owner");
         } else {
            this._task = ThreadPoolManager.getInstance().schedule(this, protectTime);
         }
      }
   }
}
