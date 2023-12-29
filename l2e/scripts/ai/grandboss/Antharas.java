package l2e.scripts.ai.grandboss;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.instancemanager.EpicBossManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.MountType;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.zone.type.NoRestartZone;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.EarthQuake;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SpecialCamera;
import l2e.scripts.ai.AbstractNpcAI;

public class Antharas extends AbstractNpcAI {
   private static final SkillHolder ANTH_JUMP = new SkillHolder(4106, 1);
   private static final SkillHolder ANTH_TAIL = new SkillHolder(4107, 1);
   private static final SkillHolder ANTH_FEAR = new SkillHolder(4108, 1);
   private static final SkillHolder ANTH_DEBUFF = new SkillHolder(4109, 1);
   private static final SkillHolder ANTH_MOUTH = new SkillHolder(4110, 2);
   private static final SkillHolder ANTH_BREATH = new SkillHolder(4111, 1);
   private static final SkillHolder ANTH_NORM_ATTACK = new SkillHolder(4112, 1);
   private static final SkillHolder ANTH_NORM_ATTACK_EX = new SkillHolder(4113, 1);
   private static final SkillHolder ANTH_REGEN_1 = new SkillHolder(4125, 1);
   private static final SkillHolder ANTH_REGEN_2 = new SkillHolder(4239, 1);
   private static final SkillHolder ANTH_REGEN_3 = new SkillHolder(4240, 1);
   private static final SkillHolder ANTH_REGEN_4 = new SkillHolder(4241, 1);
   private static final SkillHolder DISPEL_BOM = new SkillHolder(5042, 1);
   private static final SkillHolder ANTH_ANTI_STRIDER = new SkillHolder(4258, 1);
   private static final SkillHolder ANTH_FEAR_SHORT = new SkillHolder(5092, 1);
   private static final SkillHolder ANTH_METEOR = new SkillHolder(5093, 1);
   private static Player attacker_1 = null;
   private static Player attacker_2 = null;
   private static Player attacker_3 = null;
   private static int attacker_1_hate = 0;
   private static int attacker_2_hate = 0;
   private static int attacker_3_hate = 0;
   private final List<Spawner> _teleportCubeSpawn = new ArrayList<>();
   private final List<Npc> _teleportCube = new ArrayList<>();
   private final Map<Integer, Spawner> _monsterSpawn = new ConcurrentHashMap<>();
   private final List<Npc> _monsters = new ArrayList<>();
   private GrandBossInstance _antharas = null;
   private ScheduledFuture<?> _cubeSpawnTask = null;
   private ScheduledFuture<?> _monsterSpawnTask = null;
   private ScheduledFuture<?> _activityCheckTask = null;
   private ScheduledFuture<?> _socialTask = null;
   private ScheduledFuture<?> _mobiliseTask = null;
   private ScheduledFuture<?> _mobsSpawnTask = null;
   private ScheduledFuture<?> _moveAtRandomTask = null;
   private ScheduledFuture<?> _targetTask = null;
   private ScheduledFuture<?> _unlockTask = null;
   private long _LastAction = 0L;
   private static NoRestartZone _zone = ZoneManager.getInstance().getZoneById(70050, NoRestartZone.class);

   private Antharas(String name, String descr) {
      super(name, descr);
      this.addSpawnId(new int[]{29068});
      this.addMoveFinishedId(29070);
      this.addAggroRangeEnterId(new int[]{29070});
      this.addSpellFinishedId(new int[]{29068});
      this.addAttackId(new int[]{29068, 29070});
      this.addKillId(new int[]{29068, 29190, 29069});
      this.init();
   }

