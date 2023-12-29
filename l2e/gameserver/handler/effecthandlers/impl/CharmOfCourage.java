package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.serverpackets.EtcStatusUpdate;

public class CharmOfCourage extends Effect {
   public CharmOfCourage(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.CHARM_OF_COURAGE.getMask();
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.CHARMOFCOURAGE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected().isPlayer()) {
         this.getEffected().sendPacket(new EtcStatusUpdate(this.getEffected().getActingPlayer()));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onExit() {
      if (this.getEffected().isPlayer()) {
         this.getEffected().sendPacket(new EtcStatusUpdate(this.getEffected().getActingPlayer()));
      }
   }
}
