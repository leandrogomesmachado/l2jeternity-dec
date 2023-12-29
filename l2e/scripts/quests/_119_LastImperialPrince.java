package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _119_LastImperialPrince extends Quest {
   private static final String qn = "_119_LastImperialPrince";
   private static int SPIRIT = 31453;
   private static int DEVORIN = 32009;
   private static int BROOCH = 7262;

   public _119_LastImperialPrince(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(SPIRIT);
      this.addTalkId(SPIRIT);
      this.addTalkId(DEVORIN);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_119_LastImperialPrince");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31453-4.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32009-2.htm")) {
            if (st.getQuestItemsCount(BROOCH) < 1L) {
               htmltext = "32009-2a.htm";
               st.exitQuest(true);
            }
         } else if (event.equalsIgnoreCase("32009-3.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("31453-7.htm")) {
            st.giveItems(57, 150292L);
            st.addExpAndSp(902439, 90067);
            st.setState((byte)2);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_119_LastImperialPrince");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 74) {
                  htmltext = "31453-0.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "31453-1.htm";
               }
               break;
            case 1:
               if (npcId == SPIRIT) {
                  if (cond == 1) {
                     htmltext = "31453-4.htm";
                  } else if (cond == 2) {
                     htmltext = "31453-5.htm";
                  }
               } else if (npcId == DEVORIN) {
                  if (cond == 1) {
                     htmltext = "32009-1.htm";
                  } else if (cond == 2) {
                     htmltext = "32009-3.htm";
                  }
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _119_LastImperialPrince(119, "_119_LastImperialPrince", "");
   }
}
