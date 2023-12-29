package l2e.scripts.ai.grandboss;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.EpicBossManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.MountType;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.zone.type.NoRestartZone;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.EarthQuake;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.scripts.ai.AbstractNpcAI;

public class ValakasManager extends AbstractNpcAI {
   private static final Location[] TELEPORT_CUBE_LOCATIONS = new Location[]{
      new Location(214880, -116144, -1644),
      new Location(213696, -116592, -1644),
      new Location(212112, -116688, -1644),
      new Location(211184, -115472, -1664),
      new Location(210336, -114592, -1644),
      new Location(211360, -113904, -1644),
      new Location(213152, -112352, -1644),
      new Location(214032, -113232, -1644),
      new Location(214752, -114592, -1644),
      new Location(209824, -115568, -1421),
      new Location(210528, -112192, -1403),
      new Location(213120, -111136, -1408),
      new Location(215184, -111504, -1392),
      new Location(215456, -117328, -1392),
      new Location(213200, -118160, -1424)
   };
   private static List<MonsterInstance> _spawnedMinions = new ArrayList<>();
   private static NoRestartZone _zone = ZoneManager.getInstance().getZoneById(70052, NoRestartZone.class);
   private static GrandBossInstance _valakas;
   private static long _lastAttackTime = 0L;
   private static ScheduledFuture<?> _valakasSpawnTask = null;
   private static ScheduledFuture<?> _intervalEndTask = null;
   private static ScheduledFuture<?> _socialTask = null;
   private static ScheduledFuture<?> _sleepCheckTask = null;

