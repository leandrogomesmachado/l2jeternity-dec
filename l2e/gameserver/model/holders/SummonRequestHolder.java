package l2e.gameserver.model.holders;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;

public class SummonRequestHolder {
   private final Player _target;
   private final Skill _skill;
   private final boolean _isAdminRecall;

   public SummonRequestHolder(Player destination, Skill skill, boolean isAdminRecall) {
      this._target = destination;
      this._skill = skill;
      this._isAdminRecall = isAdminRecall;
   }

   public Player getTarget() {
      return this._target;
   }

   public Skill getSkill() {
      return this._skill;
   }

   public boolean isAdminRecall() {
      return this._isAdminRecall;
   }
}
