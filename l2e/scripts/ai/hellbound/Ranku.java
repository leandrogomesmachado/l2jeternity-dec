package l2e.scripts.ai.hellbound;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.model.MinionList;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MinionInstance;
import l2e.gameserver.model.actor.templates.npc.MinionData;
import l2e.gameserver.model.actor.templates.npc.MinionTemplate;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.scripts.ai.AbstractNpcAI;

public class Ranku extends AbstractNpcAI {
   private static final int RANKU = 25542;
   private static final int MINION = 32305;
   private static final int MINION_2 = 25543;
   private static final Set<Integer> MY_TRACKING_SET = ConcurrentHashMap.newKeySet();

   private Ranku(String name, String descr) {
      super(name, descr);
      this.addAttackId(25542);
      this.addKillId(new int[]{25542, 32305});
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("checkup") && npc.getId() == 25542 && !npc.isDead()) {
         MinionList ml = npc.getMinionList();
         if (ml != null && ml.hasAliveMinions()) {
            for(MinionInstance minion : ml.getAliveMinions()) {
               if (minion != null && !minion.isDead() && MY_TRACKING_SET.contains(minion.getObjectId())) {
                  List<Player> players = World.getInstance().getAroundPlayers(minion);
                  Player killer = players.get(getRandom(players.size()));
                  minion.reduceCurrentHp(minion.getMaxHp() / 100.0, killer, null);
               }
            }
         }

         this.startQuestTimer("checkup", 1000L, npc, null);
      }

      return null;
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill) {
      if (npc.getId() == 25542) {
         MinionList ml = npc.getMinionList();
         if (ml != null && ml.hasAliveMinions()) {
            for(MinionInstance minion : ml.getAliveMinions()) {
               if (minion != null && !minion.isDead() && !MY_TRACKING_SET.contains(minion.getObjectId())) {
                  minion.broadcastPacket(new NpcSay(minion.getObjectId(), 22, minion.getId(), NpcStringId.DONT_KILL_ME_PLEASE_SOMETHINGS_STRANGLING_ME), 2000);
                  this.startQuestTimer("checkup", 1000L, npc, null);
                  MY_TRACKING_SET.add(minion.getObjectId());
               }
            }
         }
      }

      return super.onAttack(npc, attacker, damage, isSummon, skill);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (npc.getId() == 32305) {
         if (MY_TRACKING_SET.contains(npc.getObjectId())) {
            MY_TRACKING_SET.remove(npc.getObjectId());
         }

         Npc master = ((MinionInstance)npc).getLeader();
         if (master != null && !master.isDead()) {
            master.getMinionList().addMinion(new MinionData(new MinionTemplate(25543, 1)), true);
         }
      } else if (npc.getId() == 25542) {
         MinionList ml = npc.getMinionList();
         if (ml != null) {
            if (ml.hasAliveMinions()) {
               for(MinionInstance minion : ml.getAliveMinions()) {
                  if (minion != null && MY_TRACKING_SET.contains(minion.getObjectId())) {
                     MY_TRACKING_SET.remove(minion.getObjectId());
                  }
               }
            }

            ml.deleteMinions();
         }
      }

      return super.onKill(npc, killer, isSummon);
   }

   public static void main(String[] args) {
      new Ranku(Ranku.class.getSimpleName(), "ai");
   }
}
