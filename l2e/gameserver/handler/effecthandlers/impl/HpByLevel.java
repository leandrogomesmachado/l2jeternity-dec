package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class HpByLevel extends Effect {
   public HpByLevel(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public boolean onStart() {
      if (this.getEffector() == null || this.getEffected() == null) {
         return false;
      } else if (this.getEffector().isHealBlocked()) {
         return false;
      } else {
         double abs = this.calc();
         double absorb = this.getEffector().getCurrentHp() + abs > this.getEffector().getMaxHp()
            ? this.getEffector().getMaxHp()
            : this.getEffector().getCurrentHp() + abs;
         int restored = (int)(absorb - this.getEffector().getCurrentHp());
         this.getEffector().setCurrentHp(absorb);
         StatusUpdate su = new StatusUpdate(this.getEffector());
         su.addAttribute(9, (int)absorb);
         this.getEffector().sendPacket(su);
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
         sm.addNumber(restored);
         this.getEffector().sendPacket(sm);
         return true;
      }
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.BUFF;
   }
}
