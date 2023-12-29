package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _268_TracesOfEvil extends Quest {
   private static final String qn = "_268_TracesOfEvil";
   private static final int[] NPCS = new int[]{20474, 20476, 20478};

   public _268_TracesOfEvil(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30559);
      this.addTalkId(30559);

      for(int mob : NPCS) {
         this.addKillId(mob);
      }

      this.questItemIds = new int[]{10869};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_268_TracesOfEvil");
      if (st == null) {
         return null;
      } else {
         if (event.equalsIgnoreCase("30559-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_268_TracesOfEvil");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 15) {
                  htmltext = "30559-00.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "30559-01.htm";
               }
               break;
            case 1:
               if (st.getQuestItemsCount(10869) >= 30L) {
                  htmltext = "30559-04.htm";
                  st.takeItems(10869, -1L);
                  st.giveItems(57, 2474L);
                  st.addExpAndSp(8738, 409);
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(true);
               } else {
                  htmltext = "30559-03.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_268_TracesOfEvil");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1) {
            if (st.getQuestItemsCount(10869) < 29L) {
               st.playSound("ItemSound.quest_itemget");
            } else if (st.getQuestItemsCount(10869) >= 29L) {
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "2");
               st.giveItems(10869, 1L);
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _268_TracesOfEvil(268, "_268_TracesOfEvil", "");
   }
}
