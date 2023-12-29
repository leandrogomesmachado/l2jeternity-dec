package l2e.gameserver.data.parser;

import gnu.trove.map.hash.TIntLongHashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public final class BotReportParser {
   static final Logger _log = Logger.getLogger(BotReportParser.class.getName());
   private static final int COLUMN_BOT_ID = 1;
   private static final int COLUMN_REPORTER_ID = 2;
   private static final int COLUMN_REPORT_TIME = 3;
   public static final int ATTACK_ACTION_BLOCK_ID = -1;
   public static final int TRADE_ACTION_BLOCK_ID = -2;
   public static final int PARTY_ACTION_BLOCK_ID = -3;
   public static final int ACTION_BLOCK_ID = -4;
   public static final int CHAT_BLOCK_ID = -5;
   private static final String SQL_LOAD_REPORTED_CHAR_DATA = "SELECT * FROM bot_report_data";
   private static final String SQL_INSERT_REPORTED_CHAR_DATA = "INSERT INTO bot_report_data VALUES (?,?,?)";
   private static final String SQL_CLEAR_REPORTED_CHAR_DATA = "DELETE FROM bot_report_data";
   private TIntLongHashMap _ipRegistry;
   private Map<Integer, BotReportParser.ReporterCharData> _charRegistry;
   private Map<Integer, BotReportParser.ReportedCharData> _reports;
   private Map<Integer, BotReportParser.PunishHolder> _punishments;

   BotReportParser() {
      if (Config.BOTREPORT_ENABLE) {
         this._ipRegistry = new TIntLongHashMap();
         this._charRegistry = new ConcurrentHashMap<>();
         this._reports = new ConcurrentHashMap<>();
         this._punishments = new ConcurrentHashMap<>();

         try {
            File punishments = new File(Config.DATAPACK_ROOT, "data/stats/admin/botreport_punishments.xml");
            if (!punishments.exists()) {
               throw new FileNotFoundException(punishments.getName());
            }

            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(punishments, new BotReportParser.PunishmentsLoader());
         } catch (Exception var3) {
            _log.log(Level.WARNING, "BotReportParser: Could not load punishments from /data/stats/admin/botreport_punishments.xml", (Throwable)var3);
         }

         this.loadReportedCharData();
         this.scheduleResetPointTask();
      }
   }

   private void loadReportedCharData() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement st = con.prepareStatement("SELECT * FROM bot_report_data");
         ResultSet rset = st.executeQuery();
         long lastResetTime = 0L;

         try {
            String[] hour = Config.BOTREPORT_RESETPOINT_HOUR;
            Calendar c = Calendar.getInstance();
            c.set(11, Integer.parseInt(hour[0]));
            c.set(12, Integer.parseInt(hour[1]));
            if (System.currentTimeMillis() < c.getTimeInMillis()) {
               c.set(6, c.get(6) - 1);
            }

            lastResetTime = c.getTimeInMillis();
         } catch (Exception var21) {
         }

         while(rset.next()) {
            int botId = rset.getInt(1);
            int reporter = rset.getInt(2);
            long date = rset.getLong(3);
            if (this._reports.containsKey(botId)) {
               this._reports.get(botId).addReporter(reporter, date);
            } else {
               BotReportParser.ReportedCharData rcd = new BotReportParser.ReportedCharData();
               rcd.addReporter(reporter, date);
               this._reports.put(rset.getInt(1), rcd);
            }

            if (date > lastResetTime) {
               BotReportParser.ReporterCharData rcd = null;
               if ((rcd = this._charRegistry.get(reporter)) != null) {
                  rcd.setPoints(rcd.getPointsLeft() - 1);
               } else {
                  rcd = new BotReportParser.ReporterCharData();
                  rcd.setPoints(6);
                  this._charRegistry.put(reporter, rcd);
               }
            }
         }

         rset.close();
         st.close();
         _log.info("BotReportParser: Loaded " + this._reports.size() + " bot reports.");
      } catch (Exception var24) {
         _log.log(Level.WARNING, "BotReportParser: Could not load reported char data!", (Throwable)var24);
      }
   }

   public void saveReportedCharData() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement st = con.prepareStatement("DELETE FROM bot_report_data");
         st.execute();
         st = con.prepareStatement("INSERT INTO bot_report_data VALUES (?,?,?)");

         for(Entry<Integer, BotReportParser.ReportedCharData> entrySet : this._reports.entrySet()) {
            TIntLongHashMap reportTable = entrySet.getValue()._reporters;

            for(int reporterId : reportTable.keys()) {
               st.setInt(1, entrySet.getKey());
               st.setInt(2, reporterId);
               st.setLong(3, reportTable.get(reporterId));
               st.execute();
               st.clearParameters();
            }
         }

         st.close();
      } catch (Exception var21) {
         _log.log(Level.SEVERE, "BotReportParser: Could not update reported char data in database!", (Throwable)var21);
      }
   }

   public boolean reportBot(Player reporter) {
      GameObject target = reporter.getTarget();
      Player bot = null;
      if (target == null || (bot = target.getActingPlayer()) == null || target.getObjectId() == reporter.getObjectId()) {
         return false;
      } else if (bot.isInsideZone(ZoneId.PEACE) || bot.isInsideZone(ZoneId.PVP)) {
         reporter.sendPacket(SystemMessageId.YOU_CANNOT_REPORT_CHARACTER_IN_PEACE_OR_BATTLE_ZONE);
         return false;
      } else if (bot.isInOlympiadMode()) {
         reporter.sendPacket(SystemMessageId.TARGET_NOT_REPORT_CANNOT_REPORT_PEACE_PVP_ZONE_OR_OLYMPIAD_OR_CLAN_WAR_ENEMY);
         return false;
      } else if (bot.getClan() != null && bot.getClan().isAtWarWith(reporter.getClan())) {
         reporter.sendPacket(SystemMessageId.YOU_CANNOT_REPORT_CLAN_WAR_ENEMY);
         return false;
      } else if (bot.getExp() == bot.getStat().getStartingExp()) {
         reporter.sendPacket(SystemMessageId.YOU_CANNOT_REPORT_CHAR_WHO_ACQUIRED_XP);
         return false;
      } else {
         BotReportParser.ReportedCharData rcd = this._reports.get(bot.getObjectId());
         BotReportParser.ReporterCharData rcdRep = this._charRegistry.get(reporter.getObjectId());
         int reporterId = reporter.getObjectId();
         synchronized(this) {
            if (this._reports.containsKey(reporterId)) {
               reporter.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_AND_CANNOT_REPORT);
               return false;
            }

            int ip = hashIp(reporter);
            if (!timeHasPassed(this._ipRegistry, ip)) {
               reporter.sendPacket(SystemMessageId.CANNOT_REPORT_TARGET_ALREDY_REPORTED_BY_CLAN_ALLY_MEMBER_OR_SAME_IP);
               return false;
            }

            if (rcd != null) {
               if (rcd.alredyReportedBy(reporterId)) {
                  reporter.sendPacket(SystemMessageId.YOU_CANNOT_REPORT_CHAR_AT_THIS_TIME_1);
                  return false;
               }

               if (!Config.BOTREPORT_ALLOW_REPORTS_FROM_SAME_CLAN_MEMBERS && rcd.reportedBySameClan(reporter.getClan())) {
                  reporter.sendPacket(SystemMessageId.CANNOT_REPORT_TARGET_ALREDY_REPORTED_BY_CLAN_ALLY_MEMBER_OR_SAME_IP);
                  return false;
               }
            }

            if (rcdRep != null) {
               if (rcdRep.getPointsLeft() == 0) {
                  reporter.sendPacket(SystemMessageId.YOU_HAVE_USED_ALL_POINTS_POINTS_ARE_RESET_AT_NOON);
                  return false;
               }

               long reuse = System.currentTimeMillis() - rcdRep.getLastReporTime();
               if (reuse < Config.BOTREPORT_REPORT_DELAY) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_CAN_REPORT_IN_S1_MINS_YOU_HAVE_S2_POINTS_LEFT);
                  sm.addNumber((int)(reuse / 60000L));
                  sm.addNumber(rcdRep.getPointsLeft());
                  reporter.sendPacket(sm);
                  return false;
               }
            }

            long curTime = System.currentTimeMillis();
            if (rcd == null) {
               rcd = new BotReportParser.ReportedCharData();
               this._reports.put(bot.getObjectId(), rcd);
            }

            rcd.addReporter(reporterId, curTime);
            if (rcdRep == null) {
               rcdRep = new BotReportParser.ReporterCharData();
            }

            rcdRep.registerReport(curTime);
            this._ipRegistry.put(ip, curTime);
            this._charRegistry.put(reporterId, rcdRep);
         }

         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_WAS_REPORTED_AS_BOT);
         sm.addCharName(bot);
         reporter.sendPacket(sm);
         sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_USED_REPORT_POINT_ON_C1_YOU_HAVE_C2_POINTS_LEFT);
         sm.addCharName(bot);
         sm.addNumber(rcdRep.getPointsLeft());
         reporter.sendPacket(sm);
         this.handleReport(bot, rcd);
         return true;
      }
   }

   private void handleReport(Player bot, BotReportParser.ReportedCharData rcd) {
      this.punishBot(bot, this._punishments.get(rcd.getReportCount()));

      for(int key : this._punishments.keySet()) {
         if (key < 0 && Math.abs(key) <= rcd.getReportCount()) {
            this.punishBot(bot, this._punishments.get(key));
         }
      }
   }

   private void punishBot(Player bot, BotReportParser.PunishHolder ph) {
      if (ph != null) {
         ph._punish.getEffects(bot, bot, false);
         if (ph._systemMessageId > -1) {
            SystemMessageId id = SystemMessageId.getSystemMessageId(ph._systemMessageId);
            if (id != null) {
               bot.sendPacket(id);
            }
         }
      }
   }

   void addPunishment(int neededReports, int skillId, int skillLevel, int sysMsg) {
      Skill sk = SkillsParser.getInstance().getInfo(skillId, skillLevel);
      if (sk != null) {
         this._punishments.put(neededReports, new BotReportParser.PunishHolder(sk, sysMsg));
      } else {
         _log.warning(
            "BotReportParser: Could not add punishment for " + neededReports + " report(s): Skill " + skillId + "-" + skillLevel + " does not exist!"
         );
      }
   }

   void resetPointsAndSchedule() {
      synchronized(this._charRegistry) {
         for(BotReportParser.ReporterCharData rcd : this._charRegistry.values()) {
            rcd.setPoints(7);
         }
      }

      this.scheduleResetPointTask();
   }

   private void scheduleResetPointTask() {
      try {
         String[] hour = Config.BOTREPORT_RESETPOINT_HOUR;
         Calendar c = Calendar.getInstance();
         c.set(11, Integer.parseInt(hour[0]));
         c.set(12, Integer.parseInt(hour[1]));
         if (System.currentTimeMillis() > c.getTimeInMillis()) {
            c.set(6, c.get(6) + 1);
         }

         ThreadPoolManager.getInstance().schedule(new BotReportParser.ResetPointTask(), c.getTimeInMillis() - System.currentTimeMillis());
      } catch (Exception var3) {
         ThreadPoolManager.getInstance().schedule(new BotReportParser.ResetPointTask(), 86400000L);
         _log.log(Level.WARNING, "BotReportParser: Could not properly schedule bot report points reset task. Scheduled in 24 hours.", (Throwable)var3);
      }
   }

   public static BotReportParser getInstance() {
      return BotReportParser.SingletonHolder.INSTANCE;
   }

   private static int hashIp(Player player) {
      String con = player.getIPAddress();
      String[] rawByte = con.split("\\.");
      int[] rawIp = new int[4];

      for(int i = 0; i < 4; ++i) {
         rawIp[i] = Integer.parseInt(rawByte[i]);
      }

      return rawIp[0] | rawIp[1] << 8 | rawIp[2] << 16 | rawIp[3] << 24;
   }

   private static boolean timeHasPassed(TIntLongHashMap map, int objectId) {
      long time;
      if ((time = map.get(objectId)) != map.getNoEntryValue()) {
         return System.currentTimeMillis() - time > Config.BOTREPORT_REPORT_DELAY;
      } else {
         return true;
      }
   }

   class PunishHolder {
      final Skill _punish;
      final int _systemMessageId;

      PunishHolder(Skill sk, int sysMsg) {
         this._punish = sk;
         this._systemMessageId = sysMsg;
      }
   }

   private final class PunishmentsLoader extends DefaultHandler {
      PunishmentsLoader() {
      }

      @Override
      public void startElement(String uri, String localName, String qName, Attributes attr) {
         if (qName.equals("punishment")) {
            int reportCount = -1;
            int skillId = -1;
            int skillLevel = 1;
            int sysMessage = -1;

            try {
               reportCount = Integer.parseInt(attr.getValue("neededReportCount"));
               skillId = Integer.parseInt(attr.getValue("skillId"));
               String level = attr.getValue("skillLevel");
               String systemMessageId = attr.getValue("sysMessageId");
               if (level != null) {
                  skillLevel = Integer.parseInt(level);
               }

               if (systemMessageId != null) {
                  sysMessage = Integer.parseInt(systemMessageId);
               }
            } catch (Exception var11) {
               var11.printStackTrace();
            }

            BotReportParser.this.addPunishment(reportCount, skillId, skillLevel, sysMessage);
         }
      }
   }

   private final class ReportedCharData {
      TIntLongHashMap _reporters = new TIntLongHashMap();

      ReportedCharData() {
      }

      int getReportCount() {
         return this._reporters.size();
      }

      boolean alredyReportedBy(int objectId) {
         return this._reporters.contains(objectId);
      }

      void addReporter(int objectId, long reportTime) {
         this._reporters.put(objectId, reportTime);
      }

      boolean reportedBySameClan(Clan clan) {
         if (clan == null) {
            return false;
         } else {
            for(int reporterId : this._reporters.keys()) {
               if (clan.isMember(reporterId)) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   private final class ReporterCharData {
      private long _lastReport;
      private byte _reportPoints = 7;

      ReporterCharData() {
         this._lastReport = 0L;
      }

      void registerReport(long time) {
         --this._reportPoints;
         this._lastReport = time;
      }

      long getLastReporTime() {
         return this._lastReport;
      }

      byte getPointsLeft() {
         return this._reportPoints;
      }

      void setPoints(int points) {
         this._reportPoints = (byte)points;
      }
   }

   class ResetPointTask implements Runnable {
      @Override
      public void run() {
         BotReportParser.this.resetPointsAndSchedule();
      }
   }

   private static final class SingletonHolder {
      static final BotReportParser INSTANCE = new BotReportParser();
   }
}
