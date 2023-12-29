package l2e.scripts.custom;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class MissQueen extends Quest {
   private static final String qn = "MissQueen";
   private static final int COUPNE_ONE = 7832;
   private static final int COUPNE_TWO = 7833;
   private static final int[] NPCs = new int[]{31760, 31761, 31762, 31763, 31764, 31765, 31766};
   private static boolean QUEEN_ENABLED = false;
   private static final int NEWBIE_REWARD = 16;
   private static final int TRAVELER_REWARD = 32;

   public MissQueen(int id, String name, String descr) {
      super(id, name, descr);

      for(int i : NPCs) {
         this.addStartNpc(i);
         this.addFirstTalkId(i);
         this.addTalkId(i);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      if (!QUEEN_ENABLED) {
         return event;
      } else {
         QuestState st = player.getQuestState("MissQueen");
         int newbie = player.getNewbie();
         int level = player.getLevel();
         int occupation_level = player.getClassId().level();
         int pkkills = player.getPkKills();
         if (event.equals("newbie_give_coupon")) {
            if (level >= 6 && level <= 25 && pkkills == 0 && occupation_level == 0) {
               if ((newbie | 16) != newbie) {
                  player.setNewbie(newbie | 16);
                  st.giveItems(7832, 1L);
                  htmltext = "31760-2.htm";
               } else {
                  htmltext = "31760-1.htm";
               }
            } else {
               htmltext = "31760-3.htm";
            }
         } else if (event.equals("traveller_give_coupon")) {
            if (level >= 6 && level <= 25 && pkkills == 0 && occupation_level == 1) {
               if ((newbie | 32) != newbie) {
                  player.setNewbie(newbie | 32);
                  st.giveItems(7833, 1L);
                  htmltext = "31760-5.htm";
               } else {
                  htmltext = "31760-4.htm";
               }
            } else {
               htmltext = "31760-6.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("MissQueen");
      if (st == null) {
         st = this.newQuestState(player);
      }

      return "31760.htm";
   }

   public static void main(String[] args) {
      new MissQueen(-1, "MissQueen", "custom");
   }
}
