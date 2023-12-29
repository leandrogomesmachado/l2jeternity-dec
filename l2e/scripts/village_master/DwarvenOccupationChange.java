package l2e.scripts.village_master;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import org.apache.commons.lang.ArrayUtils;

public class DwarvenOccupationChange extends Quest {
   private static final String qn = "DwarvenOccupationChange";
   private static final Map<String, int[]> CLASSES1 = new HashMap<>();
   private static final Map<String, int[]> CLASSES2 = new HashMap<>();
   private static final int[] BH_NPCS = new int[]{30511, 30676, 30685, 30845, 30894, 31269, 31314, 31958};
   private static final int[] WS_NPCS = new int[]{30512, 30677, 30687, 30847, 30897, 31272, 31317, 31961};
   private static final int[] SCAV_NPCS = new int[]{30503, 30594, 30498, 32092, 32093, 32158, 32171};
   private static final int[] ARTI_NPCS = new int[]{30504, 30595, 30499, 32157};
   private static final int[] BH_MARKS = new int[]{2809, 3119, 3238};
   private static final int[] WS_MARKS = new int[]{2867, 3119, 3238};
   private static final int SCAV_MARK = 1642;
   private static final int ARTI_MARK = 1635;
   private static final int SHADOW_WEAPON_COUPON_DGRADE = 8869;
   private static final int SHADOW_WEAPON_COUPON_CGRADE = 8870;
   private static final int[] UNIQUE_DIALOGS = new int[]{30594, 30595, 30498, 30499};

   public DwarvenOccupationChange(int questId, String name, String descr) {
      super(questId, name, descr);

      for(int npcId : BH_NPCS) {
         this.addStartNpc(npcId);
         this.addTalkId(npcId);
      }

      for(int npcId : WS_NPCS) {
         this.addStartNpc(npcId);
         this.addTalkId(npcId);
      }

      for(int npcId : SCAV_NPCS) {
         this.addStartNpc(npcId);
         this.addTalkId(npcId);
      }

      for(int npcId : ARTI_NPCS) {
         this.addStartNpc(npcId);
         this.addTalkId(npcId);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("DwarvenOccupationChange");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int race = player.getRace().ordinal();
         int classid = player.getClassId().getId();
         int level = player.getLevel();
         if (CLASSES1.keySet().contains(event) || CLASSES2.keySet().contains(event)) {
            Map<String, int[]> CLASSES;
            if (!event.equalsIgnoreCase("BH") && !event.equalsIgnoreCase("WS")) {
               CLASSES = CLASSES2;
            } else {
               CLASSES = CLASSES1;
            }

            String prefix = ((int[])CLASSES.get(event))[0] + "-";
            int intended_race = CLASSES.get(event)[1];
            int required_class = CLASSES.get(event)[2];
            int[] required_marks = new int[]{CLASSES.get(event)[9], CLASSES.get(event)[10], CLASSES.get(event)[11]};
            int required_level = CLASSES.get(event)[12];
            int new_class = CLASSES.get(event)[13];
            int reward = CLASSES.get(event)[14];
            if (ArrayUtils.contains(UNIQUE_DIALOGS, npcId)) {
               prefix = npcId + "-";
            }

            if (classid == required_class && race == intended_race) {
               int marks = 0;

               for(int item : required_marks) {
                  if (item != 0 && st.hasQuestItems(item)) {
                     ++marks;
                  }
               }

               int lenght = !event.equalsIgnoreCase("AR") && !event.equalsIgnoreCase("SC") ? required_marks.length : 1;
               if (level < required_level) {
                  if (marks < lenght) {
                     htmltext = prefix + "05.htm";
                  } else {
                     htmltext = prefix + "06.htm";
                  }
               } else if (marks < lenght) {
                  htmltext = prefix + "07.htm";
               } else {
                  for(int item : required_marks) {
                     if (item != 0) {
                        st.takeItems(item, 1L);
                     }
                  }

                  if (reward > 0) {
                     st.giveItems(reward, 15L);
                  }

                  player.setClassId(new_class);
                  player.setBaseClass(new_class);
                  player.broadcastCharInfo();
                  st.playSound("ItemSound.quest_fanfare_2");
                  htmltext = prefix + "08.htm";
               }
            } else {
               htmltext = getNoQuestMsg(player);
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("DwarvenOccupationChange");
      if (st == null) {
         st = this.newQuestState(player);
      }

      String key = "";
      int npcId = npc.getId();
      int race = player.getRace().ordinal();
      int classid = player.getClassId().getId();
      if (player.isSubClassActive()) {
         st.exitQuest(true);
         return htmltext;
      } else {
         if (ArrayUtils.contains(BH_NPCS, npcId)) {
            key = "BH";
         } else if (ArrayUtils.contains(WS_NPCS, npcId)) {
            key = "WS";
         } else if (ArrayUtils.contains(SCAV_NPCS, npcId)) {
            key = "SC";
         } else if (ArrayUtils.contains(ARTI_NPCS, npcId)) {
            key = "AR";
         }

         Map<String, int[]> CLASSES;
         if (!key.equalsIgnoreCase("BH") && !key.equalsIgnoreCase("WS")) {
            CLASSES = CLASSES2;
         } else {
            CLASSES = CLASSES1;
         }

         if (!key.equalsIgnoreCase("")) {
            String prefix = ((int[])CLASSES.get(key))[0] + "-";
            int intended_race = CLASSES.get(key)[1];
            int required_class = CLASSES.get(key)[2];
            int[] denial1 = new int[]{CLASSES.get(key)[3], CLASSES.get(key)[4]};
            int[] denial2 = new int[]{CLASSES.get(key)[5], CLASSES.get(key)[6], CLASSES.get(key)[7], CLASSES.get(key)[8]};
            if (ArrayUtils.contains(UNIQUE_DIALOGS, npcId)) {
               prefix = npcId + "-";
            }

            htmltext = prefix + "11.htm";
            if (race == intended_race) {
               if (classid == required_class) {
                  htmltext = prefix + "01.htm";
               } else if (ArrayUtils.contains(denial1, classid)) {
                  htmltext = prefix + "09.htm";
                  st.exitQuest(true);
               } else if (ArrayUtils.contains(denial2, classid)) {
                  htmltext = prefix + "10.htm";
                  st.exitQuest(true);
               } else {
                  st.exitQuest(true);
               }
            }
         } else {
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new DwarvenOccupationChange(-1, "DwarvenOccupationChange", "village_master");
   }

   static {
      CLASSES1.put("BH", new int[]{30511, 4, 54, 53, 0, 55, 57, 117, 118, BH_MARKS[0], BH_MARKS[1], BH_MARKS[2], 40, 55, 0});
      CLASSES1.put("WS", new int[]{30512, 4, 56, 53, 0, 55, 57, 117, 118, WS_MARKS[0], WS_MARKS[1], WS_MARKS[2], 40, 57, 8870});
      CLASSES2.put("SC", new int[]{30503, 4, 53, 54, 56, 55, 57, 117, 118, 1642, 0, 0, 20, 54, 8869});
      CLASSES2.put("AR", new int[]{30504, 4, 53, 54, 56, 55, 57, 117, 118, 1635, 0, 0, 20, 56, 8869});
   }
}
