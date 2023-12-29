package l2e.gameserver.model.items.type;

public enum ArmorType implements ItemType {
   NONE("None"),
   LIGHT("Light"),
   HEAVY("Heavy"),
   MAGIC("Magic"),
   SIGIL("Sigil"),
   SHIELD("Shield");

   final int _mask = 1 << this.ordinal() + WeaponType.values().length;
   final String _name;

   private ArmorType(String name) {
      this._name = name;
   }

   @Override
   public int mask() {
      return this._mask;
   }

   @Override
   public String toString() {
      return this._name;
   }
}
