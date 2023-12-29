package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _286_FabulousFeathers extends Quest {
   private static final String qn = "_286_FabulousFeathers";
   private static int ERINU = 32164;
   private static final int[] MOBS = new int[]{22251, 22253, 22254, 22255, 22256};
   private static int FEATHER = 9746;

   public _286_FabulousFeathers(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(ERINU);
      this.addTalkId(ERINU);

      for(int mob : MOBS) {
         this.addKillId(mob);
      }

      this.questItemIds = new int[]{FEATHER};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_286_FabulousFeathers");
      if (st == null) {
         return null;
      } else {
         if (event.equalsIgnoreCase("32164-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32164-06.htm")) {
            st.takeItems(FEATHER, -1L);
            st.giveItems(57, 4160L);
            st.playSound("ItemSound.quest_finish");
            st.unset("cond");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_286_FabulousFeathers");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 17) {
                  htmltext = "32164-01.htm";
               } else {
                  htmltext = "32164-02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (cond == 1) {
                  htmltext = "32164-04.htm";
               } else if (cond == 2) {
                  htmltext = "32164-05.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_286_FabulousFeathers");
      if (st != null && st.isStarted()) {
         if (Rnd.chance(70)) {
            st.giveItems(FEATHER, 1L);
            st.playSound("ItemSound.quest_itemget");
            if (st.getQuestItemsCount(FEATHER) == 80L) {
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static void main(String[] args) {
      new _286_FabulousFeathers(286, "_286_FabulousFeathers", "");
   }
}
