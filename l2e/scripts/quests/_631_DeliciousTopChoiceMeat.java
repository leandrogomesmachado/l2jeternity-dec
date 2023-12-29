package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _631_DeliciousTopChoiceMeat extends Quest {
   public _631_DeliciousTopChoiceMeat(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31537);
      this.addTalkId(31537);
      this.addKillId(new int[]{18878, 18879, 18885, 18886, 18892, 18893, 18899, 18900});
      this.questItemIds = new int[]{15534};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31537-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31537-05.htm")) {
            if (st.getQuestItemsCount(15534) >= 120L) {
               st.takeItems(15534, 120L);
               st.calcReward(this.getId(), getRandom(1, 3), true);
               st.playSound("ItemSound.quest_middle");
               htmltext = "31537-07.htm";
            }
         } else if (event.equalsIgnoreCase("31537-08.htm")) {
            st.takeItems(15534, -1L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
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
         long count = st.getQuestItemsCount(15534);
         if (npc.getId() == 31537) {
            if (id == 0 && cond == 0) {
               if (player.getLevel() < 82) {
                  htmltext = "31537-02.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "31537-01.htm";
               }
            } else if (id == 1 && cond == 1) {
               if (count < 120L) {
                  htmltext = "31537-05.htm";
               } else {
                  htmltext = "31537-04.htm";
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember != null) {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st != null) {
            st.calcDropItems(this.getId(), 15534, npc.getId(), Integer.MAX_VALUE);
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new _631_DeliciousTopChoiceMeat(631, _631_DeliciousTopChoiceMeat.class.getSimpleName(), "");
   }
}
