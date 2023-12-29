package l2e.scripts.instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.instance.TrapInstance;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.FlyToLocation;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SpecialCamera;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.ValidateLocation;

public class CrystalCaverns extends AbstractReflection {
   private static final int[] CGMOBS = new int[]{22311, 22312, 22313, 22314, 22315, 22316, 22317};
   private static final int[] SPAWN = new int[]{60000, 120000, 90000, 60000, 50000, 40000};
   private static final int[][] ALARMSPAWN = new int[][]{
      {153572, 141277, -12738}, {153572, 142852, -12738}, {154358, 142075, -12738}, {152788, 142075, -12738}
   };
   private static final int[][] ordreOracle1 = new int[][]{
      {32274, 147090, 152505, -12169, 31613},
      {32275, 147090, 152575, -12169, 31613},
      {32274, 147090, 152645, -12169, 31613},
      {32274, 147090, 152715, -12169, 31613}
   };
   private static final int[][] ordreOracle2 = new int[][]{
      {32274, 149783, 152505, -12169, 31613}, {32274, 149783, 152645, -12169, 31613}, {32276, 149783, 152715, -12169, 31613}
   };
   private static final int[][] ordreOracle3 = new int[][]{{32274, 152461, 152505, -12169, 31613}, {32277, 152461, 152645, -12169, 31613}};
   private static int[][] SPAWNS = new int[][]{
      {141842, 152556, -11814, 50449},
      {141503, 153395, -11814, 40738},
      {141070, 153201, -11814, 39292},
      {141371, 152986, -11814, 35575},
      {141602, 154188, -11814, 24575},
      {141382, 154719, -11814, 37640},
      {141376, 154359, -11814, 12054},
      {140895, 154383, -11814, 37508},
      {140972, 154740, -11814, 52690},
      {141045, 154504, -11814, 50674},
      {140757, 152740, -11814, 39463},
      {140406, 152376, -11814, 16599},
      {140268, 152007, -11817, 45316},
      {139996, 151485, -11814, 47403},
      {140378, 151190, -11814, 58116},
      {140521, 150711, -11815, 55997},
      {140816, 150215, -11814, 53682},
      {141528, 149909, -11814, 22020},
      {141644, 150360, -11817, 13283},
      {142048, 150695, -11815, 5929},
      {141852, 151065, -11817, 27071},
      {142408, 151211, -11815, 2402},
      {142481, 151762, -11815, 12876},
      {141929, 152193, -11815, 27511},
      {142083, 151791, -11814, 47176},
      {141435, 150402, -11814, 41798},
      {140390, 151199, -11814, 50069},
      {140557, 151849, -11814, 45293},
      {140964, 153445, -11814, 56672},
      {142851, 154109, -11814, 24920},
      {142379, 154725, -11814, 30342},
      {142816, 154712, -11814, 33193},
      {142276, 154223, -11814, 33922},
      {142459, 154490, -11814, 33184},
      {142819, 154372, -11814, 21318},
      {141157, 154541, -11814, 27090},
      {141095, 150281, -11814, 55186}
   };
   private static int[][] FIRST_SPAWNS = new int[][]{
      {22276, 148109, 149601, -12132, 34490},
      {22276, 148017, 149529, -12132, 33689},
      {22278, 148065, 151202, -12132, 35323},
      {22278, 147966, 151117, -12132, 33234},
      {22279, 144063, 150238, -12132, 29654},
      {22279, 144300, 149118, -12135, 5520},
      {22279, 144397, 149337, -12132, 644},
      {22279, 144426, 150639, -12132, 50655},
      {22282, 145841, 151097, -12132, 31810},
      {22282, 144387, 149958, -12132, 61173},
      {22282, 145821, 149498, -12132, 31490},
      {22282, 146619, 149694, -12132, 33374},
      {22282, 146669, 149244, -12132, 31360},
      {22284, 144147, 151375, -12132, 58395},
      {22284, 144485, 151067, -12132, 64786},
      {22284, 144356, 149571, -12132, 63516},
      {22285, 144151, 150962, -12132, 664},
      {22285, 146657, 151365, -12132, 33154},
      {22285, 146623, 150857, -12132, 28034},
      {22285, 147046, 151089, -12132, 32941},
      {22285, 145704, 151255, -12132, 32523},
      {22285, 145359, 151101, -12132, 32767},
      {22285, 147785, 150817, -12132, 27423},
      {22285, 147727, 151375, -12132, 37117},
      {22285, 145428, 149494, -12132, 890},
      {22285, 145601, 149682, -12132, 32442},
      {22285, 147003, 149476, -12132, 31554},
      {22285, 147738, 149210, -12132, 20971},
      {22285, 147769, 149757, -12132, 34980}
   };
   private static int[][] EMERALD_SPAWNS = new int[][]{
      {22280, 144437, 143395, -11969, 34248},
      {22281, 149241, 143735, -12230, 24575},
      {22281, 147917, 146861, -12289, 60306},
      {22281, 144406, 147782, -12133, 14349},
      {22281, 144960, 146881, -12039, 23881},
      {22281, 144985, 147679, -12135, 27594},
      {22283, 147784, 143540, -12222, 2058},
      {22283, 149091, 143491, -12230, 24836},
      {22287, 144479, 147569, -12133, 20723},
      {22287, 145158, 146986, -12058, 21970},
      {22287, 145142, 147175, -12092, 24420},
      {22287, 145110, 147133, -12088, 22465},
      {22287, 144664, 146604, -12028, 14861},
      {22287, 144596, 146600, -12028, 14461},
      {22288, 143925, 146773, -12037, 10813},
      {22288, 144415, 147070, -12069, 8568},
      {22288, 143794, 145584, -12027, 14849},
      {22288, 143429, 146166, -12030, 4078},
      {22288, 144477, 147009, -12056, 8752},
      {22289, 142577, 145319, -12029, 5403},
      {22289, 143831, 146902, -12051, 9717},
      {22289, 143714, 146705, -12028, 10044},
      {22289, 143937, 147134, -12078, 7517},
      {22293, 143356, 145287, -12027, 8126},
      {22293, 143462, 144352, -12008, 25905},
      {22293, 143745, 142529, -11882, 17102},
      {22293, 144574, 144032, -12005, 34668},
      {22295, 143992, 142419, -11884, 19697},
      {22295, 144671, 143966, -12004, 32088},
      {22295, 144440, 143269, -11957, 34169},
      {22295, 142642, 146362, -12028, 281},
      {22295, 143865, 142707, -11881, 21326},
      {22295, 143573, 142530, -11879, 16141},
      {22295, 143148, 146039, -12031, 65014},
      {22295, 143001, 144853, -12014, 0},
      {22296, 147505, 146580, -12260, 59041},
      {22296, 149366, 146932, -12358, 39407},
      {22296, 149284, 147029, -12352, 41120},
      {22296, 149439, 143940, -12230, 23189},
      {22296, 147698, 143995, -12220, 27028},
      {22296, 141885, 144969, -12007, 2526},
      {22296, 147843, 143763, -12220, 28386},
      {22296, 144753, 143650, -11982, 35429},
      {22296, 147613, 146760, -12271, 56296}
   };
   private static int[][] ROOM1_SPAWNS = new int[][]{
      {22288, 143114, 140027, -11888, 15025},
      {22288, 142173, 140973, -11888, 55698},
      {22289, 143210, 140577, -11888, 17164},
      {22289, 142638, 140107, -11888, 6571},
      {22297, 142547, 140938, -11888, 48556},
      {22298, 142690, 140479, -11887, 7663}
   };
   private static int[][] ROOM2_SPAWNS = new int[][]{
      {22303, 146276, 141483, -11880, 34643},
      {22287, 145707, 142161, -11880, 28799},
      {22288, 146857, 142129, -11880, 33647},
      {22288, 146869, 142000, -11880, 31215},
      {22289, 146897, 140880, -11880, 19210}
   };
   private static int[][] ROOM3_SPAWNS = new int[][]{
      {22302, 145123, 143713, -12808, 65323},
      {22294, 145188, 143331, -12808, 496},
      {22294, 145181, 144104, -12808, 64415},
      {22293, 144994, 143431, -12808, 65431},
      {22293, 144976, 143915, -12808, 61461}
   };
   private static int[][] ROOM4_SPAWNS = new int[][]{
      {22304, 150563, 142240, -12108, 16454},
      {22294, 150769, 142495, -12108, 16870},
      {22281, 150783, 141995, -12108, 20033},
      {22283, 150273, 141983, -12108, 16043},
      {22294, 150276, 142492, -12108, 13540}
   };
   private static int[][] STEAM1_SPAWNS = new int[][]{
      {22305, 145260, 152387, -12165, 32767},
      {22305, 144967, 152390, -12165, 30464},
      {22305, 145610, 152586, -12165, 17107},
      {22305, 145620, 152397, -12165, 8191},
      {22418, 146081, 152847, -12165, 31396},
      {22418, 146795, 152641, -12165, 33850}
   };
   private static int[][] STEAM2_SPAWNS = new int[][]{
      {22306, 147740, 152767, -12165, 65043},
      {22306, 148215, 152828, -12165, 970},
      {22306, 147743, 152846, -12165, 64147},
      {22418, 148207, 152725, -12165, 61801},
      {22419, 149058, 152828, -12165, 64564}
   };
   private static int[][] STEAM3_SPAWNS = new int[][]{
      {22307, 150735, 152316, -12145, 31930},
      {22307, 150725, 152467, -12165, 33635},
      {22307, 151058, 152316, -12146, 65342},
      {22307, 151057, 152461, -12165, 2171}
   };
   private static int[][] STEAM4_SPAWNS = new int[][]{
      {22416, 151636, 150280, -12142, 36869},
      {22416, 149893, 150232, -12165, 64258},
      {22416, 149864, 150110, -12165, 65054},
      {22416, 151926, 150218, -12165, 31613},
      {22420, 149986, 150051, -12165, 105},
      {22420, 151970, 149997, -12165, 32170},
      {22420, 150744, 150006, -12165, 63}
   };

