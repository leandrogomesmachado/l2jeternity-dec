package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _650_ABrokenDream extends Quest {
   private static final String qn = "_650_ABrokenDream";
   private static final int GHOST = 32054;
   private static final int DREAM_FRAGMENT = 8514;
   private static final int CREWMAN = 22027;
   private static final int VAGABOND = 22028;

   public _650_ABrokenDream(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32054);
      this.addTalkId(32054);
      this.addKillId(new int[]{22027, 22028});
      this.questItemIds = new int[]{8514};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_650_ABrokenDream");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32054-01a.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32054-03.htm")) {
            if (st.getQuestItemsCount(8514) == 0L) {
               htmltext = "32054-04.htm";
            }
         } else if (event.equalsIgnoreCase("32054-05.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_giveup");
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_650_ABrokenDream");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               QuestState st2 = player.getQuestState("_117_TheOceanOfDistantStars");
               if (st2 != null && st2.isCompleted() && player.getLevel() >= 39) {
                  htmltext = "32054-01.htm";
               } else {
                  htmltext = "32054-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               htmltext = "32054-02.htm";
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_650_ABrokenDream");
      if (st == null) {
         return null;
      } else {
         if (st.isStarted() && st.getRandom(100) < 25) {
            st.giveItems(8514, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _650_ABrokenDream(650, "_650_ABrokenDream", "");
   }
}
