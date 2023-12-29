package l2e.scripts.ai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.QuestState;
import l2e.scripts.custom.IOPRace;

public class PrisonGuards extends AbstractNpcAI {
   private static final String[] GUARDVARS = new String[]{"1st", "2nd", "3rd", "4th"};
   private final Map<Npc, Integer> _guards = new ConcurrentHashMap<>();

   private PrisonGuards(String name, String descr) {
      super(name, descr);
      int[] mob = new int[]{18367, 18368};
      this.registerMobs(mob);
      this._guards.put(addSpawn(18368, 160704, 184704, -3704, 49152, false, 0L), 0);
      this._guards.put(addSpawn(18368, 160384, 184704, -3704, 49152, false, 0L), 0);
      this._guards.put(addSpawn(18367, 160528, 185216, -3704, 49152, false, 0L), 0);
      this._guards.put(addSpawn(18368, 135120, 171856, -3704, 49152, false, 0L), 1);
      this._guards.put(addSpawn(18368, 134768, 171856, -3704, 49152, false, 0L), 1);
      this._guards.put(addSpawn(18367, 134928, 172432, -3704, 49152, false, 0L), 1);
      this._guards.put(addSpawn(18368, 146880, 151504, -2872, 49152, false, 0L), 2);
      this._guards.put(addSpawn(18368, 146366, 151506, -2872, 49152, false, 0L), 2);
      this._guards.put(addSpawn(18367, 146592, 151888, -2872, 49152, false, 0L), 2);
      this._guards.put(addSpawn(18368, 155840, 160448, -3352, 0, false, 0L), 3);
      this._guards.put(addSpawn(18368, 155840, 159936, -3352, 0, false, 0L), 3);
      this._guards.put(addSpawn(18367, 155578, 160177, -3352, 0, false, 0L), 3);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("Respawn")) {
         Npc newGuard = addSpawn(npc.getId(), npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ(), npc.getSpawn().getHeading(), false, 0L);
         int place = this._guards.get(npc);
         this._guards.remove(npc);
         this._guards.put(newGuard, place);
      }

      return null;
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      if (this._guards.containsKey(npc)) {
         this.startQuestTimer("Respawn", 20000L, npc, null);
      }

      if (npc.getId() == 18367) {
         QuestState qs = player.getQuestState(IOPRace.class.getSimpleName());
         if (qs != null && qs.getInt(GUARDVARS[this._guards.get(npc)]) != 1) {
            qs.set(GUARDVARS[this._guards.get(npc)], "1");
            qs.giveItems(10013, 1L);
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new PrisonGuards(PrisonGuards.class.getSimpleName(), "ai");
   }
}
