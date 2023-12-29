package l2e.gameserver.model.actor.tasks.character;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;

public final class MagicUseTask implements Runnable {
   private final Creature _character;
   private GameObject[] _targets;
   private final Skill _skill;
   private int _count;
   private int _hitTime;
   private int _coolTime;
   private int _phase;
   private final boolean _simultaneously;

   public MagicUseTask(Creature character, GameObject[] tgts, Skill s, int hit, int cool, boolean simultaneous) {
      this._character = character;
      this._targets = tgts;
      this._skill = s;
      this._count = 0;
      this._phase = 1;
      this._hitTime = hit;
      this._coolTime = cool;
      this._simultaneously = simultaneous;
   }

   @Override
   public void run() {
      if (this._character != null) {
         switch(this._phase) {
            case 1:
               this._character.onMagicLaunchedTimer(this);
               break;
            case 2:
               this._character.onMagicHitTimer(this);
               break;
            case 3:
               this._character.onMagicFinalizer(this);
         }
      }
   }

   public int getCount() {
      return this._count;
   }

   public int getPhase() {
      return this._phase;
   }

   public Skill getSkill() {
      return this._skill;
   }

   public int getHitTime() {
      return this._hitTime;
   }

   public int getCoolTime() {
      return this._coolTime;
   }

   public GameObject[] getTargets() {
      return this._targets;
   }

   public boolean isSimultaneous() {
      return this._simultaneously;
   }

   public void setCount(int count) {
      this._count = count;
   }

   public void setPhase(int phase) {
      this._phase = phase;
   }

   public void setHitTime(int skillTime) {
      this._hitTime = skillTime;
   }

   public void setCoolTime(int cool) {
      this._coolTime = cool;
   }

   public void setTargets(GameObject[] targets) {
      this._targets = targets;
   }
}
