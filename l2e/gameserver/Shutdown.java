package l2e.gameserver;

import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Broadcast;
import l2e.fake.FakePlayer;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.parser.BotReportParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.database.StreamDatabaseFactory;
import l2e.gameserver.instancemanager.BloodAltarManager;
import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.CastleManorManager;
import l2e.gameserver.instancemanager.CursedWeaponsManager;
import l2e.gameserver.instancemanager.EpicBossManager;
import l2e.gameserver.instancemanager.GlobalVariablesManager;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.instancemanager.ItemAuctionManager;
import l2e.gameserver.instancemanager.ItemsOnGroundManager;
import l2e.gameserver.instancemanager.LakfiManager;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.RaidBossSpawnManager;
import l2e.gameserver.instancemanager.games.FishingChampionship;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Hero;
import l2e.gameserver.model.olympiad.Olympiad;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.communication.AuthServerCommunication;
import l2e.gameserver.network.serverpackets.ServerClose;
import l2e.gameserver.network.serverpackets.SystemMessage;
import org.nio.impl.SelectorThread;

public class Shutdown extends Thread {
   private static Logger _log = Logger.getLogger(Shutdown.class.getName());
   private static Shutdown _counterInstance = null;
   private int _secondsShut;
   private int _shutdownMode;
   public static final int SIGTERM = 0;
   public static final int GM_SHUTDOWN = 1;
   public static final int GM_RESTART = 2;
   public static final int ABORT = 3;
   private static final String[] MODE_TEXT = new String[]{"SIGTERM", "shutting down", "restarting", "aborting"};

   private void SendServerQuit(int seconds) {
      SystemMessage sysm = SystemMessage.getSystemMessage(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS);
      sysm.addNumber(seconds);
      Broadcast.toAllOnlinePlayers(sysm);
   }

   private void SendServerQuitAnnounce(int seconds) {
      Announcements.getInstance().announceToAll("The server will be coming down in " + seconds / 60 + " minutes!");
   }

   public void autoRestart(int time) {
      this._secondsShut = time;
      this.countdown();
      this._shutdownMode = 2;
      System.exit(2);
   }

   protected Shutdown() {
      this._secondsShut = -1;
      this._shutdownMode = 0;
   }

   public Shutdown(int seconds, boolean restart) {
      if (seconds < 0) {
         seconds = 0;
      }

      this._secondsShut = seconds;
      if (restart) {
         this._shutdownMode = 2;
      } else {
         this._shutdownMode = 1;
      }
   }

