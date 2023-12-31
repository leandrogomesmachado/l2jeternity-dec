package l2e.scripts.custom;

import java.util.HashMap;
import java.util.Map;
import l2e.commons.util.Util;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.spawn.Spawner;

public class RaidbossInfo extends Quest {
   private static final String qn = "RaidbossInfo";
   private static final int[] NPC = new int[]{
      31729,
      31730,
      31731,
      31732,
      31733,
      31734,
      31735,
      31736,
      31737,
      31738,
      31739,
      31740,
      31741,
      31742,
      31743,
      31744,
      31745,
      31746,
      31747,
      31748,
      31749,
      31750,
      31751,
      31752,
      31753,
      31754,
      31755,
      31756,
      31757,
      31758,
      31759,
      31760,
      31761,
      31762,
      31763,
      31764,
      31765,
      31766,
      31767,
      31768,
      31769,
      31770,
      31771,
      31772,
      31773,
      31774,
      31775,
      31776,
      31777,
      31778,
      31779,
      31780,
      31781,
      31782,
      31783,
      31784,
      31785,
      31786,
      31787,
      31788,
      31789,
      31790,
      31791,
      31792,
      31793,
      31794,
      31795,
      31796,
      31797,
      31798,
      31799,
      31800,
      31801,
      31802,
      31803,
      31804,
      31805,
      31806,
      31807,
      31808,
      31809,
      31810,
      31811,
      31812,
      31813,
      31814,
      31815,
      31816,
      31817,
      31818,
      31819,
      31820,
      31821,
      31822,
      31823,
      31824,
      31825,
      31826,
      31827,
      31828,
      31829,
      31830,
      31831,
      31832,
      31833,
      31834,
      31835,
      31836,
      31837,
      31838,
      31839,
      31840,
      31841,
      32337,
      32338,
      32339,
      32340
   };
   private static final Map<Integer, Location> RADAR = new HashMap<>();

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("RaidbossInfo");
      if (st == null) {
         return event;
      } else {
         if (Util.isDigit(event)) {
            htmltext = null;
            int rbid = Integer.parseInt(event);
            if (RADAR.containsKey(rbid)) {
               Location loc = RADAR.get(rbid);
               st.addRadar(loc.getX(), loc.getY(), loc.getZ());
            }

            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      return "info.htm";
   }

   public RaidbossInfo(int id, String name, String descr) {
      super(id, name, descr);

      for(int i : NPC) {
         this.addStartNpc(i);
         this.addTalkId(i);
      }

      for(NpcTemplate raid : NpcsParser.getInstance().getAllNpcOfClassType("RaidBoss")) {
         int x = 0;
         int y = 0;
         int z = 0;

         for(Spawner spawn : SpawnParser.getInstance().getSpawnData()) {
            if (spawn.getId() == raid.getId()) {
               x = spawn.getX();
               y = spawn.getY();
               z = spawn.getZ();
               break;
            }
         }

         RADAR.put(raid.getId(), new Location(x, y, z));
      }
   }

   public static void main(String[] args) {
      new RaidbossInfo(-1, "RaidbossInfo", "custom");
   }
}
