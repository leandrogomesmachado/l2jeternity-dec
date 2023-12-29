package l2e.scripts.ai;

import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.instancemanager.ServerVariables;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.scripts.quests._604_DaimonTheWhiteEyedPart2;

public class Daimon extends Fighter {
   public Daimon(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      ServerVariables.set(_604_DaimonTheWhiteEyedPart2.class.getSimpleName(), String.valueOf(System.currentTimeMillis()));
      if (_604_DaimonTheWhiteEyedPart2._npc != null) {
         _604_DaimonTheWhiteEyedPart2._npc = null;
      }

      super.onEvtDead(killer);
   }
}
