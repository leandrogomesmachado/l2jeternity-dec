package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10271_TheEnvelopingDarkness extends Quest {
   public _10271_TheEnvelopingDarkness(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32560);
      this.addTalkId(32560);
      this.addTalkId(32556);
      this.addTalkId(32528);
      this.questItemIds = new int[]{13852};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32560-02.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("32556-02.htm")) {
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("32556-05.htm")) {
            st.takeItems(13852, 1L);
            st.setCond(4, true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         } else if (npc.getId() == 32560) {
            if (st.getCond() == 0) {
               QuestState _prev = player.getQuestState("_10269_ToTheSeedOfDestruction");
               if (_prev != null && _prev.getState() == 2 && player.getLevel() >= 75) {
                  htmltext = "32560-01.htm";
               } else {
                  htmltext = "32560-00.htm";
               }
            } else if (st.getCond() >= 1 && st.getCond() < 4) {
               htmltext = "32560-03.htm";
            } else if (st.isCond(4)) {
               htmltext = "32560-04.htm";
               st.calcExpAndSp(this.getId());
               st.calcReward(this.getId());
               st.exitQuest(false, true);
            }
         } else if (npc.getId() == 32556) {
            if (st.isCond(1)) {
               htmltext = "32556-01.htm";
            } else if (st.isCond(2)) {
               htmltext = "32556-03.htm";
            } else if (st.isCond(3)) {
               htmltext = "32556-04.htm";
            } else if (st.isCond(4)) {
               htmltext = "32556-06.htm";
            }
         } else if (npc.getId() == 32528) {
            if (st.isCond(2)) {
               htmltext = "32528-01.htm";
               st.giveItems(13852, 1L);
               st.setCond(3, true);
            } else if (st.isCond(3)) {
               htmltext = "32528-02.htm";
            }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _10271_TheEnvelopingDarkness(10271, _10271_TheEnvelopingDarkness.class.getSimpleName(), "");
   }
}
