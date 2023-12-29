package l2e.scripts.ai;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.NpcStringId;

public class Rooney extends AbstractNpcAI {
   private static final int NPC_ID = 32049;
   private static final Location[] LOCATIONS = new Location[]{
      new Location(175937, -112167, -5550),
      new Location(178896, -112425, -5860),
      new Location(180628, -115992, -6135),
      new Location(183010, -114753, -6135),
      new Location(184496, -116773, -6135),
      new Location(181857, -109491, -5865),
      new Location(178917, -107633, -5853),
      new Location(178804, -110080, -5853),
      new Location(182221, -106806, -6025),
      new Location(186488, -109715, -5915),
      new Location(183847, -119231, -3113),
      new Location(185193, -120342, -3113),
      new Location(188047, -120867, -3113),
      new Location(189734, -120471, -3113),
      new Location(188754, -118940, -3313),
      new Location(190022, -116803, -3313),
      new Location(188443, -115814, -3313),
      new Location(186421, -114614, -3313),
      new Location(185188, -113307, -3313),
      new Location(187378, -112946, -3313),
      new Location(189815, -113425, -3313),
      new Location(189301, -111327, -3313),
      new Location(190289, -109176, -3313),
      new Location(187783, -110478, -3313),
      new Location(185889, -109990, -3313),
      new Location(181881, -109060, -3695),
      new Location(183570, -111344, -3675),
      new Location(182077, -112567, -3695),
      new Location(180127, -112776, -3698),
      new Location(179155, -108629, -3695),
      new Location(176282, -109510, -3698),
      new Location(176071, -113163, -3515),
      new Location(179376, -117056, -3640),
      new Location(179760, -115385, -3640),
      new Location(177950, -119691, -4140),
      new Location(177037, -120820, -4340),
      new Location(181125, -120148, -3702),
      new Location(182212, -117969, -3352),
      new Location(186074, -118154, -3312)
   };

   public Rooney(String name, String descr) {
      super(name, descr);
      Npc npc = addSpawn(32049, LOCATIONS[getRandom(LOCATIONS.length)], false, 0L);
      this.startQuestTimer("checkArea", 1000L, npc, null, true);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      switch(event) {
         case "checkArea":
            if (!World.getInstance().getAroundPlayers(npc, 300, 200).isEmpty()) {
               this.cancelQuestTimers("checkArea");
               this.broadcastNpcSay(npc, 22, NpcStringId.WELCOME);
               this.startQuestTimer("say1", 60000L, npc, null);
            }
            break;
         case "say1":
            this.broadcastNpcSay(npc, 22, NpcStringId.HURRY_HURRY);
            this.startQuestTimer("say2", 60000L, npc, null);
            break;
         case "say2":
            this.broadcastNpcSay(npc, 22, NpcStringId.I_AM_NOT_THAT_TYPE_OF_PERSON_WHO_STAYS_IN_ONE_PLACE_FOR_A_LONG_TIME);
            this.startQuestTimer("say3", 60000L, npc, null);
            break;
         case "say3":
            this.broadcastNpcSay(npc, 22, NpcStringId.ITS_HARD_FOR_ME_TO_KEEP_STANDING_LIKE_THIS);
            this.startQuestTimer("say4", 60000L, npc, null);
            break;
         case "say4":
            this.broadcastNpcSay(npc, 22, NpcStringId.WHY_DONT_I_GO_THAT_WAY_THIS_TIME);
            this.startQuestTimer("teleport", 60000L, npc, null);
            break;
         case "teleport":
            npc.teleToLocation(LOCATIONS[getRandom(LOCATIONS.length)], false);
            this.startQuestTimer("checkArea", 1000L, npc, null, true);
      }

      return null;
   }

   public static void main(String[] args) {
      new Rooney(Rooney.class.getSimpleName(), "ai");
   }
}
