package l2e.scripts.village_master;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class ElvenHumanFighters1 extends Quest {
   private static final String qn = "ElvenHumanFighters1";
   private static int[] NPCS = new int[]{30066, 30288, 30373, 32154};
   private static int MEDALLION_OF_WARRIOR = 1145;
   private static int SWORD_OF_RITUAL = 1161;
   private static int BEZIQUES_RECOMMENDATION = 1190;
   private static int ELVEN_KNIGHT_BROOCH = 1204;
   private static int REORIA_RECOMMENDATION = 1217;
   private static int SHADOW_WEAPON_COUPON_DGRADE = 8869;
   private static int[][] CLASSES = new int[][]{
      {19, 18, 18, 19, 20, 21, ELVEN_KNIGHT_BROOCH},
      {22, 18, 22, 23, 24, 25, REORIA_RECOMMENDATION},
      {1, 0, 26, 27, 28, 29, MEDALLION_OF_WARRIOR},
      {4, 0, 30, 31, 32, 33, SWORD_OF_RITUAL},
      {7, 0, 34, 35, 36, 37, BEZIQUES_RECOMMENDATION}
   };

   public ElvenHumanFighters1(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(NPCS);
      this.addTalkId(NPCS);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("ElvenHumanFighters1");
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
      QuestState st = player.getQuestState("ElvenHumanFighters1");
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (player.isSubClassActive()) {
         return htmltext;
      } else {
         ClassId cid = player.getClassId();
         if (cid.getRace() != Race.Elf && cid.getRace() != Race.Human) {
            htmltext = npc.getId() + "-40.htm";
         } else {
            switch(cid) {
               case elvenFighter:
                  htmltext = npc.getId() + "-01.htm";
                  break;
               case fighter:
                  htmltext = npc.getId() + "-08.htm";
                  break;
               default:
                  if (cid.level() == 1) {
                     return npc.getId() + "-38.htm";
                  }

                  if (cid.level() >= 2) {
                     return npc.getId() + "-39.htm";
                  }
            }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new ElvenHumanFighters1(-1, "ElvenHumanFighters1", "village_master");
   }
}
