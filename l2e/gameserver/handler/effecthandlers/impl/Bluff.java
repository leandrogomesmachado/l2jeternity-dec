package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.instance.NpcInstance;
import l2e.gameserver.model.actor.instance.SiegeSummonInstance;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.network.serverpackets.FinishRotatings;
import l2e.gameserver.network.serverpackets.StartRotation;

public class Bluff extends Effect {
   private final int _chance;

   public Bluff(Env env, EffectTemplate template) {
      super(env, template);
      this._chance = template.hasParameters() ? template.getParameters().getInteger("chance", 100) : 100;
   }

   @Override
   public boolean calcSuccess() {
      return Formulas.calcProbability((double)this._chance, this.getEffector(), this.getEffected(), this.getSkill(), false);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected() instanceof NpcInstance) {
         return false;
      } else if (this.getEffected() instanceof Npc && ((Npc)this.getEffected()).getId() == 35062) {
         return false;
      } else if (this.getEffected() instanceof SiegeSummonInstance) {
         return false;
      } else {
         this.getEffected().broadcastPacket(new StartRotation(this.getEffected().getObjectId(), this.getEffected().getHeading(), 1, 65535));
         this.getEffected().broadcastPacket(new FinishRotatings(this.getEffected().getObjectId(), this.getEffector().getHeading(), 65535));
         this.getEffected().setHeading(this.getEffector().getHeading());
         return true;
      }
   }
}
