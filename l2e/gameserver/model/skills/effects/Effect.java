package l2e.gameserver.model.skills.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.ChanceCondition;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.interfaces.IChanceSkillTrigger;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.skills.funcs.FuncTemplate;
import l2e.gameserver.model.skills.funcs.Lambda;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.MagicSkillLaunched;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.SystemMessage;

public abstract class Effect implements IChanceSkillTrigger {
   protected static final Logger _log = Logger.getLogger(Effect.class.getName());
   private static final Func[] _emptyFunctionSet = new Func[0];
   private final Creature _effector;
   private final Creature _effected;
   private final Skill _skill;
   private final Lambda _lambda;
   private EffectState _state;
   protected int _periodStartTicks;
   protected int _periodFirstTime;
   private final EffectTemplate _template;
   private final FuncTemplate[] _funcTemplates;
   private int _tickCount;
   private final int _totalTickCount;
   private int _abnormalTime;
   private boolean _isSelfEffect = false;
   private boolean _isPassiveEffect = false;
   public boolean _preventExitUpdate;
   private volatile ScheduledFuture<?> _currentFuture;
   private boolean _inUse = false;
   private boolean _startConditionsCorrect = true;
   private boolean _isRemoved = false;
   private boolean _isReflectable = true;
   private final String _abnormalType;
   private final byte _abnormalLvl;

   protected Effect(Env env, EffectTemplate template) {
      this._state = EffectState.CREATED;
      this._skill = env.getSkill();
      this._template = template;
      this._effected = env.getTarget();
      this._effector = env.getCharacter();
      this._lambda = template.getLambda();
      this._funcTemplates = template.funcTemplates;
      this._totalTickCount = Formulas.calcEffectTickCount(env, template);
      this._tickCount = 0;
      this._abnormalTime = Formulas.calcEffectAbnormalTime(env, template);
      this._periodStartTicks = GameTimeController.getInstance().getGameTicks();
      this._periodFirstTime = 0;
      this._abnormalType = template.abnormalType;
      this._abnormalLvl = template.abnormalLvl;
      this._isReflectable = template.isReflectable();
   }

   protected Effect(Env env, Effect effect) {
      this._template = effect._template;
      this._state = EffectState.CREATED;
      this._skill = env.getSkill();
      this._effected = env.getTarget();
      this._effector = env.getCharacter();
      this._lambda = this._template.getLambda();
      this._funcTemplates = this._template.funcTemplates;
      this._totalTickCount = this._template._totalTickCount;
      this._tickCount = effect.getTickCount();
      this._abnormalTime = effect.getAbnormalTime();
      this._periodStartTicks = effect.getPeriodStartTicks();
      this._periodFirstTime = effect.getTime();
      this._abnormalType = this._template.abnormalType;
      this._abnormalLvl = this._template.abnormalLvl;
      this._isReflectable = effect.isReflectable();
   }

   public int getTickCount() {
      return this._tickCount;
   }

   public void setCount(int newTickCount) {
      this._tickCount = Math.min(newTickCount, this._totalTickCount);
   }

   public void setFirstTime(int newFirstTime) {
      this._periodFirstTime = Math.min(newFirstTime, this._abnormalTime);
      this._periodStartTicks -= this._periodFirstTime * 10;
   }

   public boolean isIconDisplay() {
      return this._template.isIconDisplay();
   }

   public void setAbnormalTime(int time) {
      this._abnormalTime = time;
   }

   public int getAbnormalTime() {
      return this._abnormalTime;
   }

   public int getTime() {
      return (GameTimeController.getInstance().getGameTicks() - this._periodStartTicks) / 10;
   }

   public int getTimeLeft() {
      return this._totalTickCount > 1 ? this._totalTickCount - this._tickCount - this.getTime() : this._abnormalTime - this.getTime();
   }

   public boolean isInUse() {
      return this._inUse;
   }

   public boolean setInUse(boolean inUse) {
      this._inUse = inUse;
      if (this._inUse) {
         this._startConditionsCorrect = this.onStart();
      } else {
         this.onExit();
      }

      return this._startConditionsCorrect;
   }

   public String getAbnormalType(String ngt) {
      return this._abnormalType;
   }

   public String getAbnormalType() {
      return this._abnormalType;
   }

   public byte getAbnormalLvl() {
      return this._abnormalLvl;
   }

