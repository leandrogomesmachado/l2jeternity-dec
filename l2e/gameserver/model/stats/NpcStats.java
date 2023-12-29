package l2e.gameserver.model.stats;

import l2e.gameserver.model.interfaces.IRestorable;
import l2e.gameserver.model.interfaces.IStorable;

public class NpcStats extends StatsSet implements IRestorable, IStorable {
   private static final long serialVersionUID = -3001662130903838717L;
   private volatile boolean _changes = false;

   public final void set(String name, boolean value) {
      super.set(name, value);
      this._changes = true;
   }

   public final void set(String name, double value) {
      super.set(name, value);
      this._changes = true;
   }

   public final void set(String name, Enum<?> value) {
      super.set(name, value);
      this._changes = true;
   }

   public final void set(String name, int value) {
      super.set(name, value);
      this._changes = true;
   }

   public final void set(String name, long value) {
      super.set(name, value);
      this._changes = true;
   }

   public final void set(String name, String value) {
      super.set(name, value);
      this._changes = true;
   }

   public int getInteger(String key) {
      return super.getInteger(key, 0);
   }

   @Override
   public boolean restoreMe() {
      return true;
   }

   @Override
   public boolean storeMe() {
      return true;
   }

   public boolean hasVariable(String name) {
      return this.containsKey(name);
   }

   public final boolean getAndResetChanges() {
      boolean changes = this._changes;
      if (changes) {
         this._changes = false;
      }

      return changes;
   }

   public final void remove(String name) {
      this.unset(name);
      this._changes = true;
   }
}
