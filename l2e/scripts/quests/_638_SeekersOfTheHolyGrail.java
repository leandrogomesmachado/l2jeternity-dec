package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _638_SeekersOfTheHolyGrail extends Quest {
   private static final String qn = "_638_SeekersOfTheHolyGrail";
   private static final int DROP_CHANCE = 30;
   private static final int INNOCENTIN = 31328;
   private static final int[] MOBS = new int[]{
      22138,
      22139,
      22140,
      22142,
      22143,
      22144,
      22145,
      22146,
      22147,
      22148,
      22149,
      22150,
      22151,
      22152,
      22153,
      22154,
      22154,
      22155,
      22156,
      22157,
      22158,
      22159,
      22160,
      22161,
      22161,
      22162,
      22163,
      22164,
      22165,
      22166,
      22167,
      22168,
      22169,
      22170,
      22171,
      22172,
      22173,
      22174,
      22175
   };
   private static final int TOTEM = 8068;
   private static final int ANTEROOMKEY = 8273;
   private static final int CHAPELKEY = 8274;
   private static final int KEYOFDARKNESS = 8275;
   private static final int RitualOffering = 22149;
   private static final int ZombieWorker = 22140;
   private static final int TriolsBeliever = 22143;
   private static final int TriolsLayperson = 22142;
   private static final int TriolsPriest2 = 22151;
   private static final int TriolsPriest3 = 22146;

   public _638_SeekersOfTheHolyGrail(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31328);
      this.addTalkId(31328);

      for(int npcId : MOBS) {
         this.addKillId(npcId);
      }

      this.questItemIds = new int[]{8068};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_638_SeekersOfTheHolyGrail");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31328-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("31328-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_638_SeekersOfTheHolyGrail");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 73) {
                  htmltext = "31328-01.htm";
               } else {
                  htmltext = "31328-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(8068) >= 2000L) {
                  int rr = st.getRandom(3);
                  if (rr == 0) {
                     st.takeItems(8068, 2000L);
                     st.giveItems(959, (long)(st.getRandom(4) + 3));
                     st.playSound("ItemSound.quest_middle");
                  }

                  if (rr == 1) {
                     st.takeItems(8068, 2000L);
                     st.giveItems(57, 3576000L);
                     st.playSound("ItemSound.quest_middle");
                  }

                  if (rr == 2) {
                     st.takeItems(8068, 2000L);
                     st.giveItems(960, (long)(st.getRandom(4) + 3));
                     st.playSound("ItemSound.quest_middle");
                  }

                  htmltext = "31328-03.htm";
               } else {
                  htmltext = "31328-04.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st == null) {
            return null;
         } else {
            int npcId = npc.getId();
            if (st.getInt("cond") == 1) {
               st.dropQuestItems(8068, 1, 1, 0L, true, 30.0F, true);
               if (npcId == 22149 || npcId == 22140) {
                  st.giveItems(8273, 6L);
               } else if (npcId == 22143 || npcId == 22142) {
                  st.giveItems(8274, 1L);
               } else if ((npcId == 22151 || npcId == 22146) && getRandom(100) < 10) {
                  st.giveItems(8275, 1L);
               }
            }

            return null;
         }
      }
   }

   public static void main(String[] args) {
      new _638_SeekersOfTheHolyGrail(638, "_638_SeekersOfTheHolyGrail", "");
   }
}
