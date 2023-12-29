package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _032_AnObviousLie extends Quest {
   public _032_AnObviousLie(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30120);
      this.addTalkId(new int[]{30120, 30094, 31706});
      this.addKillId(20135);
      this.questItemIds = new int[]{7166};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30120-1.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("30094-1.htm")) {
            st.giveItems(7165, 1L);
            st.setCond(2, false);
         } else if (event.equalsIgnoreCase("31706-1.htm")) {
            st.takeItems(7165, 1L);
            st.setCond(3, false);
         } else if (event.equalsIgnoreCase("30094-4.htm")) {
            if (st.getQuestItemsCount(7166) == 20L) {
               st.takeItems(7166, 20L);
               st.setCond(5, false);
            } else {
               htmltext = "no_items.htm";
               st.setCond(3, false);
            }
         } else if (event.equalsIgnoreCase("30094-7.htm")) {
            if (st.getQuestItemsCount(3031) >= 500L) {
               st.takeItems(3031, 500L);
               st.setCond(6, false);
            } else {
               htmltext = "no_items.htm";
            }
         } else if (event.equalsIgnoreCase("31706-4.htm")) {
            st.setCond(7, false);
         } else if (event.equalsIgnoreCase("30094-10.htm")) {
            st.setCond(8, false);
         } else if (event.equalsIgnoreCase("30094-13.htm")) {
            if (st.getQuestItemsCount(1868) >= 1000L && st.getQuestItemsCount(1866) >= 500L) {
               st.takeItems(1868, 1000L);
               st.takeItems(1866, 500L);
            } else {
               htmltext = "no_items.htm";
            }
         } else if ((event.equalsIgnoreCase("cat") || event.equalsIgnoreCase("racoon") || event.equalsIgnoreCase("rabbit")) && st.getInt("cond") == 8) {
            if (event.equalsIgnoreCase("cat")) {
               st.calcReward(this.getId(), 1);
            } else if (event.equalsIgnoreCase("racoon")) {
               st.calcReward(this.getId(), 2);
            } else if (event.equalsIgnoreCase("rabbit")) {
               st.calcReward(this.getId(), 3);
            }

            st.exitQuest(false, true);
            htmltext = "30094-14.htm";
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
         int cond = st.getCond();
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 45) {
                  htmltext = "30120-0.htm";
               } else {
                  htmltext = "30120-0a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30094:
                     switch(cond) {
                        case 1:
                           return "30094-0.htm";
                        case 2:
                           return "30094-2.htm";
                        case 3:
                        default:
                           return htmltext;
                        case 4:
                           return "30094-3.htm";
                        case 5:
                           if (st.getQuestItemsCount(3031) < 500L) {
                              htmltext = "30094-5.htm";
                           } else if (st.getQuestItemsCount(3031) >= 500L) {
                              return "30094-6.htm";
                           }

                           return htmltext;
                        case 6:
                           return "30094-8.htm";
                        case 7:
                           return "30094-9.htm";
                        case 8:
                           if (st.getQuestItemsCount(1868) < 1000L || st.getQuestItemsCount(1866) < 500L) {
                              htmltext = "30094-11.htm";
                           } else if (st.getQuestItemsCount(1868) >= 1000L || st.getQuestItemsCount(1866) >= 500L) {
                              return "30094-12.htm";
                           }

                           return htmltext;
                     }
                  case 30120:
                     if (cond == 1) {
                        htmltext = "30120-2.htm";
                     }

                     return htmltext;
                  case 31706:
                     switch(cond) {
                        case 2:
                           return "31706-0.htm";
                        case 3:
                           return "31706-2.htm";
                        case 4:
                        case 5:
                        default:
                           return htmltext;
                        case 6:
                           return "31706-3.htm";
                        case 7:
                           htmltext = "31706-5.htm";
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
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 3);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st.calcDropItems(this.getId(), 7166, npc.getId(), 20)) {
            st.setCond(4);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _032_AnObviousLie(32, _032_AnObviousLie.class.getSimpleName(), "");
   }
}
