package l2e.gameserver.model.skills.effects;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.handler.effecthandlers.EffectHandler;
import l2e.gameserver.model.ChanceCondition;
import l2e.gameserver.model.skills.conditions.Condition;
import l2e.gameserver.model.skills.funcs.FuncTemplate;
import l2e.gameserver.model.skills.funcs.Lambda;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.StatsSet;

public class EffectTemplate {
   private static final Logger _log = Logger.getLogger(EffectTemplate.class.getName());
   private final Class<?> _handler;
   private final Constructor<?> _constructor;
   private final Condition _attachCond;
   private final Lambda _lambda;
   public int _totalTickCount;
   private final int _abnormalTime;
   private final AbnormalEffect[] _abnormalEffect;
   public FuncTemplate[] funcTemplates;
   private final boolean _showIcon;
   private final String _name;
   private final double _effectPower;
   private final int _triggeredId;
   private final int _triggeredLevel;
   private final ChanceCondition _chanceCondition;
   private final StatsSet _parameters;
   private final boolean _isReflectable;
   public final String abnormalType;
   public final byte abnormalLvl;
   private int _cubicId = 0;

   public EffectTemplate(Condition attachCond, Condition applyCond, Lambda lambda, String pAbnormalType, byte pAbnormalLvl, StatsSet set, StatsSet params) {
      this._attachCond = attachCond;
      this._lambda = lambda;
      this._name = set.getString("name");
      this._totalTickCount = set.getInteger("ticks", 0);
      this._abnormalTime = set.getInteger("abnormalTime", 0);
      this.abnormalType = pAbnormalType;
      this.abnormalLvl = pAbnormalLvl;
      String[] specialEffects = set.getString("abnormalVisualEffect", "").split(",");
      this._abnormalEffect = new AbnormalEffect[specialEffects.length];

      for(int i = 0; i < specialEffects.length; ++i) {
         this._abnormalEffect[i] = AbnormalEffect.getByName(specialEffects[i]);
      }

      this._showIcon = set.getInteger("noicon", 0) == 0;
      this._effectPower = set.getDouble("effectPower", -1.0);
      this._triggeredId = set.getInteger("triggeredId", 0);
      this._triggeredLevel = set.getInteger("triggeredLevel", 1);
      this._isReflectable = set.getBool("isReflectable", true);
      this._chanceCondition = ChanceCondition.parse(
         set.getString("chanceType", null),
         set.getInteger("activationChance", -1),
         set.getInteger("activationMinDamage", -1),
         set.getString("activationElements", null),
         set.getString("activationSkills", null),
         set.getBool("pvpChanceOnly", false)
      );
      this._parameters = params;
      this._handler = EffectHandler.getInstance().getHandler(this._name);
      if (this._handler == null) {
         throw new RuntimeException(this.getClass().getSimpleName() + ": Requested unexistent effect handler: " + this._name);
      } else {
         try {
            this._constructor = this._handler.getConstructor(Env.class, EffectTemplate.class);
         } catch (NoSuchMethodException var10) {
            throw new RuntimeException(var10);
         }
      }
   }

   public Effect getEffect(Env env) {
      return this.getEffect(env, false);
   }

   public Effect getEffect(Env env, boolean ignoreTest) {
      if (!ignoreTest && this._attachCond != null && !this._attachCond.test(env)) {
         return null;
      } else {
         try {
            return (Effect)this._constructor.newInstance(env, this);
         } catch (InstantiationException | IllegalAccessException var4) {
            _log.log(Level.WARNING, "", (Throwable)var4);
            return null;
         } catch (InvocationTargetException var5) {
            _log.log(
               Level.WARNING,
               "Error creating new instance of Class " + this._handler + " Exception was: " + var5.getTargetException().getMessage(),
               var5.getTargetException()
            );
            return null;
         }
      }
   }

   public Effect getStolenEffect(Env env, Effect stolen) {
      Constructor<?> stolenCons;
      try {
         stolenCons = this._handler.getConstructor(Env.class, Effect.class);
      } catch (NoSuchMethodException var7) {
         throw new RuntimeException(var7);
      }

      try {
         return (Effect)stolenCons.newInstance(env, stolen);
      } catch (InstantiationException | IllegalAccessException var5) {
         _log.log(Level.WARNING, "", (Throwable)var5);
         return null;
      } catch (InvocationTargetException var6) {
         _log.log(
            Level.WARNING,
            "Error creating new instance of Class " + this._handler + " Exception was: " + var6.getTargetException().getMessage(),
            var6.getTargetException()
         );
         return null;
      }
   }

   public void attach(FuncTemplate f) {
      if (this.funcTemplates == null) {
         this.funcTemplates = new FuncTemplate[]{f};
      } else {
         int len = this.funcTemplates.length;
         FuncTemplate[] tmp = new FuncTemplate[len + 1];
         System.arraycopy(this.funcTemplates, 0, tmp, 0, len);
         tmp[len] = f;
         this.funcTemplates = tmp;
      }
   }

   public Lambda getLambda() {
      return this._lambda;
   }

   public int getTotalTickCount() {
      return this._totalTickCount;
   }

   public int getAbnormalTime() {
      return this._abnormalTime;
   }

   public String getName() {
      return this._name;
   }

   public AbnormalEffect[] getAbnormalEffect() {
      return this._abnormalEffect;
   }

   public FuncTemplate[] getFuncTemplates() {
      return this.funcTemplates;
   }

   public boolean isIconDisplay() {
      return this._showIcon;
   }

   public double getEffectPower() {
      return this._effectPower;
   }

   public int getTriggeredId() {
      return this._triggeredId;
   }

   public int getTriggeredLevel() {
      return this._triggeredLevel;
   }

   public ChanceCondition getChanceCondition() {
      return this._chanceCondition;
   }

   public StatsSet getParameters() {
      return this._parameters;
   }

   public boolean hasParameters() {
      return this._parameters != null;
   }

   public final boolean isReflectable() {
      return this._isReflectable;
   }

   @Override
   public String toString() {
      return "Effect template[" + this._handler + "]";
   }

   public int getCubicId() {
      return this._cubicId;
   }

   public void setCubicId(int npcId) {
      this._cubicId = npcId;
   }
}
