package l2e.scripts.village_master;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class ElvenHumanMystics2 extends Quest {
   private static final String qn = "ElvenHumanMystics2";
   private static int[] NPCS = new int[]{30115, 30174, 30176, 30694, 30854, 31996};
   private static int MARK_OF_SCHOLAR = 2674;
   private static int MARK_OF_TRUST = 2734;
   private static int MARK_OF_MAGUS = 2840;
   private static int MARK_OF_LIFE = 3140;
   private static int MARK_OF_WITCHCRAFT = 3307;
   private static int MARK_OF_SUMMONER = 3336;
   private static int[][] CLASSES = new int[][]{
      {27, 26, 18, 19, 20, 21, MARK_OF_SCHOLAR, MARK_OF_LIFE, MARK_OF_MAGUS},
      {28, 26, 22, 23, 24, 25, MARK_OF_SCHOLAR, MARK_OF_LIFE, MARK_OF_SUMMONER},
      {12, 11, 26, 27, 28, 29, MARK_OF_SCHOLAR, MARK_OF_TRUST, MARK_OF_MAGUS},
      {13, 11, 30, 31, 32, 33, MARK_OF_SCHOLAR, MARK_OF_TRUST, MARK_OF_WITCHCRAFT},
      {14, 11, 34, 35, 36, 37, MARK_OF_SCHOLAR, MARK_OF_TRUST, MARK_OF_SUMMONER}
   };

   public ElvenHumanMystics2(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(NPCS);
      this.addTalkId(NPCS);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("ElvenHumanMystics2");
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

               event = "30115-" + suffix + ".htm";
            }
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("ElvenHumanMystics2");
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (player.isSubClassActive()) {
         return htmltext;
      } else {
         ClassId cid = player.getClassId();
         if (cid.getRace() != Race.Elf && cid.getRace() != Race.Human) {
            htmltext = "30115-40.htm";
         } else {
            switch(cid) {
               case elvenWizard:
                  htmltext = "30115-01.htm";
                  break;
               case wizard:
                  htmltext = "30115-08.htm";
                  break;
               default:
                  if (cid.level() == 0) {
                     htmltext = "30115-38.htm";
                  } else if (cid.level() >= 2) {
                     htmltext = "30115-39.htm";
                  } else {
                     htmltext = "30115-40.htm";
                  }
            }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new ElvenHumanMystics2(-1, "ElvenHumanMystics2", "village_master");
   }
}
