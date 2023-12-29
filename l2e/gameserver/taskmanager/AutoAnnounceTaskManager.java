package l2e.gameserver.taskmanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Broadcast;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.database.DatabaseFactory;

public class AutoAnnounceTaskManager {
   private static final Logger _log = Logger.getLogger(AutoAnnounceTaskManager.class.getName());
   protected final List<AutoAnnounceTaskManager.AutoAnnouncement> _announces = new ArrayList<>();
   private int _nextId = 1;

   protected AutoAnnounceTaskManager() {
      this.restore();
   }

   public List<AutoAnnounceTaskManager.AutoAnnouncement> getAutoAnnouncements() {
      return this._announces;
   }

   public void restore() {
      if (!this._announces.isEmpty()) {
         for(AutoAnnounceTaskManager.AutoAnnouncement a : this._announces) {
            a.stopAnnounce();
         }

         this._announces.clear();
      }

      int count = 0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         Statement s = con.createStatement();
         ResultSet data = s.executeQuery("SELECT * FROM auto_announcements");
      ) {
         while(data.next()) {
            int id = data.getInt("id");
            long initial = data.getLong("initial");
            long delay = data.getLong("delay");
            int repeat = data.getInt("cycle");
            String memo = data.getString("memo");
            boolean isCritical = Boolean.parseBoolean(data.getString("isCritical"));
            String[] text = memo.split("/n");
            ThreadPoolManager.getInstance().schedule(new AutoAnnounceTaskManager.AutoAnnouncement(id, delay, repeat, text, isCritical), initial);
            ++count;
            if (this._nextId <= id) {
               this._nextId = id + 1;
            }
         }
      } catch (Exception var68) {
         _log.log(Level.SEVERE, "AutoAnnoucements: Failed to load announcements data.", (Throwable)var68);
      }

      _log.log(Level.INFO, "AutoAnnoucements: Loaded " + count + " Auto Annoucement Data.");
   }

   public void addAutoAnnounce(long initial, long delay, int repeat, String memo, boolean isCritical) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "INSERT INTO auto_announcements (id, initial, delay, cycle, memo, isCritical) VALUES (?,?,?,?,?,?)"
         );
      ) {
         statement.setInt(1, this._nextId);
         statement.setLong(2, initial);
         statement.setLong(3, delay);
         statement.setInt(4, repeat);
         statement.setString(5, memo);
         statement.setString(6, String.valueOf(isCritical));
         statement.execute();
         String[] text = memo.split("/n");
         ThreadPoolManager.getInstance().schedule(new AutoAnnounceTaskManager.AutoAnnouncement(this._nextId++, delay, repeat, text, isCritical), initial);
      } catch (Exception var40) {
         _log.log(Level.SEVERE, "AutoAnnoucements: Failed to add announcements data.", (Throwable)var40);
      }
   }

   public void deleteAutoAnnounce(int index) {
      AutoAnnounceTaskManager.AutoAnnouncement a = this._announces.remove(index);
      a.stopAnnounce();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM auto_announcements WHERE id = ?");
      ) {
         statement.setInt(1, a.getId());
         statement.execute();
      } catch (Exception var35) {
         _log.log(Level.SEVERE, "AutoAnnoucements: Failed to delete announcements data.", (Throwable)var35);
      }
   }

   public void announce(String text, boolean isCritical) {
      Broadcast.announceToOnlinePlayers(text, isCritical);
      if (Config.LOG_AUTO_ANNOUNCEMENTS) {
         _log.info((isCritical ? "Critical AutoAnnounce" : "AutoAnnounce") + text);
      }
   }

   public static AutoAnnounceTaskManager getInstance() {
      return AutoAnnounceTaskManager.SingletonHolder._instance;
   }

   public class AutoAnnouncement implements Runnable {
      private final int _id;
      private final long _delay;
      private int _repeat = -1;
      private final String[] _memo;
      private boolean _stopped = false;
      private final boolean _isCritical;

      public AutoAnnouncement(int id, long delay, int repeat, String[] memo, boolean isCritical) {
         this._id = id;
         this._delay = delay;
         this._repeat = repeat;
         this._memo = memo;
         this._isCritical = isCritical;
         if (!AutoAnnounceTaskManager.this._announces.contains(this)) {
            AutoAnnounceTaskManager.this._announces.add(this);
         }
      }

      public int getId() {
         return this._id;
      }

      public String[] getMemo() {
         return this._memo;
      }

      public void stopAnnounce() {
         this._stopped = true;
      }

      public boolean isCritical() {
         return this._isCritical;
      }

      @Override
      public void run() {
         if (!this._stopped && this._repeat != 0) {
            for(String text : this._memo) {
               AutoAnnounceTaskManager.this.announce(text, this._isCritical);
            }

            if (this._repeat > 0) {
               --this._repeat;
            }

            ThreadPoolManager.getInstance().schedule(this, this._delay);
         } else {
            this.stopAnnounce();
         }
      }
   }

   private static class SingletonHolder {
      protected static final AutoAnnounceTaskManager _instance = new AutoAnnounceTaskManager();
   }
}
