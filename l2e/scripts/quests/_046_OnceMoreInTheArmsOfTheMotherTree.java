package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _046_OnceMoreInTheArmsOfTheMotherTree extends Quest {
   public _046_OnceMoreInTheArmsOfTheMotherTree(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30097);
      this.addTalkId(30097);
      this.addTalkId(30094);
      this.addTalkId(30090);
      this.addTalkId(30116);
      this.questItemIds = new int[]{7563, 7564, 7565, 7568, 7567, 7566};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            st.startQuest();
            st.giveItems(7563, 1L);
            htmltext = "30097-03.htm";
         } else if (event.equalsIgnoreCase("2")) {
            st.takeItems(7563, 1L);
            st.giveItems(7568, 1L);
            st.setCond(2, true);
            htmltext = "30094-02.htm";
         } else if (event.equalsIgnoreCase("3")) {
            st.takeItems(7568, 1L);
            st.giveItems(7564, 1L);
            st.setCond(3, true);
            htmltext = "30097-06.htm";
         } else if (event.equalsIgnoreCase("4")) {
            st.takeItems(7564, 1L);
            st.giveItems(7567, 1L);
            st.setCond(4, true);
            htmltext = "30090-02.htm";
         } else if (event.equalsIgnoreCase("5")) {
            st.takeItems(7567, 1L);
            st.giveItems(7565, 1L);
            st.setCond(5, true);
            htmltext = "30097-09.htm";
         } else if (event.equalsIgnoreCase("6")) {
            st.takeItems(7565, 1L);
            st.giveItems(7566, 1L);
            st.setCond(6, true);
            htmltext = "30116-02.htm";
         } else if (event.equalsIgnoreCase("7")) {
            st.takeItems(7566, 1L);
            st.takeItems(7570, -1L);
            htmltext = "30097-12.htm";
            st.calcReward(this.getId());
            st.exitQuest(false, true);
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
         byte id = st.getState();
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         } else if (id == 0) {
            if (npcId == 30097 & st.getCond() == 0) {
               if (player.getRace().ordinal() == 1 && st.getQuestItemsCount(7570) > 0L) {
                  htmltext = "30097-02.htm";
               } else {
                  htmltext = "30097-01.htm";
                  st.exitQuest(true);
               }
            }
         } else if (id == 1) {
            if (npcId == 30097 && st.getCond() == 1) {
               htmltext = "30097-04.htm";
            } else if (npcId == 30097 && st.getCond() == 2) {
               htmltext = "30097-05.htm";
            } else if (npcId == 30097 && st.getCond() == 3) {
               htmltext = "30097-07.htm";
            } else if (npcId == 30097 && st.getCond() == 4) {
               htmltext = "30097-08.htm";
            } else if (npcId == 30097 && st.getCond() == 5) {
               htmltext = "30097-10.htm";
            } else if (npcId == 30097 && st.getCond() == 6) {
               htmltext = "30097-11.htm";
            } else if (npcId == 30094 && st.getCond() == 1) {
               htmltext = "30094-01.htm";
            } else if (npcId == 30094 && st.getCond() == 2) {
               htmltext = "30094-03.htm";
            } else if (npcId == 30090 && st.getCond() == 3) {
               htmltext = "30090-01.htm";
            } else if (npcId == 30090 && st.getCond() == 4) {
               htmltext = "30090-03.htm";
            } else if (npcId == 30116 && st.getCond() == 5) {
               htmltext = "30116-01.htm";
            } else if (npcId == 30116 && st.getCond() == 6) {
               htmltext = "30116-03.htm";
            }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _046_OnceMoreInTheArmsOfTheMotherTree(46, _046_OnceMoreInTheArmsOfTheMotherTree.class.getSimpleName(), "");
   }
}
