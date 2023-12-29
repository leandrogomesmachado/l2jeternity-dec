package l2e.scripts.ai.fantasy_isle;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.scripts.ai.AbstractNpcAI;

public class MC_Show extends AbstractNpcAI {
   private static int MC = 32433;
   private static int[] singers = new int[]{32431, 32432};
   private static int[] CIRCUS = new int[]{32442, 32443, 32444, 32445, 32446};
   private static int[] INDIVIDUALS = new int[]{32439, 32440, 32441};
   private static int[] SHOWSTUFF = new int[]{32424, 32425, 32426, 32427, 32428};
   private static boolean IS_STARTED = false;
   private static NpcStringId[] MESSAGES = new NpcStringId[]{
      NpcStringId.HOW_COME_PEOPLE_ARE_NOT_HERE_WE_ARE_ABOUT_TO_START_THE_SHOW_HMM,
      NpcStringId.UGH_I_HAVE_BUTTERFLIES_IN_MY_STOMACH_THE_SHOW_STARTS_SOON,
      NpcStringId.THANK_YOU_ALL_FOR_COMING_HERE_TONIGHT,
      NpcStringId.IT_IS_AN_HONOR_TO_HAVE_THE_SPECIAL_SHOW_TODAY,
      NpcStringId.FANTASY_ISLE_IS_FULLY_COMMITTED_TO_YOUR_HAPPINESS,
      NpcStringId.NOW_ID_LIKE_TO_INTRODUCE_THE_MOST_BEAUTIFUL_SINGER_IN_ADEN_PLEASE_WELCOMELEYLA_MIRA,
      NpcStringId.HERE_SHE_COMES,
      NpcStringId.THANK_YOU_VERY_MUCH_LEYLA,
      NpcStringId.JUST_BACK_FROM_THEIR_WORLD_TOUR_PUT_YOUR_HANDS_TOGETHER_FOR_THE_FANTASY_ISLE_CIRCUS,
      NpcStringId.COME_ON_EVERYONE,
      NpcStringId.DID_YOU_LIKE_IT_THAT_WAS_SO_AMAZING,
      NpcStringId.NOW_WE_ALSO_INVITED_INDIVIDUALS_WITH_SPECIAL_TALENTS,
      NpcStringId.LETS_WELCOME_THE_FIRST_PERSON_HERE,
      NpcStringId.OH,
      NpcStringId.OKAY_NOW_HERE_COMES_THE_NEXT_PERSON_COME_ON_UP_PLEASE,
      NpcStringId.OH_IT_LOOKS_LIKE_SOMETHING_GREAT_IS_GOING_TO_HAPPEN_RIGHT,
      NpcStringId.OH_MY,
      NpcStringId.THATS_G_GREAT_NOW_HERE_COMES_THE_LAST_PERSON,
      NpcStringId.NOW_THIS_IS_THE_END_OF_TODAYS_SHOW,
      NpcStringId.HOW_WAS_IT_I_HOPE_YOU_ALL_ENJOYED_IT,
      NpcStringId.PLEASE_REMEMBER_THAT_FANTASY_ISLE_IS_ALWAYS_PLANNING_A_LOT_OF_GREAT_SHOWS_FOR_YOU,
      NpcStringId.WELL_I_WISH_I_COULD_CONTINUE_ALL_NIGHT_LONG_BUT_THIS_IS_IT_FOR_TODAY_THANK_YOU,
      NpcStringId.WE_LOVE_YOU
   };
   private static Map<String, MC_Show.ShoutInfo> talks = new HashMap<>();
   private static Map<String, MC_Show.WalkInfo> walks = new HashMap<>();

