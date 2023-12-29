package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Ranger;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.quest.Quest;

public class AncientRanger extends Ranger {
   public AncientRanger(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      Attackable npc = this.getActiveChar();
      if (Rnd.get(1000) < 2) {
         Attackable box = (Attackable)Quest.addSpawn(18693, npc.getX(), npc.getY(), npc.getZ(), 0, false, 300000L);
         int x = box.getX();
         int y = box.getY();
         Attackable guard1 = (Attackable)Quest.addSpawn(18694, x + 50, y + 50, npc.getZ(), 0, false, 300000L);
         guard1.addDamageHate(killer, 0, 999);
         guard1.getAI().setIntention(CtrlIntention.ATTACK, killer);
         Attackable guard2 = (Attackable)Quest.addSpawn(18695, x + 50, y - 50, npc.getZ(), 0, false, 300000L);
         guard2.addDamageHate(killer, 0, 999);
         guard2.getAI().setIntention(CtrlIntention.ATTACK, killer);
         Attackable guard3 = (Attackable)Quest.addSpawn(18695, x - 50, y + 50, npc.getZ(), 0, false, 300000L);
         guard3.addDamageHate(killer, 0, 999);
         guard3.getAI().setIntention(CtrlIntention.ATTACK, killer);
         Attackable guard4 = (Attackable)Quest.addSpawn(18694, x - 50, y - 50, npc.getZ(), 0, false, 300000L);
         guard4.addDamageHate(killer, 0, 999);
         guard4.getAI().setIntention(CtrlIntention.ATTACK, killer);
      }

      super.onEvtDead(killer);
   }
}
