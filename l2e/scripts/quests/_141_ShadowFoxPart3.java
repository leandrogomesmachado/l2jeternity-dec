package l2e.scripts.quests;

import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _141_ShadowFoxPart3 extends Quest {
   private static final String qn = "_141_ShadowFoxPart3";
   private static final int NATOOLS = 30894;
   private static final int REPORT = 10350;
   private static final int[] NPC = new int[]{20791, 20792, 20135};

   public _141_ShadowFoxPart3(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addFirstTalkId(30894);
      this.addTalkId(30894);

      for(int mob : NPC) {
         this.addKillId(mob);
      }

      this.questItemIds = new int[]{10350};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_141_ShadowFoxPart3");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30894-02.htm")) {
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30894-04.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30894-15.htm")) {
            st.set("cond", "4");
            st.unset("talk");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30894-18.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.unset("talk");
            st.exitQuest(false);
            st.giveItems(57, 88888L);
            if (player.getLevel() >= 37 && player.getLevel() <= 42) {
               st.addExpAndSp(278005, 17058);
            }

            QuestState qs = player.getQuestState("_998_FallenAngelSelect");
            if (qs == null) {
               Quest q = QuestManager.getInstance().getQuest("_998_FallenAngelSelect");
               if (q != null) {
                  qs = q.newQuestState(player);
                  qs.setState((byte)1);
               }
            }
         }

         return event;
      }
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_141_ShadowFoxPart3");
      if (st == null) {
         QuestState qs = player.getQuestState("_140_ShadowFoxPart2");
         st = this.newQuestState(player);
         if (qs != null && qs.getState() == 2 && st.getState() == 0) {
            st.setState((byte)1);
         }
      } else if (st.getState() == 2 && player.getLevel() >= 38) {
         QuestState qs2 = player.getQuestState("_998_FallenAngelSelect");
         QuestState qs3 = player.getQuestState("142_FallenAngelRequestOfDawn");
         QuestState qs4 = player.getQuestState("143_FallenAngelRequestOfDusk");
         if (qs2 != null && qs2.getState() == 2 && (qs3 == null || qs4 == null)) {
            qs2.setState((byte)1);
         }
      }

      npc.showChatWindow(player);
      return null;
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_141_ShadowFoxPart3");
      if (st == null) {
         return htmltext;
      } else {
         int id = st.getState();
         int cond = st.getInt("cond");
         int talk = st.getInt("talk");
         if (id == 0) {
            return htmltext;
         } else {
            if (id == 2) {
               htmltext = getAlreadyCompletedMsg(player);
            } else if (id == 1) {
               if (cond == 0) {
                  if (player.getLevel() >= 37) {
                     htmltext = "30894-01.htm";
                  } else {
                     htmltext = "30894-00.htm";
                     st.exitQuest(true);
                  }
               } else if (cond == 1) {
                  htmltext = "30894-02.htm";
               } else if (cond == 2) {
                  htmltext = "30894-05.htm";
               } else if (cond == 3) {
                  if (cond == talk) {
                     htmltext = "30894-07.htm";
                  } else {
                     htmltext = "30894-06.htm";
                     st.takeItems(10350, -1L);
                     st.set("talk", "1");
                  }
               } else if (cond == 4) {
                  htmltext = "30894-16.htm";
               }
            }

            return htmltext;
         }
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_141_ShadowFoxPart3");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 2 && st.getRandom(100) <= 80 && st.getQuestItemsCount(10350) < 30L) {
            st.giveItems(10350, 1L);
            if (st.getQuestItemsCount(10350) >= 30L) {
               st.set("cond", "3");
               st.playSound("ItemSound.quest_middle");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _141_ShadowFoxPart3(141, "_141_ShadowFoxPart3", "");
   }
}
