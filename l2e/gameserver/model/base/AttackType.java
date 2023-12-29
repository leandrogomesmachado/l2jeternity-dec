package l2e.gameserver.model.base;

public enum AttackType {
   Normal(0, false),
   Magic(1, false),
   Crit(2, false),
   MCrit(3, false),
   Blow(4, false),
   PSkillDamage(5, false),
   PSkillCritical(6, false);

   private int _attackId;
   public static final AttackType[] VALUES = values();
   private final boolean _isOlyVsAll;

   private AttackType(int attackId, boolean IsOnlyVsAll) {
      this._attackId = attackId;
      this._isOlyVsAll = IsOnlyVsAll;
   }

   public int getId() {
      return this._attackId;
   }

   public boolean isOnlyVsAll() {
      return this._isOlyVsAll;
   }
}