   private void load() {
      talks.put("1", new MC_Show.ShoutInfo(MESSAGES[1], "2", 1000));
      talks.put("2", new MC_Show.ShoutInfo(MESSAGES[2], "3", 6000));
      talks.put("3", new MC_Show.ShoutInfo(MESSAGES[3], "4", 4000));
      talks.put("4", new MC_Show.ShoutInfo(MESSAGES[4], "5", 5000));
      talks.put("5", new MC_Show.ShoutInfo(MESSAGES[5], "6", 3000));
      talks.put("8", new MC_Show.ShoutInfo(MESSAGES[8], "9", 5000));
      talks.put("9", new MC_Show.ShoutInfo(MESSAGES[9], "10", 5000));
      talks.put("12", new MC_Show.ShoutInfo(MESSAGES[11], "13", 5000));
      talks.put("13", new MC_Show.ShoutInfo(MESSAGES[12], "14", 5000));
      talks.put("15", new MC_Show.ShoutInfo(MESSAGES[13], "16", 5000));
      talks.put("16", new MC_Show.ShoutInfo(MESSAGES[14], "17", 5000));
      talks.put("18", new MC_Show.ShoutInfo(MESSAGES[16], "19", 5000));
      talks.put("19", new MC_Show.ShoutInfo(MESSAGES[17], "20", 5000));
      talks.put("21", new MC_Show.ShoutInfo(MESSAGES[18], "22", 5000));
      talks.put("22", new MC_Show.ShoutInfo(MESSAGES[19], "23", 400));
      talks.put("25", new MC_Show.ShoutInfo(MESSAGES[20], "26", 5000));
      talks.put("26", new MC_Show.ShoutInfo(MESSAGES[21], "27", 5400));
      walks.put("npc1_1", new MC_Show.WalkInfo(new Location(-56546, -56384, -2008, 0), "npc1_2", 1200));
      walks.put("npc1_2", new MC_Show.WalkInfo(new Location(-56597, -56384, -2008, 0), "npc1_3", 1200));
      walks.put("npc1_3", new MC_Show.WalkInfo(new Location(-56596, -56428, -2008, 0), "npc1_4", 1200));
      walks.put("npc1_4", new MC_Show.WalkInfo(new Location(-56593, -56474, -2008, 0), "npc1_5", 1000));
      walks.put("npc1_5", new MC_Show.WalkInfo(new Location(-56542, -56474, -2008, 0), "npc1_6", 1000));
      walks.put("npc1_6", new MC_Show.WalkInfo(new Location(-56493, -56473, -2008, 0), "npc1_7", 2000));
      walks.put("npc1_7", new MC_Show.WalkInfo(new Location(-56495, -56425, -2008, 0), "npc1_1", 4000));
      walks.put("npc2_1", new MC_Show.WalkInfo(new Location(-56550, -56291, -2008, 0), "npc2_2", 1200));
      walks.put("npc2_2", new MC_Show.WalkInfo(new Location(-56601, -56293, -2008, 0), "npc2_3", 1200));
      walks.put("npc2_3", new MC_Show.WalkInfo(new Location(-56603, -56247, -2008, 0), "npc2_4", 1200));
      walks.put("npc2_4", new MC_Show.WalkInfo(new Location(-56605, -56203, -2008, 0), "npc2_5", 1000));
      walks.put("npc2_5", new MC_Show.WalkInfo(new Location(-56553, -56202, -2008, 0), "npc2_6", 1100));
      walks.put("npc2_6", new MC_Show.WalkInfo(new Location(-56504, -56200, -2008, 0), "npc2_7", 2000));
      walks.put("npc2_7", new MC_Show.WalkInfo(new Location(-56503, -56243, -2008, 0), "npc2_1", 4000));
      walks.put("npc3_1", new MC_Show.WalkInfo(new Location(-56500, -56290, -2008, 0), "npc3_2", 1200));
      walks.put("npc3_2", new MC_Show.WalkInfo(new Location(-56551, -56313, -2008, 0), "npc3_3", 1200));
      walks.put("npc3_3", new MC_Show.WalkInfo(new Location(-56601, -56293, -2008, 0), "npc3_4", 1200));
      walks.put("npc3_4", new MC_Show.WalkInfo(new Location(-56651, -56294, -2008, 0), "npc3_5", 1200));
      walks.put("npc3_5", new MC_Show.WalkInfo(new Location(-56653, -56250, -2008, 0), "npc3_6", 1200));
      walks.put("npc3_6", new MC_Show.WalkInfo(new Location(-56654, -56204, -2008, 0), "npc3_7", 1200));
      walks.put("npc3_7", new MC_Show.WalkInfo(new Location(-56605, -56203, -2008, 0), "npc3_8", 1200));
      walks.put("npc3_8", new MC_Show.WalkInfo(new Location(-56554, -56202, -2008, 0), "npc3_9", 1200));
      walks.put("npc3_9", new MC_Show.WalkInfo(new Location(-56503, -56200, -2008, 0), "npc3_10", 1200));
      walks.put("npc3_10", new MC_Show.WalkInfo(new Location(-56502, -56244, -2008, 0), "npc3_1", 900));
      walks.put("npc4_1", new MC_Show.WalkInfo(new Location(-56495, -56381, -2008, 0), "npc4_2", 1200));
      walks.put("npc4_2", new MC_Show.WalkInfo(new Location(-56548, -56383, -2008, 0), "npc4_3", 1200));
      walks.put("npc4_3", new MC_Show.WalkInfo(new Location(-56597, -56383, -2008, 0), "npc4_4", 1200));
      walks.put("npc4_4", new MC_Show.WalkInfo(new Location(-56643, -56385, -2008, 0), "npc4_5", 1200));
      walks.put("npc4_5", new MC_Show.WalkInfo(new Location(-56639, -56436, -2008, 0), "npc4_6", 1200));
      walks.put("npc4_6", new MC_Show.WalkInfo(new Location(-56639, -56473, -2008, 0), "npc4_7", 1200));
      walks.put("npc4_7", new MC_Show.WalkInfo(new Location(-56589, -56473, -2008, 0), "npc4_8", 1200));
      walks.put("npc4_8", new MC_Show.WalkInfo(new Location(-56541, -56473, -2008, 0), "npc4_9", 1200));
      walks.put("npc4_9", new MC_Show.WalkInfo(new Location(-56496, -56473, -2008, 0), "npc4_10", 1200));
      walks.put("npc4_10", new MC_Show.WalkInfo(new Location(-56496, -56429, -2008, 0), "npc4_1", 900));
      walks.put("npc5_1", new MC_Show.WalkInfo(new Location(-56549, -56335, -2008, 0), "npc5_2", 1000));
      walks.put("npc5_2", new MC_Show.WalkInfo(new Location(-56599, -56337, -2008, 0), "npc5_3", 2000));
      walks.put("npc5_3", new MC_Show.WalkInfo(new Location(-56649, -56341, -2008, 0), "npc5_4", 26000));
      walks.put("npc5_4", new MC_Show.WalkInfo(new Location(-56600, -56341, -2008, 0), "npc5_5", 1000));
      walks.put("npc5_5", new MC_Show.WalkInfo(new Location(-56553, -56341, -2008, 0), "npc5_6", 1000));
      walks.put("npc5_6", new MC_Show.WalkInfo(new Location(-56508, -56331, -2008, 0), "npc5_2", 8000));
      walks.put("npc6_1", new MC_Show.WalkInfo(new Location(-56595, -56428, -2008, 0), "npc6_2", 1000));
      walks.put("npc6_2", new MC_Show.WalkInfo(new Location(-56596, -56383, -2008, 0), "npc6_3", 1000));
      walks.put("npc6_3", new MC_Show.WalkInfo(new Location(-56648, -56384, -2008, 0), "npc6_4", 1000));
      walks.put("npc6_4", new MC_Show.WalkInfo(new Location(-56645, -56429, -2008, 0), "npc6_5", 1000));
      walks.put("npc6_5", new MC_Show.WalkInfo(new Location(-56644, -56475, -2008, 0), "npc6_6", 1000));
      walks.put("npc6_6", new MC_Show.WalkInfo(new Location(-56595, -56473, -2008, 0), "npc6_7", 1000));
      walks.put("npc6_7", new MC_Show.WalkInfo(new Location(-56542, -56473, -2008, 0), "npc6_8", 1000));
      walks.put("npc6_8", new MC_Show.WalkInfo(new Location(-56492, -56472, -2008, 0), "npc6_9", 1200));
      walks.put("npc6_9", new MC_Show.WalkInfo(new Location(-56495, -56426, -2008, 0), "npc6_10", 2000));
      walks.put("npc6_10", new MC_Show.WalkInfo(new Location(-56540, -56426, -2008, 0), "npc6_1", 3000));
      walks.put("npc7_1", new MC_Show.WalkInfo(new Location(-56603, -56249, -2008, 0), "npc7_2", 1000));
      walks.put("npc7_2", new MC_Show.WalkInfo(new Location(-56601, -56294, -2008, 0), "npc7_3", 1000));
      walks.put("npc7_3", new MC_Show.WalkInfo(new Location(-56651, -56295, -2008, 0), "npc7_4", 1000));
      walks.put("npc7_4", new MC_Show.WalkInfo(new Location(-56653, -56248, -2008, 0), "npc7_5", 1000));
      walks.put("npc7_5", new MC_Show.WalkInfo(new Location(-56605, -56203, -2008, 0), "npc7_6", 1000));
      walks.put("npc7_6", new MC_Show.WalkInfo(new Location(-56554, -56202, -2008, 0), "npc7_7", 1000));
      walks.put("npc7_7", new MC_Show.WalkInfo(new Location(-56504, -56201, -2008, 0), "npc7_8", 1000));
      walks.put("npc7_8", new MC_Show.WalkInfo(new Location(-56502, -56247, -2008, 0), "npc7_9", 1200));
      walks.put("npc7_9", new MC_Show.WalkInfo(new Location(-56549, -56248, -2008, 0), "npc7_10", 2000));
      walks.put("npc7_10", new MC_Show.WalkInfo(new Location(-56549, -56248, -2008, 0), "npc7_1", 3000));
      walks.put("npc8_1", new MC_Show.WalkInfo(new Location(-56493, -56426, -2008, 0), "npc8_2", 1000));
      walks.put("npc8_2", new MC_Show.WalkInfo(new Location(-56497, -56381, -2008, 0), "npc8_3", 1200));
      walks.put("npc8_3", new MC_Show.WalkInfo(new Location(-56544, -56381, -2008, 0), "npc8_4", 1200));
      walks.put("npc8_4", new MC_Show.WalkInfo(new Location(-56596, -56383, -2008, 0), "npc8_5", 1200));
      walks.put("npc8_5", new MC_Show.WalkInfo(new Location(-56594, -56428, -2008, 0), "npc8_6", 900));
      walks.put("npc8_6", new MC_Show.WalkInfo(new Location(-56645, -56429, -2008, 0), "npc8_7", 1200));
      walks.put("npc8_7", new MC_Show.WalkInfo(new Location(-56647, -56384, -2008, 0), "npc8_8", 1200));
      walks.put("npc8_8", new MC_Show.WalkInfo(new Location(-56649, -56362, -2008, 0), "npc8_9", 9200));
      walks.put("npc8_9", new MC_Show.WalkInfo(new Location(-56654, -56429, -2008, 0), "npc8_10", 1200));
      walks.put("npc8_10", new MC_Show.WalkInfo(new Location(-56644, -56474, -2008, 0), "npc8_11", 900));
      walks.put("npc8_11", new MC_Show.WalkInfo(new Location(-56593, -56473, -2008, 0), "npc8_12", 1100));
      walks.put("npc8_12", new MC_Show.WalkInfo(new Location(-56543, -56472, -2008, 0), "npc8_13", 1200));
      walks.put("npc8_13", new MC_Show.WalkInfo(new Location(-56491, -56471, -2008, 0), "npc8_1", 1200));
      walks.put("npc9_1", new MC_Show.WalkInfo(new Location(-56505, -56246, -2008, 0), "npc9_2", 1000));
      walks.put("npc9_2", new MC_Show.WalkInfo(new Location(-56504, -56291, -2008, 0), "npc9_3", 1200));
      walks.put("npc9_3", new MC_Show.WalkInfo(new Location(-56550, -56291, -2008, 0), "npc9_4", 1200));
      walks.put("npc9_4", new MC_Show.WalkInfo(new Location(-56600, -56292, -2008, 0), "npc9_5", 1200));
      walks.put("npc9_5", new MC_Show.WalkInfo(new Location(-56603, -56248, -2008, 0), "npc9_6", 900));
      walks.put("npc9_6", new MC_Show.WalkInfo(new Location(-56653, -56249, -2008, 0), "npc9_7", 1200));
      walks.put("npc9_7", new MC_Show.WalkInfo(new Location(-56651, -56294, -2008, 0), "npc9_8", 1200));
      walks.put("npc9_8", new MC_Show.WalkInfo(new Location(-56650, -56316, -2008, 0), "npc9_9", 9200));
      walks.put("npc9_9", new MC_Show.WalkInfo(new Location(-56660, -56250, -2008, 0), "npc9_10", 1200));
      walks.put("npc9_10", new MC_Show.WalkInfo(new Location(-56656, -56205, -2008, 0), "npc9_11", 900));
      walks.put("npc9_11", new MC_Show.WalkInfo(new Location(-56606, -56204, -2008, 0), "npc9_12", 1100));
      walks.put("npc9_12", new MC_Show.WalkInfo(new Location(-56554, -56203, -2008, 0), "npc9_13", 1200));
      walks.put("npc9_13", new MC_Show.WalkInfo(new Location(-56506, -56203, -2008, 0), "npc9_1", 1200));
      walks.put("24", new MC_Show.WalkInfo(new Location(-56730, -56340, -2008, 0), "25", 1800));
      walks.put("27", new MC_Show.WalkInfo(new Location(-56702, -56340, -2008, 0), "29", 1800));
   }

