package l2e.scripts.ai.gracia;

import l2e.commons.util.NpcUtils;
import l2e.commons.util.Rnd;
import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.MonsterInstance;

public class WardofDeath extends DefaultAI {
   private static final int[] mobs = new int[]{22516, 22520, 22522, 22524};

   public WardofDeath(Attackable actor) {
      super(actor);
      actor.setIsImmobilized(true);
   }

   @Override
   protected boolean checkAggression(Creature target) {
      Attackable actor = this.getActiveChar();
      if (actor == null) {
         return false;
      } else if (!super.checkAggression(target)) {
         return false;
      } else {
         if (actor.getId() == 18667) {
            actor.doCast(SkillsParser.getInstance().getInfo(Rnd.get(5423, 5424), 9));
            actor.doDie(null);
         } else if (actor.getId() == 18668) {
            for(int i = 0; i < Rnd.get(1, 4); ++i) {
               MonsterInstance n = NpcUtils.spawnSingle(mobs[Rnd.get(mobs.length)], Location.findAroundPosition(actor, 60, 100), actor.getReflectionId(), 0L);
               if (target != null) {
                  n.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, Integer.valueOf(100));
               }
            }

            actor.doDie(null);
         }

         return true;
      }
   }
}
