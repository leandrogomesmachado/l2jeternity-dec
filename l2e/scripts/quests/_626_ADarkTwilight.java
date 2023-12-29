package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _626_ADarkTwilight extends Quest {
   private static final String qn = "_626_ADarkTwilight";
   private static final int BloodOfSaint = 7169;
   private static final int Hierarch = 31517;

   public _626_ADarkTwilight(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31517);
      this.addTalkId(31517);
      this.addKillId(new int[]{21520, 21523, 21524, 21526, 21529, 21530, 21531, 21532, 21535, 21536, 21539, 21540});
      this.questItemIds = new int[]{7169};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_626_ADarkTwilight");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31517-03.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("reward1")) {
            if (st.getQuestItemsCount(7169) == 300L) {
               htmltext = "31517-07.htm";
               st.takeItems(7169, 300L);
               st.addExpAndSp(162773, 12500);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(true);
            } else {
               htmltext = "31517-08.htm";
            }
         } else if (event.equalsIgnoreCase("reward2")) {
            if (st.getQuestItemsCount(7169) == 300L) {
               htmltext = "31517-07.htm";
               st.takeItems(7169, 300L);
               st.rewardItems(57, 100000L);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(true);
            } else {
               htmltext = "31517-08.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_626_ADarkTwilight");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 60 && player.getLevel() <= 71) {
                  htmltext = "31517-01.htm";
               } else {
                  htmltext = "31517-02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               if (cond == 1 && st.getQuestItemsCount(7169) < 300L) {
                  htmltext = "31517-05.htm";
               } else if (cond == 2) {
                  htmltext = "31517-04.htm";
               }
               break;
            case 2:
               htmltext = Quest.getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_626_ADarkTwilight");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.getQuestItemsCount(7169) < 300L) {
            st.giveItems(7169, 1L);
            if (st.getQuestItemsCount(7169) == 300L) {
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _626_ADarkTwilight(626, "_626_ADarkTwilight", "");
   }
}
