package l2e.gameserver.instancemanager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.daily.DailyRewardTemplate;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class DailyRewardManager {
   protected static final Logger _log = Logger.getLogger(DailyRewardManager.class.getName());
   private static final DailyRewardManager _instance = new DailyRewardManager();
   private boolean _hwidCheck;
   private final String _columnCheck;
   private int _lastDay;
   private final List<DailyRewardTemplate> _rewards = new ArrayList<>();
   private final Map<String, Integer> _rewardedList = new HashMap<>();
   private final Map<String, Integer> _rewardedDays = new HashMap<>();
   private ScheduledFuture<?> _dailyTask = null;

   public static final DailyRewardManager getInstance() {
      return _instance;
   }

   public DailyRewardManager() {
      if (Config.ALLOW_DAILY_REWARD) {
         this._rewards.clear();
         this._rewardedList.clear();
         this._rewardedDays.clear();
         this.loadRewards();
         this.loadRewardedList();
         this.checkTimeTask();
      }

      this._columnCheck = this.isHwidCheck() ? "hwid" : "ip";
   }

   private void loadRewards() {
      try {
         File file = new File(Config.DATAPACK_ROOT + "/data/stats/services/dailyRewards.xml");
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         Document doc1 = factory.newDocumentBuilder().parse(file);
         int counter = 0;

         for(Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
            if ("list".equalsIgnoreCase(n1.getNodeName())) {
               this._hwidCheck = Boolean.parseBoolean(n1.getAttributes().getNamedItem("hwidCheck").getNodeValue());
               this._lastDay = n1.getAttributes().getNamedItem("lastDay") != null
                  ? Integer.parseInt(n1.getAttributes().getNamedItem("lastDay").getNodeValue())
                  : 1;

               for(Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling()) {
                  if ("day".equalsIgnoreCase(d1.getNodeName())) {
                     ++counter;
                     DailyRewardTemplate template = null;
                     Map<Integer, Integer> rewards = new HashMap<>();
                     int number = Integer.parseInt(d1.getAttributes().getNamedItem("number").getNodeValue());
                     String image = d1.getAttributes().getNamedItem("image") != null ? d1.getAttributes().getNamedItem("image").getNodeValue() : "";

                     for(Node s1 = d1.getFirstChild(); s1 != null; s1 = s1.getNextSibling()) {
                        if ("reward".equalsIgnoreCase(s1.getNodeName())) {
                           int itemId = Integer.parseInt(s1.getAttributes().getNamedItem("itemId").getNodeValue());
                           int count = Integer.parseInt(s1.getAttributes().getNamedItem("count").getNodeValue());
                           rewards.put(itemId, count);
                           boolean isDisplayId = s1.getAttributes().getNamedItem("displayId") != null
                              && Boolean.parseBoolean(s1.getAttributes().getNamedItem("displayId").getNodeValue());
                           if (isDisplayId && image == "") {
                              image = ItemsParser.getInstance().getTemplate(itemId).getIcon();
                           }
                        }
                     }

                     template = new DailyRewardTemplate(number, rewards);
                     if (image != "") {
                        template.setDisplayImage(image);
                     }

                     this._rewards.add(template);
                  }
               }
            }
         }

         _log.info("DailyRewardManager: Loaded " + counter + " daily rewards.");
      } catch (DOMException | ParserConfigurationException | SAXException | NumberFormatException var15) {
         _log.log(Level.WARNING, "DailyRewardManager: dailyRewards.xml could not be initialized.", (Throwable)var15);
      } catch (IllegalArgumentException | IOException var16) {
         _log.log(Level.WARNING, "DailyRewardManager: IOException or IllegalArgumentException.", (Throwable)var16);
      }
   }

   public DailyRewardTemplate getDailyReward(int day) {
      for(DailyRewardTemplate dayReward : this._rewards) {
         if (dayReward.getDay() == day) {
            return dayReward;
         }
      }

      return null;
   }

   public List<DailyRewardTemplate> getDailyRewards() {
      return this._rewards;
   }

   public int size() {
      return this._rewards.size();
   }

   private void checkTimeTask() {
      long lastUpdate = ServerVariables.getLong("Daily_Rewards", 0L);
      if (System.currentTimeMillis() > lastUpdate) {
         this.cleanDailyRewards();
      } else {
         this._dailyTask = ThreadPoolManager.getInstance().schedule(new DailyRewardManager.ClearDailyRewards(), lastUpdate - System.currentTimeMillis());
      }
   }

   private void loadRewardedList() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM daily_rewards");
         ResultSet rset = statement.executeQuery();
      ) {
         while(rset.next()) {
            String checkHwid = this.isHwidCheck() ? rset.getString("hwid") : rset.getString("ip");
            int attempt = rset.getInt("attempt");
            int lastDay = rset.getInt("last_day");
            this._rewardedList.put(checkHwid, attempt);
            this._rewardedDays.put(checkHwid, lastDay);
         }

         _log.info("DailyRewardManager: Loaded " + this._rewardedList.size() + " rewarded players.");
      } catch (Exception var61) {
         _log.log(Level.WARNING, "DailyRewardManager: " + var61);
      }
   }

   public boolean isRewardedToday(Player player) {
      String checkHwid = this.isHwidCheck() ? player.getHWID() : player.getIPAddress();
      if (this._rewardedList.containsKey(checkHwid)) {
         return this._rewardedList.get(checkHwid) > 0;
      } else {
         return false;
      }
   }

   public void setRewarded(Player player) {
      this.updateRewardAttempt(player);
      this.updateRewardDay(player);
   }

   private void updateRewardAttempt(Player player) {
      String checkHwid = this.isHwidCheck() ? player.getHWID() : player.getIPAddress();
      if (this._rewardedList.get(checkHwid) != null) {
         this.updateDailyRewardAttempt(player, 1);
      }

      this._rewardedList.put(checkHwid, 1);
   }

   private void updateDailyRewardAttempt(Player player, int count) {
      String checkHwid = this.isHwidCheck() ? player.getHWID() : player.getIPAddress();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE daily_rewards SET attempt=? WHERE " + this.getColumnCheck() + "=?");
      ) {
         statement.setInt(1, count);
         statement.setString(2, checkHwid);
         statement.execute();
      } catch (Exception var36) {
         _log.log(Level.WARNING, "Failed update daily rewards attempt.", (Throwable)var36);
      }
   }

   public int getRewardDay(Player player) {
      String checkHwid = this.isHwidCheck() ? player.getHWID() : player.getIPAddress();
      return this._rewardedDays.containsKey(checkHwid) ? this._rewardedDays.get(checkHwid) : 0;
   }

   private void updateRewardDay(Player player) {
      String checkHwid = this.isHwidCheck() ? player.getHWID() : player.getIPAddress();
      if (this._rewardedDays.containsKey(checkHwid)) {
         int nextDay = this._rewardedDays.get(checkHwid) + 1;
         this.updateDailyRewardDay(player, nextDay);
         this._rewardedDays.put(checkHwid, nextDay);
      }
   }

   public void updateDailyRewardDay(Player player, int day) {
      String checkHwid = this.isHwidCheck() ? player.getHWID() : player.getIPAddress();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE daily_rewards SET last_day=? WHERE " + this.getColumnCheck() + "=?");
      ) {
         statement.setInt(1, day);
         statement.setString(2, checkHwid);
         statement.execute();
      } catch (Exception var36) {
         _log.log(Level.WARNING, "Failed update daily rewards day.", (Throwable)var36);
      }
   }

   public void addNewDailyPlayer(Player player) {
      String checkHwid = this.isHwidCheck() ? player.getHWID() : player.getIPAddress();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("INSERT INTO daily_rewards (" + this.getColumnCheck() + ", attempt, last_day) VALUES (?,?,?)");
      ) {
         statement.setString(1, checkHwid);
         statement.setInt(2, 0);
         statement.setInt(3, this._lastDay);
         statement.executeUpdate();
         this._rewardedList.put(checkHwid, 0);
         this._rewardedDays.put(checkHwid, this._lastDay);
      } catch (Exception var35) {
         _log.log(Level.WARNING, "Could not insert daily rewards: " + var35.getMessage(), (Throwable)var35);
      }
   }

   public void cleanDailyRewards() {
      for(String hwid : this._rewardedList.keySet()) {
         this._rewardedList.put(hwid, 0);
      }

      Calendar calendar = Calendar.getInstance();
      if (calendar.get(5) == 1) {
         this.refreshDays();
      }

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE daily_rewards SET attempt=?");
      ) {
         statement.setInt(1, 0);
         statement.execute();
      } catch (Exception var34) {
         _log.log(Level.WARNING, "Failed update daily rewards attempts.", (Throwable)var34);
      }

      Calendar newTime = Calendar.getInstance();
      newTime.setLenient(true);
      newTime.set(11, 6);
      newTime.set(12, 30);
      newTime.add(5, 1);
      ServerVariables.set("Daily_Rewards", newTime.getTimeInMillis());
      if (this._dailyTask != null) {
         this._dailyTask.cancel(false);
         this._dailyTask = null;
      }

      this._dailyTask = ThreadPoolManager.getInstance()
         .schedule(new DailyRewardManager.ClearDailyRewards(), newTime.getTimeInMillis() - System.currentTimeMillis());
      _log.info("DailyRewardManager: refresh rewards completed.");
      _log.info("DailyRewardManager: Next month tasks refresh at: " + Util.formatTime((int)(newTime.getTimeInMillis() - System.currentTimeMillis()) / 1000));
   }

   private void refreshDays() {
      for(String hwid : this._rewardedDays.keySet()) {
         this._rewardedDays.put(hwid, 1);
      }

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE daily_rewards SET last_day=?");
      ) {
         statement.setInt(1, 1);
         statement.execute();
      } catch (Exception var33) {
         _log.log(Level.WARNING, "Failed update daily rewards last_day.", (Throwable)var33);
      }
   }

   public boolean isHwidCheck() {
      return this._hwidCheck;
   }

   public String getColumnCheck() {
      return this._columnCheck;
   }

   private class ClearDailyRewards extends RunnableImpl {
      private ClearDailyRewards() {
      }

      @Override
      public void runImpl() {
         DailyRewardManager.this.cleanDailyRewards();
      }
   }
}
