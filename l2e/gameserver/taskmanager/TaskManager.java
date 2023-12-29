package l2e.gameserver.taskmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.threading.RunnableImpl;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.taskmanager.tasks.SoIStageUpdater;
import l2e.gameserver.taskmanager.tasks.TaskBirthday;
import l2e.gameserver.taskmanager.tasks.TaskChatMessage;
import l2e.gameserver.taskmanager.tasks.TaskClanLeaderApply;
import l2e.gameserver.taskmanager.tasks.TaskCleanUp;
import l2e.gameserver.taskmanager.tasks.TaskDailySkillReuseClean;
import l2e.gameserver.taskmanager.tasks.TaskDailyTasks;
import l2e.gameserver.taskmanager.tasks.TaskGlobalVariablesSave;
import l2e.gameserver.taskmanager.tasks.TaskNevitSystem;
import l2e.gameserver.taskmanager.tasks.TaskOlympiadSave;
import l2e.gameserver.taskmanager.tasks.TaskQuestHwidMap;
import l2e.gameserver.taskmanager.tasks.TaskRecom;
import l2e.gameserver.taskmanager.tasks.TaskRestart;
import l2e.gameserver.taskmanager.tasks.TaskRevenges;
import l2e.gameserver.taskmanager.tasks.TaskSevenSignsUpdate;
import l2e.gameserver.taskmanager.tasks.TaskShutdown;

public final class TaskManager {
   protected static final Logger _log = Logger.getLogger(TaskManager.class.getName());
   private final Map<Integer, Task> _tasks = new ConcurrentHashMap<>();
   protected final List<TaskManager.ExecutedTask> _currentTasks = new CopyOnWriteArrayList<>();
   protected static final String[] SQL_STATEMENTS = new String[]{
      "SELECT id,task,type,last_activation,param1,param2,param3 FROM global_tasks",
      "UPDATE global_tasks SET last_activation=? WHERE id=?",
      "SELECT id FROM global_tasks WHERE task=?",
      "INSERT INTO global_tasks (task,type,last_activation,param1,param2,param3) VALUES(?,?,?,?,?,?)"
   };

   protected TaskManager() {
      this.initializate();
      this.startAllTasks();
      _log.log(Level.INFO, this.getClass().getSimpleName() + ": Loaded: " + this._tasks.size() + " Tasks");
   }

   private void initializate() {
      this.registerTask(new SoIStageUpdater());
      this.registerTask(new TaskBirthday());
      if (Config.ALLOW_CUSTOM_CHAT) {
         this.registerTask(new TaskChatMessage());
      }

      if (Config.ALLOW_DAILY_TASKS) {
         this.registerTask(new TaskDailyTasks());
      }

      if (Config.ALLOW_REVENGE_SYSTEM) {
         this.registerTask(new TaskRevenges());
      }

      this.registerTask(new TaskClanLeaderApply());
      this.registerTask(new TaskCleanUp());
      this.registerTask(new TaskDailySkillReuseClean());
      this.registerTask(new TaskGlobalVariablesSave());
      this.registerTask(new TaskNevitSystem());
      this.registerTask(new TaskOlympiadSave());
      this.registerTask(new TaskRecom());
      this.registerTask(new TaskRestart());
      this.registerTask(new TaskSevenSignsUpdate());
      this.registerTask(new TaskQuestHwidMap());
      this.registerTask(new TaskShutdown());
   }

   public void registerTask(Task task) {
      int key = task.getName().hashCode();
      if (!this._tasks.containsKey(key)) {
         this._tasks.put(key, task);
         task.initializate();
      }
   }

