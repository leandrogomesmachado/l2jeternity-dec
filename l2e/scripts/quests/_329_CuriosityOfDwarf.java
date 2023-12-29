package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _329_CuriosityOfDwarf extends Quest {
   private static final String qn = "_329_CuriosityOfDwarf";
   private static int GOLEM_HEARTSTONE = 1346;
   private static int BROKEN_HEARTSTONE = 1365;

   public _329_CuriosityOfDwarf(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30437);
      this.addTalkId(30437);
      this.addKillId(new int[]{20083, 20085});
      this.questItemIds = new int[]{BROKEN_HEARTSTONE, GOLEM_HEARTSTONE};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_329_CuriosityOfDwarf");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30437-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if ("30437-06.htm".equals(event)) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_329_CuriosityOfDwarf");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 33) {
                  htmltext = "30437-02.htm";
               } else {
                  htmltext = "30437-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               int heart = (int)st.getQuestItemsCount(GOLEM_HEARTSTONE);
               int broken = (int)st.getQuestItemsCount(BROKEN_HEARTSTONE);
               if (broken + heart > 0) {
                  st.giveItems(57, (long)(50 * broken + 1000 * heart));
                  st.takeItems(BROKEN_HEARTSTONE, -1L);
                  st.takeItems(GOLEM_HEARTSTONE, -1L);
                  htmltext = "30437-05.htm";
               } else {
                  htmltext = "30437-04.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_329_CuriosityOfDwarf");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int n = st.getRandom(100);
         if (st.isStarted()) {
            if (npcId == 20085) {
               if (n < 5) {
                  st.giveItems(GOLEM_HEARTSTONE, 1L);
                  st.playSound("ItemSound.quest_itemget");
               } else if (n < 58) {
                  st.giveItems(BROKEN_HEARTSTONE, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            } else if (npcId == 20083) {
               if (n < 6) {
                  st.giveItems(GOLEM_HEARTSTONE, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }

               if (n < 56) {
                  st.giveItems(BROKEN_HEARTSTONE, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _329_CuriosityOfDwarf(329, "_329_CuriosityOfDwarf", "");
   }
}
