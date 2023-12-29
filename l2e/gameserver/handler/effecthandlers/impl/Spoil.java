package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.network.SystemMessageId;

public class Spoil extends Effect {
   public Spoil(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.SPOIL;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected().isMonster() && !this.getEffected().isDead()) {
         MonsterInstance target = (MonsterInstance)this.getEffected();
         if (target.isSpoil()) {
            this.getEffector().sendPacket(SystemMessageId.ALREADY_SPOILED);
            return false;
         } else {
            if (Formulas.calcMagicSuccess(this.getEffector(), target, this.getSkill())) {
               target.setSpoil(true);
               target.setIsSpoiledBy(this.getEffector().getObjectId());
               this.getEffector().sendPacket(SystemMessageId.SPOIL_SUCCESS);
            }

            target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this.getEffector(), Integer.valueOf(0));
            return true;
         }
      } else {
         this.getEffector().sendPacket(SystemMessageId.INCORRECT_TARGET);
         return false;
      }
   }
}
