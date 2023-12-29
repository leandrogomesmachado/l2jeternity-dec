package l2e.scripts.village_master;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class ElvenHumanFighters2 extends Quest {
   private static final String qn = "ElvenHumanFighters2";
   private static int[] NPCS = new int[]{30109, 30187, 30689, 30849, 30900, 31965, 32094};
   private static int MARK_OF_CHALLENGER = 2627;
   private static int MARK_OF_DUTY = 2633;
   private static int MARK_OF_SEEKER = 2673;
   private static int MARK_OF_TRUST = 2734;
   private static int MARK_OF_DUELIST = 2762;
   private static int MARK_OF_SEARCHER = 2809;
   private static int MARK_OF_HEALER = 2820;
   private static int MARK_OF_LIFE = 3140;
   private static int MARK_OF_CHAMPION = 3276;
   private static int MARK_OF_SAGITTARIUS = 3293;
   private static int MARK_OF_WITCHCRAFT = 3307;
   private static int[][] CLASSES = new int[][]{
      {20, 19, 36, 37, 38, 39, MARK_OF_DUTY, MARK_OF_LIFE, MARK_OF_HEALER},
      {21, 19, 40, 41, 42, 43, MARK_OF_CHALLENGER, MARK_OF_LIFE, MARK_OF_DUELIST},
      {5, 4, 44, 45, 46, 47, MARK_OF_DUTY, MARK_OF_TRUST, MARK_OF_HEALER},
      {6, 4, 48, 49, 50, 51, MARK_OF_DUTY, MARK_OF_TRUST, MARK_OF_WITCHCRAFT},
      {8, 7, 52, 53, 54, 55, MARK_OF_SEEKER, MARK_OF_TRUST, MARK_OF_SEARCHER},
      {9, 7, 56, 57, 58, 59, MARK_OF_SEEKER, MARK_OF_TRUST, MARK_OF_SAGITTARIUS},
      {23, 22, 60, 61, 62, 63, MARK_OF_SEEKER, MARK_OF_LIFE, MARK_OF_SEARCHER},
      {24, 22, 64, 65, 66, 67, MARK_OF_SEEKER, MARK_OF_LIFE, MARK_OF_SAGITTARIUS},
      {2, 1, 68, 69, 70, 71, MARK_OF_CHALLENGER, MARK_OF_TRUST, MARK_OF_DUELIST},
      {3, 1, 72, 73, 74, 75, MARK_OF_CHALLENGER, MARK_OF_TRUST, MARK_OF_CHAMPION}
   };

   public ElvenHumanFighters2(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(NPCS);
      this.addTalkId(NPCS);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("ElvenHumanFighters2");
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         if (Util.isDigit(event)) {
            int i = Integer.valueOf(event);
            ClassId cid = player.getClassId();
            if ((cid.getRace() == Race.Elf || cid.getRace() == Race.Human) && cid.getId() == CLASSES[i][1]) {
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

               event = "30109-" + suffix + ".htm";
            }
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("ElvenHumanFighters2");
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (player.isSubClassActive()) {
         return htmltext;
      } else {
         ClassId cid = player.getClassId();
         if (cid.getRace() != Race.Elf && cid.getRace() != Race.Human) {
            htmltext = "30109-78.htm";
         } else {
            switch(cid) {
               case elvenKnight:
                  htmltext = "30109-01.htm";
                  break;
               case knight:
                  htmltext = "30109-08.htm";
                  break;
               case rogue:
                  htmltext = "30109-15.htm";
                  break;
               case elvenScout:
                  htmltext = "30109-22.htm";
                  break;
               case warrior:
                  htmltext = "30109-29.htm";
                  break;
               default:
                  if (cid.level() == 0) {
                     htmltext = "30109-76.htm";
                  } else if (cid.level() >= 2) {
                     htmltext = "30109-77.htm";
                  } else {
                     htmltext = "30109-78.htm";
                  }
            }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new ElvenHumanFighters2(-1, "ElvenHumanFighters2", "village_master");
   }
}
