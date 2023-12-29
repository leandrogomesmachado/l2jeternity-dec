package l2e.gameserver.handler.effecthandlers.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;

public class CancelProbability extends Effect {
   private final String _dispel;
   private final Map<String, Short> _dispelAbnormals;
   private final int _rate;

   public CancelProbability(Env env, EffectTemplate template) {
      super(env, template);
      this._dispel = template.getParameters().getString("dispel", null);
      this._rate = template.getParameters().getInteger("rate", 0);
      if (this._dispel != null && !this._dispel.isEmpty()) {
         this._dispelAbnormals = new ConcurrentHashMap<>();

         for(String ngtStack : this._dispel.split(";")) {
            String[] ngt = ngtStack.split(",");
            this._dispelAbnormals.put(ngt[0], ngt.length > 1 ? Short.parseShort(ngt[1]) : 32767);
         }
      } else {
         this._dispelAbnormals = Collections.emptyMap();
      }
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.CANCEL;
   }

   @Override
   public boolean isInstant() {
      return true;
   }

   @Override
   public boolean onStart() {
      if (this._dispelAbnormals.isEmpty()) {
         return false;
      } else {
         Creature target = this.getEffected();
         if (target != null && !target.isDead()) {
            for(Entry<String, Short> value : this._dispelAbnormals.entrySet()) {
               String stackType = value.getKey();
               float stackOrder = (float)value.getValue().shortValue();
               int skillCast = this.getSkill().getId();

               for(Effect e : target.getAllEffects()) {
                  if (e.getSkill().canBeDispeled()
                     && Formulas.calcStealSuccess(this.getEffector(), target, this.getSkill(), (double)this._rate)
                     && stackType.equalsIgnoreCase(e.getAbnormalType())
                     && e.getSkill().getId() != skillCast
                     && e.getSkill() != null) {
                     if (e.triggersChanceSkill()) {
                        target.removeChanceEffect(e);
                     }

                     if (stackOrder == -1.0F) {
                        target.stopSkillEffects(e.getSkill().getId());
                     } else if (stackOrder >= (float)e.getAbnormalLvl()) {
                        target.stopSkillEffects(e.getSkill().getId());
                     }
                  }
               }
            }

            return true;
         } else {
            return false;
         }
      }
   }
}
