package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _452_FindingtheLostSoldiers extends Quest {
   private static final String qn = "_452_FindingtheLostSoldiers";
   private static final int JAKAN = 32773;
   private static final int TAG_ID = 15513;
   private static final int[] SOLDIER_CORPSES = new int[]{32769, 32770, 32771, 32772};

   public _452_FindingtheLostSoldiers(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32773);
      this.addTalkId(32773);
      this.addTalkId(SOLDIER_CORPSES);
      this.questItemIds = new int[]{15513};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_452_FindingtheLostSoldiers");
      if (st == null) {
         return event;
      } else {
         if (npc.getId() == 32773) {
            if (event.equalsIgnoreCase("32773-3.htm")) {
               st.set("cond", "1");
               st.setState((byte)1);
               st.playSound("ItemSound.quest_accept");
            }
         } else if (Util.contains(SOLDIER_CORPSES, npc.getId())) {
            if (st.getInt("cond") != 1) {
               return "corpse-3.htm";
            }

            st.giveItems(15513, 1L);
            st.set("cond", "2");
            npc.deleteMe();
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_452_FindingtheLostSoldiers");
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 32773) {
            switch(st.getState()) {
               case 0:
                  htmltext = player.getLevel() < 84 ? "32773-0.htm" : "32773-1.htm";
                  break;
               case 1:
                  if (st.getInt("cond") == 1) {
                     htmltext = "32773-4.htm";
                  } else if (st.getInt("cond") == 2) {
                     htmltext = "32773-5.htm";
                     st.takeItems(15513, 1L);
                     st.rewardItems(57, 95200L);
                     st.addExpAndSp(435024, 50366);
                     st.exitQuest(QuestState.QuestType.DAILY);
                  }
                  break;
               case 2:
                  if (st.isNowAvailable()) {
                     st.setState((byte)0);
                     htmltext = player.getLevel() < 84 ? "32773-0.htm" : "32773-1.htm";
                  } else {
                     htmltext = "32773-6.htm";
                  }
            }
         } else if (Util.contains(SOLDIER_CORPSES, npc.getId()) && st.getInt("cond") == 1) {
            htmltext = "corpse-1.htm";
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _452_FindingtheLostSoldiers(452, "_452_FindingtheLostSoldiers", "");
   }
}
