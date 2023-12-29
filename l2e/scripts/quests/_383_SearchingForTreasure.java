package l2e.scripts.quests;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _383_SearchingForTreasure extends Quest {
   private static final String qn = "_383_SearchingForTreasure";
   private static final int PIRATES_TREASURE_MAP = 5915;
   private static final int SHARK = 20314;
   private static final int ESPEN = 30890;
   private static final int PIRATES_CHEST = 31148;
   private static List<_383_SearchingForTreasure.rewardInfo> rewards = new ArrayList<>();

   public _383_SearchingForTreasure(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30890);
      this.addTalkId(30890);
      this.addTalkId(31148);
      this.questItemIds = new int[]{5915};
      rewards.add(new _383_SearchingForTreasure.rewardInfo(952, 1, 8));
      rewards.add(new _383_SearchingForTreasure.rewardInfo(956, 1, 15));
      rewards.add(new _383_SearchingForTreasure.rewardInfo(1337, 1, 130));
      rewards.add(new _383_SearchingForTreasure.rewardInfo(1338, 2, 150));
      rewards.add(new _383_SearchingForTreasure.rewardInfo(2450, 1, 2));
      rewards.add(new _383_SearchingForTreasure.rewardInfo(2451, 1, 2));
      rewards.add(new _383_SearchingForTreasure.rewardInfo(3452, 1, 140));
      rewards.add(new _383_SearchingForTreasure.rewardInfo(3455, 1, 120));
      rewards.add(new _383_SearchingForTreasure.rewardInfo(4408, 1, 220));
      rewards.add(new _383_SearchingForTreasure.rewardInfo(4409, 1, 220));
      rewards.add(new _383_SearchingForTreasure.rewardInfo(4418, 1, 220));
      rewards.add(new _383_SearchingForTreasure.rewardInfo(4419, 1, 220));
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_383_SearchingForTreasure");
      if (st == null) {
         return null;
      } else {
         if (event.equalsIgnoreCase("30890-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
         } else if (event.equalsIgnoreCase("30890-07.htm")) {
            if (st.getQuestItemsCount(5915) > 0L) {
               st.set("cond", "2");
               st.takeItems(5915, 1L);
               st.addSpawn(31148, 106583, 197747, -4209, 900000);
               st.addSpawn(20314, 106570, 197740, -4209, 900000);
               st.addSpawn(20314, 106580, 197747, -4209, 900000);
               st.addSpawn(20314, 106590, 197743, -4209, 900000);
               st.playSound("ItemSound.quest_accept");
            } else {
               htmltext = "You don't have required items";
               st.exitQuest(true);
            }
         } else if (event.equalsIgnoreCase("30890-02b.htm")) {
            if (st.getQuestItemsCount(5915) > 0L) {
               st.giveItems(57, 1000L);
               st.playSound("ItemSound.quest_finish");
            } else {
               htmltext = "You don't have required items";
               st.exitQuest(true);
            }
         } else if (event.equalsIgnoreCase("31148-02.htm")) {
            if (st.getQuestItemsCount(1661) > 0L) {
               st.takeItems(1661, 1L);
               st.giveItems(57, (long)(500 + getRandom(5) * 300));
               int count = 0;

               while(count < 1) {
                  for(_383_SearchingForTreasure.rewardInfo reward : rewards) {
                     int id = reward.id;
                     int qty = reward.count;
                     int chance = reward.chance;
                     if (getRandom(1000) < chance && count < 2) {
                        st.giveItems(id, (long)(getRandom(qty) + 1));
                        ++count;
                     }

                     if (count < 2) {
                        for(int i = 4481; i <= 4505; ++i) {
                           if (getRandom(500) == 1 && count < 2) {
                              st.giveItems(i, 1L);
                              ++count;
                           }
                        }
                     }
                  }
               }

               st.playSound("ItemSound.quest_finish");
               st.exitQuest(true);
            } else {
               htmltext = "31148-03.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_383_SearchingForTreasure");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 42) {
                  if (st.getQuestItemsCount(5915) > 0L) {
                     htmltext = "30890-01.htm";
                  } else {
                     htmltext = "30890-00.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30890-01a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (npcId == 30890) {
                  htmltext = "30890-03a.htm";
               } else if (npcId == 31148 && st.getInt("cond") == 2) {
                  htmltext = "31148-01.htm";
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _383_SearchingForTreasure(383, "_383_SearchingForTreasure", "");
   }

   private class rewardInfo {
      public int id;
      public int count;
      public int chance;

      public rewardInfo(int _id, int _count, int _chance) {
         this.id = _id;
         this.count = _count;
         this.chance = _chance;
      }
   }
}