   private void startAllTasks() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(SQL_STATEMENTS[0]);
         ResultSet rset = statement.executeQuery();
      ) {
         while(rset.next()) {
            Task task = this._tasks.get(rset.getString("task").trim().toLowerCase().hashCode());
            if (task != null) {
               TaskTypes type = TaskTypes.valueOf(rset.getString("type"));
               if (type != TaskTypes.TYPE_NONE) {
                  TaskManager.ExecutedTask current = new TaskManager.ExecutedTask(task, type, rset);
                  if (this.launchTask(current)) {
                     this._currentTasks.add(current);
                  }
               }
            }
         }
      } catch (Exception var61) {
         _log.log(Level.SEVERE, this.getClass().getSimpleName() + ": Error while loading Global Task table: " + var61.getMessage(), (Throwable)var61);
      }
   }

   private boolean launchTask(TaskManager.ExecutedTask task) {
      ThreadPoolManager scheduler = ThreadPoolManager.getInstance();
      TaskTypes type = task.getType();
      switch(type) {
         case TYPE_STARTUP:
            task.run();
            return false;
         case TYPE_SHEDULED: {
            long delay = Long.valueOf(task.getParams()[0]);
            task.scheduled = scheduler.schedule(task, delay);
            return true;
         }
         case TYPE_FIXED_SHEDULED: {
            long delay = Long.valueOf(task.getParams()[0]);
            long interval = Long.valueOf(task.getParams()[1]);
            task.scheduled = scheduler.scheduleAtFixedRate(task, delay, interval);
            return true;
         }
         case TYPE_TIME:
            try {
               Date desired = DateFormat.getInstance().parse(task.getParams()[0]);
               long diff = desired.getTime() - System.currentTimeMillis();
               if (diff >= 0L) {
                  task.scheduled = scheduler.schedule(task, diff);
                  return true;
               }

               _log.info(this.getClass().getSimpleName() + ": Task " + task.getId() + " is obsoleted.");
            } catch (Exception var14) {
            }
            break;
         case TYPE_SPECIAL:
            ScheduledFuture<?> result = task.getTask().launchSpecial(task);
            if (result != null) {
               task.scheduled = result;
               return true;
            }
            break;
         case TYPE_GLOBAL_TASK: {
            long interval = Long.valueOf(task.getParams()[0]) * 86400000L;
            String[] hour = task.getParams()[1].split(":");
            if (hour.length != 3) {
               _log.warning(this.getClass().getSimpleName() + ": Task " + task.getId() + " has incorrect parameters");
               return false;
            }

            Calendar check = Calendar.getInstance();
            check.setTimeInMillis(task.getLastActivation() + interval);
            Calendar min = Calendar.getInstance();

            try {
               min.set(11, Integer.parseInt(hour[0]));
               min.set(12, Integer.parseInt(hour[1]));
               min.set(13, Integer.parseInt(hour[2]));
            } catch (Exception var13) {
               _log.log(
                  Level.WARNING, this.getClass().getSimpleName() + ": Bad parameter on task " + task.getId() + ": " + var13.getMessage(), (Throwable)var13
               );
               return false;
            }

            long delay = min.getTimeInMillis() - System.currentTimeMillis();
            if (check.after(min) || delay < 0L) {
               delay += interval;
            }

            task.scheduled = scheduler.scheduleAtFixedRate(task, delay, interval);
            return true;
         }
         default:
            return false;
      }

      return false;
   }

   public static boolean addUniqueTask(String task, TaskTypes type, String param1, String param2, String param3) {
      return addUniqueTask(task, type, param1, param2, param3, 0L);
   }

   public static boolean addUniqueTask(String task, TaskTypes type, String param1, String param2, String param3, long lastActivation) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps1 = con.prepareStatement(SQL_STATEMENTS[2]);
      ) {
         ps1.setString(1, task);

         try (ResultSet rs = ps1.executeQuery()) {
            if (!rs.next()) {
               try (PreparedStatement ps2 = con.prepareStatement(SQL_STATEMENTS[3])) {
                  ps2.setString(1, task);
                  ps2.setString(2, type.toString());
                  ps2.setLong(3, lastActivation);
                  ps2.setString(4, param1);
                  ps2.setString(5, param2);
                  ps2.setString(6, param3);
                  ps2.execute();
               }
            }
         }

         return true;
      } catch (SQLException var98) {
         _log.log(Level.WARNING, TaskManager.class.getSimpleName() + ": Cannot add the unique task: " + var98.getMessage(), (Throwable)var98);
         return false;
      }
   }

   public static boolean addTask(String task, TaskTypes type, String param1, String param2, String param3) {
      return addTask(task, type, param1, param2, param3, 0L);
   }

   public static boolean addTask(String task, TaskTypes type, String param1, String param2, String param3, long lastActivation) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(SQL_STATEMENTS[3]);
      ) {
         statement.setString(1, task);
         statement.setString(2, type.toString());
         statement.setLong(3, lastActivation);
         statement.setString(4, param1);
         statement.setString(5, param2);
         statement.setString(6, param3);
         statement.execute();
         return true;
      } catch (SQLException var40) {
         _log.log(Level.WARNING, TaskManager.class.getSimpleName() + ": Cannot add the task:  " + var40.getMessage(), (Throwable)var40);
         return false;
      }
   }

   public static TaskManager getInstance() {
      return TaskManager.SingletonHolder._instance;
   }

   public class ExecutedTask extends RunnableImpl {
      int id;
      long lastActivation;
      Task task;
      TaskTypes type;
      String[] params;
      ScheduledFuture<?> scheduled;

      public ExecutedTask(Task ptask, TaskTypes ptype, ResultSet rset) throws SQLException {
         this.task = ptask;
         this.type = ptype;
         this.id = rset.getInt("id");
         this.lastActivation = rset.getLong("last_activation");
         this.params = new String[]{rset.getString("param1"), rset.getString("param2"), rset.getString("param3")};
      }

      @Override
      public void runImpl() throws Exception {
         this.task.onTimeElapsed(this);
         this.lastActivation = System.currentTimeMillis();

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(TaskManager.SQL_STATEMENTS[1]);
         ) {
            statement.setLong(1, this.lastActivation);
            statement.setInt(2, this.id);
            statement.executeUpdate();
         } catch (SQLException var33) {
            TaskManager._log
               .log(
                  Level.WARNING, this.getClass().getSimpleName() + ": Cannot updated the Global Task " + this.id + ": " + var33.getMessage(), (Throwable)var33
               );
         }

         if (this.type == TaskTypes.TYPE_SHEDULED || this.type == TaskTypes.TYPE_TIME) {
            this.stopTask();
         }
      }

      @Override
      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else if (!(object instanceof TaskManager.ExecutedTask)) {
            return false;
         } else {
            return this.id == ((TaskManager.ExecutedTask)object).id;
         }
      }

      @Override
      public int hashCode() {
         return this.id;
      }

      public Task getTask() {
         return this.task;
      }

      public TaskTypes getType() {
         return this.type;
      }

      public int getId() {
         return this.id;
      }

      public String[] getParams() {
         return this.params;
      }

      public long getLastActivation() {
         return this.lastActivation;
      }

      public void stopTask() {
         this.task.onDestroy();
         if (this.scheduled != null) {
            this.scheduled.cancel(true);
         }

         TaskManager.this._currentTasks.remove(this);
      }
   }

   private static class SingletonHolder {
      protected static final TaskManager _instance = new TaskManager();
   }
}
