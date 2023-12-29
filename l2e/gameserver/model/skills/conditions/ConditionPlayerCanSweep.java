package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.Config;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class ConditionPlayerCanSweep extends Condition {
   private final boolean _val;

   public ConditionPlayerCanSweep(boolean val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      boolean canSweep = false;
      if (env.getPlayer() != null) {
         Player sweeper = env.getPlayer();
         Skill sweep = env.getSkill();
         if (sweep != null) {
            GameObject[] targets = sweep.getTargetList(sweeper);
            if (targets != null) {
               if (sweep.getId() == 444) {
                  for(GameObject objTarget : targets) {
                     if (objTarget instanceof Attackable) {
                        Attackable target = (Attackable)objTarget;
                        if (target.isDead()
                           && target.isSpoil()
                           && target.checkSpoilOwner(sweeper, false)
                           && !target.isOldCorpse(sweeper, Config.MAX_SWEEPER_TIME * 1000, false)) {
                           canSweep = sweeper.getInventory().checkInventorySlotsAndWeight(target.getSpoilLootItems(), true, true);
                        }
                     }
                  }
               } else {
                  for(GameObject objTarget : targets) {
                     if (objTarget instanceof Attackable) {
                        Attackable target = (Attackable)objTarget;
                        if (target.isDead()) {
                           if (target.isSpoil()) {
                              canSweep = target.checkSpoilOwner(sweeper, true);
                              canSweep &= !target.isOldCorpse(sweeper, Config.MAX_SWEEPER_TIME * 1000, true);
                              canSweep &= sweeper.getInventory().checkInventorySlotsAndWeight(target.getSpoilLootItems(), true, true);
                           } else {
                              sweeper.sendPacket(SystemMessageId.SWEEPER_FAILED_TARGET_NOT_SPOILED);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return this._val == canSweep;
   }
}
