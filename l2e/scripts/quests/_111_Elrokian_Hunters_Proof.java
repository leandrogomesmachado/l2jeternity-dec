package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _111_Elrokian_Hunters_Proof extends Quest {
   private static final String qn = "_111_Elrokian_Hunters_Proof";
   private static final int[] QUEST_NPC = new int[]{32113, 32114, 32115, 32116, 32117};
   private static final int[] QUEST_ITEM = new int[]{8768};
   private static final int[] QUEST_MONSTERS1 = new int[]{22196, 22197, 22198, 22218};
   private static final int[] QUEST_MONSTERS2 = new int[]{22200, 22201, 22202, 22219};
   private static final int[] QUEST_MONSTERS3 = new int[]{22208, 22209, 22210, 22221};
   private static final int[] QUEST_MONSTERS4 = new int[]{22203, 22204, 22205, 22220};

   public _111_Elrokian_Hunters_Proof(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(QUEST_NPC[0]);

      for(int i : QUEST_NPC) {
         this.addTalkId(i);
      }

      for(int i : QUEST_MONSTERS1) {
         this.addKillId(i);
      }

      for(int i : QUEST_MONSTERS2) {
         this.addKillId(i);
      }

      for(int i : QUEST_MONSTERS3) {
         this.addKillId(i);
      }

      for(int i : QUEST_MONSTERS4) {
         this.addKillId(i);
      }

      this.questItemIds = QUEST_ITEM;
   }

   private boolean checkPartyCondition(QuestState st, Player leader) {
      if (leader == null) {
         return false;
      } else {
         Party party = leader.getParty();
         if (party == null) {
            return false;
         } else if (party.getLeader() != leader) {
            return false;
         } else {
            for(Player player : party.getMembers()) {
               if (player.getLevel() < 75) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_111_Elrokian_Hunters_Proof");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         switch(st.getState()) {
            case 0:
               if (npcId == QUEST_NPC[0]) {
                  if (this.checkPartyCondition(st, player)) {
                     st.set("cond", "1");
                     st.playSound("ItemSound.quest_accept");
                     st.setState((byte)1);
                     htmltext = "32113-1.htm";
                  } else {
                     st.exitQuest(true);
                     htmltext = "32113-0.htm";
                  }
               }
               break;
            case 1:
               if (npcId == QUEST_NPC[0]) {
                  switch(cond) {
                     case 3:
                        st.set("cond", "4");
                        st.playSound("ItemSound.quest_middle");
                        return "32113-2.htm";
                     case 5:
                        if (st.getQuestItemsCount(QUEST_ITEM[0]) >= 50L) {
                           st.takeItems(QUEST_ITEM[0], -1L);
                           st.set("cond", "6");
                           st.playSound("ItemSound.quest_middle");
                           htmltext = "32113-3.htm";
                        }
                  }
               } else if (npcId == QUEST_NPC[1]) {
                  if (cond == 1) {
                     st.set("cond", "2");
                     st.playSound("ItemSound.quest_middle");
                     htmltext = "32114-1.htm";
                  }
               } else if (npcId == QUEST_NPC[2]) {
                  switch(cond) {
                     case 2:
                        st.set("cond", "3");
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "32115-1.htm";
                        return htmltext;
                     case 3:
                     case 4:
                     case 5:
                     case 6:
                     case 7:
                     case 10:
                     default:
                        return htmltext;
                     case 8:
                        st.set("cond", "9");
                        st.playSound("ItemSound.quest_middle");
                        return "32115-2.htm";
                     case 9:
                        st.set("cond", "10");
                        st.playSound("ItemSound.quest_middle");
                        return "32115-3.htm";
                     case 11:
                        st.set("cond", "12");
                        st.playSound("ItemSound.quest_middle");
                        st.giveItems(8773, 1L);
                        htmltext = "32115-5.htm";
                  }
               } else if (npcId == QUEST_NPC[3]) {
                  switch(cond) {
                     case 6:
                        st.set("cond", "8");
                        st.playSound("EtcSound.elcroki_song_full");
                        return "32116-1.htm";
                     case 12:
                        if (st.getQuestItemsCount(8773) >= 1L) {
                           st.takeItems(8773, 1L);
                           st.giveItems(8763, 1L);
                           st.giveItems(8764, 100L);
                           st.giveItems(57, 1022636L);
                           st.playSound("ItemSound.quest_finish");
                           st.exitQuest(false);
                           htmltext = "32116-2.htm";
                        }
                  }
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      if (player != null && player.getParty() != null) {
         QuestState st = player.getParty().getLeader().getQuestState("_111_Elrokian_Hunters_Proof");
         if (st != null && st.getState() == 1) {
            int cond = st.getInt("cond");
            int npcId = npc.getId();
            switch(cond) {
               case 4:
                  if (Util.contains(QUEST_MONSTERS1, npcId) && getRandom(100) < 25) {
                     st.giveItems(QUEST_ITEM[0], 1L);
                     if (st.getQuestItemsCount(QUEST_ITEM[0]) <= 49L) {
                        st.playSound("ItemSound.quest_itemget");
                     } else {
                        st.set("cond", "5");
                        st.playSound("ItemSound.quest_middle");
                     }
                  }
                  break;
               case 10:
                  if (Util.contains(QUEST_MONSTERS2, npcId)) {
                     if (getRandom(100) < 75) {
                        st.giveItems(8770, 1L);
                        if (st.getQuestItemsCount(8770) <= 9L) {
                           st.playSound("ItemSound.quest_itemget");
                        }
                     }
                  } else if (Util.contains(QUEST_MONSTERS3, npcId)) {
                     if (getRandom(100) < 75) {
                        st.giveItems(8772, 1L);
                        if (st.getQuestItemsCount(8771) <= 9L) {
                           st.playSound("ItemSound.quest_itemget");
                        }
                     }
                  } else if (Util.contains(QUEST_MONSTERS4, npcId) && getRandom(100) < 75) {
                     st.giveItems(8771, 1L);
                     if (st.getQuestItemsCount(8772) <= 9L) {
                        st.playSound("ItemSound.quest_itemget");
                     }
                  }

                  if (st.getQuestItemsCount(8770) >= 10L && st.getQuestItemsCount(8771) >= 10L && st.getQuestItemsCount(8772) >= 10L) {
                     st.set("cond", "11");
                     st.playSound("ItemSound.quest_middle");
                  }
            }

            return super.onKill(npc, player, isSummon);
         } else {
            return super.onKill(npc, player, isSummon);
         }
      } else {
         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _111_Elrokian_Hunters_Proof(111, "_111_Elrokian_Hunters_Proof", "");
   }
}
