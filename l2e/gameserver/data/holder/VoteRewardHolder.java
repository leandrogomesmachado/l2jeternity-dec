package l2e.gameserver.data.holder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.dao.JdbcEntityState;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.entity.mods.votereward.VoteRewardRecord;

public class VoteRewardHolder {
   private static final Logger _log = Logger.getLogger(VoteRewardHolder.class.getName());
   private static final VoteRewardHolder _instance = new VoteRewardHolder();
   private static final String SELECT_SQL_QUERY = "SELECT * FROM votereward_records WHERE site=?";
   private static final String INSERT_SQL_QUERY = "INSERT INTO votereward_records (site, identifier, votes, lastvotedate) VALUES (?,?,?,?)";
   private static final String UPDATE_SQL_QUERY = "UPDATE votereward_records SET votes=?, lastvotedate=? WHERE site=? AND identifier=?";

   public static VoteRewardHolder getInstance() {
      return _instance;
   }

   public void restore(Map<String, VoteRewardRecord> records, String site) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM votereward_records WHERE site=?");
      ) {
         statement.setString(1, site);

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               String identifier = rset.getString("identifier");
               int votes = rset.getInt("votes");
               int lastvotedate = rset.getInt("lastvotedate");
               records.put(identifier, new VoteRewardRecord(site, identifier, votes, lastvotedate));
            }
         }
      } catch (Exception var63) {
         _log.log(Level.SEVERE, "VoteRewardRecordsDAO.select(String):" + var63, (Throwable)var63);
      }
   }

   public void save(VoteRewardRecord voteRewardRecord) {
      if (voteRewardRecord.getJdbcState().isSavable()) {
         voteRewardRecord.setJdbcState(JdbcEntityState.STORED);
         this.save0(voteRewardRecord);
      }
   }

   private void save0(VoteRewardRecord voteRewardRecord) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("INSERT INTO votereward_records (site, identifier, votes, lastvotedate) VALUES (?,?,?,?)");
      ) {
         statement.setString(1, voteRewardRecord.getSite());
         statement.setString(2, voteRewardRecord.getIdentifier());
         statement.setInt(3, voteRewardRecord.getVotes());
         statement.setInt(4, voteRewardRecord.getLastVoteTime());
         statement.executeUpdate();
      } catch (Exception var34) {
         _log.log(Level.SEVERE, "VoteRewardRecordsDAO.save0(VoteRewardRecord):" + var34, (Throwable)var34);
      }
   }

   public void update(VoteRewardRecord voteRewardRecord) {
      if (voteRewardRecord.getJdbcState().isUpdatable()) {
         voteRewardRecord.setJdbcState(JdbcEntityState.STORED);
         this.update0(voteRewardRecord);
      }
   }

   private void update0(VoteRewardRecord voteRewardRecord) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE votereward_records SET votes=?, lastvotedate=? WHERE site=? AND identifier=?");
      ) {
         statement.setInt(1, voteRewardRecord.getVotes());
         statement.setInt(2, voteRewardRecord.getLastVoteTime());
         statement.setString(3, voteRewardRecord.getSite());
         statement.setString(4, voteRewardRecord.getIdentifier());
         statement.execute();
      } catch (Exception var34) {
         _log.log(Level.SEVERE, "VoteRewardRecordsDAO.update0(VoteRewardRecord):" + var34, (Throwable)var34);
      }
   }
}
