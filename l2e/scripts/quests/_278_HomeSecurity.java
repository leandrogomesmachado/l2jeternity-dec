package l2e.scripts.quests;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _278_HomeSecurity extends Quest {
   private static final String qn = "_278_HomeSecurity";
   private static final int TUNATUN = 31537;
   private static final int DROP_CHANCE = 510;
   private static final int TORCH = 15531;
   private static final int[] MOBS = new int[]{18906, 18907};
   private static final int REWARDS = 959;
   private static final int REWARDS2 = 960;
   private static final int REWARDS3 = 9553;

   public _278_HomeSecurity(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31537);
      this.addTalkId(31537);

      for(int i : MOBS) {
         this.addKillId(i);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_278_HomeSecurity");
      if (st == null) {
         return event;
      } else {
         long count = st.getQuestItemsCount(15531);
         long random = (long)getRandom(3);
         if (event.equalsIgnoreCase("31537-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("31537-05.htm")) {
            if (count >= 300L) {
               if (random == 0L) {
                  st.takeItems(15531, 300L);
                  st.rewardItems(959, 1L);
               }

               if (random == 1L) {
                  st.takeItems(15531, 300L);
                  st.rewardItems(960, (long)(getRandom(9) + 1));
                  st.playSound("ItemSound.quest_middle");
               }

               if (random == 2L) {
                  st.takeItems(15531, 300L);
                  st.rewardItems(9553, (long)(getRandom(1) + 1));
                  st.playSound("ItemSound.quest_middle");
               }

               htmltext = "31537-07.htm";
            }
         } else if (event.equalsIgnoreCase("31537-08.htm")) {
            st.takeItems(15531, -1L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_278_HomeSecurity");
      if (st == null) {
         return htmltext;
      } else {
         int id = st.getState();
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         long count = st.getQuestItemsCount(15531);
         if (npcId == 31537) {
            if (id == 0 && cond == 0) {
               if (player.getLevel() < 82) {
                  htmltext = "31537-02.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "31537-01.htm";
               }
            } else if (id == 1 && cond == 1) {
               if (count < 300L) {
                  htmltext = "31537-05.htm";
               } else {
                  htmltext = "31537-04.htm";
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState("_278_HomeSecurity");
         if (st == null) {
            return null;
         } else {
            int id = st.getState();
            int cond = st.getInt("cond");
            if (id == 1) {
               long count = st.getQuestItemsCount(15531);
               if (cond == 1) {
                  int chance = (int)(510.0F * Config.RATE_QUEST_DROP);
                  int numItems = chance / 1000;
                  chance %= 1000;
                  if (getRandom(1000) < chance) {
                     ++numItems;
                  }

                  if (numItems > 0) {
                     if ((count + (long)numItems) / 300L > count / 300L) {
                        st.playSound("ItemSound.quest_middle");
                     } else {
                        st.playSound("ItemSound.quest_itemget");
                     }

                     st.giveItems(15531, (long)numItems);
                  }
               }
            }

            return null;
         }
      }
   }

   public static void main(String[] args) {
      new _278_HomeSecurity(278, "_278_HomeSecurity", "");
   }
}
