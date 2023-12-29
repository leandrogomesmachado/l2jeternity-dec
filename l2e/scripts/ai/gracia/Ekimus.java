package l2e.scripts.ai.gracia;

import l2e.gameserver.ai.npc.Mystic;
import l2e.gameserver.model.actor.Attackable;

public class Ekimus extends Mystic {
   public Ekimus(Attackable actor) {
      super(actor);
      actor.setRandomAnimationEnabled(false);
   }
}
