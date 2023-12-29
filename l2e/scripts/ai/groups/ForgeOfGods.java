package l2e.scripts.ai.groups;

import l2e.commons.util.NpcUtils;
import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import org.apache.commons.lang.ArrayUtils;

public class ForgeOfGods extends Fighter {
   private static final int[] RANDOM_SPAWN_MOBS = new int[]{18799, 18800, 18801, 18802, 18803};
   private static final int[] FOG_MOBS = new int[]{
      22634, 22635, 22636, 22637, 22638, 22639, 22640, 22641, 22642, 22643, 22644, 22645, 22646, 22647, 22648, 22649
   };

   public ForgeOfGods(Attackable actor) {
      super(actor);
      if (ArrayUtils.contains(RANDOM_SPAWN_MOBS, actor.getId())) {
         actor.setIsImmobilized(true);
      }
   }

   @Override
   protected void onEvtDead(Creature killer) {
      if (ArrayUtils.contains(FOG_MOBS, this.getActiveChar().getId()) && Rnd.chance(30)) {
         MonsterInstance npc = NpcUtils.spawnSingle(RANDOM_SPAWN_MOBS[Rnd.get(RANDOM_SPAWN_MOBS.length)], this.getActiveChar().getLocation());
         npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, Integer.valueOf(Rnd.get(1, 100)));
      }

      super.onEvtDead(killer);
   }
}
