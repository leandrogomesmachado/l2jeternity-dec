package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _643_RiseandFalloftheElrokiTribe extends Quest {
   private static final Map<String, int[]> _requests = new HashMap<>();

   public _643_RiseandFalloftheElrokiTribe(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32106);
      this.addTalkId(new int[]{32106, 32117});
      this.addKillId(new int[]{22201, 22202, 22204, 22205, 22209, 22210, 22212, 22213, 22219, 22220, 22221, 22222, 22224, 22225, 22742, 22743, 22744, 22745});
      this.questItemIds = new int[]{8776};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         long count = st.getQuestItemsCount(8776);
         if (event.equalsIgnoreCase("32106-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("32117-03.htm")) {
            if (count >= 300L) {
               st.takeItems(8776, 300L);
               st.calcReward(this.getId(), 8, true);
            } else {
               htmltext = "32117-04.htm";
            }
         } else if (_requests.containsKey(event)) {
            if (count >= (long)((int[])_requests.get(event))[1]) {
               st.takeItems(8776, (long)((int[])_requests.get(event))[1]);
               st.calcReward(this.getId(), Integer.parseInt(event));
               htmltext = "32117-06.htm";
            } else {
               htmltext = "32117-07.htm";
            }
         } else if (event.equalsIgnoreCase("32106-07.htm")) {
            st.exitQuest(true, true);
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int id = st.getState();
         int cond = st.getCond();
         int npcId = npc.getId();
         long count = st.getQuestItemsCount(8776);
         switch(st.getState()) {
            case 0:
               if (npcId == 32106 && id == 0 && cond == 0) {
                  if (player.getLevel() >= getMinLvl(this.getId())) {
                     htmltext = "32106-01.htm";
                  } else {
                     htmltext = "32106-00.htm";
                     st.exitQuest(true);
                  }
               }
               break;
            case 1:
               if (npcId == 32106) {
                  if (cond == 1) {
                     if (count == 0L) {
                        htmltext = "32106-05.htm";
                     } else {
                        htmltext = "32106-05a.htm";
                        st.takeItems(8776, -1L);
                        st.calcRewardPerItem(this.getId(), 7, (int)count);
                     }
                  }
               } else if (npcId == 32117) {
                  if (cond == 1) {
                     htmltext = "32117-01.htm";
                  } else {
                     st.exitQuest(true);
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      Player member = this.getRandomPartyMemberState(player, (byte)1);
      if (member != null) {
         QuestState st = member.getQuestState(this.getName());
         if (st != null && st.isCond(1)) {
            st.calcDropItems(this.getId(), 8776, npc.getId(), Integer.MAX_VALUE);
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new _643_RiseandFalloftheElrokiTribe(643, _643_RiseandFalloftheElrokiTribe.class.getSimpleName(), "");
   }

   static {
      _requests.put("1", new int[]{9492, 400});
      _requests.put("2", new int[]{9493, 250});
      _requests.put("3", new int[]{9494, 200});
      _requests.put("4", new int[]{9495, 134});
      _requests.put("5", new int[]{9496, 134});
      _requests.put("6", new int[]{10115, 287});
   }
}
