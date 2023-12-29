package l2e.scripts.hellbound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.Util;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.RaidBossSpawnManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.templates.npc.MinionData;
import l2e.gameserver.model.actor.templates.npc.MinionTemplate;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.model.zone.type.DamageZone;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class TullyWorkshop extends Quest {
   private static final int AGENT = 32372;
   private static final int CUBE_68 = 32467;
   private static final int DORIAN = 32373;
   private static final int DARION = 25603;
   private static final int TULLY = 25544;
   private static final int DWARVEN_GHOST = 32370;
   private static final int TOMBSTONE = 32344;
   private static final int INGENIOUS_CONTRAPTION = 32371;
   private static final int PILLAR = 18506;
   private static final int TIMETWISTER_GOLEM = 22392;
   private static final int[] SIN_WARDENS = new int[]{22423, 22431};
   private static final int SERVANT_FIRST = 22405;
   private static final int SERVANT_LAST = 22410;
   private static final int TEMENIR = 25600;
   private static final int DRAXIUS = 25601;
   private static final int KIRETCENAH = 25602;
   private static final int[] REWARDS = new int[]{10427, 10428, 10429, 10430, 10431};
   private static final int[] DEATH_COUNTS = new int[]{7, 10};
   private static final byte STATE_OPEN = 0;
   private static final byte STATE_CLOSE = 1;
   private static final int[] TELEPORTING_MONSTERS = new int[]{22377, 22378, 22379, 22383};
   private static final Map<Integer, int[]> TULLY_DOORLIST = new HashMap<>();
   private static final Map<Integer, int[][]> TELE_COORDS = new HashMap<>();
   protected int countdownTime;
   private int nextServantIdx = 0;
   private int killedFollowersCount = 0;
   private boolean allowServantSpawn = true;
   private boolean allowAgentSpawn = true;
   private boolean allowAgentSpawn_7th = true;
   private boolean is7thFloorAttackBegan = false;
   protected ScheduledFuture<?> _countdown = null;
   protected static List<Npc> postMortemSpawn = new ArrayList<>();
   protected static Set<Integer> brokenContraptions = ConcurrentHashMap.newKeySet();
   protected static Set<Integer> rewardedContraptions = new HashSet<>();
   protected static Set<Integer> talkedContraptions = new HashSet<>();
   private final List<MonsterInstance> spawnedFollowers = new ArrayList<>();
   private final List<MonsterInstance> spawnedFollowerMinions = new ArrayList<>();
   private Npc spawnedAgent = null;
   private Spawner pillarSpawn = null;
   private final int[][] deathCount = new int[2][4];
   private static final int[][] POST_MORTEM_SPAWNLIST = new int[][]{
      {32371, -12524, 273932, -9014, 49151, 0},
      {32371, -10831, 273890, -9040, 81895, 0},
      {32371, -10817, 273986, -9040, -16452, 0},
      {32371, -13773, 275119, -9040, 8428, 49151, 0},
      {32371, -11547, 271772, -9040, -19124, 0},
      {22392, -10832, 273808, -9040, 0, 0},
      {22392, -10816, 274096, -9040, 14964, 0},
      {22392, -13824, 275072, -9040, -24644, 0},
      {22392, -11504, 271952, -9040, 9328, 0},
      {22392, -11680, 275353, -9040, 0, 0},
      {22392, -12388, 271668, -9040, 0, 0},
      {32370, -11984, 272928, -9040, 23644, 900000},
      {32370, -14643, 274588, -9040, 49152, 0},
      {32344, -14756, 274788, -9040, -13868, 0}
   };
   private static final int[][] SPAWNLIST_7TH_FLOOR = new int[][]{
      {25602, -12528, 279488, -11622, 16384},
      {25600, -12736, 279681, -11622, 0},
      {25601, -12324, 279681, -11622, 32768},
      {25599, -12281, 281497, -11935, 49151},
      {25599, -11903, 281488, -11934, 49151},
      {25599, -11966, 277935, -11936, 16384},
      {25599, -12334, 277935, -11936, 16384},
      {25599, -12739, 277935, -11936, 16384},
      {25599, -13063, 277934, -11936, 16384},
      {25599, -13077, 281506, -11935, 49151},
      {25599, -12738, 281503, -11935, 49151},
      {25597, -11599, 281323, -11933, -23808},
      {25597, -11381, 281114, -11934, -23808},
      {25597, -11089, 280819, -11934, -23808},
      {25597, -10818, 280556, -11934, -23808},
      {25597, -10903, 278798, -11934, 25680},
      {25597, -11134, 278558, -11934, 25680},
      {25597, -11413, 278265, -11934, 25680},
      {25597, -11588, 278072, -11935, 25680},
      {25597, -13357, 278058, -11935, 9068},
      {25597, -13617, 278289, -11935, 9068},
      {25597, -13920, 278567, -11935, 9068},
      {25597, -14131, 278778, -11936, 9068},
      {25597, -14184, 280545, -11936, -7548},
      {25597, -13946, 280792, -11936, -7548},
      {25597, -13626, 281105, -11936, -7548},
      {25597, -13386, 281360, -11935, -7548},
      {25598, -10697, 280244, -11936, 32768},
      {25598, -10702, 279926, -11936, 32768},
      {25598, -10722, 279470, -11936, 32768},
      {25598, -10731, 279126, -11936, 32768},
      {25598, -14284, 279140, -11936, 0},
      {25598, -14286, 279464, -11936, 0},
      {25598, -14290, 279909, -11935, 0},
      {25598, -14281, 280229, -11936, 0}
   };
   private static final int[][] SPAWN_ZONE_DEF = new int[][]{{200012, 200013, 200014, 200015}, {200016, 200017, 200018, 200019}};
   private static final int[][] AGENT_COORDINATES = new int[][]{
      {-13312, 279172, -13599, -20300},
      {-11696, 280208, -13599, 13244},
      {-13008, 280496, -13599, 27480},
      {-11984, 278880, -13599, -4472},
      {-13312, 279172, -10492, -20300},
      {-11696, 280208, -10492, 13244},
      {-13008, 280496, -10492, 27480},
      {-11984, 278880, -10492, -4472}
   };
   private static final int[][] SERVANT_COORDINATES = new int[][]{
      {-13214, 278493, -13601, 0},
      {-11727, 280711, -13601, 0},
      {-13562, 280175, -13601, 0},
      {-11514, 278592, -13601, 0},
      {-13370, 278459, -10497, 0},
      {-11984, 280894, -10497, 0},
      {-14050, 280312, -10497, 0},
      {-11559, 278725, -10495, 0}
   };
   private static final int[][] CUBE_68_TELEPORTS = new int[][]{{-12176, 279696, -13596}, {-12176, 279696, -10492}, {21935, 243923, 11088}};

   public TullyWorkshop(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32373);
      this.addTalkId(32373);

      for(int npcId : TULLY_DOORLIST.keySet()) {
         if (npcId != 99999) {
            this.addFirstTalkId(npcId);
            this.addStartNpc(npcId);
            this.addTalkId(npcId);
         }
      }

      for(int npcId : TELE_COORDS.keySet()) {
         this.addStartNpc(npcId);
         this.addTalkId(npcId);
      }

      for(int monsterId : TELEPORTING_MONSTERS) {
         this.addAttackId(monsterId);
      }

      for(int monsterId : SIN_WARDENS) {
         this.addKillId(monsterId);
      }

      this.addStartNpc(32372);
      this.addStartNpc(32467);
      this.addStartNpc(32371);
      this.addStartNpc(32370);
      this.addStartNpc(32344);
      this.addTalkId(32372);
      this.addTalkId(32467);
      this.addTalkId(32371);
      this.addTalkId(32370);
      this.addTalkId(32370);
      this.addTalkId(32344);
      this.addFirstTalkId(32372);
      this.addFirstTalkId(32467);
      this.addFirstTalkId(32371);
      this.addFirstTalkId(32370);
      this.addFirstTalkId(32344);
      this.addKillId(25544);
      this.addKillId(22392);
      this.addKillId(25600);
      this.addKillId(25601);
      this.addKillId(25602);
      this.addKillId(25603);
      this.addKillId(18506);
      this.addFactionCallId(new int[]{25600});
      this.addFactionCallId(new int[]{25601});
      this.addFactionCallId(new int[]{25602});
      this.addSpawnId(new int[]{32467});
      this.addSpawnId(new int[]{25603});
      this.addSpawnId(new int[]{25544});
      this.addSpawnId(new int[]{18506});
      this.addSpellFinishedId(new int[]{32372});
      this.addSpellFinishedId(new int[]{25600});

      for(int i = 22405; i <= 22410; ++i) {
         this.addKillId(i);
      }

      for(int i = 22405; i <= 22410; ++i) {
         this.addSpellFinishedId(new int[]{i});
      }

      this.initDeathCounter(0);
      this.initDeathCounter(1);
      this.do7thFloorSpawn();
      this.doOnLoadSpawn();
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      ClassId classId = player.getClassId();
      int npcId = npc.getId();
      if (TULLY_DOORLIST.containsKey(npcId)) {
         return classId.equalsOrChildOf(ClassId.maestro) ? "doorman-01c.htm" : "doorman-01.htm";
      } else if (npcId == 32371) {
         if (talkedContraptions.contains(npc.getObjectId())) {
            return "32371-02.htm";
         } else if (!brokenContraptions.contains(npc.getObjectId())) {
            return classId.equalsOrChildOf(ClassId.maestro) ? "32371-01a.htm" : "32371-01.htm";
         } else {
            return "32371-04.htm";
         }
      } else if (npcId == 32370) {
         if (postMortemSpawn.indexOf(npc) == 11) {
            npc.broadcastPacket(
               new NpcSay(
                  npc.getObjectId(), 22, npc.getId(), NpcStringId.HA_HA_YOU_WERE_SO_AFRAID_OF_DEATH_LET_ME_SEE_IF_YOU_FIND_ME_IN_TIME_MAYBE_YOU_CAN_FIND_A_WAY
               ),
               2000
            );
            npc.deleteMe();
            return null;
         } else if (postMortemSpawn.indexOf(npc) == 12) {
            return "32370-01.htm";
         } else {
            return npc.isInsideRadius(-45531, 245872, -14192, 100, true, false) ? "32370-03.htm" : "32370-02.htm";
         }
      } else if (npcId == 32372) {
         Party party = player.getParty();
         if (party != null && party.getLeaderObjectId() == player.getObjectId()) {
            int[] roomData = this.getRoomData(npc);
            return roomData[0] >= 0 && roomData[1] >= 0 ? "32372-01.htm" : "32372-02.htm";
         } else {
            return "32372-01a.htm";
         }
      } else if (npcId == 32467) {
         if (npc.isInsideRadius(-12752, 279696, -13596, 100, true, false)) {
            return "32467-01.htm";
         } else {
            return npc.isInsideRadius(-12752, 279696, -10492, 100, true, false) ? "32467-02.htm" : "32467-03.htm";
         }
      } else if (npcId == 32344) {
         for(int itemId : REWARDS) {
            if (player.getInventory().getInventoryItemCount(itemId, -1, false) > 0L) {
               return "32344-01.htm";
            }
         }

         return "32344-01a.htm";
      } else {
         return null;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      if (npc.getId() == 32344) {
         Party party = player.getParty();
         if (party == null) {
            return "32344-03.htm";
         }

         boolean[] haveItems = new boolean[]{false, false, false, false, false};

         for(Player pl : party.getMembers()) {
            if (pl != null) {
               for(int i = 0; i < REWARDS.length; ++i) {
                  if (pl.getInventory().getInventoryItemCount(REWARDS[i], -1, false) > 0L && Util.checkIfInRange(300, pl, npc, true)) {
                     haveItems[i] = true;
                     break;
                  }
               }
            }
         }

         int medalsCount = 0;

         for(boolean haveItem : haveItems) {
            if (haveItem) {
               ++medalsCount;
            }
         }

         if (medalsCount == 0) {
            return "32344-03.htm";
         }

         if (medalsCount < 5) {
            return "32344-02.htm";
         }

         for(Player pl : party.getMembers()) {
            if (pl != null && Util.checkIfInRange(6000, pl, npc, false)) {
               pl.teleToLocation(26612, 248567, -2856, true);
            }
         }
      }

      return null;
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      if (event.equalsIgnoreCase("disable_zone")) {
         DamageZone dmgZone = (DamageZone)ZoneManager.getInstance().getZoneById(200011);
         if (dmgZone != null) {
            dmgZone.setEnabled(false);
         }
      } else if (event.equalsIgnoreCase("cube_68_spawn")) {
         Npc spawnedNpc = addSpawn(32467, 12527, 279714, -11622, 16384, false, 0L, false);
         this.startQuestTimer("cube_68_despawn", 600000L, spawnedNpc, null);
      } else if (event.equalsIgnoreCase("end_7th_floor_attack")) {
         this.do7thFloorDespawn();
      } else if (event.equalsIgnoreCase("start_7th_floor_spawn")) {
         this.do7thFloorSpawn();
      }

      if (npc == null) {
         return null;
      } else {
         int npcId = npc.getId();
         if (event.equalsIgnoreCase("close") && TULLY_DOORLIST.containsKey(npcId)) {
            if (npcId == 18455 && npc.getX() == -14610) {
               npcId = 99999;
            }

            int[] doors = (int[])TULLY_DOORLIST.get(npcId);

            for(int doorId : doors) {
               DoorParser.getInstance().getDoor(doorId).closeMe();
            }
         }

         if (event.equalsIgnoreCase("repair_device")) {
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 23, npc.getId(), NpcStringId.DE_ACTIVATE_THE_ALARM));
            brokenContraptions.remove(npc.getObjectId());
         } else if (event.equalsIgnoreCase("despawn_servant") && !npc.isDead()) {
            if (npc.getAI().getIntention() != CtrlIntention.ATTACK && npc.getAI().getIntention() != CtrlIntention.CAST && npc.getCurrentHp() == npc.getMaxHp()
               )
             {
               npc.deleteMe();
               this.allowServantSpawn = true;
            } else {
               this.startQuestTimer("despawn_servant", 180000L, npc, null);
            }
         } else if (event.equalsIgnoreCase("despawn_agent")) {
            npc.deleteMe();
            this.allowServantSpawn = true;
            this.allowAgentSpawn = true;
         } else if (event.equalsIgnoreCase("despawn_agent_7")) {
            for(Player pl : World.getInstance().getAroundPlayers(npc, 300, 200)) {
               if (pl != null) {
                  pl.teleToLocation(-12176, 279696, -10492, true);
               }
            }

            this.allowAgentSpawn_7th = true;
            this.spawnedAgent = null;
            npc.deleteMe();
         } else if (event.equalsIgnoreCase("cube_68_despawn")) {
            for(Player pl : World.getInstance().getAroundPlayers(npc, 500, 200)) {
               if (pl != null) {
                  pl.teleToLocation(-12176, 279696, -10492, true);
               }
            }

            npc.deleteMe();
            this.startQuestTimer("start_7th_floor_spawn", 120000L, null, null);
         }

         if (player == null) {
            return null;
         } else {
            if (event.equalsIgnoreCase("enter") && npcId == 32373) {
               Party party = player.getParty();
               if (party != null && party.getLeaderObjectId() == player.getObjectId()) {
                  for(Player partyMember : party.getMembers()) {
                     if (!Util.checkIfInRange(300, partyMember, npc, true)) {
                        return "32373-02.htm";
                     }
                  }

                  for(Player partyMember : party.getMembers()) {
                     partyMember.teleToLocation(-13400, 272827, -15300, true);
                  }

                  htmltext = null;
               } else {
                  htmltext = "32373-02a.htm";
               }
            } else if (event.equalsIgnoreCase("open") && TULLY_DOORLIST.containsKey(npcId)) {
               if (npcId == 18455 && npc.getX() == -14610) {
                  npcId = 99999;
               }

               int[] doors = (int[])TULLY_DOORLIST.get(npcId);

               for(int doorId : doors) {
                  DoorParser.getInstance().getDoor(doorId).openMe();
               }

               this.startQuestTimer("close", 120000L, npc, null);
               htmltext = null;
            } else if ((event.equalsIgnoreCase("up") || event.equalsIgnoreCase("down")) && TELE_COORDS.containsKey(npcId)) {
               int direction = event.equalsIgnoreCase("up") ? 0 : 1;
               Party party = player.getParty();
               if (party == null) {
                  player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
               } else if (party.getLeaderObjectId() != player.getObjectId()) {
                  player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
               } else if (!Util.checkIfInRange(4000, player, npc, true)) {
                  player.sendPacket(SystemMessageId.TOO_FAR_FROM_NPC);
               } else {
                  int[] tele = TELE_COORDS.get(npcId)[direction];

                  for(Player partyMember : party.getMembers()) {
                     if (Util.checkIfInRange(4000, partyMember, npc, true)) {
                        partyMember.teleToLocation(tele[0], tele[1], tele[2], true);
                     }
                  }
               }

               htmltext = null;
            } else if (npcId == 32371) {
               if (event.equalsIgnoreCase("touch_device")) {
                  int i0 = talkedContraptions.contains(npc.getObjectId()) ? 0 : 1;
                  int i1 = player.getClassId().equalsOrChildOf(ClassId.maestro) ? 6 : 3;
                  if (getRandom(1000) < (i1 - i0) * 100) {
                     talkedContraptions.add(npc.getObjectId());
                     htmltext = player.getClassId().equalsOrChildOf(ClassId.maestro) ? "32371-03a.htm" : "32371-03.htm";
                  } else {
                     brokenContraptions.add(npc.getObjectId());
                     this.startQuestTimer("repair_device", 60000L, npc, null);
                     htmltext = "32371-04.htm";
                  }
               } else if (event.equalsIgnoreCase("take_reward")) {
                  boolean alreadyHaveItem = false;

                  for(int itemId : REWARDS) {
                     if (player.getInventory().getInventoryItemCount(itemId, -1, false) > 0L) {
                        alreadyHaveItem = true;
                        break;
                     }
                  }

                  if (!alreadyHaveItem && !rewardedContraptions.contains(npc.getObjectId())) {
                     int idx = postMortemSpawn.indexOf(npc);
                     if (idx > -1 && idx < 5) {
                        player.addItem("Quest", REWARDS[idx], 1L, npc, true);
                        rewardedContraptions.add(npc.getObjectId());
                        if (idx != 0) {
                           npc.deleteMe();
                        }
                     }

                     htmltext = null;
                  } else {
                     htmltext = "32371-05.htm";
                  }
               }
            } else if (npcId == 32372) {
               if (event.equalsIgnoreCase("tele_to_7th_floor") && !this.allowAgentSpawn) {
                  htmltext = null;
                  Party party = player.getParty();
                  if (party == null) {
                     player.teleToLocation(-12501, 281397, -11936, true);
                     if (this.allowAgentSpawn_7th) {
                        if (this.spawnedAgent != null) {
                           this.spawnedAgent.deleteMe();
                        }

                        this.spawnedAgent = addSpawn(32372, -12527, 279714, -11622, 16384, false, 0L, false);
                        this.allowAgentSpawn_7th = false;
                     }
                  } else if (party.getLeaderObjectId() != player.getObjectId()) {
                     player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
                  } else {
                     for(Player partyMember : party.getMembers()) {
                        if (Util.checkIfInRange(6000, partyMember, npc, true)) {
                           partyMember.teleToLocation(-12501, 281397, -11936, true);
                        }
                     }

                     if (this.allowAgentSpawn_7th) {
                        if (this.spawnedAgent != null) {
                           this.spawnedAgent.deleteMe();
                        }

                        this.spawnedAgent = addSpawn(32372, -12527, 279714, -11622, 16384, false, 0L, false);
                        this.allowAgentSpawn_7th = false;
                     }
                  }
               } else if (event.equalsIgnoreCase("buff") && !this.allowAgentSpawn_7th) {
                  htmltext = null;
                  Party party = player.getParty();
                  if (party == null) {
                     if (!Util.checkIfInRange(400, player, npc, true)) {
                        htmltext = "32372-01b.htm";
                     } else {
                        npc.setTarget(player);
                     }

                     npc.doCast(SkillsParser.getInstance().getInfo(5526, 1));
                  } else {
                     for(Player partyMember : party.getMembers()) {
                        if (!Util.checkIfInRange(400, partyMember, npc, true)) {
                           return "32372-01b.htm";
                        }
                     }

                     for(Player partyMember : party.getMembers()) {
                        npc.setTarget(partyMember);
                        npc.doCast(SkillsParser.getInstance().getInfo(5526, 1));
                     }

                     this.startQuestTimer("despawn_agent_7", 60000L, npc, null);
                  }
               } else if (event.equalsIgnoreCase("refuse") && !this.allowAgentSpawn_7th) {
                  this.allowAgentSpawn_7th = true;
                  npc.deleteMe();
                  this.spawnedAgent = null;

                  for(MonsterInstance monster : this.spawnedFollowers) {
                     if (monster != null && !monster.isDead()) {
                        if (!monster.hasMinions()) {
                           monster.getMinionList().addMinion(new MinionData(new MinionTemplate(25596, 2)), true);
                        }

                        Player target = player.getParty() == null
                           ? player
                           : player.getParty().getMembers().get(getRandom(player.getParty().getMembers().size()));
                        if (target != null && !target.isDead()) {
                           monster.addDamageHate(target, 0, 999);
                           monster.getAI().setIntention(CtrlIntention.ATTACK, target, null);
                        }
                     }
                  }

                  if (!this.is7thFloorAttackBegan) {
                     this.is7thFloorAttackBegan = true;
                     this.startQuestTimer("end_7th_floor_attack", 1200000L, null, null);
                  }
               }
            } else if (event.equalsIgnoreCase("teleport") && npcId == 32370) {
               htmltext = null;
               Party party = player.getParty();
               if (party == null) {
                  player.teleToLocation(-12176, 279696, -13596, true);
               } else {
                  if (party.getLeaderObjectId() != player.getObjectId()) {
                     player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
                     return null;
                  }

                  for(Player partyMember : party.getMembers()) {
                     if (!Util.checkIfInRange(3000, partyMember, npc, true)) {
                        return "32370-01f.htm";
                     }
                  }

                  for(Player partyMember : party.getMembers()) {
                     if (Util.checkIfInRange(6000, partyMember, npc, true)) {
                        partyMember.teleToLocation(-12176, 279696, -13596, true);
                     }
                  }
               }
            } else if (npcId == 32467 && event.startsWith("cube68_tp")) {
               htmltext = null;
               int tpId = Integer.parseInt(event.substring(10));
               Party party = player.getParty();
               if (party != null) {
                  if (party.getLeaderObjectId() != player.getObjectId()) {
                     player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
                  } else if (!Util.checkIfInRange(3000, player, npc, true)) {
                     htmltext = "32467-04.htm";
                  } else {
                     for(Player partyMember : party.getMembers()) {
                        if (Util.checkIfInRange(6000, partyMember, npc, true)) {
                           partyMember.teleToLocation(CUBE_68_TELEPORTS[tpId][0], CUBE_68_TELEPORTS[tpId][1], CUBE_68_TELEPORTS[tpId][2], true);
                        }
                     }
                  }
               } else {
                  player.teleToLocation(CUBE_68_TELEPORTS[tpId][0], CUBE_68_TELEPORTS[tpId][1], CUBE_68_TELEPORTS[tpId][2], true);
               }
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill) {
      int npcId = npc.getId();
      if (Arrays.binarySearch(TELEPORTING_MONSTERS, npcId) >= 0) {
         if (Math.abs(npc.getZ() - attacker.getZ()) > 150) {
            ((MonsterInstance)npc).clearAggroList();
            attacker.teleToLocation(npc.getX() + 50, npc.getY() - 50, npc.getZ(), true);
         }
      } else if ((npcId == 25600 || npcId == 25602) && this.spawnedFollowers.contains(npc)) {
         MonsterInstance victim1 = this.spawnedFollowers.get(1);
         MonsterInstance victim2 = this.spawnedFollowers.get(0);
         MonsterInstance actor = this.spawnedFollowers.get(2);
         if (actor != null && !actor.isDead()) {
            double transferringHp = actor.getMaxHp() * 1.0E-4;
            if (getRandom(10000) > 1500 && victim1 != null && !victim1.isDead() && actor.getCurrentHp() - transferringHp > 1.0) {
               actor.setCurrentHp(actor.getCurrentHp() - transferringHp);
               victim1.setCurrentHp(victim1.getCurrentHp() + transferringHp);
            }

            if (getRandom(10000) > 3000 && victim2 != null && !victim2.isDead() && actor.getCurrentHp() - transferringHp > 1.0) {
               actor.setCurrentHp(actor.getCurrentHp() - transferringHp);
               victim2.setCurrentHp(victim2.getCurrentHp() + transferringHp);
            }
         }
      }

      if ((npcId == 25600 || npcId == 25601) && this.spawnedFollowers.contains(npc)) {
         MonsterInstance victim = npcId == 25600 ? this.spawnedFollowers.get(1) : this.spawnedFollowers.get(2);
         MonsterInstance actor = this.spawnedFollowers.get(0);
         if (actor != null && victim != null && !actor.isDead() && !victim.isDead() && getRandom(1000) > 333) {
            actor.clearAggroList();
            actor.getAI().setIntention(CtrlIntention.ACTIVE);
            actor.setTarget(victim);
            actor.doCast(SkillsParser.getInstance().getInfo(4065, 11));
            victim.setCurrentHp(victim.getCurrentHp() + victim.getMaxHp() * 0.03);
         }
      }

      return super.onAttack(npc, attacker, damage, isSummon, skill);
   }

   @Override
   public String onFactionCall(Npc npc, Npc caller, Player attacker, boolean isSummon) {
      int npcId = npc.getId();
      if (npcId == 25600 || npcId == 25601 || npcId == 25602) {
         if (!npc.hasMinions()) {
            npc.getMinionList().addMinion(new MinionData(new MinionTemplate(25596, 2)), true);
         }

         if (!this.is7thFloorAttackBegan) {
            this.is7thFloorAttackBegan = true;
            this.startQuestTimer("end_7th_floor_attack", 1200000L, null, null);
            if (this.spawnedAgent != null) {
               this.spawnedAgent.deleteMe();
               this.spawnedAgent = null;
               this.allowAgentSpawn_7th = true;
            }
         }
      }

      return super.onFactionCall(npc, caller, attacker, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      int npcId = npc.getId();
      if (npcId == 25544) {
         for(int[] i : POST_MORTEM_SPAWNLIST) {
            Npc spawnedNpc = addSpawn(i[0], i[1], i[2], i[3], i[4], false, (long)i[5], false);
            postMortemSpawn.add(spawnedNpc);
         }

         DoorParser.getInstance().getDoor(19260051).openMe();
         DoorParser.getInstance().getDoor(19260052).openMe();
         this.countdownTime = 600000;
         this._countdown = ThreadPoolManager.getInstance().scheduleAtFixedRate(new TullyWorkshop.CountdownTask(), 60000L, 10000L);
         NpcSay ns = new NpcSay(
            postMortemSpawn.get(0).getObjectId(), 23, postMortemSpawn.get(0).getId(), NpcStringId.DETONATOR_INITIALIZATION_TIME_S1_MINUTES_FROM_NOW
         );
         ns.addStringParameter(Integer.toString(this.countdownTime / 60000));
         postMortemSpawn.get(0).broadcastPacket(ns, 2000);
      } else if (npcId == 22392 && this._countdown != null) {
         if (getRandom(1000) >= 700) {
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NpcStringId.A_FATAL_ERROR_HAS_OCCURRED), 2000);
            if (this.countdownTime > 180000) {
               this.countdownTime = Math.max(this.countdownTime - 180000, 60000);
               if (postMortemSpawn != null && postMortemSpawn.size() > 0 && postMortemSpawn.get(0) != null && postMortemSpawn.get(0).getId() == 32371) {
                  postMortemSpawn.get(0)
                     .broadcastPacket(
                        new NpcSay(
                           postMortemSpawn.get(0).getObjectId(),
                           23,
                           postMortemSpawn.get(0).getId(),
                           NpcStringId.ZZZZ_CITY_INTERFERENCE_ERROR_FORWARD_EFFECT_CREATED
                        )
                     );
               }
            }
         } else {
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NpcStringId.TIME_RIFT_DEVICE_ACTIVATION_SUCCESSFUL), 2000);
            if (this.countdownTime > 0 && this.countdownTime <= 420000) {
               this.countdownTime += 180000;
               if (postMortemSpawn != null && postMortemSpawn.size() > 0 && postMortemSpawn.get(0) != null && postMortemSpawn.get(0).getId() == 32371) {
                  postMortemSpawn.get(0)
                     .broadcastPacket(
                        new NpcSay(
                           postMortemSpawn.get(0).getObjectId(),
                           23,
                           postMortemSpawn.get(0).getId(),
                           NpcStringId.ZZZZ_CITY_INTERFERENCE_ERROR_RECURRENCE_EFFECT_CREATED
                        )
                     );
               }
            }
         }
      } else if (Arrays.binarySearch(SIN_WARDENS, npcId) >= 0) {
         int[] roomData = this.getRoomData(npc);
         if (roomData[0] >= 0 && roomData[1] >= 0) {
            this.deathCount[roomData[0]][roomData[1]]++;
            if (this.allowServantSpawn) {
               int max = 0;
               int floor = roomData[0];
               int room = -1;

               for(int i = 0; i < 4; ++i) {
                  if (this.deathCount[floor][i] > max) {
                     max = this.deathCount[floor][i];
                     room = i;
                  }
               }

               if (room >= 0 && max >= DEATH_COUNTS[floor]) {
                  int cf = floor == 1 ? 3 : 0;
                  int servantId = 22405 + this.nextServantIdx + cf;
                  int[] coords = SERVANT_COORDINATES[room + cf];
                  Npc spawnedNpc = addSpawn(servantId, coords[0], coords[1], coords[2], 0, false, 0L, false);
                  this.allowServantSpawn = false;
                  this.startQuestTimer("despawn_servant", 180000L, spawnedNpc, null);
               }
            }
         }
      } else if (npcId >= 22405 && npcId <= 22410) {
         int[] roomData = this.getRoomData(npc);
         if (roomData[0] >= 0 && roomData[1] >= 0 && this.allowAgentSpawn) {
            this.allowServantSpawn = true;
            if (this.nextServantIdx == 2) {
               this.nextServantIdx = 0;
               this.initDeathCounter(roomData[0]);
               if (RaidBossSpawnManager.getInstance().getRaidBossStatusId(25603) == RaidBossSpawnManager.StatusEnum.ALIVE) {
                  this.allowAgentSpawn = false;
                  this.allowServantSpawn = false;
                  int cf = roomData[0] == 1 ? 3 : 0;
                  int[] coords = AGENT_COORDINATES[roomData[1] + cf];
                  Npc spawnedNpc = addSpawn(32372, coords[0], coords[1], coords[2], 0, false, 0L, false);
                  this.startQuestTimer("despawn_agent", 180000L, spawnedNpc, null);
               }
            } else {
               for(int i = 0; i < 4; ++i) {
                  if (i == roomData[1]) {
                     this.deathCount[roomData[0]][i] = 0;
                  } else {
                     this.deathCount[roomData[0]][i] = (this.deathCount[roomData[0]][i] + 1) * getRandom(3);
                  }
               }

               if (getRandom(1000) > 500) {
                  ++this.nextServantIdx;
               }
            }
         }

         if (npc.getId() - 22404 != 3 && npc.getId() - 22404 != 6) {
            NpcSay ns = new NpcSay(npc.getObjectId(), 23, npc.getId(), NpcStringId.S1_ILL_BE_BACK_DONT_GET_COMFORTABLE);
            ns.addStringParameter(killer.getName());
            npc.broadcastPacket(ns);
         } else {
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 23, npc.getId(), NpcStringId.I_FAILED_PLEASE_FORGIVE_ME_DARION));
         }
      } else if ((npcId == 25600 || npcId == 25601 || npcId == 25602) && this.spawnedFollowers.contains(npc)) {
         ++this.killedFollowersCount;
         if (this.killedFollowersCount >= 3) {
            this.do7thFloorDespawn();
         }
      } else if (npcId == 25603) {
         if (this.pillarSpawn.getLastSpawn() != null) {
            this.pillarSpawn.getLastSpawn().setIsInvul(false);
         }

         this.handleDoorsOnDeath();
      } else if (npcId == 18506) {
         addSpawn(32370, npc.getX() + 30, npc.getY() - 30, npc.getZ(), 0, false, 900000L, false);
      }

      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public final String onSpawn(Npc npc) {
      if (npc.getId() == 25544) {
         for(Npc spawnedNpc : postMortemSpawn) {
            if (spawnedNpc != null) {
               spawnedNpc.deleteMe();
            }
         }

         postMortemSpawn.clear();
      } else if (npc.getId() == 25603) {
         if (this.pillarSpawn.getLastSpawn() != null) {
            this.pillarSpawn.getLastSpawn().setIsInvul(true);
         }

         this.handleDoorsOnRespawn();
      } else if (npc.getId() == 18506) {
         npc.setIsInvul(RaidBossSpawnManager.getInstance().getRaidBossStatusId(25603) == RaidBossSpawnManager.StatusEnum.ALIVE);
      }

      return super.onSpawn(npc);
   }

   @Override
   public String onSpellFinished(Npc npc, Player player, Skill skill) {
      int npcId = npc.getId();
      int skillId = skill.getId();
      if (npcId == 32372 && skillId == 5526) {
         player.teleToLocation(21935, 243923, 11088, true);
      } else if (npcId == 25600 && skillId == 5331) {
         if (!npc.isDead()) {
            npc.setCurrentHp(npc.getCurrentHp() + npc.getMaxHp() * 0.005);
         }
      } else if (npcId >= 22405 && npcId <= 22410 && skillId == 5392) {
         NpcSay ns = new NpcSay(npc.getObjectId(), 22, npc.getId(), NpcStringId.S1_THANK_YOU_FOR_GIVING_ME_YOUR_LIFE);
         ns.addStringParameter(player.getName());
         npc.broadcastPacket(ns, 2000);
         int dmg = (int)(player.getCurrentHp() / (double)(npc.getId() - 22404));
         player.reduceCurrentHp((double)dmg, null, null);
         npc.setCurrentHp(npc.getCurrentHp() + 10.0 - (double)(npc.getId() - 22404));
      }

      return null;
   }

   private int[] getRoomData(Npc npc) {
      int[] ret = new int[]{-1, -1};
      if (npc != null) {
         Spawner spawn = npc.getSpawn();
         int x = spawn.getX();
         int y = spawn.getY();
         int z = spawn.getZ();

         for(ZoneType zone : ZoneManager.getInstance().getZones(x, y, z)) {
            for(int i = 0; i < 2; ++i) {
               for(int j = 0; j < 4; ++j) {
                  if (SPAWN_ZONE_DEF[i][j] == zone.getId()) {
                     ret[0] = i;
                     ret[1] = j;
                     return ret;
                  }
               }
            }
         }
      }

      return ret;
   }

   private void initDeathCounter(int floor) {
      for(int i = 0; i < 4; ++i) {
         this.deathCount[floor][i] = getRandom(DEATH_COUNTS[floor]);
      }
   }

   private void do7thFloorSpawn() {
      this.killedFollowersCount = 0;
      this.is7thFloorAttackBegan = false;

      for(int[] data : SPAWNLIST_7TH_FLOOR) {
         MonsterInstance monster = (MonsterInstance)addSpawn(data[0], data[1], data[2], data[3], data[4], false, 0L, false);
         if (data[0] != 25600 && data[0] != 25601 && data[0] != 25602) {
            this.spawnedFollowerMinions.add(monster);
         } else {
            this.spawnedFollowers.add(monster);
         }
      }
   }

   private void do7thFloorDespawn() {
      this.cancelQuestTimers("end_7th_floor_attack");

      for(MonsterInstance monster : this.spawnedFollowers) {
         if (monster != null && !monster.isDead()) {
            monster.deleteMe();
         }
      }

      for(MonsterInstance monster : this.spawnedFollowerMinions) {
         if (monster != null && !monster.isDead()) {
            monster.deleteMe();
         }
      }

      this.spawnedFollowers.clear();
      this.spawnedFollowerMinions.clear();
      this.startQuestTimer("cube_68_spawn", 60000L, null, null);
   }

   private void doOnLoadSpawn() {
      if (RaidBossSpawnManager.getInstance().getRaidBossStatusId(25544) != RaidBossSpawnManager.StatusEnum.ALIVE) {
         for(int i = 12; i <= 13; ++i) {
            int[] data = POST_MORTEM_SPAWNLIST[i];
            Npc spawnedNpc = addSpawn(data[0], data[1], data[2], data[3], data[4], false, 0L, false);
            postMortemSpawn.add(spawnedNpc);
         }
      }

      this.pillarSpawn = addSpawn(18506, 21008, 244000, 11087, 0, false, 0L, false).getSpawn();
      this.pillarSpawn.setAmount(1);
      this.pillarSpawn.setRespawnDelay(1200);
      this.pillarSpawn.startRespawn();
      if (RaidBossSpawnManager.getInstance().getRaidBossStatusId(25603) != RaidBossSpawnManager.StatusEnum.ALIVE) {
         this.handleDoorsOnDeath();
      }
   }

   private void handleDoorsOnDeath() {
      DoorParser.getInstance().getDoor(20250005).openMe();
      DoorParser.getInstance().getDoor(20250004).openMe();
      ThreadPoolManager.getInstance().schedule(new TullyWorkshop.DoorTask(new int[]{20250006, 20250007}, (byte)0), 2000L);
      ThreadPoolManager.getInstance().schedule(new TullyWorkshop.DoorTask(new int[]{20250778}, (byte)1), 3000L);
      ThreadPoolManager.getInstance().schedule(new TullyWorkshop.DoorTask(new int[]{20250777}, (byte)1), 6000L);
      ThreadPoolManager.getInstance().schedule(new TullyWorkshop.DoorTask(new int[]{20250009, 20250008}, (byte)0), 11000L);
   }

   private void handleDoorsOnRespawn() {
      DoorParser.getInstance().getDoor(20250009).closeMe();
      DoorParser.getInstance().getDoor(20250008).closeMe();
      ThreadPoolManager.getInstance().schedule(new TullyWorkshop.DoorTask(new int[]{20250777, 20250778}, (byte)0), 1000L);
      ThreadPoolManager.getInstance().schedule(new TullyWorkshop.DoorTask(new int[]{20250005, 20250004, 20250006, 20250007}, (byte)1), 4000L);
   }

   public static void main(String[] args) {
      new TullyWorkshop(-1, TullyWorkshop.class.getSimpleName(), "hellbound");
   }

   static {
      TULLY_DOORLIST.put(18445, new int[]{19260001, 19260002});
      TULLY_DOORLIST.put(18446, new int[]{19260003});
      TULLY_DOORLIST.put(18447, new int[]{19260003, 19260004, 19260005});
      TULLY_DOORLIST.put(18448, new int[]{19260006, 19260007});
      TULLY_DOORLIST.put(18449, new int[]{19260007, 19260008});
      TULLY_DOORLIST.put(18450, new int[]{19260010});
      TULLY_DOORLIST.put(18451, new int[]{19260011, 19260012});
      TULLY_DOORLIST.put(18452, new int[]{19260009, 19260011});
      TULLY_DOORLIST.put(18453, new int[]{19260014, 19260023, 19260013});
      TULLY_DOORLIST.put(18454, new int[]{19260015, 19260023});
      TULLY_DOORLIST.put(18455, new int[]{19260016});
      TULLY_DOORLIST.put(18456, new int[]{19260017, 19260018});
      TULLY_DOORLIST.put(18457, new int[]{19260021, 19260020});
      TULLY_DOORLIST.put(18458, new int[]{19260022});
      TULLY_DOORLIST.put(18459, new int[]{19260018});
      TULLY_DOORLIST.put(18460, new int[]{19260051});
      TULLY_DOORLIST.put(18461, new int[]{19260052});
      TULLY_DOORLIST.put(99999, new int[]{19260019});
      TELE_COORDS.put(32753, new int[][]{{-12700, 273340, -13600}, {0, 0, 0}});
      TELE_COORDS.put(32754, new int[][]{{-13246, 275740, -11936}, {-12894, 273900, -15296}});
      TELE_COORDS.put(32755, new int[][]{{-12798, 273458, -10496}, {-12718, 273490, -13600}});
      TELE_COORDS.put(32756, new int[][]{{-13500, 275912, -9032}, {-13246, 275740, -11936}});
   }

   protected class CountdownTask implements Runnable {
      @Override
      public void run() {
         TullyWorkshop.this.countdownTime -= 10000;
         Npc npc = null;
         if (TullyWorkshop.postMortemSpawn != null && TullyWorkshop.postMortemSpawn.size() > 0) {
            npc = TullyWorkshop.postMortemSpawn.get(0);
         }

         if (TullyWorkshop.this.countdownTime > 60000) {
            if (TullyWorkshop.this.countdownTime % 60000 == 0 && npc != null && npc.getId() == 32371) {
               NpcSay ns = new NpcSay(npc.getObjectId(), 23, npc.getId(), NpcStringId.S1_MINUTES_REMAINING);
               ns.addStringParameter(Integer.toString(TullyWorkshop.this.countdownTime / 60000));
               npc.broadcastPacket(ns);
            }
         } else if (TullyWorkshop.this.countdownTime <= 0) {
            if (TullyWorkshop.this._countdown != null) {
               TullyWorkshop.this._countdown.cancel(false);
               TullyWorkshop.this._countdown = null;
            }

            for(Npc spawnedNpc : TullyWorkshop.postMortemSpawn) {
               if (spawnedNpc != null && (spawnedNpc.getId() == 32371 || spawnedNpc.getId() == 22392)) {
                  spawnedNpc.deleteMe();
               }
            }

            TullyWorkshop.brokenContraptions.clear();
            TullyWorkshop.rewardedContraptions.clear();
            TullyWorkshop.talkedContraptions.clear();
            DamageZone dmgZone = (DamageZone)ZoneManager.getInstance().getZoneById(200011);
            if (dmgZone != null) {
               dmgZone.setEnabled(true);
            }

            TullyWorkshop.this.startQuestTimer("disable_zone", 300000L, null, null);
         } else if (npc != null && npc.getId() == 32371) {
            NpcSay ns = new NpcSay(npc.getObjectId(), 23, npc.getId(), NpcStringId.S1_SECONDS_REMAINING);
            ns.addStringParameter(Integer.toString(TullyWorkshop.this.countdownTime / 1000));
            npc.broadcastPacket(ns);
         }
      }
   }

   private static class DoorTask implements Runnable {
      private final int[] _doorIds;
      private final byte _state;

      public DoorTask(int[] doorIds, byte state) {
         this._doorIds = doorIds;
         this._state = state;
      }

      @Override
      public void run() {
         for(int doorId : this._doorIds) {
            DoorInstance door = DoorParser.getInstance().getDoor(doorId);
            if (door != null) {
               switch(this._state) {
                  case 0:
                     door.openMe();
                     break;
                  case 1:
                     door.closeMe();
               }
            }
         }
      }
   }
}
