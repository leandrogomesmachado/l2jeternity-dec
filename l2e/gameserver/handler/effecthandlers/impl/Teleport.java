package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class Teleport extends Effect {
   private final Location _loc;

   public Teleport(Env env, EffectTemplate template) {
      super(env, template);
      this._loc = new Location(
         template.getParameters().getInteger("x", 0), template.getParameters().getInteger("y", 0), template.getParameters().getInteger("z", 0)
      );
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.TELEPORT;
   }

   @Override
   public boolean calcSuccess() {
      return true;
   }

   @Override
   public boolean onStart() {
      this.getEffected().teleToLocation(this._loc, true);
      return true;
   }
}
