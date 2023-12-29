package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _139_ShadowFoxPart1 extends Quest {
   private static final String qn = "_139_ShadowFoxPart1";
   private static final int MIA = 30896;
   private static final int FRAGMENT = 10345;
   private static final int CHEST = 10346;
   private static final int[] NPC = new int[]{20636, 20637, 20638, 20639};

   public _139_ShadowFoxPart1(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addFirstTalkId(30896);
      this.addTalkId(30896);

      for(int mob : NPC) {
         this.addKillId(mob);
      }

      this.questItemIds = new int[]{10345, 10346};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_139_ShadowFoxPart1");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30896-03.htm")) {
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30896-11.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30896-14.htm")) {
            st.takeItems(10345, -1L);
            st.takeItems(10346, -1L);
            st.set("talk", "1");
         } else if (event.equalsIgnoreCase("30896-16.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.unset("talk");
            st.exitQuest(false);
            st.giveItems(57, 14050L);
            if (player.getLevel() >= 37 && player.getLevel() <= 42) {
               st.addExpAndSp(30000, 2000);
            }
         }

         return event;
      }
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_139_ShadowFoxPart1");
      if (st == null) {
         st = this.newQuestState(player);
      }

      QuestState qs = player.getQuestState("_138_TempleChampionPart2");
      if (qs != null && qs.getState() == 2 && st.getState() == 0) {
         st.setState((byte)1);
      }

      npc.showChatWindow(player);
      return null;
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_139_ShadowFoxPart1");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         int cond = st.getInt("cond");
         int talk = st.getInt("talk");
         if (id == 0) {
            return htmltext;
         } else {
            if (id == 2) {
               htmltext = getAlreadyCompletedMsg(player);
            } else if (npcId == 30896) {
               if (cond == 0) {
                  if (player.getLevel() >= 37) {
                     htmltext = "30896-01.htm";
                  } else {
                     htmltext = "30896-00.htm";
                     st.exitQuest(true);
                  }
               } else if (cond == 1) {
                  htmltext = "30896-03.htm";
               } else if (cond == 2) {
                  if (st.getQuestItemsCount(10345) >= 10L && st.getQuestItemsCount(10346) >= 1L) {
                     htmltext = "30896-13.htm";
                  } else if (cond == talk) {
                     htmltext = "30896-14.htm";
                  } else {
                     htmltext = "30896-12.htm";
                  }
               }
            }

            return htmltext;
         }
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_139_ShadowFoxPart1");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 2) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(10345, 1L);
            if (st.getRandom(100) <= 2) {
               st.giveItems(10346, 1L);
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _139_ShadowFoxPart1(139, "_139_ShadowFoxPart1", "");
   }
}
