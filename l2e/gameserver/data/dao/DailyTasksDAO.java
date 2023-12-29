package l2e.gameserver.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.DailyTaskManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.daily.DailyTaskTemplate;
import l2e.gameserver.model.actor.templates.player.PlayerTaskTemplate;

public class DailyTasksDAO {
   private static final Logger _log = Logger.getLogger(DailyTasksDAO.class.getName());
   private static final String INSERT_DAILY = "INSERT INTO daily_tasks (obj_Id, taskId, type, params, status, rewarded) VALUES (?,?,?,?,?,?)";
   private static final String UPDATE_DAILY = "UPDATE daily_tasks SET status=? WHERE obj_Id=? and taskId=?";
   private static final String UPDATE_DAILY_REWARD = "UPDATE daily_tasks SET rewarded=? WHERE obj_Id=? and taskId=?";
   private static final String UPDATE_DAILY_PARAM = "UPDATE daily_tasks SET params=? WHERE obj_Id=? and taskId=?";
   private static final String RESTORE_DAILY = "SELECT * FROM daily_tasks WHERE obj_Id=?";
   private static final String REMOVE_DAILY = "DELETE FROM daily_tasks WHERE obj_Id=? and taskId=? LIMIT 1";
   private static final String INSERT_TASK_COUNT = "INSERT INTO daily_tasks_count ("
      + DailyTaskManager.getInstance().getColumnCheck()
      + ", dailyCount, weeklyCount, monthCount) VALUES (?,?,?,?)";
   private static final String UPDATE_TASK_COUNT = "UPDATE daily_tasks_count SET dailyCount=?,weeklyCount=?,monthCount=? WHERE "
      + DailyTaskManager.getInstance().getColumnCheck()
      + "=?";
   private static final String RESTORE_TASK_COUNT = "SELECT * FROM daily_tasks_count WHERE " + DailyTaskManager.getInstance().getColumnCheck() + "=?";
   private static DailyTasksDAO _instance = new DailyTasksDAO();

