package l2e.scripts.village_master;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class DarkElvenChange1 extends Quest {
   private static final String qn = "DarkElvenChange1";
   private static int[] NPCS = new int[]{30290, 30297, 30462, 32160};
   private static int GAZE_OF_ABYSS = 1244;
   private static int IRON_HEART = 1252;
   private static int JEWEL_OF_DARKNESS = 1261;
   private static int ORB_OF_ABYSS = 1270;
   private static int SHADOW_WEAPON_COUPON_DGRADE = 8869;
   private static int[][] CLASSES = new int[][]{
      {32, 31, 15, 16, 17, 18, GAZE_OF_ABYSS},
      {35, 31, 19, 20, 21, 22, IRON_HEART},
      {39, 38, 23, 24, 25, 26, JEWEL_OF_DARKNESS},
      {42, 38, 27, 28, 29, 30, ORB_OF_ABYSS}
   };

   public DarkElvenChange1(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(NPCS);
      this.addTalkId(NPCS);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("DarkElvenChange1");
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         if (Util.isDigit(event)) {
            int i = Integer.valueOf(event);
            ClassId cid = player.getClassId();
            if (cid.getRace() == Race.DarkElf && cid.getId() == CLASSES[i][1]) {
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
      QuestState st = player.getQuestState("DarkElvenChange1");
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (player.isSubClassActive()) {
         return htmltext;
      } else {
         ClassId cid = player.getClassId();
         if (cid.getRace() == Race.DarkElf) {
            switch(cid) {
               case darkFighter:
                  htmltext = npc.getId() + "-01.htm";
                  break;
               case darkMage:
                  htmltext = npc.getId() + "-08.htm";
                  break;
               default:
                  if (cid.level() == 1) {
                     return npc.getId() + "-32.htm";
                  }

                  if (cid.level() >= 2) {
                     return npc.getId() + "-31.htm";
                  }
            }
         } else {
            htmltext = npc.getId() + "-33.htm";
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new DarkElvenChange1(-1, "DarkElvenChange1", "village_master");
   }
}
