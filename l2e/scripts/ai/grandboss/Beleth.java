package l2e.scripts.ai.grandboss;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.instancemanager.EpicBossManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.SpecialCamera;
import l2e.scripts.ai.AbstractNpcAI;

public class Beleth extends AbstractNpcAI {
   private Npc _camera1;
   private Npc _camera2;
   private Npc _camera3;
   private Npc _camera4;
   private Npc _beleth;
   private Npc _vortex;
   private Npc _priest;
   private final ZoneType _zone = ZoneManager.getInstance().getZoneById(12018);
   private Player _killer;
   private Npc _stone;
   private boolean _movie = false;
   private final List<Spawner> _minions = new ArrayList<>();
   private final List<Location> _spawnBelethLocs = new ArrayList<>();
   private ScheduledFuture<?> _spawnTask = null;
   private ScheduledFuture<?> _activityCheckTask = null;
   private final Map<Npc, Location> _clonesLoc = new ConcurrentHashMap<>();
   private final Location[] _cloneLoc = new Location[56];
   private long _lastAction = 0L;
   private static ScheduledFuture<?> _intervalEndTask = null;

   private Beleth(String name, String descr) {
      super(name, descr);
      this.addEnterZoneId(new int[]{12018});
      this.registerMobs(new int[]{29118, 29119});
      this.addSpawnId(new int[]{29118, 29119});
      this.addStartNpc(32470);
      this.addTalkId(32470);
      this.addFirstTalkId(29128);
      StatsSet info = EpicBossManager.getInstance().getStatsSet(29118);
      int status = EpicBossManager.getInstance().getBossStatus(29118);
      if (status == 3) {
         long temp = info.getLong("respawnTime") - System.currentTimeMillis();
         if (temp > 0L) {
            if (_intervalEndTask != null) {
               _intervalEndTask.cancel(false);
               _intervalEndTask = null;
            }

            _intervalEndTask = ThreadPoolManager.getInstance().schedule(new Beleth.Unlock(), temp);
            DoorParser.getInstance().getDoor(20240001).closeMe();
         } else {
            EpicBossManager.getInstance().setBossStatus(29118, 0, true);
            DoorParser.getInstance().getDoor(20240001).openMe();
         }
      } else {
         EpicBossManager.getInstance().setBossStatus(29118, 0, false);
         DoorParser.getInstance().getDoor(20240001).openMe();
      }
   }

   protected static Npc spawn(int npcId, Location loc) {
      Npc result = null;

      try {
         NpcTemplate template = NpcsParser.getInstance().getTemplate(npcId);
         if (template != null) {
            Spawner spawn = new Spawner(template);
            spawn.setLocation(loc);
            spawn.setHeading(loc.getHeading());
            spawn.setAmount(1);
            if (npcId == 29119) {
               spawn.setRespawnDelay(Config.BELETH_CLONES_RESPAWN);
               spawn.startRespawn();
            } else {
               spawn.stopRespawn();
            }

            return spawn.spawnOne(false);
         }
      } catch (Exception var5) {
      }

      return null;
   }

   @Override
   public String onEnterZone(Creature character, ZoneType zone) {
      if (character.isPlayer() && EpicBossManager.getInstance().getBossStatus(29118) == 1 && this._spawnTask == null) {
         this.setBelethSpawnTask();
      }

      return null;
   }

