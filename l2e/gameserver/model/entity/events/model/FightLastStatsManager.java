package l2e.gameserver.model.entity.events.model;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.parser.FightEventParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.model.template.FightEventLastPlayerStats;

public class FightLastStatsManager {
   private static final Logger _log = Logger.getLogger(FightLastStatsManager.class.getName());
   private final Map<Integer, List<FightEventLastPlayerStats>> _allStats = new HashMap<>();

   public void updateStat(int eventId, Player player, FightLastStatsManager.FightEventStatType type, int score) {
      FightEventLastPlayerStats myStat = this.getMyStat(eventId, player);
      if (myStat == null) {
         myStat = new FightEventLastPlayerStats(player, type.getName(), score);
         this._allStats.get(eventId).add(myStat);
      } else {
         myStat.setScore(score);
      }
   }

   public FightEventLastPlayerStats getMyStat(int id, Player player) {
      for(int eventId : this._allStats.keySet()) {
         if (eventId == id) {
            for(FightEventLastPlayerStats stat : this._allStats.get(eventId)) {
               if (stat.isMyStat(player)) {
                  return stat;
               }
            }
         }
      }

      return null;
   }

   public void updateEventStats(int id) {
      for(int eventId : this._allStats.keySet()) {
         if (eventId == id) {
            for(FightEventLastPlayerStats stat : this._allStats.get(eventId)) {
               this.addEventStats(eventId, stat);
            }
         }
      }
   }

   private void addEventStats(int eventId, FightEventLastPlayerStats stat) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "INSERT INTO events_statistic (eventId, char_name, clan_name, ally_name, classId, scores) VALUES (?,?,?,?,?,?)"
         );
      ) {
         statement.setInt(1, eventId);
         statement.setString(2, stat.getPlayerName());
         statement.setString(3, stat.getClanName());
         statement.setString(4, stat.getAllyName());
         statement.setInt(5, stat.getClassId());
         statement.setInt(6, stat.getScore());
         statement.executeUpdate();
      } catch (Exception var35) {
         _log.log(Level.WARNING, "Could not insert new event stat: " + var35.getMessage(), (Throwable)var35);
      }
   }

   private void clearEventStats(int eventId) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM events_statistic WHERE eventId=?");
      ) {
         statement.setInt(1, eventId);
         statement.execute();
      } catch (Exception var34) {
         _log.log(Level.WARNING, "Failed to clean up event statistic.", (Throwable)var34);
      }
   }

   public List<FightEventLastPlayerStats> getStats(int eventId, boolean sortByScore) {
      List<FightEventLastPlayerStats> listToSort = new ArrayList<>();
      listToSort.addAll(this._allStats.get(eventId));
      if (sortByScore) {
         Comparator<FightEventLastPlayerStats> statsComparator = new FightLastStatsManager.SortRanking();
         Collections.sort(listToSort, statsComparator);
      }

      return listToSort;
   }

   public void clearStats(int eventId) {
      this._allStats.get(eventId).clear();
      this.clearEventStats(eventId);
   }

   public void restoreStats() {
      for(AbstractFightEvent event : FightEventParser.getInstance().getEvents().valueCollection()) {
         if (event != null && !this.isFoundStats(event.getId())) {
            List<FightEventLastPlayerStats> list = new ArrayList<>();
            this._allStats.put(event.getId(), list);
         }
      }

      _log.info(this.getClass().getSimpleName() + ": Clean up all event statistics.");
   }

   private boolean isFoundStats(int id) {
      List<FightEventLastPlayerStats> list = new ArrayList<>();
      boolean found = false;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM events_statistic WHERE eventId=?");
      ) {
         statement.setInt(1, id);

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               found = true;
               String charName = rset.getString("char_name");
               String clan_name = rset.getString("clan_name");
               String ally_name = rset.getString("ally_name");
               int classId = rset.getInt("classId");
               int scores = rset.getInt("scores");
               list.add(new FightEventLastPlayerStats(charName, clan_name, ally_name, classId, scores));
            }
         }
      } catch (Exception var66) {
         _log.log(Level.WARNING, "Failed restore event statistic.", (Throwable)var66);
      }

      if (found) {
         this._allStats.put(id, list);
      }

      return found;
   }

   public static FightLastStatsManager getInstance() {
      return FightLastStatsManager.FightLastStatsManagerHolder._instance;
   }

   public static enum FightEventStatType {
      KILL_PLAYER("Kill Player");

      private final String _name;

      private FightEventStatType(String name) {
         this._name = name;
      }

      public String getName() {
         return this._name;
      }
   }

   private static class FightLastStatsManagerHolder {
      private static final FightLastStatsManager _instance = new FightLastStatsManager();
   }

   private static class SortRanking implements Comparator<FightEventLastPlayerStats>, Serializable {
      private static final long serialVersionUID = 7691414259610932752L;

      private SortRanking() {
      }

      public int compare(FightEventLastPlayerStats o1, FightEventLastPlayerStats o2) {
         return Integer.compare(o2.getScore(), o1.getScore());
      }
   }
}
