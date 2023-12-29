package l2e.scripts.teleports;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.network.serverpackets.NpcSay;

public class TeleportToRaceTrack extends Quest {
   private static final Map<Integer, Integer> TELEPORTERS = new HashMap<>();
   private static final Location[] RETURN_LOCS = new Location[]{
      new Location(-80826, 149775, -3043),
      new Location(-12672, 122776, -3116),
      new Location(15670, 142983, -2705),
      new Location(83400, 147943, -3404),
      new Location(111409, 219364, -3545),
      new Location(82956, 53162, -1495),
      new Location(146331, 25762, -2018),
      new Location(116819, 76994, -2714),
      new Location(43835, -47749, -792),
      new Location(147930, -55281, -2728),
      new Location(87386, -143246, -1293),
      new Location(12882, 181053, -3560)
   };

   public TeleportToRaceTrack(int questId, String name, String descr) {
      super(questId, name, descr);
      TELEPORTERS.put(30059, 3);
      TELEPORTERS.put(30080, 4);
      TELEPORTERS.put(30177, 6);
      TELEPORTERS.put(30233, 8);
      TELEPORTERS.put(30256, 2);
      TELEPORTERS.put(30320, 1);
      TELEPORTERS.put(30848, 7);
      TELEPORTERS.put(30899, 5);
      TELEPORTERS.put(31320, 9);
      TELEPORTERS.put(31275, 10);
      TELEPORTERS.put(31964, 11);
      TELEPORTERS.put(31210, 12);

      for(int npcId : TELEPORTERS.keySet()) {
         this.addStartNpc(npcId);
         this.addTalkId(npcId);
      }

      this.addStartNpc(30995);
      this.addTalkId(30995);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = "";
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         if (TELEPORTERS.containsKey(npc.getId())) {
            if (BotFunctions.getInstance().isAutoTpByIdEnable(player)) {
               BotFunctions.getInstance().getAutoTeleportById(player, player.getLocation(), new Location(12661, 181687, -3560), 1000);
               st.setState((byte)1);
               st.set("id", String.valueOf(TELEPORTERS.get(npc.getId())));
               return null;
            }

            player.teleToLocation(12661, 181687, -3560, true);
            st.setState((byte)1);
            st.set("id", String.valueOf(TELEPORTERS.get(npc.getId())));
         } else if (npc.getId() == 30995) {
            if (st.getState() == 1 && st.getInt("id") > 0) {
               int return_id = st.getInt("id") - 1;
               if (return_id < 13) {
                  if (BotFunctions.getInstance().isAutoTpByIdEnable(player)) {
                     BotFunctions.getInstance().getAutoTeleportById(player, player.getLocation(), RETURN_LOCS[return_id], 1000);
                     st.unset("id");
                     return null;
                  }

                  player.teleToLocation(RETURN_LOCS[return_id], false);
                  st.unset("id");
               }
            } else {
               player.sendPacket(
                  new NpcSay(
                     npc.getObjectId(),
                     0,
                     npc.getId(),
                     "You've arrived here from a different way. I'll send you to Dion Castle Town which is the nearest town."
                  )
               );
               if (BotFunctions.getInstance().isAutoTpByIdEnable(player)) {
                  BotFunctions.getInstance().getAutoTeleportById(player, player.getLocation(), new Location(15670, 142983, -2700), 1000);
                  return null;
               }

               player.teleToLocation(15670, 142983, -2700, true);
            }

            st.exitQuest(true);
         }

         return "";
      }
   }

   public static void main(String[] args) {
      new TeleportToRaceTrack(-1, TeleportToRaceTrack.class.getSimpleName(), "teleports");
   }
}
