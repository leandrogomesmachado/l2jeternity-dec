package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _344_1000YearsEndofLamentation extends Quest {
   private static final String qn = "_344_1000YearsEndofLamentation";
   private static final int DEAD_HEROES = 4269;
   private static final int OLD_KEY = 4270;
   private static final int OLD_HILT = 4271;
   private static final int OLD_TOTEM = 4272;
   private static final int CRUCIFIX = 4273;
   private static final int GILMORE = 30754;
   private static final int RODEMAI = 30756;
   private static final int ORVEN = 30857;
   private static final int KAIEN = 30623;
   private static final int GARVARENTZ = 30704;

   public _344_1000YearsEndofLamentation(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30754);
      this.addTalkId(30754);
      this.addTalkId(30756);
      this.addTalkId(30857);
      this.addTalkId(30704);
      this.addTalkId(30623);

      for(int mob = 20236; mob < 20241; ++mob) {
         this.addKillId(mob);
      }

      this.questItemIds = new int[]{4269, 4270, 4271, 4272, 4273};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_344_1000YearsEndofLamentation");
      if (st == null) {
         return event;
      } else {
         long amount = st.getQuestItemsCount(4269);
         int cond = st.getInt("cond");
         int level = player.getLevel();
         if (event.equalsIgnoreCase("30754-04.htm")) {
            if (level >= 48 && cond == 0) {
               st.setState((byte)1);
               st.set("cond", "1");
               st.playSound("ItemSound.quest_accept");
            } else {
               htmltext = getNoQuestMsg(player);
               st.exitQuest(true);
            }
         } else if (event.equalsIgnoreCase("30754-08.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         } else if (event.equalsIgnoreCase("30754-06.htm") && cond == 1) {
            if (amount == 0L) {
               htmltext = "30754-06a.htm";
            } else {
               st.giveItems(57, amount * 60L);
               st.takeItems(4269, -1L);
               int random = getRandom(1000);
               if (random < 10) {
                  htmltext = "30754-12.htm";
                  st.giveItems(4270, 1L);
                  st.set("cond", "2");
               } else if (random < 20) {
                  htmltext = "30754-13.htm";
                  st.giveItems(4271, 1L);
                  st.set("cond", "2");
               } else if (random < 30) {
                  htmltext = "30754-14.htm";
                  st.giveItems(4272, 1L);
                  st.set("cond", "2");
               } else if (random < 40) {
                  htmltext = "30754-15.htm";
                  st.giveItems(4273, 1L);
                  st.set("cond", "2");
               } else {
                  htmltext = "30754-16.htm";
                  st.set("cond", "1");
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_344_1000YearsEndofLamentation");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         long amount = st.getQuestItemsCount(4269);
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 48) {
                  htmltext = "30754-02.htm";
               } else {
                  htmltext = "30754-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (npcId == 30754 && cond == 1) {
                  if (amount > 0L) {
                     htmltext = "30754-05.htm";
                  } else {
                     htmltext = "30754-04.htm";
                  }
               } else if (cond == 2) {
                  if (npcId == 30754) {
                     htmltext = "30754-15.htm";
                  } else if (this.rewards(st, npcId)) {
                     htmltext = npcId + "-01.htm";
                     st.set("cond", "1");
                     st.unset("mission");
                     st.playSound("ItemSound.quest_middle");
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_344_1000YearsEndofLamentation");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1) {
            int chance = 36 + (npc.getId() - 20234) * 2;
            if (getRandom(100) < chance) {
               st.giveItems(4269, 1L);
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   private boolean rewards(QuestState st, int npcId) {
      boolean state = false;
      int chance = getRandom(100);
      if (npcId == 30857 && st.getQuestItemsCount(4273) > 0L) {
         st.set("mission", "1");
         st.takeItems(4273, -1L);
         state = true;
         if (chance < 50) {
            st.giveItems(1875, 19L);
         } else if (chance < 70) {
            st.giveItems(952, 5L);
         } else {
            st.giveItems(2437, 1L);
         }
      } else if (npcId == 30704 && st.getQuestItemsCount(4272) > 0L) {
         st.set("mission", "2");
         st.takeItems(4272, -1L);
         state = true;
         if (chance < 45) {
            st.giveItems(1882, 70L);
         } else if (chance < 95) {
            st.giveItems(1881, 50L);
         } else {
            st.giveItems(191, 1L);
         }
      } else if (npcId == 30623 && st.getQuestItemsCount(4271) > 0L) {
         st.set("mission", "3");
         st.takeItems(4271, -1L);
         state = true;
         if (chance < 50) {
            st.giveItems(1874, 25L);
         } else if (chance < 75) {
            st.giveItems(1887, 10L);
         } else if (chance < 99) {
            st.giveItems(951, 1L);
         } else {
            st.giveItems(133, 1L);
         }
      } else if (npcId == 30756 && st.getQuestItemsCount(4270) > 0L) {
         st.set("mission", "4");
         st.takeItems(4270, -1L);
         state = true;
         if (chance < 40) {
            st.giveItems(1879, 55L);
         } else if (chance < 90) {
            st.giveItems(951, 1L);
         } else {
            st.giveItems(885, 1L);
         }
      }

      return state;
   }

   public static void main(String[] args) {
      new _344_1000YearsEndofLamentation(344, "_344_1000YearsEndofLamentation", "");
   }
}
