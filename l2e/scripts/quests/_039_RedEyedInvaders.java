package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _039_RedEyedInvaders extends Quest {
   public _039_RedEyedInvaders(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30334);
      this.addTalkId(30334);
      this.addTalkId(30332);
      this.addKillId(20919);
      this.addKillId(20920);
      this.addKillId(20921);
      this.addKillId(20925);
      this.questItemIds = new int[]{7178, 7180, 7179, 7181};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30334-1.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("30332-1.htm")) {
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("30332-3a.htm")) {
            if (st.getQuestItemsCount(7178) == 100L && st.getQuestItemsCount(7178) == 100L) {
               st.takeItems(7178, -1L);
               st.takeItems(7179, -1L);
               st.setCond(4, true);
            } else {
               htmltext = "no_items.htm";
            }
         } else if (event.equalsIgnoreCase("30332-5.htm")) {
            if (st.getQuestItemsCount(7180) == 30L && st.getQuestItemsCount(7181) == 30L) {
               st.takeItems(7180, -1L);
               st.takeItems(7181, -1L);
               st.calcReward(this.getId());
               st.exitQuest(false, true);
            } else {
               htmltext = "no_items.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getCond();
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         }

         if (npcId == 30334) {
            if (cond == 0) {
               if (player.getLevel() < 20) {
                  htmltext = "30334-2.htm";
                  st.exitQuest(true);
               } else if (player.getLevel() >= 20) {
                  htmltext = "30334-0.htm";
               }
            } else if (cond == 1) {
               htmltext = "30334-3.htm";
            }
         } else if (npcId == 30332) {
            if (cond == 1) {
               htmltext = "30332-0.htm";
            } else if (cond != 2 || st.getQuestItemsCount(7178) >= 100L && st.getQuestItemsCount(7179) >= 100L) {
               if (cond == 3 && st.getQuestItemsCount(7178) == 100L && st.getQuestItemsCount(7179) == 100L) {
                  htmltext = "30332-3.htm";
               } else if (cond != 4 || st.getQuestItemsCount(7180) >= 30L && st.getQuestItemsCount(7181) >= 30L) {
                  if (cond == 5 && st.getQuestItemsCount(7180) == 30L && st.getQuestItemsCount(7181) == 30L) {
                     htmltext = "30332-4.htm";
                  }
               } else {
                  htmltext = "30332-3b.htm";
               }
            } else {
               htmltext = "30332-2.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st.isCond(2)) {
            if (npc.getId() == 20919 || npc.getId() == 20920) {
               st.calcDoDropItems(this.getId(), 7178, npc.getId(), 100);
            } else if (npc.getId() == 20921) {
               st.calcDoDropItems(this.getId(), 7179, npc.getId(), 100);
            }

            if (st.getQuestItemsCount(7178) + st.getQuestItemsCount(7179) == 200L) {
               st.setCond(3, true);
            }
         }

         if (st.isCond(4)) {
            if (npc.getId() == 20920 || npc.getId() == 20921) {
               st.calcDoDropItems(this.getId(), 7180, npc.getId(), 30);
            } else if (npc.getId() == 20925) {
               st.calcDoDropItems(this.getId(), 7181, npc.getId(), 30);
            }

            if (st.getQuestItemsCount(7180) + st.getQuestItemsCount(7181) == 60L) {
               st.setCond(5, true);
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _039_RedEyedInvaders(39, _039_RedEyedInvaders.class.getSimpleName(), "");
   }
}
