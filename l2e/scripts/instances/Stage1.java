package l2e.scripts.instances;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.commons.geometry.Polygon;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.instancemanager.SoDManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.MinionList;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.instance.TrapInstance;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.SpawnTerritory;
import l2e.gameserver.network.NpcStringId;
import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class Stage1 extends AbstractReflection {
   private final Map<Integer, SpawnTerritory> _spawnZoneList = new HashMap<>();
   private final Map<Integer, List<Stage1.SODSpawn>> _spawnList = new HashMap<>();
   private final List<Integer> _mustKillMobsId = new ArrayList<>();
   private static final int[] TRAP_18771_NPCS = new int[]{22541, 22544, 22541, 22544};
   private static final int[] TRAP_OTHER_NPCS = new int[]{22546, 22546, 22538, 22537};
   private static final int[] SPAWN_MOB_IDS = new int[]{22536, 22537, 22538, 22539, 22540, 22541, 22542, 22543, 22544, 22547, 22550, 22551, 22552, 22596};
   private static final int[] TIAT_MINION_IDS = new int[]{29162, 22538, 22540, 22547, 22542, 22548};
   private static final int[] ATTACKABLE_DOORS = new int[]{
      12240005,
      12240006,
      12240007,
      12240008,
      12240009,
      12240010,
      12240013,
      12240014,
      12240015,
      12240016,
      12240017,
      12240018,
      12240021,
      12240022,
      12240023,
      12240024,
      12240025,
      12240026,
      12240028,
      12240029,
      12240030
   };
   private static final int[] ENTRANCE_ROOM_DOORS = new int[]{12240001, 12240002};
   private static final int[] SQUARE_DOORS = new int[]{12240003, 12240004, 12240011, 12240012, 12240019, 12240020};
   private static final NpcStringId[] TIAT_TEXT = new NpcStringId[]{
      NpcStringId.YOULL_REGRET_CHALLENGING_ME, NpcStringId.HA_HA_YES_DIE_SLOWLY_WRITHING_IN_PAIN_AND_AGONY
   };

   public Stage1(String name, String descr) {
      super(name, descr);
      this.load();
      this.addStartNpc(32526);
      this.addTalkId(32526);
      this.addStartNpc(32601);
      this.addTalkId(32601);
      this.addAttackId(18776);
      this.addKillId(18776);
      this.addSpawnId(new int[]{18776, 18777, 29162, 18778});
      this.addKillId(18777);
      this.addKillId(18778);
      this.addAttackId(29163);
      this.addKillId(29163);
      this.addKillId(18696);
      this.addKillId(29162);
      this.addAggroRangeEnterId(new int[]{29169});

      for(int i = 18771; i <= 18774; ++i) {
         this.addTrapActionId(new int[]{i});
      }

      for(int mobId : this._mustKillMobsId) {
         this.addKillId(mobId);
      }
   }

   private void load() {
      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         File file = new File(Config.DATAPACK_ROOT + "/data/stats/npcs/spawnZones/seed_of_destruction.xml");
         if (!file.exists()) {
            _log.severe("[Seed of Destruction] Missing seed_of_destruction.xml. The quest wont work without it!");
            return;
         }

         Document doc = factory.newDocumentBuilder().parse(file);
         Node first = doc.getFirstChild();
         if (first != null && "list".equalsIgnoreCase(first.getNodeName())) {
            for(Node n = first.getFirstChild(); n != null; n = n.getNextSibling()) {
               if ("npc".equalsIgnoreCase(n.getNodeName())) {
                  for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                     if ("spawn".equalsIgnoreCase(d.getNodeName())) {
                        NamedNodeMap attrs = d.getAttributes();
                        Node att = attrs.getNamedItem("npcId");
                        if (att == null) {
                           _log.severe("[Seed of Destruction] Missing npcId in npc List, skipping");
                        } else {
                           int npcId = Integer.parseInt(attrs.getNamedItem("npcId").getNodeValue());
                           att = attrs.getNamedItem("flag");
                           if (att == null) {
                              _log.severe("[Seed of Destruction] Missing flag in npc List npcId: " + npcId + ", skipping");
                           } else {
                              int flag = Integer.parseInt(attrs.getNamedItem("flag").getNodeValue());
                              if (!this._spawnList.containsKey(flag)) {
                                 this._spawnList.put(flag, new ArrayList<>());
                              }

                              for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                                 if ("loc".equalsIgnoreCase(cd.getNodeName())) {
                                    attrs = cd.getAttributes();
                                    Stage1.SODSpawn spw = new Stage1.SODSpawn();
                                    spw.npcId = npcId;
                                    att = attrs.getNamedItem("x");
                                    if (att != null) {
                                       spw.x = Integer.parseInt(att.getNodeValue());
                                       att = attrs.getNamedItem("y");
                                       if (att != null) {
                                          spw.y = Integer.parseInt(att.getNodeValue());
                                          att = attrs.getNamedItem("z");
                                          if (att != null) {
                                             spw.z = Integer.parseInt(att.getNodeValue());
                                             att = attrs.getNamedItem("heading");
                                             if (att != null) {
                                                spw.h = Integer.parseInt(att.getNodeValue());
                                                att = attrs.getNamedItem("mustKill");
                                                if (att != null) {
                                                   spw.isNeededNextFlag = Boolean.parseBoolean(att.getNodeValue());
                                                }

                                                if (spw.isNeededNextFlag) {
                                                   this._mustKillMobsId.add(npcId);
                                                }

                                                this._spawnList.get(flag).add(spw);
                                             }
                                          }
                                       }
                                    }
                                 } else if ("zone".equalsIgnoreCase(cd.getNodeName())) {
                                    attrs = cd.getAttributes();
                                    Stage1.SODSpawn spw = new Stage1.SODSpawn();
                                    spw.npcId = npcId;
                                    spw.isZone = true;
                                    att = attrs.getNamedItem("id");
                                    if (att != null) {
                                       spw.zone = Integer.parseInt(att.getNodeValue());
                                       att = attrs.getNamedItem("count");
                                       if (att != null) {
                                          spw.count = Integer.parseInt(att.getNodeValue());
                                          att = attrs.getNamedItem("mustKill");
                                          if (att != null) {
                                             spw.isNeededNextFlag = Boolean.parseBoolean(att.getNodeValue());
                                          }

                                          if (spw.isNeededNextFlag) {
                                             this._mustKillMobsId.add(npcId);
                                          }

                                          this._spawnList.get(flag).add(spw);
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               } else if ("spawnZones".equalsIgnoreCase(n.getNodeName())) {
                  for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                     if ("zone".equalsIgnoreCase(d.getNodeName())) {
                        NamedNodeMap attrs = d.getAttributes();
                        Node att = attrs.getNamedItem("id");
                        if (att == null) {
                           _log.severe("[Seed of Destruction] Missing id in spawnZones List, skipping");
                        } else {
                           int id = Integer.parseInt(att.getNodeValue());
                           SpawnTerritory ter = new SpawnTerritory();
                           Polygon temp = new Polygon();

                           for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                              if ("add".equalsIgnoreCase(cd.getNodeName())) {
                                 attrs = cd.getAttributes();
                                 int x = Integer.parseInt(attrs.getNamedItem("x").getNodeValue());
                                 int y = Integer.parseInt(attrs.getNamedItem("y").getNodeValue());
                                 int zmin = Integer.parseInt(attrs.getNamedItem("zmin").getNodeValue());
                                 int zmax = Integer.parseInt(attrs.getNamedItem("zmax").getNodeValue());
                                 temp.add(x, y).setZmin(zmin).setZmax(zmax);
                              }
                           }

                           ter.add(temp);
                           this._spawnZoneList.put(id, ter);
                        }
                     }
                  }
               }
            }
         }
      } catch (Exception var17) {
         _log.log(Level.WARNING, "[Seed of Destruction] Could not parse data.xml file: " + var17.getMessage(), (Throwable)var17);
      }
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      if (this.enterInstance(player, npc, new Stage1.SOD1World(), 110)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         this.spawnState((Stage1.SOD1World)world);

         for(DoorInstance door : ReflectionManager.getInstance().getReflection(world.getReflectionId()).getDoors()) {
            if (Util.contains(ATTACKABLE_DOORS, door.getDoorId())) {
               door.setIsAttackableDoor(true);
            }
         }
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

   protected boolean checkKillProgress(Npc mob, Stage1.SOD1World world) {
      synchronized(world) {
         return world.npcList.remove(mob) && world.npcList.size() == 0;
      }
   }

   private void spawnFlaggedNPCs(Stage1.SOD1World world, int flag) {
      if (world.lock.tryLock()) {
         try {
            for(Stage1.SODSpawn spw : this._spawnList.get(flag)) {
               if (spw.isZone) {
                  for(int i = 0; i < spw.count; ++i) {
                     if (this._spawnZoneList.containsKey(spw.zone)) {
                        this.spawn(world, spw.npcId, 0, 0, 0, 0, spw.isNeededNextFlag, true, this._spawnZoneList.get(spw.zone));
                     }
                  }
               } else {
                  this.spawn(world, spw.npcId, spw.x, spw.y, spw.z, spw.h, spw.isNeededNextFlag, false, null);
               }
            }
         } finally {
            world.lock.unlock();
         }
      }
   }

   protected boolean spawnState(Stage1.SOD1World world) {
      if (!world.lock.tryLock()) {
         return false;
      } else {
         try {
            world.npcList.clear();
            switch(world.getStatus()) {
               case 0:
                  this.spawnFlaggedNPCs(world, 0);
                  break;
               case 1:
                  this.manageScreenMsg(world, NpcStringId.THE_ENEMIES_HAVE_ATTACKED_EVERYONE_COME_OUT_AND_FIGHT_URGH);

                  for(int i : ENTRANCE_ROOM_DOORS) {
                     world.getReflection().openDoor(i);
                  }

                  this.spawnFlaggedNPCs(world, 1);
                  break;
               case 2:
               case 3:
                  return true;
               case 4:
                  this.manageScreenMsg(world, NpcStringId.OBELISK_HAS_COLLAPSED_DONT_LET_THE_ENEMIES_JUMP_AROUND_WILDLY_ANYMORE);

                  for(int i : SQUARE_DOORS) {
                     world.getReflection().openDoor(i);
                  }

                  this.spawnFlaggedNPCs(world, 4);
                  break;
               case 5:
                  world.getReflection().openDoor(12240027);
                  this.spawnFlaggedNPCs(world, 3);
                  this.spawnFlaggedNPCs(world, 5);
                  break;
               case 6:
                  world.getReflection().openDoor(12240031);
                  break;
               case 7:
                  this.spawnFlaggedNPCs(world, 7);
                  break;
               case 8:
                  this.manageScreenMsg(world, NpcStringId.COME_OUT_WARRIORS_PROTECT_SEED_OF_DESTRUCTION);
                  world.deviceSpawnedMobCount = 0;
                  this.spawnFlaggedNPCs(world, 8);
               case 9:
            }

            world.incStatus();
            return true;
         } finally {
            world.lock.unlock();
         }
      }
   }

   protected void spawn(Stage1.SOD1World world, int npcId, int x, int y, int z, int h, boolean addToKillTable, boolean isTerrytory, SpawnTerritory ter) {
      if (npcId >= 18720 && npcId <= 18774) {
         Skill skill = null;
         if (npcId <= 18728) {
            skill = new SkillHolder(4186, 9).getSkill();
         } else if (npcId <= 18736) {
            skill = new SkillHolder(4072, 10).getSkill();
         } else if (npcId <= 18770) {
            skill = new SkillHolder(5340, 4).getSkill();
         } else {
            skill = new SkillHolder(10002, 1).getSkill();
         }

         this.addTrap(npcId, x, y, z, h, skill, world.getReflectionId());
      } else {
         Npc npc = null;
         if (isTerrytory) {
            Reflection r = ReflectionManager.getInstance().getReflection(world.getReflectionId());
            npc = addSpawn(npcId, ter, 0L, false, world.getReflectionId(), r.getGeoIndex());
         } else {
            npc = addSpawn(npcId, x, y, z, h, false, 0L, false, world.getReflectionId());
         }

         if (addToKillTable) {
            world.npcList.add(npc);
         }

         npc.setIsNoRndWalk(true);
         if (npc.isInstanceType(GameObject.InstanceType.Attackable)) {
            ((Attackable)npc).setSeeThroughSilentMove(true);
         }

         if (npcId == 29169) {
            this.startQuestTimer("DoorCheck", 10000L, npc, null);
         } else if (npcId == 18696) {
            npc.disableCoreAI(true);
            this.startQuestTimer("Spawn", 5000L, npc, null, true);
         } else if (npcId == 29163) {
            npc.setIsImmobilized(true);
            world.tiat = (MonsterInstance)npc;

            for(int i = 0; i < 5; ++i) {
               this.addMinion(world.tiat, 29162);
            }
         }
      }
   }

   private void manageScreenMsg(Stage1.SOD1World world, NpcStringId stringId) {
      for(int objId : world.getAllowed()) {
         Player player = World.getInstance().getPlayer(objId);
         if (player != null) {
            showOnScreenMsg(player, stringId, 2, 5000, new String[0]);
         }
      }
   }

   @Override
   public String onSpawn(Npc npc) {
      if (npc.getId() == 29162) {
         Player target = this.selectRndPlayer(npc);
         if (target != null) {
            npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, Integer.valueOf(30000));
         }

         return super.onSpawn(npc);
      } else {
         npc.disableCoreAI(true);
         return super.onSpawn(npc);
      }
   }

   @Override
   public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon) {
      if (!isSummon && player != null) {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(player.getReflectionId());
         if (tmpworld instanceof Stage1.SOD1World) {
            Stage1.SOD1World world = (Stage1.SOD1World)tmpworld;
            if (world.isStatus(7) && this.spawnState(world)) {
               for(int objId : world.getAllowed()) {
                  Player pl = World.getInstance().getPlayer(objId);
                  if (pl != null) {
                     pl.showQuestMovie(5);
                  }
               }

               npc.deleteMe();
            }
         }
      }

      return null;
   }

   @Override
   public String onAttack(final Npc npc, Player attacker, int damage, boolean isSummon, Skill skill) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld instanceof Stage1.SOD1World) {
         Stage1.SOD1World world = (Stage1.SOD1World)tmpworld;
         if (world.isStatus(2) && npc.getId() == 18776) {
            world.setStatus(4);
            this.spawnFlaggedNPCs(world, 3);
         } else if (world.isStatus(3) && npc.getId() == 18776) {
            world.setStatus(4);
            this.spawnFlaggedNPCs(world, 2);
         } else if (world.getStatus() <= 8 && npc.getId() == 29163) {
            if (npc.getCurrentHp() < npc.getMaxHp() / 2.0) {
               if (this.spawnState(world)) {
                  if (npc.isImmobilized()) {
                     npc.setIsImmobilized(false);
                  }

                  npc.setTarget(npc);
                  npc.setIsInvul(true);
                  npc.doCast(SkillsParser.getInstance().getInfo(5974, 1));
                  this.handleReenterTime(world);
                  ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
                     @Override
                     public void runImpl() throws Exception {
                        npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
                        npc.setIsInvul(false);
                     }
                  }, (long)SkillsParser.getInstance().getInfo(5974, 1).getHitTime());
               }
            } else if (world.lastFactionNotifyTime < System.currentTimeMillis()) {
               for(Npc mob : World.getInstance().getAroundNpc(npc, (int)(4000.0 + npc.getColRadius()), 200)) {
                  if (ArrayUtils.contains(TIAT_MINION_IDS, mob.getId())) {
                     mob.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(30000));
                  }
               }

               if (Rnd.chance(5)) {
                  this.manageScreenMsg(world, TIAT_TEXT[Rnd.get(TIAT_TEXT.length)]);
               }

               world.lastFactionNotifyTime = System.currentTimeMillis() + 10000L;
            }
         }
      }

      return null;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld instanceof Stage1.SOD1World) {
         Stage1.SOD1World world = (Stage1.SOD1World)tmpworld;
         if (event.equalsIgnoreCase("Spawn")) {
            if (world.deviceSpawnedMobCount < 100) {
               Attackable mob = (Attackable)addSpawn(
                  SPAWN_MOB_IDS[getRandom(SPAWN_MOB_IDS.length)],
                  npc.getSpawn().getX(),
                  npc.getSpawn().getY(),
                  npc.getSpawn().getZ(),
                  npc.getSpawn().getHeading(),
                  false,
                  0L,
                  false,
                  world.getReflectionId()
               );
               ++world.deviceSpawnedMobCount;
               mob.setSeeThroughSilentMove(true);
               mob.setRunning();
               if (world.getStatus() < 7) {
                  mob.getAI().setIntention(CtrlIntention.MOVING, new Location(-251432, 214905, -12088, 16384));
               }
            }
         } else if (event.equalsIgnoreCase("DoorCheck")) {
            DoorInstance tmp = world.getReflection().getDoor(12240030);
            if (tmp.getCurrentHp() < tmp.getMaxHp()) {
               world.deviceSpawnedMobCount = 0;
               this.spawnFlaggedNPCs(world, 6);
               this.manageScreenMsg(world, NpcStringId.ENEMIES_ARE_TRYING_TO_DESTROY_THE_FORTRESS_EVERYONE_DEFEND_THE_FORTRESS);
            } else {
               this.startQuestTimer("DoorCheck", 10000L, npc, null);
            }
         }
      }

      return "";
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      if (npc.getId() == 18696) {
         this.cancelQuestTimer("Spawn", npc, null);
         return "";
      } else {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
         if (tmpworld instanceof Stage1.SOD1World) {
            Stage1.SOD1World world = (Stage1.SOD1World)tmpworld;
            if (world.isStatus(1)) {
               if (this.checkKillProgress(npc, world)) {
                  this.spawnState(world);
               }
            } else if (world.isStatus(2)) {
               if (this.checkKillProgress(npc, world)) {
                  world.incStatus();
               }
            } else if (world.isStatus(4) && npc.getId() == 18776) {
               this.spawnState(world);
            } else if (world.isStatus(5) && npc.getId() == 18777) {
               if (this.checkKillProgress(npc, world)) {
                  this.spawnState(world);
               }
            } else if (world.isStatus(6) && npc.getId() == 18778) {
               if (this.checkKillProgress(npc, world)) {
                  this.spawnState(world);
               }
            } else if (world.getStatus() >= 7) {
               if (npc.getId() == 29163) {
                  world.incStatus();
                  ReflectionManager.getInstance().getReflection(world.getReflectionId()).cleanupNpcs();

                  for(int objId : world.getAllowed()) {
                     Player pl = World.getInstance().getPlayer(objId);
                     if (pl != null) {
                        pl.showQuestMovie(6);
                     }
                  }

                  MinionList ml = npc.getMinionList();
                  if (ml != null) {
                     ml.deleteMinions();
                  }

                  SoDManager.addTiatKill();
                  this.finishInstance(world, 900000, false);
               } else if (npc.getId() == 29162) {
                  this.addMinion(world.tiat, 29162);
               }
            }
         }

         return null;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      int npcId = npc.getId();
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (npcId == 32526) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         if (!SoDManager.isAttackStage() && (world == null || !(world instanceof Stage1.SOD1World))) {
            if (!SoDManager.isAttackStage()) {
               SoDManager.teleportIntoSeed(player);
            }
         } else {
            this.enterInstance(player, npc);
         }
      } else if (npcId == 32601) {
         this.teleportPlayer(player, new Location(-245802, 220528, -12104), player.getReflectionId(), false);
      }

      return "";
   }

   @Override
   public String onTrapAction(TrapInstance trap, Creature trigger, Quest.TrapAction action) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(trap.getReflectionId());
      if (tmpworld instanceof Stage1.SOD1World) {
         Stage1.SOD1World world = (Stage1.SOD1World)tmpworld;
         switch(action) {
            case TRAP_TRIGGERED:
               if (trap.getId() == 18771) {
                  for(int npcId : TRAP_18771_NPCS) {
                     addSpawn(npcId, trap.getX(), trap.getY(), trap.getZ(), trap.getHeading(), true, 0L, true, world.getReflectionId());
                  }
               } else {
                  for(int npcId : TRAP_OTHER_NPCS) {
                     addSpawn(npcId, trap.getX(), trap.getY(), trap.getZ(), trap.getHeading(), true, 0L, true, world.getReflectionId());
                  }
               }
         }
      }

      return null;
   }

   protected Player selectRndPlayer(Npc npc) {
      List<Player> selectPlayers = null;
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld instanceof Stage1.SOD1World) {
         Stage1.SOD1World world = (Stage1.SOD1World)tmpworld;
         if (world.getAllowed().size() > 0) {
            selectPlayers = new ArrayList<>();

            for(int objId : world.getAllowed()) {
               Player player = World.getInstance().getPlayer(objId);
               if (player != null) {
                  selectPlayers.add(player);
               }
            }
         }
      }

      return selectPlayers != null ? selectPlayers.get(Rnd.get(selectPlayers.size())) : null;
   }

   public static void main(String[] args) {
      new Stage1(Stage1.class.getSimpleName(), "instances");
   }

   private class SOD1World extends ReflectionWorld {
      public List<Npc> npcList = new CopyOnWriteArrayList<>();
      public int deviceSpawnedMobCount = 0;
      public final Lock lock = new ReentrantLock();
      public MonsterInstance tiat;
      public long lastFactionNotifyTime = 0L;

      public SOD1World() {
      }
   }

   protected static class SODSpawn {
      public boolean isZone = false;
      public boolean isNeededNextFlag = false;
      public int npcId;
      public int x = 0;
      public int y = 0;
      public int z = 0;
      public int h = 0;
      public int zone = 0;
      public int count = 0;
   }
}
