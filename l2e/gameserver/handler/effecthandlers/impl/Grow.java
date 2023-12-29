package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class Grow extends Effect {
   public Grow(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.BUFF;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected().isNpc()) {
         Npc npc = (Npc)this.getEffected();
         npc.setCollisionRadius(npc.getColRadius() * 1.19);
         this.getEffected().startAbnormalEffect(AbnormalEffect.GROW);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onExit() {
      if (this.getEffected() instanceof Npc) {
         Npc npc = (Npc)this.getEffected();
         npc.setCollisionRadius(npc.getTemplate().getfCollisionRadius());
         this.getEffected().stopAbnormalEffect(AbnormalEffect.GROW);
      }
   }
}