   public void setBelethSpawnTask() {
      if (this._spawnTask == null) {
         synchronized(this) {
            if (this._spawnTask == null) {
               EpicBossManager.getInstance().setBossStatus(29118, 2, true);
               this._spawnTask = ThreadPoolManager.getInstance().schedule(new Beleth.SpawnTask(1), (long)(Config.BELETH_SPAWN_DELAY * 60000));
               this.initSpawnLocs();
            }
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (npc.getId() == 29118 && killer != null) {
         this.setBelethKiller(killer);
         long respawnTime = EpicBossManager.getInstance().setRespawnTime(29118, Config.BELETH_RESPAWN_PATTERN);
         if (_intervalEndTask != null) {
            _intervalEndTask.cancel(false);
            _intervalEndTask = null;
         }

         _intervalEndTask = ThreadPoolManager.getInstance().schedule(new Beleth.Unlock(), respawnTime - System.currentTimeMillis());
         this.deleteAllClones();
         if (this._beleth != null) {
            this._beleth.deleteMe();
         }

         this._movie = true;
         ThreadPoolManager.getInstance().schedule(new Beleth.SpawnTask(27), 1000L);
         this._stone = addSpawn(32470, new Location(12470, 215607, -9381, 49152));
         ThreadPoolManager.getInstance().schedule(new Beleth.SpawnTask(28), 1500L);
      }

      return super.onKill(npc, killer, isSummon);
   }

   @Override
   public String onSkillSee(Npc npc, Player player, Skill skill, GameObject[] targets, boolean isSummon) {
      if (npc != null
         && !npc.isDead()
         && (npc.getId() == 29118 || npc.getId() == 29119)
         && !npc.isCastingNow()
         && skill.hasEffectType(EffectType.HEAL)
         && getRandom(100) < 80) {
         npc.setTarget(player);
         npc.doCast(new SkillHolder(5497, 1).getSkill());
      }

      return null;
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      if (npc.getId() == 29118 || npc.getId() == 29119) {
         if (npc.getId() == 29118) {
            this._lastAction = System.currentTimeMillis();
         }

         if (getRandom(100) < 40) {
            return null;
         }

         double distance = Math.sqrt(npc.getPlanDistanceSq(attacker.getX(), attacker.getY()));
         if ((!(distance > 500.0) || !(distance < 890.0)) && getRandom(100) >= 80) {
            if (!npc.isDead() && !npc.isCastingNow()) {
               if (!World.getInstance().getAroundPlayers(npc, 200, 200).isEmpty()) {
                  npc.doCast(new SkillHolder(5499, 1).getSkill());
                  return null;
               }

               ((Attackable)npc).clearAggroList();
            }
         } else {
            for(Spawner spawn : this._minions) {
               Npc minion = spawn.getLastSpawn();
               if (minion != null && !minion.isDead() && Util.checkIfInRange(900, minion, attacker, false) && !minion.isCastingNow()) {
                  minion.setTarget(attacker);
                  minion.doCast(new SkillHolder(5496, 1).getSkill());
               }
            }

            if (this._beleth != null && !this._beleth.isDead() && Util.checkIfInRange(900, this._beleth, attacker, false) && !this._beleth.isCastingNow()) {
               this._beleth.setTarget(attacker);
               this._beleth.doCast(new SkillHolder(5496, 1).getSkill());
            }
         }
      }

      return null;
   }

   @Override
   public String onSpellFinished(Npc npc, Player player, Skill skill) {
      if (npc != null && !npc.isDead() && (npc.getId() == 29118 || npc.getId() == 29119) && !npc.isCastingNow()) {
         if (player != null && !player.isDead()) {
            double distance2 = Math.sqrt(npc.getPlanDistanceSq(player.getX(), player.getY()));
            if (distance2 > 890.0 && !npc.isMovementDisabled()) {
               npc.setTarget(player);
               npc.getAI().setIntention(CtrlIntention.FOLLOW, player);
               double speed = npc.isRunning() ? npc.getRunSpeed() : npc.getWalkSpeed();
               int time = (int)((distance2 - 890.0) / speed * 1000.0);
               ThreadPoolManager.getInstance().schedule(new Beleth.SkillUse(new SkillHolder(5496, 1), npc), (long)time);
            } else if (distance2 < 890.0) {
               npc.setTarget(player);
               npc.doCast(new SkillHolder(5496, 1).getSkill());
            }

            return null;
         }

         if (getRandom(100) < 40 && !World.getInstance().getAroundPlayers(npc, 200, 200).isEmpty()) {
            npc.doCast(new SkillHolder(5499, 1).getSkill());
            return null;
         }

         Iterator distance2 = World.getInstance().getAroundPlayers(npc, 950, 200).iterator();
         if (distance2.hasNext()) {
            Player plr = (Player)distance2.next();
            npc.setTarget(plr);
            npc.doCast(new SkillHolder(5496, 1).getSkill());
            return null;
         }

         ((Attackable)npc).clearAggroList();
      }

      return null;
   }

   @Override
   public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon) {
      if (npc != null && !npc.isDead() && (npc.getId() == 29118 || npc.getId() == 29119) && !npc.isCastingNow() && !this._movie) {
         if (getRandom(100) < 40 && !World.getInstance().getAroundPlayers(npc, 200, 200).isEmpty()) {
            npc.doCast(new SkillHolder(5495, 1).getSkill());
            return null;
         }

         npc.setTarget(player);
         npc.doCast(new SkillHolder(5496, 1).getSkill());
      }

      return super.onAggroRangeEnter(npc, player, isSummon);
   }

   @Override
   public String onSpawn(Npc npc) {
      if (npc.getId() == 29119) {
         npc.setIsNoRndWalk(true);
         npc.setIsSpecialCamera(true);
         if (!this._movie && !World.getInstance().getAroundPlayers(npc, 300, 200).isEmpty() && getRandom(100) < 60) {
            npc.doCast(new SkillHolder(5495, 1).getSkill());
         }
      }

      if (npc.getId() == 29118) {
         npc.setIsNoRndWalk(true);
         npc.setIsSpecialCamera(true);
         if (!this._movie && !World.getInstance().getAroundPlayers(npc, 300, 200).isEmpty() && getRandom(100) < 60) {
            npc.doCast(new SkillHolder(5495, 1).getSkill());
         }
      }

      return null;
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String html;
      if (this._killer != null && player.getObjectId() == this._killer.getObjectId()) {
         this._killer = null;
         player.addItem("Kill Beleth", 10314, 1L, null, true);
         html = "32470a.htm";
      } else {
         html = "32470b.htm";
      }

      return HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/default/" + html);
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      if (npc.getId() == 29128) {
         player.teleToLocation(-24095, 251617, -3374, true);
         if (player.getSummon() != null) {
            player.getSummon().teleToLocation(-24095, 251617, -3374, true);
         }
      }

      return null;
   }

   private void setBelethKiller(Player killer) {
      this._killer = killer.getParty() != null
         ? (killer.getParty().getCommandChannel() != null ? killer.getParty().getCommandChannel().getLeader() : killer.getParty().getLeader())
         : killer;
   }

   private void deleteAllClones() {
      if (this._activityCheckTask != null) {
         this._activityCheckTask.cancel(true);
         this._activityCheckTask = null;
      }

      if (!this._minions.isEmpty()) {
         for(Spawner clone : this._minions) {
            if (clone != null) {
               clone.stopRespawn();
               Npc bel = clone.getLastSpawn();
               if (bel != null) {
                  bel.deleteMe();
               }
            }
         }
      }

      this._minions.clear();
      this._clonesLoc.clear();
   }

   private void deleteBeleth() {
      if (this._beleth != null) {
         this._beleth.abortCast();
         this._beleth.setTarget(null);
         this._beleth.getAI().setIntention(CtrlIntention.IDLE);
         this._beleth.deleteMe();
         this._beleth = null;
      }

      if (this._vortex != null) {
         this._vortex.deleteMe();
      }

      if (this._camera1 != null) {
         this._camera1.deleteMe();
      }

      if (this._camera2 != null) {
         this._camera2.deleteMe();
      }

      if (this._camera3 != null) {
         this._camera3.deleteMe();
      }

      if (this._camera4 != null) {
         this._camera4.deleteMe();
      }

      this._spawnBelethLocs.clear();
   }

   private void setUnspawn() {
      this._movie = false;
      this.deleteAllClones();
      this.deleteBeleth();
      if (this._spawnTask != null) {
         this._spawnTask.cancel(true);
         this._spawnTask = null;
      }

      if (this._activityCheckTask != null) {
         this._activityCheckTask.cancel(true);
         this._activityCheckTask = null;
      }

      DoorParser.getInstance().getDoor(20240002).closeMe();
      DoorParser.getInstance().getDoor(20240001).openMe();
      EpicBossManager.getInstance().getZone(12018).oustAllPlayers();
   }

   private void spawnClone(int id) {
      Npc clone = spawn(29119, new Location(this._cloneLoc[id].getX(), this._cloneLoc[id].getY(), -9353, 49152));
      if (clone != null) {
         this._zone.broadcastPacket(new SocialAction(clone.getObjectId(), 0));
         this._clonesLoc.put(clone, clone.getLocation());
         this._spawnBelethLocs.add(clone.getLocation());
         this._minions.add(clone.getSpawn());
      }
   }

   private void initSpawnLocs() {
      double angle = Math.toRadians(22.5);
      int radius = 700;

      for(int i = 0; i < 16; ++i) {
         if (i % 2 == 0) {
            radius -= 50;
         } else {
            radius += 50;
         }

         this._cloneLoc[i] = new Location(
            16325 + (int)((double)radius * Math.sin((double)i * angle)),
            213135 + (int)((double)radius * Math.cos((double)i * angle)),
            convertDegreeToClientHeading(270.0 - (double)i * 22.5)
         );
      }

      int var15 = 1340;
      angle = Math.asin(1.0 / Math.sqrt(3.0));
      int mulX = 1;
      int mulY = 1;
      int addH = 3;
      double decX = 1.0;
      double decY = 1.0;

      for(int i = 0; i < 16; ++i) {
         byte var19;
         if (i % 8 == 0) {
            var19 = 0;
         } else if (i < 8) {
            var19 = -1;
         } else {
            var19 = 1;
         }

         byte var20;
         if (i == 4 || i == 12) {
            var20 = 0;
         } else if (i > 4 && i < 12) {
            var20 = -1;
         } else {
            var20 = 1;
         }

         if (i % 8 != 1 && i != 7 && i != 15) {
            decX = 1.0;
         } else {
            decX = 0.5;
         }

         if (i % 10 != 3 && i != 5 && i != 11) {
            decY = 1.0;
         } else {
            decY = 0.5;
         }

         if ((i + 2) % 4 == 0) {
            ++addH;
         }

         this._cloneLoc[i + 16] = new Location(
            16325 + (int)((double)var15 * decX * (double)var19),
            213135 + (int)((double)var15 * decY * (double)var20),
            convertDegreeToClientHeading((double)(180 + addH * 90))
         );
      }

      angle = Math.toRadians(22.5);
      var15 = 1000;

      for(int i = 0; i < 16; ++i) {
         if (i % 2 == 0) {
            var15 -= 70;
         } else {
            var15 += 70;
         }

         this._cloneLoc[i + 32] = new Location(
            16325 + (int)((double)var15 * Math.sin((double)i * angle)),
            213135 + (int)((double)var15 * Math.cos((double)i * angle)),
            this._cloneLoc[i].getHeading()
         );
      }

      int order = 48;
      int var17 = 650;

      for(int i = 1; i < 16; i += 2) {
         if (i == 1 || i == 15) {
            this._cloneLoc[order] = new Location(this._cloneLoc[i].getX(), this._cloneLoc[i].getY() + var17, this._cloneLoc[i + 16].getHeading());
         } else if (i == 3 || i == 5) {
            this._cloneLoc[order] = new Location(this._cloneLoc[i].getX() + var17, this._cloneLoc[i].getY(), this._cloneLoc[i].getHeading());
         } else if (i == 7 || i == 9) {
            this._cloneLoc[order] = new Location(this._cloneLoc[i].getX(), this._cloneLoc[i].getY() - var17, this._cloneLoc[i + 16].getHeading());
         } else if (i == 11 || i == 13) {
            this._cloneLoc[order] = new Location(this._cloneLoc[i].getX() - var17, this._cloneLoc[i].getY(), this._cloneLoc[i].getHeading());
         }

         ++order;
      }
   }

   private void showSocialActionMovie(
      Creature target, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int widescreen, int relAngle, int unk
   ) {
      if (target != null) {
         SpecialCamera movie = new SpecialCamera(target, dist, yaw, pitch, time, duration, turn, rise, widescreen, relAngle, unk);
         this._zone.broadcastPacket(movie);
      }
   }

   private static int convertDegreeToClientHeading(double degree) {
      if (degree < 0.0) {
         degree += 360.0;
      }

      return (int)(degree * 182.044444444);
   }

   @Override
   public boolean unload(boolean removeFromList) {
      this.setUnspawn();
      if (_intervalEndTask != null) {
         _intervalEndTask.cancel(false);
         _intervalEndTask = null;
      }

      int status = EpicBossManager.getInstance().getBossStatus(29118);
      if (status > 0 && status < 3) {
         EpicBossManager.getInstance().setBossStatus(29118, 0, true);
      }

      return super.unload(removeFromList);
   }

   public static void main(String[] args) {
      new Beleth(Beleth.class.getSimpleName(), "ai");
   }

   private class CheckActivity implements Runnable {
      private CheckActivity() {
      }

      @Override
      public void run() {
         Long temp = System.currentTimeMillis() - Beleth.this._lastAction;
         if (temp > 3600000L && Beleth.this._beleth != null) {
            EpicBossManager.getInstance().setBossStatus(Beleth.this._beleth.getId(), 0, true);
            Beleth.this.setUnspawn();
         }
      }
   }

   private class DoActionBeleth implements Runnable {
      private final int _socialAction;
      private final Skill _skill;

      public DoActionBeleth(int socialAction, Skill skill) {
         this._socialAction = socialAction;
         this._skill = skill;
      }

      @Override
      public void run() {
         if (this._socialAction > 0) {
            Beleth.this._zone.broadcastPacket(new SocialAction(Beleth.this._beleth.getObjectId(), this._socialAction));
         }

         if (Beleth.this._beleth != null && this._skill != null) {
            Beleth.this._zone
               .broadcastPacket(new MagicSkillUse(Beleth.this._beleth, Beleth.this._beleth, this._skill.getId(), 1, this._skill.getHitTime(), 1));
         }
      }
   }

   private class ShowBeleth implements Runnable {
      private ShowBeleth() {
      }

      @Override
      public void run() {
         Location spawn = null;
         if (Beleth.this._spawnBelethLocs != null && !Beleth.this._spawnBelethLocs.isEmpty()) {
            spawn = Beleth.this._spawnBelethLocs.get(Rnd.get(Beleth.this._spawnBelethLocs.size())).rnd(50, 100, true);
         } else {
            spawn = new Location(16323, 213059, -9357, 49152);
         }

         Beleth.this._beleth = Beleth.spawn(29118, new Location(spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getHeading()));
         Beleth.this._lastAction = System.currentTimeMillis();
         Beleth.this._activityCheckTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(Beleth.this.new CheckActivity(), 60000L, 60000L);
      }
   }

   protected static class SkillUse implements Runnable {
      SkillHolder _skill;
      Npc _npc;

      public SkillUse(SkillHolder skill, Npc npc) {
         this._skill = skill;
         this._npc = npc;
      }

      @Override
      public void run() {
         if (this._npc != null && !this._npc.isDead() && !this._npc.isCastingNow()) {
            this._npc.getAI().setIntention(CtrlIntention.ACTIVE);
            this._npc.doCast(this._skill.getSkill());
         }
      }
   }

   protected class SpawnTask implements Runnable {
      private int _taskId = 0;

      public SpawnTask(int taskId) {
         this._taskId = taskId;
      }

      @Override
      public void run() {
         try {
            switch(this._taskId) {
               case 1:
                  Beleth.this._spawnTask.cancel(false);
                  Beleth.this._spawnTask = null;
                  Beleth.this._movie = true;

                  for(Creature npc : Beleth.this._zone.getCharactersInside()) {
                     if (npc != null && npc.isNpc()) {
                        npc.deleteMe();
                     }
                  }

                  Beleth.this._camera1 = Beleth.spawn(29120, new Location(16323, 213142, -9357, 0));
                  Beleth.this._camera1.setIsSpecialCamera(true);
                  Beleth.this._camera2 = Beleth.spawn(29121, new Location(16323, 210741, -9357, 0));
                  Beleth.this._camera2.setIsSpecialCamera(true);
                  Beleth.this._camera3 = Beleth.spawn(29122, new Location(16323, 213170, -9357, 0));
                  Beleth.this._camera3.setIsSpecialCamera(true);
                  Beleth.this._camera4 = Beleth.spawn(29123, new Location(16323, 214917, -9356, 0));
                  Beleth.this._camera4.setIsSpecialCamera(true);
                  Beleth.this._zone
                     .broadcastPacket(
                        new PlaySound(
                           1,
                           "BS07_A",
                           1,
                           Beleth.this._camera1.getObjectId(),
                           Beleth.this._camera1.getX(),
                           Beleth.this._camera1.getY(),
                           Beleth.this._camera1.getZ()
                        )
                     );
                  Beleth.this.showSocialActionMovie(Beleth.this._camera1, 1700, 110, 50, 0, 2600, 0, 0, 1, 0, 0);
                  Beleth.this.showSocialActionMovie(Beleth.this._camera1, 1700, 100, 50, 0, 2600, 0, 0, 1, 0, 0);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 300L);
                  break;
               case 2:
                  Beleth.this.showSocialActionMovie(Beleth.this._camera1, 1800, -65, 30, 6000, 5000, 0, 0, 1, 0, 0);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 4900L);
                  break;
               case 3:
                  Beleth.this.showSocialActionMovie(Beleth.this._camera1, 2200, -120, 30, 6000, 5000, 0, 0, 1, 0, 0);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 4900L);
                  break;
               case 4:
                  Beleth.this.showSocialActionMovie(Beleth.this._camera2, 2200, 130, 20, 1000, 1500, -20, 10, 1, 0, 0);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 1400L);
                  break;
               case 5:
                  Beleth.this.showSocialActionMovie(Beleth.this._camera2, 2300, 100, 10, 2000, 4500, 0, 10, 1, 0, 0);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 2500L);
                  break;
               case 6:
                  DoorParser.getInstance().getDoor(20240001).closeMe();
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 1700L);
                  break;
               case 7:
                  Beleth.this.showSocialActionMovie(Beleth.this._camera4, 1500, 210, 5, 0, 1500, 0, 0, 1, 0, 0);
                  Beleth.this.showSocialActionMovie(Beleth.this._camera4, 900, 255, 5, 5000, 6500, 0, 10, 1, 0, 0);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 6000L);
                  break;
               case 8:
                  Beleth.this._vortex = Beleth.spawn(29125, new Location(16323, 214917, -9356, 0));
                  Beleth.this._vortex.setIsSpecialCamera(true);
                  Beleth.this.showSocialActionMovie(Beleth.this._camera4, 900, 255, 5, 0, 1500, 0, 5, 1, 0, 0);
                  Beleth.this._beleth = Beleth.spawn(29118, new Location(16321, 214211, -9352, 49369));
                  Beleth.this._beleth.setIsSpecialCamera(true);
                  Beleth.this._beleth.setShowSummonAnimation(true);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 1000L);
                  break;
               case 9:
                  Beleth.this.showSocialActionMovie(Beleth.this._camera4, 1100, 255, 10, 7000, 19000, 0, 20, 1, 0, 0);
                  Beleth.this._zone.broadcastPacket(new SocialAction(Beleth.this._beleth.getObjectId(), 1));
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 4000L);
                  break;
               case 10:
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 200L);
                  break;
               case 11:
                  for(int i = 0; i < 6; ++i) {
                     int x = (int)(150.0 * Math.cos((double)i * 1.046666667) + 16323.0);
                     int y = (int)(150.0 * Math.sin((double)i * 1.046666667) + 213059.0);
                     Npc minion = Beleth.spawn(29119, new Location(x, y, -9357, 49152));
                     minion.setShowSummonAnimation(true);
                     minion.setIsSpecialCamera(true);
                     minion.decayMe();
                     Beleth.this._minions.add(minion.getSpawn());
                  }

                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 6800L);
                  break;
               case 12:
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new DoActionBeleth(0, new SkillHolder(5531, 1).getSkill()), 1000L);
                  Beleth.this.showSocialActionMovie(Beleth.this._beleth, 0, 270, 5, 0, 6000, 0, 0, 1, 0, 0);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 5500L);
                  break;
               case 13:
                  Beleth.this.showSocialActionMovie(Beleth.this._beleth, 800, 270, 10, 3000, 6000, 0, 0, 1, 0, 0);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 5000L);
                  break;
               case 14:
                  Beleth.this.showSocialActionMovie(Beleth.this._camera3, 100, 270, 15, 0, 5000, 0, 0, 1, 0, 0);
                  Beleth.this.showSocialActionMovie(Beleth.this._camera3, 100, 270, 15, 0, 5000, 0, 0, 1, 0, 0);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 100L);
                  break;
               case 15:
                  Beleth.this.showSocialActionMovie(Beleth.this._camera3, 100, 270, 15, 3000, 6000, 0, 5, 1, 0, 0);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 1400L);
                  break;
               case 16:
                  Beleth.this._beleth.teleToLocation(16323, 213059, -9357, 49152, false);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 200L);
                  break;
               case 17:
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new DoActionBeleth(0, new SkillHolder(5532, 1).getSkill()), 100L);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 2000L);
                  break;
               case 18:
                  Beleth.this.showSocialActionMovie(Beleth.this._camera3, 700, 270, 20, 1500, 8000, 0, 0, 1, 0, 0);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 6900L);
                  break;
               case 19:
                  Beleth.this.showSocialActionMovie(Beleth.this._camera3, 40, 260, 15, 0, 4000, 0, 0, 1, 0, 0);

                  for(Spawner blth : Beleth.this._minions) {
                     blth.spawnOne(false);
                  }

                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 3000L);
                  break;
               case 20:
                  Beleth.this.showSocialActionMovie(Beleth.this._camera3, 40, 280, 15, 0, 4000, 5, 0, 1, 0, 0);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 3000L);
                  break;
               case 21:
                  Beleth.this.showSocialActionMovie(Beleth.this._camera3, 5, 250, 15, 0, 13300, 20, 15, 1, 0, 0);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 1000L);
                  break;
               case 22:
                  Beleth.this._zone.broadcastPacket(new SocialAction(Beleth.this._beleth.getObjectId(), 3));
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 4000L);
                  break;
               case 23:
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new DoActionBeleth(0, new SkillHolder(5533, 1).getSkill()), 100L);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 6800L);
                  break;
               case 24:
                  if (Beleth.this._beleth != null) {
                     Beleth.this._beleth.deleteMe();
                  }

                  for(Spawner spawn : Beleth.this._minions) {
                     if (spawn != null) {
                        spawn.stopRespawn();
                        Npc bel = spawn.getLastSpawn();
                        if (bel != null) {
                           bel.deleteMe();
                        }
                     }
                  }

                  Beleth.this._minions.clear();
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(26), 10L);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new ShowBeleth(), (long)(Config.BELETH_SPAWN_DELAY * 60000));
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 2000L);
                  break;
               case 25:
                  Beleth.this._camera1.deleteMe();
                  Beleth.this._camera2.deleteMe();
                  Beleth.this._camera3.deleteMe();
                  Beleth.this._camera4.deleteMe();
                  Beleth.this._movie = false;

                  for(Npc clones : Beleth.this._clonesLoc.keySet()) {
                     Beleth.this._zone.broadcastPacket(new SocialAction(clones.getObjectId(), 0));
                  }
                  break;
               case 26:
                  for(int i = 0; i < 56; ++i) {
                     Beleth.this.spawnClone(i);
                  }
                  break;
               case 27:
                  Beleth.this._beleth = Beleth.spawn(29118, new Location(16323, 213170, -9357, 49152));
                  Beleth.this._beleth.setIsInvul(true);
                  Beleth.this._beleth.setIsSpecialCamera(true);
                  Beleth.this._beleth.setIsImmobilized(true);
                  Beleth.this._beleth.disableAllSkills();
                  Beleth.this._priest = Beleth.spawn(29128, new Location(Beleth.this._beleth));
                  Beleth.this._priest.setIsSpecialCamera(true);
                  Beleth.this._priest.setShowSummonAnimation(true);
                  Beleth.this._priest.decayMe();
                  break;
               case 28:
                  Beleth.this._beleth.doDie(null);
                  Beleth.this._camera1 = Beleth.spawn(29122, new Location(16323, 213170, -9357, 0));
                  Beleth.this._camera1.setIsSpecialCamera(true);
                  Beleth.this._camera1
                     .broadcastPacket(
                        new PlaySound(
                           1,
                           "BS07_D",
                           1,
                           Beleth.this._camera1.getObjectId(),
                           Beleth.this._camera1.getX(),
                           Beleth.this._camera1.getY(),
                           Beleth.this._camera1.getZ()
                        )
                     );
                  Beleth.this.showSocialActionMovie(Beleth.this._camera1, 400, 290, 25, 0, 10000, 0, 0, 1, 0, 0);
                  Beleth.this.showSocialActionMovie(Beleth.this._camera1, 400, 290, 25, 0, 10000, 0, 0, 1, 0, 0);
                  Beleth.this.showSocialActionMovie(Beleth.this._camera1, 400, 110, 25, 4000, 10000, 0, 0, 1, 0, 0);
                  Beleth.this._zone.broadcastPacket(new SocialAction(Beleth.this._beleth.getObjectId(), 5));

                  for(Creature npc : Beleth.this._zone.getCharactersInside()) {
                     if (npc != null && npc.getId() == 29119) {
                        npc.deleteMe();
                     }
                  }

                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 4000L);
                  break;
               case 29:
                  Beleth.this.showSocialActionMovie(Beleth.this._camera1, 400, 295, 25, 4000, 5000, 0, 0, 1, 0, 0);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 4500L);
                  break;
               case 30:
                  Beleth.this.showSocialActionMovie(Beleth.this._camera1, 400, 295, 10, 4000, 11000, 0, 25, 1, 0, 0);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 9000L);
                  break;
               case 31:
                  Beleth.this._vortex.deleteMe();
                  Beleth.this.showSocialActionMovie(Beleth.this._camera1, 250, 90, 25, 0, 1000, 0, 0, 1, 0, 0);
                  Beleth.this.showSocialActionMovie(Beleth.this._camera1, 250, 90, 35, 0, 10000, 0, 0, 1, 0, 0);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 2000L);
                  break;
               case 32:
                  Beleth.this._priest.spawnMe();
                  if (Beleth.this._beleth != null) {
                     Beleth.this._beleth.deleteMe();
                  }

                  Beleth.this._camera2 = Beleth.spawn(29121, new Location(14056, 213170, -9357, 0));
                  Beleth.this._camera2.setIsSpecialCamera(true);
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 3500L);
                  break;
               case 33:
                  Beleth.this.showSocialActionMovie(Beleth.this._camera2, 800, 180, 0, 0, 4000, 0, 10, 1, 0, 0);
                  Beleth.this.showSocialActionMovie(Beleth.this._camera2, 800, 180, 0, 0, 4000, 0, 10, 1, 0, 0);
                  DoorParser.getInstance().getDoor(20240002).openMe();
                  DoorParser.getInstance().getDoor(20240003).openMe();
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), 4000L);
                  break;
               case 34:
                  Beleth.this._camera1.deleteMe();
                  Beleth.this._camera2.deleteMe();
                  Beleth.this._movie = false;
                  Beleth.this.deleteAllClones();
                  Beleth.this.deleteBeleth();
                  ThreadPoolManager.getInstance().schedule(Beleth.this.new SpawnTask(this._taskId + 1), (long)(Config.BELETH_ZONE_CLEAN_DELAY * 60 * 1000));
                  break;
               case 35:
                  if (Beleth.this._spawnTask != null) {
                     Beleth.this._spawnTask.cancel(true);
                     Beleth.this._spawnTask = null;
                  }

                  if (Beleth.this._stone != null) {
                     Beleth.this._stone.deleteMe();
                  }

                  DoorParser.getInstance().getDoor(20240002).closeMe();
                  EpicBossManager.getInstance().getZone(12018).oustAllPlayers();
            }
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }
   }

   protected static class Unlock implements Runnable {
      @Override
      public void run() {
         EpicBossManager.getInstance().setBossStatus(29118, 0, true);
         DoorParser.getInstance().getDoor(20240001).openMe();
      }
   }
}
