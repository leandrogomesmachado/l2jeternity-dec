package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _326_VanquishRemnants extends Quest {
   private static final String qn = "_326_VanquishRemnants";
   private static final int RED_CROSS_BADGE = 1359;
   private static final int BLUE_CROSS_BADGE = 1360;
   private static final int BLACK_CROSS_BADGE = 1361;
   private static final int BLACK_LION_MARK = 1369;

   public _326_VanquishRemnants(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30435);
      this.addTalkId(30435);
      this.addKillId(new int[]{20053, 20437, 20058, 20436, 20061, 20439, 20063, 20066, 20438});
      this.questItemIds = new int[]{1359, 1360, 1361};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_326_VanquishRemnants");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30435-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30435-07.htm")) {
            st.playSound("ItemSound.quest_giveup");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_326_VanquishRemnants");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 21) {
                  htmltext = "30435-02.htm";
               } else {
                  htmltext = "30435-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               long redBadges = st.getQuestItemsCount(1359);
               long blueBadges = st.getQuestItemsCount(1360);
               long blackBadges = st.getQuestItemsCount(1361);
               long badgesSum = redBadges + blueBadges + blackBadges;
               if (badgesSum > 0L) {
                  st.takeItems(1359, -1L);
                  st.takeItems(1360, -1L);
                  st.takeItems(1361, -1L);
                  st.rewardItems(57, redBadges * 46L + blueBadges * 52L + blackBadges * 58L + (long)(badgesSum >= 10L ? 4320 : 0));
                  if (badgesSum >= 100L) {
                     if (!st.hasQuestItems(1369)) {
                        htmltext = "30435-06.htm";
                        st.giveItems(1369, 1L);
                        st.playSound("ItemSound.quest_itemget");
                     } else {
                        htmltext = "30435-09.htm";
                     }
                  } else {
                     htmltext = "30435-05.htm";
                  }
               } else {
                  htmltext = "30435-04.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_326_VanquishRemnants");
      if (st == null) {
         return null;
      } else {
         int chance = getRandom(100);
         switch(npc.getId()) {
            case 20053:
            case 20058:
            case 20437:
               if (chance <= 33) {
                  st.giveItems(1359, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
               break;
            case 20061:
            case 20063:
            case 20436:
            case 20439:
               if (chance <= 16) {
                  st.giveItems(1360, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
               break;
            case 20066:
            case 20438:
               if (chance <= 12) {
                  st.giveItems(1361, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _326_VanquishRemnants(326, "_326_VanquishRemnants", "");
   }
}