   private void init() {
      try {
         NpcTemplate template1 = NpcsParser.getInstance().getTemplate(29068);
         Spawner tempSpawn = new Spawner(template1);
         tempSpawn.setX(181323);
         tempSpawn.setY(114850);
         tempSpawn.setZ(-7623);
         tempSpawn.setHeading(32542);
         tempSpawn.setAmount(1);
         tempSpawn.setRespawnDelay(240);
         SpawnParser.getInstance().addNewSpawn(tempSpawn);
         this._monsterSpawn.put(29068, tempSpawn);
      } catch (Exception var10) {
         this._log.warning(var10.getMessage());
      }

      try {
         NpcTemplate cube = NpcsParser.getInstance().getTemplate(31859);
         if (cube != null) {
            Spawner spawnDat = new Spawner(cube);
            spawnDat.setAmount(1);
            spawnDat.setX(177615);
            spawnDat.setY(114941);
            spawnDat.setZ(-7709);
            spawnDat.setHeading(0);
            spawnDat.setRespawnDelay(60);
            spawnDat.setLocationId(0);
            SpawnParser.getInstance().addNewSpawn(spawnDat);
            this._teleportCubeSpawn.add(spawnDat);
         }
      } catch (Exception var9) {
         this._log.warning(var9.getMessage());
      }

      int status = EpicBossManager.getInstance().getBossStatus(29068);
      if (status == 2) {
         StatsSet info = EpicBossManager.getInstance().getStatsSet(29068);
         int loc_x = info.getInteger("loc_x");
         int loc_y = info.getInteger("loc_y");
         int loc_z = info.getInteger("loc_z");
         int heading = info.getInteger("heading");
         int hp = info.getInteger("currentHP");
         int mp = info.getInteger("currentMP");
         this._antharas = (GrandBossInstance)addSpawn(29068, loc_x, loc_y, loc_z, heading, false, 0L);
         EpicBossManager.getInstance().addBoss(this._antharas);
         this._antharas.setCurrentHpMp((double)hp, (double)mp);
         this._LastAction = System.currentTimeMillis();
         this._activityCheckTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Antharas.CheckActivity(), 60000L, 60000L);
      } else if (status == 1) {
         this.setAntharasSpawnTask();
      } else if (status == 3) {
         StatsSet info = EpicBossManager.getInstance().getStatsSet(29068);
         Long respawnTime = info.getLong("respawnTime");
         if (respawnTime <= System.currentTimeMillis()) {
            EpicBossManager.getInstance().setBossStatus(29068, 0, false);
            int var13 = false;
         } else {
            if (this._unlockTask != null) {
               this._unlockTask.cancel(true);
               this._unlockTask = null;
            }

            this._unlockTask = ThreadPoolManager.getInstance().schedule(new Antharas.UnlockAntharas(), respawnTime - System.currentTimeMillis());
         }
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      switch(event) {
         case "waiting":
            this.setAntharasSpawnTask();
            break;
         case "SET_REGEN":
            if (this.isFighting(npc)) {
               double hpRatio = npc.getCurrentHp() / npc.getMaxHp();
               if (hpRatio < 0.25) {
                  if (npc.getFirstEffect(ANTH_REGEN_4.getId()) == null) {
                     npc.doCast(ANTH_REGEN_4.getSkill());
                  }
               } else if (hpRatio < 0.5) {
                  if (npc.getFirstEffect(ANTH_REGEN_3.getId()) == null) {
                     npc.doCast(ANTH_REGEN_3.getSkill());
                  }
               } else if (hpRatio < 0.75) {
                  if (npc.getFirstEffect(ANTH_REGEN_2.getId()) == null) {
                     npc.doCast(ANTH_REGEN_2.getSkill());
                  }
               } else if (npc.getFirstEffect(ANTH_REGEN_1.getId()) == null) {
                  npc.doCast(ANTH_REGEN_1.getSkill());
               }

               this.startQuestTimer("SET_REGEN", 60000L, npc, null);
            }
            break;
         case "MANAGE_SKILL":
            this.manageSkills(npc);
      }

      return super.onAdvEvent(event, npc, player);
   }

   public void spawnCube() {
      if (this._mobsSpawnTask != null) {
         this._mobsSpawnTask.cancel(true);
         this._mobsSpawnTask = null;
      }

      if (this._activityCheckTask != null) {
         this._activityCheckTask.cancel(false);
         this._activityCheckTask = null;
      }

      for(Spawner spawnDat : this._teleportCubeSpawn) {
         this._teleportCube.add(spawnDat.doSpawn());
      }
   }

   public void setAntharasSpawnTask() {
      if (this._monsterSpawnTask == null) {
         synchronized(this) {
            if (this._monsterSpawnTask == null) {
               EpicBossManager.getInstance().setBossStatus(29068, 1, true);
               this._monsterSpawnTask = ThreadPoolManager.getInstance().schedule(new Antharas.AntharasSpawn(1), (long)(Config.ANTHARAS_WAIT_TIME * 60000));
            }
         }
      }
   }