   private MC_Show(String name, String descr) {
      super(name, descr);
      this.addSpawnId(new int[]{32433, 32431, 32432, 32442, 32443, 32444, 32445, 32446, 32424, 32425, 32426, 32427, 32428});
      this.load();
      this.scheduleTimer();
   }

   private int getGameTime() {
      return GameTimeController.getInstance().getGameTime();
   }

   private void scheduleTimer() {
      int gameTime = this.getGameTime();
      int hours = gameTime / 60 % 24;
      int minutes = gameTime % 60;
      int hourDiff = 20 - hours;
      if (hourDiff < 0) {
         int var9;
         hourDiff = 24 - (var9 = hourDiff * -1);
      }

      int minDiff = 30 - minutes;
      if (minDiff < 0) {
         int var11;
         minDiff = 60 - (var11 = minDiff * -1);
      }

      hourDiff *= 3600000;
      minDiff *= 60000;
      long diff = (long)(hourDiff + minDiff);
      if (Config.DEBUG) {
         SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
         this._log
            .info(
               "Fantasy Isle: MC show script starting at "
                  + format.format(Long.valueOf(System.currentTimeMillis() + diff))
                  + " and is scheduled each next 4 hours."
            );
      }

      ThreadPoolManager.getInstance().scheduleAtFixedRate(new StartMCShow(), diff, 14400000L);
   }

