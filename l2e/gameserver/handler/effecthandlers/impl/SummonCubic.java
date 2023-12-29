package l2e.gameserver.handler.effecthandlers.impl;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.CubicInstance;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class SummonCubic extends Effect {
   private final int _npcId;
   private final int _cubicPower;
   private final int _cubicDuration;
   private final int _cubicDelay;
   private final int _cubicMaxCount;
   private final int _cubicSkillChance;

   public SummonCubic(Env env, EffectTemplate template) {
      super(env, template);
      this._npcId = template.getParameters().getInteger("npcId", 0);
      this._cubicPower = template.getParameters().getInteger("cubicPower", 0);
      this._cubicDuration = template.getParameters().getInteger("cubicDuration", 0);
      this._cubicDelay = template.getParameters().getInteger("cubicDelay", 0);
      this._cubicMaxCount = template.getParameters().getInteger("cubicMaxCount", -1);
      this._cubicSkillChance = template.getParameters().getInteger("cubicSkillChance", 0);
      if (this._npcId > 0) {
         template.setCubicId(this._npcId);
      }
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.SUMMON_CUBIC;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected() == null
         || !this.getEffected().isPlayer()
         || this.getEffected().isAlikeDead()
         || this.getEffected().getActingPlayer().inObserverMode()) {
         return false;
      } else if (this._npcId <= 0) {
         _log.warning(SummonCubic.class.getSimpleName() + ": Invalid NPC Id:" + this._npcId + " in skill Id: " + this.getSkill().getId());
         return false;
      } else {
         Player player = this.getEffected().getActingPlayer();
         if (!player.inObserverMode() && !player.isMounted()) {
            int _cubicSkillLevel = this.getSkill().getLevel();
            if (_cubicSkillLevel > 100) {
               _cubicSkillLevel = (this.getSkill().getLevel() - 100) / 7 + 8;
            }

            CubicInstance cubic = player.getCubicById(this._npcId);
            if (cubic != null) {
               cubic.stopAction();
               cubic.cancelDisappear();
               player.getCubics().remove(cubic.getId());
            } else {
               Effect cubicMastery = player.getFirstPassiveEffect(EffectType.CUBIC_MASTERY);
               int cubicCount = (int)(cubicMastery != null ? cubicMastery.calc() - 1.0 : 0.0);
               if (player.getCubics().size() > cubicCount) {
                  int random = Rnd.get(player.getCubics().size());

                  for(CubicInstance removedCubic : player.getCubics().values()) {
                     if (--random < 0) {
                        removedCubic.stopAction();
                        removedCubic.cancelDisappear();
                        player.getCubics().remove(removedCubic.getId());
                        break;
                     }
                  }
               }
            }

            player.addCubic(
               this._npcId,
               _cubicSkillLevel,
               (double)this._cubicPower,
               this._cubicDelay,
               this._cubicSkillChance,
               this._cubicMaxCount,
               this._cubicDuration,
               this.getEffected() != this.getEffector()
            );
            player.broadcastUserInfo(true);
            return true;
         } else {
            return false;
         }
      }
   }
}
