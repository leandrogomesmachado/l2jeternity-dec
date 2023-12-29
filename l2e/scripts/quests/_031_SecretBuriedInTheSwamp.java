package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _031_SecretBuriedInTheSwamp extends Quest {
   public _031_SecretBuriedInTheSwamp(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31555);
      this.addTalkId(new int[]{31555, 31661, 31662, 31663, 31664, 31665});
      this.questItemIds = new int[]{7252};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         int cond = st.getCond();
         if (event.equalsIgnoreCase("31555-1.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31665-1.htm") && cond == 1) {
            st.setCond(2, true);
            st.giveItems(7252, 1L);
         } else if (event.equalsIgnoreCase("31555-4.htm") && cond == 2) {
            st.setCond(3, true);
         } else if (event.equalsIgnoreCase("31661-1.htm") && cond == 3) {
            st.setCond(4, true);
         } else if (event.equalsIgnoreCase("31662-1.htm") && cond == 4) {
            st.setCond(5, true);
         } else if (event.equalsIgnoreCase("31663-1.htm") && cond == 5) {
            st.setCond(6, true);
         } else if (event.equalsIgnoreCase("31664-1.htm") && cond == 6) {
            st.setCond(7, true);
         } else if (event.equalsIgnoreCase("31555-7.htm") && cond == 7) {
            st.takeItems(7252, -1L);
            st.calcExpAndSp(this.getId());
            st.calcReward(this.getId());
            st.exitQuest(false, true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else if (st.isCompleted()) {
         return getAlreadyCompletedMsg(player);
      } else {
         int npcId = npc.getId();
         int cond = st.getCond();
         if (npcId == 31555) {
            if (cond == 0) {
               if (player.getLevel() >= 66) {
                  htmltext = "31555-0.htm";
               } else {
                  htmltext = "31555-0a.htm";
                  st.exitQuest(true);
               }
            } else if (cond == 1) {
               htmltext = "31555-2.htm";
            } else if (cond == 2) {
               htmltext = "31555-3.htm";
            } else if (cond == 3) {
               htmltext = "31555-5.htm";
            } else if (cond == 7) {
               htmltext = "31555-6.htm";
            }
         } else if (npcId == 31665) {
            if (cond == 1) {
               htmltext = "31665-0.htm";
            } else if (cond == 2) {
               htmltext = "31665-2.htm";
            }
         } else if (npcId == 31661) {
            if (cond == 3) {
               htmltext = "31661-0.htm";
            } else if (cond > 3) {
               htmltext = "31661-2.htm";
            }
         } else if (npcId == 31662) {
            if (cond == 4) {
               htmltext = "31662-0.htm";
            } else if (cond > 4) {
               htmltext = "31662-2.htm";
            }
         } else if (npcId == 31663) {
            if (cond == 5) {
               htmltext = "31663-0.htm";
            } else if (cond > 5) {
               htmltext = "31663-2.htm";
            }
         } else if (npcId == 31664) {
            if (cond == 6) {
               htmltext = "31664-0.htm";
            } else if (cond > 6) {
               htmltext = "31664-2.htm";
            }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _031_SecretBuriedInTheSwamp(31, _031_SecretBuriedInTheSwamp.class.getSimpleName(), "");
   }
}
