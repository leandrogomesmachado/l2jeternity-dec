package l2e.scripts.ai.pagan_temple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.NpcUtils;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.type.BossZone;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import l2e.gameserver.network.serverpackets.PlaySound;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.SpecialCamera;

public class AndreasVanHalter extends Fighter {
   private boolean _firstTimeMove = false;
   private static ScheduledFuture<?> _checkTask = null;
   private static ScheduledFuture<?> _movieTask = null;
   private static BossZone _zone;
   private final List<Npc> _guard = new ArrayList<>();
   private final List<Npc> _altarGuards = new ArrayList<>();
   protected Npc _ritualOffering;

   public AndreasVanHalter(Attackable actor) {
      super(actor);
      _zone = (BossZone)ZoneManager.getInstance().getZoneById(12014);
   }

   @Override
   protected void onEvtSpawn() {
      Attackable actor = this.getActiveChar();
      if (actor != null) {
         this._guard.clear();
         this._altarGuards.clear();
         this.firstSpawn();
         this._firstTimeMove = true;
         _zone.setCanTeleport(false);
         super.onEvtSpawn();
      }
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (actor == null) {
         return true;
      } else {
         if (!this.checkAliveGuards() && this._firstTimeMove) {
            this._firstTimeMove = false;
            this._altarGuards.clear();
            DoorInstance door1 = DoorParser.getInstance().getDoor(19160016);
            if (door1 != null) {
               door1.openMe();
            }

            DoorInstance door2 = DoorParser.getInstance().getDoor(19160017);
            if (door2 != null) {
               door2.openMe();
            }

            Npc announce = World.getInstance().getNpcById(32051);
            if (announce != null) {
               announce.broadcastPacket(
                  new NpcSay(announce.getObjectId(), 23, announce.getId(), NpcStringId.THE_DOOR_TO_THE_3RD_FLOOR_OF_THE_ALTAR_IS_NOW_OPEN), 10000
               );
            }

            _movieTask = ThreadPoolManager.getInstance().schedule(new AndreasVanHalter.Movie(1), 3000L);
            actor.broadcastPacket(new PlaySound("BS04_A"));
            if (_checkTask != null) {
               _checkTask.cancel(false);
               _checkTask = null;
            }

            _checkTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AndreasVanHalter.CheckAttack(), 3600000L, 3600000L);
         }

         return true;
      }
   }

   @Override
   protected void onEvtDead(Creature killer) {
      Attackable actor = this.getActiveChar();
      if (_checkTask != null) {
         _checkTask.cancel(false);
         _checkTask = null;
      }

      _zone.broadcastPacket(new PlaySound(1, "BS01_D", 1, actor.getObjectId(), actor.getX(), actor.getY(), actor.getZ()));
      _zone.setCanTeleport(true);
      this.cleanUp();
      super.onEvtDead(killer);
   }

   private void firstSpawn() {
      this._guard.add(NpcUtils.spawnSingle(32058, new Location(-20117, -52683, -10974)));
      this._guard.add(NpcUtils.spawnSingle(32059, new Location(-20137, -54371, -11170)));
      this._guard.add(NpcUtils.spawnSingle(32060, new Location(-12710, -52677, -10974)));
      this._guard.add(NpcUtils.spawnSingle(32061, new Location(-12660, -54379, -11170)));
      this._guard.add(NpcUtils.spawnSingle(32062, new Location(-17826, -53426, -11624)));
      this._guard.add(NpcUtils.spawnSingle(32063, new Location(-17068, -53440, -11624)));
      this._guard.add(NpcUtils.spawnSingle(32064, new Location(-16353, -53549, -11624)));
      this._guard.add(NpcUtils.spawnSingle(32065, new Location(-15655, -53869, -11624)));
      this._guard.add(NpcUtils.spawnSingle(32066, new Location(-15005, -53132, -11624)));
      this._guard.add(NpcUtils.spawnSingle(32067, new Location(-16316, -56842, -10900)));
      this._guard.add(NpcUtils.spawnSingle(32068, new Location(-16395, -54055, -10439, 15992)));
      this._ritualOffering = NpcUtils.spawnSingle(32038, new Location(-16386, -53368, -10448, 15992));
      if (this._ritualOffering != null) {
         this._ritualOffering.setIsImmobilized(true);
      }

      this._guard.add(NpcUtils.spawnSingle(32051, new Location(-17248, -54832, -10424, 16384)));
      this._guard.add(NpcUtils.spawnSingle(32051, new Location(-15547, -54835, -10424, 16384)));
      this._guard.add(NpcUtils.spawnSingle(32051, new Location(-18116, -54831, -10579, 16384)));
      this._guard.add(NpcUtils.spawnSingle(32051, new Location(-14645, -54836, -10577, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-18008, -53394, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-17653, -53399, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-17827, -53575, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-18008, -53749, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-17653, -53754, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-17827, -53930, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-18008, -54100, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-17653, -54105, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-17275, -52577, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-16917, -52577, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-16738, -52577, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-17003, -52404, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-17353, -52404, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-17362, -52752, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-17006, -52752, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-17721, -52752, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-17648, -52968, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-17292, -52968, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-16374, -52577, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-16648, -52404, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-16284, -52404, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-16013, -52577, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-15658, -52577, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-15306, -52577, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-15923, -52404, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-15568, -52404, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-15216, -52404, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-15745, -52752, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-15394, -52752, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-15475, -52969, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-15119, -52969, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-15149, -53411, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-14794, -53416, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-14968, -53592, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-15149, -53766, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-14794, -53771, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-14968, -53947, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-15149, -54117, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22175, new Location(-14794, -54122, -10594, 16384)));
      this._altarGuards.add(NpcUtils.spawnSingle(22188, new Location(-16392, -52124, -10592)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16380, -45796, -10726, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16290, -45796, -10726, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16471, -45796, -10726, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16380, -45514, -10726, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16290, -45514, -10726, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16471, -45514, -10726, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16380, -45243, -10726, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16290, -45243, -10726, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16471, -45243, -10726, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16380, -44973, -10726, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16290, -44973, -10726, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16471, -44973, -10726, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16380, -44703, -10726, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16290, -44703, -10726, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16471, -44703, -10726, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16471, -44443, -10726, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16382, -47685, -10822, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16292, -47685, -10822, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16474, -47685, -10822, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16382, -47404, -10822, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16292, -47404, -10822, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16474, -47404, -10822, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16382, -47133, -10822, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16292, -47133, -10822, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16474, -47133, -10822, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16382, -46862, -10822, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16292, -46862, -10822, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16474, -46862, -10822, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16382, -46593, -10822, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16292, -46593, -10822, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16474, -46593, -10822, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16382, -46333, -10822, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16292, -46333, -10822, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16474, -46333, -10822, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16381, -49743, -10918, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16291, -49743, -10918, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16473, -49743, -10918, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16381, -49461, -10918, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16291, -49461, -10918, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16473, -49461, -10918, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16381, -49191, -10918, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16291, -49191, -10918, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16473, -49191, -10918, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16381, -48920, -10918, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16291, -48920, -10918, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16473, -48920, -10918, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16381, -48650, -10918, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16291, -48650, -10918, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16473, -48650, -10918, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16381, -48391, -10918, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16291, -48391, -10918, 16384)));
      this._guard.add(NpcUtils.spawnSingle(22176, new Location(-16473, -48391, -10918, 16384)));
   }

   private void secondSpawn() {
      this._guard.add(NpcUtils.spawnSingle(22189, new Location(-16199, -53591, -10449, 14881)));
      this._guard.add(NpcUtils.spawnSingle(22189, new Location(-16331, -53260, -10449, 6134)));
      this._guard.add(NpcUtils.spawnSingle(22189, new Location(-16479, -53528, -10449, 11646)));
      this._guard.add(NpcUtils.spawnSingle(22190, new Location(-15847, -53336, -10449, 31957)));
      this._guard.add(NpcUtils.spawnSingle(22190, new Location(-16297, -53086, -10449, 7806)));
      this._guard.add(NpcUtils.spawnSingle(22190, new Location(-16712, -53438, -10449, 4083)));
      this._guard.add(NpcUtils.spawnSingle(22191, new Location(-15690, -54030, -10439, 15992)));
      this._guard.add(NpcUtils.spawnSingle(22191, new Location(-16385, -53268, -10439, 15992)));
      this._guard.add(NpcUtils.spawnSingle(22191, new Location(-17150, -54046, -10439, 15992)));
      this._guard.add(NpcUtils.spawnSingle(22192, new Location(-16385, -53268, -10439, 15992)));
      this._guard.add(NpcUtils.spawnSingle(22192, new Location(-17150, -54046, -10439, 15992)));
      this._guard.add(NpcUtils.spawnSingle(22192, new Location(-15690, -54030, -10439, 15992)));
      this._guard.add(NpcUtils.spawnSingle(22193, new Location(-16385, -53268, -10439, 15992)));
      this._guard.add(NpcUtils.spawnSingle(22193, new Location(-17150, -54046, -10439, 15992)));
      this._guard.add(NpcUtils.spawnSingle(22193, new Location(-15690, -54030, -10439, 15992)));
   }

   private void cleanUp() {
      if (_movieTask != null) {
         _movieTask.cancel(false);
         _movieTask = null;
      }

      if (_checkTask != null) {
         _checkTask.cancel(false);
         _checkTask = null;
      }

      for(Npc npc : this._guard) {
         if (npc != null) {
            npc.deleteMe();
         }
      }

      this._guard.clear();

      for(Npc npc : this._altarGuards) {
         if (npc != null) {
            npc.deleteMe();
         }
      }

      this._altarGuards.clear();
      if (this._ritualOffering != null) {
         this._ritualOffering.deleteMe();
         this._ritualOffering = null;
      }

      DoorInstance door1 = DoorParser.getInstance().getDoor(19160016);
      if (door1 != null && door1.isOpen()) {
         door1.closeMe();
      }

      DoorInstance door2 = DoorParser.getInstance().getDoor(19160017);
      if (door2 != null && door2.isOpen()) {
         door2.closeMe();
      }
   }

   private boolean checkAliveGuards() {
      for(Npc guard : this._altarGuards) {
         if (guard != null && !guard.isDead()) {
            return true;
         }
      }

      return false;
   }

   protected List<Player> getPlayersInside() {
      List<Player> list = new ArrayList<>();

      for(Player pl : _zone.getPlayersInside()) {
         list.add(pl);
      }

      return list;
   }

   private class CheckAttack extends RunnableImpl {
      private CheckAttack() {
      }

      @Override
      public void runImpl() {
         Attackable actor = AndreasVanHalter.this.getActiveChar();
         if (actor != null && !actor.isAttackingNow() && !actor.isInCombat()) {
            AndreasVanHalter.this.cleanUp();
            ThreadPoolManager.getInstance().schedule(AndreasVanHalter.this.new NewSpawn(), 10000L);
         }
      }
   }

   private class Movie implements Runnable {
      private int _taskId;
      private List<Player> _players;

      public Movie(int taskId) {
         this._taskId = taskId;
      }

      private void nextMovie(int taskId, long delay) {
         this._taskId = taskId;
         AndreasVanHalter._movieTask = ThreadPoolManager.getInstance().schedule(this, delay);
      }

      private void specialCamera(GameObject npc, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int widescreen, int unk) {
         SpecialCamera packet = new SpecialCamera(npc.getObjectId(), dist, yaw, pitch, time, duration, turn, rise, widescreen, unk);

         for(Player pc : this._players) {
            pc.sendPacket(packet);
         }
      }

      private void enterMovieMode(Player pc) {
         pc.setTarget(null);
         pc.stopMove(null);
         pc.setIsParalyzed(true);
         pc.setIsInvul(true);
         pc.setIsImmobilized(true);
      }

      public void leaveMovieMode(Player pc) {
         pc.setTarget(null);
         pc.stopMove(null);
         pc.setIsParalyzed(false);
         pc.setIsInvul(false);
         pc.setIsImmobilized(false);
      }

      @Override
      public void run() {
         AndreasVanHalter._movieTask = null;
         Attackable actor = AndreasVanHalter.this.getActiveChar();
         switch(this._taskId) {
            case 1:
               this._players = AndreasVanHalter.this.getPlayersInside();

               for(Player pc : this._players) {
                  this.enterMovieMode(pc);
               }

               actor.abortAttack();
               actor.abortCast();
               actor.setHeading(16384);
               actor.setTarget(AndreasVanHalter.this._ritualOffering);
               this.specialCamera(actor, 1650, 90, 89, 0, 15000, 0, 89, 0, 0);
               this.nextMovie(2, 50L);
               break;
            case 2:
               this.specialCamera(actor, 1650, 90, 89, 0, 15000, 0, 89, 0, 0);
               this.nextMovie(3, 4000L);
               break;
            case 3:
               this.specialCamera(actor, 1450, 90, 80, 4000, 15000, 0, 80, 0, 0);
               this.nextMovie(4, 2000L);
               break;
            case 4:
               this.specialCamera(actor, 1250, 90, 70, 4000, 15000, 0, 70, 0, 0);
               this.nextMovie(5, 2000L);
               break;
            case 5:
               this.specialCamera(actor, 1050, 90, 60, 4000, 15000, 0, 60, 0, 0);
               this.nextMovie(6, 2000L);
               break;
            case 6:
               this.specialCamera(actor, 850, 90, 50, 4000, 15000, 0, 45, 0, 0);
               this.nextMovie(7, 2000L);
               break;
            case 7:
               this.specialCamera(actor, 650, 90, 40, 4000, 15000, 0, 30, 0, 0);
               this.nextMovie(8, 2000L);
               break;
            case 8:
               this.specialCamera(actor, 450, 90, 30, 4000, 15000, 0, 15, 0, 0);
               this.nextMovie(9, 2000L);
               break;
            case 9:
               this.specialCamera(actor, 250, 90, 20, 4000, 15000, 0, 1, 0, 0);
               this.nextMovie(12, 2000L);
            case 10:
            case 11:
            default:
               break;
            case 12:
               this.specialCamera(actor, 50, 90, 10, 4000, 15000, 0, 0, 0, 0);
               this.nextMovie(13, 3000L);
               break;
            case 13:
               Skill skill = SkillsParser.getInstance().getInfo(1168, 7);
               if (skill != null) {
                  actor.setTarget(AndreasVanHalter.this._ritualOffering);
                  actor.setIsImmobilized(false);
                  actor.doCast(skill);
                  actor.setIsImmobilized(true);
               }

               this.nextMovie(14, 4700L);
               break;
            case 14:
               AndreasVanHalter.this._ritualOffering.broadcastPacket(new SocialAction(AndreasVanHalter.this._ritualOffering, 1));
               this.nextMovie(15, 2500L);
               break;
            case 15:
               AndreasVanHalter.this.secondSpawn();
               if (AndreasVanHalter.this._ritualOffering != null) {
                  AndreasVanHalter.this._ritualOffering.deleteMe();
                  AndreasVanHalter.this._ritualOffering = null;
               }

               this.specialCamera(actor, 100, 90, 15, 1500, 15000, 0, 0, 0, 0);
               this.nextMovie(16, 3000L);
               break;
            case 16:
               this.specialCamera(actor, 5200, 90, 10, 9500, 6000, 0, 20, 0, 0);
               this.nextMovie(17, 7000L);
               break;
            case 17:
               for(Player pc : this._players) {
                  this.leaveMovieMode(pc);
               }

               this._players = null;
               actor.setIsImmobilized(false);
               actor.setIsInvul(false);
         }
      }
   }

   private class NewSpawn extends RunnableImpl {
      private NewSpawn() {
      }

      @Override
      public void runImpl() {
         AndreasVanHalter.this.firstSpawn();
         AndreasVanHalter.this._firstTimeMove = true;
      }
   }
}
