package l2e.gameserver.handler.effecthandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Rnd;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class RandomizeHate extends Effect {
   public RandomizeHate(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected() != null && this.getEffected() != this.getEffector() && this.getEffected().isAttackable()) {
         Attackable effectedMob = (Attackable)this.getEffected();
         List<Creature> targetList = new ArrayList<>();

         for(Creature cha : World.getInstance().getAroundCharacters(this.getEffected())) {
            if (cha != null
               && cha != effectedMob
               && cha != this.getEffector()
               && (!cha.isAttackable() || ((Attackable)cha).getFaction().isNone() || !((Attackable)cha).isInFaction(effectedMob))) {
               targetList.add(cha);
            }
         }

         if (targetList.isEmpty()) {
            return true;
         } else {
            Creature target = targetList.get(Rnd.get(targetList.size()));
            int hate = effectedMob.getHating(this.getEffector());
            effectedMob.stopHating(this.getEffector());
            effectedMob.addDamageHate(target, 0, hate);
            return true;
         }
      } else {
         return false;
      }
   }
}
