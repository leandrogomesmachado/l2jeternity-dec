package l2e.gameserver.instancemanager;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.quest.Quest;

public class SoDManager {
   protected static final Logger _log = Logger.getLogger(SoDManager.class.getName());
   private static SoDManager _instance;
   private static long SOD_OPEN_TIME = 43200000L;
   private static long SOD_DEFENCE_TIME = 43200000L;
   private static final int _zone = 60009;
   private static boolean _isOpened = false;
   private static ScheduledFuture<?> _openTimeTask = null;

   public static SoDManager getInstance() {
      if (_instance == null) {
         _instance = new SoDManager();
      }

      return _instance;
   }

   public SoDManager() {
      _log.info("Seed of Destruction Manager: Loaded. Current stage is: " + this.getStage());
      if (!isAttackStage()) {
         if (isDefenceStage()) {
            startDefenceStage(false);
         } else {
            openSeed(getOpenedTimeLimit());
         }
      }
   }

   private String getStage() {
      return isAttackStage() ? "1 (Attack)" : (isDefenceStage() ? "3 (Defence)" : "2 (Open)");
   }

   public static boolean isAttackStage() {
      return getOpenedTimeLimit() <= 0L && getDefenceStageTimeLimit() <= 0L;
   }

   public static boolean isDefenceStage() {
      return getDefenceStageTimeLimit() > 0L;
   }

   public static void addTiatKill() {
      if (isAttackStage()) {
         if (getTiatKills() < Config.SOD_TIAT_KILL_COUNT) {
            ServerVariables.set("Tial_kills", getTiatKills() + 1);
         } else {
            openSeed(SOD_OPEN_TIME);
         }
      }
   }

   public static int getTiatKills() {
      return ServerVariables.getInt("Tial_kills", 0);
   }

   public static long getDefenceStageTimeLimit() {
      return ServerVariables.getLong("SoD_defence", 0L) * 1000L - System.currentTimeMillis();
   }

   public static long getOpenedTimeLimit() {
      return ServerVariables.getLong("SoD_opened", 0L) * 1000L - System.currentTimeMillis();
   }

   public static void teleportIntoSeed(Player p) {
      p.teleToLocation(new Location(-245800, 220488, -12112), true);
   }

   public static void handleDoors(boolean doOpen) {
      for(int i = 12240003; i <= 12240031; ++i) {
         DoorInstance door = DoorParser.getInstance().getDoor(i);
         if (door != null) {
            if (doOpen) {
               door.openMe();
            } else {
               door.closeMe();
            }
         }
      }
   }

   public static void openSeed(long timelimit) {
      if (!_isOpened) {
         _isOpened = true;
         AerialCleftEvent.getInstance().openRegistration();
         ServerVariables.unset("Tial_kills");
         ServerVariables.set("SoD_opened", (System.currentTimeMillis() + timelimit) / 1000L);
         _log.info("Seed of Destruction Manager: Opening the seed for " + Util.formatTime((int)timelimit / 1000));
         SpawnParser.getInstance().spawnGroup("sod_free");
         handleDoors(true);
         _openTimeTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
               SoDManager.startDefenceStage(true);
            }
         }, timelimit);
      }
   }

   public static void startDefenceStage(boolean refreshTime) {
      if (_openTimeTask != null) {
         _openTimeTask.cancel(false);
         _openTimeTask = null;
      }

      if (!_isOpened) {
         AerialCleftEvent.getInstance().openRegistration();
         ServerVariables.unset("Tial_kills");
         SpawnParser.getInstance().spawnGroup("sod_free");
         handleDoors(true);
      }

      _isOpened = true;
      Quest qs = QuestManager.getInstance().getQuest("SoDDefenceStage");
      if (qs != null) {
         if (refreshTime) {
            ServerVariables.set("SoD_defence", (System.currentTimeMillis() + SOD_DEFENCE_TIME) / 1000L);
         }

         _log.info("Seed of Destruction Manager: Seed in defence stage for " + Util.formatTime((int)getDefenceStageTimeLimit() / 1000));
         qs.notifyEvent("StartDefence", null, null);
      } else {
         closeSeed();
      }
   }

   public static void closeSeed() {
      if (_isOpened) {
         if (_openTimeTask != null) {
            _openTimeTask.cancel(false);
            _openTimeTask = null;
         }

         _isOpened = false;
         _log.info("Seed of Destruction Manager: Closing the seed.");
         ServerVariables.unset("SoD_opened");
         ServerVariables.unset("SoD_defence");
         SpawnParser.getInstance().despawnGroup("sod_free");

         for(Player p : ZoneManager.getInstance().getZoneById(60009).getPlayersInside()) {
            if (p != null) {
               p.teleToLocation(-248717, 250260, 4337, true);
            }
         }

         handleDoors(false);
      }
   }

   public static boolean isOpened() {
      return _isOpened;
   }
}
