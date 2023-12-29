package l2e.gameserver.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.database.StreamDatabaseFactory;
import l2e.gameserver.model.entity.mods.streaming.StreamManager;
import l2e.gameserver.model.entity.mods.streaming.StreamTemplate;

public final class StreamingDAO {
   protected static final Logger _log = Logger.getLogger(StreamingDAO.class.getName());
   private static final String IDS_TO_APPROVE_REGEX = ";";

   public static void onStreamCreated(StreamTemplate stream) {
      try (Connection con = StreamDatabaseFactory.getStreamDatabaseConnection()) {
         try (PreparedStatement statement = con.prepareStatement("SELECT * FROM streams WHERE channel_name=?")) {
            statement.setString(1, stream.getChannelName());

            try (ResultSet rset = statement.executeQuery()) {
               if (rset.next()) {
                  int attachedPlayerId = rset.getInt("connected_player_id");
                  if (attachedPlayerId > 0) {
                     stream.setAttachedPlayerId(attachedPlayerId, rset.getString("connected_player_server"));
                  }

                  List<Integer> idsToApprove = parseIdsToApprove(rset.getString("ids_awaiting_approval"));
                  if (!idsToApprove.isEmpty()) {
                     stream.addIdsToApprove(idsToApprove);
                  }

                  stream.setNotRewardedSeconds(rset.getLong("not_rewarded_seconds"));
                  stream.setTotalRewardedSecondsToday(rset.getLong("total_rewarded_seconds_today"));
                  stream.setPunishedUntilDate(rset.getLong("punished_until_date"));
                  return;
               }
            }
         } catch (SQLException var107) {
            _log.log(Level.WARNING, "Error while searching for " + stream + " in streams Table!", (Throwable)var107);
         }

         try (PreparedStatement statement = con.prepareStatement("INSERT INTO streams VALUES(?,?,?,?,?,?,?)")) {
            statement.setString(1, stream.getChannelName());
            statement.setInt(2, stream.getAttachedPlayerId());
            statement.setString(3, stream.getAttachedPlayerId() > 0 ? Config.SERVER_NAME : "");
            statement.setString(4, createIdsToApproveString(stream.getIdsToApprove()));
            statement.setInt(5, 0);
            statement.setInt(6, 0);
            statement.setInt(7, -1);
            statement.executeUpdate();
         } catch (SQLException var102) {
            _log.log(Level.WARNING, "Error while inserting " + stream + " to streams Table!", (Throwable)var102);
         }
      } catch (SQLException var110) {
         _log.log(Level.WARNING, "Error while connecting to Database with streams Table!", (Throwable)var110);
      }
   }

   public static void updateStream(StreamTemplate stream) {
      try (
         Connection con = StreamDatabaseFactory.getStreamDatabaseConnection();
         PreparedStatement statement = con.prepareStatement(
            "UPDATE streams SET connected_player_id=?, connected_player_server=?, ids_awaiting_approval = ?, not_rewarded_seconds = ?, total_rewarded_seconds_today = ?, punished_until_date = ? WHERE channel_name=?"
         );
      ) {
         statement.setInt(1, stream.getAttachedPlayerId());
         statement.setString(2, stream.getAttachedPlayerServer());
         statement.setString(3, createIdsToApproveString(stream.getIdsToApprove()));
         statement.setLong(4, stream.getNotRewardedSeconds());
         statement.setLong(5, stream.getTotalRewardedSecondsToday());
         statement.setLong(6, stream.getPunishedUntilDate());
         statement.setString(7, stream.getChannelName());
         statement.executeUpdate();
      } catch (SQLException var33) {
         _log.log(Level.WARNING, "Error while updating " + stream + "!", (Throwable)var33);
      }
   }

   public static void reloadStreams() {
      try (
         Connection con = StreamDatabaseFactory.getStreamDatabaseConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM streams");
         ResultSet rset = statement.executeQuery();
      ) {
         while(rset.next()) {
            String channelName = rset.getString("channel_name");
            StreamTemplate stream = StreamManager.getInstance().getStreamByChannelName(channelName);
            if (stream != null) {
               stream.setAttachedPlayerId(rset.getInt("connected_player_id"), rset.getString("connected_player_server"));
               stream.getIdsToApprove().clear();
               stream.addIdsToApprove(parseIdsToApprove(rset.getString("ids_awaiting_approval")));
            }
         }
      } catch (SQLException var59) {
         _log.log(Level.WARNING, "Error while reloading Streams!", (Throwable)var59);
      }
   }

   public static void resetTotalRewardedTimes() {
      try (
         Connection con = StreamDatabaseFactory.getStreamDatabaseConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE streams SET total_rewarded_seconds_today = 0");
      ) {
         statement.executeUpdate();
      } catch (SQLException var32) {
         _log.log(Level.WARNING, "Error while resetting Total Rewarded Times of Streams!", (Throwable)var32);
      }
   }

   public static void saveRewardTimes() {
      try (
         Connection con = StreamDatabaseFactory.getStreamDatabaseConnection();
         PreparedStatement statement = con.prepareStatement(
            "UPDATE streams SET not_rewarded_seconds = ?, total_rewarded_seconds_today = ? WHERE channel_name=?"
         );
      ) {
         for(StreamTemplate stream : StreamManager.getInstance().getAllActiveStreamsCopy()) {
            if (stream.getAttachedPlayerId() > 0 && stream.getAttachedPlayerServer().equals(Config.SERVER_NAME)) {
               statement.setLong(1, stream.getNotRewardedSeconds());
               statement.setLong(2, stream.getTotalRewardedSecondsToday());
               statement.setString(3, stream.getChannelName());
               statement.executeUpdate();
            }
         }
      } catch (SQLException var33) {
         _log.log(Level.WARNING, "Error while saving Streams to Database!", (Throwable)var33);
      }
   }

   private static String createIdsToApproveString(List<Integer> idsToApprove) {
      StringBuilder builder = new StringBuilder();

      for(int i = 0; i < idsToApprove.size(); ++i) {
         if (i > 0) {
            builder.append(";");
         }

         Integer id = idsToApprove.get(i);
         builder.append(id);
      }

      return builder.toString();
   }

   private static List<Integer> parseIdsToApprove(String textToParse) {
      String[] idsString = textToParse.split(";");
      if (idsString.length == 1 && idsString[0].isEmpty()) {
         return new ArrayList<>(0);
      } else {
         List<Integer> ids = new ArrayList<>(idsString.length);

         for(String anIdsString : idsString) {
            ids.add(Integer.parseInt(anIdsString));
         }

         return ids;
      }
   }
}
