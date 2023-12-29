package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _116_BeyondTheHillsOfWinter extends Quest {
   private static final String qn = "_116_BeyondTheHillsOfWinter";
   private static final int FILAUR = 30535;
   private static final int OBI = 32052;
   private static final int BANDAGE = 1833;
   private static final int ENERGY_STONE = 5589;
   private static final int THIEF_KEY = 1661;
   private static final int GOODS = 8098;
   private static final int SSD = 1463;

   public _116_BeyondTheHillsOfWinter(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30535);
      this.addTalkId(30535);
      this.addTalkId(32052);
      this.questItemIds = new int[]{8098};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_116_BeyondTheHillsOfWinter");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30535-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30535-05.htm")) {
            st.set("cond", "3");
            st.giveItems(8098, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("materials")) {
            htmltext = "32052-02.htm";
            st.takeItems(8098, -1L);
            st.rewardItems(1463, 1650L);
            st.addExpAndSp(82792, 4981);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
         } else if (event.equalsIgnoreCase("adena")) {
            htmltext = "32052-02.htm";
            st.takeItems(8098, -1L);
            st.giveItems(57, 16500L);
            st.addExpAndSp(82792, 4981);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_116_BeyondTheHillsOfWinter");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 30 && player.getRace().ordinal() == 4) {
                  htmltext = "30535-01.htm";
               } else {
                  htmltext = "30535-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30535:
                     if (cond == 1) {
                        if (st.getQuestItemsCount(1833) >= 20L && st.getQuestItemsCount(5589) >= 5L && st.getQuestItemsCount(1661) >= 10L) {
                           htmltext = "30535-03.htm";
                           st.set("cond", "2");
                           st.takeItems(1833, 20L);
                           st.takeItems(5589, 5L);
                           st.takeItems(1661, 10L);
                        } else {
                           htmltext = "30535-04.htm";
                        }

                        return htmltext;
                     } else {
                        if (cond == 2) {
                           htmltext = "30535-03.htm";
                        } else if (cond == 3) {
                           htmltext = "30535-05.htm";
                           return htmltext;
                        }

                        return htmltext;
                     }
                  case 32052:
                     if (cond == 3 && st.getQuestItemsCount(8098) == 1L) {
                        htmltext = "32052-00.htm";
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
      new _116_BeyondTheHillsOfWinter(116, "_116_BeyondTheHillsOfWinter", "");
   }
}
