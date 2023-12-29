package l2e.scripts.village_master;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class ElvenHumanMystics1 extends Quest {
   private static final String qn = "ElvenHumanMystics1";
   private static int[] NPCS = new int[]{30070, 30289, 30037, 32153, 32147};
   private static int MARK_OF_FAITH = 1201;
   private static int ETERNITY_DIAMOND = 1230;
   private static int LEAF_OF_ORACLE = 1235;
   private static int BEAD_OF_SEASON = 1292;
   private static int SHADOW_WEAPON_COUPON_DGRADE = 8869;
   private static int[][] CLASSES = new int[][]{
      {26, 25, 15, 16, 17, 18, ETERNITY_DIAMOND},
      {29, 25, 19, 20, 21, 22, LEAF_OF_ORACLE},
      {11, 10, 23, 24, 25, 26, BEAD_OF_SEASON},
      {15, 10, 27, 28, 29, 30, MARK_OF_FAITH}
   };

   public ElvenHumanMystics1(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(NPCS);
      this.addTalkId(NPCS);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("ElvenHumanMystics1");
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         if (Util.isDigit(event)) {
            int i = Integer.valueOf(event);
            ClassId cid = player.getClassId();
            if ((cid.getRace() == Race.Elf || cid.getRace() == Race.Human) && cid.getId() == CLASSES[i][1]) {
               boolean item = st.hasQuestItems(CLASSES[i][6]);
               int suffix;
               if (player.getLevel() < 20) {
                  suffix = !item ? CLASSES[i][2] : CLASSES[i][3];
               } else if (!item) {
                  suffix = CLASSES[i][4];
               } else {
                  suffix = CLASSES[i][5];
                  st.giveItems(SHADOW_WEAPON_COUPON_DGRADE, 15L);
                  st.takeItems(CLASSES[i][6], -1L);
                  player.setClassId(CLASSES[i][0]);
                  player.setBaseClass(CLASSES[i][0]);
                  st.playSound("ItemSound.quest_fanfare_2");
                  player.broadcastCharInfo();
                  st.exitQuest(false);
               }

               event = npc.getId() + "-" + suffix + ".htm";
            }
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("ElvenHumanMystics1");
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (player.isSubClassActive()) {
         return htmltext;
      } else {
         ClassId cid = player.getClassId();
         if (cid.getRace() != Race.Elf && cid.getRace() != Race.Human) {
            htmltext = npc.getId() + "-33.htm";
         } else {
            switch(cid) {
               case elvenMage:
                  htmltext = npc.getId() + "-01.htm";
                  break;
               case mage:
                  htmltext = npc.getId() + "-08.htm";
                  break;
               default:
                  if (cid.level() == 1) {
                     return npc.getId() + "-31.htm";
                  }

                  if (cid.level() >= 2) {
                     return npc.getId() + "-32.htm";
                  }
            }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new ElvenHumanMystics1(-1, "ElvenHumanMystics1", "village_master");
   }
}
