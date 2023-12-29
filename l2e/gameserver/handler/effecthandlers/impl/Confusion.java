package l2e.gameserver.handler.effecthandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectFlag;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class Confusion extends Effect {
   public Confusion(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public int getEffectFlags() {
      return EffectFlag.CONFUSED.getMask();
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   private Creature getRndTarget() {
      List<Creature> targetList = new ArrayList<>();

      for(Creature obj : World.getInstance().getAroundCharacters(this.getEffected(), 2000, 400)) {
         if ((this.getEffected().isMonster() && obj.isAttackable() || obj.isCreature()) && obj != this.getEffected() && obj != this.getEffector()) {
            targetList.add(obj);
         }
      }

      return !targetList.isEmpty() ? targetList.get(Rnd.nextInt(targetList.size())) : null;
   }

   @Override
   public boolean onStart() {
      this.getEffected().startConfused();
      Creature target = this.getRndTarget();
      if (target != null) {
         if (this.getEffected().isMonster()) {
            ((Attackable)this.getEffected()).addDamageHate(target, 1, 99999);
         }

         this.getEffected().setTarget(target);
         this.getEffected().getAI().setIntention(CtrlIntention.ATTACK, target);
      }

      return true;
   }

   @Override
   public void onExit() {
      this.getEffected().stopConfused();
      if (this.getEffected().isMonster() && this.getEffected().getTarget() != null && this.getEffected().getTarget().isMonster()) {
         ((Attackable)this.getEffected()).stopHating((Creature)this.getEffected().getTarget());
         this.getEffected().breakAttack();
         this.getEffected().abortCast();
         ((Attackable)this.getEffected().getTarget()).stopHating(this.getEffected());
         ((Attackable)this.getEffected().getTarget()).breakAttack();
         ((Attackable)this.getEffected().getTarget()).abortCast();
      }
   }
}
