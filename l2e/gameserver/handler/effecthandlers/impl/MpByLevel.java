package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class MpByLevel extends Effect {
   public MpByLevel(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public boolean onStart() {
      if (this.getEffector() == null || this.getEffected() == null) {
         return false;
      } else if (!this.getEffected().isHealBlocked() && !this.getEffected().isDead()) {
         int abs = (int)this.calc();
         double absorb = this.getEffected().getCurrentMp() + (double)abs > this.getEffected().getMaxMp()
            ? this.getEffected().getMaxMp()
            : this.getEffected().getCurrentMp() + (double)abs;
         int restored = (int)(absorb - this.getEffected().getCurrentMp());
         this.getEffected().setCurrentMp(absorb);
         StatusUpdate su = new StatusUpdate(this.getEffected());
         su.addAttribute(11, (int)absorb);
         this.getEffected().sendPacket(su);
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED);
         sm.addNumber(restored);
         this.getEffected().sendPacket(sm);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.BUFF;
   }
}
