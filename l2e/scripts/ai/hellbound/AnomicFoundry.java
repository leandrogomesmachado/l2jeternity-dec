package l2e.scripts.ai.hellbound;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.SpawnTemplate;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.scripts.ai.AbstractNpcAI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class AnomicFoundry extends AbstractNpcAI {
   private static int LABORER = 22396;
   private static int FOREMAN = 22397;
   private static int LESSER_EVIL = 22398;
   private static int GREATER_EVIL = 22399;
   protected Future<?> _runTask = null;
   protected static final Map<Integer, AnomicFoundry.AnomicFoundryRoute> _anomicFoundryRoute = new ConcurrentHashMap<>();
   private final List<Npc> spawnedMobs = new ArrayList<>();
   private int respawnTime = 10000;
   private final int respawnMin = 15000;
   private final int respawnMax = 100000;
   protected final int[] _spawned = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
   private final Map<Integer, Integer> _atkIndex = new ConcurrentHashMap<>();

   public AnomicFoundry(String name, String descr) {
      super(name, descr);
      this.addAggroRangeEnterId(new int[]{LABORER});
      this.addAttackId(LESSER_EVIL);
      this.addAttackId(LABORER);
      this.addKillId(LABORER);
      this.addKillId(LESSER_EVIL);
      this.addKillId(GREATER_EVIL);
      this.addSpawnId(new int[]{LABORER});
      this.addSpawnId(new int[]{LESSER_EVIL});
      this.addSpawnId(new int[]{GREATER_EVIL});
      this.load();
      this.startQuestTimer("make_spawn_1", (long)this.respawnTime, null, null);
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("make_spawn_1")) {
         if (HellboundManager.getInstance().getLevel() >= 10) {
            if (this._runTask == null) {
               this.loadSpawns(1);
               this.loadSpawns(2);
               this.loadSpawns(3);
               ThreadPoolManager.getInstance().schedule(new Runnable() {
                  @Override
                  public void run() {
                     AnomicFoundry.this.loadSpawns(4);
                     AnomicFoundry.this.loadSpawns(5);
                     AnomicFoundry.this.loadSpawns(6);
                  }
               }, 25000L);
               ThreadPoolManager.getInstance().schedule(new Runnable() {
                  @Override
                  public void run() {
                     AnomicFoundry.this.loadSpawns(7);
                     AnomicFoundry.this.loadSpawns(8);
                     AnomicFoundry.this.loadSpawns(9);
                  }
               }, 45000L);
               this._runTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AnomicFoundry.RunTask(), 1000L, 1000L);
            }
         } else {
            if (this._runTask != null) {
               this._runTask.cancel(false);
               this._runTask = null;
            }

            for(Npc mob : this.spawnedMobs) {
               if (mob != null) {
                  mob.getSpawn().stopRespawn();
                  mob.deleteMe();
               }
            }

            this.spawnedMobs.clear();
         }

         this.startQuestTimer("make_spawn_1", (long)this.respawnTime, null, null);
      } else if (event.equalsIgnoreCase("return_laborer")) {
         if (npc != null && !npc.isDead()) {
            ((Attackable)npc).returnHome();
         }
      } else if (event.equalsIgnoreCase("reset_respawn_time")) {
         this.respawnTime = 60000;
      }

      return null;
   }

   @Override
   public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon) {
      if (getRandom(10000) < 2000) {
         requestHelp(npc, player, 500);
      }

      return super.onAggroRangeEnter(npc, player, isSummon);
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill) {
      if (npc.getId() == LESSER_EVIL) {
         AnomicFoundry.AnomicFoundryRoute group = this.getGroup(npc);
         if (group != null) {
            group._attackDirection = true;
         }
      } else if (npc.getId() == LABORER) {
         int atkIndex = this._atkIndex.containsKey(npc.getObjectId()) ? this._atkIndex.get(npc.getObjectId()) : 0;
         if (atkIndex == 0) {
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NpcStringId.ENEMY_INVASION_HURRY_UP), 2000);
            this.cancelQuestTimer("return_laborer", npc, null);
            this.startQuestTimer("return_laborer", 60000L, npc, null);
            if (this.respawnTime > 15000) {
               this.respawnTime -= 5000;
            } else if (this.respawnTime <= 15000 && this.getQuestTimer("reset_respawn_time", null, null) == null) {
               this.startQuestTimer("reset_respawn_time", 600000L, null, null);
            }
         }

         if (getRandom(10000) < 2000) {
            this._atkIndex.put(npc.getObjectId(), ++atkIndex);
            requestHelp(npc, attacker, 1000 * atkIndex);
            if (getRandom(10) < 1) {
               npc.getAI()
                  .setIntention(
                     CtrlIntention.MOVING, new Location(npc.getX() + getRandom(-800, 800), npc.getY() + getRandom(-800, 800), npc.getZ(), npc.getHeading())
                  );
            }
         }
      }

      return null;
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (npc.getId() == LESSER_EVIL || npc.getId() == GREATER_EVIL) {
         this.removeGroupId(npc, true);
      } else if (npc.getId() == LABORER) {
         if (getRandom(10000) < 8000) {
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NpcStringId.PROCESS_SHOULDNT_BE_DELAYED_BECAUSE_OF_ME), 2000);
            if (this.respawnTime < 100000) {
               this.respawnTime += 10000;
            } else if (this.respawnTime >= 100000 && this.getQuestTimer("reset_respawn_time", null, null) == null) {
               this.startQuestTimer("reset_respawn_time", 600000L, null, null);
            }
         }

         this._atkIndex.remove(npc.getObjectId());
      }

      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public final String onSpawn(Npc npc) {
      switch(npc.getId()) {
         case 22396:
            if (!npc.isTeleporting()) {
               npc.setIsNoRndWalk(true);
            }
         case 22397:
         default:
            break;
         case 22398:
         case 22399:
            ((Attackable)npc).setOnKillDelay(0);
      }

      return super.onSpawn(npc);
   }

   private static void requestHelp(Npc requester, Player agressor, int range) {
      for(Spawner npcSpawn : SpawnParser.getInstance().getSpawnData()) {
         if (npcSpawn.getId() == FOREMAN || npcSpawn.getId() == LESSER_EVIL || npcSpawn.getId() == GREATER_EVIL) {
            MonsterInstance monster = (MonsterInstance)npcSpawn.getLastSpawn();
            if (monster != null && !monster.isDead() && monster.isInsideRadius(requester, range, true, false) && agressor != null && !agressor.isDead()) {
               monster.addDamageHate(agressor, 0, 1000);
            }
         }
      }
   }

   protected void load() {
      File f = new File(Config.DATAPACK_ROOT, "data/stats/npcs/spawnZones/anomicFoundry.xml");
      if (!f.exists()) {
         this._log.severe("[Anomic Foundry]: Error! anomicFoundry.xml file is missing!");
      } else {
         try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            factory.setValidating(true);
            Document doc = factory.newDocumentBuilder().parse(f);

            for(Node n = doc.getDocumentElement().getFirstChild(); n != null; n = n.getNextSibling()) {
               if ("route".equalsIgnoreCase(n.getNodeName())) {
                  int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
                  AnomicFoundry.AnomicFoundryRoute group = new AnomicFoundry.AnomicFoundryRoute(id);
                  group._pathRoutes = new TreeMap<>();

                  for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                     if ("pathRoute".equalsIgnoreCase(d.getNodeName())) {
                        int order = Integer.parseInt(d.getAttributes().getNamedItem("position").getNodeValue());
                        int x = Integer.parseInt(d.getAttributes().getNamedItem("locX").getNodeValue());
                        int y = Integer.parseInt(d.getAttributes().getNamedItem("locY").getNodeValue());
                        int z = Integer.parseInt(d.getAttributes().getNamedItem("locZ").getNodeValue());
                        Location loc = new Location(x, y, z, 0);
                        group._pathRoutes.put(order, loc);
                     }
                  }

                  _anomicFoundryRoute.put(id, group);
               }
            }
         } catch (Exception var13) {
            this._log.log(Level.WARNING, "[Anomic Foundry]: Error while loading anomicFoundry.xml file: " + var13.getMessage(), (Throwable)var13);
         }
      }
   }

   protected void loadSpawns(int groupId) {
      if (groupId < 10 || this._spawned[groupId] <= 0) {
         int npcId = 0;

         for(Integer integer : _anomicFoundryRoute.keySet()) {
            int _groupId = integer;
            if (groupId == _groupId) {
               AnomicFoundry.AnomicFoundryRoute group = _anomicFoundryRoute.get(_groupId);
               Location spawn = group._pathRoutes.firstEntry().getValue();
               if (_groupId >= 10 && !group._respawn) {
                  return;
               }

               if (groupId >= 19) {
                  npcId = GREATER_EVIL;
               } else {
                  npcId = LESSER_EVIL;
               }

               SpawnTemplate tpl = new SpawnTemplate("none", 1, 0, 0);
               tpl.addSpawnRange(new Location(spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getHeading()));
               group._anomicFoundryNpc = addSpawn(npcId, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getHeading(), false, 0L);
               group._anomicFoundryNpc.getSpawn().setSpawnTemplate(tpl);
               group._anomicFoundryNpc.getSpawn().setAmount(1);
               group._anomicFoundryNpc.getSpawn().startRespawn();
               if (_groupId >= 10) {
                  group._anomicFoundryNpc.getSpawn().setRespawnDelay(0);
                  group._anomicFoundryNpc.getSpawn().stopRespawn();
               } else {
                  group._anomicFoundryNpc.getSpawn().setRespawnDelay(60);
               }

               group._anomicFoundryNpc.setIsRunner(true);
               group._anomicFoundryNpc.setIsTeleporting(false);
               group._anomicFoundryNpc.setRunning();
               ((Attackable)group._anomicFoundryNpc).setCanReturnToSpawnPoint(false);
               this.spawnedMobs.add(group._anomicFoundryNpc);
               this._spawned[groupId]++;
               SpawnParser.getInstance().addRandomSpawnByNpc(group._anomicFoundryNpc.getSpawn(), group._anomicFoundryNpc.getTemplate());
            }
         }
      }
   }

   protected int getNextRoute(AnomicFoundry.AnomicFoundryRoute group, int currentRoute, int groupId) {
      if (group._pathRoutes.lastKey() == currentRoute) {
         group._currentRoute = 0;
         group._anomicFoundryNpc.doDie(group._anomicFoundryNpc);
         switch(groupId) {
            case 1:
               this.loadSpawns(10);
               this.loadSpawns(28);
               break;
            case 2:
               this.loadSpawns(11);
               this.loadSpawns(29);
               break;
            case 3:
               this.loadSpawns(12);
               this.loadSpawns(30);
               break;
            case 4:
               this.loadSpawns(13);
               this.loadSpawns(31);
               break;
            case 5:
               this.loadSpawns(14);
               this.loadSpawns(32);
               break;
            case 6:
               this.loadSpawns(15);
               this.loadSpawns(33);
               break;
            case 7:
               this.loadSpawns(16);
               this.loadSpawns(34);
               break;
            case 8:
               this.loadSpawns(17);
               this.loadSpawns(35);
               break;
            case 9:
               this.loadSpawns(18);
               this.loadSpawns(36);
               break;
            case 10:
               this.loadSpawns(19);
               break;
            case 11:
               this.loadSpawns(20);
               break;
            case 12:
               this.loadSpawns(21);
               break;
            case 13:
               this.loadSpawns(22);
               break;
            case 14:
               this.loadSpawns(23);
               break;
            case 15:
               this.loadSpawns(24);
               break;
            case 16:
               this.loadSpawns(25);
               break;
            case 17:
               this.loadSpawns(26);
               break;
            case 18:
               this.loadSpawns(27);
         }

         this.removeGroupId(group._anomicFoundryNpc, false);
         return 0;
      } else {
         return group._pathRoutes.higherKey(currentRoute);
      }
   }

   protected AnomicFoundry.AnomicFoundryRoute getGroup(Npc npc) {
      if (npc != null && npc.getId() == LESSER_EVIL) {
         for(AnomicFoundry.AnomicFoundryRoute group : _anomicFoundryRoute.values()) {
            if (npc.getId() == LESSER_EVIL && npc.equals(group._anomicFoundryNpc)) {
               return group;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   protected void removeGroupId(Npc npc, boolean onKill) {
      if (npc.getId() == LESSER_EVIL || npc.getId() == GREATER_EVIL) {
         for(AnomicFoundry.AnomicFoundryRoute group : _anomicFoundryRoute.values()) {
            if (npc.equals(group._anomicFoundryNpc)) {
               if (group._id >= 10) {
                  group._currentRoute = 0;
                  this.spawnedMobs.remove(npc);
                  this._spawned[group._id]--;
                  if (!onKill) {
                     SpawnParser.getInstance().removeRandomSpawnByNpc(npc);
                     npc.deleteMe();
                     group._respawn = false;
                     ThreadPoolManager.getInstance().schedule(new AnomicFoundry.RespawnTask(group._id), 60000L);
                  }
               } else {
                  group._currentRoute = 0;
               }
            }
         }
      }
   }

   protected int calculateHeading(Location fromLoc, Location toLoc) {
      return Util.calculateHeadingFrom(fromLoc.getX(), fromLoc.getY(), toLoc.getX(), toLoc.getY());
   }

   public static void main(String[] args) {
      new AnomicFoundry(AnomicFoundry.class.getSimpleName(), "hellbound");
   }

   protected class AnomicFoundryRoute {
      protected int _id;
      protected Npc _anomicFoundryNpc;
      protected int _currentRoute = 0;
      protected boolean _attackDirection = false;
      protected TreeMap<Integer, Location> _pathRoutes;
      protected boolean _respawn = true;

      protected AnomicFoundryRoute(int id) {
         this._id = id;
      }
   }

   protected class RespawnTask implements Runnable {
      private final int _groupId;

      public RespawnTask(int groupId) {
         this._groupId = groupId;
      }

      @Override
      public void run() {
         for(AnomicFoundry.AnomicFoundryRoute group : AnomicFoundry._anomicFoundryRoute.values()) {
            if (group._id == this._groupId) {
               group._respawn = true;
            }
         }
      }
   }

   protected class RunTask implements Runnable {
      @Override
      public void run() {
         for(int groupId : AnomicFoundry._anomicFoundryRoute.keySet()) {
            AnomicFoundry.AnomicFoundryRoute group = AnomicFoundry._anomicFoundryRoute.get(groupId);
            if (group._anomicFoundryNpc != null
               && !group._anomicFoundryNpc.isInCombat()
               && !group._anomicFoundryNpc.isCastingNow()
               && !group._anomicFoundryNpc.isAttackingNow()
               && !group._anomicFoundryNpc.isDead()) {
               Location oldLoc = group._pathRoutes.get(group._currentRoute);
               if (!Util.checkIfInRange(40, oldLoc.getX(), oldLoc.getY(), oldLoc.getZ(), group._anomicFoundryNpc, false)) {
                  if (!group._anomicFoundryNpc.isRunning()) {
                     group._anomicFoundryNpc.setRunning();
                  }

                  group._anomicFoundryNpc.getAI().setIntention(CtrlIntention.MOVING, oldLoc);
               } else {
                  group._currentRoute = AnomicFoundry.this.getNextRoute(group, group._currentRoute, groupId);
                  Location loc = group._pathRoutes.get(group._currentRoute);
                  int nextPathRoute;
                  if (group._attackDirection) {
                     nextPathRoute = AnomicFoundry.this.getNextRoute(group, group._currentRoute - 1, groupId);
                  } else {
                     nextPathRoute = AnomicFoundry.this.getNextRoute(group, group._currentRoute, groupId);
                  }

                  loc.setHeading(AnomicFoundry.this.calculateHeading(loc, group._pathRoutes.get(nextPathRoute)));
                  if (!group._anomicFoundryNpc.isRunning()) {
                     group._anomicFoundryNpc.setIsRunning(true);
                  }

                  group._anomicFoundryNpc.getAI().setIntention(CtrlIntention.MOVING, loc);
               }
            }
         }
      }
   }
}