   public CrystalCaverns(String name, String descr) {
      super(name, descr);
      this.addStartNpc(new int[]{32279, 32281});
      this.addTalkId(new int[]{32275, 32276, 32277, 32279, 32280, 32281});
      this.addFirstTalkId(new int[]{32274, 32275, 32276, 32277, 32281, 32278, 32328, 32279});
      this.addAttackId(25534);
      this.addTrapActionId(new int[]{18378});
      this.addSpellFinishedId(new int[]{29099});
      this.addSkillSeeId(new int[]{25534, 32275, 32276, 32277, 29099});
      this.addEnterZoneId(new int[]{20105, 20106, 20107});
      this.addExitZoneId(new int[]{20105, 20106, 20107});
      this.addKillId(
         new int[]{
            18474,
            22311,
            22312,
            22313,
            22314,
            22315,
            22316,
            22317,
            22279,
            22280,
            22281,
            22282,
            22283,
            22285,
            22286,
            22287,
            22288,
            22289,
            22293,
            22294,
            22295,
            22296,
            22297,
            22305,
            22306,
            22307,
            22416,
            22418,
            22419,
            22420,
            22275,
            22277,
            22292,
            22298,
            22299,
            22301,
            22303,
            22304,
            25531,
            25532,
            25534,
            29099
         }
      );
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      if (this.enterInstance(player, npc, new CrystalCaverns.CCWorld(System.currentTimeMillis() + 5400000L), 10)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         this.runOracle((CrystalCaverns.CCWorld)world);
      }
   }

   @Override
   protected void onTeleportEnter(Player player, ReflectionTemplate template, ReflectionWorld world, boolean firstEntrance) {
      if (firstEntrance) {
         world.addAllowed(player.getObjectId());
         player.getAI().setIntention(CtrlIntention.IDLE);
         player.setReflectionId(world.getReflectionId());
         Location teleLoc = template.getTeleportCoord();
         player.teleToLocation(teleLoc, true);
         if (player.hasSummon()) {
            player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
            player.getSummon().setReflectionId(world.getReflectionId());
            player.getSummon().teleToLocation(teleLoc, true);
         }
      } else {
         player.getAI().setIntention(CtrlIntention.IDLE);
         player.setReflectionId(world.getReflectionId());
         Location teleLoc = template.getTeleportCoord();
         player.teleToLocation(teleLoc, true);
         if (player.hasSummon()) {
            player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
            player.getSummon().setReflectionId(world.getReflectionId());
            player.getSummon().teleToLocation(teleLoc, true);
         }
      }
   }

   private boolean checkOracleConditions(Player player) {
      Party party = player.getParty();
      if (party == null) {
         player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
         return false;
      } else if (party.getLeader() != player) {
         player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
         return false;
      } else {
         for(Player partyMember : party.getMembers()) {
            ItemInstance item = partyMember.getInventory().getItemByItemId(9692);
            if (item == null) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
               sm.addPcName(partyMember);
               party.broadCast(sm);
               return false;
            }

            if (!Util.checkIfInRange(1000, player, partyMember, true)) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
               sm.addPcName(partyMember);
               party.broadCast(sm);
               return false;
            }
         }

