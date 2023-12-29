package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _138_TempleChampionPart2 extends Quest {
   private static final String qn = "_138_TempleChampionPart2";
   private static final int SYLVAIN = 30070;
   private static final int PUPINA = 30118;
   private static final int ANGUS = 30474;
   private static final int SLA = 30666;
   private static final int[] MOBS = new int[]{20176, 20550, 20551, 20552};
   private static final int MANIFESTO = 10340;
   private static final int RELIC = 10340;
   private static final int ANGUS_REC = 10343;
   private static final int PUPINA_REC = 10344;

   public _138_TempleChampionPart2(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30070);
      this.addTalkId(new int[]{30070, 30118, 30474, 30666});
      this.addKillId(MOBS);
      this.questItemIds = new int[]{10340, 10340, 10343, 10344};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_138_TempleChampionPart2");
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         switch(event) {
            case "30070-02.htm":
               st.startQuest();
               st.giveItems(10340, 1L);
               break;
            case "30070-05.htm":
               st.rewardItems(57, 84593L);
               if (player.getLevel() < 42) {
                  st.addExpAndSp(187062, 11307);
               }

               st.exitQuest(false, true);
               break;
            case "30070-03.htm":
               st.setCond(2, true);
               break;
            case "30118-06.htm":
               st.setCond(3, true);
               break;
            case "30118-09.htm":
               st.setCond(6, true);
               st.giveItems(10344, 1L);
               break;
            case "30474-02.htm":
               st.setCond(4, true);
               break;
            case "30666-02.htm":
               if (st.hasQuestItems(10344)) {
                  st.set("talk", "1");
                  st.takeItems(10344, -1L);
               }
               break;
            case "30666-03.htm":
               if (st.hasQuestItems(10340)) {
                  st.set("talk", "2");
                  st.takeItems(10340, -1L);
               }
               break;
            case "30666-08.htm":
               st.setCond(7, true);
               st.unset("talk");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_138_TempleChampionPart2");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(npc.getId()) {
            case 30070:
               switch(cond) {
                  case 1:
                     return "30070-02.htm";
                  case 2:
                  case 3:
                  case 4:
                  case 5:
                  case 6:
                     return "30070-03.htm";
                  case 7:
                     return "30070-04.htm";
                  default:
                     if (st.isCompleted()) {
                        return getAlreadyCompletedMsg(player);
                     }

                     return player.getLevel() >= 36 ? "30070-01.htm" : "30070-00.htm";
               }
            case 30118:
               switch(cond) {
                  case 2:
                     return "30118-01.htm";
                  case 3:
                  case 4:
                     return "30118-07.htm";
                  case 5:
                     htmltext = "30118-08.htm";
                     if (st.hasQuestItems(10343)) {
                        st.takeItems(10343, -1L);
                     }

                     return htmltext;
                  case 6:
                     return "30118-10.htm";
                  default:
                     return htmltext;
               }
            case 30474:
               switch(cond) {
                  case 3:
                     return "30474-01.htm";
                  case 4:
                     if (st.getQuestItemsCount(10340) >= 10L) {
                        st.takeItems(10340, -1L);
                        st.giveItems(10343, 1L);
                        st.setCond(5, true);
                        htmltext = "30474-04.htm";
                     } else {
                        htmltext = "30474-03.htm";
                     }

                     return htmltext;
                  case 5:
                     return "30474-05.htm";
                  default:
                     return htmltext;
               }
            case 30666:
               switch(cond) {
                  case 6:
                     switch(st.getInt("talk")) {
                        case 1:
                           return "30666-02.htm";
                        case 2:
                           return "30666-03.htm";
                        default:
                           return "30666-01.htm";
                     }
                  case 7:
                     htmltext = "30666-09.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_138_TempleChampionPart2");
      if (st != null && st.isStarted() && st.isCond(4) && st.getQuestItemsCount(10340) < 10L) {
         st.giveItems(10340, 1L);
         if (st.getQuestItemsCount(10340) >= 10L) {
            st.playSound("ItemSound.quest_middle");
         } else {
            st.playSound("ItemSound.quest_itemget");
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new _138_TempleChampionPart2(138, "_138_TempleChampionPart2", "");
   }
}
