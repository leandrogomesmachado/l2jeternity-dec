package l2e.scripts.village_master;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class DarkElvenChange2 extends Quest {
   private static final String qn = "DarkElvenChange2";
   private static int[] NPCS = new int[]{31328, 30195, 30699, 30474, 31324, 30862, 30910, 31285, 31331, 31334, 31974, 32096};
   private static int MARK_OF_CHALLENGER = 2627;
   private static int MARK_OF_DUTY = 2633;
   private static int MARK_OF_SEEKER = 2673;
   private static int MARK_OF_SCHOLAR = 2674;
   private static int MARK_OF_PILGRIM = 2721;
   private static int MARK_OF_DUELIST = 2762;
   private static int MARK_OF_SEARCHER = 2809;
   private static int MARK_OF_REFORMER = 2821;
   private static int MARK_OF_MAGUS = 2840;
   private static int MARK_OF_FATE = 3172;
   private static int MARK_OF_SAGITTARIUS = 3293;
   private static int MARK_OF_WITCHCRAFT = 3307;
   private static int MARK_OF_SUMMONER = 3336;
   private static int[][] CLASSES = new int[][]{
      {33, 32, 26, 27, 28, 29, MARK_OF_DUTY, MARK_OF_FATE, MARK_OF_WITCHCRAFT},
      {34, 32, 30, 31, 32, 33, MARK_OF_CHALLENGER, MARK_OF_FATE, MARK_OF_DUELIST},
      {43, 42, 34, 35, 36, 37, MARK_OF_PILGRIM, MARK_OF_FATE, MARK_OF_REFORMER},
      {36, 35, 38, 39, 40, 41, MARK_OF_SEEKER, MARK_OF_FATE, MARK_OF_SEARCHER},
      {37, 35, 42, 43, 44, 45, MARK_OF_SEEKER, MARK_OF_FATE, MARK_OF_SAGITTARIUS},
      {40, 39, 46, 47, 48, 49, MARK_OF_SCHOLAR, MARK_OF_FATE, MARK_OF_MAGUS},
      {41, 39, 50, 51, 52, 53, MARK_OF_SCHOLAR, MARK_OF_FATE, MARK_OF_SUMMONER}
   };

   public DarkElvenChange2(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(NPCS);
      this.addTalkId(NPCS);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("DarkElvenChange2");
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         if (Util.isDigit(event)) {
            int i = Integer.valueOf(event);
            ClassId cid = player.getClassId();
            if (cid.getRace() == Race.DarkElf && cid.getId() == CLASSES[i][1]) {
               boolean item1 = st.hasQuestItems(CLASSES[i][6]);
               boolean item2 = st.hasQuestItems(CLASSES[i][7]);
               boolean item3 = st.hasQuestItems(CLASSES[i][8]);
               int suffix;
               if (player.getLevel() >= 40) {
                  if (item1 && item2 && item3) {
                     suffix = CLASSES[i][5];
                     st.takeItems(CLASSES[i][6], -1L);
                     st.takeItems(CLASSES[i][7], -1L);
                     st.takeItems(CLASSES[i][8], -1L);
                     st.playSound("ItemSound.quest_fanfare_2");
                     player.setClassId(CLASSES[i][0]);
                     player.setBaseClass(CLASSES[i][0]);
                     player.broadcastCharInfo();
                     st.exitQuest(false);
                  } else {
                     suffix = CLASSES[i][4];
                  }
               } else {
                  suffix = item1 && item2 && item3 ? CLASSES[i][3] : CLASSES[i][2];
               }

               event = "30474-" + suffix + ".htm";
            }
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("DarkElvenChange2");
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (player.isSubClassActive()) {
         return htmltext;
      } else {
         ClassId cid = player.getClassId();
         if (cid.getRace() == Race.DarkElf) {
            switch(cid) {
               case palusKnight:
                  htmltext = "30474-01.htm";
                  break;
               case shillienOracle:
                  htmltext = "30474-08.htm";
                  break;
               case assassin:
                  htmltext = "30474-12.htm";
                  break;
               case darkWizard:
                  htmltext = "30474-19.htm";
                  break;
               default:
                  if (cid.level() == 0) {
                     htmltext = "30474-55.htm";
                  } else if (cid.level() >= 2) {
                     htmltext = "30474-54.htm";
                  } else {
                     htmltext = "30474-56.htm";
                  }
            }
         } else {
            htmltext = "30474-56.htm";
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new DarkElvenChange2(-1, "DarkElvenChange2", "village_master");
   }
}
