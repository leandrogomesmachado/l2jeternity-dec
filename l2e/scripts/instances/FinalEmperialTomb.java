package l2e.scripts.instances;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.commons.geometry.Polygon;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.SpawnTerritory;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.EarthQuake;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.MagicSkillCanceled;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.NpcInfo;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.SpecialCamera;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class FinalEmperialTomb extends AbstractReflection {
   private final Map<Integer, SpawnTerritory> _spawnZoneList = new HashMap<>();
   private final Map<Integer, List<FinalEmperialTomb.FETSpawn>> _spawnList = new HashMap<>();
   private final List<Integer> _mustKillMobsId = new ArrayList<>();
   protected static final FinalEmperialTomb.FrintezzaSong[] FRINTEZZASONGLIST = new FinalEmperialTomb.FrintezzaSong[]{
      new FinalEmperialTomb.FrintezzaSong(new SkillHolder(5007, 1), new SkillHolder(5008, 1), NpcStringId.REQUIEM_OF_HATRED, 5),
      new FinalEmperialTomb.FrintezzaSong(new SkillHolder(5007, 2), new SkillHolder(5008, 2), NpcStringId.RONDO_OF_SOLITUDE, 50),
      new FinalEmperialTomb.FrintezzaSong(new SkillHolder(5007, 3), new SkillHolder(5008, 3), NpcStringId.FRENETIC_TOCCATA, 70),
      new FinalEmperialTomb.FrintezzaSong(new SkillHolder(5007, 4), new SkillHolder(5008, 4), NpcStringId.FUGUE_OF_JUBILATION, 90),
      new FinalEmperialTomb.FrintezzaSong(new SkillHolder(5007, 5), new SkillHolder(5008, 5), NpcStringId.HYPNOTIC_MAZURKA, 100)
   };
   protected static final int[] FIRST_ROOM_DOORS = new int[]{17130051, 17130052, 17130053, 17130054, 17130055, 17130056, 17130057, 17130058};
   protected static final int[] SECOND_ROOM_DOORS = new int[]{
      17130061, 17130062, 17130063, 17130064, 17130065, 17130066, 17130067, 17130068, 17130069, 17130070
   };
   protected static final int[] FIRST_ROUTE_DOORS = new int[]{17130042, 17130043};
   protected static final int[] SECOND_ROUTE_DOORS = new int[]{17130045, 17130046};
   protected static final int[][] PORTRAIT_SPAWNS = new int[][]{
      {29048, -89381, -153981, -9168, 3368, -89378, -153968, -9168, 3368},
      {29048, -86234, -152467, -9168, 37656, -86261, -152492, -9168, 37656},
      {29049, -89342, -152479, -9168, -5152, -89311, -152491, -9168, -5152},
      {29049, -86189, -153968, -9168, 29456, -86217, -153956, -9168, 29456}
   };
   protected int spawnCount;

   public FinalEmperialTomb(String name, String descr) {
      super(name, descr);
      this.load();
      this.addStartNpc(new int[]{32011, 29061});
      this.addTalkId(new int[]{32011, 29061});
      this.addAttackId(new int[]{29046, 29045, 29048, 29049});
      this.addKillId(new int[]{18328, 18329, 18339, 29047, 29046, 29048, 29049, 29050, 29051});
      this.addKillId(this._mustKillMobsId);
      this.addSpellFinishedId(new int[]{18333});
      this.addSpawnId(new int[]{29045, 29046, 29047, 29048, 29049, 29050, 29051, 29059});
   }

   protected void load() {
      this.spawnCount = 0;

      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         File file = new File(Config.DATAPACK_ROOT + "/data/stats/npcs/spawnZones/final_emperial_tomb.xml");
         if (!file.exists()) {
            _log.severe("[Final Emperial Tomb] Missing final_emperial_tomb.xml. The quest wont work without it!");
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
                           _log.severe("[Final Emperial Tomb] Missing npcId in npc List, skipping");
                        } else {
                           int npcId = Integer.parseInt(attrs.getNamedItem("npcId").getNodeValue());
                           att = attrs.getNamedItem("flag");
                           if (att == null) {
                              _log.severe("[Final Emperial Tomb] Missing flag in npc List npcId: " + npcId + ", skipping");
                           } else {
                              int flag = Integer.parseInt(attrs.getNamedItem("flag").getNodeValue());
                              if (!this._spawnList.containsKey(flag)) {
                                 this._spawnList.put(flag, new ArrayList<>());
                              }

                              for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                                 if ("loc".equalsIgnoreCase(cd.getNodeName())) {
                                    attrs = cd.getAttributes();
                                    FinalEmperialTomb.FETSpawn spw = new FinalEmperialTomb.FETSpawn();
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
                                                ++this.spawnCount;
                                             }
                                          }
                                       }
                                    }
                                 } else if ("zone".equalsIgnoreCase(cd.getNodeName())) {
                                    attrs = cd.getAttributes();
                                    FinalEmperialTomb.FETSpawn spw = new FinalEmperialTomb.FETSpawn();
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
                                          ++this.spawnCount;
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
                           _log.severe("[Final Emperial Tomb] Missing id in spawnZones List, skipping");
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
         _log.log(Level.WARNING, "[Final Emperial Tomb] Could not parse final_emperial_tomb.xml file: " + var17.getMessage(), (Throwable)var17);
      }
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      if (this.enterInstance(player, npc, new FinalEmperialTomb.FETWorld(), 136)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         this.controlStatus((FinalEmperialTomb.FETWorld)world);
         if (player.getParty() == null || player.getParty().getCommandChannel() == null) {
            if (player.getInventory().getItemByItemId(8556) != null) {
               player.destroyItemByItemId(this.getName(), 8556, player.getInventory().getInventoryItemCount(8556, -1), null, true);
            }
         } else if (player.getParty().getCommandChannel() != null) {
            for(Player channelMember : player.getParty().getCommandChannel().getMembers()) {
               if (channelMember != null && channelMember.getInventory().getItemByItemId(8556) != null) {
                  channelMember.destroyItemByItemId(this.getName(), 8556, channelMember.getInventory().getInventoryItemCount(8556, -1), null, true);
               }
            }
         } else {
            for(Player member : player.getParty().getMembers()) {
               if (member != null && member.getInventory().getItemByItemId(8556) != null) {
                  member.destroyItemByItemId(this.getName(), 8556, member.getInventory().getInventoryItemCount(8556, -1), null, true);
               }
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

   protected boolean checkKillProgress(FinalEmperialTomb.FETWorld world) {
      if (ReflectionManager.getInstance().getWorld(world.getReflectionId()) != world) {
         return false;
      } else {
         if (world.npcSize < 10) {
            for(Npc npc : world.npcList) {
               if (npc != null && !npc.isInRangeZ(npc.getSpawn().getLocation(), (long)Config.MAX_PURSUE_RANGE)) {
                  npc.deleteMe();
                  --world.npcSize;
               }
            }
         }

         return world.npcSize <= 0;
      }
   }

   private void spawnFlaggedNPCs(FinalEmperialTomb.FETWorld world, int flag) {
      if (ReflectionManager.getInstance().getWorld(world.getReflectionId()) == world) {
         if (world.lock.tryLock()) {
            if (!world.npcList.isEmpty()) {
               for(Npc npc : world.npcList) {
                  if (npc != null) {
                     npc.deleteMe();
                  }
               }
            }

            world.npcList.clear();
            world.npcSize = 0;

            try {
               for(FinalEmperialTomb.FETSpawn spw : this._spawnList.get(flag)) {
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

               world.npcSize = world.npcList.size();
            } finally {
               world.lock.unlock();
            }
         }
      }
   }

   protected boolean controlStatus(FinalEmperialTomb.FETWorld world) {
      if (ReflectionManager.getInstance().getWorld(world.getReflectionId()) != world) {
         return false;
      } else if (!world.lock.tryLock()) {
         return false;
      } else {
         boolean var14;
         try {
            world.npcList.clear();
            switch(world.getStatus()) {
               case 0:
                  this.spawnFlaggedNPCs(world, 0);
                  break;
               case 1:
                  for(int doorId : FIRST_ROUTE_DOORS) {
                     world.getReflection().openDoor(doorId);
                  }

                  this.spawnFlaggedNPCs(world, world.getStatus());
                  break;
               case 2:
                  for(int doorId : SECOND_ROUTE_DOORS) {
                     world.getReflection().openDoor(doorId);
                  }

                  ThreadPoolManager.getInstance().schedule(new FinalEmperialTomb.IntroTask(world, 0), 300000L);
                  break;
               case 3:
                  if (world.songEffectTask != null) {
                     world.songEffectTask.cancel(false);
                  }

                  world.songEffectTask = null;
                  world.activeScarlet.setIsInvul(true);
                  if (world.activeScarlet.isCastingNow()) {
                     world.activeScarlet.abortCast();
                  }

                  this.handleReenterTime(world);
                  world.activeScarlet.doCast(new SkillHolder(5017, 1).getSkill());
                  ThreadPoolManager.getInstance().schedule(new FinalEmperialTomb.SongTask(world, 2), 1500L);
                  break;
               case 4:
                  if (!world.isScarletSecondStage) {
                     world.isScarletSecondStage = true;
                     world.isVideo = true;
                     this.broadCastPacket(world, new MagicSkillCanceled(world.frintezza.getObjectId()));
                     if (world.songEffectTask != null) {
                        world.songEffectTask.cancel(false);
                     }

                     world.songEffectTask = null;
                     ThreadPoolManager.getInstance().schedule(new FinalEmperialTomb.IntroTask(world, 23), 2000L);
                     ThreadPoolManager.getInstance().schedule(new FinalEmperialTomb.IntroTask(world, 24), 2100L);
                  }
                  break;
               case 5:
                  world.isVideo = true;
                  this.broadCastPacket(world, new MagicSkillCanceled(world.frintezza.getObjectId()));
                  if (world.songTask != null) {
                     world.songTask.cancel(true);
                  }

                  if (world.songEffectTask != null) {
                     world.songEffectTask.cancel(false);
                  }

                  world.songTask = null;
                  world.songEffectTask = null;
                  ThreadPoolManager.getInstance().schedule(new FinalEmperialTomb.IntroTask(world, 33), 500L);
                  break;
               case 6:
                  for(int doorId : FIRST_ROOM_DOORS) {
                     world.getReflection().openDoor(doorId);
                  }

                  for(int doorId : FIRST_ROUTE_DOORS) {
                     world.getReflection().openDoor(doorId);
                  }

                  for(int doorId : SECOND_ROUTE_DOORS) {
                     world.getReflection().openDoor(doorId);
                  }

                  for(int doorId : SECOND_ROOM_DOORS) {
                     world.getReflection().closeDoor(doorId);
                  }
            }

            world.incStatus();
            var14 = true;
         } finally {
            world.lock.unlock();
         }

         return var14;
      }
   }

   protected void spawn(
      FinalEmperialTomb.FETWorld world, int npcId, int x, int y, int z, int h, boolean addToKillTable, boolean isTerrytory, SpawnTerritory ter
   ) {
      if (ReflectionManager.getInstance().getWorld(world.getReflectionId()) == world) {
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

         if (npcId == 18328) {
            npc.disableCoreAI(true);
         }

         if (npcId == 18339) {
            ++world.darkChoirPlayerCount;
         }
      }
   }

   protected void broadCastPacket(FinalEmperialTomb.FETWorld world, GameServerPacket packet) {
      for(int objId : world.getAllowed()) {
         Player player = World.getInstance().getPlayer(objId);
         if (player != null && player.isOnline() && player.getReflectionId() == world.getReflectionId()) {
            player.sendPacket(packet);
         }
      }
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld != null && tmpworld instanceof FinalEmperialTomb.FETWorld) {
         FinalEmperialTomb.FETWorld world = (FinalEmperialTomb.FETWorld)tmpworld;
         if (ReflectionManager.getInstance().getWorld(world.getReflectionId()) != world) {
            return null;
         }

         if (npc.getId() == 29046 && world.isStatus(3) && npc.getCurrentHp() < npc.getMaxHp() * 0.8) {
            this.controlStatus(world);
         } else if (npc.getId() == 29046 && world.isStatus(4) && npc.getCurrentHp() < npc.getMaxHp() * 0.2) {
            this.controlStatus(world);
         }

         if (skill != null) {
            if ((npc.getId() == 29048 || npc.getId() == 29049) && skill.getId() == 2276) {
               npc.doDie(attacker);
            } else if (npc.getId() == 29045 && skill.getId() == 2234) {
               npc.setScriptValue(1);
               npc.setTarget(null);
               npc.getAI().setIntention(CtrlIntention.IDLE);
            }
         }
      }

      return null;
   }

   @Override
   public String onSpellFinished(Npc npc, Player player, Skill skill) {
      return skill.isSuicideAttack() ? this.onKill(npc, null, false) : super.onSpellFinished(npc, player, skill);
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld != null && tmpworld instanceof FinalEmperialTomb.FETWorld) {
         FinalEmperialTomb.FETWorld world = (FinalEmperialTomb.FETWorld)tmpworld;
         if (ReflectionManager.getInstance().getWorld(world.getReflectionId()) != world) {
            return null;
         }

         if (npc.getId() == 18328) {
            ThreadPoolManager.getInstance().schedule(new FinalEmperialTomb.StatusTask(world, 0), 2000L);
         } else if (npc.getId() == 18339) {
            --world.darkChoirPlayerCount;
            if (world.darkChoirPlayerCount < 1) {
               ThreadPoolManager.getInstance().schedule(new FinalEmperialTomb.StatusTask(world, 2), 2000L);
            }
         } else if (npc.getId() == 29046) {
            if (world.isStatus(3)) {
               this.handleReenterTime(world);
               world.setStatus(4);
               this.controlStatus(world);
            } else if (world.isStatus(4) && !world.isScarletSecondStage) {
               this.controlStatus(world);
            }
         } else if (npc.getId() == 29047) {
            if (world.demonsSpawnTask != null) {
               world.demonsSpawnTask.cancel(true);
               world.demonsSpawnTask = null;
            }

            for(Npc demon : world.demons) {
               if (demon != null) {
                  demon.deleteMe();
               }
            }

            for(Npc portrait : world.portraits.keySet()) {
               if (portrait != null) {
                  portrait.deleteMe();
               }
            }

            world.demons.clear();
            world.portraits.clear();
            this.controlStatus(world);
            this.finishInstance(world, false);
         } else if (world.getStatus() <= 2) {
            if (world.npcList.contains(npc)) {
               world.npcList.remove(npc);
               --world.npcSize;
               if (this.checkKillProgress(world)) {
                  this.controlStatus(world);
               }
            }

            if (npc.getId() == 18329 && getRandom(100) < 5) {
               ((MonsterInstance)npc).dropItem(player, 8556, 1L);
            }
         } else if (world.demons.contains(npc)) {
            world.demons.remove(npc);
         } else if (world.portraits.containsKey(npc)) {
            world.portraits.remove(npc);
         }
      }

      return "";
   }

   @Override
   public String onSpawn(Npc npc) {
      npc.setWatchDistance(4500);
      if (npc.isAttackable()) {
         ((Attackable)npc).setSeeThroughSilentMove(true);
      }

      return super.onSpawn(npc);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      int npcId = npc.getId();
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (npcId == 32011) {
         this.enterInstance(player, npc);
      } else if (npc.getId() == 29061) {
         int x = -87534 + getRandom(500);
         int y = -153048 + getRandom(500);
         if (BotFunctions.getInstance().isAutoTpByIdEnable(player)) {
            BotFunctions.getInstance().getAutoTeleportById(player, player.getLocation(), new Location(x, y, -9165), 1000);
            return null;
         }

         player.teleToLocation(x, y, -9165, true);
         return null;
      }

      return "";
   }

   public static void main(String[] args) {
      new FinalEmperialTomb(FinalEmperialTomb.class.getSimpleName(), "instances");
   }

   private class DemonSpawnTask implements Runnable {
      private final FinalEmperialTomb.FETWorld _world;

      DemonSpawnTask(FinalEmperialTomb.FETWorld world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (this._world != null) {
            if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) != this._world || this._world.portraits.isEmpty()) {
               return;
            }

            for(int i : this._world.portraits.values()) {
               if (this._world.demons.size() > 24) {
                  break;
               }

               MonsterInstance demon = (MonsterInstance)Quest.addSpawn(
                  FinalEmperialTomb.PORTRAIT_SPAWNS[i][0] + 2,
                  FinalEmperialTomb.PORTRAIT_SPAWNS[i][5],
                  FinalEmperialTomb.PORTRAIT_SPAWNS[i][6],
                  FinalEmperialTomb.PORTRAIT_SPAWNS[i][7],
                  FinalEmperialTomb.PORTRAIT_SPAWNS[i][8],
                  false,
                  0L,
                  false,
                  this._world.getReflectionId()
               );
               this._world.demons.add(demon);
            }

            this._world.demonsSpawnTask = ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new DemonSpawnTask(this._world), 20000L);
         }
      }
   }

   protected static class FETSpawn {
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

   private class FETWorld extends ReflectionWorld {
      public Lock lock = new ReentrantLock();
      public List<Npc> npcList = new CopyOnWriteArrayList<>();
      public int npcSize = 0;
      public int darkChoirPlayerCount = 0;
      public FinalEmperialTomb.FrintezzaSong OnSong = null;
      public ScheduledFuture<?> songTask = null;
      public ScheduledFuture<?> songEffectTask = null;
      public boolean isVideo = false;
      public boolean isScarletSecondStage = false;
      public Npc frintezzaDummy = null;
      public Npc overheadDummy = null;
      public Npc portraitDummy1 = null;
      public Npc portraitDummy3 = null;
      public Npc scarletDummy = null;
      public GrandBossInstance frintezza = null;
      public GrandBossInstance activeScarlet = null;
      public List<MonsterInstance> demons = new ArrayList<>();
      public Map<MonsterInstance, Integer> portraits = new ConcurrentHashMap<>();
      public int scarlet_x = 0;
      public int scarlet_y = 0;
      public int scarlet_z = 0;
      public int scarlet_h = 0;
      public int scarlet_a = 0;
      protected Future<?> demonsSpawnTask;

      public FETWorld() {
      }
   }

   private static class FrintezzaSong {
      public SkillHolder skill;
      public SkillHolder effectSkill;
      public NpcStringId songName;
      public int chance;

      public FrintezzaSong(SkillHolder sk, SkillHolder esk, NpcStringId sn, int ch) {
         this.skill = sk;
         this.effectSkill = esk;
         this.songName = sn;
         this.chance = ch;
      }
   }

   private class IntroTask implements Runnable {
      private final FinalEmperialTomb.FETWorld _world;
      private final int _status;

      IntroTask(FinalEmperialTomb.FETWorld world, int status) {
         this._world = world;
         this._status = status;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            switch(this._status) {
               case 0:
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 1), 27000L);
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 2), 30000L);
                  FinalEmperialTomb.this.broadCastPacket(this._world, new EarthQuake(-87784, -155083, -9087, 45, 27));
                  break;
               case 1:
                  for(int doorId : FinalEmperialTomb.FIRST_ROOM_DOORS) {
                     this._world.getReflection().closeDoor(doorId);
                  }

                  for(int doorId : FinalEmperialTomb.FIRST_ROUTE_DOORS) {
                     this._world.getReflection().closeDoor(doorId);
                  }

                  for(int doorId : FinalEmperialTomb.SECOND_ROOM_DOORS) {
                     this._world.getReflection().closeDoor(doorId);
                  }

                  for(int doorId : FinalEmperialTomb.SECOND_ROUTE_DOORS) {
                     this._world.getReflection().closeDoor(doorId);
                  }

                  Quest.addSpawn(29061, -87904, -141296, -9168, 0, false, 0L, false, this._world.getReflectionId());
                  break;
               case 2:
                  this._world.frintezzaDummy = Quest.addSpawn(29052, -87784, -155083, -9087, 16048, false, 0L, false, this._world.getReflectionId());
                  this._world.frintezzaDummy.setIsInvul(true);
                  this._world.frintezzaDummy.setIsImmobilized(true);
                  this._world.overheadDummy = Quest.addSpawn(29052, -87784, -153298, -9175, 16384, false, 0L, false, this._world.getReflectionId());
                  this._world.overheadDummy.setIsInvul(true);
                  this._world.overheadDummy.setIsImmobilized(true);
                  this._world.overheadDummy.setCollisionHeight(600.0);
                  FinalEmperialTomb.this.broadCastPacket(this._world, new NpcInfo.Info(this._world.overheadDummy, null));
                  this._world.portraitDummy1 = Quest.addSpawn(29052, -89566, -153168, -9165, 16048, false, 0L, false, this._world.getReflectionId());
                  this._world.portraitDummy1.setIsImmobilized(true);
                  this._world.portraitDummy1.setIsInvul(true);
                  this._world.portraitDummy3 = Quest.addSpawn(29052, -86004, -153168, -9165, 16048, false, 0L, false, this._world.getReflectionId());
                  this._world.portraitDummy3.setIsImmobilized(true);
                  this._world.portraitDummy3.setIsInvul(true);
                  this._world.scarletDummy = Quest.addSpawn(29053, -87784, -153298, -9175, 16384, false, 0L, false, this._world.getReflectionId());
                  this._world.scarletDummy.setIsInvul(true);
                  this._world.scarletDummy.setIsImmobilized(true);
                  this.stopPc();
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 3), 1000L);
                  break;
               case 3:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.overheadDummy, 0, 75, -89, 0, 100, 0, 0, 1, 0, 0));
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.overheadDummy, 0, 75, -89, 0, 100, 0, 0, 1, 0, 0));
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.overheadDummy, 300, 90, -10, 6500, 7000, 0, 0, 1, 0, 0));
                  this._world.frintezza = (GrandBossInstance)Quest.addSpawn(
                     29045, -87780, -155086, -9080, 16384, false, 0L, false, this._world.getReflectionId()
                  );
                  this._world.frintezza.setIsImmobilized(true);
                  this._world.frintezza.setIsRunner(true);
                  this._world.frintezza.setIsInvul(true);
                  this._world.frintezza.disableAllSkills();

                  for(int[] element : FinalEmperialTomb.PORTRAIT_SPAWNS) {
                     MonsterInstance demon = (MonsterInstance)Quest.addSpawn(
                        element[0] + 2, element[5], element[6], element[7], element[8], false, 0L, false, this._world.getReflectionId()
                     );
                     demon.setIsImmobilized(true);
                     demon.disableAllSkills();
                     this._world.demons.add(demon);
                  }

                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 4), 6500L);
                  break;
               case 4:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.frintezzaDummy, 1800, 90, 8, 6500, 7000, 0, 0, 1, 0, 0));
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 5), 900L);
                  break;
               case 5:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.frintezzaDummy, 140, 90, 10, 2500, 4500, 0, 0, 1, 0, 0));
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 6), 4000L);
                  break;
               case 6:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.frintezza, 40, 75, -10, 0, 1000, 0, 0, 1, 0, 0));
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.frintezza, 40, 75, -10, 0, 12000, 0, 0, 1, 0, 0));
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 7), 1350L);
                  break;
               case 7:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SocialAction(this._world.frintezza.getObjectId(), 2));
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 8), 7000L);
                  break;
               case 8:
                  this._world.frintezzaDummy.deleteMe();
                  this._world.frintezzaDummy = null;
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 9), 1000L);
                  break;
               case 9:
                  if (this._world.demons.size() >= 3) {
                     FinalEmperialTomb.this.broadCastPacket(this._world, new SocialAction(this._world.demons.get(1).getObjectId(), 1));
                     FinalEmperialTomb.this.broadCastPacket(this._world, new SocialAction(this._world.demons.get(2).getObjectId(), 1));
                  }

                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 10), 400L);
                  break;
               case 10:
                  if (this._world.demons.size() > 0) {
                     FinalEmperialTomb.this.broadCastPacket(this._world, new SocialAction(this._world.demons.get(0).getObjectId(), 1));
                     if (this._world.demons.size() > 3) {
                        FinalEmperialTomb.this.broadCastPacket(this._world, new SocialAction(this._world.demons.get(3).getObjectId(), 1));
                     }
                  }

                  this.sendPacketX(
                     new SpecialCamera(this._world.portraitDummy1, 1000, 118, 0, 0, 1000, 0, 0, 1, 0, 0),
                     new SpecialCamera(this._world.portraitDummy3, 1000, 62, 0, 0, 1000, 0, 0, 1, 0, 0),
                     -87784
                  );
                  this.sendPacketX(
                     new SpecialCamera(this._world.portraitDummy1, 1000, 118, 0, 0, 10000, 0, 0, 1, 0, 0),
                     new SpecialCamera(this._world.portraitDummy3, 1000, 62, 0, 0, 10000, 0, 0, 1, 0, 0),
                     -87784
                  );
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 11), 2000L);
                  break;
               case 11:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.frintezza, 240, 90, 0, 0, 1000, 0, 0, 1, 0, 0));
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.frintezza, 240, 90, 25, 5500, 10000, 0, 0, 1, 0, 0));
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SocialAction(this._world.frintezza.getObjectId(), 3));
                  this._world.portraitDummy1.deleteMe();
                  this._world.portraitDummy3.deleteMe();
                  this._world.portraitDummy1 = null;
                  this._world.portraitDummy3 = null;
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 12), 4500L);
                  break;
               case 12:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.frintezza, 100, 195, 35, 0, 10000, 0, 0, 1, 0, 0));
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 13), 700L);
                  break;
               case 13:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.frintezza, 100, 195, 35, 0, 10000, 0, 0, 1, 0, 0));
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 14), 1300L);
                  break;
               case 14:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new ExShowScreenMessage(NpcStringId.MOURNFUL_CHORALE_PRELUDE, 2, 5000));
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.frintezza, 120, 180, 45, 1500, 10000, 0, 0, 1, 0, 0));
                  FinalEmperialTomb.this.broadCastPacket(this._world, new MagicSkillUse(this._world.frintezza, this._world.frintezza, 5006, 1, 34000, 0));
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 15), 1500L);
                  break;
               case 15:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.frintezza, 520, 135, 45, 8000, 10000, 0, 0, 1, 0, 0));
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 16), 7500L);
                  break;
               case 16:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.frintezza, 1500, 110, 25, 10000, 13000, 0, 0, 1, 0, 0));
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 17), 9500L);
                  break;
               case 17:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.overheadDummy, 930, 160, -20, 0, 1000, 0, 0, 1, 0, 0));
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.overheadDummy, 600, 180, -25, 0, 10000, 0, 0, 1, 0, 0));
                  FinalEmperialTomb.this.broadCastPacket(this._world, new MagicSkillUse(this._world.scarletDummy, this._world.overheadDummy, 5004, 1, 5800, 0));
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 18), 5000L);
                  break;
               case 18:
                  this._world.activeScarlet = (GrandBossInstance)Quest.addSpawn(
                     29046, -87789, -153295, -9176, 16384, false, 0L, false, this._world.getReflectionId()
                  );
                  this._world.activeScarlet.setRHandId(8204);
                  this._world.activeScarlet.setIsInvul(true);
                  this._world.activeScarlet.setIsImmobilized(true);
                  this._world.activeScarlet.disableAllSkills();
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SocialAction(this._world.activeScarlet.getObjectId(), 3));
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.scarletDummy, 800, 180, 10, 1000, 10000, 0, 0, 1, 0, 0));
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 19), 2100L);
                  break;
               case 19:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.activeScarlet, 300, 60, 8, 0, 10000, 0, 0, 1, 0, 0));
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 20), 2000L);
                  break;
               case 20:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.activeScarlet, 500, 90, 10, 3000, 5000, 0, 0, 1, 0, 0));
                  this._world.songTask = ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new SongTask(this._world, 0), 100L);
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 21), 3000L);
                  break;
               case 21:
                  for(int i = 0; i < FinalEmperialTomb.PORTRAIT_SPAWNS.length; ++i) {
                     MonsterInstance portrait = (MonsterInstance)Quest.addSpawn(
                        FinalEmperialTomb.PORTRAIT_SPAWNS[i][0],
                        FinalEmperialTomb.PORTRAIT_SPAWNS[i][1],
                        FinalEmperialTomb.PORTRAIT_SPAWNS[i][2],
                        FinalEmperialTomb.PORTRAIT_SPAWNS[i][3],
                        FinalEmperialTomb.PORTRAIT_SPAWNS[i][4],
                        false,
                        0L,
                        false,
                        this._world.getReflectionId()
                     );
                     this._world.portraits.put(portrait, i);
                  }

                  this._world.overheadDummy.deleteMe();
                  this._world.scarletDummy.deleteMe();
                  this._world.overheadDummy = null;
                  this._world.scarletDummy = null;
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 22), 2000L);
                  break;
               case 22:
                  for(MonsterInstance demon : this._world.demons) {
                     demon.setIsImmobilized(false);
                     demon.enableAllSkills();
                  }

                  this._world.activeScarlet.setIsInvul(false);
                  this._world.activeScarlet.setIsImmobilized(false);
                  this._world.activeScarlet.enableAllSkills();
                  this._world.activeScarlet.setRunning();
                  this._world.activeScarlet.doCast(new SkillHolder(5004, 1).getSkill());
                  this._world.frintezza.enableAllSkills();
                  this._world.frintezza.disableCoreAI(true);
                  this._world.frintezza.setIsMortal(false);
                  this.startPc();
                  this._world.demonsSpawnTask = ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new DemonSpawnTask(this._world), 20000L);
                  break;
               case 23:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SocialAction(this._world.frintezza.getObjectId(), 4));
                  break;
               case 24:
                  this.stopPc();
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.frintezza, 250, 120, 15, 0, 1000, 0, 0, 1, 0, 0));
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.frintezza, 250, 120, 15, 0, 10000, 0, 0, 1, 0, 0));
                  this._world.activeScarlet.abortAttack();
                  this._world.activeScarlet.abortCast();
                  this._world.activeScarlet.setIsInvul(true);
                  this._world.activeScarlet.setIsImmobilized(true);
                  this._world.activeScarlet.disableAllSkills();
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 25), 7000L);
                  break;
               case 25:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new MagicSkillUse(this._world.frintezza, this._world.frintezza, 5006, 1, 34000, 0));
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.frintezza, 500, 70, 15, 3000, 10000, 0, 0, 1, 0, 0));
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 26), 3000L);
                  break;
               case 26:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.frintezza, 2500, 90, 12, 6000, 10000, 0, 0, 1, 0, 0));
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 27), 3000L);
                  break;
               case 27:
                  this._world.scarlet_x = this._world.activeScarlet.getX();
                  this._world.scarlet_y = this._world.activeScarlet.getY();
                  this._world.scarlet_z = this._world.activeScarlet.getZ();
                  this._world.scarlet_h = this._world.activeScarlet.getHeading();
                  if (this._world.scarlet_h < 32768) {
                     this._world.scarlet_a = Math.abs(180 - (int)((double)this._world.scarlet_h / 182.044444444));
                  } else {
                     this._world.scarlet_a = Math.abs(540 - (int)((double)this._world.scarlet_h / 182.044444444));
                  }

                  FinalEmperialTomb.this.broadCastPacket(
                     this._world, new SpecialCamera(this._world.activeScarlet, 250, this._world.scarlet_a, 12, 0, 1000, 0, 0, 1, 0, 0)
                  );
                  FinalEmperialTomb.this.broadCastPacket(
                     this._world, new SpecialCamera(this._world.activeScarlet, 250, this._world.scarlet_a, 12, 0, 10000, 0, 0, 1, 0, 0)
                  );
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 28), 500L);
                  break;
               case 28:
                  this._world.activeScarlet.doDie(this._world.activeScarlet);
                  FinalEmperialTomb.this.broadCastPacket(
                     this._world, new SpecialCamera(this._world.activeScarlet, 450, this._world.scarlet_a, 14, 8000, 8000, 0, 0, 1, 0, 0)
                  );
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 29), 6250L);
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 30), 7200L);
                  break;
               case 29:
                  this._world.activeScarlet.deleteMe();
                  this._world.activeScarlet = null;
                  break;
               case 30:
                  this._world.activeScarlet = (GrandBossInstance)Quest.addSpawn(
                     29047,
                     this._world.scarlet_x,
                     this._world.scarlet_y,
                     this._world.scarlet_z,
                     this._world.scarlet_h,
                     false,
                     0L,
                     false,
                     this._world.getReflectionId()
                  );
                  this._world.activeScarlet.setIsInvul(true);
                  this._world.activeScarlet.setIsImmobilized(true);
                  this._world.activeScarlet.disableAllSkills();

                  for(MonsterInstance demon : this._world.demons) {
                     demon.setIsImmobilized(true);
                     demon.disableAllSkills();
                  }

                  FinalEmperialTomb.this.broadCastPacket(
                     this._world, new SpecialCamera(this._world.activeScarlet, 450, this._world.scarlet_a, 12, 500, 14000, 0, 0, 1, 0, 0)
                  );
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 31), 8100L);
                  break;
               case 31:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SocialAction(this._world.activeScarlet.getObjectId(), 2));
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 32), 9000L);
                  break;
               case 32:
                  this.startPc();
                  this._world.activeScarlet.setIsInvul(false);
                  this._world.activeScarlet.setIsImmobilized(false);
                  this._world.activeScarlet.enableAllSkills();

                  for(MonsterInstance demon : this._world.demons) {
                     demon.setIsImmobilized(false);
                     demon.enableAllSkills();
                  }

                  this._world.isVideo = false;
                  break;
               case 33:
                  FinalEmperialTomb.this.broadCastPacket(
                     this._world, new SpecialCamera(this._world.activeScarlet, 300, this._world.scarlet_a - 180, 5, 0, 7000, 0, 0, 1, 0, 0)
                  );
                  FinalEmperialTomb.this.broadCastPacket(
                     this._world, new SpecialCamera(this._world.activeScarlet, 200, this._world.scarlet_a, 85, 4000, 10000, 0, 0, 1, 0, 0)
                  );
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 34), 7400L);
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 35), 7500L);
                  break;
               case 34:
                  this._world.frintezza.doDie(this._world.frintezza);
                  break;
               case 35:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.frintezza, 100, 120, 5, 0, 7000, 0, 0, 1, 0, 0));
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.frintezza, 100, 90, 5, 5000, 15000, 0, 0, 1, 0, 0));
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 36), 7000L);
                  break;
               case 36:
                  FinalEmperialTomb.this.broadCastPacket(this._world, new SpecialCamera(this._world.frintezza, 900, 90, 25, 7000, 10000, 0, 0, 1, 0, 0));
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new IntroTask(this._world, 37), 9000L);
                  break;
               case 37:
                  FinalEmperialTomb.this.controlStatus(this._world);
                  this._world.isVideo = false;
                  this.startPc();
            }
         }
      }

      private void stopPc() {
         for(int objId : this._world.getAllowed()) {
            Player player = World.getInstance().getPlayer(objId);
            if (player != null && player.isOnline() && player.getReflectionId() == this._world.getReflectionId()) {
               player.abortAttack();
               player.abortCast();
               player.disableAllSkills();
               player.setTarget(null);
               player.stopMove(null);
               player.setIsImmobilized(true);
               player.getAI().setIntention(CtrlIntention.IDLE);
            }
         }
      }

      private void startPc() {
         for(int objId : this._world.getAllowed()) {
            Player player = World.getInstance().getPlayer(objId);
            if (player != null && player.isOnline() && player.getReflectionId() == this._world.getReflectionId()) {
               player.enableAllSkills();
               player.setIsImmobilized(false);
            }
         }
      }

      private void sendPacketX(GameServerPacket packet1, GameServerPacket packet2, int x) {
         for(int objId : this._world.getAllowed()) {
            Player player = World.getInstance().getPlayer(objId);
            if (player != null && player.isOnline() && player.getReflectionId() == this._world.getReflectionId()) {
               if (player.getX() < x) {
                  player.sendPacket(packet1);
               } else {
                  player.sendPacket(packet2);
               }
            }
         }
      }
   }

   private class SongTask implements Runnable {
      private final FinalEmperialTomb.FETWorld _world;
      private final int _status;

      SongTask(FinalEmperialTomb.FETWorld world, int status) {
         this._world = world;
         this._status = status;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            switch(this._status) {
               case 0:
                  if (this._world.isVideo) {
                     this._world.songTask = ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new SongTask(this._world, 0), 1000L);
                  } else if (this._world.frintezza != null && !this._world.frintezza.isDead()) {
                     if (this._world.frintezza.getScriptValue() != 1) {
                        int rnd = Quest.getRandom(100);

                        for(FinalEmperialTomb.FrintezzaSong element : FinalEmperialTomb.FRINTEZZASONGLIST) {
                           if (rnd < element.chance) {
                              this._world.OnSong = element;
                              FinalEmperialTomb.this.broadCastPacket(
                                 this._world, new ExShowScreenMessage(2, -1, 2, 0, 0, 0, 0, true, 4000, false, null, element.songName, null)
                              );
                              FinalEmperialTomb.this.broadCastPacket(
                                 this._world,
                                 new MagicSkillUse(
                                    this._world.frintezza,
                                    this._world.frintezza,
                                    element.skill.getId(),
                                    element.skill.getLvl(),
                                    element.skill.getSkill().getHitTime(),
                                    0
                                 )
                              );
                              this._world.songEffectTask = ThreadPoolManager.getInstance()
                                 .schedule(FinalEmperialTomb.this.new SongTask(this._world, 1), 3000L);
                              this._world.songTask = ThreadPoolManager.getInstance()
                                 .schedule(FinalEmperialTomb.this.new SongTask(this._world, 0), (long)element.skill.getSkill().getHitTime());
                              return;
                           }
                        }
                     } else {
                        ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new SoulBreakingArrow(this._world.frintezza), 35000L);
                     }
                  }
                  break;
               case 1:
                  this._world.songEffectTask = null;
                  Skill skill = this._world.OnSong.effectSkill.getSkill();
                  if (skill == null) {
                     return;
                  }

                  if (this._world.activeScarlet == null || this._world.activeScarlet.isDead() || !this._world.activeScarlet.isVisible()) {
                     return;
                  }

                  if (this._world.isVideo) {
                     return;
                  }

                  this._world.activeScarlet.doCast(SkillsParser.getInstance().getInfo(skill.getId(), skill.getLevel()));
                  break;
               case 2:
                  this._world.activeScarlet.setRHandId(7903);
                  this._world.activeScarlet.setIsInvul(false);
            }
         }
      }
   }

   private class SoulBreakingArrow implements Runnable {
      private final Npc _npc;

      protected SoulBreakingArrow(Npc npc) {
         this._npc = npc;
      }

      @Override
      public void run() {
         this._npc.setScriptValue(0);
      }
   }

   private class StatusTask implements Runnable {
      private final FinalEmperialTomb.FETWorld _world;
      private final int _status;

      StatusTask(FinalEmperialTomb.FETWorld world, int status) {
         this._world = world;
         this._status = status;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            switch(this._status) {
               case 0:
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new StatusTask(this._world, 1), 2000L);

                  for(int doorId : FinalEmperialTomb.FIRST_ROOM_DOORS) {
                     this._world.getReflection().openDoor(doorId);
                  }
                  break;
               case 1:
                  this.addAggroToMobs();
                  break;
               case 2:
                  ThreadPoolManager.getInstance().schedule(FinalEmperialTomb.this.new StatusTask(this._world, 3), 100L);

                  for(int doorId : FinalEmperialTomb.SECOND_ROOM_DOORS) {
                     this._world.getReflection().openDoor(doorId);
                  }
                  break;
               case 3:
                  this.addAggroToMobs();
                  break;
               case 4:
                  FinalEmperialTomb.this.controlStatus(this._world);
            }
         }
      }

      private void addAggroToMobs() {
         Player target = World.getInstance().getPlayer(this._world.getAllowed().get(Quest.getRandom(this._world.getAllowed().size())));
         if (target == null || target.getReflectionId() != this._world.getReflectionId() || target.isDead() || target.isFakeDeath()) {
            for(int objId : this._world.getAllowed()) {
               target = World.getInstance().getPlayer(objId);
               if (target != null && target.getReflectionId() == this._world.getReflectionId() && !target.isDead() && !target.isFakeDeath()) {
                  break;
               }

               target = null;
            }
         }

         for(Npc mob : this._world.npcList) {
            mob.setRunning();
            if (target != null) {
               ((MonsterInstance)mob).addDamageHate(target, 0, 500);
               mob.getAI().setIntention(CtrlIntention.ATTACK, target);
            } else {
               mob.getAI().setIntention(CtrlIntention.MOVING, new Location(-87904, -141296, -9168, 0));
            }
         }
      }
   }
}
