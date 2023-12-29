package l2e.scripts.ai.gracia;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;

public class FeralHound extends Fighter {
   public FeralHound(Attackable actor) {
      super(actor);
      actor.setIsInvul(true);
      actor.setRandomAnimationEnabled(false);
      actor.setIsNoRndWalk(true);
   }
}
