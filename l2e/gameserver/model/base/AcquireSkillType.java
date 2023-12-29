package l2e.gameserver.model.base;

public enum AcquireSkillType {
   CLASS,
   FISHING,
   PLEDGE,
   SUBPLEDGE,
   TRANSFORM,
   TRANSFER,
   SUBCLASS,
   COLLECT;

   public static AcquireSkillType getAcquireSkillType(int id) {
      return values()[id];
   }
}
