package l2e.scripts.custom;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class PcBangPoint extends Quest {
   private static final String qn = "PcBangPoint";
   private static final int[] NPCs = new int[]{
      31729,
      31730,
      31731,
      31732,
      31733,
      31734,
      31735,
      31736,
      31737,
      31738,
      31775,
      31776,
      31777,
      31778,
      31779,
      31780,
      31781,
      31782,
      31783,
      31784,
      31785,
      31786,
      31787,
      31788,
      31789,
      31790,
      31791,
      31792,
      31793,
      31794,
      31795,
      31796,
      31797,
      31798,
      31799,
      31800,
      31801,
      31802,
      31803,
      31804,
      31805,
      31806,
      31807,
      31808,
      31809,
      31810,
      31811,
      31812,
      31813,
      31814,
      31815,
      31816,
      31817,
      31818,
      31819,
      31820,
      31821,
      31822,
      31823,
      31824,
      31825,
      31826,
      31827,
      31828,
      31829,
      31830,
      31831,
      31832,
      31833,
      31834,
      31835,
      31836,
      31837,
      31838,
      31839,
      31840,
      31841,
      31991,
      31992,
      31993,
      31994,
      31995,
      32337,
      32338,
      32339,
      32340
   };
   private static final Map<String, int[]> PETSKILL = new HashMap<>();
   private static final Map<String, int[]> POINTSSKILL = new HashMap<>();
   private static final Map<String, int[]> TELEPORTERS = new HashMap<>();

   public PcBangPoint(int id, String name, String descr) {
      super(id, name, descr);

      for(int i : NPCs) {
         this.addStartNpc(i);
         this.addTalkId(i);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("PcBangPoint");
      if (st == null) {
         return event;
      } else {
         if (POINTSSKILL.containsKey(event)) {
            if (player.getLevel() < 55) {
               htmltext = "skill_nolevel.htm";
            } else {
               if (player.getPcBangPoints() >= ((int[])POINTSSKILL.get(event))[2]) {
                  int cost = player.getPcBangPoints() - ((int[])POINTSSKILL.get(event))[2];
                  player.setPcBangPoints(cost);
                  SystemMessage smsgpc = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
                  smsgpc.addNumber(POINTSSKILL.get(event)[2]);
                  player.sendPacket(smsgpc);
                  player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), POINTSSKILL.get(event)[2], false, false, 1));
                  npc.setTarget(player);
                  npc.doCast(SkillsParser.getInstance().getInfo(POINTSSKILL.get(event)[0], POINTSSKILL.get(event)[1]));
                  return "Individual_skill_info.htm";
               }

               htmltext = "nopoint.htm";
            }
         } else if (PETSKILL.containsKey(event)) {
            if (player.hasSummon() && player.getSummon().isServitor()) {
               if (player.getPcBangPoints() >= ((int[])PETSKILL.get(event))[2]) {
                  int cost = player.getPcBangPoints() - ((int[])PETSKILL.get(event))[2];
                  player.setPcBangPoints(cost);
                  SystemMessage smsgpc = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
                  smsgpc.addNumber(PETSKILL.get(event)[2]);
                  player.sendPacket(smsgpc);
                  player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), PETSKILL.get(event)[2], false, false, 1));
                  npc.setTarget(player.getSummon());
                  npc.doCast(SkillsParser.getInstance().getInfo(PETSKILL.get(event)[0], PETSKILL.get(event)[1]));
                  return "Individual_pet_skill_info.htm";
               }

               htmltext = "nopoint.htm";
            } else {
               htmltext = "nosummon.htm";
            }
         } else if (TELEPORTERS.containsKey(event)) {
            if (player.getPcBangPoints() >= ((int[])TELEPORTERS.get(event))[3]) {
               int cost = player.getPcBangPoints() - ((int[])TELEPORTERS.get(event))[3];
               player.setPcBangPoints(cost);
               SystemMessage smsgpc = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
               smsgpc.addNumber(TELEPORTERS.get(event)[3]);
               player.sendPacket(smsgpc);
               player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), TELEPORTERS.get(event)[3], false, false, 1));
               if (BotFunctions.getInstance().isAutoTpByCoordsEnable(player)) {
                  BotFunctions.getInstance()
                     .getAutoTeleportById(
                        player, player.getLocation(), new Location(TELEPORTERS.get(event)[0], TELEPORTERS.get(event)[1], TELEPORTERS.get(event)[2]), 1000
                     );
                  return null;
               }

               player.teleToLocation(TELEPORTERS.get(event)[0], TELEPORTERS.get(event)[1], TELEPORTERS.get(event)[2], true);
               return null;
            }

            htmltext = "nopoint.htm";
         } else if (event.equalsIgnoreCase("tele")) {
            htmltext = npc.getId() + "-tele.htm";
         } else if (event.equalsIgnoreCase("wyvern")) {
            if (player.getPcBangPoints() >= 2500) {
               int cost = player.getPcBangPoints() - 2500;
               player.setPcBangPoints(cost);
               SystemMessage smsgpc = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
               smsgpc.addNumber(2500);
               player.sendPacket(smsgpc);
               player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), 2500, false, false, 1));
               player.mount(12621, 0, true);
               player.addSkill(SkillsParser.FrequentSkill.WYVERN_BREATH.getSkill());
               return null;
            }

            htmltext = "nopoint.htm";
         } else if (event.equalsIgnoreCase("warrior")) {
            if (player.getLevel() < 55) {
               htmltext = "skill_nolevel.htm";
            } else if (player.getPcBangPoints() >= 5600) {
               int cost = player.getPcBangPoints() - 5600;
               player.setPcBangPoints(cost);
               SystemMessage smsgpc = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
               smsgpc.addNumber(5600);
               player.sendPacket(smsgpc);
               player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), 5600, false, false, 1));
               npc.setTarget(player);
               npc.doCast(SkillsParser.getInstance().getInfo(4397, 2));
               npc.doCast(SkillsParser.getInstance().getInfo(4393, 3));
               npc.doCast(SkillsParser.getInstance().getInfo(4392, 3));
               npc.doCast(SkillsParser.getInstance().getInfo(4391, 2));
               npc.doCast(SkillsParser.getInstance().getInfo(4404, 3));
               npc.doCast(SkillsParser.getInstance().getInfo(4396, 2));
               npc.doCast(SkillsParser.getInstance().getInfo(4405, 3));
               npc.doCast(SkillsParser.getInstance().getInfo(4403, 3));
               npc.doCast(SkillsParser.getInstance().getInfo(4398, 3));
               npc.doCast(SkillsParser.getInstance().getInfo(4394, 4));
               npc.doCast(SkillsParser.getInstance().getInfo(4402, 2));
               npc.doCast(SkillsParser.getInstance().getInfo(4406, 3));
               npc.doCast(SkillsParser.getInstance().getInfo(4399, 3));
               htmltext = "skill_info.htm";
            } else {
               htmltext = "nopoint.htm";
            }
         } else if (event.equalsIgnoreCase("pet_warrior")) {
            if (player.hasSummon() && player.getSummon().isServitor()) {
               if (player.getPcBangPoints() >= 4000) {
                  int cost = player.getPcBangPoints() - 4000;
                  player.setPcBangPoints(cost);
                  SystemMessage smsgpc = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
                  smsgpc.addNumber(4000);
                  player.sendPacket(smsgpc);
                  player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), 4000, false, false, 1));
                  npc.setTarget(player.getSummon());
                  npc.doCast(SkillsParser.getInstance().getInfo(4397, 1));
                  npc.doCast(SkillsParser.getInstance().getInfo(4393, 2));
                  npc.doCast(SkillsParser.getInstance().getInfo(4392, 2));
                  npc.doCast(SkillsParser.getInstance().getInfo(4391, 2));
                  npc.doCast(SkillsParser.getInstance().getInfo(4404, 2));
                  npc.doCast(SkillsParser.getInstance().getInfo(4396, 1));
                  npc.doCast(SkillsParser.getInstance().getInfo(4405, 2));
                  npc.doCast(SkillsParser.getInstance().getInfo(4403, 2));
                  npc.doCast(SkillsParser.getInstance().getInfo(4398, 2));
                  npc.doCast(SkillsParser.getInstance().getInfo(4394, 3));
                  npc.doCast(SkillsParser.getInstance().getInfo(4402, 1));
                  npc.doCast(SkillsParser.getInstance().getInfo(4406, 2));
                  npc.doCast(SkillsParser.getInstance().getInfo(4399, 2));
                  htmltext = "pet_skill_info.htm";
               } else {
                  htmltext = "nopoint.htm";
               }
            } else {
               htmltext = "nosummon.htm";
            }
         } else if (event.equalsIgnoreCase("mage")) {
            if (player.getLevel() < 55) {
               htmltext = "skill_nolevel.htm";
            } else if (player.getPcBangPoints() >= 3000) {
               int cost = player.getPcBangPoints() - 3000;
               player.setPcBangPoints(cost);
               SystemMessage smsgpc = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
               smsgpc.addNumber(3000);
               player.sendPacket(smsgpc);
               player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), 3000, false, false, 1));
               npc.setTarget(player);
               npc.doCast(SkillsParser.getInstance().getInfo(4397, 2));
               npc.doCast(SkillsParser.getInstance().getInfo(4396, 2));
               npc.doCast(SkillsParser.getInstance().getInfo(4392, 2));
               npc.doCast(SkillsParser.getInstance().getInfo(4391, 2));
               npc.doCast(SkillsParser.getInstance().getInfo(4395, 4));
               npc.doCast(SkillsParser.getInstance().getInfo(4401, 3));
               npc.doCast(SkillsParser.getInstance().getInfo(4400, 3));
               htmltext = "skill_info.htm";
            } else {
               htmltext = "nopoint.htm";
            }
         } else if (event.equalsIgnoreCase("pet_mage")) {
            if (player.hasSummon() && player.getSummon().isServitor()) {
               if (player.getPcBangPoints() >= 2100) {
                  int cost = player.getPcBangPoints() - 2100;
                  player.setPcBangPoints(cost);
                  SystemMessage smsgpc = SystemMessage.getSystemMessage(SystemMessageId.USING_S1_PCPOINT);
                  smsgpc.addNumber(2100);
                  player.sendPacket(smsgpc);
                  player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), 2100, false, false, 1));
                  npc.setTarget(player.getSummon());
                  npc.doCast(SkillsParser.getInstance().getInfo(4397, 1));
                  npc.doCast(SkillsParser.getInstance().getInfo(4396, 1));
                  npc.doCast(SkillsParser.getInstance().getInfo(4392, 2));
                  npc.doCast(SkillsParser.getInstance().getInfo(4391, 2));
                  npc.doCast(SkillsParser.getInstance().getInfo(4395, 3));
                  npc.doCast(SkillsParser.getInstance().getInfo(4401, 2));
                  npc.doCast(SkillsParser.getInstance().getInfo(4400, 2));
                  htmltext = "pet_skill_info.htm";
               } else {
                  htmltext = "nopoint.htm";
               }
            } else {
               htmltext = "nosummon.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      return "info.htm";
   }

   public static void main(String[] args) {
      new PcBangPoint(-1, "PcBangPoint", "custom");
   }

   static {
      POINTSSKILL.put("S4391", new int[]{4391, 2, 300});
      POINTSSKILL.put("S4392", new int[]{4392, 3, 200});
      POINTSSKILL.put("S4393", new int[]{4393, 3, 400});
      POINTSSKILL.put("S4394", new int[]{4394, 4, 400});
      POINTSSKILL.put("S4395", new int[]{4395, 4, 400});
      POINTSSKILL.put("S4396", new int[]{4396, 2, 400});
      POINTSSKILL.put("S4397", new int[]{4397, 2, 500});
      POINTSSKILL.put("S4398", new int[]{4398, 3, 200});
      POINTSSKILL.put("S4399", new int[]{4399, 3, 400});
      POINTSSKILL.put("S4440", new int[]{4400, 3, 950});
      POINTSSKILL.put("S4401", new int[]{4401, 3, 400});
      POINTSSKILL.put("S4402", new int[]{4402, 2, 950});
      POINTSSKILL.put("S4403", new int[]{4403, 3, 400});
      POINTSSKILL.put("S4404", new int[]{4404, 3, 800});
      POINTSSKILL.put("S4405", new int[]{4405, 3, 950});
      POINTSSKILL.put("S4406", new int[]{4406, 3, 400});
      PETSKILL.put("P4391", new int[]{4391, 2, 300});
      PETSKILL.put("P4392", new int[]{4392, 2, 150});
      PETSKILL.put("P4393", new int[]{4393, 2, 300});
      PETSKILL.put("P4394", new int[]{4394, 3, 300});
      PETSKILL.put("P4395", new int[]{4395, 3, 300});
      PETSKILL.put("P4396", new int[]{4396, 1, 300});
      PETSKILL.put("P4397", new int[]{4397, 1, 300});
      PETSKILL.put("P4398", new int[]{4398, 2, 150});
      PETSKILL.put("P4399", new int[]{4399, 2, 300});
      PETSKILL.put("P4440", new int[]{4400, 2, 600});
      PETSKILL.put("P4401", new int[]{4401, 2, 300});
      PETSKILL.put("P4402", new int[]{4402, 1, 400});
      PETSKILL.put("P4403", new int[]{4403, 2, 300});
      PETSKILL.put("P4404", new int[]{4404, 2, 650});
      PETSKILL.put("P4405", new int[]{4405, 2, 800});
      PETSKILL.put("P4406", new int[]{4406, 2, 300});
      TELEPORTERS.put("TELE_01", new int[]{-112367, 234703, -3688, 30});
      TELEPORTERS.put("TELE_02", new int[]{-111728, 244330, -3448, 20});
      TELEPORTERS.put("TELE_03", new int[]{-106696, 214691, -3424, 30});
      TELEPORTERS.put("TELE_04", new int[]{-99586, 237637, -3568, 20});
      TELEPORTERS.put("TELE_05", new int[]{21362, 51122, -3688, 20});
      TELEPORTERS.put("TELE_06", new int[]{29294, 74968, -3776, 30});
      TELEPORTERS.put("TELE_07", new int[]{-10612, 75881, -3592, 50});
      TELEPORTERS.put("TELE_08", new int[]{-22224, 14168, -3232, 30});
      TELEPORTERS.put("TELE_09", new int[]{-21966, 40544, -3192, 30});
      TELEPORTERS.put("TELE_10", new int[]{-61095, 75104, -3352, 90});
      TELEPORTERS.put("TELE_11", new int[]{-10612, 75881, -3592, 50});
      TELEPORTERS.put("TELE_12", new int[]{-4190, -80040, -2696, 50});
      TELEPORTERS.put("TELE_13", new int[]{-10983, -117484, -2464, 30});
      TELEPORTERS.put("TELE_14", new int[]{9340, -112509, -2536, 40});
      TELEPORTERS.put("TELE_15", new int[]{8652, -139941, -1144});
      TELEPORTERS.put("TELE_16", new int[]{139714, -177456, -1536, 20});
      TELEPORTERS.put("TELE_17", new int[]{169008, -208272, -3504, 60});
      TELEPORTERS.put("TELE_18", new int[]{136910, -205082, -3664, 30});
      TELEPORTERS.put("TELE_19", new int[]{171946, -173352, 3440, 280});
      TELEPORTERS.put("TELE_20", new int[]{178591, -184615, -360, 300});
      TELEPORTERS.put("TELE_21", new int[]{-44763, 203497, -3592, 50});
      TELEPORTERS.put("TELE_22", new int[]{-63736, 101522, -3552, 40});
      TELEPORTERS.put("TELE_23", new int[]{-75437, 168800, -3632, 20});
      TELEPORTERS.put("TELE_24", new int[]{-53001, 191425, -3568, 50});
      TELEPORTERS.put("TELE_25", new int[]{-89763, 105359, -3576, 50});
      TELEPORTERS.put("TELE_26", new int[]{-88539, 83389, -2864, 70});
      TELEPORTERS.put("TELE_27", new int[]{-49853, 147089, -2784, 30});
      TELEPORTERS.put("TELE_28", new int[]{-16526, 208032, -3664, 90});
      TELEPORTERS.put("TELE_29", new int[]{-42256, 198333, -2800, 100});
      TELEPORTERS.put("TELE_30", new int[]{-41248, 122848, -2904, 20});
      TELEPORTERS.put("TELE_31", new int[]{-19120, 136816, -3752, 20});
      TELEPORTERS.put("TELE_32", new int[]{-9959, 176184, -4160, 60});
      TELEPORTERS.put("TELE_33", new int[]{-28327, 155125, -3496, 40});
      TELEPORTERS.put("TELE_34", new int[]{5106, 126916, -3664, 20});
      TELEPORTERS.put("TELE_35", new int[]{17225, 114173, -3440, 60});
      TELEPORTERS.put("TELE_36", new int[]{47382, 111278, -2104, 50});
      TELEPORTERS.put("TELE_37", new int[]{630, 179184, -3720, 40});
      TELEPORTERS.put("TELE_38", new int[]{34475, 188095, -2976, 80});
      TELEPORTERS.put("TELE_39", new int[]{60374, 164301, -2856, 100});
      TELEPORTERS.put("TELE_40", new int[]{50568, 152408, -2656, 40});
      TELEPORTERS.put("TELE_41", new int[]{33565, 162393, -3600, 40});
      TELEPORTERS.put("TELE_42", new int[]{26810, 172787, -3376, 20});
      TELEPORTERS.put("TELE_43", new int[]{87691, 162835, -3563, 300});
      TELEPORTERS.put("TELE_44", new int[]{82192, 226128, -3664, 150});
      TELEPORTERS.put("TELE_45", new int[]{115583, 192261, -3488, 60});
      TELEPORTERS.put("TELE_46", new int[]{84413, 234334, -3656, 60});
      TELEPORTERS.put("TELE_47", new int[]{149518, 195280, -3736, 180});
      TELEPORTERS.put("TELE_48", new int[]{73024, 118485, -3688, 50});
      TELEPORTERS.put("TELE_49", new int[]{131557, 114509, -3712, 180});
      TELEPORTERS.put("TELE_50", new int[]{43408, 206881, -3752, 150});
      TELEPORTERS.put("TELE_51", new int[]{85546, 131328, -3672, 30});
      TELEPORTERS.put("TELE_52", new int[]{76839, 63851, -3648, 20});
      TELEPORTERS.put("TELE_53", new int[]{87252, 85514, -3056, 50});
      TELEPORTERS.put("TELE_54", new int[]{91539, -12204, -2440, 130});
      TELEPORTERS.put("TELE_55", new int[]{64328, 26803, -3768, 70});
      TELEPORTERS.put("TELE_56", new int[]{124904, 61992, -3920, 40});
      TELEPORTERS.put("TELE_57", new int[]{104426, 33746, -3800, 90});
      TELEPORTERS.put("TELE_58", new int[]{142065, 81300, -3000, 50});
      TELEPORTERS.put("TELE_59", new int[]{168217, 37990, -4072, 50});
      TELEPORTERS.put("TELE_60", new int[]{184742, 19745, -3168, 80});
      TELEPORTERS.put("TELE_61", new int[]{142065, 81300, -3000, 110});
      TELEPORTERS.put("TELE_62", new int[]{155310, -16339, -3320, 170});
      TELEPORTERS.put("TELE_63", new int[]{183543, -14974, -2776, 170});
      TELEPORTERS.put("TELE_64", new int[]{106517, -2871, -3416, 150});
      TELEPORTERS.put("TELE_65", new int[]{170838, 55776, -5280, 160});
      TELEPORTERS.put("TELE_66", new int[]{114649, 11115, -5120, 110});
      TELEPORTERS.put("TELE_67", new int[]{174491, 50942, -4360, 190});
      TELEPORTERS.put("TELE_68", new int[]{125740, -40864, -3736, 110});
      TELEPORTERS.put("TELE_69", new int[]{146990, -67128, -3640, 50});
      TELEPORTERS.put("TELE_70", new int[]{144880, -113468, -2560, 240});
      TELEPORTERS.put("TELE_71", new int[]{165054, -47861, -3560, 60});
      TELEPORTERS.put("TELE_72", new int[]{106414, -87799, -2920, 250});
      TELEPORTERS.put("TELE_73", new int[]{169018, -116303, -2432, 250});
      TELEPORTERS.put("TELE_74", new int[]{53516, -82831, -2700, 120});
      TELEPORTERS.put("TELE_75", new int[]{65307, -71445, -3688, 100});
      TELEPORTERS.put("TELE_76", new int[]{52107, -54328, -3152, 30});
      TELEPORTERS.put("TELE_77", new int[]{69340, -50203, -3288, 80});
      TELEPORTERS.put("TELE_78", new int[]{106414, -87799, -2920, 350});
      TELEPORTERS.put("TELE_79", new int[]{89513, -44800, -2136, 230});
      TELEPORTERS.put("TELE_80", new int[]{11235, -24026, -3640, 160});
      TELEPORTERS.put("TELE_81", new int[]{47692, -115745, -3744, 240});
      TELEPORTERS.put("TELE_82", new int[]{111965, -154172, -1528, 40});
      TELEPORTERS.put("TELE_83", new int[]{68693, -110438, -1904, 190});
      TELEPORTERS.put("TELE_84", new int[]{91280, -117152, -3928, 60});
      TELEPORTERS.put("TELE_85", new int[]{113903, -108752, -856, 90});
      TELEPORTERS.put("TELE_86", new int[]{73024, 118485, -3688, 50});
      TELEPORTERS.put("TELE_87", new int[]{131557, 114509, -3712, 80});
      TELEPORTERS.put("TELE_88", new int[]{113553, 134813, -3540, 40});
      TELEPORTERS.put("TELE_89", new int[]{60374, 164301, -2856, 140});
      TELEPORTERS.put("TELE_90", new int[]{106517, -2871, -3416, 90});
      TELEPORTERS.put("TELE_91", new int[]{93218, 16969, -3904, 20});
      TELEPORTERS.put("TELE_92", new int[]{67097, 68815, -3648, 120});
   }
}
