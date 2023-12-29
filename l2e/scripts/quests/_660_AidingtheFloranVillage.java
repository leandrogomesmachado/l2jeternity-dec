package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _660_AidingtheFloranVillage extends Quest {
   private static final String qn = "_660_AidingtheFloranVillage";
   public final int MARIA = 30608;
   public final int ALEX = 30291;
   public final int CARSED_SEER = 21106;
   public final int PLAIN_WATCMAN = 21102;
   public final int ROUGH_HEWN_ROCK_GOLEM = 21103;
   public final int DELU_LIZARDMAN_SHAMAN = 20781;
   public final int DELU_LIZARDMAN_SAPPLIER = 21104;
   public final int DELU_LIZARDMAN_COMMANDER = 21107;
   public final int DELU_LIZARDMAN_SPESIAL_AGENT = 21105;
   public final int WATCHING_EYES = 8074;
   public final int ROUGHLY_HEWN_ROCK_GOLEM_SHARD = 8075;
   public final int DELU_LIZARDMAN_SCALE = 8076;
   public final int SCROLL_ENCANT_ARMOR = 956;
   public final int SCROLL_ENCHANT_WEAPON = 955;

   public _660_AidingtheFloranVillage(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30608);
      this.addTalkId(30608);
      this.addTalkId(30291);
      this.addKillId(21106);
      this.addKillId(21102);
      this.addKillId(21103);
      this.addKillId(20781);
      this.addKillId(21104);
      this.addKillId(21107);
      this.addKillId(21105);
      this.questItemIds = new int[]{8074, 8076, 8075};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_660_AidingtheFloranVillage");
      if (st == null) {
         return event;
      } else {
         long EYES = st.getQuestItemsCount(8074);
         long SCALE = st.getQuestItemsCount(8076);
         long SHARD = st.getQuestItemsCount(8075);
         if (event.equalsIgnoreCase("30608-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30291-05.htm")) {
            if (EYES + SCALE + SHARD >= 45L) {
               st.giveItems(57, EYES * 100L + SCALE * 100L + SHARD * 100L + 9000L);
               st.takeItems(8074, -1L);
               st.takeItems(8076, -1L);
               st.takeItems(8075, -1L);
            } else {
               st.giveItems(57, EYES * 100L + SCALE * 100L + SHARD * 100L);
               st.takeItems(8074, -1L);
               st.takeItems(8076, -1L);
               st.takeItems(8075, -1L);
            }

            st.playSound("ItemSound.quest_finish");
         } else if (event.equalsIgnoreCase("30291-11.htm")) {
            if (EYES + SCALE + SHARD >= 99L) {
               long n = 100L - EYES;
               long t = 100L - SCALE - EYES;
               if (EYES >= 100L) {
                  st.takeItems(8074, 100L);
               } else {
                  st.takeItems(8074, -1L);
                  if (SCALE >= n) {
                     st.takeItems(8076, n);
                  } else {
                     st.takeItems(8076, -1L);
                     st.takeItems(8075, t);
                  }
               }

               if (getRandom(10) < 8) {
                  st.giveItems(57, 13000L);
                  st.giveItems(956, 1L);
               } else {
                  st.giveItems(57, 1000L);
               }

               st.playSound("ItemSound.quest_finish");
            } else {
               htmltext = "30291-14.htm";
            }
         } else if (event.equalsIgnoreCase("30291-12.htm")) {
            if (EYES + SCALE + SHARD >= 199L) {
               long n = 200L - EYES;
               long t = 200L - SCALE - EYES;
               int luck = getRandom(15);
               if (EYES >= 200L) {
                  st.takeItems(8074, 200L);
               } else {
                  st.takeItems(8074, -1L);
               }

               if (SCALE >= n) {
                  st.takeItems(8076, n);
               } else {
                  st.takeItems(8076, -1L);
               }

               st.takeItems(8075, t);
               if (luck < 9) {
                  st.giveItems(57, 20000L);
                  st.giveItems(956, 1L);
               } else if (luck > 8 && luck < 12) {
                  st.giveItems(955, 1L);
               } else {
                  st.giveItems(57, 2000L);
               }

               st.playSound("ItemSound.quest_finish");
            } else {
               htmltext = "30291-14.htm";
            }
         } else if (event.equalsIgnoreCase("30291-13.htm")) {
            if (EYES + SCALE + SHARD >= 499L) {
               long n = 500L - EYES;
               long t = 500L - SCALE - EYES;
               if (EYES >= 500L) {
                  st.takeItems(8074, 500L);
               } else {
                  st.takeItems(8074, -1L);
               }

               if (SCALE >= n) {
                  st.takeItems(8076, n);
               } else {
                  st.takeItems(8076, -1L);
                  st.takeItems(8075, t);
               }

               if (getRandom(10) < 8) {
                  st.giveItems(57, 45000L);
                  st.giveItems(955, 1L);
               } else {
                  st.giveItems(57, 5000L);
               }

               st.playSound("ItemSound.quest_finish");
            } else {
               htmltext = "30291-14.htm";
            }
         } else if (event.equalsIgnoreCase("30291-15.htm")) {
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30291-06.htm")) {
            st.unset("cond");
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_660_AidingtheFloranVillage");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         int id = st.getState();
         if (id == 0) {
            if (npcId == 30608 && cond == 0) {
               if (player.getLevel() < 30) {
                  htmltext = "30608-01.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "30608-02.htm";
               }
            }
         } else if (id == 1) {
            if (npcId == 30608 && cond == 1) {
               htmltext = "30608-06.htm";
            } else if (npcId == 30291 && cond == 1) {
               htmltext = "30291-01.htm";
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "2");
            } else if (npcId == 30291 && cond == 2) {
               if (st.getQuestItemsCount(8074) + st.getQuestItemsCount(8076) + st.getQuestItemsCount(8075) == 0L) {
                  htmltext = "30291-02.htm";
               } else {
                  htmltext = "30291-03.htm";
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_660_AidingtheFloranVillage");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int chance = getRandom(100) + 1;
         if (st.getInt("cond") == 2) {
            if (npcId == 21106 | npcId == 21102 && chance < 79) {
               st.giveItems(8074, 1L);
               st.playSound("ItemSound.quest_itemget");
            } else if (npcId == 21103 && chance < 75) {
               st.giveItems(8075, 1L);
               st.playSound("ItemSound.quest_itemget");
            } else if (npcId == 20781 | npcId == 21104 | npcId == 21107 | npcId == 21105 && chance < 67) {
               st.giveItems(8076, 1L);
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _660_AidingtheFloranVillage(660, "_660_AidingtheFloranVillage", "");
   }
}
