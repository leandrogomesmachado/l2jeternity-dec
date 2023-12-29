package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _379_FantasyWine extends Quest {
   private static final String qn = "_379_FantasyWine";
   private static final int HARLAN = 30074;
   private static final int ENKU_CHAMPION = 20291;
   private static final int ENKU_SHAMAN = 20292;
   private static final int LEAF = 5893;
   private static final int STONE = 5894;

   public _379_FantasyWine(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30074);
      this.addTalkId(30074);
      this.addKillId(new int[]{20291, 20292});
      this.questItemIds = new int[]{5893, 5894};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_379_FantasyWine");
      if (st == null) {
         return event;
      } else {
         long leaf = st.getQuestItemsCount(5893);
         long stone = st.getQuestItemsCount(5894);
         if (event.equalsIgnoreCase("30074-3.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30074-6.htm")) {
            if (leaf == 80L && stone == 100L) {
               st.takeItems(5893, 80L);
               st.takeItems(5894, 100L);
               int rand = st.getRandom(100);
               if (rand < 25) {
                  st.giveItems(5956, 1L);
                  htmltext = "30074-6.htm";
               } else if (rand < 50) {
                  st.giveItems(5957, 1L);
                  htmltext = "30074-7.htm";
               } else {
                  st.giveItems(5958, 1L);
                  htmltext = "30074-8.htm";
               }

               st.playSound("ItemSound.quest_finish");
               st.exitQuest(true);
            } else {
               htmltext = "30074-4.htm";
            }
         } else if (event.equalsIgnoreCase("30074-2a.htm")) {
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_379_FantasyWine");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 20 && player.getLevel() <= 25) {
                  htmltext = "30074-0.htm";
               } else {
                  htmltext = "30074-0a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               long cond = (long)st.getInt("cond");
               long leaf = st.getQuestItemsCount(5893);
               long stone = st.getQuestItemsCount(5894);
               if (cond == 1L) {
                  if (leaf < 80L && stone < 100L) {
                     htmltext = "30074-4.htm";
                  } else if (leaf == 80L && stone < 100L) {
                     htmltext = "30074-4a.htm";
                  } else if (leaf < 80L & stone == 100L) {
                     htmltext = "30074-4b.htm";
                  }
               } else if (cond == 2L && leaf == 80L && stone == 100L) {
                  htmltext = "30074-5.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_379_FantasyWine");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         if (st.isStarted()) {
            if (npcId == 20291 && st.getQuestItemsCount(5893) < 80L) {
               st.giveItems(5893, 1L);
            } else if (npcId == 20292 && st.getQuestItemsCount(5894) < 100L) {
               st.giveItems(5894, 1L);
            }

            if (st.getQuestItemsCount(5893) >= 80L && st.getQuestItemsCount(5894) >= 100L) {
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "2");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _379_FantasyWine(379, "_379_FantasyWine", "");
   }
}
