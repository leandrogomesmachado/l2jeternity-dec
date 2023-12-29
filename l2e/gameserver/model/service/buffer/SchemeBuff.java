package l2e.gameserver.model.service.buffer;

public class SchemeBuff {
   private final int _skillId;
   private final int _skillLevel;
   private final int _premiumSkillLevel;
   private final boolean _isDanceSlot;

   public SchemeBuff(int skillId, int skillLevel, int premiumSkillLevel, boolean isDanceSlot) {
      this._skillId = skillId;
      this._skillLevel = skillLevel;
      this._premiumSkillLevel = premiumSkillLevel;
      this._isDanceSlot = isDanceSlot;
   }

   public int getSkillId() {
      return this._skillId;
   }

   public int getLevel() {
      return this._skillLevel;
   }

   public int getPremiumLevel() {
      return this._premiumSkillLevel;
   }

   public boolean isDanceSlot() {
      return this._isDanceSlot;
   }
}
