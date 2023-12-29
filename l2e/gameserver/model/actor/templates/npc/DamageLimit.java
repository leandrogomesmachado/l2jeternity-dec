package l2e.gameserver.model.actor.templates.npc;

public class DamageLimit {
   private final int _damage;
   private final int _physicDamage;
   private final int _magicDamage;

   public DamageLimit(int damage, int physicDamage, int magicDamage) {
      this._damage = damage;
      this._physicDamage = physicDamage;
      this._magicDamage = magicDamage;
   }

   public int getDamage() {
      return this._damage;
   }

   public int getPhysicDamage() {
      return this._physicDamage;
   }

   public int getMagicDamage() {
      return this._magicDamage;
   }
}
