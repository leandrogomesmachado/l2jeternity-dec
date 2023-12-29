package l2e.commons.dao;

public enum JdbcEntityState {
   CREATED(true, false, false, false),
   STORED(false, true, false, true),
   UPDATED(false, true, true, true),
   DELETED(false, false, false, false);

   private final boolean savable;
   private final boolean deletable;
   private final boolean updatable;
   private final boolean persisted;

   private JdbcEntityState(boolean savable, boolean deletable, boolean updatable, boolean persisted) {
      this.savable = savable;
      this.deletable = deletable;
      this.updatable = updatable;
      this.persisted = persisted;
   }

   public boolean isSavable() {
      return this.savable;
   }

   public boolean isDeletable() {
      return this.deletable;
   }

   public boolean isUpdatable() {
      return this.updatable;
   }

   public boolean isPersisted() {
      return this.persisted;
   }
}
