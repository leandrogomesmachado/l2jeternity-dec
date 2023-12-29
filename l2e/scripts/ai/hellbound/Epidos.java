package l2e.scripts.ai.hellbound;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.MinionList;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.MinionData;
import l2e.gameserver.model.actor.templates.npc.MinionTemplate;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.scripts.ai.AbstractNpcAI;

public class Epidos extends AbstractNpcAI {
   private static final int[] EPIDOSES = new int[]{25609, 25610, 25611, 25612};
   private static final int[] MINIONS = new int[]{25605, 25606, 25607, 25608};
   private static final int[] MINIONS_COUNT = new int[]{3, 6, 11};
   private static final int NAIA_CUBE = 32376;
   private final Map<Integer, Double> _lastHp = new ConcurrentHashMap<>();

   private Epidos(String name, String descr) {
      super(name, descr);
      this.addKillId(EPIDOSES);
      this.addSpawnId(EPIDOSES);
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("check_minions")) {
         if (getRandom(1000) > 250 && this._lastHp.containsKey(npc.getObjectId())) {
            int hpDecreasePercent = (int)((this._lastHp.get(npc.getObjectId()) - npc.getCurrentHp()) * 100.0 / npc.getMaxHp());
            int minionsCount = 0;
            int spawnedMinions = 0;
            MinionList ml = npc.getMinionList();
            if (ml != null) {
               spawnedMinions = npc.getMinionList().getAliveMinions().size();
            }

            if (hpDecreasePercent > 5 && hpDecreasePercent <= 15 && spawnedMinions <= 9) {
               minionsCount = MINIONS_COUNT[0];
            } else if ((hpDecreasePercent > 1 && hpDecreasePercent <= 5 || hpDecreasePercent > 15 && hpDecreasePercent <= 30) && spawnedMinions <= 6) {
               minionsCount = MINIONS_COUNT[1];
            } else if (spawnedMinions == 0) {
               minionsCount = MINIONS_COUNT[2];
            }

            for(int i = 0; i < minionsCount; ++i) {
               npc.getMinionList().addMinion(new MinionData(new MinionTemplate(MINIONS[Arrays.binarySearch(EPIDOSES, npc.getId())], 1)), true);
            }

            this._lastHp.put(npc.getObjectId(), npc.getCurrentHp());
         }

         this.startQuestTimer("check_minions", 10000L, npc, null);
      } else if (event.equalsIgnoreCase("check_idle")) {
         if (npc.getAI().getIntention() == CtrlIntention.ACTIVE) {
            npc.deleteMe();
         } else {
            this.startQuestTimer("check_idle", 600000L, npc, null);
         }
      }

      return null;
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (npc.isInsideRadius(-45474, 247450, -13994, 2000, true, false)) {
         Npc teleCube = addSpawn(32376, -45482, 246277, -14184, 0, false, 0L, false);
         teleCube.broadcastPacket(
            new NpcSay(teleCube.getObjectId(), 22, teleCube.getObjectId(), "Teleportation to Beleth Throne Room is available for 2 minutes."), 2000
         );
      }

      this._lastHp.remove(npc.getObjectId());
      MinionList ml = npc.getMinionList();
      if (ml != null) {
         ml.deleteMinions();
      }

      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public final String onSpawn(Npc npc) {
      this.startQuestTimer("check_minions", 10000L, npc, null);
      this.startQuestTimer("check_idle", 600000L, npc, null);
      this._lastHp.put(npc.getObjectId(), npc.getMaxHp());
      return super.onSpawn(npc);
   }

   public static void main(String[] args) {
      new Epidos(Epidos.class.getSimpleName(), "ai");
   }
}
