package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _246_PossessorOfAPreciousSoul_3 extends Quest {
   public _246_PossessorOfAPreciousSoul_3(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31740);
      this.addTalkId(new int[]{31740, 30721, 31741});
      this.addKillId(new int[]{21541, 21544, 25325, 21539, 21537, 21536, 21532});
      this.questItemIds = new int[]{7591, 7592, 7593, 7594, 21725};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         int cond = st.getCond();
         if (event.equalsIgnoreCase("31740-4.htm")) {
            if (cond == 0) {
               st.startQuest();
            }
         } else if (event.equalsIgnoreCase("31741-2.htm")) {
            if (cond == 1) {
               st.set("awaitsWaterbinder", "1");
               st.set("awaitsEvergreen", "1");
               st.setCond(2, true);
               st.takeItems(7678, 1L);
            }
         } else if (event.equalsIgnoreCase("31744-2.htm")) {
            if (cond == 2) {
               st.setCond(3, true);
            }
         } else if (event.equalsIgnoreCase("31741-5.htm")) {
            if (cond == 3) {
               st.takeItems(7591, 1L);
               st.takeItems(7592, 1L);
               st.setCond(4, true);
            }
         } else if (event.equalsIgnoreCase("31741-9.htm")) {
            if (cond == 5) {
               st.takeItems(7593, -1L);
               st.takeItems(21725, -1L);
               st.giveItems(7594, 1L);
               st.setCond(6, true);
            }
         } else if (event.equalsIgnoreCase("30721-2.htm") && cond == 6) {
            st.takeItems(7594, 1L);
            st.calcExpAndSp(this.getId());
            st.calcReward(this.getId());
            st.exitQuest(false, true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      String htmltext = getNoQuestMsg(talker);
      QuestState st = talker.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else if (npc.getId() != 31740 && st.getState() != 1) {
         return htmltext;
      } else {
         int cond = st.getCond();
         if (talker.isSubClassActive()) {
            switch(npc.getId()) {
               case 30721:
                  if (cond == 6) {
                     htmltext = "30721-1.htm";
                  }
                  break;
               case 31740:
                  if (cond == 0 && st.getQuestItemsCount(7678) == 1L) {
                     if (st.getState() == 2) {
                        htmltext = getAlreadyCompletedMsg(talker);
                     } else if (talker.getLevel() < 65) {
                        htmltext = "31740-2.htm";
                        st.exitQuest(true);
                     } else if (talker.getLevel() >= 65) {
                        htmltext = "31740-1.htm";
                     }
                  } else if (cond == 1) {
                     htmltext = "31740-5.htm";
                  }
                  break;
               case 31741:
                  if (cond == 1) {
                     htmltext = "31741-1.htm";
                  } else if (cond == 2) {
                     htmltext = "31741-4.htm";
                  } else if (cond == 3 && st.hasQuestItems(7591) && st.hasQuestItems(7592)) {
                     htmltext = "31741-3.htm";
                  } else if (cond == 4) {
                     htmltext = "31741-8.htm";
                  } else if (cond != 5 || !st.hasQuestItems(7593) && st.getQuestItemsCount(21725) < 100L) {
                     if (cond == 6 && st.hasQuestItems(7594)) {
                        htmltext = "31741-11.htm";
                     }
                  } else {
                     htmltext = "31741-7.htm";
                  }
            }
         } else {
            htmltext = "sub.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      switch(npc.getId()) {
         case 21532:
         case 21536:
         case 21537:
         case 21539:
            QuestState st = this.getQuestState(killer, false);
            if (st != null && st.isCond(4) && st.calcDropItems(this.getId(), 21725, npc.getId(), 100)) {
               st.setCond(5, true);
            }
            break;
         case 21541:
            Player partyMember = this.getRandomPartyMember(killer, "awaitsWaterbinder", "1");
            if (partyMember != null) {
               QuestState st = this.getQuestState(partyMember, false);
               if (st != null && st.isCond(2) && st.calcDropItems(this.getId(), 7591, npc.getId(), 1)) {
                  st.unset("awaitsWaterbinder");
                  if (st.hasQuestItems(7592)) {
                     st.setCond(3, true);
                  }
               }
            }
            break;
         case 21544:
            Player partyMember = this.getRandomPartyMember(killer, "awaitsEvergreen", "1");
            if (partyMember != null) {
               QuestState st = this.getQuestState(partyMember, false);
               if (st != null && st.isCond(2) && st.calcDropItems(this.getId(), 7592, npc.getId(), 1)) {
                  st.unset("awaitsEvergreen");
                  if (st.hasQuestItems(7591)) {
                     st.setCond(3, true);
                  }
               }
            }
            break;
         case 25325:
            if (killer.getParty() != null && !killer.getParty().getMembers().isEmpty()) {
               for(Player pm : killer.getParty().getMembers()) {
                  QuestState pst = this.getQuestState(pm, false);
                  if (pst != null && pst != null && pst.isCond(4) && pst.calcDropItems(this.getId(), 7593, npc.getId(), 1)) {
                     pst.setCond(5, true);
                  }
               }
            } else {
               QuestState pst = this.getQuestState(killer, false);
               if (pst != null && pst.isCond(4) && pst.calcDropItems(this.getId(), 7593, npc.getId(), 1)) {
                  pst.setCond(5, true);
               }
            }
      }

      return super.onKill(npc, killer, isSummon);
   }

   public static void main(String[] args) {
      new _246_PossessorOfAPreciousSoul_3(246, _246_PossessorOfAPreciousSoul_3.class.getSimpleName(), "");
   }
}
