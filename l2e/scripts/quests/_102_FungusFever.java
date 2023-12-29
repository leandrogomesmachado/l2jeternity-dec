package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _102_FungusFever extends Quest {
   public _102_FungusFever(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30284);
      this.addTalkId(30156);
      this.addTalkId(30217);
      this.addTalkId(30219);
      this.addTalkId(30221);
      this.addTalkId(30284);
      this.addTalkId(30285);
      this.addKillId(20013);
      this.addKillId(20019);
      this.questItemIds = new int[]{964, 965, 966, 1130, 1131, 1132, 1133, 1134, 746};
   }

   private void check(QuestState st) {
      if (st.getQuestItemsCount(1131) == 0L && st.getQuestItemsCount(1132) == 0L && st.getQuestItemsCount(1133) == 0L && st.getQuestItemsCount(1134) == 0L) {
         st.setCond(6, true);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            htmltext = "30284-02.htm";
            st.giveItems(964, 1L);
            st.startQuest();
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getCond();
         if (npcId == 30284) {
            if (cond == 0) {
               if (player.getRace().ordinal() != 1) {
                  htmltext = "30284-00.htm";
                  st.exitQuest(true);
               } else {
                  if (player.getLevel() >= 12) {
                     return "30284-07.htm";
                  }

                  htmltext = "30284-08.htm";
                  st.exitQuest(true);
               }
            } else if (cond == 1 && st.getQuestItemsCount(964) == 1L) {
               htmltext = "30284-03.htm";
            } else if (cond == 2 && st.getQuestItemsCount(965) == 1L) {
               htmltext = "30284-09.htm";
            } else if (cond == 4 && st.getQuestItemsCount(1130) == 1L) {
               st.setCond(5, true);
               st.takeItems(1130, 1L);
               st.giveItems(746, 1L);
               htmltext = "30284-04.htm";
            } else if (cond == 5) {
               htmltext = "30284-05.htm";
            } else if (cond == 6 && st.getQuestItemsCount(746) == 1L) {
               st.takeItems(746, 1L);
               st.exitQuest(false, true);
               htmltext = "30284-06.htm";
               if (player.getClassId().isMage()) {
                  st.calcReward(this.getId(), 1);
               } else {
                  st.calcReward(this.getId(), 2);
               }
            }
         } else if (npcId == 30156) {
            if (cond == 1 && st.getQuestItemsCount(964) == 1L) {
               st.takeItems(964, 1L);
               st.giveItems(965, 1L);
               st.setCond(2, true);
               htmltext = "30156-03.htm";
            } else if (cond == 2 && st.getQuestItemsCount(965) > 0L && st.getQuestItemsCount(966) < 10L) {
               htmltext = "30156-04.htm";
            } else if (cond > 3 && st.getQuestItemsCount(746) > 0L) {
               htmltext = "30156-07.htm";
            } else if (cond == 3 && st.getQuestItemsCount(965) > 0L && st.getQuestItemsCount(966) >= 10L) {
               st.takeItems(965, 1L);
               st.takeItems(966, -1L);
               st.giveItems(1130, 1L);
               st.giveItems(1131, 1L);
               st.giveItems(1132, 1L);
               st.giveItems(1133, 1L);
               st.giveItems(1134, 1L);
               st.setCond(4, true);
               htmltext = "30156-05.htm";
            } else if (cond == 4) {
               htmltext = "30156-06.htm";
            }
         } else if (npcId == 30217 && cond == 5 && st.getQuestItemsCount(746) == 1L && st.getQuestItemsCount(1131) == 1L) {
            st.takeItems(1131, 1L);
            htmltext = "30217-01.htm";
            this.check(st);
         } else if (npcId == 30219 && cond == 5 && st.getQuestItemsCount(746) == 1L && st.getQuestItemsCount(1132) == 1L) {
            st.takeItems(1132, 1L);
            htmltext = "30219-01.htm";
            this.check(st);
         } else if (npcId == 30221 && cond == 5 && st.getQuestItemsCount(746) == 1L && st.getQuestItemsCount(1133) == 1L) {
            st.takeItems(1133, 1L);
            htmltext = "30221-01.htm";
            this.check(st);
         } else if (npcId == 30285 && cond == 5 && st.getQuestItemsCount(746) == 1L && st.getQuestItemsCount(1134) == 1L) {
            st.takeItems(1134, 1L);
            htmltext = "30285-01.htm";
            this.check(st);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(killer, 2);
      if (partyMember == null) {
         return super.onKill(npc, killer, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if ((npc.getId() == 20013 || npc.getId() == 20019) && st.calcDropItems(this.getId(), 966, npc.getId(), 10)) {
            st.setCond(3);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _102_FungusFever(102, _102_FungusFever.class.getSimpleName(), "");
   }
}
