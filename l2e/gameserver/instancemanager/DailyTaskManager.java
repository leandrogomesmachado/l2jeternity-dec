package l2e.gameserver.instancemanager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.daily.DailyTaskTemplate;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class DailyTaskManager {
   protected static final Logger _log = Logger.getLogger(DailyTaskManager.class.getName());
   private static final DailyTaskManager _instance = new DailyTaskManager();
   private final List<DailyTaskTemplate> _tasks = new ArrayList<>();
   private boolean _hwidCheck;
   private final String _columnCheck;
   private int _taskPerDay;
   private int _taskPerWeek;
   private int _taskPerMonth;
   private final int[] _taskPrice = new int[2];
   private ScheduledFuture<?> _weeklyTask = null;
   private ScheduledFuture<?> _monthTask = null;

   public static final DailyTaskManager getInstance() {
      return _instance;
   }

   public DailyTaskManager() {
      if (Config.ALLOW_DAILY_TASKS) {
         this._tasks.clear();
         this.getTaskLoad();
         this.checkDailyTimeTask();
         this.checkWeeklyTimeTask();
         this.checkMonthTimeTask();
      }

      this._columnCheck = this.isHwidCheck() ? "hwid" : "ip";
   }

   private void getTaskLoad() {
      try {
         File file = new File(Config.DATAPACK_ROOT + "/data/stats/services/dailyTasks.xml");
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         Document doc1 = factory.newDocumentBuilder().parse(file);
         int counter = 0;

         for(Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
            if ("list".equalsIgnoreCase(n1.getNodeName())) {
               this._hwidCheck = Boolean.parseBoolean(n1.getAttributes().getNamedItem("hwidCheck").getNodeValue());
               this._taskPerDay = Integer.parseInt(n1.getAttributes().getNamedItem("taskPerDay").getNodeValue());
               this._taskPerWeek = Integer.parseInt(n1.getAttributes().getNamedItem("taskPerWeek").getNodeValue());
               this._taskPerMonth = Integer.parseInt(n1.getAttributes().getNamedItem("taskPerMonth").getNodeValue());
               this._taskPrice[0] = n1.getAttributes().getNamedItem("priceId") != null
                  ? Integer.parseInt(n1.getAttributes().getNamedItem("priceId").getNodeValue())
                  : 0;
               this._taskPrice[1] = n1.getAttributes().getNamedItem("amount") != null
                  ? Integer.parseInt(n1.getAttributes().getNamedItem("amount").getNodeValue())
                  : 0;

               for(Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling()) {
                  if ("task".equalsIgnoreCase(d1.getNodeName())) {
                     ++counter;
                     DailyTaskTemplate template = null;
                     int id = Integer.parseInt(d1.getAttributes().getNamedItem("id").getNodeValue());
                     String type = d1.getAttributes().getNamedItem("type").getNodeValue();
                     String sort = d1.getAttributes().getNamedItem("sort").getNodeValue();
                     String name = d1.getAttributes().getNamedItem("name").getNodeValue();
                     String image = d1.getAttributes().getNamedItem("image") != null ? d1.getAttributes().getNamedItem("image").getNodeValue() : "";
                     String descr = "";
                     Map<Integer, Long> rewards = new HashMap<>();

                     for(Node s1 = d1.getFirstChild(); s1 != null; s1 = s1.getNextSibling()) {
                        if ("description".equalsIgnoreCase(s1.getNodeName())) {
                           descr = s1.getAttributes().getNamedItem("val").getNodeValue();
                           if (descr.isEmpty()) {
                              _log.log(Level.WARNING, "DailyTaskManager: Error! Must be a description for task id: " + id);
                           } else {
                              template = new DailyTaskTemplate(id, type, sort, name, descr, image);
                           }
                        } else if ("reward".equalsIgnoreCase(s1.getNodeName())) {
                           for(Node r1 = s1.getFirstChild(); r1 != null; r1 = r1.getNextSibling()) {
                              if ("item".equalsIgnoreCase(r1.getNodeName())) {
                                 int itemId = Integer.parseInt(r1.getAttributes().getNamedItem("id").getNodeValue());
                                 long amount = Long.parseLong(r1.getAttributes().getNamedItem("count").getNodeValue());
                                 rewards.put(itemId, amount);
                              }
                           }

                           template.setRewards(rewards);
                        } else if ("npc".equalsIgnoreCase(s1.getNodeName())) {
                           template.setNpcId(Integer.parseInt(s1.getAttributes().getNamedItem("id").getNodeValue()));
                           template.setNpcCount(Integer.parseInt(s1.getAttributes().getNamedItem("count").getNodeValue()));
                        } else if ("quest".equalsIgnoreCase(s1.getNodeName())) {
                           template.setQuestId(Integer.parseInt(s1.getAttributes().getNamedItem("id").getNodeValue()));
                        } else if ("reflection".equalsIgnoreCase(s1.getNodeName())) {
                           template.setReflectionId(Integer.parseInt(s1.getAttributes().getNamedItem("id").getNodeValue()));
                        } else if ("pvp".equalsIgnoreCase(s1.getNodeName())) {
                           template.setPvpCount(Integer.parseInt(s1.getAttributes().getNamedItem("count").getNodeValue()));
                        } else if ("pk".equalsIgnoreCase(s1.getNodeName())) {
                           template.setPkCount(Integer.parseInt(s1.getAttributes().getNamedItem("count").getNodeValue()));
                        } else if ("olympiad".equalsIgnoreCase(s1.getNodeName())) {
                           template.setOlyMatchCount(Integer.parseInt(s1.getAttributes().getNamedItem("match").getNodeValue()));
                        } else if ("event".equalsIgnoreCase(s1.getNodeName())) {
                           template.setEventsCount(Integer.parseInt(s1.getAttributes().getNamedItem("count").getNodeValue()));
                        } else if ("siege".equalsIgnoreCase(s1.getNodeName())) {
                           boolean castle = s1.getAttributes().getNamedItem("castle") != null
                              && Boolean.parseBoolean(s1.getAttributes().getNamedItem("castle").getNodeValue());
                           boolean fortress = s1.getAttributes().getNamedItem("fortress") != null
                              && Boolean.parseBoolean(s1.getAttributes().getNamedItem("fortress").getNodeValue());
                           if (castle) {
                              template.setSiegeCastle(Boolean.parseBoolean(s1.getAttributes().getNamedItem("castle").getNodeValue()));
                           } else if (fortress) {
                              template.setSiegeFort(Boolean.parseBoolean(s1.getAttributes().getNamedItem("fortress").getNodeValue()));
                           }
                        }
                     }

                     if (this.checkTask(template)) {
                        this._tasks.add(template);
                     }
                  }
               }
            }
         }

         _log.info("DailyTaskManager: Loaded " + counter + " daily tasks.");
      } catch (DOMException | ParserConfigurationException | SAXException | NumberFormatException var20) {
         _log.log(Level.WARNING, "DailyTaskManager: dailyTasks.xml could not be initialized.", (Throwable)var20);
      } catch (IllegalArgumentException | IOException var21) {
         _log.log(Level.WARNING, "DailyTaskManager: IOException or IllegalArgumentException.", (Throwable)var21);
      }
   }

   private boolean checkTask(DailyTaskTemplate template) {
      if (template != null && !template.getType().isEmpty()) {
         String var2 = template.getType();
         switch(var2) {
            case "Farm":
               if (template.getNpcId() <= 0 || template.getNpcCount() <= 0) {
                  _log.log(Level.WARNING, "DailyTaskManager: Error! npcId or npcCount template not correct for daily task id:" + template.getId());
                  return false;
               }
               break;
            case "Quest":
               if (template.getQuestId() <= 0) {
                  _log.log(Level.WARNING, "DailyTaskManager: Error! questId template not correct for daily task id:" + template.getId());
                  return false;
               }
               break;
            case "Reflection":
               if (template.getReflectionId() <= 0) {
                  _log.log(Level.WARNING, "DailyTaskManager: Error! reflection id template not correct for daily task id:" + template.getId());
                  return false;
               }
               break;
            case "Pvp":
               if (template.getPvpCount() <= 0) {
                  _log.log(Level.WARNING, "DailyTaskManager: Error! pvp count template not correct for daily task id:" + template.getId());
                  return false;
               }
               break;
            case "Pk":
               if (template.getPkCount() <= 0) {
                  _log.log(Level.WARNING, "DailyTaskManager: Error! pk count template not correct for daily task id:" + template.getId());
                  return false;
               }
               break;
            case "Olympiad":
               if (template.getOlyMatchCount() <= 0) {
                  _log.log(Level.WARNING, "DailyTaskManager: Error! olimpiad match template not correct for daily task id:" + template.getId());
                  return false;
               }
               break;
            case "Event":
               if (template.getEventsCount() <= 0) {
                  _log.log(Level.WARNING, "DailyTaskManager: Error! events template not correct for daily task id:" + template.getId());
                  return false;
               }
               break;
            case "Siege":
               if (!template.getSiegeFort() && !template.getSiegeCastle() || template.getSiegeFort() && template.getSiegeCastle()) {
                  _log.log(Level.WARNING, "DailyTaskManager: Error! siegge template not correct for daily task id:" + template.getId());
                  return false;
               }
         }

         return true;
      } else {
         _log.log(Level.WARNING, "DailyTaskManager: Error! Daily Task Template null!");
         return false;
      }
   }

   public DailyTaskTemplate getDailyTask(int id) {
      for(DailyTaskTemplate task : this._tasks) {
         if (task.getId() == id) {
            return task;
         }
      }

      return null;
   }

   public List<DailyTaskTemplate> getDailyTasks() {
      return this._tasks;
   }

   public int size() {
      return this._tasks.size();
   }

   public boolean checkHWID(Player attacker, Player defender) {
      if (this.isHwidCheck()) {
         if (defender.getHWID() != null && !defender.getHWID().equalsIgnoreCase(attacker.getHWID())) {
            return true;
         }
      } else if (defender.getIPAddress() != null && !defender.getIPAddress().equalsIgnoreCase(attacker.getIPAddress())) {
         return true;
      }

      return false;
   }

   public void checkDailyTimeTask() {
      Calendar currentTime = Calendar.getInstance();
      long lastUpdate = ServerVariables.getLong("Daily_Tasks", 0L);
      if (currentTime.getTimeInMillis() > lastUpdate) {
         Calendar newTime = Calendar.getInstance();
         newTime.setLenient(true);
         newTime.set(11, 6);
         newTime.set(12, 30);
         if (newTime.getTimeInMillis() < currentTime.getTimeInMillis()) {
            newTime.add(5, 1);
         }

         ServerVariables.set("Daily_Tasks", newTime.getTimeInMillis());

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("DELETE FROM daily_tasks WHERE type = ?");
         ) {
            statement.setString(1, "daily");
            statement.execute();
         } catch (Exception var97) {
            _log.log(Level.SEVERE, "Failed to clean up daily tasks.", (Throwable)var97);
         }

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE daily_tasks_count SET dailyCount = ?");
         ) {
            statement.setInt(1, this.getTaskPerDay());
            statement.execute();
         } catch (Exception var93) {
            _log.log(Level.SEVERE, "Failed update daily tasks count.", (Throwable)var93);
         }

         for(Player player : World.getInstance().getAllPlayers()) {
            if (player != null) {
               player.cleanDailyTasks();
            }
         }

         _log.info("DailyTaskManager: Daily tasks reshresh completed.");
         _log.info(
            "DailyTaskManager: Next daily tasks refresh throught: " + Util.formatTime((int)(newTime.getTimeInMillis() - System.currentTimeMillis()) / 1000)
         );
      }
   }

   public void checkWeeklyTimeTask() {
      long lastUpdate = ServerVariables.getLong("Weekly_Tasks", 0L);
      if (System.currentTimeMillis() > lastUpdate) {
         this.clearWeeklyTasks();
      } else {
         this._weeklyTask = ThreadPoolManager.getInstance().schedule(new DailyTaskManager.ClearWeeklyTask(), lastUpdate - System.currentTimeMillis());
      }
   }

   public void checkMonthTimeTask() {
      long lastUpdate = ServerVariables.getLong("Month_Tasks", 0L);
      if (System.currentTimeMillis() > lastUpdate) {
         this.clearMonthTasks();
      } else {
         this._monthTask = ThreadPoolManager.getInstance().schedule(new DailyTaskManager.ClearMonthTask(), lastUpdate - System.currentTimeMillis());
      }
   }

   private void clearMonthTasks() {
      Calendar curTime = Calendar.getInstance();
      int month = curTime.get(2) + 1;
      Calendar newTime = Calendar.getInstance();
      newTime.setLenient(true);
      newTime.set(2, month);
      newTime.set(5, 1);
      newTime.set(11, 6);
      newTime.set(12, 32);
      ServerVariables.set("Month_Tasks", newTime.getTimeInMillis());

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM daily_tasks WHERE type = ?");
      ) {
         statement.setString(1, "month");
         statement.execute();
      } catch (Exception var96) {
         _log.log(Level.SEVERE, "Failed to clean up month tasks.", (Throwable)var96);
      }

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE daily_tasks_count SET monthCount = ?");
      ) {
         statement.setInt(1, this.getTaskPerMonth());
         statement.execute();
      } catch (Exception var92) {
         _log.log(Level.SEVERE, "Failed update weekly tasks count.", (Throwable)var92);
      }

      for(Player player : World.getInstance().getAllPlayers()) {
         if (player != null) {
            player.cleanMonthTasks();
         }
      }

      if (this._monthTask != null) {
         this._monthTask.cancel(false);
         this._monthTask = null;
      }

      this._monthTask = ThreadPoolManager.getInstance()
         .schedule(new DailyTaskManager.ClearWeeklyTask(), newTime.getTimeInMillis() - System.currentTimeMillis());
      _log.info("DailyTaskManager: Month tasks reshresh completed.");
      _log.info(
         "DailyTaskManager: Next month tasks refresh throught: " + Util.formatTime((int)(newTime.getTimeInMillis() - System.currentTimeMillis()) / 1000)
      );
   }

   private void clearWeeklyTasks() {
      Calendar newTime = Calendar.getInstance();
      newTime.setLenient(true);
      newTime.add(10, 168);
      newTime.set(7, 2);
      newTime.set(11, 6);
      newTime.set(12, 31);
      ServerVariables.set("Weekly_Tasks", newTime.getTimeInMillis());

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM daily_tasks WHERE type = ?");
      ) {
         statement.setString(1, "weekly");
         statement.execute();
      } catch (Exception var94) {
         _log.log(Level.SEVERE, "Failed to clean up weekly tasks.", (Throwable)var94);
      }

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE daily_tasks_count SET weeklyCount = ?");
      ) {
         statement.setInt(1, this.getTaskPerWeek());
         statement.execute();
      } catch (Exception var90) {
         _log.log(Level.SEVERE, "Failed update weekly tasks count.", (Throwable)var90);
      }

      for(Player player : World.getInstance().getAllPlayers()) {
         if (player != null) {
            player.cleanWeeklyTasks();
         }
      }

      if (this._weeklyTask != null) {
         this._weeklyTask.cancel(false);
         this._weeklyTask = null;
      }

      this._weeklyTask = ThreadPoolManager.getInstance()
         .schedule(new DailyTaskManager.ClearWeeklyTask(), newTime.getTimeInMillis() - System.currentTimeMillis());
      _log.info("DailyTaskManager: Weekly tasks reshresh completed.");
      _log.info(
         "DailyTaskManager: Next weekly tasks refresh throught: " + Util.formatTime((int)(newTime.getTimeInMillis() - System.currentTimeMillis()) / 1000)
      );
   }

   public boolean isHwidCheck() {
      return this._hwidCheck;
   }

   public String getColumnCheck() {
      return this._columnCheck;
   }

   public int getTaskPerDay() {
      return this._taskPerDay;
   }

   public int getTaskPerWeek() {
      return this._taskPerWeek;
   }

   public int getTaskPerMonth() {
      return this._taskPerMonth;
   }

   public int[] getTaskPrice() {
      return this._taskPrice;
   }

   private class ClearMonthTask extends RunnableImpl {
      private ClearMonthTask() {
      }

      @Override
      public void runImpl() {
         DailyTaskManager.this.clearMonthTasks();
      }
   }

   private class ClearWeeklyTask extends RunnableImpl {
      private ClearWeeklyTask() {
      }

      @Override
      public void runImpl() {
         DailyTaskManager.this.clearWeeklyTasks();
      }
   }
}
