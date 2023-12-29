package l2e.scripts.village_master;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import org.apache.commons.lang.ArrayUtils;

public final class KamaelOccupationChange extends Quest {
   private static final String qn = "KamaelOccupationChange";
   private final String preffix = "32139";
   private static final int GWAINS_RECOMMENTADION = 9753;
   private static final int ORKURUS_RECOMMENDATION = 9760;
   private static final int STEELRAZOR_EVALUATION = 9772;
   private static final int KAMAEL_INQUISITOR_MARK = 9782;
   private static final int SOUL_BREAKER_CERTIFICATE = 9806;
   private static final int SHADOW_WEAPON_COUPON_DGRADE = 8869;
   private static final int SHADOW_WEAPON_COUPON_CGRADE = 8870;
   private static final Map<String, int[]> CLASSES = new HashMap<>();
   private static final int[] NPCS_MALE1 = new int[]{32139, 32196, 32199};
   private static final int[] NPCS_FEMALE1 = new int[]{32140, 32193, 32202};
   private static final int[] NPCS_MALE2 = new int[]{32146, 32205, 32209, 32213, 32217, 32221, 32225, 32229, 32233};
   private static final int[] NPCS_FEMALE2 = new int[]{32145, 32206, 32210, 32214, 32218, 32222, 32226, 32230, 32234};
   private static final int[] NPCS_ALL = new int[]{
      32139,
      32196,
      32199,
      32140,
      32193,
      32202,
      32146,
      32205,
      32209,
      32213,
      32217,
      32221,
      32225,
      32229,
      32233,
      32145,
      32206,
      32210,
      32214,
      32218,
      32222,
      32226,
      32230,
      32234
   };

   public KamaelOccupationChange(int questId, String name, String descr) {
      super(questId, name, descr);

      for(int id : NPCS_ALL) {
         this.addStartNpc(id);
         this.addTalkId(id);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      String suffix = "";
      QuestState st = player.getQuestState("KamaelOccupationChange");
      if (st == null) {
         return null;
      } else if (!CLASSES.containsKey(event)) {
         return event;
      } else {
         int req_class = CLASSES.get(event)[1];
         int req_race = CLASSES.get(event)[2];
         int req_level = CLASSES.get(event)[3];
         int low_ni = CLASSES.get(event)[4];
         int low_i = CLASSES.get(event)[5];
         int ok_ni = CLASSES.get(event)[6];
         int ok_i = CLASSES.get(event)[7];
         int req_item = CLASSES.get(event)[8];
         boolean item = st.hasQuestItems(req_item);
         if (player.getRace().ordinal() == req_race && player.getClassId().getId() == req_class) {
            if (player.getLevel() < req_level) {
               suffix = "" + low_i;
               if (!item) {
                  suffix = "" + low_ni;
               }
            } else if (!item) {
               suffix = "" + ok_ni;
            } else {
               suffix = "" + ok_i;
               this.changeClass(st, player, event, req_item);
            }
         }

         st.exitQuest(true);
         return "32139-" + suffix + ".htm";
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("KamaelOccupationChange");
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (player.isSubClassActive()) {
         return htmltext;
      } else {
         int race = player.getRace().ordinal();
         htmltext = "32139";
         if (race == 5) {
            ClassId classId = player.getClassId();
            int id = classId.getId();
            int npcId = npc.getId();
            if (classId.level() >= 2) {
               htmltext = htmltext + "-32.htm";
            } else if (!ArrayUtils.contains(NPCS_MALE1, npcId) && !ArrayUtils.contains(NPCS_FEMALE1, npcId)) {
               if (ArrayUtils.contains(NPCS_MALE2, npcId) || ArrayUtils.contains(NPCS_FEMALE2, npcId)) {
                  if (id == 125) {
                     htmltext = htmltext + "-09.htm";
                  } else if (id == 126) {
                     htmltext = htmltext + "-35.htm";
                  }
               }
            } else if (id == 123) {
               htmltext = htmltext + "-01.htm";
            } else if (id == 124) {
               htmltext = htmltext + "-05.htm";
            }
         } else {
            htmltext = htmltext + "-33.htm";
         }

         st.exitQuest(true);
         return htmltext;
      }
   }

   private void changeClass(QuestState st, Player player, String event, int req_item) {
      int newclass = CLASSES.get(event)[0];
      st.takeItems(req_item, 1L);
      st.giveItems(CLASSES.get(event)[9], 15L);
      st.playSound("ItemSound.quest_fanfare_2");
      player.setClassId(newclass);
      player.setBaseClass(newclass);
      player.broadcastCharInfo();
   }

   public static void main(String[] args) {
      new KamaelOccupationChange(-1, "KamaelOccupationChange", "village_master");
   }

   static {
      CLASSES.put("DR", new int[]{125, 123, 5, 20, 16, 17, 18, 19, 9753, 8869});
      CLASSES.put("WA", new int[]{126, 124, 5, 20, 20, 21, 22, 23, 9772, 8869});
      CLASSES.put("BE", new int[]{127, 125, 5, 40, 24, 25, 26, 27, 9760, 8870});
      CLASSES.put("AR", new int[]{130, 126, 5, 40, 28, 29, 30, 31, 9782, 8870});
      CLASSES.put("SBF", new int[]{129, 126, 5, 40, 40, 41, 42, 43, 9806, 8870});
      CLASSES.put("SBM", new int[]{128, 125, 5, 40, 40, 41, 42, 43, 9806, 8870});
   }
}
