package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _292_CrushBrigands extends Quest {
   private static final String qn = "_292_CrushBrigands";
   private static final int GOBLIN_NECKLACE = 1483;
   private static final int GOBLIN_PENDANT = 1484;
   private static final int GOBLIN_LORD_PENDANT = 1485;
   private static final int SUSPICIOUS_MEMO = 1486;
   private static final int SUSPICIOUS_CONTRACT = 1487;

   public _292_CrushBrigands(int scriptId, String name, String descr) {
      super(scriptId, name, descr);
      this.addStartNpc(30532);
      this.addTalkId(new int[]{30532, 30533});
      this.addKillId(new int[]{20322, 20323, 20324, 20327, 20528});
      this.questItemIds = new int[]{1483, 1484, 1485, 1487, 1486};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_292_CrushBrigands");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30532-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30532-06.htm")) {
            st.takeItems(1486, -1L);
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_292_CrushBrigands");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         switch(st.getState()) {
            case 0:
               if (npcId == 30532) {
                  if (player.getRace().ordinal() != 4) {
                     htmltext = "30532-00.htm";
                     st.exitQuest(true);
                  } else {
                     if (player.getLevel() >= 5) {
                        return "30532-02.htm";
                     }

                     htmltext = "30532-01.htm";
                     st.exitQuest(true);
                  }
               }
               break;
            case 1:
               if (npcId == 30532) {
                  long neckl = st.getQuestItemsCount(1483);
                  long penda = st.getQuestItemsCount(1484);
                  long lordp = st.getQuestItemsCount(1485);
                  long smemo = st.getQuestItemsCount(1486);
                  long scont = st.getQuestItemsCount(1487);
                  if (neckl != 0L && penda != 0L && lordp != 0L && smemo != 0L && scont != 0L) {
                     st.takeItems(1483, -1L);
                     st.takeItems(1484, -1L);
                     st.takeItems(1485, -1L);
                     if (scont == 0L) {
                        if (smemo == 1L) {
                           htmltext = "30532-08.htm";
                        } else if (smemo >= 2L) {
                           htmltext = "30532-09.htm";
                        } else {
                           htmltext = "30532-05.htm";
                        }
                     } else {
                        htmltext = "30532-10.htm";
                        st.takeItems(1487, -1L);
                     }

                     st.giveItems(57, 12L * neckl + 36L * penda + 33L * lordp + 100L * scont);
                  } else {
                     htmltext = "30532-04.htm";
                  }
               } else if (npcId == 30533) {
                  if (st.getQuestItemsCount(1487) == 0L) {
                     htmltext = "30533-01.htm";
                  } else {
                     htmltext = "30533-02.htm";
                     st.giveItems(57, st.getQuestItemsCount(1487) * 120L);
                     st.takeItems(1487, -1L);
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_292_CrushBrigands");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1) {
            int item = 0;
            int npcId = npc.getId();
            if (npcId == 20322 || npcId == 20323) {
               item = 1483;
            }

            if (npcId == 20324 || npcId == 20327) {
               item = 1484;
            }

            if (npcId == 20528) {
               item = 1485;
            }

            int n = getRandom(10);
            if (n > 5) {
               st.giveItems(item, 1L);
               st.playSound("ItemSound.quest_itemget");
            } else if (n > 4 && st.getQuestItemsCount(1487) == 0L) {
               if (st.getQuestItemsCount(1486) < 3L) {
                  st.giveItems(1486, 1L);
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  st.giveItems(1487, 1L);
                  st.takeItems(1486, -1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "2");
               }
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _292_CrushBrigands(292, "_292_CrushBrigands", "");
   }
}