   protected void startMinionSpawns() {
      this._mobsSpawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Antharas.MobsSpawn(), 120000L, 120000L);
   }

   protected void broadcastPacket(GameServerPacket mov) {
      if (_zone != null) {
         for(Player player : _zone.getPlayersInside()) {
            if (player != null) {
               player.sendPacket(mov);
            }
         }
      }
   }

   @Override
   public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon) {
      npc.doCast(DISPEL_BOM.getSkill());
      npc.doDie(player);
      return super.onAggroRangeEnter(npc, player, isSummon);
   }

   @Override
   public boolean onMoveFinished(Npc npc) {
      npc.doCast(DISPEL_BOM.getSkill());
      npc.doDie(null);
      return false;
   }

   @Override
   public String onSpawn(Npc npc) {
      if (npc.getId() == 29068) {
         ((Attackable)npc).setOnKillDelay(0);
      } else {
         for(int i = 1; i <= 6; ++i) {
            Attackable bomber = (Attackable)addSpawn(29070, npc.getX(), npc.getY(), npc.getZ(), 0, true, 15000L, true);
            bomber.getAI().setIntention(CtrlIntention.MOVING, new Location(bomber.getX() + 100, bomber.getY() + 30, npc.getZ()));
         }

         npc.deleteMe();
      }

      return super.onSpawn(npc);
   }

   @Override
   public String onSpellFinished(Npc npc, Player player, Skill skill) {
      this.startQuestTimer("MANAGE_SKILL", 1000L, npc, null);
      return super.onSpellFinished(npc, player, skill);
   }

   public void setUnspawn() {
      for(Player player : _zone.getPlayersInside()) {
         if (player != null) {
            player.teleToLocation(79800 + getRandom(600), 151200 + getRandom(1100), -3534, true);
         }
      }

      if (this._cubeSpawnTask != null) {
         this._cubeSpawnTask.cancel(true);
         this._cubeSpawnTask = null;
      }

      if (this._monsterSpawnTask != null) {
         this._monsterSpawnTask.cancel(true);
         this._monsterSpawnTask = null;
      }

      if (this._activityCheckTask != null) {
         this._activityCheckTask.cancel(false);
         this._activityCheckTask = null;
      }

      if (this._socialTask != null) {
         this._socialTask.cancel(true);
         this._socialTask = null;
      }

      if (this._mobiliseTask != null) {
         this._mobiliseTask.cancel(true);
         this._mobiliseTask = null;
      }

      if (this._mobsSpawnTask != null) {
         this._mobsSpawnTask.cancel(true);
         this._mobsSpawnTask = null;
      }

      if (this._moveAtRandomTask != null) {
         this._moveAtRandomTask.cancel(true);
         this._moveAtRandomTask = null;
      }

      if (this._targetTask != null) {
         this._targetTask.cancel(true);
         this._targetTask = null;
      }

      for(Npc mob : this._monsters) {
         mob.getSpawn().stopRespawn();
         mob.deleteMe();
      }

      this._monsters.clear();

      for(Npc cube : this._teleportCube) {
         cube.getSpawn().stopRespawn();
         cube.deleteMe();
      }

      this._teleportCube.clear();
      this._antharas = null;
      attacker_1 = null;
      attacker_2 = null;
      attacker_3 = null;
      attacker_1_hate = 0;
      attacker_2_hate = 0;
      attacker_3_hate = 0;
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill) {
      if (npc.getId() == 29068) {
         this._LastAction = System.currentTimeMillis();
         if (this._mobsSpawnTask == null) {
            this.startMinionSpawns();
         }

         if (attacker.getMountType() == MountType.STRIDER
            && attacker.getFirstEffect(ANTH_ANTI_STRIDER.getId()) == null
            && npc.checkDoCastConditions(ANTH_ANTI_STRIDER.getSkill(), false)) {
            npc.setTarget(attacker);
            npc.doCast(ANTH_ANTI_STRIDER.getSkill());
         }

         double hpRatio = npc.getCurrentHp() / npc.getMaxHp();
         if (skill == null) {
            this.refreshAiParams(attacker, damage * 1000);
         } else if (hpRatio < 0.25) {
            this.refreshAiParams(attacker, damage * 33);
         } else if (hpRatio < 0.5) {
            this.refreshAiParams(attacker, damage * 20);
         } else if (hpRatio < 0.75) {
            this.refreshAiParams(attacker, damage * 10);
         } else {
            this.refreshAiParams(attacker, damage * 6);
         }

         this.manageSkills(npc);
      } else if (npc.getId() == 29070 && (int)Math.sqrt(npc.getDistanceSq(attacker)) < 200 && Rnd.get(100) < 5) {
         npc.doCast(DISPEL_BOM.getSkill());
         npc.doDie(attacker);
      }

      return super.onAttack(npc, attacker, damage, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (npc.getId() == 29068) {
         this.cancelQuestTimer("SET_REGEN", npc, null);
         this.broadcastPacket(new SpecialCamera(this._antharas, 1200, 20, -10, 0, 10000, 13000, 0, 0, 0, 0, 0));
         npc.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
         this._cubeSpawnTask = ThreadPoolManager.getInstance().schedule(new Antharas.CubeSpawn(0), 10000L);
         long respawnTime = EpicBossManager.getInstance().setRespawnTime(29068, Config.ANTHARAS_RESPAWN_PATTERN);
         if (this._unlockTask != null) {
            this._unlockTask.cancel(true);
            this._unlockTask = null;
         }

         this._unlockTask = ThreadPoolManager.getInstance().schedule(new Antharas.UnlockAntharas(), respawnTime - System.currentTimeMillis());
         this.broadcastPacket(new ExShowScreenMessage(NpcStringId.THE_EVIL_LAND_DRAGON_ANTHARAS_HAS_BEEN_DEFEATED, 2, 30000));
         this._antharas = null;
      }

      if (this._monsters.contains(npc)) {
         this._monsters.remove(npc);
      }

      return super.onKill(npc, killer, isSummon);
   }

   private final void refreshAiParams(Player attacker, int damage) {
      if (attacker_1 != null && attacker == attacker_1) {
         if (attacker_1_hate < damage + 1000) {
            attacker_1_hate = damage + getRandom(3000);
         }
      } else if (attacker_2 != null && attacker == attacker_2) {
         if (attacker_2_hate < damage + 1000) {
            attacker_2_hate = damage + getRandom(3000);
         }
      } else if (attacker_3 == null || attacker != attacker_3) {
         int i1 = Util.min(attacker_1_hate, attacker_2_hate, attacker_3_hate);
         if (attacker_1_hate == i1) {
            attacker_1_hate = damage + getRandom(3000);
            attacker_1 = attacker;
         } else if (attacker_2_hate == i1) {
            attacker_2_hate = damage + getRandom(3000);
            attacker_2 = attacker;
         } else if (attacker_3_hate == i1) {
            attacker_3_hate = damage + getRandom(3000);
            attacker_3 = attacker;
         }
      } else if (attacker_3_hate < damage + 1000) {
         attacker_3_hate = damage + getRandom(3000);
      }
   }

   private void manageSkills(Npc npc) {
      if (npc != null && !npc.isDead() && !npc.isCastingNow() && !npc.isCoreAIDisabled() && npc.isInCombat()) {
         int i1 = 0;
         int i2 = 0;
         Player c2 = null;
         if (attacker_1 == null || npc.calculateDistance(attacker_1, true, false) > 9000.0 || attacker_1.isDead()) {
            attacker_1_hate = 0;
         }

         if (attacker_2 == null || npc.calculateDistance(attacker_2, true, false) > 9000.0 || attacker_2.isDead()) {
            attacker_2_hate = 0;
         }

         if (attacker_3 == null || npc.calculateDistance(attacker_3, true, false) > 9000.0 || attacker_3.isDead()) {
            attacker_3_hate = 0;
         }

         if (attacker_1_hate > attacker_2_hate) {
            i1 = 2;
            i2 = attacker_1_hate;
            c2 = attacker_1;
         } else if (attacker_2_hate > 0) {
            i1 = 3;
            i2 = attacker_2_hate;
            c2 = attacker_2;
         }

         if (attacker_3_hate > i2) {
            i1 = 4;
            i2 = attacker_3_hate;
            c2 = attacker_3;
         }

         if (i2 > 0) {
            if (getRandom(100) < 70) {
               switch(i1) {
                  case 2:
                     attacker_1_hate = 500;
                     break;
                  case 3:
                     attacker_2_hate = 500;
                     break;
                  case 4:
                     attacker_3_hate = 500;
               }
            }

            double distance_c2 = npc.calculateDistance(c2, true, false);
            double direction_c2 = npc.calculateDirectionTo(c2);
            SkillHolder skillToCast = null;
            double hpRatio = npc.getCurrentHp() / npc.getMaxHp();
            if (hpRatio < 0.25) {
               if (getRandom(100) < 30) {
                  npc.setTarget(c2);
                  skillToCast = ANTH_MOUTH;
               } else if (getRandom(100) >= 80
                  || (!(distance_c2 < 1423.0) || !(direction_c2 < 188.0) || !(direction_c2 > 172.0))
                     && (!(distance_c2 < 802.0) || !(direction_c2 < 194.0) || !(direction_c2 > 166.0))) {
                  if (getRandom(100) >= 40
                     || (!(distance_c2 < 850.0) || !(direction_c2 < 210.0) || !(direction_c2 > 150.0))
                        && (!(distance_c2 < 425.0) || !(direction_c2 < 270.0) || !(direction_c2 > 90.0))) {
                     if (getRandom(100) < 10 && distance_c2 < 1100.0) {
                        skillToCast = ANTH_JUMP;
                     } else if (getRandom(100) < 10) {
                        npc.setTarget(c2);
                        skillToCast = ANTH_METEOR;
                     } else if (getRandom(100) < 6) {
                        npc.setTarget(c2);
                        skillToCast = ANTH_BREATH;
                     } else if (getRandomBoolean()) {
                        npc.setTarget(c2);
                        skillToCast = ANTH_NORM_ATTACK_EX;
                     } else if (getRandom(100) < 5) {
                        npc.setTarget(c2);
                        skillToCast = getRandomBoolean() ? ANTH_FEAR : ANTH_FEAR_SHORT;
                     } else {
                        npc.setTarget(c2);
                        skillToCast = ANTH_NORM_ATTACK;
                     }
                  } else {
                     skillToCast = ANTH_DEBUFF;
                  }
               } else {
                  skillToCast = ANTH_TAIL;
               }
            } else if (hpRatio < 0.5) {
               if (getRandom(100) >= 80
                  || (!(distance_c2 < 1423.0) || !(direction_c2 < 188.0) || !(direction_c2 > 172.0))
                     && (!(distance_c2 < 802.0) || !(direction_c2 < 194.0) || !(direction_c2 > 166.0))) {
                  if (getRandom(100) >= 40
                     || (!(distance_c2 < 850.0) || !(direction_c2 < 210.0) || !(direction_c2 > 150.0))
                        && (!(distance_c2 < 425.0) || !(direction_c2 < 270.0) || !(direction_c2 > 90.0))) {
                     if (getRandom(100) < 10 && distance_c2 < 1100.0) {
                        skillToCast = ANTH_JUMP;
                     } else if (getRandom(100) < 7) {
                        npc.setTarget(c2);
                        skillToCast = ANTH_METEOR;
                     } else if (getRandom(100) < 6) {
                        npc.setTarget(c2);
                        skillToCast = ANTH_BREATH;
                     } else if (getRandomBoolean()) {
                        npc.setTarget(c2);
                        skillToCast = ANTH_NORM_ATTACK_EX;
                     } else if (getRandom(100) < 5) {
                        npc.setTarget(c2);
                        skillToCast = getRandomBoolean() ? ANTH_FEAR : ANTH_FEAR_SHORT;
                     } else {
                        npc.setTarget(c2);
                        skillToCast = ANTH_NORM_ATTACK;
                     }
                  } else {
                     skillToCast = ANTH_DEBUFF;
                  }
               } else {
                  skillToCast = ANTH_TAIL;
               }
            } else if (hpRatio < 0.75) {
               if (getRandom(100) >= 80
                  || (!(distance_c2 < 1423.0) || !(direction_c2 < 188.0) || !(direction_c2 > 172.0))
                     && (!(distance_c2 < 802.0) || !(direction_c2 < 194.0) || !(direction_c2 > 166.0))) {
                  if (getRandom(100) < 10 && distance_c2 < 1100.0) {
                     skillToCast = ANTH_JUMP;
                  } else if (getRandom(100) < 5) {
                     npc.setTarget(c2);
                     skillToCast = ANTH_METEOR;
                  } else if (getRandom(100) < 6) {
                     npc.setTarget(c2);
                     skillToCast = ANTH_BREATH;
                  } else if (getRandomBoolean()) {
                     npc.setTarget(c2);
                     skillToCast = ANTH_NORM_ATTACK_EX;
                  } else if (getRandom(100) < 5) {
                     npc.setTarget(c2);
                     skillToCast = getRandomBoolean() ? ANTH_FEAR : ANTH_FEAR_SHORT;
                  } else {
                     npc.setTarget(c2);
                     skillToCast = ANTH_NORM_ATTACK;
                  }
               } else {
                  skillToCast = ANTH_TAIL;
               }
            } else if (getRandom(100) >= 80
               || (!(distance_c2 < 1423.0) || !(direction_c2 < 188.0) || !(direction_c2 > 172.0))
                  && (!(distance_c2 < 802.0) || !(direction_c2 < 194.0) || !(direction_c2 > 166.0))) {
               if (getRandom(100) < 3) {
                  npc.setTarget(c2);
                  skillToCast = ANTH_METEOR;
               } else if (getRandom(100) < 6) {
                  npc.setTarget(c2);
                  skillToCast = ANTH_BREATH;
               } else if (getRandomBoolean()) {
                  npc.setTarget(c2);
                  skillToCast = ANTH_NORM_ATTACK_EX;
               } else if (getRandom(100) < 5) {
                  npc.setTarget(c2);
                  skillToCast = getRandomBoolean() ? ANTH_FEAR : ANTH_FEAR_SHORT;
               } else {
                  npc.setTarget(c2);
                  skillToCast = ANTH_NORM_ATTACK;
               }
            } else {
               skillToCast = ANTH_TAIL;
            }

            if (skillToCast != null && npc.checkDoCastConditions(skillToCast.getSkill(), false)) {
               npc.doCast(skillToCast.getSkill());
            }
         }
      }
   }

   private Player getRandomTarget(Npc npc) {
      List<Player> result = new ArrayList<>();

      for(Player player : _zone.getPlayersInside()) {
         if (player != null && !player.isDead()) {
            result.add(player);
         }
      }

      return result.isEmpty() ? null : result.get(getRandom(result.size()));
   }

   private boolean isFighting(Npc npc) {
      return npc.isVisible() && !npc.isDead();
   }

   @Override
   public boolean unload(boolean removeFromList) {
      if (this._antharas != null) {
         this.cancelQuestTimer("SET_REGEN", this._antharas, null);
         this._antharas.deleteMe();
         this._antharas = null;
      }

      if (this._unlockTask != null) {
         this._unlockTask.cancel(true);
         this._unlockTask = null;
      }

      this.setUnspawn();
      int status = EpicBossManager.getInstance().getBossStatus(29068);
      if (status > 0 && status < 3) {
         EpicBossManager.getInstance().setBossStatus(29068, 0, true);
      }

      return super.unload(removeFromList);
   }

   public static void main(String[] args) {
      new Antharas(Antharas.class.getSimpleName(), "ai");
   }

   private class AntharasSpawn implements Runnable {
      private int _taskId = 0;

      public AntharasSpawn(int taskId) {
         this._taskId = taskId;
      }

      @Override
      public void run() {
         Spawner antharasSpawn = null;
         switch(this._taskId) {
            case 1:
               Antharas.this._monsterSpawnTask.cancel(false);
               Antharas.this._monsterSpawnTask = null;
               antharasSpawn = Antharas.this._monsterSpawn.get(29068);
               Antharas.this._antharas = (GrandBossInstance)antharasSpawn.doSpawn();
               EpicBossManager.getInstance().addBoss(Antharas.this._antharas);
               Antharas.this._monsters.add(Antharas.this._antharas);
               Antharas.this._antharas.setIsImmobilized(true);
               EpicBossManager.getInstance().setBossStatus(29068, 2, true);
               Antharas.this._LastAction = System.currentTimeMillis();
               Antharas.this._activityCheckTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(Antharas.this.new CheckActivity(), 60000L, 60000L);
               Antharas.this.startMinionSpawns();
               if (Antharas.this._socialTask != null) {
                  Antharas.this._socialTask.cancel(true);
                  Antharas.this._socialTask = null;
               }

               Antharas.this._socialTask = ThreadPoolManager.getInstance().schedule(Antharas.this.new AntharasSpawn(2), 16L);
               break;
            case 2:
               Antharas.this.broadcastPacket(new SpecialCamera(Antharas.this._antharas, 700, 13, -19, 0, 10000, 20000, 0, 0, 0, 0, 0));
               if (Antharas.this._socialTask != null) {
                  Antharas.this._socialTask.cancel(true);
                  Antharas.this._socialTask = null;
               }

               Antharas.this._socialTask = ThreadPoolManager.getInstance().schedule(Antharas.this.new AntharasSpawn(3), 3000L);
               break;
            case 3:
               Antharas.this.broadcastPacket(new SpecialCamera(Antharas.this._antharas, 700, 13, 0, 6000, 10000, 20000, 0, 0, 0, 0, 0));
               if (Antharas.this._socialTask != null) {
                  Antharas.this._socialTask.cancel(true);
                  Antharas.this._socialTask = null;
               }

               Antharas.this._socialTask = ThreadPoolManager.getInstance().schedule(Antharas.this.new AntharasSpawn(4), 10000L);
               break;
            case 4:
               Antharas.this.broadcastPacket(new SpecialCamera(Antharas.this._antharas, 3700, 0, -3, 0, 10000, 10000, 0, 0, 0, 0, 0));
               if (Antharas.this._socialTask != null) {
                  Antharas.this._socialTask.cancel(true);
                  Antharas.this._socialTask = null;
               }

               Antharas.this._socialTask = ThreadPoolManager.getInstance().schedule(Antharas.this.new AntharasSpawn(5), 200L);
               break;
            case 5:
               Antharas.this.broadcastPacket(new SpecialCamera(Antharas.this._antharas, 1100, 0, -3, 22000, 10000, 30000, 0, 0, 0, 0, 0));
               if (Antharas.this._socialTask != null) {
                  Antharas.this._socialTask.cancel(true);
                  Antharas.this._socialTask = null;
               }

               Antharas.this._socialTask = ThreadPoolManager.getInstance().schedule(Antharas.this.new AntharasSpawn(6), 10800L);
               break;
            case 6:
               Antharas.this.broadcastPacket(new SpecialCamera(Antharas.this._antharas, 1100, 0, -3, 300, 10000, 7000, 0, 0, 0, 0, 0));
               if (Antharas.this._socialTask != null) {
                  Antharas.this._socialTask.cancel(true);
                  Antharas.this._socialTask = null;
               }

               Antharas.this._socialTask = ThreadPoolManager.getInstance().schedule(Antharas.this.new AntharasSpawn(7), 1900L);
               break;
            case 7:
               Antharas.this._antharas.abortCast();
               Antharas.this._mobiliseTask = ThreadPoolManager.getInstance().schedule(Antharas.this.new SetMobilised(Antharas.this._antharas), 16L);
               Location pos = new Location(Quest.getRandom(175000, 178500), Quest.getRandom(112400, 116000), -7707, 0);
               Antharas.this._moveAtRandomTask = ThreadPoolManager.getInstance().schedule(new Antharas.MoveAtRandom(Antharas.this._antharas, pos), 500L);
               Antharas.this._targetTask = ThreadPoolManager.getInstance()
                  .scheduleAtFixedRate(Antharas.this.new CheckTarget(Antharas.this._antharas), 60000L, 5000L);
               Antharas.this.startQuestTimer("SET_REGEN", 60000L, Antharas.this._antharas, null);
               Antharas.this.startQuestTimer("MANAGE_SKILL", 20000L, Antharas.this._antharas, null);
               if (Antharas.this._socialTask != null) {
                  Antharas.this._socialTask.cancel(true);
                  Antharas.this._socialTask = null;
               }
         }
      }
   }

   protected class CheckActivity implements Runnable {
      @Override
      public void run() {
         Long temp = System.currentTimeMillis() - Antharas.this._LastAction;
         if (temp > 900000L) {
            EpicBossManager.getInstance().setBossStatus(Antharas.this._antharas.getId(), 0, true);
            Antharas.this.cancelQuestTimer("SET_REGEN", Antharas.this._antharas, null);
            Antharas.this.setUnspawn();
         } else {
            if (Antharas.attacker_1_hate > 10) {
               Antharas.attacker_1_hate = Antharas.attacker_1_hate - Quest.getRandom(10);
            }

            if (Antharas.attacker_2_hate > 10) {
               Antharas.attacker_2_hate = Antharas.attacker_2_hate - Quest.getRandom(10);
            }

            if (Antharas.attacker_3_hate > 10) {
               Antharas.attacker_3_hate = Antharas.attacker_3_hate - Quest.getRandom(10);
            }

            Antharas.this.manageSkills(Antharas.this._antharas);
         }
      }
   }

   protected class CheckTarget implements Runnable {
      private final Npc _npc;

      public CheckTarget(Npc npc) {
         this._npc = npc;
      }

      @Override
      public void run() {
         if (!this._npc.isCastingNow() && this._npc != null && !this._npc.isDead() && this._npc.getAI().getIntention() != CtrlIntention.MOVING) {
            Player target = Antharas.this.getRandomTarget(this._npc);
            if (target != null) {
               Antharas.this.attackPlayer((Attackable)this._npc, target);
               Antharas.this.manageSkills(this._npc);
            }
         }
      }
   }

   private class CubeSpawn implements Runnable {
      private final int _type;

      public CubeSpawn(int type) {
         this._type = type;
      }

      @Override
      public void run() {
         if (this._type == 0) {
            Antharas.this.spawnCube();
            Antharas.this._cubeSpawnTask = ThreadPoolManager.getInstance().schedule(Antharas.this.new CubeSpawn(1), 1800000L);
         } else {
            Antharas.this.setUnspawn();
         }
      }
   }

   private class MobsSpawn implements Runnable {
      public MobsSpawn() {
      }

      @Override
      public void run() {
         boolean isBehemoth = Quest.getRandom(100) < 60;

         try {
            int mobNumber = isBehemoth ? 3 : 4;

            for(int i = 0; i < mobNumber && Antharas.this._monsters.size() < 100; ++i) {
               int npcId;
               if (isBehemoth) {
                  npcId = Quest.getRandomBoolean() ? 29069 : 29190;
               } else {
                  npcId = 29070;
               }

               NpcTemplate template1 = NpcsParser.getInstance().getTemplate(npcId);
               Spawner tempSpawn = new Spawner(template1);
               int tried = 0;
               boolean notFound = true;
               int x = 175000;
               int y = 112400;
               int dt = (Antharas.this._antharas.getX() - x) * (Antharas.this._antharas.getX() - x)
                  + (Antharas.this._antharas.getY() - y) * (Antharas.this._antharas.getY() - y);

               while(tried++ < 25 && notFound) {
                  int rx = Quest.getRandom(175000, 179900);
                  int ry = Quest.getRandom(112400, 116000);
                  int rdt = (Antharas.this._antharas.getX() - rx) * (Antharas.this._antharas.getX() - rx)
                     + (Antharas.this._antharas.getY() - ry) * (Antharas.this._antharas.getY() - ry);
                  if (GeoEngine.canSeeTarget(Antharas.this._antharas.getX(), Antharas.this._antharas.getY(), -7704, rx, ry, -7704) && rdt < dt) {
                     x = rx;
                     y = ry;
                     dt = rdt;
                     if (rdt <= 900000) {
                        notFound = false;
                     }
                  }
               }

               tempSpawn.setX(x);
               tempSpawn.setY(y);
               tempSpawn.setZ(-7704);
               tempSpawn.setHeading(0);
               tempSpawn.setAmount(1);
               tempSpawn.setRespawnDelay(240);
               SpawnParser.getInstance().addNewSpawn(tempSpawn);
               Antharas.this._monsters.add(tempSpawn.doSpawn());
            }
         } catch (Exception var15) {
            Antharas.this._log.warning(var15.getMessage());
         }
      }
   }

   private static class MoveAtRandom implements Runnable {
      private final Npc _npc;
      private final Location _loc;

      public MoveAtRandom(Npc npc, Location loc) {
         this._npc = npc;
         this._loc = loc;
      }

      @Override
      public void run() {
         this._npc.getAI().setIntention(CtrlIntention.MOVING, this._loc);
      }
   }

   private class SetMobilised implements Runnable {
      private final GrandBossInstance _boss;

      public SetMobilised(GrandBossInstance boss) {
         this._boss = boss;
      }

      @Override
      public void run() {
         this._boss.setIsImmobilized(false);
         if (Antharas.this._socialTask != null) {
            Antharas.this._socialTask.cancel(true);
            Antharas.this._socialTask = null;
         }
      }
   }

   private static class UnlockAntharas implements Runnable {
      private UnlockAntharas() {
      }

      @Override
      public void run() {
         EpicBossManager.getInstance().setBossStatus(29068, 0, true);

         for(Player p : World.getInstance().getAllPlayers()) {
            p.broadcastPacket(new EarthQuake(185708, 114298, -8221, 20, 10));
         }
      }
   }
}
