package l2e.scripts.ai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class SinWardens extends AbstractNpcAI {
   private static final int[] SIN_WARDEN_MINIONS = new int[]{22424, 22425, 22426, 22427, 22428, 22429, 22430, 22432, 22433, 22434, 22435, 22436, 22437, 22438};
   private final Map<Integer, Integer> killedMinionsCount = new ConcurrentHashMap<>();

   private SinWardens(String name, String descr) {
      super(name, descr);
      this.addKillId(SIN_WARDEN_MINIONS);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (npc.isMinion()) {
         Creature master = ((Attackable)npc).getLeader();
         if (master != null && !master.isDead()) {
            int killedCount = this.killedMinionsCount.containsKey(master.getObjectId()) ? this.killedMinionsCount.get(master.getObjectId()) : 0;
            if (++killedCount == 5) {
               master.broadcastPacket(
                  new NpcSay(master.getObjectId(), 22, master.getId(), NpcStringId.WE_MIGHT_NEED_NEW_SLAVES_ILL_BE_BACK_SOON_SO_WAIT), 2000
               );
               master.doDie(killer);
               this.killedMinionsCount.remove(master.getObjectId());
            } else {
               this.killedMinionsCount.put(master.getObjectId(), killedCount);
            }
         }
      }

      return super.onKill(npc, killer, isSummon);
   }

   public static void main(String[] args) {
      new SinWardens(SinWardens.class.getSimpleName(), "ai");
   }
}
