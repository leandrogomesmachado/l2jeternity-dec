package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.data.parser.TransformParser;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class Transformation extends Effect {
   public Transformation(Env env, EffectTemplate template) {
      super(env, template);
   }

   public Transformation(Env env, Effect effect) {
      super(env, effect);
   }

   @Override
   public boolean canBeStolen() {
      return false;
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.TRANSFORMATION;
   }

   @Override
   public void onExit() {
      this.getEffected().stopTransformation(false);
   }

   @Override
   public boolean onStart() {
      return !this.getEffected().isPlayer() ? false : TransformParser.getInstance().transformPlayer((int)this.calc(), this.getEffected().getActingPlayer());
   }
}
