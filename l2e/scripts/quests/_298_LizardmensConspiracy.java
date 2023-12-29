package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _298_LizardmensConspiracy extends Quest {
   private static final String qn = "_298_LizardmensConspiracy";
   private static final int PRAGA = 30333;
   private static final int ROHMER = 30344;
   private static final int PATROL_REPORT = 7182;
   private static final int WHITE_GEM = 7183;
   private static final int RED_GEM = 7184;

   public _298_LizardmensConspiracy(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30333);
      this.addTalkId(30333);
      this.addTalkId(30344);
      this.addKillId(new int[]{20926, 20927, 20922, 20923, 20924});
      this.questItemIds = new int[]{7182, 7183, 7184};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_298_LizardmensConspiracy");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30333-1.htm")) {
            st.set("cond", "1");
            st.giveItems(7182, 1L);
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30344-1.htm")) {
            st.takeItems(7182, 1L);
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30344-3.htm")) {
            if (st.getQuestItemsCount(7183) >= 50L && st.getQuestItemsCount(7184) >= 50L) {
               st.takeItems(7183, -1L);
               st.takeItems(7184, -1L);
               st.addExpAndSp(0, 42000);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(true);
            } else {
               htmltext = "30344-4.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_298_LizardmensConspiracy");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 25 && player.getLevel() <= 34) {
                  htmltext = "30333-0a.htm";
               } else {
                  htmltext = "30333-0b.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30333:
                     htmltext = "30333-2.htm";
                     break;
                  case 30344:
                     if (cond == 1) {
                        if (st.getQuestItemsCount(7182) == 1L) {
                           htmltext = "30344-0.htm";
                        } else {
                           htmltext = "30344-0a.htm";
                        }
                     } else if (cond == 2 || cond == 3) {
                        htmltext = "30344-2.htm";
                     }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 2);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState("_298_LizardmensConspiracy");
         if (st.getRandom(100) < 62) {
            switch(npc.getId()) {
               case 20922:
               case 20923:
               case 20924:
                  if (st.getQuestItemsCount(7183) < 50L) {
                     st.giveItems(7183, 1L);
                     if (st.getQuestItemsCount(7184) >= 50L && st.getQuestItemsCount(7183) >= 50L) {
                        st.set("cond", "3");
                        st.playSound("ItemSound.quest_middle");
                     } else {
                        st.playSound("ItemSound.quest_itemget");
                     }
                  }
               case 20925:
               default:
                  break;
               case 20926:
               case 20927:
                  if (st.getQuestItemsCount(7184) < 50L) {
                     st.giveItems(7184, 1L);
                     if (st.getQuestItemsCount(7183) >= 50L && st.getQuestItemsCount(7184) >= 50L) {
                        st.set("cond", "3");
                        st.playSound("ItemSound.quest_middle");
                     } else {
                        st.playSound("ItemSound.quest_itemget");
                     }
                  }
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _298_LizardmensConspiracy(298, "_298_LizardmensConspiracy", "");
   }
}
