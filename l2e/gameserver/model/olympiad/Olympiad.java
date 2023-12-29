package l2e.gameserver.model.olympiad;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.commons.time.cron.SchedulingPattern;
import l2e.commons.util.GameSettings;
import l2e.gameserver.Announcements;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.DoubleSessionManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.player.OlympiadTemplate;
import l2e.gameserver.model.entity.Hero;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Olympiad {
   protected static final Logger _log = Logger.getLogger(Olympiad.class.getName());
   protected static final Logger _logResults = Logger.getLogger("olympiad");
   private static final Map<Integer, StatsSet> NOBLES = new ConcurrentHashMap<>();
   private static final List<StatsSet> HEROS_TO_BE = new ArrayList<>();
   private static final Map<Integer, Integer> NOBLES_RANK = new HashMap<>();
   public static final String OLYMPIAD_HTML_PATH = "data/html/olympiad/";
   private static final String OLYMPIAD_LOAD_DATA = "SELECT current_cycle, period, comp_start, comp_end, olympiad_end, validation_end, next_weekly_change FROM olympiad_data WHERE id = 0";
   private static final String OLYMPIAD_SAVE_DATA = "INSERT INTO olympiad_data (id, current_cycle, period, comp_start, comp_end, olympiad_end, validation_end, next_weekly_change) VALUES (0,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE current_cycle=?, period=?, olympiad_end=?, validation_end=?, next_weekly_change=?";
   private static final String OLYMPIAD_UPDATE_COMP_DATA = "UPDATE olympiad_data SET comp_start=?, comp_end=?";
   private static final String OLYMPIAD_LOAD_NOBLES = "SELECT olympiad_nobles.charId, olympiad_nobles.class_id, characters.char_name, olympiad_nobles.olympiad_points, olympiad_nobles.olympiad_points_past, olympiad_nobles.competitions_done, olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost, olympiad_nobles.competitions_drawn, olympiad_nobles.competitions_done_week, olympiad_nobles.competitions_done_week_classed, olympiad_nobles.competitions_done_week_non_classed, olympiad_nobles.competitions_done_week_team FROM olympiad_nobles, characters WHERE characters.charId = olympiad_nobles.charId";
   private static final String OLYMPIAD_SAVE_NOBLES = "INSERT INTO olympiad_nobles (`charId`,`class_id`,`olympiad_points`,`olympiad_points_past`,`competitions_done`,`competitions_won`,`competitions_lost`,`competitions_drawn`, `competitions_done_week`, `competitions_done_week_classed`, `competitions_done_week_non_classed`, `competitions_done_week_team`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
   private static final String OLYMPIAD_UPDATE_NOBLES = "UPDATE olympiad_nobles SET class_id = ?, olympiad_points = ?, olympiad_points_past = ?, competitions_done = ?, competitions_won = ?, competitions_lost = ?, competitions_drawn = ?, competitions_done_week = ?, competitions_done_week_classed = ?, competitions_done_week_non_classed = ?, competitions_done_week_team = ? WHERE charId = ?";
   private static final String OLYMPIAD_GET_HEROS = "SELECT olympiad_nobles.charId, characters.char_name FROM olympiad_nobles, characters WHERE characters.charId = olympiad_nobles.charId AND olympiad_nobles.class_id = ? AND olympiad_nobles.competitions_done >= "
      + Config.ALT_OLY_MIN_MATCHES
      + " AND olympiad_nobles.competitions_won > 0 ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC, olympiad_nobles.competitions_won DESC";
   private static final String GET_ALL_CLASSIFIED_NOBLESS = "SELECT charId from olympiad_nobles_eom WHERE competitions_done >= "
      + Config.ALT_OLY_MIN_MATCHES
      + " ORDER BY olympiad_points DESC, competitions_done DESC, competitions_won DESC";
   private static final String GET_EACH_CLASS_LEADER = "SELECT olympiad_nobles_eom.olympiad_points, olympiad_nobles_eom.competitions_won, olympiad_nobles_eom.competitions_lost, characters.char_name from olympiad_nobles_eom, characters WHERE characters.charId = olympiad_nobles_eom.charId AND olympiad_nobles_eom.class_id = ? AND olympiad_nobles_eom.competitions_done >= "
      + Config.ALT_OLY_MIN_MATCHES
      + " ORDER BY olympiad_nobles_eom.olympiad_points DESC, olympiad_nobles_eom.competitions_done DESC, olympiad_nobles_eom.competitions_won DESC LIMIT 10";
   private static final String GET_EACH_CLASS_LEADER_CURRENT = "SELECT olympiad_nobles.olympiad_points, olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost, characters.char_name from olympiad_nobles, characters WHERE characters.charId = olympiad_nobles.charId AND olympiad_nobles.class_id = ? AND olympiad_nobles.competitions_done >= "
      + Config.ALT_OLY_MIN_MATCHES
      + " ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC, olympiad_nobles.competitions_won DESC LIMIT 10";
   private static final String GET_EACH_CLASS_LEADER_SOULHOUND = "SELECT olympiad_nobles_eom.olympiad_points, olympiad_nobles_eom.competitions_won, olympiad_nobles_eom.competitions_lost, characters.char_name from olympiad_nobles_eom, characters WHERE characters.charId = olympiad_nobles_eom.charId AND (olympiad_nobles_eom.class_id = ? OR olympiad_nobles_eom.class_id = 133) AND olympiad_nobles_eom.competitions_done >= "
      + Config.ALT_OLY_MIN_MATCHES
      + " ORDER BY olympiad_nobles_eom.olympiad_points DESC, olympiad_nobles_eom.competitions_done DESC, olympiad_nobles_eom.competitions_won DESC LIMIT 10";
   private static final String GET_EACH_CLASS_LEADER_CURRENT_SOULHOUND = "SELECT olympiad_nobles.olympiad_points, olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost, characters.char_name from olympiad_nobles, characters WHERE characters.charId = olympiad_nobles.charId AND (olympiad_nobles.class_id = ? OR olympiad_nobles.class_id = 133) AND olympiad_nobles.competitions_done >= "
      + Config.ALT_OLY_MIN_MATCHES
      + " ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC, olympiad_nobles.competitions_won DESC LIMIT 10";
   private static final String OLYMPIAD_CALCULATE_POINTS = "UPDATE `olympiad_nobles` SET `olympiad_points_past` = `olympiad_points` WHERE `competitions_done` >= ?";
   private static final String OLYMPIAD_CLEANUP_NOBLES = "UPDATE `olympiad_nobles` SET `olympiad_points` = ?, `competitions_done` = 0, `competitions_won` = 0, `competitions_lost` = 0, `competitions_drawn` = 0, `competitions_done_week` = 0, `competitions_done_week_classed` = 0, `competitions_done_week_non_classed` = 0, `competitions_done_week_team` = 0";
   private static final String OLYMPIAD_MONTH_CLEAR = "TRUNCATE olympiad_nobles_eom";
   private static final String OLYMPIAD_MONTH_CREATE = "INSERT INTO olympiad_nobles_eom SELECT charId, class_id, olympiad_points, competitions_done, competitions_won, competitions_lost, competitions_drawn FROM olympiad_nobles";
   private static final int[] HERO_IDS = new int[]{
      88,
      89,
      90,
      91,
      92,
      93,
      94,
      95,
      96,
      97,
      98,
      99,
      100,
      101,
      102,
      103,
      104,
      105,
      106,
      107,
      108,
      109,
      110,
      111,
      112,
      113,
      114,
      115,
      116,
      117,
      118,
      131,
      132,
      133,
      134
   };
   protected static final int DEFAULT_POINTS = Config.ALT_OLY_START_POINTS;
   protected static final int WEEKLY_POINTS = Config.ALT_OLY_WEEKLY_POINTS;
   public static final String CHAR_ID = "charId";
   public static final String CLASS_ID = "class_id";
   public static final String CHAR_NAME = "char_name";
   public static final String POINTS = "olympiad_points";
   public static final String POINTS_PAST = "olympiad_points_past";
   public static final String COMP_DONE = "competitions_done";
   public static final String COMP_WON = "competitions_won";
   public static final String COMP_LOST = "competitions_lost";
   public static final String COMP_DRAWN = "competitions_drawn";
   public static final String COMP_DONE_WEEK = "competitions_done_week";
   public static final String COMP_DONE_WEEK_CLASSED = "competitions_done_week_classed";
   public static final String COMP_DONE_WEEK_NON_CLASSED = "competitions_done_week_non_classed";
   public static final String COMP_DONE_WEEK_TEAM = "competitions_done_week_team";
   protected long _olympiadEnd;
   protected long _validationEnd;
   protected int _period;
   protected long _nextWeeklyChange;
   protected int _currentCycle;
   private long _compEnd;
   private long _compStart;
   protected static boolean _inCompPeriod;
   protected static boolean _compStarted = false;
   protected ScheduledFuture<?> _scheduledCompStart;
   protected ScheduledFuture<?> _scheduledCompEnd;
   protected ScheduledFuture<?> _scheduledOlympiadEnd;
   protected ScheduledFuture<?> _scheduledWeeklyTask;
   protected ScheduledFuture<?> _scheduledValdationTask;
   protected ScheduledFuture<?> _gameManager = null;
   protected ScheduledFuture<?> _gameAnnouncer = null;

   public static Olympiad getInstance() {
      return Olympiad.SingletonHolder._instance;
   }

   protected Olympiad() {
      this.load();
      DoubleSessionManager.getInstance().registerEvent(100);
      if (this._period == 0) {
         this.init();
      }
   }

   private void load() {
      NOBLES.clear();
      boolean loaded = false;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT current_cycle, period, comp_start, comp_end, olympiad_end, validation_end, next_weekly_change FROM olympiad_data WHERE id = 0"
         );
         ResultSet rset = statement.executeQuery();
      ) {
         while(rset.next()) {
            this._currentCycle = rset.getInt("current_cycle");
            this._period = rset.getInt("period");
            this._olympiadEnd = rset.getLong("olympiad_end");
            this._compStart = rset.getLong("comp_start");
            this._compEnd = rset.getLong("comp_end");
            this._validationEnd = rset.getLong("validation_end");
            this._nextWeeklyChange = rset.getLong("next_weekly_change");
            loaded = true;
         }
      } catch (Exception var251) {
         _log.log(Level.WARNING, "Olympiad: Error loading olympiad data from database: ", (Throwable)var251);
      }

      if (!loaded) {
         _log.log(Level.INFO, "Olympiad: failed to load data from database, trying to load from file.");
         GameSettings OlympiadProperties = new GameSettings();

         try {
            Object statement = null;

            try (InputStream is = new FileInputStream("./config/main/olympiad.ini")) {
               OlympiadProperties.load(is);
            }
         } catch (Exception var236) {
            _log.log(Level.SEVERE, "Olympiad: Error loading olympiad.ini: ", (Throwable)var236);
            return;
         }

         this._currentCycle = Integer.parseInt(OlympiadProperties.getProperty("CurrentCycle", "1"));
         this._period = Integer.parseInt(OlympiadProperties.getProperty("Period", "0"));
         this._olympiadEnd = Long.parseLong(OlympiadProperties.getProperty("OlympiadEnd", "0"));
         this._validationEnd = Long.parseLong(OlympiadProperties.getProperty("ValidationEnd", "0"));
         this._nextWeeklyChange = Long.parseLong(OlympiadProperties.getProperty("NextWeeklyChange", "0"));
      }

      switch(this._period) {
         case 0:
            if (this._olympiadEnd != 0L && this._olympiadEnd >= Calendar.getInstance().getTimeInMillis()) {
               this.scheduleWeeklyChange();
            } else {
               this.setNewOlympiadEnd();
            }
            break;
         case 1:
            if (this._validationEnd > Calendar.getInstance().getTimeInMillis()) {
               this._scheduledValdationTask = ThreadPoolManager.getInstance().schedule(new Olympiad.ValidationEndTask(), this.getMillisToValidationEnd());
            } else {
               ++this._currentCycle;
               this._period = 0;
               this.cleanupNobles();
               this.setNewOlympiadEnd();
            }
            break;
         default:
            _log.warning("Olympiad: Omg something went wrong in loading!! Period = " + this._period);
            return;
      }

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "SELECT olympiad_nobles.charId, olympiad_nobles.class_id, characters.char_name, olympiad_nobles.olympiad_points, olympiad_nobles.olympiad_points_past, olympiad_nobles.competitions_done, olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost, olympiad_nobles.competitions_drawn, olympiad_nobles.competitions_done_week, olympiad_nobles.competitions_done_week_classed, olympiad_nobles.competitions_done_week_non_classed, olympiad_nobles.competitions_done_week_team FROM olympiad_nobles, characters WHERE characters.charId = olympiad_nobles.charId"
         );
         ResultSet rset = statement.executeQuery();
      ) {
         while(rset.next()) {
            StatsSet statData = new StatsSet();
            statData.set("class_id", rset.getInt("class_id"));
            statData.set("char_name", rset.getString("char_name"));
            statData.set("olympiad_points", rset.getInt("olympiad_points"));
            statData.set("olympiad_points_past", rset.getInt("olympiad_points_past"));
            statData.set("competitions_done", rset.getInt("competitions_done"));
            statData.set("competitions_won", rset.getInt("competitions_won"));
            statData.set("competitions_lost", rset.getInt("competitions_lost"));
            statData.set("competitions_drawn", rset.getInt("competitions_drawn"));
            statData.set("competitions_done_week", rset.getInt("competitions_done_week"));
            statData.set("competitions_done_week_classed", rset.getInt("competitions_done_week_classed"));
            statData.set("competitions_done_week_non_classed", rset.getInt("competitions_done_week_non_classed"));
            statData.set("competitions_done_week_team", rset.getInt("competitions_done_week_team"));
            statData.set("to_save", false);
            addNobleStats(rset.getInt("charId"), statData);
         }
      } catch (Exception var244) {
         _log.log(Level.WARNING, "Olympiad: Error loading noblesse data from database: ", (Throwable)var244);
      }

      synchronized(this) {
         if (this._period == 0) {
            _log.info("Olympiad: Currently in Olympiad period.");
            _log.info("Olympiad: Olympiad period will end " + new Date(this._olympiadEnd));
         } else {
            _log.info("Olympiad: Currently in Validation period.");
            _log.info("Olympiad: Validation period will end " + new Date(this._validationEnd));
         }

         if (this._period == 0) {
            _log.info("Olympiad: Next weekly battle and point datas " + new Date(this._nextWeeklyChange));
         }
      }

      _log.info("Olympiad: Loaded " + NOBLES.size() + " nobleses.");
   }

   public void loadNoblesRank() {
      NOBLES_RANK.clear();
      Map<Integer, Integer> tmpPlace = new HashMap<>();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(GET_ALL_CLASSIFIED_NOBLESS);
         ResultSet rset = statement.executeQuery();
      ) {
         int place = 1;

         while(rset.next()) {
            tmpPlace.put(rset.getInt("charId"), place++);
         }
      } catch (Exception var60) {
         _log.log(Level.WARNING, "Olympiad: Error loading noblesse data from database for Ranking: ", (Throwable)var60);
      }

      int rank1 = (int)Math.round((double)tmpPlace.size() * 0.01);
      int rank2 = (int)Math.round((double)tmpPlace.size() * 0.1);
      int rank3 = (int)Math.round((double)tmpPlace.size() * 0.25);
      int rank4 = (int)Math.round((double)tmpPlace.size() * 0.5);
      if (rank1 == 0) {
         rank1 = 1;
         ++rank2;
         ++rank3;
         ++rank4;
      }

      for(Entry<Integer, Integer> chr : tmpPlace.entrySet()) {
         if (chr.getValue() <= rank1) {
            NOBLES_RANK.put(chr.getKey(), 1);
         } else if (tmpPlace.get(chr.getKey()) <= rank2) {
            NOBLES_RANK.put(chr.getKey(), 2);
         } else if (tmpPlace.get(chr.getKey()) <= rank3) {
            NOBLES_RANK.put(chr.getKey(), 3);
         } else if (tmpPlace.get(chr.getKey()) <= rank4) {
            NOBLES_RANK.put(chr.getKey(), 4);
         } else {
            NOBLES_RANK.put(chr.getKey(), 5);
         }
      }
   }

   protected void init() {
      if (this._period != 1) {
         if (this._compStart < System.currentTimeMillis() && this._compEnd < System.currentTimeMillis()) {
            SchedulingPattern timePattern = new SchedulingPattern(Config.ALT_OLY_START_TIME);
            this._compStart = timePattern.next(System.currentTimeMillis());
            this._compEnd = this._compStart + Config.ALT_OLY_CPERIOD * 3600000L;
            this.updateCompDbStatus();
         }

         if (this._scheduledOlympiadEnd != null) {
            this._scheduledOlympiadEnd.cancel(true);
         }

         this._scheduledOlympiadEnd = ThreadPoolManager.getInstance().schedule(new Olympiad.OlympiadEndTask(), this.getMillisToOlympiadEnd());
         this.updateCompStatus();
         this.loadNoblesRank();
      }
   }

   private void updateCompDbStatus() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE olympiad_data SET comp_start=?, comp_end=?");
         statement.setLong(1, this._compStart);
         statement.setLong(2, this._compEnd);
         statement.execute();
         statement.close();
      } catch (Exception var14) {
         _log.log(Level.WARNING, "Error could not update comp status: " + var14.getMessage(), (Throwable)var14);
      }
   }

   protected static int getNobleCount() {
      return NOBLES.size();
   }

   protected static StatsSet getNobleStats(int playerId) {
      return NOBLES.get(playerId);
   }

   private void updateCompStatus() {
      synchronized(this) {
         this.getMillisToCompBegin();
         _log.info("Olympiad: Battles will begin: " + new Date(this._compStart));
      }

      this._scheduledCompStart = ThreadPoolManager.getInstance().schedule(new Runnable() {
         @Override
         public void run() {
            if (!Olympiad.this.isOlympiadEnd()) {
               Olympiad._inCompPeriod = true;
               Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_STARTED));
               Olympiad._log.info("Olympiad: Olympiad game started.");
               Olympiad._logResults.info("Result,Player1,Player2,Player1 HP,Player2 HP,Player1 Damage,Player2 Damage,Points,Classed");
               Olympiad.this._gameManager = ThreadPoolManager.getInstance().scheduleAtFixedRate(OlympiadGameManager.getInstance(), 30000L, 30000L);
               if (Config.ALT_OLY_ANNOUNCE_GAMES) {
                  Olympiad.this._gameAnnouncer = ThreadPoolManager.getInstance().scheduleAtFixedRate(new OlympiadAnnouncer(), 30000L, 500L);
               }

               long regEnd = Olympiad.this.getMillisToCompEnd() - 600000L;
               if (regEnd > 0L) {
                  ThreadPoolManager.getInstance().schedule(new Runnable() {
                     @Override
                     public void run() {
                        Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_REGISTRATION_PERIOD_ENDED));
                     }
                  }, regEnd);
               }

               Olympiad.this._scheduledCompEnd = ThreadPoolManager.getInstance().schedule(new Runnable() {
                  @Override
                  public void run() {
                     if (!Olympiad.this.isOlympiadEnd()) {
                        Olympiad._inCompPeriod = false;
                        Announcements.getInstance().announceToAll(SystemMessage.getSystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_ENDED));
                        Olympiad._log.info("Olympiad: Olympiad game ended.");

                        while(OlympiadGameManager.getInstance().isBattleStarted()) {
                           try {
                              Thread.sleep(60000L);
                           } catch (InterruptedException var2) {
                           }
                        }

                        if (Olympiad.this._gameManager != null) {
                           Olympiad.this._gameManager.cancel(false);
                           Olympiad.this._gameManager = null;
                        }

                        if (Olympiad.this._gameAnnouncer != null) {
                           Olympiad.this._gameAnnouncer.cancel(false);
                           Olympiad.this._gameAnnouncer = null;
                        }

                        Olympiad.this.saveOlympiadStatus();
                        Olympiad.this.init();
                     }
                  }
               }, Olympiad.this.getMillisToCompEnd());
            }
         }
      }, this.getMillisToCompBegin());
   }

   private long getMillisToOlympiadEnd() {
      return this._olympiadEnd - Calendar.getInstance().getTimeInMillis();
   }

   public long getOlympiadEndDate() {
      return this._olympiadEnd;
   }

   public void manualSelectHeroes() {
      if (this._scheduledOlympiadEnd != null) {
         this._scheduledOlympiadEnd.cancel(true);
      }

      this._scheduledOlympiadEnd = ThreadPoolManager.getInstance().schedule(new Olympiad.OlympiadEndTask(), 0L);
   }

   public void manualStartNewOlympiad() {
      if (this._scheduledValdationTask != null) {
         this._scheduledValdationTask.cancel(true);
      }

      this._scheduledValdationTask = ThreadPoolManager.getInstance().schedule(new Olympiad.ValidationEndTask(), 0L);
   }

   protected long getMillisToValidationEnd() {
      return this._validationEnd > Calendar.getInstance().getTimeInMillis() ? this._validationEnd - Calendar.getInstance().getTimeInMillis() : 10L;
   }

   public long getValidationEndDate() {
      return this._validationEnd;
   }

   public boolean isOlympiadEnd() {
      return this._period != 0;
   }

   protected void setNewOlympiadEnd() {
      SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_STARTED);
      sm.addNumber(this._currentCycle);
      Announcements.getInstance().announceToAll(sm);

      SchedulingPattern cronTime;
      try {
         cronTime = new SchedulingPattern(Config.OLYMPIAD_PERIOD);
      } catch (SchedulingPattern.InvalidPatternException var4) {
         return;
      }

      this._olympiadEnd = cronTime.next(System.currentTimeMillis());
      this._nextWeeklyChange = this.getWeeklyChangeDate();
      this.scheduleWeeklyChange();
   }

   private long getWeeklyChangeDate() {
      Calendar nextChange = Calendar.getInstance();
      nextChange.add(10, (int)Config.ALT_OLY_WPERIOD);
      if (Config.ALT_OLY_WPERIOD == 168L) {
         nextChange.set(7, 2);
      }

      nextChange.set(9, 0);
      nextChange.set(10, 12);
      nextChange.set(12, 0);
      nextChange.set(13, 0);
      return nextChange.getTimeInMillis();
   }

   public boolean inCompPeriod() {
      return _inCompPeriod;
   }

   private long getMillisToCompBegin() {
      if (this._compStart < System.currentTimeMillis() && this._compEnd > System.currentTimeMillis()) {
         return 10L;
      } else {
         return this._compStart > System.currentTimeMillis() ? this._compStart - System.currentTimeMillis() : this.setNewCompBegin();
      }
   }

   private long setNewCompBegin() {
      SchedulingPattern timePattern = new SchedulingPattern(Config.ALT_OLY_START_TIME);
      this._compStart = timePattern.next(System.currentTimeMillis());
      this._compEnd = this._compStart + Config.ALT_OLY_CPERIOD * 3600000L;
      this.updateCompDbStatus();
      return this._compStart - System.currentTimeMillis();
   }

   protected long getMillisToCompEnd() {
      return this._compEnd - Calendar.getInstance().getTimeInMillis();
   }

   private long getMillisToWeekChange() {
      return this._nextWeeklyChange > Calendar.getInstance().getTimeInMillis()
         ? this._nextWeeklyChange - Calendar.getInstance().getTimeInMillis()
         : this.getWeeklyChangeDate();
   }

   private void scheduleWeeklyChange() {
      this._scheduledWeeklyTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
         @Override
         public void run() {
            Olympiad.this.addWeeklyPoints();
            Olympiad._log.info("Olympiad: Added weekly points to nobles.");
            Olympiad.this.resetWeeklyMatches();
            Olympiad._log.info("Olympiad: Reset weekly matches to nobles.");
            Olympiad.this._nextWeeklyChange = Olympiad.this.getWeeklyChangeDate();
         }
      }, this.getMillisToWeekChange(), this.getWeeklyChangeDate());
   }

   protected synchronized void addWeeklyPoints() {
      if (this._period != 1) {
         for(StatsSet nobleInfo : NOBLES.values()) {
            int currentPoints = nobleInfo.getInteger("olympiad_points");
            currentPoints += WEEKLY_POINTS;
            nobleInfo.set("olympiad_points", currentPoints);
         }
      }
   }

   protected synchronized void resetWeeklyMatches() {
      if (this._period != 1) {
         for(StatsSet nobleInfo : NOBLES.values()) {
            nobleInfo.set("competitions_done_week", 0);
            nobleInfo.set("competitions_done_week_classed", 0);
            nobleInfo.set("competitions_done_week_non_classed", 0);
            nobleInfo.set("competitions_done_week_team", 0);
         }
      }
   }

   public int getCurrentCycle() {
      return this._currentCycle;
   }

   public boolean playerInStadia(Player player) {
      return ZoneManager.getInstance().getOlympiadStadium(player) != null;
   }

   protected synchronized void saveNobleData() {
      if (!NOBLES.isEmpty()) {
         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            for(Entry<Integer, StatsSet> entry : NOBLES.entrySet()) {
               StatsSet nobleInfo = entry.getValue();
               if (nobleInfo != null) {
                  int charId = entry.getKey();
                  int classId = nobleInfo.getInteger("class_id");
                  int points = nobleInfo.getInteger("olympiad_points");
                  int points_past = nobleInfo.getInteger("olympiad_points_past");
                  int compDone = nobleInfo.getInteger("competitions_done");
                  int compWon = nobleInfo.getInteger("competitions_won");
                  int compLost = nobleInfo.getInteger("competitions_lost");
                  int compDrawn = nobleInfo.getInteger("competitions_drawn");
                  int compDoneWeek = nobleInfo.getInteger("competitions_done_week");
                  int compDoneWeekClassed = nobleInfo.getInteger("competitions_done_week_classed");
                  int compDoneWeekNonClassed = nobleInfo.getInteger("competitions_done_week_non_classed");
                  int compDoneWeekTeam = nobleInfo.getInteger("competitions_done_week_team");
                  boolean toSave = nobleInfo.getBool("to_save");

                  try (PreparedStatement statement = con.prepareStatement(
                        toSave
                           ? "INSERT INTO olympiad_nobles (`charId`,`class_id`,`olympiad_points`,`olympiad_points_past`,`competitions_done`,`competitions_won`,`competitions_lost`,`competitions_drawn`, `competitions_done_week`, `competitions_done_week_classed`, `competitions_done_week_non_classed`, `competitions_done_week_team`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"
                           : "UPDATE olympiad_nobles SET class_id = ?, olympiad_points = ?, olympiad_points_past = ?, competitions_done = ?, competitions_won = ?, competitions_lost = ?, competitions_drawn = ?, competitions_done_week = ?, competitions_done_week_classed = ?, competitions_done_week_non_classed = ?, competitions_done_week_team = ? WHERE charId = ?"
                     )) {
                     if (toSave) {
                        statement.setInt(1, charId);
                        statement.setInt(2, classId);
                        statement.setInt(3, points);
                        statement.setInt(4, points_past);
                        statement.setInt(5, compDone);
                        statement.setInt(6, compWon);
                        statement.setInt(7, compLost);
                        statement.setInt(8, compDrawn);
                        statement.setInt(9, compDoneWeek);
                        statement.setInt(10, compDoneWeekClassed);
                        statement.setInt(11, compDoneWeekNonClassed);
                        statement.setInt(12, compDoneWeekTeam);
                        nobleInfo.set("to_save", false);
                     } else {
                        statement.setInt(1, classId);
                        statement.setInt(2, points);
                        statement.setInt(3, points_past);
                        statement.setInt(4, compDone);
                        statement.setInt(5, compWon);
                        statement.setInt(6, compLost);
                        statement.setInt(7, compDrawn);
                        statement.setInt(8, compDoneWeek);
                        statement.setInt(9, compDoneWeekClassed);
                        statement.setInt(10, compDoneWeekNonClassed);
                        statement.setInt(11, compDoneWeekTeam);
                        statement.setInt(12, charId);
                     }

                     statement.execute();
                  }
               }
            }
         } catch (SQLException var49) {
            _log.log(Level.SEVERE, "Olympiad: Failed to save noblesse data to database: ", (Throwable)var49);
         }
      }
   }

   public void saveOlympiadStatus() {
      this.saveNobleData();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "INSERT INTO olympiad_data (id, current_cycle, period, comp_start, comp_end, olympiad_end, validation_end, next_weekly_change) VALUES (0,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE current_cycle=?, period=?, olympiad_end=?, validation_end=?, next_weekly_change=?"
         );
      ) {
         statement.setInt(1, this._currentCycle);
         statement.setInt(2, this._period);
         statement.setLong(3, this._compStart);
         statement.setLong(4, this._compEnd);
         statement.setLong(5, this._olympiadEnd);
         statement.setLong(6, this._validationEnd);
         statement.setLong(7, this._nextWeeklyChange);
         statement.setInt(8, this._currentCycle);
         statement.setInt(9, this._period);
         statement.setLong(10, this._olympiadEnd);
         statement.setLong(11, this._validationEnd);
         statement.setLong(12, this._nextWeeklyChange);
         statement.execute();
      } catch (SQLException var33) {
         _log.log(Level.SEVERE, "Olympiad: Failed to save olympiad data to database: ", (Throwable)var33);
      }
   }

   protected void updateMonthlyData() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps1 = con.prepareStatement("TRUNCATE olympiad_nobles_eom");
         PreparedStatement ps2 = con.prepareStatement(
            "INSERT INTO olympiad_nobles_eom SELECT charId, class_id, olympiad_points, competitions_done, competitions_won, competitions_lost, competitions_drawn FROM olympiad_nobles"
         );
      ) {
         ps1.execute();
         ps2.execute();
      } catch (SQLException var134) {
         _log.log(Level.SEVERE, "Olympiad: Failed to update monthly noblese data: ", (Throwable)var134);
      }

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("UPDATE `olympiad_nobles` SET `olympiad_points_past` = `olympiad_points` WHERE `competitions_done` >= ?");
      ) {
         ps.setInt(1, Config.ALT_OLY_MIN_MATCHES);
         ps.execute();
      } catch (SQLException var128) {
         _log.log(Level.SEVERE, "Olympiad: Failed to calculate noblese points: ", (Throwable)var128);
      }

      for(Integer nobleId : NOBLES.keySet()) {
         StatsSet nobleInfo = NOBLES.get(nobleId);
         int points = nobleInfo.getInteger("olympiad_points");
         int compDone = nobleInfo.getInteger("competitions_done");
         if (compDone >= Config.ALT_OLY_MIN_MATCHES) {
            nobleInfo.set("olympiad_points_past", points);
         } else {
            nobleInfo.set("olympiad_points_past", 0);
         }
      }
   }

   protected void sortHerosToBe() {
      if (this._period == 1) {
         _logResults.info("Noble,charid,classid,compDone,points");

         for(Entry<Integer, StatsSet> entry : NOBLES.entrySet()) {
            StatsSet nobleInfo = entry.getValue();
            if (nobleInfo != null) {
               int charId = entry.getKey();
               int classId = nobleInfo.getInteger("class_id");
               String charName = nobleInfo.getString("char_name");
               int points = nobleInfo.getInteger("olympiad_points");
               int compDone = nobleInfo.getInteger("competitions_done");
               LogRecord record = new LogRecord(Level.INFO, charName);
               record.setParameters(new Object[]{charId, classId, compDone, points});
               _logResults.log(record);
            }
         }

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(OLYMPIAD_GET_HEROS);
         ) {
            List<StatsSet> soulHounds = new ArrayList<>();

            for(int element : HERO_IDS) {
               statement.setInt(1, element);

               try (ResultSet rset = statement.executeQuery()) {
                  if (rset.next()) {
                     StatsSet hero = new StatsSet();
                     hero.set("class_id", element);
                     hero.set("charId", rset.getInt("charId"));
                     hero.set("char_name", rset.getString("char_name"));
                     if (element != 132 && element != 133) {
                        LogRecord record = new LogRecord(Level.INFO, "Hero " + hero.getString("char_name"));
                        record.setParameters(new Object[]{hero.getInteger("charId"), hero.getInteger("class_id")});
                        _logResults.log(record);
                        HEROS_TO_BE.add(hero);
                     } else {
                        hero = NOBLES.get(hero.getInteger("charId"));
                        hero.set("charId", rset.getInt("charId"));
                        soulHounds.add(hero);
                     }
                  }
               }
            }

            switch(soulHounds.size()) {
               case 0:
               default:
                  break;
               case 1: {
                  StatsSet hero = new StatsSet();
                  StatsSet winner = soulHounds.get(0);
                  hero.set("class_id", winner.getInteger("class_id"));
                  hero.set("charId", winner.getInteger("charId"));
                  hero.set("char_name", winner.getString("char_name"));
                  LogRecord record = new LogRecord(Level.INFO, "Hero " + hero.getString("char_name"));
                  record.setParameters(new Object[]{hero.getInteger("charId"), hero.getInteger("class_id")});
                  _logResults.log(record);
                  HEROS_TO_BE.add(hero);
                  break;
               }
               case 2: {
                  StatsSet hero = new StatsSet();
                  StatsSet hero1 = soulHounds.get(0);
                  StatsSet hero2 = soulHounds.get(1);
                  int hero1Points = hero1.getInteger("olympiad_points");
                  int hero2Points = hero2.getInteger("olympiad_points");
                  int hero1Comps = hero1.getInteger("competitions_done");
                  int hero2Comps = hero2.getInteger("competitions_done");
                  int hero1Wins = hero1.getInteger("competitions_won");
                  int hero2Wins = hero2.getInteger("competitions_won");
                  StatsSet winner;
                  if (hero1Points > hero2Points) {
                     winner = hero1;
                  } else if (hero2Points > hero1Points) {
                     winner = hero2;
                  } else if (hero1Comps > hero2Comps) {
                     winner = hero1;
                  } else if (hero2Comps > hero1Comps) {
                     winner = hero2;
                  } else if (hero1Wins > hero2Wins) {
                     winner = hero1;
                  } else {
                     winner = hero2;
                  }

                  hero.set("class_id", winner.getInteger("class_id"));
                  hero.set("charId", winner.getInteger("charId"));
                  hero.set("char_name", winner.getString("char_name"));
                  LogRecord record = new LogRecord(Level.INFO, "Hero " + hero.getString("char_name"));
                  record.setParameters(new Object[]{hero.getInteger("charId"), hero.getInteger("class_id")});
                  _logResults.log(record);
                  HEROS_TO_BE.add(hero);
               }
            }
         } catch (SQLException var66) {
            _log.warning("Olympiad: Couldnt load heros from DB");
         }
      }
   }

   public List<OlympiadTemplate> getClassLeaderBoard(int classId) {
      List<OlympiadTemplate> list = new ArrayList<>();
      int rank = 1;
      String query = Config.ALT_OLY_SHOW_MONTHLY_WINNERS
         ? (classId == 132 ? GET_EACH_CLASS_LEADER_SOULHOUND : GET_EACH_CLASS_LEADER)
         : (classId == 132 ? GET_EACH_CLASS_LEADER_CURRENT_SOULHOUND : GET_EACH_CLASS_LEADER_CURRENT);

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement(query);
      ) {
         ps.setInt(1, classId);

         try (ResultSet rset = ps.executeQuery()) {
            while(rset.next()) {
               long points = rset.getLong("olympiad_points");
               double win = rset.getDouble("competitions_won");
               double lose = rset.getDouble("competitions_lost");
               double mod = win / (win + lose) * 100.0;
               list.add(new OlympiadTemplate(rank, rset.getString("char_name"), points, (int)win, (int)lose, (int)mod));
               ++rank;
            }
         }
      } catch (SQLException var70) {
         _log.warning("Olympiad: Couldn't load olympiad leaders from DB!");
      }

      return list;
   }

   public int getNoblessePasses(Player player, boolean clear) {
      if (player != null && !NOBLES_RANK.isEmpty()) {
         int objId = player.getObjectId();
         if (!NOBLES_RANK.containsKey(objId)) {
            return 0;
         } else {
            StatsSet noble = NOBLES.get(objId);
            if (noble != null && noble.getInteger("olympiad_points_past") != 0) {
               int rank = NOBLES_RANK.get(objId);
               int points = !player.isHero() && !Hero.getInstance().isInactiveHero(player.getObjectId()) ? 0 : Config.ALT_OLY_HERO_POINTS;
               switch(rank) {
                  case 1:
                     points += Config.ALT_OLY_RANK1_POINTS;
                     break;
                  case 2:
                     points += Config.ALT_OLY_RANK2_POINTS;
                     break;
                  case 3:
                     points += Config.ALT_OLY_RANK3_POINTS;
                     break;
                  case 4:
                     points += Config.ALT_OLY_RANK4_POINTS;
                     break;
                  case 5:
                  default:
                     points += Config.ALT_OLY_RANK5_POINTS;
               }

               if (clear) {
                  noble.set("olympiad_points_past", 0);
               }

               return points * Config.ALT_OLY_GP_PER_POINT;
            } else {
               return 0;
            }
         }
      } else {
         return 0;
      }
   }

   public int getNoblePoints(int objId) {
      return !NOBLES.containsKey(objId) ? 0 : NOBLES.get(objId).getInteger("olympiad_points");
   }

   public int getNoblePointsPast(int objId) {
      return !NOBLES.containsKey(objId) ? 0 : NOBLES.get(objId).getInteger("olympiad_points_past");
   }

   public int getLastNobleOlympiadPoints(int objId) {
      int result = 0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT olympiad_points FROM olympiad_nobles_eom WHERE charId = ?");
      ) {
         ps.setInt(1, objId);

         try (ResultSet rs = ps.executeQuery()) {
            if (rs.first()) {
               result = rs.getInt(1);
            }
         }
      } catch (Exception var61) {
         _log.log(Level.WARNING, "Could not load last olympiad points:", (Throwable)var61);
      }

      return result;
   }

   public int getCompetitionDone(int objId) {
      return !NOBLES.containsKey(objId) ? 0 : NOBLES.get(objId).getInteger("competitions_done");
   }

   public int getCompetitionWon(int objId) {
      return !NOBLES.containsKey(objId) ? 0 : NOBLES.get(objId).getInteger("competitions_won");
   }

   public int getCompetitionLost(int objId) {
      return !NOBLES.containsKey(objId) ? 0 : NOBLES.get(objId).getInteger("competitions_lost");
   }

   public int getCompetitionDoneWeek(int objId) {
      return !NOBLES.containsKey(objId) ? 0 : NOBLES.get(objId).getInteger("competitions_done_week");
   }

   public int getCompetitionDoneWeekClassed(int objId) {
      return !NOBLES.containsKey(objId) ? 0 : NOBLES.get(objId).getInteger("competitions_done_week_classed");
   }

   public int getCompetitionDoneWeekNonClassed(int objId) {
      return !NOBLES.containsKey(objId) ? 0 : NOBLES.get(objId).getInteger("competitions_done_week_non_classed");
   }

   public int getCompetitionDoneWeekTeam(int objId) {
      return !NOBLES.containsKey(objId) ? 0 : NOBLES.get(objId).getInteger("competitions_done_week_team");
   }

   public int getRemainingWeeklyMatches(int objId) {
      return Math.max(Config.ALT_OLY_MAX_WEEKLY_MATCHES - this.getCompetitionDoneWeek(objId), 0);
   }

   public int getRemainingWeeklyMatchesClassed(int objId) {
      return Math.max(Config.ALT_OLY_MAX_WEEKLY_MATCHES_CLASSED - this.getCompetitionDoneWeekClassed(objId), 0);
   }

   public int getRemainingWeeklyMatchesNonClassed(int objId) {
      return Math.max(Config.ALT_OLY_MAX_WEEKLY_MATCHES_NON_CLASSED - this.getCompetitionDoneWeekNonClassed(objId), 0);
   }

   public int getRemainingWeeklyMatchesTeam(int objId) {
      return Math.max(Config.ALT_OLY_MAX_WEEKLY_MATCHES_TEAM - this.getCompetitionDoneWeekTeam(objId), 0);
   }

   protected void cleanupNobles() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "UPDATE `olympiad_nobles` SET `olympiad_points` = ?, `competitions_done` = 0, `competitions_won` = 0, `competitions_lost` = 0, `competitions_drawn` = 0, `competitions_done_week` = 0, `competitions_done_week_classed` = 0, `competitions_done_week_non_classed` = 0, `competitions_done_week_team` = 0"
         );
      ) {
         statement.setInt(1, DEFAULT_POINTS);
         statement.execute();
      } catch (SQLException var33) {
         _log.warning("Olympiad: Couldn't clean up nobles from DB!");
      }

      for(StatsSet nobleInfo : NOBLES.values()) {
         nobleInfo.set("olympiad_points", DEFAULT_POINTS);
         nobleInfo.set("competitions_done", 0);
         nobleInfo.set("competitions_won", 0);
         nobleInfo.set("competitions_lost", 0);
         nobleInfo.set("competitions_drawn", 0);
         nobleInfo.set("competitions_done_week", 0);
         nobleInfo.set("competitions_done_week_classed", 0);
         nobleInfo.set("competitions_done_week_non_classed", 0);
         nobleInfo.set("competitions_done_week_team", 0);
         nobleInfo.set("to_save", false);
      }
   }

   public int getPeriod() {
      return this._period;
   }

   protected static StatsSet addNobleStats(int charId, StatsSet data) {
      return NOBLES.put(charId, data);
   }

   public static synchronized void addNoble(Player noble) {
      StatsSet statDat = getNobleStats(noble.getObjectId());
      if (statDat != null) {
         int classId = NOBLES.get(noble.getObjectId()).getInteger("class_id");
         if (classId != noble.getBaseClass()) {
            statDat.set("class_id", noble.getBaseClass());
         }
      } else {
         statDat = new StatsSet();
         statDat.set("class_id", noble.getBaseClass());
         statDat.set("char_name", noble.getName());
         statDat.set("olympiad_points", DEFAULT_POINTS);
         statDat.set("olympiad_points_past", 0);
         statDat.set("competitions_done", 0);
         statDat.set("competitions_won", 0);
         statDat.set("competitions_lost", 0);
         statDat.set("competitions_drawn", 0);
         statDat.set("competitions_done_week", 0);
         statDat.set("competitions_done_week_classed", 0);
         statDat.set("competitions_done_week_non_classed", 0);
         statDat.set("competitions_done_week_team", 0);
         statDat.set("to_save", true);
         addNobleStats(noble.getObjectId(), statDat);
         noble.getCounters().addAchivementInfo("setNobless", 0, -1L, false, false, false);
      }
   }

   public static synchronized void removeNoble(Player noble) {
      NOBLES.remove(noble.getObjectId());
   }

   protected class OlympiadEndTask implements Runnable {
      @Override
      public void run() {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_ENDED);
         sm.addNumber(Olympiad.this._currentCycle);
         Announcements.getInstance().announceToAll(sm);
         if (Olympiad.this._scheduledWeeklyTask != null) {
            Olympiad.this._scheduledWeeklyTask.cancel(true);
         }

         Olympiad.this.saveNobleData();
         Olympiad.this._period = 1;
         Hero.getInstance().clearHeroes();
         Hero.getInstance().resetData();
         Calendar validationEnd = Calendar.getInstance();
         Olympiad.this._validationEnd = validationEnd.getTimeInMillis() + Config.ALT_OLY_VPERIOD * 3600000L;
         Olympiad.this.saveOlympiadStatus();
         Olympiad.this.updateMonthlyData();
         Olympiad.this._scheduledValdationTask = ThreadPoolManager.getInstance()
            .schedule(Olympiad.this.new ValidationEndTask(), Olympiad.this.getMillisToValidationEnd());
      }
   }

   private static class SingletonHolder {
      protected static final Olympiad _instance = new Olympiad();
   }

   protected class ValidationEndTask implements Runnable {
      @Override
      public void run() {
         Olympiad.this.sortHerosToBe();
         Hero.getInstance().computeNewHeroes(Olympiad.HEROS_TO_BE);
         Olympiad.this._period = 0;
         ++Olympiad.this._currentCycle;
         Olympiad.this.cleanupNobles();
         Olympiad.this.setNewOlympiadEnd();
         Olympiad.this.init();
      }
   }
}
