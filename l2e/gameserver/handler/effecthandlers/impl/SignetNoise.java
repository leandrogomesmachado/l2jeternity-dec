package l2e.gameserver.handler.effecthandlers.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.tasks.player.BuffsBackTask;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class SignetNoise extends Effect {
   protected ScheduledFuture<?> timerTask;

   public SignetNoise(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.SIGNET_GROUND;
   }

   @Override
   public void onExit() {
      if (this.timerTask != null) {
         this.timerTask.cancel(false);
      }

      if (this.getEffected() != null) {
         this.getEffected().deleteMe();
      }
   }

   @Override
   public boolean onStart() {
      this.timerTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new SignetNoise.TimerTask(), 2000L, 2000L);
      return true;
   }

   protected class TimerTask implements Runnable {
      @Override
      public void run() {
         Player caster = SignetNoise.this.getEffector().getActingPlayer();

         for(Creature target : World.getInstance().getAroundCharacters(SignetNoise.this.getEffected(), SignetNoise.this.getSkill().getAffectRange(), 300)) {
            if (target != null && target != caster && caster.canAttackCharacter(target)) {
               List<Effect> effects = new ArrayList<>();

               for(Effect effect : target.getAllEffects()) {
                  if (effect.getSkill().isDance()) {
                     if (Config.RESTORE_DISPEL_SKILLS) {
                        if (SignetNoise.this.getSkill().hasEffectType(EffectType.HEAL_OVER_TIME)
                           || SignetNoise.this.getSkill().hasEffectType(EffectType.CPHEAL_OVER_TIME)
                           || SignetNoise.this.getSkill().hasEffectType(EffectType.MANA_HEAL_OVER_TIME)) {
                           continue;
                        }

                        Effect efR = effect.getEffectTemplate().getEffect(new Env(effect.getEffector(), effect.getEffected(), effect.getSkill()));
                        efR.setCount(effect.getTickCount());
                        efR.setAbnormalTime(effect.getAbnormalTime());
                        efR.setFirstTime(effect.getTime());
                        effects.add(efR);
                     }

                     effect.exit();
                  }
               }

               if (Config.RESTORE_DISPEL_SKILLS && !effects.isEmpty()) {
                  ThreadPoolManager.getInstance()
                     .schedule(new BuffsBackTask(SignetNoise.this.getEffected().getActingPlayer(), effects), (long)(Config.RESTORE_DISPEL_SKILLS_TIME * 1000));
               }
            }
         }
      }
   }
}
