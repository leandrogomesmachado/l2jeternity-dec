package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _035_FindGlitteringJewelry extends Quest {
   public _035_FindGlitteringJewelry(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30091);
      this.addTalkId(30091);
      this.addTalkId(30879);
      this.addKillId(20135);
      this.questItemIds = new int[]{7162};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         int cond = st.getCond();
         if (event.equalsIgnoreCase("30091-1.htm") && cond == 0) {
            st.startQuest();
         }

         if (event.equalsIgnoreCase("30879-1.htm") && cond == 1) {
            st.setCond(2, true);
         }

         if (event.equalsIgnoreCase("30091-3.htm") && cond == 3) {
            st.takeItems(7162, 10L);
            st.setCond(4, true);
         }

         if (event.equalsIgnoreCase("30091-5.htm") && cond == 4) {
            if (st.getQuestItemsCount(1893) < 5L || st.getQuestItemsCount(1873) < 500L || st.getQuestItemsCount(4044) < 150L) {
               return "no_items.htm";
            }

            st.takeItems(1893, 5L);
            st.takeItems(1873, 500L);
            st.takeItems(4044, 150L);
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
      } else {
         int cond = st.getCond();
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         } else if (npc.getId() == 30091 && cond == 0 && st.getQuestItemsCount(7077) == 0L) {
            QuestState fwear = player.getQuestState("_037_PleaseMakeMeFormalWear");
            if (fwear != null) {
               if (fwear.get("cond") == "6") {
                  htmltext = "30091-0.htm";
               } else {
                  htmltext = "30091-6.htm";
                  st.exitQuest(true);
               }
            } else {
               htmltext = "30091-6.htm";
               st.exitQuest(true);
            }

            st.exitQuest(true);
         } else if (npc.getId() == 30879 && cond == 1) {
            htmltext = "30879-0.htm";
         } else if (npc.getId() == 30879 && cond == 2) {
            htmltext = "30879-1a.htm";
         } else if (npc.getId() == 30879 && cond == 3) {
            htmltext = "30879-1a.htm";
         } else if (st.getState() == 1) {
            if (npc.getId() == 30091 && st.getQuestItemsCount(7162) == 10L) {
               htmltext = "30091-2.htm";
            } else {
               htmltext = "30091-1a.htm";
            }
         } else if (npc.getId() == 30091
            && cond == 4
            && st.getQuestItemsCount(1893) >= 5L
            && st.getQuestItemsCount(1873) >= 500L
            && st.getQuestItemsCount(4044) >= 150L) {
            htmltext = "30091-4.htm";
         } else {
            htmltext = "30091-3a.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 2);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st.calcDropItems(this.getId(), 7162, npc.getId(), 10)) {
            st.setCond(3);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _035_FindGlitteringJewelry(35, _035_FindGlitteringJewelry.class.getSimpleName(), "");
   }
}