   public void addNewDailyTask(Player player, PlayerTaskTemplate template) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("INSERT INTO daily_tasks (obj_Id, taskId, type, params, status, rewarded) VALUES (?,?,?,?,?,?)");
      ) {
         statement.setInt(1, player.getObjectId());
         statement.setInt(2, template.getId());
         statement.setString(3, template.getSort());
         statement.setInt(4, 0);
         statement.setInt(5, 0);
         statement.setInt(6, 0);
         statement.executeUpdate();
         if (template.getSort().equalsIgnoreCase("daily")) {
            player.addActiveDailyTasks(template.getId(), template);
            int nextCount = player.getLastDailyTasks() - 1;
            player.setLastDailyTasks(nextCount);
         } else if (template.getSort().equalsIgnoreCase("weekly")) {
            player.addActiveDailyTasks(template.getId(), template);
            int nextCount = player.getLastWeeklyTasks() - 1;
            player.setLastWeeklyTasks(nextCount);
         } else if (template.getSort().equalsIgnoreCase("month")) {
            player.addActiveDailyTasks(template.getId(), template);
            int nextCount = player.getLastMonthTasks() - 1;
            player.setLastMonthTasks(nextCount);
         }
      } catch (Exception var35) {
         _log.log(Level.SEVERE, "Could not insert tasks count: " + var35.getMessage(), (Throwable)var35);
      }
   }

   public void restoreDailyTasks(Player player) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM daily_tasks WHERE obj_Id=?");
      ) {
         statement.setInt(1, player.getObjectId());

         PlayerTaskTemplate playerTask;
         try (ResultSet rset = statement.executeQuery()) {
            for(; rset.next(); player.addActiveDailyTasks(playerTask.getId(), playerTask)) {
               DailyTaskTemplate task = DailyTaskManager.getInstance().getDailyTask(rset.getInt("taskId"));
               playerTask = new PlayerTaskTemplate(task.getId(), task.getType(), task.getSort());
               String var10 = task.getType();
               switch(var10) {
                  case "Farm":
                     playerTask.setCurrentNpcCount(rset.getInt("params"));
                     break;
                  case "Pvp":
                     playerTask.setCurrentPvpCount(rset.getInt("params"));
                     break;
                  case "Pk":
                     playerTask.setCurrentPkCount(rset.getInt("params"));
                     break;
                  case "Olympiad":
                     playerTask.setCurrentOlyMatchCount(rset.getInt("params"));
               }

               if (rset.getInt("status") == 1) {
                  playerTask.setIsComplete(true);
               }

               if (rset.getInt("rewarded") == 1) {
                  playerTask.setIsRewarded(true);
               }
            }
         }
      } catch (Exception var63) {
         _log.log(Level.SEVERE, "Failed restore daily tasks.", (Throwable)var63);
      }
   }

   public void updateTaskStatus(Player player, PlayerTaskTemplate template) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE daily_tasks SET status=? WHERE obj_Id=? and taskId=?");
      ) {
         statement.setInt(1, template.isComplete() ? 1 : 0);
         statement.setInt(2, player.getObjectId());
         statement.setInt(3, template.getId());
         statement.execute();
      } catch (Exception var35) {
         _log.log(Level.SEVERE, "Failed update daily task.", (Throwable)var35);
      }
   }

   public void updateTaskRewardStatus(Player player, PlayerTaskTemplate template) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE daily_tasks SET rewarded=? WHERE obj_Id=? and taskId=?");
      ) {
         statement.setInt(1, template.isRewarded() ? 1 : 0);
         statement.setInt(2, player.getObjectId());
         statement.setInt(3, template.getId());
         statement.execute();
      } catch (Exception var35) {
         _log.log(Level.SEVERE, "Failed update daily task.", (Throwable)var35);
      }
   }

   public void updateTaskParams(Player player, int taskId, int params) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE daily_tasks SET params=? WHERE obj_Id=? and taskId=?");
      ) {
         statement.setInt(1, params);
         statement.setInt(2, player.getObjectId());
         statement.setInt(3, taskId);
         statement.execute();
      } catch (Exception var36) {
         _log.log(Level.SEVERE, "Failed update daily task.", (Throwable)var36);
      }
   }

   public void removeTask(Player player, int taskId) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM daily_tasks WHERE obj_Id=? and taskId=? LIMIT 1");
      ) {
         statement.setInt(1, player.getObjectId());
         statement.setInt(2, taskId);
         statement.execute();
         player.removeActiveDailyTasks(taskId);
      } catch (Exception var35) {
         _log.log(Level.SEVERE, "Failed remove daily task.", (Throwable)var35);
      }
   }

   private void addDailyTasksCount(Player player, String checkHwid) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(INSERT_TASK_COUNT);
      ) {
         statement.setString(1, checkHwid);
         statement.setInt(2, DailyTaskManager.getInstance().getTaskPerDay());
         statement.setInt(3, DailyTaskManager.getInstance().getTaskPerWeek());
         statement.setInt(4, DailyTaskManager.getInstance().getTaskPerMonth());
         statement.executeUpdate();
         this.restoreTasksCount(player);
      } catch (Exception var35) {
         _log.log(Level.SEVERE, "Could not insert daily tasks count: " + var35.getMessage(), (Throwable)var35);
      }
   }

   public void updateDailyTasksCount(Player player, int dailyCount, int weeklyCount, int monthCount) {
      String checkHwid = DailyTaskManager.getInstance().isHwidCheck() ? player.getHWID() : player.getIPAddress();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(UPDATE_TASK_COUNT);
      ) {
         statement.setInt(1, dailyCount);
         statement.setInt(2, weeklyCount);
         statement.setInt(3, monthCount);
         statement.setString(4, checkHwid);
         statement.executeUpdate();
      } catch (Exception var38) {
         _log.log(Level.SEVERE, "Failed update tasks count.", (Throwable)var38);
      }
   }

   public void restoreTasksCount(Player player) {
      String checkHwid = DailyTaskManager.getInstance().isHwidCheck() ? player.getHWID() : player.getIPAddress();
      boolean found = false;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(RESTORE_TASK_COUNT);
      ) {
         statement.setString(1, checkHwid);

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               player.setLastDailyTasks(rset.getInt("dailyCount"));
               player.setLastWeeklyTasks(rset.getInt("weeklyCount"));
               player.setLastMonthTasks(rset.getInt("monthCount"));
               found = true;
               if (player.getLastDailyTasks() > 0 || player.getLastWeeklyTasks() > 0 || player.getLastMonthTasks() > 0) {
                  player.updateDailyCount(0, 0, 0);
               }
            }
         }
      } catch (Exception var62) {
         _log.log(Level.SEVERE, "Failed restore daily tasks count.", (Throwable)var62);
      }

      if (!found) {
         this.addDailyTasksCount(player, checkHwid);
      }
   }

   public void restoreDailyTasksCount(Player player) {
      String checkHwid = DailyTaskManager.getInstance().isHwidCheck() ? player.getHWID() : player.getIPAddress();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(RESTORE_TASK_COUNT);
      ) {
         statement.setString(1, checkHwid);

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               player.setLastDailyTasks(rset.getInt("dailyCount"));
               if (player.getLastDailyTasks() > 0) {
                  player.updateDailyCount(0, 0, 0);
               }
            }
         }
      } catch (Exception var61) {
         _log.log(Level.SEVERE, "Failed restore daily tasks count.", (Throwable)var61);
      }
   }

   public void restoreWeeklyTasksCount(Player player) {
      String checkHwid = DailyTaskManager.getInstance().isHwidCheck() ? player.getHWID() : player.getIPAddress();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(RESTORE_TASK_COUNT);
      ) {
         statement.setString(1, checkHwid);

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               player.setLastWeeklyTasks(rset.getInt("weeklyCount"));
               if (player.getLastWeeklyTasks() > 0) {
                  player.updateDailyCount(0, 0, 0);
               }
            }
         }
      } catch (Exception var61) {
         _log.log(Level.SEVERE, "Failed restore weekly tasks count.", (Throwable)var61);
      }
   }

   public void restoreMonthTasksCount(Player player) {
      String checkHwid = DailyTaskManager.getInstance().isHwidCheck() ? player.getHWID() : player.getIPAddress();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(RESTORE_TASK_COUNT);
      ) {
         statement.setString(1, checkHwid);

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               player.setLastMonthTasks(rset.getInt("monthCount"));
               if (player.getLastMonthTasks() > 0) {
                  player.updateDailyCount(0, 0, 0);
               }
            }
         }
      } catch (Exception var61) {
         _log.log(Level.SEVERE, "Failed restore month tasks count.", (Throwable)var61);
      }
   }

   public static DailyTasksDAO getInstance() {
      return _instance;
   }
}
