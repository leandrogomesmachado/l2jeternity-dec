package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _166_MassOfDarkness extends Quest {
   private static final String qn = "_166_MassOfDarkness";
   private static final int UNDRIAS = 30130;
   private static final int IRIA = 30135;
   private static final int DORANKUS = 30139;
   private static final int TRUDY = 30143;
   private static final int UNDRIAS_LETTER = 1088;
   private static final int CEREMONIAL_DAGGER = 1089;
   private static final int DREVIANT_WINE = 1090;
   private static final int GARMIELS_SCRIPTURE = 1091;
   private static final int ADENA = 57;

   public _166_MassOfDarkness(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30130);
      this.addTalkId(new int[]{30130, 30135, 30139, 30143});
      this.questItemIds = new int[]{1088, 1089, 1090, 1091};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_166_MassOfDarkness");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30130-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.giveItems(1088, 1L);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_166_MassOfDarkness");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() == 2) {
                  if (player.getLevel() >= 2 && player.getLevel() <= 5) {
                     htmltext = "30130-03.htm";
                  } else {
                     htmltext = "30130-00.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30130-02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               switch(npc.getId()) {
                  case 30130:
                     if (cond == 1) {
                        htmltext = "30130-05.htm";
                     } else if (cond == 2) {
                        htmltext = "30130-06.htm";
                        st.takeItems(1089, 1L);
                        st.takeItems(1090, 1L);
                        st.takeItems(1091, 1L);
                        st.takeItems(1088, 1L);
                        st.rewardItems(57, 500L);
                        st.addExpAndSp(5672, 446);
                        st.playSound("ItemSound.quest_finish");
                        st.exitQuest(false);
                        showOnScreenMsg(player, NpcStringId.DELIVERY_DUTY_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
                     }
                     break;
                  case 30135:
                     if (st.getQuestItemsCount(1089) == 0L) {
                        st.giveItems(1089, 1L);
                        htmltext = "30135-01.htm";
                     } else {
                        htmltext = "30135-02.htm";
                     }
                     break;
                  case 30139:
                     if (st.getQuestItemsCount(1090) == 0L) {
                        st.giveItems(1090, 1L);
                        htmltext = "30139-01.htm";
                     } else {
                        htmltext = "30139-02.htm";
                     }
                     break;
                  case 30143:
                     if (st.getQuestItemsCount(1091) == 0L) {
                        st.giveItems(1091, 1L);
                        htmltext = "30143-01.htm";
                     } else {
                        htmltext = "30143-02.htm";
                     }
               }

               if (cond == 1 && st.getQuestItemsCount(1089) + st.getQuestItemsCount(1090) + st.getQuestItemsCount(1091) >= 3L) {
                  st.set("cond", "2");
                  st.playSound("ItemSound.quest_middle");
               }
               break;
            case 2:
               htmltext = Quest.getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _166_MassOfDarkness(166, "_166_MassOfDarkness", "");
   }
}
