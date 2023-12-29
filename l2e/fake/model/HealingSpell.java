package l2e.fake.model;

import l2e.gameserver.model.skills.targets.TargetType;

public class HealingSpell extends BotSkill {
   private final TargetType _targetType;

   public HealingSpell(int skillId, TargetType targetType, SpellUsageCondition condition, int conditionValue, int priority) {
      super(skillId, condition, conditionValue, priority);
      this._targetType = targetType;
   }

   public HealingSpell(int skillId, TargetType targetType, int conditionValue, int priority) {
      super(skillId, SpellUsageCondition.LESSHPPERCENT, conditionValue, priority);
      this._targetType = targetType;
   }

   public TargetType getTargetType() {
      return this._targetType;
   }
}
