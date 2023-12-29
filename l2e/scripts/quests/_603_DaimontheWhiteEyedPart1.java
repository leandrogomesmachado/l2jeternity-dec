package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _603_DaimontheWhiteEyedPart1 extends Quest {
   public _603_DaimontheWhiteEyedPart1(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31683);
      this.addTalkId(new int[]{31683, 31548, 31549, 31550, 31551, 31552});
      this.addKillId(new int[]{21297, 21299, 21304});
      this.questItemIds = new int[]{7190, 7191};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31683-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31683-06.htm")) {
            if (st.getQuestItemsCount(7191) > 4L) {
               st.takeItems(7191, -1L);
               st.setCond(7, true);
            } else {
               htmltext = "31683-07.htm";
            }
         } else if (event.equalsIgnoreCase("31683-10.htm")) {
            if (st.getQuestItemsCount(7190) >= 200L) {
               st.takeItems(7190, -1L);
               st.calcReward(this.getId());
               st.exitQuest(true, true);
            } else {
               st.setCond(7);
               htmltext = "31683-11.htm";
            }
         } else if (event.equalsIgnoreCase("31548-02.htm")) {
            st.giveItems(7191, 1L);
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("31549-02.htm")) {
            st.giveItems(7191, 1L);
            st.setCond(3, true);
         } else if (event.equalsIgnoreCase("31550-02.htm")) {
            st.giveItems(7191, 1L);
            st.setCond(4, true);
         } else if (event.equalsIgnoreCase("31551-02.htm")) {
            st.giveItems(7191, 1L);
            st.setCond(5, true);
         } else if (event.equalsIgnoreCase("31552-02.htm")) {
            st.giveItems(7191, 1L);
            st.setCond(6, true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 73) {
                  htmltext = "31683-02.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "31683-01.htm";
               }
               break;
            case 1:
               int cond = st.getCond();
               switch(npc.getId()) {
                  case 31548:
                     if (cond == 1) {
                        htmltext = "31548-01.htm";
                     } else if (cond >= 2) {
                        htmltext = "31548-03.htm";
                     }
                     break;
                  case 31549:
                     if (cond == 2) {
                        htmltext = "31549-01.htm";
                     } else if (cond >= 3) {
                        htmltext = "31549-03.htm";
                     }
                     break;
                  case 31550:
                     if (cond == 3) {
                        htmltext = "31550-01.htm";
                     } else if (cond >= 4) {
                        htmltext = "31550-03.htm";
                     }
                     break;
                  case 31551:
                     if (cond == 4) {
                        htmltext = "31551-01.htm";
                     } else if (cond >= 5) {
                        htmltext = "31551-03.htm";
                     }
                     break;
                  case 31552:
                     if (cond == 5) {
                        htmltext = "31552-01.htm";
                     } else if (cond >= 6) {
                        htmltext = "31552-03.htm";
                     }
                     break;
                  case 31683:
                     if (cond >= 1 && cond <= 5) {
                        htmltext = "31683-04.htm";
                     } else if (cond == 6) {
                        htmltext = "31683-05.htm";
                     } else if (cond == 7) {
                        htmltext = "31683-08.htm";
                     } else if (cond == 8) {
                        htmltext = "31683-09.htm";
                     }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 7);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st != null && st.calcDropItems(this.getId(), 7190, npc.getId(), 200)) {
            st.setCond(8, true);
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _603_DaimontheWhiteEyedPart1(603, _603_DaimontheWhiteEyedPart1.class.getSimpleName(), "");
   }
}
