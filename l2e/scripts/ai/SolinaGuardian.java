package l2e.scripts.ai;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Attackable;

public class SolinaGuardian extends Fighter {
   public SolinaGuardian(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      super.onEvtSpawn();
      this.getActiveChar().makeTriggerCast(SkillsParser.getInstance().getInfo(6371, 1), this.getActiveChar());
   }
}
