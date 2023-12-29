package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.ChanceCondition;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class ChanceSkillTrigger extends Effect {
   public ChanceSkillTrigger(Env env, EffectTemplate template) {
      super(env, template);
   }

   public ChanceSkillTrigger(Env env, Effect effect) {
      super(env, effect);
   }

   @Override
   public boolean canBeStolen() {
      return true;
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public ChanceCondition getTriggeredChanceCondition() {
      return this.getEffectTemplate().getChanceCondition();
   }

   @Override
   public int getTriggeredChanceId() {
      return this.getEffectTemplate().getTriggeredId();
   }

   @Override
   public int getTriggeredChanceLevel() {
      return this.getEffectTemplate().getTriggeredLevel();
   }

   @Override
   public boolean onActionTime() {
      this.getEffected().onActionTimeChanceEffect(this.getSkill().getElement());
      return this.getSkill().isPassive();
   }

   @Override
   public boolean onStart() {
      this.getEffected().addChanceTrigger(this);
      this.getEffected().onStartChanceEffect(this.getSkill().getElement());
      return super.onStart();
   }

   @Override
   public void onExit() {
      if (this.isInUse() && this.getTickCount() >= this.getEffectTemplate().getTotalTickCount()) {
         this.getEffected().onExitChanceEffect(this.getSkill().getElement());
      }

      this.getEffected().removeChanceEffect(this);
      super.onExit();
   }

   @Override
   public boolean triggersChanceSkill() {
      return this.getEffectTemplate().getTriggeredId() > 1;
   }
}
