package l2e.gameserver.model;

import java.util.concurrent.Future;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;

public final class FusionSkill {
   protected static final Logger _log = Logger.getLogger(FusionSkill.class.getName());
   protected int _skillCastRange;
   protected int _fusionId;
   protected int _fusionLevel;
   protected Creature _caster;
   protected Creature _target;
   protected Future<?> _geoCheckTask;

   public Creature getCaster() {
      return this._caster;
   }

   public Creature getTarget() {
      return this._target;
   }

   public FusionSkill(Creature caster, Creature target, Skill skill) {
      this._skillCastRange = skill.getCastRange();
      this._caster = caster;
      this._target = target;
      this._fusionId = skill.getTriggeredId();
      this._fusionLevel = skill.getTriggeredLevel();
      Effect effect = this._target.getFirstEffect(this._fusionId);
      if (effect != null) {
         effect.increaseEffect();
      } else {
         Skill force = SkillsParser.getInstance().getInfo(this._fusionId, this._fusionLevel);
         if (force != null) {
            force.getEffects(this._caster, this._target, null, true);
         } else {
            _log.warning("Triggered skill [" + this._fusionId + ";" + this._fusionLevel + "] not found!");
         }
      }

      this._geoCheckTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new FusionSkill.GeoCheckTask(), 1000L, 1000L);
   }

   public void onCastAbort() {
      this._caster.setFusionSkill(null);
      Effect effect = this._target.getFirstEffect(this._fusionId);
      if (effect != null) {
         effect.decreaseForce();
      }

      this._geoCheckTask.cancel(true);
   }

   public class GeoCheckTask implements Runnable {
      @Override
      public void run() {
         try {
            if (!Util.checkIfInRange(FusionSkill.this._skillCastRange, FusionSkill.this._caster, FusionSkill.this._target, true)) {
               FusionSkill.this._caster.abortCast();
            }

            if (!GeoEngine.canSeeTarget(FusionSkill.this._caster, FusionSkill.this._target, false)) {
               FusionSkill.this._caster.abortCast();
            }
         } catch (Exception var2) {
         }
      }
   }
}
