package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class Fusion extends Effect {
   public int _effect = this.getSkill().getLevel();
   public int _maxEffect = SkillsParser.getInstance().getMaxLevel(this.getSkill().getId());

   public Fusion(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.FUSION;
   }

   @Override
   public void increaseEffect() {
      if (this._effect < this._maxEffect) {
         ++this._effect;
         this.updateBuff();
      }
   }

   @Override
   public void decreaseForce() {
      --this._effect;
      if (this._effect < 1) {
         this.exit();
      } else {
         this.updateBuff();
      }
   }

   private void updateBuff() {
      this.exit();
      SkillsParser.getInstance().getInfo(this.getSkill().getId(), this._effect).getEffects(this.getEffector(), this.getEffected(), true);
   }
}
