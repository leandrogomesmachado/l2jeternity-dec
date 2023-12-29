package l2e.scripts.village_master;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class ElvenHumanBuffers2 extends Quest {
   private static final String qn = "ElvenHumanBuffers2";
   private static int[] NPCS = new int[]{30120, 30191, 30857, 30905, 31276, 31321, 31279, 31755, 31968, 32095, 31336};
   private static int MARK_OF_PILGRIM = 2721;
   private static int MARK_OF_TRUST = 2734;
   private static int MARK_OF_HEALER = 2820;
   private static int MARK_OF_REFORMER = 2821;
   private static int MARK_OF_LIFE = 3140;
   private static int[][] CLASSES = new int[][]{
      {30, 29, 12, 13, 14, 15, MARK_OF_PILGRIM, MARK_OF_LIFE, MARK_OF_HEALER},
      {16, 15, 16, 17, 18, 19, MARK_OF_PILGRIM, MARK_OF_TRUST, MARK_OF_HEALER},
      {17, 15, 20, 21, 22, 23, MARK_OF_PILGRIM, MARK_OF_TRUST, MARK_OF_REFORMER}
   };

   public ElvenHumanBuffers2(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(NPCS);
      this.addTalkId(NPCS);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("ElvenHumanBuffers2");
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

               event = "30120-" + suffix + ".htm";
            }
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("ElvenHumanBuffers2");
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (player.isSubClassActive()) {
         return htmltext;
      } else {
         ClassId cid = player.getClassId();
         if (cid.getRace() != Race.Elf && cid.getRace() != Race.Human) {
            htmltext = "30120-26.htm";
         } else {
            switch(cid) {
               case oracle:
                  htmltext = "30120-01.htm";
                  break;
               case cleric:
                  htmltext = "30120-05.htm";
                  break;
               default:
                  if (cid.level() == 0) {
                     htmltext = "30120-24.htm";
                  } else if (cid.level() >= 2) {
                     htmltext = "30120-25.htm";
                  } else {
                     htmltext = "30120-26.htm";
                  }
            }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new ElvenHumanBuffers2(-1, "ElvenHumanBuffers2", "village_master");
   }
}