   public final Skill getSkill() {
      return this._skill;
   }

   public final Creature getEffector() {
      return this._effector;
   }

   public final Creature getEffected() {
      return this._effected;
   }

   public boolean isSelfEffect() {
      return this._isSelfEffect;
   }

   public void setSelfEffect() {
      this._isSelfEffect = true;
   }

   public boolean isPassiveEffect() {
      return this._isPassiveEffect;
   }

   public void setPassiveEffect() {
      this._isPassiveEffect = true;
   }

   public final double calc() {
      Env env = new Env();
      env.setCharacter(this._effector);
      env.setTarget(this._effected);
      env.setSkill(this._skill);
      return this._lambda.calc(env);
   }

   public boolean calcSuccess() {
      Env env = new Env();
      env.setSkillMastery(Formulas.calcSkillMastery(this.getEffector(), this.getSkill()));
      env.setCharacter(this.getEffector());
      env.setTarget(this.getEffected());
      env.setSkill(this.getSkill());
      env.setEffect(this);
      return Formulas.calcEffectSuccess(env);
   }

   private final synchronized void startEffectTask() {
      this.stopEffectTask(true);
      int delay = Math.max((this._abnormalTime - this._periodFirstTime) * 1000, 5);
      if (this._totalTickCount > 0) {
         if (this.getSkill().isToggle()) {
            int period = (this._abnormalTime > 1 ? Math.max(this._abnormalTime / this._totalTickCount, 1) : this._totalTickCount) * 1000;
            this._currentFuture = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Effect.EffectTask(), (long)(delay / period), (long)period);
         } else {
            this._currentFuture = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Effect.EffectTask(), (long)delay, (long)(this._abnormalTime * 1000));
         }
      } else {
         this._currentFuture = ThreadPoolManager.getInstance().schedule(new Effect.EffectTask(), (long)delay);
      }

      if (this._state == EffectState.ACTING) {
         if (this.isSelfEffectType()) {
            this._effector.addEffect(this);
         } else {
            this._effected.addEffect(this);
         }
      }
   }

   public final void exit() {
      this.exit(false, true);
   }

   public final void exit(boolean preventExitUpdate, boolean printMessage) {
      this._preventExitUpdate = preventExitUpdate;
      this._state = EffectState.FINISHING;
      this.scheduleEffect(printMessage);
   }

   public final synchronized void stopEffectTask(boolean printMessage) {
      try {
         if (this._currentFuture != null) {
            this._currentFuture.cancel(false);
            this._currentFuture = null;
            if (this.isSelfEffectType() && this.getEffector() != null) {
               this.getEffector().removeEffect(this, printMessage);
            } else if (this.getEffected() != null) {
               this.getEffected().removeEffect(this, printMessage);
            }

            if (this.getEffected() != null && this.getEffected().hasSummon()) {
               this.getEffected().getSummon().removeEffect(this, printMessage);
            }
         }
      } catch (Exception var3) {
      }
   }

   public abstract EffectType getEffectType();

   public boolean onStart() {
      for(AbnormalEffect eff : this._template.getAbnormalEffect()) {
         if (eff != null && eff != AbnormalEffect.NONE) {
            this.getEffected().startAbnormalEffect(eff);
         }
      }

      return true;
   }

   public void onExit() {
      for(AbnormalEffect eff : this._template.getAbnormalEffect()) {
         if (eff != null && eff != AbnormalEffect.NONE) {
            this.getEffected().stopAbnormalEffect(eff);
         }
      }
   }

   public boolean onActionTime() {
      return this.getAbnormalTime() < 0;
   }

   public final void scheduleEffect(boolean printMessage) {
      switch(this._state) {
         case CREATED:
            this._state = EffectState.ACTING;
            if (this.getEffector().isPlayer() && this._skill.isDebuff() && Config.ALLOW_DEBUFF_INFO) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SUCCEEDED);
               sm.addSkillName(this._skill);
               this.getEffector().sendPacket(sm);
            }

            if (this._skill.isOffensive() && this.isIconDisplay() && this.getEffected().isPlayer()) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
               sm.addSkillName(this._skill);
               this.getEffected().sendPacket(sm);
            }

            this.getEffected().updateAbnormalEffect();
            if (this._abnormalTime != 0) {
               this.startEffectTask();
               return;
            } else {
               this._startConditionsCorrect = this.onStart();
            }
         case ACTING:
            if (this.isInUse()) {
               ++this._tickCount;
               if (this.onActionTime() && this._startConditionsCorrect) {
                  return;
               }
            }

            if (this._tickCount < this._totalTickCount && !this.isNevativeTime()) {
               return;
            } else {
               this._state = EffectState.FINISHING;
            }
         case FINISHING:
            if (this._currentFuture == null && this.getEffected() != null) {
               this.getEffected().removeEffect(this, printMessage);
            }

            this.stopEffectTask(printMessage);
            this.getEffected().updateAbnormalEffect();
            if ((this.isInUse() || this._tickCount <= 1 && this._abnormalTime <= 0 || this.isNevativeTime())
               && (this._startConditionsCorrect || this.isNevativeTime())) {
               this.onExit();
            }

            if (this._skill.getAfterEffectId() > 0) {
               Skill skill = SkillsParser.getInstance().getInfo(this._skill.getAfterEffectId(), this._skill.getAfterEffectLvl());
               if (skill != null) {
                  this.getEffected().broadcastPacket(new MagicSkillUse(this._effected, skill.getId(), skill.getLevel(), 0, 0));
                  this.getEffected().broadcastPacket(new MagicSkillLaunched(this._effected, skill.getId(), skill.getLevel()));
                  skill.getEffects(this.getEffected(), this.getEffected(), true);
               }
            }
      }
   }

   private boolean isNevativeTime() {
      return !this._skill.isToggle() && this.getTimeLeft() < 0;
   }

   public Func[] getStatFuncs() {
      if (this._funcTemplates == null) {
         return _emptyFunctionSet;
      } else {
         List<Func> funcs = new ArrayList<>(this._funcTemplates.length);
         Env env = new Env();
         env.setCharacter(this._effector);
         env.setTarget(this._effected);
         env.setSkill(this._skill);

         for(FuncTemplate t : this._funcTemplates) {
            Func f = t.getFunc(env, this);
            if (f != null) {
               funcs.add(f);
            }
         }

         return funcs.isEmpty() ? _emptyFunctionSet : funcs.toArray(new Func[funcs.size()]);
      }
   }

   public int getPeriodStartTicks() {
      return this._periodStartTicks;
   }

   public EffectTemplate getEffectTemplate() {
      return this._template;
   }

   public double getEffectPower() {
      return this._template.getEffectPower();
   }

   public boolean canBeStolen() {
      return !this.getSkill().isPassive()
         && this.getEffectType() != EffectType.TRANSFORMATION
         && !this.getSkill().isToggle()
         && !this.getSkill().isDebuff()
         && !this.getSkill().isHeroSkill()
         && !this.getSkill().isGMSkill()
         && (!this.getSkill().isStatic() || this.getSkill().getId() == 2341)
         && this.getSkill().canBeDispeled();
   }

   public int getEffectFlags() {
      return EffectFlag.NONE.getMask();
   }

   @Override
   public String toString() {
      return "Effect "
         + this.getClass().getSimpleName()
         + ", "
         + this._skill
         + ", State: "
         + this._state
         + ", Time: "
         + this._abnormalTime
         + ", Remaining: "
         + this.getTimeLeft();
   }

   public boolean isSelfEffectType() {
      return false;
   }

   public void decreaseForce() {
   }

   public void increaseEffect() {
   }

   public boolean checkCondition(Object obj) {
      return true;
   }

   @Override
   public boolean triggersChanceSkill() {
      return false;
   }

   @Override
   public int getTriggeredChanceId() {
      return 0;
   }

   @Override
   public int getTriggeredChanceLevel() {
      return 0;
   }

   @Override
   public ChanceCondition getTriggeredChanceCondition() {
      return null;
   }

   public boolean isInstant() {
      return false;
   }

   public boolean isRemoved() {
      return this._isRemoved;
   }

   public void setRemoved(boolean val) {
      this._isRemoved = val;
   }

   public boolean isReflectable() {
      return this._isReflectable;
   }

   protected final class EffectTask implements Runnable {
      @Override
      public void run() {
         try {
            Effect.this._periodFirstTime = 0;
            Effect.this._periodStartTicks = GameTimeController.getInstance().getGameTicks();
            Effect.this.scheduleEffect(true);
         } catch (Exception var2) {
            Effect._log.log(Level.SEVERE, "", (Throwable)var2);
         }
      }
   }
}