   private void autoChat(Npc npc, NpcStringId npcString, int type) {
      npc.broadcastPacket(new NpcSay(npc.getObjectId(), type, npc.getId(), npcString), 2000);
   }

   @Override
   public String onSpawn(Npc npc) {
      if (IS_STARTED) {
         switch(npc.getId()) {
            case 32424:
            case 32425:
            case 32426:
            case 32427:
            case 32428:
               this.startQuestTimer("social1", 5500L, npc, null);
               this.startQuestTimer("social1", 12500L, npc, null);
               this.startQuestTimer("28", 19700L, npc, null);
            case 32429:
            case 32430:
            case 32434:
            case 32435:
            case 32436:
            case 32437:
            case 32438:
            case 32439:
            case 32440:
            case 32441:
            default:
               break;
            case 32431:
               npc.getAI().setIntention(CtrlIntention.MOVING, new Location(-56657, -56338, -2008, 33102));
               this.startQuestTimer("social1", 6000L, npc, null, true);
               this.startQuestTimer("7", 215000L, npc, null);
               break;
            case 32432:
               this.startQuestTimer("social1", 6000L, npc, null, true);
               this.startQuestTimer("7", 215000L, npc, null);
               break;
            case 32433:
               this.autoChat(npc, MESSAGES[0], 23);
               this.startQuestTimer("1", 30000L, npc, null);
               break;
            case 32442:
            case 32443:
            case 32444:
            case 32445:
            case 32446:
               this.startQuestTimer("11", 100000L, npc, null);
         }
      }

      return super.onSpawn(npc);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event != null && !event.isEmpty()) {
         if (event.equalsIgnoreCase("Start")) {
            IS_STARTED = true;
            addSpawn(MC, -56698, -56430, -2008, 32768, false, 0L);
         } else if (npc != null && IS_STARTED) {
            if (event.equalsIgnoreCase("6")) {
               this.autoChat(npc, MESSAGES[6], 23);
               npc.getAI().setIntention(CtrlIntention.MOVING, new Location(-56511, -56647, -2008, 36863));
               npc.broadcastPacket(new PlaySound(1, "NS22_F", 0, 0, 0, 0, 0));
               addSpawn(singers[0], -56344, -56328, -2008, 32768, false, 224000L);
               addSpawn(singers[1], -56552, -56245, -2008, 36863, false, 224000L);
               addSpawn(singers[1], -56546, -56426, -2008, 28672, false, 224000L);
               addSpawn(singers[1], -56570, -56473, -2008, 28672, false, 224000L);
               addSpawn(singers[1], -56594, -56516, -2008, 28672, false, 224000L);
               addSpawn(singers[1], -56580, -56203, -2008, 36863, false, 224000L);
               addSpawn(singers[1], -56606, -56157, -2008, 36863, false, 224000L);
               this.startQuestTimer("7", 215000L, npc, null);
            } else if (event.equalsIgnoreCase("7")) {
               switch(npc.getId()) {
                  case 32433:
                     this.autoChat(npc, MESSAGES[7], 23);
                     npc.getAI().setIntention(CtrlIntention.MOVING, new Location(-56698, -56430, -2008, 32768));
                     this.startQuestTimer("8", 12000L, npc, null);
                     break;
                  default:
                     this.cancelQuestTimer("social1", npc, null);
                     npc.getAI().setIntention(CtrlIntention.MOVING, new Location(-56594, -56064, -2008, 32768));
               }
            } else if (event.equalsIgnoreCase("10")) {
               npc.getAI().setIntention(CtrlIntention.MOVING, new Location(-56483, -56665, -2034, 32768));
               npc.broadcastPacket(new PlaySound(1, "TP05_F", 0, 0, 0, 0, 0));
               this.startQuestTimer("npc1_1", 3000L, addSpawn(CIRCUS[0], -56495, -56375, -2008, 32768, false, 101000L), null);
               this.startQuestTimer("npc2_1", 3000L, addSpawn(CIRCUS[0], -56491, -56289, -2008, 32768, false, 101000L), null);
               this.startQuestTimer("npc3_1", 3000L, addSpawn(CIRCUS[1], -56502, -56246, -2008, 32768, false, 101000L), null);
               this.startQuestTimer("npc4_1", 3000L, addSpawn(CIRCUS[1], -56496, -56429, -2008, 32768, false, 101000L), null);
               this.startQuestTimer("npc5_1", 3500L, addSpawn(CIRCUS[2], -56505, -56334, -2008, 32768, false, 101000L), null);
               this.startQuestTimer("npc6_1", 4000L, addSpawn(CIRCUS[3], -56545, -56427, -2008, 32768, false, 101000L), null);
               this.startQuestTimer("npc7_1", 4000L, addSpawn(CIRCUS[3], -56552, -56248, -2008, 32768, false, 101000L), null);
               this.startQuestTimer("npc8_1", 3000L, addSpawn(CIRCUS[4], -56493, -56473, -2008, 32768, false, 101000L), null);
               this.startQuestTimer("npc9_1", 3000L, addSpawn(CIRCUS[4], -56504, -56201, -2008, 32768, false, 101000L), null);
               this.startQuestTimer("11", 100000L, npc, null);
            } else if (event.equalsIgnoreCase("11")) {
               switch(npc.getId()) {
                  case 32433:
                     this.autoChat(npc, MESSAGES[10], 23);
                     npc.getAI().setIntention(CtrlIntention.MOVING, new Location(-56698, -56430, -2008, 32768));
                     this.startQuestTimer("12", 5000L, npc, null);
                     break;
                  default:
                     npc.getAI().setIntention(CtrlIntention.MOVING, new Location(-56343, -56330, -2008, 32768));
               }
            } else if (event.equalsIgnoreCase("14")) {
               this.startQuestTimer("social1", 2000L, addSpawn(INDIVIDUALS[0], -56700, -56385, -2008, 32768, false, 49000L), null);
               this.startQuestTimer("15", 7000L, npc, null);
            } else if (event.equalsIgnoreCase("17")) {
               this.autoChat(npc, MESSAGES[15], 23);
               this.startQuestTimer("social1", 2000L, addSpawn(INDIVIDUALS[1], -56700, -56340, -2008, 32768, false, 32000L), null);
               this.startQuestTimer("18", 9000L, npc, null);
            } else if (event.equalsIgnoreCase("20")) {
               this.startQuestTimer("social1", 2000L, addSpawn(INDIVIDUALS[2], -56703, -56296, -2008, 32768, false, 13000L), null);
               this.startQuestTimer("21", 8000L, npc, null);
            } else if (event.equalsIgnoreCase("23")) {
               npc.getAI().setIntention(CtrlIntention.MOVING, new Location(-56702, -56340, -2008, 32768));
               this.startQuestTimer("24", 2800L, npc, null);
               addSpawn(SHOWSTUFF[0], -56672, -56406, -2000, 32768, false, 20900L);
               addSpawn(SHOWSTUFF[1], -56648, -56368, -2000, 32768, false, 20900L);
               addSpawn(SHOWSTUFF[2], -56608, -56338, -2000, 32768, false, 20900L);
               addSpawn(SHOWSTUFF[3], -56652, -56307, -2000, 32768, false, 20900L);
               addSpawn(SHOWSTUFF[4], -56672, -56272, -2000, 32768, false, 20900L);
            } else if (event.equalsIgnoreCase("28")) {
               this.autoChat(npc, MESSAGES[22], 22);
               this.startQuestTimer("social1", 1L, npc, null);
            } else if (event.equalsIgnoreCase("29")) {
               npc.getAI().setIntention(CtrlIntention.MOVING, new Location(-56730, -56340, -2008, 32768));
               this.startQuestTimer("clean_npc", 4100L, npc, null);
               this.startQuestTimer("timer_check", 60000L, null, null, true);
            } else if (event.equalsIgnoreCase("social1")) {
               npc.broadcastSocialAction(1);
            } else if (event.equalsIgnoreCase("clean_npc")) {
               IS_STARTED = false;
               npc.deleteMe();
            } else if (talks.containsKey(event)) {
               MC_Show.ShoutInfo si = talks.get(event);
               if (si != null) {
                  this.autoChat(npc, si.getNpcStringId(), 23);
                  this.startQuestTimer(si.getNextEvent(), (long)si.getTime(), npc, null);
               }
            } else if (walks.containsKey(event)) {
               MC_Show.WalkInfo wi = walks.get(event);
               if (wi != null) {
                  npc.getAI().setIntention(CtrlIntention.MOVING, wi.getCharPos());
                  this.startQuestTimer(wi.getNextEvent(), (long)wi.getTime(), npc, null);
               }
            }
         }

         return null;
      } else {
         this._log.warning("MC_Show: Null/Empty event for npc " + npc + " and player " + player + "!");
         return null;
      }
   }

   public static void main(String[] args) {
      new MC_Show(MC_Show.class.getSimpleName(), "ai");
   }

   private class ShoutInfo {
      private final NpcStringId _npcStringId;
      private final String _nextEvent;
      private final int _time;

      public ShoutInfo(NpcStringId npcStringId, String nextEvent, int time) {
         this._npcStringId = npcStringId;
         this._nextEvent = nextEvent;
         this._time = time;
      }

      public NpcStringId getNpcStringId() {
         return this._npcStringId;
      }

      public String getNextEvent() {
         return this._nextEvent;
      }

      public int getTime() {
         return this._time;
      }
   }

   private class WalkInfo {
      private final Location _charPos;
      private final String _nextEvent;
      private final int _time;

      public WalkInfo(Location charPos, String nextEvent, int time) {
         this._charPos = charPos;
         this._nextEvent = nextEvent;
         this._time = time;
      }

      public Location getCharPos() {
         return this._charPos;
      }

      public String getNextEvent() {
         return this._nextEvent;
      }

      public int getTime() {
         return this._time;
      }
   }
}
