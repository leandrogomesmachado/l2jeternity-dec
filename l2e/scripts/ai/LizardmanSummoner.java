package l2e.scripts.ai;

import l2e.commons.util.NpcUtils;
import l2e.commons.util.PositionUtils;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Mystic;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.MonsterInstance;

public class LizardmanSummoner extends Mystic {
   public LizardmanSummoner(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && actor.isScriptValue(0) && attacker.isPlayable()) {
         actor.setScriptValue(1);

         for(int i = 0; i < 2; ++i) {
            MonsterInstance npc = NpcUtils.spawnSingle(22768, Location.findPointToStay(actor, 100, 120, true));
            if (npc != null) {
               npc.setHeading(PositionUtils.calculateHeadingFrom(npc, attacker));
               npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(1000));
            }
         }
      }

      super.onEvtAttacked(attacker, damage);
   }
}