   @Override
   public void run() {
      if (this == getInstance()) {
         Shutdown.TimeCounter tc = new Shutdown.TimeCounter();
         Shutdown.TimeCounter tc1 = new Shutdown.TimeCounter();
         this.saveData();
         tc.restartCounter();

         try {
            AuthServerCommunication.getInstance().shutdown();
            _log.info("Login Server Communication: has been shutdown(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
         } catch (Throwable var12) {
         }

         try {
            this.disconnectAllCharacters();
            _log.info("All players disconnected and saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
         } catch (Throwable var11) {
         }

         try {
            GameTimeController.getInstance().stopTimer();
            _log.info("Game Time Controller: Timer stopped(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
         } catch (Throwable var10) {
         }

         try {
            ThreadPoolManager.getInstance().shutdown();
            _log.info("Thread Pool Manager: Manager has been shut down(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
         } catch (Throwable var9) {
         }

         try {
            if (GameServer.getInstance() != null) {
               for(SelectorThread<GameClient> st : GameServer.getInstance().getSelectorThreads()) {
                  try {
                     st.shutdown();
                  } catch (Exception var8) {
                     var8.printStackTrace();
                  }
               }
            }

            _log.info("Game Server: Selector thread has been shut down(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
         } catch (Throwable var13) {
         }

         if (!Config.ALLOW_STREAM_SAME_DB) {
            try {
               StreamDatabaseFactory.getInstance().shutdown();
               _log.info("Stream Database Factory: Database connection has been shut down(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
            } catch (Throwable var7) {
            }
         }

         try {
            DatabaseFactory.getInstance().shutdown();
            _log.info("Database Factory: Database connection has been shut down(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
         } catch (Throwable var6) {
         }

         if (getInstance()._shutdownMode == 2) {
            Runtime.getRuntime().halt(2);
         } else {
            Runtime.getRuntime().halt(0);
         }

         _log.info("The server has been successfully shut down in " + tc1.getEstimatedTime() / 1000L + "seconds.");
      } else {
         this.countdown();
         _log.warning("GM shutdown countdown is over. " + MODE_TEXT[this._shutdownMode] + " NOW!");
         switch(this._shutdownMode) {
            case 1:
               getInstance().setMode(1);
               System.exit(0);
               break;
            case 2:
               getInstance().setMode(2);
               System.exit(2);
         }
      }
   }

   public void startShutdown(Player activeChar, int seconds, boolean restart) {
      if (restart) {
         this._shutdownMode = 2;
      } else {
         this._shutdownMode = 1;
      }

      _log.warning(
         "GM: "
            + activeChar.getName()
            + "("
            + activeChar.getObjectId()
            + ") issued shutdown command. "
            + MODE_TEXT[this._shutdownMode]
            + " in "
            + seconds
            + " seconds!"
      );
      if (this._shutdownMode > 0) {
         switch(seconds) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 10:
            case 30:
            case 60:
            case 120:
            case 180:
            case 240:
            case 300:
            case 360:
            case 420:
            case 480:
            case 540:
               break;
            default:
               this.SendServerQuit(seconds);
         }
      }

      if (_counterInstance != null) {
         _counterInstance._abort();
      }

      _counterInstance = new Shutdown(seconds, restart);
      _counterInstance.start();
   }

   public void abort(Player activeChar) {
      _log.warning(
         "GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") issued shutdown ABORT. " + MODE_TEXT[this._shutdownMode] + " has been stopped!"
      );
      if (_counterInstance != null) {
         _counterInstance._abort();
         Announcements _an = Announcements.getInstance();
         _an.announceToAll("Server aborts " + MODE_TEXT[this._shutdownMode] + " and continues normal operation!");
      }
   }

   private void setMode(int mode) {
      this._shutdownMode = mode;
   }

   private void _abort() {
      this._shutdownMode = 3;
   }

   private void countdown() {
      try {
         while(this._secondsShut > 0) {
            switch(this._secondsShut) {
               case 1:
                  this.SendServerQuit(1);
                  break;
               case 2:
                  this.SendServerQuit(2);
                  break;
               case 3:
                  this.SendServerQuit(3);
                  break;
               case 4:
                  this.SendServerQuit(4);
                  break;
               case 5:
                  this.SendServerQuit(5);
                  break;
               case 10:
                  this.SendServerQuit(10);
                  break;
               case 30:
                  this.SendServerQuit(30);
                  break;
               case 60:
                  this.SendServerQuit(60);
                  break;
               case 120:
                  this.SendServerQuit(120);
                  break;
               case 180:
                  this.SendServerQuit(180);
                  break;
               case 240:
                  this.SendServerQuit(240);
                  break;
               case 300:
                  this.SendServerQuit(300);
                  break;
               case 360:
                  this.SendServerQuit(360);
                  break;
               case 420:
                  this.SendServerQuit(420);
                  break;
               case 480:
                  this.SendServerQuit(480);
                  break;
               case 540:
                  this.SendServerQuit(540);
                  break;
               case 600:
                  this.SendServerQuit(600);
                  break;
               case 1800:
                  this.SendServerQuitAnnounce(1800);
                  break;
               case 3600:
                  this.SendServerQuitAnnounce(3600);
                  break;
               case 5400:
                  this.SendServerQuitAnnounce(5400);
                  break;
               case 7200:
                  this.SendServerQuitAnnounce(7200);
            }

            --this._secondsShut;
            Thread.sleep(1000L);
            if (this._shutdownMode == 3) {
               break;
            }
         }
      } catch (InterruptedException var2) {
      }
   }

   private void saveData() {
      switch(this._shutdownMode) {
         case 0:
            _log.info("Shutting down NOW!");
            break;
         case 1:
            _log.info("Shutting down NOW!");
            break;
         case 2:
            _log.info("Restarting NOW!");
      }

      Shutdown.TimeCounter tc = new Shutdown.TimeCounter();
      FishingChampionship.getInstance().shutdown();
      if (!SevenSigns.getInstance().isSealValidationPeriod()) {
         SevenSignsFestival.getInstance().saveFestivalData(false);
         _log.info("SevenSignsFestival: Festival data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
      }

      SevenSigns.getInstance().saveSevenSignsData();
      _log.info("SevenSigns: Seven Signs data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
      SevenSigns.getInstance().saveSevenSignsStatus();
      _log.info("SevenSigns: Seven Signs status saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
      BloodAltarManager.getInstance().saveDb();
      _log.info("BloodAltarManager: All destruction bosses info saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
      RaidBossSpawnManager.getInstance().cleanUp();
      _log.info("RaidBossSpawnManager: All raidboss info saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
      EpicBossManager.getInstance().cleanUp();
      _log.info("EpicBossManager: All Epic Boss info saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
      HellboundManager.getInstance().cleanUp();
      _log.info("Hellbound Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
      LakfiManager.getInstance().stopTimer();
      _log.info("LakfiManager: Stop task timer.");
      ItemAuctionManager.getInstance().shutdown();
      _log.info("Item Auction Manager: All tasks stopped(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
      Olympiad.getInstance().saveOlympiadStatus();
      _log.info("Olympiad System: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
      Hero.getInstance().shutdown();
      _log.info("Hero System: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
      ClanHolder.getInstance().storeClanScore();
      _log.info("Clan System: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
      CursedWeaponsManager.getInstance().saveData();
      _log.info("Cursed Weapons Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
      CastleManorManager.getInstance().save();
      _log.info("Castle Manor Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
      CHSiegeManager.getInstance().onServerShutDown();
      _log.info("CHSiegeManager: Siegable hall attacker lists saved!");
      QuestManager.getInstance().save();
      _log.info("Quest Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
      GlobalVariablesManager.getInstance().storeMe();
      _log.info("Global Variables Manager: Variables saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
      if (Config.SAVE_DROPPED_ITEM) {
         ItemsOnGroundManager.getInstance().saveInDb();
         _log.info("Items On Ground Manager: Data saved(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
         ItemsOnGroundManager.getInstance().cleanUp();
         _log.info("Items On Ground Manager: Cleaned up(" + tc.getEstimatedTimeAndRestartCounter() + "ms).");
      }

      if (Config.BOTREPORT_ENABLE) {
         BotReportParser.getInstance().saveReportedCharData();
         _log.info("Bot Report Data: Sucessfully saved reports to database!");
      }

      try {
         Thread.sleep(2000L);
      } catch (InterruptedException var3) {
      }
   }

   private void disconnectAllCharacters() {
      for(Player player : World.getInstance().getAllPlayers()) {
         try {
            GameClient client = player.getClient();
            if (client != null) {
               client.close(ServerClose.STATIC_PACKET);
               client.setActiveChar(null);
               player.setClient(null);
            }

            if (player.isFakePlayer()) {
               FakePlayer fakePlayer = (FakePlayer)player;
               fakePlayer.despawnPlayer();
            } else {
               player.deleteMe();
            }
         } catch (Throwable var5) {
            _log.log(Level.WARNING, "Failed logour char " + player, var5);
         }
      }
   }

   public int getMode() {
      return this._shutdownMode;
   }

   public static Shutdown getInstance() {
      return Shutdown.SingletonHolder._instance;
   }

   public int getSeconds() {
      return this._secondsShut;
   }

   private static class SingletonHolder {
      protected static final Shutdown _instance = new Shutdown();
   }

   private static final class TimeCounter {
      private long _startTime;

      protected TimeCounter() {
         this.restartCounter();
      }

      protected void restartCounter() {
         this._startTime = System.currentTimeMillis();
      }

      protected long getEstimatedTimeAndRestartCounter() {
         long toReturn = System.currentTimeMillis() - this._startTime;
         this.restartCounter();
         return toReturn;
      }

      protected long getEstimatedTime() {
         return System.currentTimeMillis() - this._startTime;
      }
   }
}
