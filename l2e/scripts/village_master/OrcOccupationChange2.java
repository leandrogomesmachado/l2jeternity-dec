package l2e.scripts.village_master;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class OrcOccupationChange2 extends Quest {
   private static final String qn = "OrcOccupationChange2";
   private static int[] NPCS = new int[]{30513, 30681, 30704, 30865, 30913, 31288, 31326, 31977};
   private static int MARK_OF_CHALLENGER = 2627;
   private static int MARK_OF_PILGRIM = 2721;
   private static int MARK_OF_DUELIST = 2762;
   private static int MARK_OF_WARSPIRIT = 2879;
   private static int MARK_OF_GLORY = 3203;
   private static int MARK_OF_CHAMPION = 3276;
   private static int MARK_OF_LORD = 3390;
   private static int[][] CLASSES = new int[][]{
      {48, 47, 16, 17, 18, 19, MARK_OF_CHALLENGER, MARK_OF_GLORY, MARK_OF_DUELIST},
      {46, 45, 20, 21, 22, 23, MARK_OF_CHALLENGER, MARK_OF_GLORY, MARK_OF_CHAMPION},
      {51, 50, 24, 25, 26, 27, MARK_OF_PILGRIM, MARK_OF_GLORY, MARK_OF_LORD},
      {52, 50, 28, 29, 30, 31, MARK_OF_PILGRIM, MARK_OF_GLORY, MARK_OF_WARSPIRIT}
   };

   public OrcOccupationChange2(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(NPCS);
      this.addTalkId(NPCS);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("OrcOccupationChange2");
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         if (Util.isDigit(event)) {
            int i = Integer.valueOf(event);
            ClassId cid = player.getClassId();
            if (cid.getRace() == Race.Orc && cid.getId() == CLASSES[i][1]) {
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

               event = "30513-" + suffix + ".htm";
            }
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("OrcOccupationChange2");
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (player.isSubClassActive()) {
         return htmltext;
      } else {
         ClassId cid = player.getClassId();
         if (cid.getRace() == Race.Orc) {
            switch(cid) {
               case orcMonk:
                  htmltext = "30513-01.htm";
                  break;
               case orcRaider:
                  htmltext = "30513-05.htm";
                  break;
               case orcShaman:
                  htmltext = "30513-09.htm";
                  break;
               default:
                  if (cid.level() == 0) {
                     htmltext = "30513-33.htm";
                  } else if (cid.level() >= 2) {
                     htmltext = "30513-32.htm";
                  } else {
                     htmltext = "30513-34.htm";
                  }
            }
         } else {
            htmltext = "30513-34.htm";
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new OrcOccupationChange2(-1, "OrcOccupationChange2", "village_master");
   }
}
