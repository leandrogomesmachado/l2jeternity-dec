package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _155_FindSirWindawood extends Quest {
   private static final String qn = "_155_FindSirWindawood";
   private static final int ABELLOS = 30042;
   private static final int WINDAWOOD = 30311;
   private static final int OFFICIAL_LETTER = 1019;
   private static final int HASTE_POTION = 734;

   public _155_FindSirWindawood(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30042);
      this.addTalkId(30042);
      this.addTalkId(30311);
      this.questItemIds = new int[]{1019};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_155_FindSirWindawood");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30042-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.giveItems(1019, 1L);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_155_FindSirWindawood");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 3 && player.getLevel() <= 6) {
                  htmltext = "30042-01.htm";
               } else {
                  htmltext = "30042-01a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30042:
                     return "30042-03.htm";
                  case 30311:
                     if (st.getQuestItemsCount(1019) == 1L) {
                        htmltext = "30311-01.htm";
                        st.takeItems(1019, -1L);
                        st.rewardItems(734, 1L);
                        st.playSound("ItemSound.quest_finish");
                        st.unset("cond");
                        st.exitQuest(false);
                     }

                     return htmltext;
                  default:
                     return htmltext;
               }
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _155_FindSirWindawood(155, "_155_FindSirWindawood", "");
   }
}
