package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _010_IntoTheWorld extends Quest {
   public _010_IntoTheWorld(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30533);
      this.addTalkId(30533);
      this.addTalkId(30520);
      this.addTalkId(30650);
      this.questItemIds = new int[]{7574};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30533-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("30520-02.htm")) {
            st.giveItems(7574, 1L);
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("30650-02.htm")) {
            st.takeItems(7574, 1L);
            st.setCond(3, true);
         } else if (event.equalsIgnoreCase("30520-05.htm")) {
            st.setCond(4, true);
         } else if (event.equalsIgnoreCase("30533-06.htm")) {
            st.calcReward(this.getId());
            st.exitQuest(false, true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      String htmltext = getNoQuestMsg(player);
      int cond = st.getCond();
      int npcId = npc.getId();
      switch(st.getState()) {
         case 0:
            if (npcId == 30533) {
               if (player.getRace().ordinal() == 4 && player.getLevel() >= 3) {
                  htmltext = "30533-02.htm";
               } else {
                  htmltext = "30533-01.htm";
                  st.exitQuest(true);
               }
            }
            break;
         case 1:
            switch(npcId) {
               case 30520:
                  switch(cond) {
                     case 1:
                        return "30520-01.htm";
                     case 2:
                        return "30520-03.htm";
                     case 3:
                        htmltext = "30520-04.htm";
                        st.set("cond", "4");
                        return htmltext;
                     case 4:
                        htmltext = "30520-06.htm";
                        return htmltext;
                     default:
                        return htmltext;
                  }
               case 30533:
                  switch(cond) {
                     case 1:
                        return "30533-04.htm";
                     case 4:
                        htmltext = "30533-05.htm";
                        return htmltext;
                     default:
                        return htmltext;
                  }
               case 30650:
                  switch(cond) {
                     case 2:
                        if (st.getQuestItemsCount(7574) > 0L) {
                           htmltext = "30520-01.htm";
                        }

                        return htmltext;
                     case 3:
                        return "30650-03.htm";
                     default:
                        htmltext = "30650-04.htm";
                        return htmltext;
                  }
               default:
                  return htmltext;
            }
         case 2:
            htmltext = getAlreadyCompletedMsg(player);
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _010_IntoTheWorld(10, _010_IntoTheWorld.class.getSimpleName(), "");
   }
}
