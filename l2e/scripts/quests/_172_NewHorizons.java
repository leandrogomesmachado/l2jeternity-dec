package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _172_NewHorizons extends Quest {
   private static final String qn = "_172_NewHorizons";
   private static int ZENYA = 32140;
   private static int RAGARA = 32163;
   private static int SCROLL_OF_ESCAPE_GIRAN = 7559;
   private static int MARK_OF_TRAVELER = 7570;

   public _172_NewHorizons(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(ZENYA);
      this.addTalkId(ZENYA);
      this.addTalkId(RAGARA);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_172_NewHorizons");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32140-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32163-02.htm")) {
            st.unset("cond");
            st.giveItems(SCROLL_OF_ESCAPE_GIRAN, 1L);
            st.giveItems(MARK_OF_TRAVELER, 1L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_172_NewHorizons");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         switch(st.getState()) {
            case 0:
               if (npcId == ZENYA) {
                  if (player.getLevel() >= 3 && player.getRace().ordinal() == 5) {
                     htmltext = "32140-01.htm";
                  } else {
                     htmltext = "32140-02.htm";
                     st.exitQuest(true);
                  }
               }
               break;
            case 1:
               if (npcId == ZENYA) {
                  if (cond == 1) {
                     htmltext = "32140-04.htm";
                  }
               } else if (npcId == RAGARA && cond == 1) {
                  htmltext = "32163-01.htm";
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _172_NewHorizons(172, "_172_NewHorizons", "");
   }
}
