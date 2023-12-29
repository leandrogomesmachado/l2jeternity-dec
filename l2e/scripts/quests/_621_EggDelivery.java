package l2e.scripts.quests;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _621_EggDelivery extends Quest {
   private static final String qn = "_621_EggDelivery";
   private final int RPCHANCE = 10;
   private final int JEREMY = 31521;
   private final int PULIN = 31543;
   private final int NAFF = 31544;
   private final int CROCUS = 31545;
   private final int KUBER = 31546;
   private final int BEORIN = 31547;
   private final int VALENTINE = 31584;
   private final int[] NPCS = new int[]{31543, 31544, 31545, 31546, 31547, 31521, 31584};
   private final int BOILED_EGGS = 7195;
   private final int FEE_OF_EGGS = 7196;
   private final int[] ITEMS = new int[]{6847, 6849, 6851};
   private final int HASTE_POTION = 734;

   public _621_EggDelivery(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31521);

      for(int i = 0; i < this.NPCS.length; ++i) {
         this.addTalkId(this.NPCS[i]);
      }

      this.questItemIds = new int[]{7195, 7196};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_621_EggDelivery");
      if (st == null) {
         return event;
      } else {
         int cond = st.getInt("cond");
         if (event.equalsIgnoreCase("31521-1.htm")) {
            if (cond == 0) {
               st.set("cond", "1");
               st.setState((byte)1);
               st.giveItems(7195, 5L);
               st.playSound("ItemSound.quest_accept");
            } else {
               htmltext = getNoQuestMsg(player);
            }
         } else if (event.equalsIgnoreCase("31543-1.htm")) {
            if (st.getQuestItemsCount(7195) > 0L) {
               if (cond == 1) {
                  st.takeItems(7195, 1L);
                  st.giveItems(7196, 1L);
                  st.set("cond", "2");
               } else {
                  htmltext = getNoQuestMsg(player);
               }
            } else {
               htmltext = "LMFAO!";
               st.exitQuest(true);
            }
         } else if (event.equalsIgnoreCase("31544-1.htm")) {
            if (st.getQuestItemsCount(7195) > 0L) {
               if (cond == 2) {
                  st.takeItems(7195, 1L);
                  st.giveItems(7196, 1L);
                  st.set("cond", "3");
               } else {
                  htmltext = getNoQuestMsg(player);
               }
            } else {
               htmltext = "LMFAO!";
               st.exitQuest(true);
            }
         } else if (event.equalsIgnoreCase("31545-1.htm")) {
            if (st.getQuestItemsCount(7195) > 0L) {
               if (cond == 3) {
                  st.takeItems(7195, 1L);
                  st.giveItems(7196, 1L);
                  st.set("cond", "4");
               } else {
                  htmltext = getNoQuestMsg(player);
               }
            } else {
               htmltext = "LMFAO!";
               st.exitQuest(true);
            }
         } else if (event.equalsIgnoreCase("31546-1.htm")) {
            if (st.getQuestItemsCount(7195) > 0L) {
               if (cond == 4) {
                  st.takeItems(7195, 1L);
                  st.giveItems(7196, 1L);
                  st.set("cond", "5");
               } else {
                  htmltext = getNoQuestMsg(player);
               }
            } else {
               htmltext = "LMFAO!";
               st.exitQuest(true);
            }
         } else if (event.equalsIgnoreCase("31547-1.htm")) {
            if (st.getQuestItemsCount(7195) > 0L) {
               if (cond == 5) {
                  st.takeItems(7195, 1L);
                  st.giveItems(7196, 1L);
                  st.set("cond", "6");
               } else {
                  htmltext = getNoQuestMsg(player);
               }
            } else {
               htmltext = "LMFAO!";
               st.exitQuest(true);
            }
         } else if (event.equalsIgnoreCase("31521-3.htm")) {
            st.set("cond", "7");
         } else if (event.equalsIgnoreCase("31584-2.htm")) {
            if (st.getQuestItemsCount(7196) == 5L) {
               st.takeItems(7196, 5L);
               if (st.getRandom(100) < 10) {
                  st.giveItems(this.ITEMS[st.getRandom(3)], 1L);
               } else {
                  st.rewardItems(57, 18800L);
                  st.giveItems(734, (long)((int)Config.RATE_QUEST_REWARD));
               }

               st.playSound("ItemSound.quest_finish");
               st.exitQuest(true);
            } else {
               htmltext = getNoQuestMsg(player);
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_621_EggDelivery");
      if (st != null) {
         int npcId = npc.getId();
         byte id = st.getState();
         if (id == 0) {
            st.set("cond", "0");
         }

         int cond = st.getInt("cond");
         if (npcId == 31521) {
            if (cond == 0) {
               if (player.getLevel() >= 68) {
                  htmltext = "31521-0.htm";
               } else {
                  st.exitQuest(true);
               }
            } else if (cond == 6 && st.getQuestItemsCount(7196) == 5L) {
               htmltext = "31521-2.htm";
            } else if (cond == 7 && st.getQuestItemsCount(7196) == 5L) {
               htmltext = "31521-4.htm";
            }
         } else if (id == 1 && st.getQuestItemsCount(7195) > 0L) {
            if (npcId == 31543 && cond == 1) {
               htmltext = "31543-0.htm";
            } else if (npcId == 31544 && cond == 2) {
               htmltext = "31544-0.htm";
            } else if (npcId == 31545 && cond == 3) {
               htmltext = "31545-0.htm";
            } else if (npcId == 31546 && cond == 4) {
               htmltext = "31546-0.htm";
            } else if (npcId == 31547 && cond == 5) {
               htmltext = "31547-0.htm";
            }
         } else if (npcId == 31584 && cond == 7 && st.getQuestItemsCount(7196) == 5L) {
            htmltext = "31584-1.htm";
         }
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _621_EggDelivery(621, "_621_EggDelivery", "");
   }
}
