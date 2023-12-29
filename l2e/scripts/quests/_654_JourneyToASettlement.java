package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _654_JourneyToASettlement extends Quest {
   private static final String qn = "_654_JourneyToASettlement";
   private static final int SPIRIT = 31453;
   private static final int[] MOBS = new int[]{21294, 21295};
   private static final int ANTELOPE_SKIN = 8072;
   private static final int FRINTEZZA_FORCE_SCROLL = 8073;

   public _654_JourneyToASettlement(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31453);
      this.addTalkId(31453);

      for(int i : MOBS) {
         this.addKillId(i);
      }

      this.questItemIds = new int[]{8072, 8073};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_654_JourneyToASettlement");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31453-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("31453-03.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("31453-05.htm") && st.hasQuestItems(8072)) {
            st.takeItems(8072, 1L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
            st.giveItems(8073, 1L);
            st.unset("cond");
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_654_JourneyToASettlement");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               QuestState qs = player.getQuestState("_119_LastImperialPrince");
               if (player.getLevel() < 74) {
                  htmltext = "31453-06.htm";
                  st.exitQuest(true);
               } else if (qs != null && qs.isCompleted()) {
                  htmltext = "31453-01.htm";
               } else {
                  htmltext = "31453-07.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (npcId == 31453 && cond != 3) {
                  htmltext = "31453-02.htm";
               } else if (npcId == 31453 && cond == 3) {
                  htmltext = "31453-04.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_654_JourneyToASettlement");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 2) {
            st.dropQuestItems(8072, 1, 1, 1L, false, 5.0F, true);
            if (st.hasQuestItems(8072)) {
               st.set("cond", "3");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _654_JourneyToASettlement(654, "_654_JourneyToASettlement", "");
   }
}
