package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class EnergyDamOverTime extends Effect {
   public EnergyDamOverTime(Env env, EffectTemplate template) {
      super(env, template);
   }

   public EnergyDamOverTime(Env env, Effect effect) {
      super(env, effect);
   }

   @Override
   public boolean canBeStolen() {
      return !this.getSkill().isToggle();
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.ENERGY_DAM_OVER_TIME;
   }

   @Override
   public boolean onActionTime() {
      if (!this.getEffected().isDead() && this.getEffected().getActingPlayer().getAgathionId() != 0) {
         ItemInstance item = this.getEffected().getInventory().getPaperdollItem(15);
         if (item == null) {
            return false;
         } else {
            double energyDam = this.calc();
            if (energyDam > (double)item.getAgathionEnergy()) {
               this.getEffected().sendPacket(SystemMessageId.THE_SKILL_HAS_BEEN_CANCELED_BECAUSE_YOU_HAVE_INSUFFICIENT_ENERGY);
               return false;
            } else {
               item.setAgathionEnergy((int)((double)item.getAgathionEnergy() - energyDam));
               return this.getSkill().isToggle();
            }
         }
      } else {
         return false;
      }
   }
}
