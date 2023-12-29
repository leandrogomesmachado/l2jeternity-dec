package l2e.scripts.quests;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _309_ForAGoodCause extends Quest {
   private static final String qn = "_309_ForAGoodCause";
   private static final int ATRA = 32647;
   private static final int[] MUCROKIANS = new int[]{22650, 22651, 22652, 22653, 22654};
   private static final int CHANGED_MUCROKIAN = 22655;
   private static final int MUCROKIAN_HIDE = 14873;
   private static final int FALLEN_MUCROKIAN_HIDE = 14874;
   private static final int MUCROKIAN_HIDE_CHANCE = 100;
   private static final int FALLEN_HIDE_CHANCE = 100;
   private static final int EXCHANGE_REC_MOIRAI_MAGE_MUCROKIAN_HIDE_COUNT = 240;
   private static final int REC_MOIRAI_MAGE_REWARD_COUNT = 6;
   private static final int[] REC_MOIRAI_MAGE = new int[]{15777, 15780, 15783, 15786, 15789, 15790};
   private static final int EXCHANGE_PART_MOIRAI_MAGE_MUCROKIAN_HIDE_COUNT = 180;
   private static final int PART_MOIRAI_MAGE_REWARD_COUNT = 6;
   private static final int PART_MOIRAI_MAGE_MIN_REWARD_ITEM_COUNT = 3;
   private static final int PART_MOIRAI_MAGE_MAX_REWARD_ITEM_COUNT = 9;
   private static final int[] PART_MOIRAI_MAGE = new int[]{15647, 15650, 15653, 15656, 15659, 15692};

   public _309_ForAGoodCause(int questID, String name, String description) {
      super(questID, name, description);
      this.addStartNpc(32647);
      this.addTalkId(32647);

      for(int currentNPCID : MUCROKIANS) {
         this.addKillId(currentNPCID);
      }

      this.addKillId(22655);
   }

   private String onExchangeRequest(QuestState questState, int exchangeID) {
      String resultHtmlText = "32647-13.htm";
      long fallenMucrokianHideCount = questState.getQuestItemsCount(14874);
      if (fallenMucrokianHideCount > 0L) {
         questState.takeItems(14874, fallenMucrokianHideCount);
         questState.giveItems(14873, fallenMucrokianHideCount * 2L);
         fallenMucrokianHideCount = 0L;
      }

      long mucrokianHideCount = questState.getQuestItemsCount(14873);
      if (exchangeID == 240 && mucrokianHideCount >= 240L) {
         int currentRecipeIndex = getRandom(6);
         questState.takeItems(14873, 240L);
         questState.giveItems(REC_MOIRAI_MAGE[currentRecipeIndex], (long)(1 * (int)Config.RATE_QUEST_REWARD_RECIPE));
         questState.playSound("ItemSound.quest_finish");
         resultHtmlText = "32647-14.htm";
      } else if (exchangeID == 180 && mucrokianHideCount >= 180L) {
         int currentPartIndex = getRandom(6);
         int minCountWithQuestRewardMultiplier = 3 * (int)Config.RATE_QUEST_REWARD_MATERIAL;
         int maxCountWithQuestRewardMultiplier = 9 * (int)Config.RATE_QUEST_REWARD_MATERIAL;
         int currentPartCount = getRandom(minCountWithQuestRewardMultiplier, maxCountWithQuestRewardMultiplier);
         questState.takeItems(14873, 180L);
         questState.giveItems(PART_MOIRAI_MAGE[currentPartIndex], (long)currentPartCount);
         questState.playSound("ItemSound.quest_finish");
         resultHtmlText = "32647-14.htm";
      }

      return resultHtmlText;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_309_ForAGoodCause");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32647-05.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32647-12.htm") || event.equalsIgnoreCase("32647-07.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         } else if (event.equalsIgnoreCase("claimreward")) {
            htmltext = "32647-09.htm";
         } else {
            int exchangeID = 0;

            try {
               exchangeID = Integer.parseInt(event);
            } catch (Exception var8) {
               exchangeID = 0;
            }

            if (exchangeID > 0) {
               htmltext = this.onExchangeRequest(st, exchangeID);
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      String htmltext = getNoQuestMsg(talker);
      QuestState questState = talker.getQuestState("_309_ForAGoodCause");
      if (questState != null) {
         int currentQuestCondition = questState.getInt("cond");
         QuestState reedFieldMaintenanceState = talker.getQuestState("_308_ReedFieldMaintenance");
         if (reedFieldMaintenanceState != null && reedFieldMaintenanceState.getState() == 1) {
            htmltext = "32647-15.htm";
         } else if (currentQuestCondition == 0) {
            if (talker.getLevel() < 82) {
               htmltext = "32647-00.htm";
               questState.exitQuest(true);
            } else {
               htmltext = "32647-01.htm";
            }
         } else if (1 == questState.getState()) {
            if (questState.getQuestItemsCount(14873) < 1L && questState.getQuestItemsCount(14874) < 1L) {
               htmltext = "32647-06.htm";
            } else {
               htmltext = "32647-08.htm";
            }
         }
      }

      return htmltext;
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (null == partyMember) {
         return null;
      } else {
         QuestState questState = partyMember.getQuestState("_309_ForAGoodCause");
         if (null == questState) {
            return null;
         } else {
            int killedNPCID = npc.getId();
            int itemIDToGive = 0;
            int itemCountToGive = 1;
            if (22655 == killedNPCID && (float)getRandom(100) < 100.0F * Config.RATE_QUEST_DROP) {
               itemIDToGive = 14874;
            } else {
               boolean containsKilledNPC = false;

               for(int currentNPCID : MUCROKIANS) {
                  if (currentNPCID == killedNPCID) {
                     containsKilledNPC = true;
                     break;
                  }
               }

               if (containsKilledNPC && (float)getRandom(100) < 100.0F * Config.RATE_QUEST_DROP) {
                  itemIDToGive = 14873;
               }
            }

            if (itemIDToGive > 0) {
               questState.giveItems(itemIDToGive, (long)itemCountToGive);
               questState.playSound("ItemSound.quest_itemget");
            }

            return null;
         }
      }
   }

   public static void main(String[] args) {
      new _309_ForAGoodCause(309, "_309_ForAGoodCause", "");
   }
}
