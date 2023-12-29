package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _104_SpiritOfMirrors extends Quest {
   private static final String qn = "_104_SpiritOfMirrors";
   private static final int GALLINT_OAK_WAND = 748;
   private static final int WAND_SPIRITBOUND1 = 1135;
   private static final int WAND_SPIRITBOUND2 = 1136;
   private static final int WAND_SPIRITBOUND3 = 1137;
   private static final int LONG_SWORD = 2;
   private static final int WAND_OF_ADEPT = 747;
   private static final int SPIRITSHOT_NO_GRADE_FOR_BEGINNERS = 5790;
   private static final int SOULSHOT_NO_GRADE_FOR_BEGINNERS = 5789;
   private static final int SPIRITSHOT_NO_GRADE = 2509;
   private static final int SOULSHOT_NO_GRADE = 1835;
   private static final int LESSER_HEALING_POT = 1060;
   private static final int GALLINT = 30017;
   private static final int ARNOLD = 30041;
   private static final int JOHNSTONE = 30043;
   private static final int KENYOS = 30045;
   private final int[] talkNpc = new int[]{30017, 30041, 30043, 30045};
   private static final int[][] DROPLIST_COND = new int[][]{{27003, 1135}, {27004, 1136}, {27005, 1137}};

   public _104_SpiritOfMirrors(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30017);

      for(int npcId : this.talkNpc) {
         this.addTalkId(npcId);
      }

      for(int[] element : DROPLIST_COND) {
         this.addKillId(element[0]);
      }

      this.questItemIds = new int[]{1135, 1136, 1137, 748};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_104_SpiritOfMirrors");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30017-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(748, 3L);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_104_SpiritOfMirrors");
      if (st == null) {
         return htmltext;
      } else {
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         }

         int npcId = npc.getId();
         int cond = st.getInt("cond");
         int id = st.getState();
         if (id == 0 && cond == 0) {
            if (npcId == 30017) {
               if (player.getRace().ordinal() != 0) {
                  htmltext = "30017-00.htm";
                  st.exitQuest(true);
               } else {
                  if (player.getLevel() >= 10) {
                     return "30017-02.htm";
                  }

                  htmltext = "30017-06.htm";
                  st.exitQuest(true);
               }
            }
         } else if (id == 1) {
            if (npcId == 30017) {
               if ((cond != 1 || st.getQuestItemsCount(748) < 1L || st.getQuestItemsCount(1135) != 0L)
                  && st.getQuestItemsCount(1136) != 0L
                  && st.getQuestItemsCount(1137) != 0L) {
                  if (cond == 3 && st.getQuestItemsCount(1135) == 1L && st.getQuestItemsCount(1136) == 1L && st.getQuestItemsCount(1137) == 1L) {
                     st.takeItems(1135, 1L);
                     st.takeItems(1136, 1L);
                     st.takeItems(1137, 1L);
                     st.giveItems(1060, 100L);

                     for(int ECHO_CHRYSTAL = 4412; ECHO_CHRYSTAL <= 4416; ++ECHO_CHRYSTAL) {
                        st.giveItems(ECHO_CHRYSTAL, 10L);
                     }

                     if (player.getClassId().isMage()) {
                        st.giveItems(5790, 3000L);
                        st.giveItems(2509, 500L);
                        st.giveItems(747, 1L);
                     } else {
                        st.giveItems(5789, 6000L);
                        st.giveItems(1835, 1000L);
                        st.giveItems(2, 1L);
                     }

                     showOnScreenMsg(player, NpcStringId.ACQUISITION_OF_RACE_SPECIFIC_WEAPON_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
                     st.addExpAndSp(39750, 3407);
                     st.giveItems(57, 16866L);
                     htmltext = "30017-05.htm";
                     st.unset("cond");
                     st.exitQuest(false);
                     st.playSound("ItemSound.quest_finish");
                  }
               } else {
                  htmltext = "30017-04.htm";
               }
            } else if (npcId == 30041 || npcId == 30043 || npcId == 30045 && cond >= 1) {
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
               htmltext = npcId + "-01.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_104_SpiritOfMirrors");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();

         for(int[] element : DROPLIST_COND) {
            if (st.getInt("cond") >= 1 && st.getItemEquipped(5) == 748 && npcId == element[0] && st.getQuestItemsCount(element[1]) == 0L) {
               st.takeItems(748, 1L);
               st.giveItems(element[1], 1L);
               long HaveAllQuestItems = st.getQuestItemsCount(1135) + st.getQuestItemsCount(1136) + st.getQuestItemsCount(1137);
               if (HaveAllQuestItems == 3L) {
                  st.set("cond", "3");
                  st.playSound("ItemSound.quest_middle");
               } else {
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _104_SpiritOfMirrors(104, "_104_SpiritOfMirrors", "");
   }
}
