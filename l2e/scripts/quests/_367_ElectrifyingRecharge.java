package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;

public class _367_ElectrifyingRecharge extends Quest {
   private static final String qn = "_367_ElectrifyingRecharge";
   private static final int LORAIN = 30673;
   private static final int LORAINS_LAMP = 5875;
   private static final int T_L1 = 5876;
   private static final int T_L2 = 5877;
   private static final int T_L3 = 5878;
   private static final int T_L4 = 5879;
   private static final int T_L5 = 5880;
   private static final int[] REWARD = new int[]{4553, 4554, 4555, 4556, 4557, 4558, 4559, 4560, 4561, 4562, 4563, 4564};

   public _367_ElectrifyingRecharge(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30673);
      this.addTalkId(30673);
      this.addSpellFinishedId(new int[]{21035});
      this.questItemIds = new int[]{5875, 5876, 5877, 5878, 5879, 5880};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_367_ElectrifyingRecharge");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30673-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(5875, 1L);
         } else if (event.equalsIgnoreCase("30673-09.htm")) {
            st.playSound("ItemSound.quest_accept");
            st.giveItems(5875, 1L);
         } else if (event.equalsIgnoreCase("30673-08.htm")) {
            st.playSound("ItemSound.quest_giveup");
            st.exitQuest(true);
         } else if (event.equalsIgnoreCase("30673-07.htm")) {
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(5875, 1L);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_367_ElectrifyingRecharge");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 37) {
                  htmltext = "30673-01.htm";
               } else {
                  htmltext = "30673-02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               if (cond == 1) {
                  if (st.hasQuestItems(5880)) {
                     htmltext = "30673-05.htm";
                     st.takeItems(5880, 1L);
                     st.giveItems(5875, 1L);
                     st.playSound("ItemSound.quest_accept");
                  } else if (st.hasQuestItems(5876)) {
                     htmltext = "30673-04.htm";
                     st.takeItems(5876, 1L);
                  } else if (st.hasQuestItems(5877)) {
                     htmltext = "30673-04.htm";
                     st.takeItems(5877, 1L);
                  } else if (st.hasQuestItems(5878)) {
                     htmltext = "30673-04.htm";
                     st.takeItems(5878, 1L);
                  } else {
                     htmltext = "30673-03.htm";
                  }
               } else if (cond == 2 && st.hasQuestItems(5879)) {
                  htmltext = "30673-06.htm";
                  st.takeItems(5879, 1L);
                  st.rewardItems(REWARD[getRandom(REWARD.length)], 1L);
                  st.playSound("ItemSound.quest_finish");
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onSpellFinished(Npc npc, Player player, Skill skill) {
      QuestState st = this.checkPlayerCondition(player, npc, "cond", "1");
      if (st == null) {
         return null;
      } else {
         if (skill.getId() == 4072 && st.hasQuestItems(5875)) {
            int randomItem = getRandom(5876, 5880);
            st.takeItems(5875, 1L);
            st.giveItems(randomItem, 1L);
            if (randomItem == 5879) {
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
      new _367_ElectrifyingRecharge(367, "_367_ElectrifyingRecharge", "");
   }
}