         return true;
      }
   }

   private boolean checkBaylorConditions(Player player) {
      Party party = player.getParty();
      if (party == null) {
         player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
         return false;
      } else if (party.getLeader() != player) {
         player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
         return false;
      } else {
         for(Player partyMember : party.getMembers()) {
            ItemInstance item1 = partyMember.getInventory().getItemByItemId(9695);
            ItemInstance item2 = partyMember.getInventory().getItemByItemId(9696);
            ItemInstance item3 = partyMember.getInventory().getItemByItemId(9697);
            if (item1 == null || item2 == null || item3 == null) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
               sm.addPcName(partyMember);
               party.broadCast(sm);
               return false;
            }

            if (!Util.checkIfInRange(1000, player, partyMember, true)) {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
               sm.addPcName(partyMember);
               party.broadCast(sm);
               return false;
            }
         }

         return true;
      }
   }

   private void Throw(Creature effector, Creature effected) {
      int curX = effected.getX();
      int curY = effected.getY();
      int curZ = effected.getZ();
      double dx = (double)(effector.getX() - curX);
      double dy = (double)(effector.getY() - curY);
      double dz = (double)(effector.getZ() - curZ);
      double distance = Math.sqrt(dx * dx + dy * dy);
      int offset = Math.min((int)distance + 300, 1400);
      offset = (int)((double)offset + Math.abs(dz));
      if (offset < 5) {
         offset = 5;
      }

      if (!(distance < 1.0)) {
         double sin = dy / distance;
         double cos = dx / distance;
         int _x = effector.getX() - (int)((double)offset * cos);
         int _y = effector.getY() - (int)((double)offset * sin);
         int _z = effected.getZ();
         if (Config.GEODATA) {
            Location destiny = GeoEngine.moveCheck(effected.getX(), effected.getY(), effected.getZ(), _x, _y, effected.getGeoIndex());
            _x = destiny.getX();
            _y = destiny.getY();
         }

         effected.broadcastPacket(new FlyToLocation(effected, _x, _y, _z, FlyToLocation.FlyType.THROW_UP));
         effected.setXYZ(_x, _y, _z);
         effected.broadcastPacket(new ValidateLocation(effected));
      }
   }

   protected void stopAttack(Player player) {
      player.setTarget(null);
      player.abortAttack();
      player.abortCast();
      player.breakAttack();
      player.breakCast();
      player.getAI().setIntention(CtrlIntention.IDLE);
      Summon pet = player.getSummon();
      if (pet != null) {
         pet.setTarget(null);
         pet.abortAttack();
         pet.abortCast();
         pet.breakAttack();
         pet.breakCast();
         pet.getAI().setIntention(CtrlIntention.IDLE);
      }
   }

   protected void runOracle(CrystalCaverns.CCWorld world) {
      world.setStatus(0);
      world.getReflection().openDoor(24220024);
      world.oracle.add(addSpawn(32281, 143172, 148894, -11975, 0, false, 0L, false, world.getReflectionId()));
   }

   protected void runEmerald(CrystalCaverns.CCWorld world) {
      world.setStatus(1);
      this.runFirst(world);
      world.getReflection().openDoor(24220021);
   }

   protected void runCoral(CrystalCaverns.CCWorld world) {
      world.setStatus(1);
      this.runHall(world);
      world.getReflection().openDoor(24220025);
   }

   protected void runHall(CrystalCaverns.CCWorld world) {
      world.setStatus(2);

      for(int[] spawn : SPAWNS) {
         Npc mob = addSpawn(CGMOBS[getRandom(CGMOBS.length)], spawn[0], spawn[1], spawn[2], spawn[3], false, 0L, false, world.getReflectionId());
         world.npcList1.put(mob, false);
      }
   }

   protected void runFirst(CrystalCaverns.CCWorld world) {
      world.setStatus(2);
      world.keyKeepers.add(addSpawn(22275, 148206, 149486, -12140, 32308, false, 0L, false, world.getReflectionId()));
      world.keyKeepers.add(addSpawn(22277, 148203, 151093, -12140, 31100, false, 0L, false, world.getReflectionId()));

      for(int[] spawn : FIRST_SPAWNS) {
         addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0L, false, world.getReflectionId());
      }
   }

   protected void runEmeraldSquare(CrystalCaverns.CCWorld world) {
      world.setStatus(3);
      Map<Npc, Boolean> spawnList = new HashMap<>();

      for(int[] spawn : EMERALD_SPAWNS) {
         Npc mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0L, false, world.getReflectionId());
         spawnList.put(mob, false);
      }

      world.npcList2.put(0, spawnList);
   }

   protected void runEmeraldRooms(CrystalCaverns.CCWorld world, int[][] spawnList, int room) {
      Map<Npc, Boolean> spawned = new HashMap<>();

      for(int[] spawn : spawnList) {
         Npc mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0L, false, world.getReflectionId());
         spawned.put(mob, false);
      }

      if (room == 1) {
         addSpawn(32359, 142110, 139896, -11888, 8033, false, 0L, false, world.getReflectionId());
      }

      world.npcList2.put(room, spawned);
      world.roomsStatus[room - 1] = 1;
   }

   protected void runDarnel(CrystalCaverns.CCWorld world) {
      world.setStatus(9);
      addSpawn(25531, 152759, 145949, -12588, 21592, false, 0L, false, world.getReflectionId());
      world.getReflection().openDoor(24220005);
      world.getReflection().openDoor(24220006);
   }

   protected void runSteamRooms(CrystalCaverns.CCWorld world, int[][] spawnList, int status) {
      world.setStatus(status);
      Map<Npc, Boolean> spawned = new HashMap<>();

      for(int[] spawn : spawnList) {
         Npc mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0L, false, world.getReflectionId());
         spawned.put(mob, false);
      }

      world.npcList2.put(0, spawned);
   }

   protected void runSteamOracles(CrystalCaverns.CCWorld world, int[][] oracleOrder) {
      world.oracles.clear();

      for(int[] oracle : oracleOrder) {
         world.oracles.put(addSpawn(oracle[0], oracle[1], oracle[2], oracle[3], oracle[4], false, 0L, false, world.getReflectionId()), null);
      }
   }

   protected boolean checkKillProgress(int room, Npc mob, CrystalCaverns.CCWorld world) {
      if (world.npcList2.get(room).containsKey(mob)) {
         world.npcList2.get(room).put(mob, true);
      }

      for(boolean isDead : world.npcList2.get(room).values()) {
         if (!isDead) {
            return false;
         }
      }

      return true;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      if (npc.getId() == 32281) {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
         if (tmpworld != null && tmpworld instanceof CrystalCaverns.CCWorld) {
            CrystalCaverns.CCWorld world = (CrystalCaverns.CCWorld)tmpworld;
            if (world.isStatus(0) && world.oracle.contains(npc)) {
               return "32281.htm";
            }
         }

         npc.showChatWindow(player);
         return null;
      } else {
         if (npc.getId() >= 32275 && npc.getId() <= 32277) {
            ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
            if (tmpworld != null && tmpworld instanceof CrystalCaverns.CCWorld) {
               CrystalCaverns.CCWorld world = (CrystalCaverns.CCWorld)tmpworld;
               if (!world.OracleTriggered[npc.getId() - 32275]) {
                  return "no.htm";
               }

               npc.showChatWindow(player);
               return null;
            }
         } else if (npc.getId() == 32274) {
            ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
            if (tmpworld != null && tmpworld instanceof CrystalCaverns.CCWorld) {
               return "no.htm";
            }
         } else {
            if (npc.getId() == 32279) {
               QuestState st = player.getQuestState("_131_BirdInACage");
               String htmltext = "32279.htm";
               if (st != null && !st.isCompleted()) {
                  htmltext = "32279-01.htm";
               }

               return htmltext;
            }

            if (npc.getId() == 32328) {
               player.sendActionFailed();
            }
         }

         return "";
      }
   }

   @Override
   public String onSkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon) {
      boolean doReturn = true;

      for(GameObject obj : targets) {
         if (obj == npc) {
            doReturn = false;
         }
      }

      if (doReturn) {
         return super.onSkillSee(npc, caster, skill, targets, isSummon);
      } else {
         switch(skill.getId()) {
            case 1011:
            case 1015:
            case 1217:
            case 1218:
            case 1401:
            case 2360:
            case 2369:
            case 5146:
               doReturn = false;
               break;
            default:
               doReturn = true;
         }

         if (doReturn) {
            return super.onSkillSee(npc, caster, skill, targets, isSummon);
         } else {
            if (npc.getId() >= 32275 && npc.getId() <= 32277 && skill.getId() != 2360 && skill.getId() != 2369) {
               ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
               if (tmpworld != null && tmpworld instanceof CrystalCaverns.CCWorld && getRandom(100) < 15) {
                  for(Npc oracle : ((CrystalCaverns.CCWorld)tmpworld).oracles.keySet()) {
                     if (oracle != npc) {
                        oracle.decayMe();
                     }
                  }

                  ((CrystalCaverns.CCWorld)tmpworld).OracleTriggered[npc.getId() - 32275] = true;
               }
            } else if (npc.isInvul() && npc.getId() == 29099 && skill.getId() == 2360 && caster != null) {
               if (caster.getParty() == null) {
                  return super.onSkillSee(npc, caster, skill, targets, isSummon);
               }

               ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
               if (tmpworld != null && tmpworld instanceof CrystalCaverns.CCWorld) {
                  CrystalCaverns.CCWorld world = (CrystalCaverns.CCWorld)tmpworld;
                  if (world._dragonClawStart + 3000L > System.currentTimeMillis() && world._dragonClawNeed > 0) {
                     --world._dragonClawNeed;
                  } else {
                     world._dragonClawStart = System.currentTimeMillis();
                     world._dragonClawNeed = caster.getParty().getMemberCount() - 1;
                  }

                  if (world._dragonClawNeed == 0) {
                     npc.stopSkillEffects(5225);
                     npc.broadcastPacket(new MagicSkillUse(npc, npc, 5480, 1, 4000, 0));
                     if (world._raidStatus == 3) {
                        ++world._raidStatus;
                     }
                  }
               }
            } else if (npc.isInvul() && npc.getId() == 25534 && skill.getId() == 2369 && caster != null) {
               ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
               if (tmpworld != null && tmpworld instanceof CrystalCaverns.CCWorld) {
                  CrystalCaverns.CCWorld world = (CrystalCaverns.CCWorld)tmpworld;
                  if (caster.getParty() == null) {
                     return super.onSkillSee(npc, caster, skill, targets, isSummon);
                  }

                  if (world.dragonScaleStart + 3000L > System.currentTimeMillis() && world.dragonScaleNeed > 0) {
                     --world.dragonScaleNeed;
                  } else {
                     world.dragonScaleStart = System.currentTimeMillis();
                     world.dragonScaleNeed = caster.getParty().getMemberCount() - 1;
                  }

                  if (world.dragonScaleNeed == 0 && getRandom(100) < 80) {
                     npc.setIsInvul(false);
                  }
               }
            }

            return super.onSkillSee(npc, caster, skill, targets, isSummon);
         }
      }
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill) {
      if (npc.getId() == 25534) {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
         if (tmpworld != null && tmpworld instanceof CrystalCaverns.CCWorld) {
            CrystalCaverns.CCWorld world = (CrystalCaverns.CCWorld)tmpworld;
            if (!world.isStatus(4) && attacker != null) {
               this.teleportPlayer(attacker, new Location(149361, 172327, -945), 0);
               world.removeAllowed(attacker.getObjectId());
            } else {
               if (world.tears != npc) {
                  return "";
               }

               if (!world.copys.isEmpty()) {
                  boolean notAOE = true;
                  if (skill != null
                     && (
                        skill.getTargetType() == TargetType.AREA
                           || skill.getTargetType() == TargetType.FRONT_AREA
                           || skill.getTargetType() == TargetType.BEHIND_AREA
                           || skill.getTargetType() == TargetType.AURA
                           || skill.getTargetType() == TargetType.FRONT_AURA
                           || skill.getTargetType() == TargetType.BEHIND_AURA
                     )) {
                     notAOE = false;
                  }

                  if (notAOE) {
                     for(Npc copy : world.copys) {
                        copy.onDecay();
                     }

                     world.copys.clear();
                  }

                  return "";
               }
            }

            double maxHp = npc.getMaxHp();
            double nowHp = npc.getStatus().getCurrentHp();
            int rand = getRandom(1000);
            if (nowHp < maxHp * 0.4 && rand < 5) {
               Party party = attacker.getParty();
               if (party != null) {
                  for(Player partyMember : party.getMembers()) {
                     this.stopAttack(partyMember);
                  }
               } else {
                  this.stopAttack(attacker);
               }

               Creature target = npc.getAI().getAttackTarget();

               for(int i = 0; i < 10; ++i) {
                  Npc copy = addSpawn(25535, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0L, false, attacker.getReflectionId());
                  copy.setRunning();
                  ((Attackable)copy).addDamageHate(target, 0, 99999);
                  copy.getAI().setIntention(CtrlIntention.ATTACK, target);
                  copy.setCurrentHp(nowHp);
                  world.copys.add(copy);
               }
            } else if (nowHp < maxHp * 0.15 && !world.isUsedInvulSkill && (rand > 994 || nowHp < maxHp * 0.1)) {
               world.isUsedInvulSkill = true;
               npc.setIsInvul(true);
            }
         }
      }

      return null;
   }

   @Override
   public String onSpellFinished(Npc npc, Player player, Skill skill) {
      if (npc.getId() == 29099 && skill.getId() == 5225) {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
         if (tmpworld != null && tmpworld instanceof CrystalCaverns.CCWorld) {
            ++((CrystalCaverns.CCWorld)tmpworld)._raidStatus;
         }
      }

      return super.onSpellFinished(npc, player, skill);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld != null && tmpworld instanceof CrystalCaverns.CCWorld) {
         CrystalCaverns.CCWorld world = (CrystalCaverns.CCWorld)tmpworld;
         if (event.equalsIgnoreCase("TeleportOut")) {
            this.teleportPlayer(player, new Location(149413, 173078, -5014), 0);
         } else if (event.equalsIgnoreCase("TeleportParme")) {
            this.teleportPlayer(player, new Location(153689, 142226, -9750), world.getReflectionId());
         } else if (event.equalsIgnoreCase("Timer2")
            || event.equalsIgnoreCase("Timer3")
            || event.equalsIgnoreCase("Timer4")
            || event.equalsIgnoreCase("Timer5")) {
            if (player.getReflectionId() == world.getReflectionId()) {
               this.teleportPlayer(player, new Location(144653, 152606, -12126), world.getReflectionId());
               player.stopSkillEffects(5239);
               SkillsParser.getInstance().getInfo(5239, 1).getEffects(player, player, false);
               this.startQuestTimer("Timer2", 300000L, npc, player);
            }
         } else if (event.equalsIgnoreCase("Timer21")
            || event.equalsIgnoreCase("Timer31")
            || event.equalsIgnoreCase("Timer41")
            || event.equalsIgnoreCase("Timer51")) {
            ReflectionManager.getInstance().getReflection(world.getReflectionId()).cleanupNpcs();
            world.npcList2.clear();
            this.runSteamRooms(world, STEAM1_SPAWNS, 22);
            this.startQuestTimer("Timer21", 300000L, npc, null);
         } else if (event.equalsIgnoreCase("checkKechiAttack")) {
            if (npc.isInCombat()) {
               this.startQuestTimer("spawnGuards", (long)SPAWN[0], npc, null);
               this.cancelQuestTimers("checkKechiAttack");
               world.getReflection().closeDoor(24220061);
               world.getReflection().closeDoor(24220023);
            } else {
               this.startQuestTimer("checkKechiAttack", 1000L, npc, null);
            }
         } else if (event.equalsIgnoreCase("spawnGuards")) {
            ++world.kechisHenchmanSpawn;
            world.guards.add(addSpawn(25533, 153622, 149699, -12131, 56890, false, 0L, false, world.getReflectionId()));
            world.guards.add(addSpawn(25533, 153609, 149622, -12131, 64023, false, 0L, false, world.getReflectionId()));
            world.guards.add(addSpawn(25533, 153606, 149428, -12131, 64541, false, 0L, false, world.getReflectionId()));
            world.guards.add(addSpawn(25533, 153601, 149534, -12131, 64901, false, 0L, false, world.getReflectionId()));
            world.guards.add(addSpawn(25533, 153620, 149354, -12131, 1164, false, 0L, false, world.getReflectionId()));
            world.guards.add(addSpawn(25533, 153637, 149776, -12131, 61733, false, 0L, false, world.getReflectionId()));
            world.guards.add(addSpawn(25533, 153638, 149292, -12131, 64071, false, 0L, false, world.getReflectionId()));
            world.guards.add(addSpawn(25533, 153647, 149857, -12131, 59402, false, 0L, false, world.getReflectionId()));
            world.guards.add(addSpawn(25533, 153661, 149227, -12131, 65275, false, 0L, false, world.getReflectionId()));
            if (world.kechisHenchmanSpawn <= 5) {
               this.startQuestTimer("spawnGuards", (long)SPAWN[world.kechisHenchmanSpawn], npc, null);
            } else {
               this.cancelQuestTimers("spawnGuards");
            }
         } else if (event.equalsIgnoreCase("EmeraldSteam")) {
            this.runEmerald(world);

            for(Npc oracle : world.oracle) {
               oracle.decayMe();
            }
         } else if (event.equalsIgnoreCase("CoralGarden")) {
            this.runCoral(world);

            for(Npc oracle : world.oracle) {
               oracle.decayMe();
            }
         } else if (event.equalsIgnoreCase("spawn_oracle")) {
            addSpawn(32271, 153572, 142075, -9728, 10800, false, 0L, false, world.getReflectionId());
            addSpawn(getRandom(10) < 5 ? 29116 : 29117, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0L, false, world.getReflectionId());
            addSpawn(32279, 153572, 142075, -12738, 10800, false, 0L, false, world.getReflectionId());
            this.cancelQuestTimer("baylor_despawn", npc, null);
            this.cancelQuestTimers("baylor_skill");
         } else if (event.equalsIgnoreCase("baylorEffect0")) {
            npc.getAI().setIntention(CtrlIntention.IDLE);
            npc.broadcastSocialAction(1);
            this.startQuestTimer("baylorCamera0", 11000L, npc, null);
            this.startQuestTimer("baylorEffect1", 19000L, npc, null);
         } else if (event.equalsIgnoreCase("baylorCamera0")) {
            npc.broadcastPacket(new SpecialCamera(npc, 500, -45, 170, 5000, 9000, 0, 0, 1, 0, 0));
         } else if (event.equalsIgnoreCase("baylorEffect1")) {
            npc.broadcastPacket(new SpecialCamera(npc, 300, 0, 120, 2000, 5000, 0, 0, 1, 0, 0));
            npc.broadcastSocialAction(3);
            this.startQuestTimer("baylorEffect2", 4000L, npc, null);
         } else if (event.equalsIgnoreCase("baylorEffect2")) {
            npc.broadcastPacket(new SpecialCamera(npc, 747, 0, 160, 2000, 3000, 0, 0, 1, 0, 0));
            npc.broadcastPacket(new MagicSkillUse(npc, npc, 5402, 1, 2000, 0));
            this.startQuestTimer("RaidStart", 2000L, npc, null);
         } else if (event.equalsIgnoreCase("BaylorMinions")) {
            for(int i = 0; i < 10; ++i) {
               int radius = 300;
               int x = (int)(300.0 * Math.cos((double)i * 0.618));
               int y = (int)(300.0 * Math.sin((double)i * 0.618));
               Npc mob = addSpawn(29104, 153571 + x, 142075 + y, -12737, 0, false, 0L, false, world.getReflectionId());
               mob.getAI().setIntention(CtrlIntention.IDLE);
               world._animationMobs.add(mob);
            }

            this.startQuestTimer("baylorEffect0", 200L, npc, null);
         } else if (event.equalsIgnoreCase("RaidStart")) {
            world._camera.decayMe();
            world._camera = null;
            npc.setIsParalyzed(false);

            for(Player p : world._raiders) {
               p.setIsParalyzed(false);
               this.Throw(npc, p);
               if (p.getSummon() != null) {
                  this.Throw(npc, p.getSummon());
               }
            }

            world._raidStatus = 0;

            for(Npc mob : world._animationMobs) {
               mob.doDie(mob);
            }

            world._animationMobs.clear();
            this.startQuestTimer("baylor_despawn", 60000L, npc, null, true);
            this.startQuestTimer("checkBaylorAttack", 1000L, npc, null);
         } else if (event.equalsIgnoreCase("checkBaylorAttack")) {
            if (npc.isInCombat()) {
               this.cancelQuestTimers("checkBaylorAttack");
               this.startQuestTimer("baylor_alarm", 40000L, npc, null);
               this.startQuestTimer("baylor_skill", 5000L, npc, null, true);
               ++world._raidStatus;
            } else {
               this.startQuestTimer("checkBaylorAttack", 1000L, npc, null);
            }
         } else if (event.equalsIgnoreCase("baylor_alarm")) {
            if (world._alarm == null) {
               int[] spawnLoc = ALARMSPAWN[getRandom(ALARMSPAWN.length)];
               npc.addSkill(SkillsParser.getInstance().getInfo(5244, 1));
               npc.addSkill(SkillsParser.getInstance().getInfo(5245, 1));
               world._alarm = addSpawn(18474, spawnLoc[0], spawnLoc[1], spawnLoc[2], 10800, false, 0L, false, world.getReflectionId());
               world._alarm.disableCoreAI(true);
               world._alarm.setIsImmobilized(true);
               world._alarm
                  .broadcastPacket(
                     new CreatureSay(
                        world._alarm.getObjectId(),
                        1,
                        world._alarm.getName(),
                        NpcStringId.AN_ALARM_HAS_BEEN_SET_OFF_EVERYBODY_WILL_BE_IN_DANGER_IF_THEY_ARE_NOT_TAKEN_CARE_OF_IMMEDIATELY
                     )
                  );
            }
         } else if (event.equalsIgnoreCase("baylor_skill")) {
            if (world._baylor == null) {
               this.cancelQuestTimers("baylor_skill");
            } else {
               double maxHp = npc.getMaxHp();
               double nowHp = npc.getStatus().getCurrentHp();
               int rand = getRandom(100);
               if (nowHp < maxHp * 0.2 && world._raidStatus < 3 && npc.getFirstEffect(5224) == null && npc.getFirstEffect(5225) == null) {
                  if (nowHp < maxHp * 0.15 && world._raidStatus == 2) {
                     npc.doCast(SkillsParser.getInstance().getInfo(5225, 1));
                     npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 1, npc.getName(), NpcStringId.DEMON_KING_BELETH_GIVE_ME_THE_POWER_AAAHH));
                  } else if (rand < 10 || nowHp < maxHp * 0.15) {
                     npc.doCast(SkillsParser.getInstance().getInfo(5225, 1));
                     npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 1, npc.getName(), NpcStringId.DEMON_KING_BELETH_GIVE_ME_THE_POWER_AAAHH));
                     this.startQuestTimer("baylor_remove_invul", 30000L, world._baylor, null);
                  }
               } else if (nowHp < maxHp * 0.3 && rand > 50 && npc.getFirstEffect(5225) == null && npc.getFirstEffect(5224) == null) {
                  npc.doCast(SkillsParser.getInstance().getInfo(5224, 1));
               } else if (rand < 33) {
                  npc.setTarget(world._raiders.get(getRandom(world._raiders.size())));
                  npc.doCast(SkillsParser.getInstance().getInfo(5229, 1));
               }
            }
         } else if (event.equalsIgnoreCase("baylor_remove_invul")) {
            npc.stopSkillEffects(5225);
         } else if (event.equalsIgnoreCase("Baylor")) {
            world._baylor = addSpawn(29099, 153572, 142075, -12738, 10800, false, 0L, false, world.getReflectionId());
            world._baylor.setIsParalyzed(true);
            world._camera = addSpawn(29120, 153273, 141400, -12738, 10800, false, 0L, false, world.getReflectionId());
            world._camera.broadcastPacket(new SpecialCamera(world._camera, 700, -45, 160, 500, 15200, 0, 0, 1, 0, 0));
            this.startQuestTimer("baylorMinions", 2000L, world._baylor, null);
         } else {
            if (!event.endsWith("Food")) {
               return "";
            }

            if (event.equalsIgnoreCase("autoFood")) {
               if (!world.crystalGolems.containsKey(npc)) {
                  world.crystalGolems.put(npc, new CrystalCaverns.CrystalGolem());
               }

               if (world.isStatus(3)
                  && world.crystalGolems.containsKey(npc)
                  && world.crystalGolems.get(npc).foodItem == null
                  && !world.crystalGolems.get(npc).isAtDestination) {
                  CrystalCaverns.CrystalGolem cryGolem = world.crystalGolems.get(npc);
                  List<GameObject> crystals = new ArrayList<>();

                  for(GameObject object : World.getInstance().getAroundObjects(npc, 300, 400)) {
                     if (object instanceof ItemInstance && ((ItemInstance)object).getId() == 9693) {
                        crystals.add(object);
                     }
                  }

                  int minDist = 300000;

                  for(GameObject crystal : crystals) {
                     int dx = npc.getX() - crystal.getX();
                     int dy = npc.getY() - crystal.getY();
                     int d = dx * dx + dy * dy;
                     if (d < minDist) {
                        minDist = d;
                        cryGolem.foodItem = (ItemInstance)crystal;
                     }
                  }

                  if (minDist != 300000) {
                     this.startQuestTimer("getFood", 2000L, npc, null);
                  } else {
                     if (getRandom(100) < 5) {
                        npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 1, npc.getName(), NpcStringId.AH_IM_HUNGRY));
                     }

                     this.startQuestTimer("autoFood", 2000L, npc, null);
                  }

                  return "";
               }

               return "";
            }

            if (!world.crystalGolems.containsKey(npc) || world.crystalGolems.get(npc).isAtDestination) {
               return "";
            }

            if (event.equalsIgnoreCase("backFood")) {
               if (npc.getAI().getIntention() == CtrlIntention.ACTIVE) {
                  this.cancelQuestTimers("backFood");
                  npc.getAI().setIntention(CtrlIntention.IDLE, null);
                  world.crystalGolems.get(npc).foodItem = null;
                  this.startQuestTimer("autoFood", 2000L, npc, null);
               }
            } else {
               if (event.equalsIgnoreCase("reachFood")) {
                  CrystalCaverns.CrystalGolem cryGolem = world.crystalGolems.get(npc);
                  if (cryGolem.foodItem != null && cryGolem.foodItem.isVisible()) {
                     if (npc.getAI().getIntention() == CtrlIntention.ACTIVE) {
                        cryGolem.foodItem.decayMe();
                        npc.getAI().setIntention(CtrlIntention.IDLE, null);
                        cryGolem.foodItem = null;
                        int dx = npc.getX() - 142999;
                        int dy = npc.getY() - 151671;
                        int d1 = dx * dx + dy * dy;
                        dx = npc.getX() - 139494;
                        dy = npc.getY() - 151668;
                        int d2 = dx * dx + dy * dy;
                        if (d1 >= 10000 && d2 >= 10000) {
                           this.startQuestTimer("autoFood", 2000L, npc, null);
                        } else {
                           npc.broadcastPacket(new MagicSkillUse(npc, npc, 5441, 1, 1, 0));
                           cryGolem.isAtDestination = true;
                           ++world.correctGolems;
                           if (world.correctGolems >= 2) {
                              world.getReflection().openDoor(24220026);
                              world.setStatus(4);
                           }
                        }

                        this.cancelQuestTimers("reachFood");
                     }

                     return "";
                  }

                  npc.getAI().setIntention(CtrlIntention.MOVING, cryGolem.oldpos);
                  this.cancelQuestTimers("reachFood");
                  this.startQuestTimer("backFood", 2000L, npc, null, true);
                  return "";
               }

               if (event.equalsIgnoreCase("getFood")) {
                  CrystalCaverns.CrystalGolem cryGolem = world.crystalGolems.get(npc);
                  Location newpos = new Location(cryGolem.foodItem.getX(), cryGolem.foodItem.getY(), npc.getZ(), 0);
                  cryGolem.oldpos = new Location(npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
                  npc.getAI().setIntention(CtrlIntention.MOVING, newpos);
                  this.startQuestTimer("reachFood", 2000L, npc, null, true);
                  this.cancelQuestTimers("getFood");
               }
            }
         }
      }

      return "";
   }

   private void giveRewards(Player player, int instanceId, int bossCry, boolean isBaylor) {
      int num = Math.max((int)(Config.RATE_DROP_RAIDBOSS * player.getPremiumBonus().getDropRaids()), 1);
      Party party = player.getParty();
      if (party != null) {
         for(Player partyMember : party.getMembers()) {
            if (partyMember.getReflectionId() == instanceId) {
               QuestState st = partyMember.getQuestState(this.getName());
               if (st == null) {
                  st = this.newQuestState(partyMember);
               }

               if (!isBaylor && st.hasQuestItems(9690)) {
                  st.takeItems(9690, 1L);
                  st.giveItems(bossCry, 1L);
               }

               if (getRandom(10) < 5) {
                  st.giveItems(9597, (long)num);
               } else {
                  st.giveItems(9598, (long)num);
               }
            }
         }
      } else if (player.getReflectionId() == instanceId) {
         QuestState st = player.getQuestState(this.getName());
         if (st == null) {
            st = this.newQuestState(player);
         }

         if (!isBaylor && st.hasQuestItems(9690)) {
            st.takeItems(9690, 1L);
            st.giveItems(bossCry, 1L);
         }

         if (getRandom(10) < 5) {
            st.giveItems(9597, (long)num);
         } else {
            st.giveItems(9598, (long)num);
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld != null && tmpworld instanceof CrystalCaverns.CCWorld) {
         CrystalCaverns.CCWorld world = (CrystalCaverns.CCWorld)tmpworld;
         if (world.isStatus(2) && world.npcList1.containsKey(npc)) {
            world.npcList1.put(npc, true);

            for(boolean isDead : world.npcList1.values()) {
               if (!isDead) {
                  return "";
               }
            }

            world.setStatus(3);
            world.tears = addSpawn(25534, 144298, 154420, -11854, 32767, false, 0L, false, world.getReflectionId());
            CrystalCaverns.CrystalGolem crygolem1 = new CrystalCaverns.CrystalGolem();
            CrystalCaverns.CrystalGolem crygolem2 = new CrystalCaverns.CrystalGolem();
            world.crystalGolems.put(addSpawn(32328, 140547, 151670, -11813, 32767, false, 0L, false, world.getReflectionId()), crygolem1);
            world.crystalGolems.put(addSpawn(32328, 141941, 151684, -11813, 63371, false, 0L, false, world.getReflectionId()), crygolem2);

            for(Npc crygolem : world.crystalGolems.keySet()) {
               this.startQuestTimer("autoFood", 2000L, crygolem, null);
            }
         } else if (world.isStatus(4) && npc.getId() == 25534) {
            ReflectionManager.getInstance().getReflection(world.getReflectionId()).setDuration(300000);
            addSpawn(32280, 144312, 154420, -11855, 0, false, 0L, false, world.getReflectionId());
            this.giveRewards(player, npc.getReflectionId(), 9697, false);
         } else if (world.isStatus(2) && world.keyKeepers.contains(npc)) {
            if (npc.getId() == 22275) {
               ((MonsterInstance)npc).dropItem(player, 9698, 1L);
               this.runEmeraldSquare(world);
            } else if (npc.getId() == 22277) {
               ((MonsterInstance)npc).dropItem(player, 9699, 1L);
               this.runSteamRooms(world, STEAM1_SPAWNS, 22);
               Party party = player.getParty();
               if (party != null) {
                  for(Player partyMember : party.getMembers()) {
                     if (partyMember.getReflectionId() == world.getReflectionId()) {
                        SkillsParser.getInstance().getInfo(5239, 1).getEffects(partyMember, partyMember, false);
                        this.startQuestTimer("Timer2", 300000L, npc, partyMember);
                     }
                  }
               } else {
                  SkillsParser.getInstance().getInfo(5239, 1).getEffects(player, player, false);
                  this.startQuestTimer("Timer2", 300000L, npc, player);
               }

               this.startQuestTimer("Timer21", 300000L, npc, null);
            }

            for(Npc gk : world.keyKeepers) {
               if (gk != npc) {
                  gk.decayMe();
               }
            }
         } else if (world.isStatus(3)) {
            if (!this.checkKillProgress(0, npc, world)) {
               return "";
            }

            world.setStatus(4);
            addSpawn(22292, 148202, 144791, -12235, 0, false, 0L, false, world.getReflectionId());
         } else if (world.isStatus(4)) {
            if (npc.getId() == 22292) {
               world.setStatus(5);
               addSpawn(22301, 147777, 146780, -12281, 0, false, 0L, false, world.getReflectionId());
            }
         } else if (world.isStatus(5)) {
            if (npc.getId() == 22301) {
               world.setStatus(6);
               addSpawn(22292, 143694, 142659, -11882, 0, false, 0L, false, world.getReflectionId());
            }
         } else if (world.isStatus(6)) {
            if (npc.getId() == 22292) {
               world.setStatus(7);
               addSpawn(22299, 142054, 143288, -11825, 0, false, 0L, false, world.getReflectionId());
            }
         } else if (world.isStatus(7)) {
            if (npc.getId() == 22299) {
               world.setStatus(8);
               this.addTrap(18378, 143682, 142492, -11886, 16384, null, world.getReflectionId());
            }
         } else if (world.isStatus(8)) {
            for(int i = 0; i < 4; ++i) {
               if (world.roomsStatus[i] == 1 && this.checkKillProgress(i + 1, npc, world)) {
                  world.roomsStatus[i] = 2;
               }

               if (world.roomsStatus[i] == 2) {
                  ++world.cleanedRooms;
                  if (world.cleanedRooms == 21) {
                     this.runDarnel(world);
                  }
               }
            }
         } else if (world.getStatus() >= 22 && world.getStatus() <= 25) {
            if (npc.getId() == 22416) {
               for(Npc oracle : world.oracles.keySet()) {
                  if (world.oracles.get(oracle) == npc) {
                     world.oracles.put(oracle, null);
                  }
               }
            }

            if (this.checkKillProgress(0, npc, world)) {
               world.npcList2.clear();
               int[][] oracleOrder;
               switch(world.getStatus()) {
                  case 22:
                     world.getReflection().closeDoor(24220022);
                     oracleOrder = ordreOracle1;
                     break;
                  case 23:
                     oracleOrder = ordreOracle2;
                     break;
                  case 24:
                     oracleOrder = ordreOracle3;
                     break;
                  case 25:
                     world.setStatus(26);
                     Party party = player.getParty();
                     if (party != null) {
                        for(Player partyMember : party.getMembers()) {
                           partyMember.stopSkillEffects(5239);
                        }
                     }

                     this.cancelQuestTimers("Timer5");
                     this.cancelQuestTimers("Timer51");
                     world.getReflection().openDoor(24220023);
                     world.getReflection().openDoor(24220061);
                     Npc kechi = addSpawn(25532, 154069, 149525, -12158, 51165, false, 0L, false, world.getReflectionId());
                     this.startQuestTimer("checkKechiAttack", 1000L, kechi, null);
                     return "";
                  default:
                     _log.warning("CrystalCavern-SteamCorridor: status " + world.getStatus() + " error. OracleOrder not found in " + world.getReflectionId());
                     return "";
               }

               this.runSteamOracles(world, oracleOrder);
            }
         } else if (world.isStatus(9) && npc.getId() == 25531 || world.isStatus(26) && npc.getId() == 25532) {
            ReflectionManager.getInstance().getReflection(world.getReflectionId()).setDuration(300000);
            int bossCry;
            if (npc.getId() == 25532) {
               bossCry = 9696;
               this.cancelQuestTimers("spawnGuards");
               addSpawn(32280, 154077, 149527, -12159, 0, false, 0L, false, world.getReflectionId());
            } else {
               if (npc.getId() != 25531) {
                  return "";
               }

               bossCry = 9695;
               addSpawn(32280, 152761, 145950, -12588, 0, false, 0L, false, world.getReflectionId());
            }

            this.giveRewards(player, npc.getReflectionId(), bossCry, false);
         }

         if (npc.getId() == 18474) {
            world._baylor.removeSkill(5244);
            world._baylor.removeSkill(5245);
            world._alarm = null;
            if (world._baylor.getMaxHp() * 0.3 < world._baylor.getStatus().getCurrentHp()) {
               this.startQuestTimer("baylor_alarm", 40000L, world._baylor, null);
            }
         } else if (npc.getId() == 29099) {
            world.setStatus(31);
            world._baylor = null;
            npc.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
            Reflection baylorInstance = ReflectionManager.getInstance().getReflection(npc.getReflectionId());
            baylorInstance.setDuration(300000);
            this.startQuestTimer("spawn_oracle", 1000L, npc, null);
            this.giveRewards(player, npc.getReflectionId(), -1, true);
         }
      }

      return "";
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      int npcId = npc.getId();
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (npcId == 32281) {
         this.enterInstance(player, npc);
         return "";
      } else {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
         if (tmpworld != null && tmpworld instanceof CrystalCaverns.CCWorld) {
            CrystalCaverns.CCWorld world = (CrystalCaverns.CCWorld)tmpworld;
            if (npcId != 32328) {
               if (npc.getId() >= 32275 && npc.getId() <= 32277 && world.OracleTriggered[npc.getId() - 32275]) {
                  boolean doTeleport = false;
                  Location loc = null;
                  Party party = player.getParty();
                  doTeleport = true;
                  switch(npc.getId()) {
                     case 32275:
                        if (world.isStatus(22)) {
                           this.runSteamRooms(world, STEAM2_SPAWNS, 23);
                        }

                        loc = new Location(147529, 152587, -12169);
                        this.cancelQuestTimers("Timer2");
                        this.cancelQuestTimers("Timer21");
                        if (party != null) {
                           for(Player partyMember : party.getMembers()) {
                              if (partyMember.getReflectionId() == world.getReflectionId()) {
                                 partyMember.stopSkillEffects(5239);
                                 SkillsParser.getInstance().getInfo(5239, 2).getEffects(partyMember, partyMember, false);
                                 this.startQuestTimer("Timer3", 600000L, npc, partyMember);
                              }
                           }
                        } else {
                           player.stopSkillEffects(5239);
                           SkillsParser.getInstance().getInfo(5239, 2).getEffects(player, player, false);
                           this.startQuestTimer("Timer3", 600000L, npc, player);
                        }

                        this.startQuestTimer("Timer31", 600000L, npc, null);
                        break;
                     case 32276:
                        if (world.isStatus(23)) {
                           this.runSteamRooms(world, STEAM3_SPAWNS, 24);
                        }

                        loc = new Location(150194, 152610, -12169);
                        this.cancelQuestTimers("Timer3");
                        this.cancelQuestTimers("Timer31");
                        if (party != null) {
                           for(Player partyMember : party.getMembers()) {
                              if (partyMember.getReflectionId() == world.getReflectionId()) {
                                 partyMember.stopSkillEffects(5239);
                                 SkillsParser.getInstance().getInfo(5239, 4).getEffects(partyMember, partyMember, false);
                                 this.startQuestTimer("Timer4", 1200000L, npc, partyMember);
                              }
                           }
                        } else {
                           player.stopSkillEffects(5239);
                           SkillsParser.getInstance().getInfo(5239, 4).getEffects(player, player, false);
                           this.startQuestTimer("Timer4", 1200000L, npc, player);
                        }

                        this.startQuestTimer("Timer41", 1200000L, npc, null);
                        break;
                     case 32277:
                        if (world.isStatus(24)) {
                           this.runSteamRooms(world, STEAM4_SPAWNS, 25);
                        }

                        loc = new Location(149743, 149986, -12141);
                        this.cancelQuestTimers("Timer4");
                        this.cancelQuestTimers("Timer41");
                        if (party != null) {
                           for(Player partyMember : party.getMembers()) {
                              if (partyMember.getReflectionId() == world.getReflectionId()) {
                                 partyMember.stopSkillEffects(5239);
                                 SkillsParser.getInstance().getInfo(5239, 3).getEffects(partyMember, partyMember, false);
                                 this.startQuestTimer("Timer5", 900000L, npc, partyMember);
                              }
                           }
                        } else {
                           player.stopSkillEffects(5239);
                           SkillsParser.getInstance().getInfo(5239, 3).getEffects(player, player, false);
                           this.startQuestTimer("Timer5", 900000L, npc, player);
                        }

                        this.startQuestTimer("Timer51", 900000L, npc, null);
                        break;
                     default:
                        doTeleport = false;
                  }

                  if (doTeleport && loc != null) {
                     if (!this.checkOracleConditions(player)) {
                        return "";
                     }

                     if (party != null) {
                        for(Player partyMember : party.getMembers()) {
                           partyMember.destroyItemByItemId("Quest", 9692, 1L, player, true);
                           this.teleportPlayer(partyMember, loc, npc.getReflectionId());
                        }
                     } else {
                        this.teleportPlayer(player, loc, npc.getReflectionId());
                     }
                  }
               } else if (npc.getId() == 32280) {
                  if (world.getStatus() >= 30 || !this.checkBaylorConditions(player)) {
                     return "";
                  }

                  world._raiders.clear();
                  Party party = player.getParty();
                  if (party == null) {
                     world._raiders.add(player);
                  } else {
                     for(Player partyMember : party.getMembers()) {
                        world._raiders.add(partyMember);
                     }
                  }

                  world.setStatus(30);
                  long time = world.endTime - System.currentTimeMillis();
                  Reflection baylorInstance = ReflectionManager.getInstance().getReflection(world.getReflectionId());
                  baylorInstance.setDuration((int)time);
                  int radius = 150;
                  int i = 0;
                  int members = world._raiders.size();

                  for(Player p : world._raiders) {
                     int x = (int)(150.0 * Math.cos((double)(i * 2) * Math.PI / (double)members));
                     int y = (int)(150.0 * Math.sin((double)(i++ * 2) * Math.PI / (double)members));
                     p.teleToLocation(153571 + x, 142075 + y, -12737, true);
                     Summon pet = p.getSummon();
                     if (pet != null) {
                        pet.teleToLocation(153571 + x, 142075 + y, -12737, true);
                        pet.broadcastPacket(new ValidateLocation(pet));
                     }

                     p.setIsParalyzed(true);
                     p.broadcastPacket(new ValidateLocation(p));
                  }

                  this.startQuestTimer("Baylor", 30000L, npc, null);
               } else if (npc.getId() == 32279 && world.isStatus(31)) {
                  this.teleportPlayer(player, new Location(153522, 144212, -9747), npc.getReflectionId());
               }
            }
         }

         return "";
      }
   }

   @Override
   public String onTrapAction(TrapInstance trap, Creature trigger, Quest.TrapAction action) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(trap.getReflectionId());
      if (tmpworld != null && tmpworld instanceof CrystalCaverns.CCWorld) {
         CrystalCaverns.CCWorld world = (CrystalCaverns.CCWorld)tmpworld;
         switch(action) {
            case TRAP_DISARMED:
               if (trap.getId() == 18378) {
                  world.getReflection().openDoor(24220001);
                  this.runEmeraldRooms(world, ROOM1_SPAWNS, 1);
               }
         }
      }

      return null;
   }

   @Override
   public String onEnterZone(Creature character, ZoneType zone) {
      if (character.isPlayer()) {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(character.getReflectionId());
         if (tmpworld != null && tmpworld instanceof CrystalCaverns.CCWorld) {
            CrystalCaverns.CCWorld world = (CrystalCaverns.CCWorld)tmpworld;
            if (world.isStatus(8)) {
               int room;
               int[][] spawns;
               switch(zone.getId()) {
                  case 20105:
                     spawns = ROOM2_SPAWNS;
                     room = 2;
                     break;
                  case 20106:
                     spawns = ROOM3_SPAWNS;
                     room = 3;
                     break;
                  case 20107:
                     spawns = ROOM4_SPAWNS;
                     room = 4;
                     break;
                  default:
                     return super.onEnterZone(character, zone);
               }

               for(DoorInstance door : ReflectionManager.getInstance().getReflection(world.getReflectionId()).getDoors()) {
                  if (door.getDoorId() == room + 24220000) {
                     if (door.getOpen()) {
                        return "";
                     }

                     QuestState st = ((Player)character).getQuestState(this.getName());
                     if (st == null) {
                        st = this.newQuestState((Player)character);
                     }

                     if (!st.hasQuestItems(9694)) {
                        return "";
                     }

                     if (world.roomsStatus[zone.getId() - 20104] == 0) {
                        this.runEmeraldRooms(world, spawns, room);
                     }

                     door.openMe();
                     st.takeItems(9694, 1L);
                     world.openedDoors.put(door, (Player)character);
                     break;
                  }
               }
            }
         }
      }

      return super.onEnterZone(character, zone);
   }

   @Override
   public String onExitZone(Creature character, ZoneType zone) {
      if (character.isPlayer()) {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(character.getReflectionId());
         if (tmpworld != null && tmpworld instanceof CrystalCaverns.CCWorld) {
            CrystalCaverns.CCWorld world = (CrystalCaverns.CCWorld)tmpworld;
            if (world.isStatus(8)) {
               int doorId;
               switch(zone.getId()) {
                  case 20105:
                     doorId = 24220002;
                     break;
                  case 20106:
                     doorId = 24220003;
                     break;
                  case 20107:
                     doorId = 24220004;
                     break;
                  default:
                     return super.onExitZone(character, zone);
               }

               for(DoorInstance door : ReflectionManager.getInstance().getReflection(world.getReflectionId()).getDoors()) {
                  if (door.getDoorId() == doorId) {
                     if (door.getOpen() && world.openedDoors.get(door) == character) {
                        door.closeMe();
                        world.openedDoors.remove(door);
                     }
                     break;
                  }
               }
            }
         }
      }

      return super.onExitZone(character, zone);
   }

   public static void main(String[] args) {
      new CrystalCaverns(CrystalCaverns.class.getSimpleName(), "instances");
   }

   private class CCWorld extends ReflectionWorld {
      public Map<Npc, Boolean> npcList1 = new HashMap<>();
      public Npc tears;
      public boolean isUsedInvulSkill = false;
      public long dragonScaleStart = 0L;
      public int dragonScaleNeed = 0;
      public int cleanedRooms = 0;
      public long endTime = 0L;
      public List<Npc> copys = new ArrayList<>();
      public Map<Npc, CrystalCaverns.CrystalGolem> crystalGolems = new HashMap<>();
      public int correctGolems = 0;
      public boolean[] OracleTriggered = new boolean[]{false, false, false};
      public int kechisHenchmanSpawn = 0;
      public int[] roomsStatus = new int[]{0, 0, 0, 0};
      public Map<DoorInstance, Player> openedDoors = new ConcurrentHashMap<>();
      public Map<Integer, Map<Npc, Boolean>> npcList2 = new HashMap<>();
      public Map<Npc, Npc> oracles = new HashMap<>();
      public List<Npc> keyKeepers = new ArrayList<>();
      public List<Npc> guards = new ArrayList<>();
      public List<Npc> oracle = new ArrayList<>();
      protected final List<Player> _raiders = new ArrayList<>();
      protected int _raidStatus = 0;
      protected long _dragonClawStart = 0L;
      protected int _dragonClawNeed = 0;
      protected final List<Npc> _animationMobs = new ArrayList<>();
      protected Npc _camera = null;
      protected Npc _baylor = null;
      protected Npc _alarm = null;

      public CCWorld(Long time) {
         this.endTime = time;
      }
   }

   protected static class CrystalGolem {
      public ItemInstance foodItem = null;
      public boolean isAtDestination = false;
      public Location oldpos = null;
   }
}
