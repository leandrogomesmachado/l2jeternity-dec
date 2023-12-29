package l2e.gameserver.model.items.type;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum WeaponType implements ItemType {
   SWORD("Sword"),
   BLUNT("Blunt"),
   DAGGER("Dagger"),
   BOW("Bow"),
   POLE("Pole"),
   NONE("None"),
   DUAL("Dual Sword"),
   ETC("Etc"),
   FIST("Fist"),
   DUALFIST("Dual Fist"),
   FISHINGROD("Rod"),
   RAPIER("Rapier"),
   ANCIENTSWORD("Ancient"),
   CROSSBOW("Crossbow"),
   FLAG("Flag"),
   OWNTHING("Ownthing"),
   DUALDAGGER("Dual Dagger"),
   BIGBLUNT("Big Blunt"),
   BIGSWORD("Big Sword");

   private static final Logger _log = Logger.getLogger(WeaponType.class.getName());
   private final int _mask = 1 << this.ordinal();
   private final String _name;

   private WeaponType(String name) {
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

   public static WeaponType findByName(String name) {
      if (name.equalsIgnoreCase("DUAL")) {
         name = "Dual Sword";
      } else if (name.equalsIgnoreCase("DUALFIST")) {
         name = "Dual Fist";
      }

      for(WeaponType type : values()) {
         if (type.toString().equalsIgnoreCase(name)) {
            return type;
         }
      }

      _log.log(Level.WARNING, WeaponType.class.getSimpleName() + ": Requested unexistent enum member: " + name, (Throwable)(new IllegalStateException()));
      return FIST;
   }

   public boolean isRanged() {
      return this == BOW || this == CROSSBOW;
   }

   public boolean isBow() {
      return this == BOW;
   }

   public boolean isCrossbow() {
      return this == CROSSBOW;
   }
}
