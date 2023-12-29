package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _154_SacrificeToTheSea extends Quest {
   private static final String qn = "_154_SacrificeToTheSea";
   private static final int ROCKSWELL = 30312;
   private static final int CRISTEL = 30051;
   private static final int ROLFE = 30055;
   private static final int FOX_FUR = 1032;
   private static final int FOX_FUR_YARN = 1033;
   private static final int MAIDEN_DOLL = 1034;
   private static final int EARING = 113;

   public _154_SacrificeToTheSea(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30312);
      this.addTalkId(new int[]{30312, 30051, 30055});
      this.addKillId(new int[]{20481, 20544, 20545});
      this.questItemIds = new int[]{1032, 1033, 1034};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_154_SacrificeToTheSea");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30312-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_154_SacrificeToTheSea");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 2 && player.getLevel() <= 7) {
                  htmltext = "30312-03.htm";
               } else {
                  htmltext = "30312-02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30051:
                     if (cond == 1) {
                        if (st.getQuestItemsCount(1032) > 0L) {
                           htmltext = "30051-01.htm";
                        } else {
                           htmltext = "30051-01a.htm";
                        }

                        return htmltext;
                     } else if (cond == 2 && st.getQuestItemsCount(1032) >= 10L) {
                        htmltext = "30051-02.htm";
                        st.giveItems(1033, 1L);
                        st.takeItems(1032, -1L);
                        st.set("cond", "3");
                        st.playSound("ItemSound.quest_middle");
                        return htmltext;
                     } else {
                        if (cond == 3 && st.getQuestItemsCount(1033) >= 1L) {
                           htmltext = "30051-03.htm";
                        } else if (cond == 4 && st.getQuestItemsCount(1034) >= 1L) {
                           return "30051-04.htm";
                        }

                        return htmltext;
                     }
                  case 30055:
                     if (cond == 3 && st.getQuestItemsCount(1033) >= 1L) {
                        htmltext = "30055-01.htm";
                        st.giveItems(1034, 1L);
                        st.takeItems(1033, -1L);
                        st.set("cond", "4");
                        st.playSound("ItemSound.quest_middle");
                        return htmltext;
                     } else {
                        if (cond == 4 && st.getQuestItemsCount(1034) >= 1L) {
                           htmltext = "30055-02.htm";
                        } else if (cond >= 1 && cond <= 2) {
                           return "30055-03.htm";
                        }

                        return htmltext;
                     }
                  case 30312:
                     if (cond == 1) {
                        return "30312-05.htm";
                     } else if (cond == 2 && st.getQuestItemsCount(1032) >= 10L) {
                        return "30312-08.htm";
                     } else {
                        if (cond == 3 && st.getQuestItemsCount(1033) >= 1L) {
                           htmltext = "30312-06.htm";
                        } else if (cond == 4 && st.getQuestItemsCount(1034) >= 1L) {
                           htmltext = "30312-07.htm";
                           st.giveItems(113, 1L);
                           st.takeItems(1034, -1L);
                           st.addExpAndSp(100, 0);
                           st.playSound("ItemSound.quest_finish");
                           st.exitQuest(false);
                           return htmltext;
                        }

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
      QuestState st = player.getQuestState("_154_SacrificeToTheSea");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.getRandom(10) < 4) {
            st.giveItems(1032, 1L);
            if (st.getQuestItemsCount(1032) == 10L) {
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "2");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _154_SacrificeToTheSea(154, "_154_SacrificeToTheSea", "");
   }
}
