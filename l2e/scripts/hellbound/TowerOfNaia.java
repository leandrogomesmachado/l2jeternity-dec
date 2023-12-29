package l2e.scripts.hellbound;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.GlobalVariablesManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.MinionList;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.templates.npc.MinionData;
import l2e.gameserver.model.actor.templates.npc.MinionTemplate;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.model.zone.type.EffectZone;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class TowerOfNaia extends Quest {
   private static final int STATE_SPORE_CHALLENGE_IN_PROGRESS = 1;
   private static final int STATE_SPORE_CHALLENGE_SUCCESSFULL = 2;
   private static final int STATE_SPORE_IDLE_TOO_LONG = 3;
   private static final int SELF_DESPAWN_LIMIT = 600;
   private static final int ELEMENT_INDEX_LIMIT = Config.EPIDOS_POINTS_NEED;
   private static final int LOCK = 18491;
   private static final int CONTROLLER = 18492;
   private static final int ROOM_MANAGER_FIRST = 18494;
   private static final int ROOM_MANAGER_LAST = 18505;
   private static final int MUTATED_ELPY = 25604;
   private static final int SPORE_BASIC = 25613;
   private static final int SPORE_FIRE = 25605;
   private static final int SPORE_WATER = 25606;
   private static final int SPORE_WIND = 25607;
   private static final int SPORE_EARTH = 25608;
   private static final int DWARVEN_GHOST = 32370;
   private static final int[] EPIDOSES = new int[]{25610, 25609, 25612, 25611};
   private static final int[] TOWER_MONSTERS = new int[]{18490, 22393, 22394, 22395, 22411, 22412, 22413, 22439, 22440, 22441, 22442};
   private static final int[] ELEMENTS = new int[]{25605, 25606, 25607, 25608};
   private static final int[] OPPOSITE_ELEMENTS = new int[]{25606, 25605, 25608, 25607};
   private static final String[] ELEMENTS_NAME = new String[]{"Fire", "Water", "Wind", "Earth"};
   private static final int[][] SPORES_MOVE_POINTS = new int[][]{
      {-46080, 246368, -14183},
      {-44816, 246368, -14183},
      {-44224, 247440, -14184},
      {-44896, 248464, -14183},
      {-46064, 248544, -14183},
      {-46720, 247424, -14183}
   };
   private static final int[][] SPORES_MERGE_POSITION = new int[][]{
      {-45488, 246768, -14183}, {-44767, 247419, -14183}, {-46207, 247417, -14183}, {-45462, 248174, -14183}
   };
   private static final NpcStringId[] SPORES_NPCSTRING_ID = new NpcStringId[]{
      NpcStringId.ITS_S1, NpcStringId.S1_IS_STRONG, NpcStringId.ITS_ALWAYS_S1, NpcStringId.S1_WONT_DO
   };
   private static Map<Integer, int[]> DOORS = new HashMap<>();
   private static Map<Integer, Integer> ZONES = new HashMap<>();
   private static Map<Integer, int[][]> SPAWNS = new HashMap<>();
   private MonsterInstance _lock;
   private final Npc _controller;
   private int _counter;
   private int _despawnedSporesCount;
   private final int[] _indexCount = new int[]{0, 0};
   private int _challengeState;
   private int _winIndex;
   private final Map<Integer, Boolean> _activeRooms = new HashMap<>();
   private final Map<Integer, List<Npc>> _spawns = new ConcurrentHashMap<>();
   private final Set<Npc> _sporeSpawn = ConcurrentHashMap.newKeySet();

   public TowerOfNaia(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addFirstTalkId(18492);
      this.addStartNpc(18492);
      this.addStartNpc(32370);
      this.addTalkId(18492);
      this.addTalkId(32370);
      this.addAttackId(18491);
      this.addKillId(18491);
      this.addKillId(25604);
      this.addSpawnId(new int[]{25604});
      this.addKillId(25613);
      this.addSpawnId(new int[]{25613});

      for(int npcId = 25605; npcId <= 25608; ++npcId) {
         this.addKillId(npcId);
         this.addSpawnId(new int[]{npcId});
      }

      for(int npcId = 18494; npcId <= 18505; ++npcId) {
         this.addFirstTalkId(npcId);
         this.addTalkId(npcId);
         this.addStartNpc(npcId);
         this.initRoom(npcId);
      }

      for(int npcId : TOWER_MONSTERS) {
         this.addKillId(npcId);
      }

      this._lock = (MonsterInstance)addSpawn(18491, 16409, 244438, 11620, -1048, false, 0L, false);
      this._controller = addSpawn(18492, 16608, 244420, 11620, 31264, false, 0L, false);
      this._counter = 90;
      this._despawnedSporesCount = 0;
      this._challengeState = 0;
      this._winIndex = -1;
      this.initSporeChallenge();
      this.spawnElpy();
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      int npcId = npc.getId();
      if (npcId == 18492) {
         return this._lock == null ? "18492-02.htm" : "18492-01.htm";
      } else if (npcId < 18494 || npcId > 18505 || !this._activeRooms.containsKey(npcId) || this._activeRooms.get(npcId)) {
         return null;
      } else if (player.getParty() == null) {
         player.sendPacket(SystemMessageId.CAN_OPERATE_MACHINE_WHEN_IN_PARTY);
         return null;
      } else {
         return "manager.htm";
      }
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      if (event.equalsIgnoreCase("spawn_lock")) {
         htmltext = null;
         this._lock = (MonsterInstance)addSpawn(18491, 16409, 244438, 11620, -1048, false, 0L, false);
         this._counter = 90;
      } else if (event.equalsIgnoreCase("despawn_total")) {
         if (this._challengeState == 3) {
            for(Npc spore : this._sporeSpawn) {
               if (spore != null && !spore.isDead()) {
                  spore.deleteMe();
               }
            }

            this._sporeSpawn.clear();
            this.initSporeChallenge();
         } else if (this._challengeState == 2 && this._winIndex >= 0) {
            if (this._despawnedSporesCount < 10 && !this._sporeSpawn.isEmpty()) {
               Iterator<Npc> it = this._sporeSpawn.iterator();

               while(it.hasNext()) {
                  Npc spore = it.next();
                  if (spore != null && !spore.isDead() && spore.getX() == spore.getSpawn().getX() && spore.getY() == spore.getSpawn().getY()) {
                     spore.deleteMe();
                     it.remove();
                     ++this._despawnedSporesCount;
                  }
               }

               this.startQuestTimer("despawn_total", 3000L, null, null);
            } else {
               if (!this._sporeSpawn.isEmpty()) {
                  for(Npc spore : this._sporeSpawn) {
                     if (spore != null && !spore.isDead()) {
                        spore.deleteMe();
                     }
                  }
               }

               this._sporeSpawn.clear();
               this._despawnedSporesCount = 0;
               int[] coords = SPORES_MERGE_POSITION[this._winIndex];
               addSpawn(EPIDOSES[this._winIndex], coords[0], coords[1], coords[2], 0, false, 0L, false);
               this.initSporeChallenge();
            }
         }
      }

      if (npc == null) {
         return null;
      } else {
         int npcId = npc.getId();
         if (event.equalsIgnoreCase("despawn_spore") && !npc.isDead() && this._challengeState == 1) {
            htmltext = null;
            this._sporeSpawn.remove(npc);
            npc.deleteMe();
            if (npcId == 25613) {
               this.spawnRandomSpore();
               this.spawnRandomSpore();
            } else if (npcId >= 25605 && npcId <= 25608) {
               ++this._despawnedSporesCount;
               if (this._despawnedSporesCount < 600) {
                  this.spawnOppositeSpore(npcId);
               } else {
                  this._challengeState = 3;
                  this.startQuestTimer("despawn_total", 60000L, null, null);
               }
            }
         } else if (event.equalsIgnoreCase("18492-05.htm")) {
            if (this._lock == null || this._lock.getCurrentHp() > this._lock.getMaxHp() / 10.0) {
               htmltext = null;
               if (this._lock != null) {
                  this._lock.deleteMe();
                  this._lock = null;
               }

               this.cancelQuestTimers("spawn_lock");
               this.startQuestTimer("spawn_lock", 300000L, null, null);
               npc.setTarget(player);
               npc.doCast(SkillsParser.getInstance().getInfo(5527, 1));
            }
         } else if (event.equalsIgnoreCase("teleport") && this._lock != null) {
            htmltext = null;
            Party party = player.getParty();
            if (party == null) {
               player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
               return null;
            }

            if (party.getLeader() != player) {
               player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
               return null;
            }

            for(Player member : party.getMembers()) {
               if (member.getLevel() < 80) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
                  sm.addPcName(member);
                  player.sendPacket(sm);
                  return null;
               }

               if (!member.isInsideRadius(player, 500, true, true)) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
                  sm.addPcName(member);
                  player.sendPacket(sm);
                  return null;
               }
            }

            for(Player partyMember : party.getMembers()) {
               if (Util.checkIfInRange(500, partyMember, npc, true)) {
                  partyMember.teleToLocation(-47271, 246098, -9120, true);
                  player.sendMessage("The Tower of Naia countdown has begun. You have only 5 minutes to pass each room.");
               }
            }

            this._lock.broadcastPacket(new MagicSkillUse(this._lock, this._lock, 5527, 1, 0, 0));
            this._lock.deleteMe();
            this._lock = null;
            this.cancelQuestTimers("spawn_lock");
            this.startQuestTimer("spawn_lock", 1200000L, null, null);
         } else if (event.equalsIgnoreCase("go") && this._activeRooms.containsKey(npcId) && !this._activeRooms.get(npcId)) {
            htmltext = null;
            Party party = player.getParty();
            if (party != null) {
               this.removeForeigners(npcId, party);
               this.startRoom(npcId);
               ThreadPoolManager.getInstance().schedule(new TowerOfNaia.StopRoomTask(npcId), 300000L);
               player.sendMessage("Group timer has been updated!");
            } else {
               player.sendPacket(SystemMessageId.CAN_OPERATE_MACHINE_WHEN_IN_PARTY);
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill) {
      if (this._lock != null && npc.getObjectId() == this._lock.getObjectId()) {
         int remaindedHpPercent = (int)(npc.getCurrentHp() * 100.0 / npc.getMaxHp());
         if (remaindedHpPercent <= this._counter && this._controller != null) {
            if (this._counter == 50) {
               this._lock.getMinionList().addMinion(new MinionData(new MinionTemplate(18493, 1)), true);
            } else if (this._counter == 10) {
               this._lock.getMinionList().addMinion(new MinionData(new MinionTemplate(18493, 2)), true);
            }

            this._controller
               .broadcastPacket(
                  new NpcSay(this._controller.getObjectId(), 22, this._controller.getId(), NpcStringId.EMERGENCY_EMERGENCY_THE_OUTER_WALL_IS_WEAKENING_RAPIDLY),
                  2000
               );
            this._counter -= 10;
         }
      }

      return super.onAttack(npc, attacker, damage, isSummon, skill);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      int npcId = npc.getId();
      if (npcId == 18491) {
         this._lock = null;
         this.cancelQuestTimers("spawn_lock");
         MinionList ml = npc.getMinionList();
         if (ml != null) {
            ml.deleteMinions();
         }

         this.startQuestTimer("spawn_lock", 300000L, null, null);
      } else if (Arrays.binarySearch(TOWER_MONSTERS, npcId) >= 0) {
         int managerId = 0;

         for(ZoneType zone : ZoneManager.getInstance().getZones(npc.getX(), npc.getY(), npc.getZ())) {
            if (ZONES.containsValue(zone.getId())) {
               for(int i : ZONES.keySet()) {
                  if (ZONES.get(i) == zone.getId()) {
                     managerId = i;
                     break;
                  }
               }
            }
         }

         if (managerId > 0 && this._spawns.containsKey(managerId)) {
            List<Npc> spawned = this._spawns.get(managerId);
            spawned.remove(npc);
            if (spawned.isEmpty() && DOORS.containsKey(managerId)) {
               int[] doorList = (int[])DOORS.get(managerId);
               DoorParser.getInstance().getDoor(doorList[1]).openMe();
               this._spawns.remove(managerId);
            }
         }
      } else if (npcId == 25604) {
         this._challengeState = 1;
         this.markElpyRespawn();
         DoorParser.getInstance().getDoor(18250025).closeMe();
         ((EffectZone)ZoneManager.getInstance().getZoneById(200100)).setZoneEnabled(true);

         for(int i = 0; i < 10; ++i) {
            addSpawn(25613, -45474, 247450, -13994, 49152, false, 0L, false);
         }
      } else if (npcId == 25613 && this._challengeState == 1) {
         this._sporeSpawn.remove(npc);
         this.spawnRandomSpore();
         this.spawnRandomSpore();
      } else if (npcId >= 25605 && npcId <= 25608 && (this._challengeState == 1 || this._challengeState == 2)) {
         this._sporeSpawn.remove(npc);
         if (this._challengeState == 1) {
            --this._despawnedSporesCount;
            int sporeGroup = this.getSporeGroup(npcId);
            if (sporeGroup >= 0) {
               if (npcId != 25605 && npcId != 25607) {
                  this._indexCount[sporeGroup] -= 2;
               } else {
                  this._indexCount[sporeGroup] += 2;
               }

               if (this._indexCount[Math.abs(sporeGroup - 1)] > 0) {
                  this._indexCount[Math.abs(sporeGroup - 1)]--;
               } else if (this._indexCount[Math.abs(sporeGroup - 1)] < 0) {
                  this._indexCount[Math.abs(sporeGroup - 1)]++;
               }

               if (Math.abs(this._indexCount[sporeGroup]) < ELEMENT_INDEX_LIMIT
                  && Math.abs(this._indexCount[sporeGroup]) > 0
                  && this._indexCount[sporeGroup] % 20 == 0
                  && getRandom(100) < 50) {
                  String el = ELEMENTS_NAME[Arrays.binarySearch(ELEMENTS, npcId)];

                  for(Npc spore : this._sporeSpawn) {
                     if (spore != null && !spore.isDead() && spore.getId() == npcId) {
                        NpcSay ns = new NpcSay(spore.getObjectId(), 22, spore.getId(), SPORES_NPCSTRING_ID[getRandom(4)]);
                        ns.addStringParameter(el);
                        spore.broadcastPacket(ns, 2000);
                     }
                  }
               }

               if (Math.abs(this._indexCount[sporeGroup]) < ELEMENT_INDEX_LIMIT) {
                  if ((
                        this._indexCount[sporeGroup] > 0 && (npcId == 25605 || npcId == 25607)
                           || this._indexCount[sporeGroup] <= 0 && (npcId == 25606 || npcId == 25608)
                     )
                     && getRandom(1000) > 200) {
                     this.spawnOppositeSpore(npcId);
                  } else {
                     this.spawnRandomSpore();
                  }
               } else {
                  this._challengeState = 2;
                  this._despawnedSporesCount = 0;
                  this._winIndex = Arrays.binarySearch(ELEMENTS, npcId);
                  int[] coord = SPORES_MERGE_POSITION[this._winIndex];

                  for(Npc spore : this._sporeSpawn) {
                     if (spore != null && !spore.isDead()) {
                        this.moveTo(spore, coord);
                     }
                  }

                  this.startQuestTimer("despawn_total", 3000L, null, null);
               }
            }
         }
      }

      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public final String onSpawn(Npc npc) {
      int npcId = npc.getId();
      if (npcId == 25604 && !npc.isTeleporting()) {
         DoorParser.getInstance().getDoor(18250025).openMe();
         ((EffectZone)ZoneManager.getInstance().getZoneById(200100)).setZoneEnabled(false);
         ((EffectZone)ZoneManager.getInstance().getZoneById(200101)).setZoneEnabled(true);
         ((EffectZone)ZoneManager.getInstance().getZoneById(200101)).setZoneEnabled(false);
      } else if ((npcId == 25613 || npcId >= 25605 && npcId <= 25608) && this._challengeState == 1) {
         this._sporeSpawn.add(npc);
         npc.setIsRunning(false);
         int[] coord = SPORES_MOVE_POINTS[getRandom(SPORES_MOVE_POINTS.length)];
         npc.getSpawn().setX(coord[0]);
         npc.getSpawn().setY(coord[1]);
         npc.getSpawn().setZ(coord[2]);
         npc.getAI().setIntention(CtrlIntention.MOVING, new Location(coord[0], coord[1], coord[2], 0));
         this.startQuestTimer("despawn_spore", 60000L, npc, null);
      }

      return super.onSpawn(npc);
   }

   private int getSporeGroup(int sporeId) {
      int ret;
      switch(sporeId) {
         case 25605:
         case 25606:
            ret = 0;
            break;
         case 25607:
         case 25608:
            ret = 1;
            break;
         default:
            ret = -1;
      }

      return ret;
   }

   protected void initRoom(int managerId) {
      this.removeAllPlayers(managerId);
      this._activeRooms.put(managerId, false);
      if (DOORS.containsKey(managerId)) {
         int[] doorList = (int[])DOORS.get(managerId);
         DoorParser.getInstance().getDoor(doorList[0]).openMe();
         DoorParser.getInstance().getDoor(doorList[1]).closeMe();
      }

      if (this._spawns.containsKey(managerId) && this._spawns.get(managerId) != null) {
         for(Npc npc : this._spawns.get(managerId)) {
            if (npc != null && !npc.isDead()) {
               npc.deleteMe();
            }
         }

         this._spawns.get(managerId).clear();
         this._spawns.remove(managerId);
      }
   }

   private void initSporeChallenge() {
      this._despawnedSporesCount = 0;
      this._challengeState = 0;
      this._winIndex = -1;
      this._indexCount[0] = 0;
      this._indexCount[1] = 0;
      ((EffectZone)ZoneManager.getInstance().getZoneById(200100)).setZoneEnabled(false);
      ((EffectZone)ZoneManager.getInstance().getZoneById(200101)).setZoneEnabled(false);
      ((EffectZone)ZoneManager.getInstance().getZoneById(200101)).setZoneEnabled(true);
   }

   private void markElpyRespawn() {
      long respawnTime = (long)(getRandom(43200, 216000) * 1000) + System.currentTimeMillis();
      GlobalVariablesManager.getInstance().storeVariable("elpy_respawn_time", Long.toString(respawnTime));
   }

   private int moveTo(Npc npc, int[] coords) {
      int time = 0;
      if (npc != null) {
         double distance = Util.calculateDistance(coords[0], coords[1], coords[2], npc.getX(), npc.getY(), npc.getZ(), true);
         int heading = Util.calculateHeadingFrom(npc.getX(), npc.getY(), coords[0], coords[1]);
         time = (int)(distance / npc.getWalkSpeed() * 1000.0);
         npc.setIsRunning(false);
         npc.disableCoreAI(true);
         npc.setIsNoRndWalk(true);
         npc.getAI().setIntention(CtrlIntention.MOVING, new Location(coords[0], coords[1], coords[2], heading));
         npc.getSpawn().setX(coords[0]);
         npc.getSpawn().setY(coords[1]);
         npc.getSpawn().setZ(coords[2]);
      }

      return time == 0 ? 100 : time;
   }

   private void spawnElpy() {
      long respawnTime = GlobalVariablesManager.getInstance().getLong("elpy_respawn_time", 0L);
      if (respawnTime <= System.currentTimeMillis()) {
         addSpawn(25604, -45480, 246824, -14209, 49152, false, 0L, false);
      } else {
         ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
               Quest.addSpawn(25604, -45480, 246824, -14209, 49152, false, 0L, false);
            }
         }, respawnTime - System.currentTimeMillis());
      }
   }

   private Npc spawnRandomSpore() {
      return addSpawn(getRandom(25605, 25608), -45474, 247450, -13994, 49152, false, 0L, false);
   }

   private Npc spawnOppositeSpore(int srcSporeId) {
      int idx = Arrays.binarySearch(ELEMENTS, srcSporeId);
      return idx >= 0 ? addSpawn(OPPOSITE_ELEMENTS[idx], -45474, 247450, -13994, 49152, false, 0L, false) : null;
   }

   private void startRoom(int managerId) {
      this._activeRooms.put(managerId, true);
      if (DOORS.containsKey(managerId)) {
         int[] doorList = (int[])DOORS.get(managerId);
         DoorParser.getInstance().getDoor(doorList[0]).closeMe();
      }

      if (SPAWNS.containsKey(managerId)) {
         int[][] spawnList = (int[][])SPAWNS.get(managerId);
         List<Npc> spawned = new CopyOnWriteArrayList<>();

         for(int[] spawn : spawnList) {
            Npc spawnedNpc = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0L, false);
            spawned.add(spawnedNpc);
         }

         if (!spawned.isEmpty()) {
            this._spawns.put(managerId, spawned);
         }
      }
   }

   private void removeForeigners(int managerId, Party party) {
      if (party != null && ZONES.containsKey(managerId) && ZoneManager.getInstance().getZoneById(ZONES.get(managerId)) != null) {
         ZoneType zone = ZoneManager.getInstance().getZoneById(ZONES.get(managerId));

         for(Player player : zone.getPlayersInside()) {
            if (player != null) {
               Party charParty = player.getParty();
               if (charParty == null || charParty.getLeaderObjectId() != party.getLeaderObjectId()) {
                  player.teleToLocation(16110, 243841, 11616, true);
               }
            }
         }
      }
   }

   private void removeAllPlayers(int managerId) {
      if (ZONES.containsKey(managerId) && ZoneManager.getInstance().getZoneById(ZONES.get(managerId)) != null) {
         ZoneType zone = ZoneManager.getInstance().getZoneById(ZONES.get(managerId));

         for(Player kicked : zone.getPlayersInside()) {
            if (kicked != null) {
               kicked.teleToLocation(17656, 244328, 11595, true);
               kicked.sendMessage("The time has expired. You cannot stay in Tower of Naia any longer...");
            }
         }
      }
   }

   public static void main(String[] args) {
      new TowerOfNaia(-1, "TowerOfNaia", "hellbound");
   }

   static {
      DOORS.put(18494, new int[]{18250001, 18250002});
      DOORS.put(18495, new int[]{18250003, 18250004});
      DOORS.put(18496, new int[]{18250005, 18250006});
      DOORS.put(18497, new int[]{18250007, 18250008});
      DOORS.put(18498, new int[]{18250009, 18250010});
      DOORS.put(18499, new int[]{18250011, 18250101});
      DOORS.put(18500, new int[]{18250013, 18250014});
      DOORS.put(18501, new int[]{18250015, 18250102});
      DOORS.put(18502, new int[]{18250017, 18250018});
      DOORS.put(18503, new int[]{18250019, 18250103});
      DOORS.put(18504, new int[]{18250021, 18250022});
      DOORS.put(18505, new int[]{18250023, 18250024});
      ZONES.put(18494, 200020);
      ZONES.put(18495, 200021);
      ZONES.put(18496, 200022);
      ZONES.put(18497, 200023);
      ZONES.put(18498, 200024);
      ZONES.put(18499, 200025);
      ZONES.put(18500, 200026);
      ZONES.put(18501, 200027);
      ZONES.put(18502, 200028);
      ZONES.put(18503, 200029);
      ZONES.put(18504, 200030);
      ZONES.put(18505, 200031);
      SPAWNS.put(
         18494,
         new int[][]{
            {22393, -46371, 246400, -9120, 0},
            {22394, -46435, 245830, -9120, 0},
            {22394, -46536, 246275, -9120, 0},
            {22393, -46239, 245996, -9120, 0},
            {22394, -46229, 246347, -9120, 0},
            {22394, -46019, 246198, -9120, 0}
         }
      );
      SPAWNS.put(
         18495,
         new int[][]{
            {22439, -48146, 249597, -9124, -16280},
            {22439, -48144, 248711, -9124, 16368},
            {22439, -48704, 249597, -9104, -16380},
            {22439, -49219, 249596, -9104, -16400},
            {22439, -49715, 249601, -9104, -16360},
            {22439, -49714, 248696, -9104, 15932},
            {22439, -49225, 248710, -9104, 16512},
            {22439, -48705, 248708, -9104, 16576}
         }
      );
      SPAWNS.put(
         18496,
         new int[][]{
            {22441, -51176, 246055, -9984, 0}, {22441, -51699, 246190, -9984, 0}, {22442, -52060, 245956, -9984, 0}, {22442, -51565, 246433, -9984, 0}
         }
      );
      SPAWNS.put(
         18497,
         new int[][]{
            {22440, -49754, 243866, -9968, -16328},
            {22440, -49754, 242940, -9968, 16336},
            {22440, -48733, 243858, -9968, -16208},
            {22440, -48745, 242936, -9968, 16320},
            {22440, -49264, 242946, -9968, 16312},
            {22440, -49268, 243869, -9968, -16448},
            {22440, -48186, 242934, -9968, 16576},
            {22440, -48185, 243855, -9968, -16448}
         }
      );
      SPAWNS.put(
         18498,
         new int[][]{
            {22411, -46355, 246375, -9984, 0},
            {22411, -46167, 246160, -9984, 0},
            {22393, -45952, 245748, -9984, 0},
            {22394, -46428, 246254, -9984, 0},
            {22393, -46490, 245871, -9984, 0},
            {22394, -45877, 246309, -9984, 0}
         }
      );
      SPAWNS.put(18499, new int[][]{{22395, -48730, 248067, -9984, 0}, {22395, -49112, 248250, -9984, 0}});
      SPAWNS.put(
         18500,
         new int[][]{
            {22393, -51954, 246475, -10848, 0},
            {22394, -51421, 246512, -10848, 0},
            {22394, -51404, 245951, -10848, 0},
            {22393, -51913, 246206, -10848, 0},
            {22394, -51663, 245979, -10848, 0},
            {22394, -51969, 245809, -10848, 0},
            {22412, -51259, 246357, -10848, 0}
         }
      );
      SPAWNS.put(18501, new int[][]{{22395, -48856, 243949, -10848, 0}, {22395, -49144, 244190, -10848, 0}});
      SPAWNS.put(
         18502,
         new int[][]{
            {22441, -46471, 246135, -11704, 0},
            {22441, -46449, 245997, -11704, 0},
            {22441, -46235, 246187, -11704, 0},
            {22441, -46513, 246326, -11704, 0},
            {22441, -45889, 246313, -11704, 0}
         }
      );
      SPAWNS.put(18503, new int[][]{{22395, -49067, 248050, -11712, 0}, {22395, -48957, 248223, -11712, 0}});
      SPAWNS.put(
         18504,
         new int[][]{
            {22413, -51748, 246138, -12568, 0},
            {22413, -51279, 246200, -12568, 0},
            {22413, -51787, 246594, -12568, 0},
            {22413, -51892, 246544, -12568, 0},
            {22413, -51500, 245781, -12568, 0},
            {22413, -51941, 246045, -12568, 0}
         }
      );
      SPAWNS.put(
         18505,
         new int[][]{
            {18490, -48238, 243347, -13376, 0},
            {18490, -48462, 244022, -13376, 0},
            {18490, -48050, 244045, -13376, 0},
            {18490, -48229, 243823, -13376, 0},
            {18490, -47871, 243208, -13376, 0},
            {18490, -48255, 243528, -13376, 0},
            {18490, -48461, 243780, -13376, 0},
            {18490, -47983, 243197, -13376, 0},
            {18490, -47841, 243819, -13376, 0},
            {18490, -48646, 243764, -13376, 0},
            {18490, -47806, 243850, -13376, 0},
            {18490, -48456, 243447, -13376, 0}
         }
      );
   }

   private class StopRoomTask implements Runnable {
      private final int _managerId;

      public StopRoomTask(int managerId) {
         this._managerId = managerId;
      }

      @Override
      public void run() {
         TowerOfNaia.this.initRoom(this._managerId);
      }
   }
}