   private ValakasManager(String name, String descr) {
      super(name, descr);
      this.registerMobs(new int[]{29028});
      StatsSet info = EpicBossManager.getInstance().getStatsSet(29028);
      int status = EpicBossManager.getInstance().getBossStatus(29028);
      if (status == 3) {
         long temp = info.getLong("respawnTime") - System.currentTimeMillis();
         if (temp > 0L) {
            _intervalEndTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.IntervalEnd(), temp);
         } else {
            EpicBossManager.getInstance().setBossStatus(29028, 0, false);
         }
      } else {
         int loc_x = info.getInteger("loc_x");
         int loc_y = info.getInteger("loc_y");
         int loc_z = info.getInteger("loc_z");
         int heading = info.getInteger("heading");
         int hp = info.getInteger("currentHP");
         int mp = info.getInteger("currentMP");
         if (status == 2) {
            _valakas = (GrandBossInstance)addSpawn(29028, loc_x, loc_y, loc_z, heading, false, 0L);
            EpicBossManager.getInstance().addBoss(_valakas);
            _valakas.setCurrentHpMp((double)hp, (double)mp);
            _valakas.setRunning();
            _lastAttackTime = System.currentTimeMillis();
            _sleepCheckTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.CheckLastAttack(), 600000L);
         } else {
            EpicBossManager.getInstance().setBossStatus(29028, 0, false);
         }
      }
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      if (npc.isBlocked()) {
         return null;
      } else {
         if (attacker.getMountType() == MountType.STRIDER) {
            Skill skill = SkillsParser.getInstance().getInfo(4258, 1);
            if (attacker.getFirstEffect(skill) == null) {
               npc.setTarget(attacker);
               npc.doCast(skill);
            }
         }

         _lastAttackTime = System.currentTimeMillis();
         return super.onAttack(npc, attacker, damage, isSummon);
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (npc.getId() == 29028) {
         ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(12), 1L);
         long respawnTime = EpicBossManager.getInstance().setRespawnTime(29028, Config.VALAKAS_RESPAWN_PATTERN);
         _intervalEndTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.IntervalEnd(), respawnTime - System.currentTimeMillis());
      }

      return super.onKill(npc, killer, isSummon);
   }

   public static void broadcastScreenMessage(NpcStringId string) {
      for(Player player : _zone.getPlayersInside()) {
         if (player != null) {
            player.sendPacket(new ExShowScreenMessage(string, 2, 8000));
         }
      }
   }

   private static void sleep() {
      setUnspawn();
      if (EpicBossManager.getInstance().getBossStatus(29028) != 0) {
         EpicBossManager.getInstance().setBossStatus(29028, 0, true);
      }
   }

   private static void setUnspawn() {
      for(Player player : _zone.getPlayersInside()) {
         if (player != null) {
            player.teleToLocation(150037 + getRandom(500), -57720 + getRandom(500), -2976, true);
         }
      }

      if (_valakas != null) {
         _valakas.deleteMe();
         _valakas = null;
      }

      for(MonsterInstance npc : _spawnedMinions) {
         if (npc != null) {
            npc.deleteMe();
         }
      }

      _spawnedMinions.clear();
      if (_valakasSpawnTask != null) {
         _valakasSpawnTask.cancel(false);
         _valakasSpawnTask = null;
      }

      if (_socialTask != null) {
         _socialTask.cancel(false);
         _socialTask = null;
      }

      if (_sleepCheckTask != null) {
         _sleepCheckTask.cancel(false);
         _sleepCheckTask = null;
      }

      if (_intervalEndTask != null) {
         _intervalEndTask.cancel(false);
         _intervalEndTask = null;
      }
   }

   private static void onValakasDie() {
      for(Location loc : TELEPORT_CUBE_LOCATIONS) {
         addSpawn(31759, loc, false, 900000L);
      }
   }

   public static synchronized void setValakasSpawnTask() {
      if (_valakasSpawnTask == null) {
         _valakasSpawnTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(1), (long)(Config.VALAKAS_WAIT_TIME * 60000));
      }
   }

   public static NoRestartZone getZone() {
      return _zone;
   }

   public static void addValakasMinion(MonsterInstance npc) {
      _spawnedMinions.add(npc);
   }

   @Override
   public boolean unload(boolean removeFromList) {
      setUnspawn();
      int status = EpicBossManager.getInstance().getBossStatus(29028);
      if (status > 0 && status < 3) {
         EpicBossManager.getInstance().setBossStatus(29028, 0, true);
      }

      return super.unload(removeFromList);
   }

   public static void main(String[] args) {
      new ValakasManager(ValakasManager.class.getSimpleName(), "ai");
   }

   private static class CheckLastAttack extends RunnableImpl {
      private CheckLastAttack() {
      }

      @Override
      public void runImpl() throws Exception {
         if (EpicBossManager.getInstance().getBossStatus(29028) == 2) {
            if (ValakasManager._lastAttackTime + 1200000L < System.currentTimeMillis()) {
               ValakasManager.sleep();
            } else {
               ValakasManager._sleepCheckTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.CheckLastAttack(), 60000L);
            }
         }
      }
   }

   private static class IntervalEnd extends RunnableImpl {
      private IntervalEnd() {
      }

      @Override
      public void runImpl() throws Exception {
         EpicBossManager.getInstance().setBossStatus(29028, 0, true);

         for(Player p : World.getInstance().getAllPlayers()) {
            p.broadcastPacket(new EarthQuake(213896, -115436, -1644, 20, 10));
         }
      }
   }

   protected static class SpawnDespawn extends RunnableImpl {
      private final int _distance = 2550;
      private final int _taskId;
      private final List<Player> _players = ValakasManager._zone.getPlayersInside();

      SpawnDespawn(int taskId) {
         this._taskId = taskId;
      }

      @Override
      public void runImpl() throws Exception {
         switch(this._taskId) {
            case 1:
               ValakasManager._valakas = (GrandBossInstance)Quest.addSpawn(29028, 212852, -114842, -1632, 833, false, 0L);
               EpicBossManager.getInstance().addBoss(ValakasManager._valakas);
               EpicBossManager.getInstance().setBossStatus(29028, 1, true);
               ValakasManager._valakas.block();
               ValakasManager._valakas.sendPacket(new PlaySound(1, "B03_A", 0, 0, 0, 0, 0));
               ValakasManager._socialTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(2), 16L);
               ValakasManager._lastAttackTime = System.currentTimeMillis();
               if (ValakasManager._valakasSpawnTask != null) {
                  ValakasManager._valakasSpawnTask.cancel(false);
                  ValakasManager._valakasSpawnTask = null;
               }
               break;
            case 2:
               ValakasManager._valakas.broadcastPacket(new SocialAction(ValakasManager._valakas.getObjectId(), 1));

               for(Player pc : this._players) {
                  if (pc.getDistance(ValakasManager._valakas) <= 2550.0) {
                     pc.enterMovieMode();
                     pc.specialCamera(ValakasManager._valakas, 1800, 180, -1, 1500, 15000, 0, 0, 1, 0);
                  } else {
                     pc.leaveMovieMode();
                  }
               }

               ValakasManager._socialTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(3), 1500L);
               break;
            case 3:
               for(Player pc : this._players) {
                  if (pc.getDistance(ValakasManager._valakas) <= 2550.0) {
                     pc.enterMovieMode();
                     pc.specialCamera(ValakasManager._valakas, 1300, 180, -5, 3000, 15000, 0, -5, 1, 0);
                  } else {
                     pc.leaveMovieMode();
                  }
               }

               ValakasManager._socialTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(4), 3300L);
               break;
            case 4:
               for(Player pc : this._players) {
                  if (pc.getDistance(ValakasManager._valakas) <= 2550.0) {
                     pc.enterMovieMode();
                     pc.specialCamera(ValakasManager._valakas, 500, 180, -8, 600, 15000, 0, 60, 1, 0);
                  } else {
                     pc.leaveMovieMode();
                  }
               }

               ValakasManager._socialTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(5), 2900L);
               break;
            case 5:
               for(Player pc : this._players) {
                  if (pc.getDistance(ValakasManager._valakas) <= 2550.0) {
                     pc.enterMovieMode();
                     pc.specialCamera(ValakasManager._valakas, 800, 180, -8, 2700, 15000, 0, 30, 1, 0);
                  } else {
                     pc.leaveMovieMode();
                  }
               }

               ValakasManager._socialTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(6), 2700L);
               break;
            case 6:
               for(Player pc : this._players) {
                  if (pc.getDistance(ValakasManager._valakas) <= 2550.0) {
                     pc.enterMovieMode();
                     pc.specialCamera(ValakasManager._valakas, 200, 250, 70, 0, 15000, 30, 80, 1, 0);
                  } else {
                     pc.leaveMovieMode();
                  }
               }

               ValakasManager._socialTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(7), 1L);
               break;
            case 7:
               for(Player pc : this._players) {
                  if (pc.getDistance(ValakasManager._valakas) <= 2550.0) {
                     pc.enterMovieMode();
                     pc.specialCamera(ValakasManager._valakas, 1100, 250, 70, 2500, 15000, 30, 80, 1, 0);
                  } else {
                     pc.leaveMovieMode();
                  }
               }

               ValakasManager._socialTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(8), 3200L);
               break;
            case 8:
               for(Player pc : this._players) {
                  if (pc.getDistance(ValakasManager._valakas) <= 2550.0) {
                     pc.enterMovieMode();
                     pc.specialCamera(ValakasManager._valakas, 700, 150, 30, 0, 15000, -10, 60, 1, 0);
                  } else {
                     pc.leaveMovieMode();
                  }
               }

               ValakasManager._socialTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(9), 1400L);
               break;
            case 9:
               for(Player pc : this._players) {
                  if (pc.getDistance(ValakasManager._valakas) <= 2550.0) {
                     pc.enterMovieMode();
                     pc.specialCamera(ValakasManager._valakas, 1200, 150, 20, 2900, 15000, -10, 30, 1, 0);
                  } else {
                     pc.leaveMovieMode();
                  }
               }

               ValakasManager._socialTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(10), 6700L);
               break;
            case 10:
               for(Player pc : this._players) {
                  if (pc.getDistance(ValakasManager._valakas) <= 2550.0) {
                     pc.enterMovieMode();
                     pc.specialCamera(ValakasManager._valakas, 750, 170, -10, 3400, 15000, 10, -15, 1, 0);
                  } else {
                     pc.leaveMovieMode();
                  }
               }

               ValakasManager._socialTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(11), 5700L);
               break;
            case 11:
               for(Player pc : this._players) {
                  pc.leaveMovieMode();
               }

               ValakasManager._valakas.unblock();
               ValakasManager._lastAttackTime = System.currentTimeMillis();
               ValakasManager.broadcastScreenMessage(NpcStringId.ARROGANT_FOOL_YOU_DARE_TO_CHALLENGE_ME_THE_RULER_OF_FLAMES_HERE_IS_YOUR_REWARD);
               EpicBossManager.getInstance().setBossStatus(29028, 2, true);
               if (ValakasManager._valakas.getAI().getIntention() == CtrlIntention.ACTIVE) {
                  ValakasManager._valakas.moveToLocation(Rnd.get(211080, 214909), Rnd.get(-115841, -112822), -1662, 0);
               }

               ValakasManager._sleepCheckTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.CheckLastAttack(), 600000L);
               break;
            case 12:
               ValakasManager._valakas.sendPacket(new PlaySound(1, "B03_D", 0, 0, 0, 0, 0));
               ValakasManager.broadcastScreenMessage(NpcStringId.THE_EVIL_FIRE_DRAGON_VALAKAS_HAS_BEEN_DEFEATED);
               ValakasManager.onValakasDie();

               for(Player pc : this._players) {
                  if (pc.getDistance(ValakasManager._valakas) <= 2550.0) {
                     pc.enterMovieMode();
                     pc.specialCamera(ValakasManager._valakas, 2000, 130, -1, 0, 15000, 0, 0, 1, 1);
                  } else {
                     pc.leaveMovieMode();
                  }
               }

               ValakasManager._socialTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(13), 500L);
               break;
            case 13:
               for(Player pc : this._players) {
                  if (pc.getDistance(ValakasManager._valakas) <= 2550.0) {
                     pc.enterMovieMode();
                     pc.specialCamera(ValakasManager._valakas, 1100, 210, -5, 3000, 15000, -13, 0, 1, 1);
                  } else {
                     pc.leaveMovieMode();
                  }
               }

               ValakasManager._socialTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(14), 3500L);
               break;
            case 14:
               for(Player pc : this._players) {
                  if (pc.getDistance(ValakasManager._valakas) <= 2550.0) {
                     pc.enterMovieMode();
                     pc.specialCamera(ValakasManager._valakas, 1300, 200, -8, 3000, 15000, 0, 15, 1, 1);
                  } else {
                     pc.leaveMovieMode();
                  }
               }

               ValakasManager._socialTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(15), 4500L);
               break;
            case 15:
               for(Player pc : this._players) {
                  if (pc.getDistance(ValakasManager._valakas) <= 2550.0) {
                     pc.enterMovieMode();
                     pc.specialCamera(ValakasManager._valakas, 1000, 190, 0, 500, 15000, 0, 10, 1, 1);
                  } else {
                     pc.leaveMovieMode();
                  }
               }

               ValakasManager._socialTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(16), 500L);
               break;
            case 16:
               for(Player pc : this._players) {
                  if (pc.getDistance(ValakasManager._valakas) <= 2550.0) {
                     pc.enterMovieMode();
                     pc.specialCamera(ValakasManager._valakas, 1700, 120, 0, 2500, 15000, 12, 40, 1, 1);
                  } else {
                     pc.leaveMovieMode();
                  }
               }

               ValakasManager._socialTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(17), 4600L);
               break;
            case 17:
               for(Player pc : this._players) {
                  if (pc.getDistance(ValakasManager._valakas) <= 2550.0) {
                     pc.enterMovieMode();
                     pc.specialCamera(ValakasManager._valakas, 1700, 20, 0, 700, 15000, 10, 10, 1, 1);
                  } else {
                     pc.leaveMovieMode();
                  }
               }

               ValakasManager._socialTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(18), 750L);
               break;
            case 18:
               for(Player pc : this._players) {
                  if (pc.getDistance(ValakasManager._valakas) <= 2550.0) {
                     pc.enterMovieMode();
                     pc.specialCamera(ValakasManager._valakas, 1700, 10, 0, 1000, 15000, 20, 70, 1, 1);
                  } else {
                     pc.leaveMovieMode();
                  }
               }

               ValakasManager._socialTask = ThreadPoolManager.getInstance().schedule(new ValakasManager.SpawnDespawn(19), 2500L);
               break;
            case 19:
               for(Player pc : this._players) {
                  pc.leaveMovieMode();
                  Skill buff = SkillsParser.getInstance().getInfo(23312, 1);
                  if (buff != null) {
                     buff.getEffects(pc, pc, false);
                  }
               }
         }
      }
   }
}
