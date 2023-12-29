package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _158_SeedOfEvil extends Quest {
   private static final String qn = "_158_SeedOfEvil";
   private static final int BIOTIN = 30031;
   private static final int CLAY_TABLET = 1025;
   private static final int NERKAS = 27016;

   private _158_SeedOfEvil(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30031);
      this.addTalkId(30031);
      this.addKillId(27016);
      this.questItemIds = new int[]{1025};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_158_SeedOfEvil");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            htmltext = "30031-04.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_158_SeedOfEvil");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 21) {
                  st.exitQuest(true);
                  htmltext = "30031-02.htm";
               } else {
                  htmltext = "30031-03.htm";
               }
               break;
            case 1:
               if (st.getQuestItemsCount(1025) != 0L) {
                  st.exitQuest(false);
                  st.rewardItems(57, 1495L);
                  st.giveItems(956, 1L);
                  st.addExpAndSp(17818, 927);
                  st.unset("cond");
                  st.playSound("ItemSound.quest_finish");
                  htmltext = "30031-06.htm";
               } else {
                  htmltext = "30031-05.htm";
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
      QuestState st = player.getQuestState("_158_SeedOfEvil");
      if (st == null) {
         return null;
      } else {
         if (npc.getId() == 27016 && st.getQuestItemsCount(1025) == 0L) {
            st.giveItems(1025, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "2");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _158_SeedOfEvil(158, "_158_SeedOfEvil", "");
   }
}
