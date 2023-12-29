package l2e.scripts.village_master;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class OrcOccupationChange1 extends Quest {
   private static final String qn = "OrcOccupationChange1";
   private static int[] NPCS = new int[]{30500, 30505, 30508, 32150};
   private static int MARK_OF_RAIDER = 1592;
   private static int KHAVATARI_TOTEM = 1615;
   private static int MASK_OF_MEDIUM = 1631;
   private static int SHADOW_WEAPON_COUPON_DGRADE = 8869;
   private static int[][] CLASSES = new int[][]{
      {45, 44, 9, 10, 11, 12, MARK_OF_RAIDER}, {47, 44, 13, 14, 15, 16, KHAVATARI_TOTEM}, {50, 49, 17, 18, 19, 20, MASK_OF_MEDIUM}
   };

   public OrcOccupationChange1(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(NPCS);
      this.addTalkId(NPCS);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("OrcOccupationChange1");
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         if (Util.isDigit(event)) {
            int i = Integer.valueOf(event);
            ClassId cid = player.getClassId();
            if (cid.getRace() == Race.Orc && cid.getId() == CLASSES[i][1]) {
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
      QuestState st = player.getQuestState("OrcOccupationChange1");
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (player.isSubClassActive()) {
         return htmltext;
      } else {
         ClassId cid = player.getClassId();
         if (cid.getRace() == Race.Orc) {
            switch(cid) {
               case orcFighter:
                  htmltext = npc.getId() + "-01.htm";
                  break;
               case orcMage:
                  htmltext = npc.getId() + "-06.htm";
                  break;
               default:
                  if (cid.level() == 1) {
                     return npc.getId() + "-21.htm";
                  }

                  if (cid.level() >= 2) {
                     return npc.getId() + "-22.htm";
                  }
            }
         } else {
            htmltext = npc.getId() + "-23.htm";
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new OrcOccupationChange1(-1, "OrcOccupationChange1", "village_master");
   }
}
