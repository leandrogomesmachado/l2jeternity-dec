package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _299_GatherIngredientsForPie extends Quest {
   private static final String qn = "_299_GatherIngredientsForPie";
   private static final int LARA = 30063;
   private static final int BRIGHT = 30466;
   private static final int EMILY = 30620;
   private static final int FRUIT_BASKET = 7136;
   private static final int AVELLAN_SPICE = 7137;
   private static final int HONEY_POUCH = 7138;
   private static final int WASP_WORKER = 20934;
   private static final int WASP_LEADER = 20935;

   public _299_GatherIngredientsForPie(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30620);
      this.addTalkId(30620);
      this.addTalkId(30063);
      this.addTalkId(30466);
      this.addKillId(new int[]{20934, 20935});
      this.questItemIds = new int[]{7136, 7137, 7138};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_299_GatherIngredientsForPie");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30620-1.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30620-3.htm")) {
            st.set("cond", "3");
            st.takeItems(7138, -1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30063-1.htm")) {
            st.set("cond", "4");
            st.giveItems(7137, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30620-5.htm")) {
            st.set("cond", "5");
            st.takeItems(7137, -1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30466-1.htm")) {
            st.set("cond", "6");
            st.giveItems(7136, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30620-7a.htm") && st.getQuestItemsCount(7136) >= 1L) {
            st.takeItems(7136, -1L);
            st.rewardItems(57, 25000L);
            st.giveItems(1865, 50L);
            st.unset("cond");
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_299_GatherIngredientsForPie");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 34 && player.getLevel() <= 40) {
                  htmltext = "30620-0.htm";
               } else {
                  htmltext = "30620-0a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30063:
                     if (cond == 3) {
                        htmltext = "30063-0.htm";
                     } else if (cond >= 4) {
                        htmltext = "30063-1a.htm";
                     }
                     break;
                  case 30466:
                     if (cond == 5) {
                        htmltext = "30466-0.htm";
                     } else if (cond >= 6) {
                        htmltext = "30466-1a.htm";
                     }
                     break;
                  case 30620:
                     if (cond == 1) {
                        htmltext = "30620-1a.htm";
                     } else if (cond == 2) {
                        if (st.getQuestItemsCount(7138) >= 100L) {
                           htmltext = "30620-2.htm";
                        } else {
                           htmltext = "30620-2a.htm";
                           st.exitQuest(true);
                        }
                     } else if (cond == 3) {
                        htmltext = "30620-3a.htm";
                     } else if (cond == 4) {
                        if (st.getQuestItemsCount(7137) >= 1L) {
                           htmltext = "30620-4.htm";
                        } else {
                           htmltext = "30620-4a.htm";
                           st.exitQuest(true);
                        }
                     } else if (cond == 5) {
                        htmltext = "30620-5a.htm";
                     } else if (cond == 6) {
                        htmltext = "30620-6.htm";
                     }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState("_299_GatherIngredientsForPie");
         if (st.getRandom(100) < 50) {
            st.giveItems(7138, 1L);
            if (st.getQuestItemsCount(7138) == 100L) {
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
      new _299_GatherIngredientsForPie(299, "_299_GatherIngredientsForPie", "");
   }
}
