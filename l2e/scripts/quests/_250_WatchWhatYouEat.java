package l2e.scripts.quests;

import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _250_WatchWhatYouEat extends Quest {
   private static final String qn = "_250_WatchWhatYouEat";
   private static final int _sally = 32743;
   private static final int[][] _mobs = new int[][]{{18864, 15493}, {18865, 15494}, {18868, 15495}};

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_250_WatchWhatYouEat");
      if (st == null) {
         return event;
      } else {
         if (npc.getId() == 32743) {
            if (event.equalsIgnoreCase("32743-03.htm")) {
               st.setState((byte)1);
               st.set("cond", "1");
               st.playSound("ItemSound.quest_accept");
            } else if (event.equalsIgnoreCase("32743-end.htm")) {
               st.unset("cond");
               st.rewardItems(57, 135661L);
               st.addExpAndSp(698334, 76369);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(false);
            } else if (event.equalsIgnoreCase("32743-22.html") && st.getState() == 2) {
               htmltext = "32743-23.html";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_250_WatchWhatYouEat");
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 32743) {
            switch(st.getState()) {
               case 0:
                  if (player.getLevel() >= 82) {
                     htmltext = "32743-01.htm";
                  } else {
                     htmltext = "32743-00.htm";
                  }
                  break;
               case 1:
                  if (st.getInt("cond") == 1) {
                     htmltext = "32743-04.htm";
                  } else if (st.getInt("cond") == 2) {
                     if (st.hasQuestItems(_mobs[0][1]) && st.hasQuestItems(_mobs[1][1]) && st.hasQuestItems(_mobs[2][1])) {
                        htmltext = "32743-05.htm";

                        for(int[] items : _mobs) {
                           st.takeItems(items[1], -1L);
                        }
                     } else {
                        htmltext = "32743-06.htm";
                     }
                  }
                  break;
               case 2:
                  htmltext = "32743-done.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_250_WatchWhatYouEat");
      if (st == null) {
         return null;
      } else {
         if (st.getState() == 1 && st.getInt("cond") == 1) {
            for(int[] mob : _mobs) {
               if (npc.getId() == mob[0] && !st.hasQuestItems(mob[1])) {
                  st.giveItems(mob[1], 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }

            if (st.hasQuestItems(_mobs[0][1]) && st.hasQuestItems(_mobs[1][1]) && st.hasQuestItems(_mobs[2][1])) {
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            }
         }

         return null;
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_250_WatchWhatYouEat");
      if (st == null) {
         Quest q = QuestManager.getInstance().getQuest("_250_WatchWhatYouEat");
         st = q.newQuestState(player);
      }

      return npc.getId() == 32743 ? "32743-20.html" : null;
   }

   public _250_WatchWhatYouEat(int questId, String name, String descr) {
      super(questId, name, descr);
      this.questItemIds = new int[]{15493, 15494, 15495};
      this.addStartNpc(32743);
      this.addFirstTalkId(32743);
      this.addTalkId(32743);

      for(int[] i : _mobs) {
         this.addKillId(i[0]);
      }
   }

   public static void main(String[] args) {
      new _250_WatchWhatYouEat(250, "_250_WatchWhatYouEat", "");
   }
}
