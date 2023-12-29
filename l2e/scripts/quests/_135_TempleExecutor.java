package l2e.scripts.quests;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _135_TempleExecutor extends Quest {
   private static final String qn = "_135_TempleExecutor";
   private static final int SHEGFIELD = 30068;
   private static final int PANO = 30078;
   private static final int ALEX = 30291;
   private static final int SONIN = 31773;
   private static final int[] mobs = new int[]{20781, 21104, 21105, 21106, 21107};
   private static final int CARGO = 10328;
   private static final int CRYSTAL = 10329;
   private static final int MAP = 10330;
   private static final int SONIN_CR = 10331;
   private static final int PANO_CR = 10332;
   private static final int ALEX_CR = 10333;
   private static final int BADGE = 10334;

   public _135_TempleExecutor(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30068);
      this.addTalkId(30068);
      this.addTalkId(30291);
      this.addTalkId(31773);
      this.addTalkId(30078);

      for(int mob : mobs) {
         this.addKillId(mob);
      }

      this.questItemIds = new int[]{10328, 10329, 10330, 10331, 10332, 10333};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_135_TempleExecutor");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30068-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30068-09.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.unset("Report");
            st.giveItems(57, 16924L);
            st.addExpAndSp(30000, 2000);
            st.giveItems(10334, 1L);
            st.exitQuest(false);
         } else if (event.equalsIgnoreCase("30068-03.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30291-06.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_135_TempleExecutor");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getCond();
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 35) {
                  htmltext = "30068-01.htm";
               } else {
                  st.exitQuest(true);
                  htmltext = "30068-00.htm";
               }
               break;
            case 1:
               if (npcId == 30068) {
                  if (cond == 1) {
                     htmltext = "30068-02.htm";
                  } else if (cond == 2 || cond == 3 || cond == 4) {
                     htmltext = "30068-04.htm";
                  } else if (cond == 5) {
                     if (st.getInt("Report") == 1) {
                        htmltext = "30068-06.htm";
                     }

                     if (st.getQuestItemsCount(10331) > 0L && st.getQuestItemsCount(10332) > 0L && st.getQuestItemsCount(10333) > 0L) {
                        st.takeItems(10332, -1L);
                        st.takeItems(10331, -1L);
                        st.takeItems(10333, -1L);
                        st.set("Report", "1");
                        htmltext = "30068-05.htm";
                     }
                  }
               }

               if (npcId == 30291) {
                  if (cond == 2) {
                     htmltext = "30291-01.htm";
                  } else if (cond == 3) {
                     htmltext = "30291-07.htm";
                  } else if (cond == 4) {
                     if (st.getQuestItemsCount(10331) > 0L && st.getQuestItemsCount(10332) > 0L) {
                        st.setCond(5);
                        st.takeItems(10330, -1L);
                        st.giveItems(10333, 1L);
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "30291-09.htm";
                     }

                     htmltext = "30291-08.htm";
                  } else if (cond == 5) {
                     htmltext = "30291-10.htm";
                  }
               }

               if (npcId == 31773 && cond == 4) {
                  if (st.getQuestItemsCount(10328) < 10L) {
                     htmltext = "31773-02.htm";
                  }

                  st.takeItems(10328, -1L);
                  st.giveItems(10331, 1L);
                  st.playSound("ItemSound.quest_middle");
                  htmltext = "31773-01.htm";
               }

               if (npcId == 30078 && cond == 4) {
                  if (st.getQuestItemsCount(10329) < 10L) {
                     htmltext = "30078-02.htm";
                  }

                  st.takeItems(10329, -1L);
                  st.giveItems(10332, 1L);
                  st.playSound("ItemSound.quest_middle");
                  htmltext = "30078-01.htm";
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_135_TempleExecutor");
      if (st == null) {
         return null;
      } else {
         if (st.getCond() == 3) {
            List<Integer> drops = new ArrayList<>();
            if (st.getQuestItemsCount(10328) < 10L) {
               drops.add(10328);
            }

            if (st.getQuestItemsCount(10329) < 10L) {
               drops.add(10329);
            }

            if (st.getQuestItemsCount(10330) < 10L) {
               drops.add(10330);
            }

            if (drops.isEmpty()) {
               return null;
            }

            int drop = drops.get(getRandom(drops.size()));
            st.giveItems(drop, 1L);
            if (drops.size() == 1 && st.getQuestItemsCount(drop) >= 10L) {
               st.set("cond", "4");
               st.playSound("ItemSound.quest_middle");
               return null;
            }

            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _135_TempleExecutor(135, "_135_TempleExecutor", "");
   }
}
