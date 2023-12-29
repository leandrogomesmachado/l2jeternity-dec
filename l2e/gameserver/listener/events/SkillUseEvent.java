package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;

public class SkillUseEvent implements EventListener {
   private Creature _caster;
   private Skill _skill;
   private Creature _target;
   private GameObject[] _targets;

   public Creature getCaster() {
      return this._caster;
   }

   public void setCaster(Creature caster) {
      this._caster = caster;
   }

   public GameObject[] getTargets() {
      return this._targets;
   }

   public void setTargets(GameObject[] targets) {
      this._targets = targets;
   }

   public Skill getSkill() {
      return this._skill;
   }

   public void setSkill(Skill skill) {
      this._skill = skill;
   }

   public Creature getTarget() {
      return this._target;
   }

   public void setTarget(Creature target) {
      this._target = target;
   }
}
