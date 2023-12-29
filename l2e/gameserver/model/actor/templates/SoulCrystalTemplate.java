package l2e.gameserver.model.actor.templates;

public class SoulCrystalTemplate {
   private final int _itemId;
   private final int _level;
   private final int _nextItemId;
   private final int _cursedNextItemId;

   public SoulCrystalTemplate(int itemId, int level, int nextItemId, int cursedNextItemId) {
      this._itemId = itemId;
      this._level = level;
      this._nextItemId = nextItemId;
      this._cursedNextItemId = cursedNextItemId;
   }

   public int getId() {
      return this._itemId;
   }

   public int getLvl() {
      return this._level;
   }

   public int getNextId() {
      return this._nextItemId;
   }

   public int getCursedNextId() {
      return this._cursedNextItemId;
   }
}
