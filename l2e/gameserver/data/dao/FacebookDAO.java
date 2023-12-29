package l2e.gameserver.data.dao;

import gnu.trove.map.hash.TObjectIntHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.holder.FacebookProfilesHolder;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.entity.mods.facebook.FacebookAction;
import l2e.gameserver.model.entity.mods.facebook.FacebookActionType;
import l2e.gameserver.model.entity.mods.facebook.OfficialPost;
import l2e.gameserver.model.entity.mods.facebook.template.CompletedTask;
import l2e.gameserver.model.entity.mods.facebook.template.FacebookProfile;

public final class FacebookDAO {
   protected static final Logger _log = Logger.getLogger(FacebookDAO.class.getName());

   public static void replaceCompletedTask(CompletedTask task) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("REPLACE INTO facebook_completed_tasks VALUES (?,?,?,?,?,?,?,?,?,?,?)");
      ) {
         statement.setInt(1, task.getPlayerId());
         statement.setLong(2, task.getTakenDate());
         statement.setInt(3, task.getCommentApprovalType().ordinal());
         statement.setInt(4, task.isRewarded() ? 1 : 0);
         statement.setString(5, task.getId() == null ? "" : task.getId());
         statement.setString(6, task.getActionType().toString());
         statement.setString(7, task.getExecutor().getId());
         statement.setLong(8, task.getCreatedDate());
         statement.setLong(9, task.getExtractionDate());
         statement.setString(10, task.getMessage() == null ? "" : task.getMessage());
         statement.setString(11, task.getFather() == null ? "" : task.getFather().getId());
         statement.executeUpdate();
      } catch (SQLException var33) {
         _log.log(Level.WARNING, "Error while replaceCompletedTask(" + task + ")", (Throwable)var33);
      }
   }

   public static void deleteCompletedTask(CompletedTask task) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "DELETE FROM facebook_completed_tasks WHERE player_id = ? AND action_id = ? AND action_type_name = ? AND father_id = ?"
         );
      ) {
         statement.setInt(1, task.getPlayerId());
         statement.setString(2, task.getId() == null ? "" : task.getId());
         statement.setString(3, task.getActionType().toString());
         statement.setString(4, task.getFather() == null ? "" : task.getFather().getId());
         statement.executeUpdate();
      } catch (SQLException var33) {
         _log.log(Level.WARNING, "Error while deleteCompletedTask(" + task + ")", (Throwable)var33);
      }
   }

   public static ArrayList<CompletedTask> loadCompletedTasks() {
      ArrayList<CompletedTask> tasks = new ArrayList<>();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM facebook_completed_tasks");
         ResultSet rset = statement.executeQuery();
      ) {
         while(rset.next()) {
            FacebookProfile executor = FacebookProfilesHolder.getInstance().getProfileById(rset.getString("executor_id"));
            if (executor != null) {
               FacebookActionType actionType = FacebookActionType.valueOf(rset.getString("action_type_name"));
               FacebookAction action = actionType.createInstance(rset);
               int playerId = rset.getInt("player_id");
               long takenDate = rset.getLong("taken_date");
               CompletedTask.CommentApprovalType approvalType = CompletedTask.CommentApprovalType.values()[rset.getInt("comment_approved")];
               boolean isRewarded = rset.getInt("rewarded") == 1;
               tasks.add(new CompletedTask(playerId, takenDate, action, approvalType, isRewarded));
            }
         }
      } catch (SQLException var66) {
         _log.log(Level.WARNING, "Error while loading Completed Tasks", (Throwable)var66);
      }

      return tasks;
   }

   public static void replaceFacebookProfile(FacebookProfile profile) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("REPLACE INTO facebook_profiles VALUES(?,?,?,?,?,?,?,?,?,?,?)");
      ) {
         statement.setString(1, profile.getId());
         statement.setString(2, profile.getName());
         statement.setLong(3, profile.getLastCompletedTaskDate());
         statement.setInt(4, profile.getPositivePoints(FacebookActionType.LIKE));
         statement.setInt(5, profile.getPositivePoints(FacebookActionType.COMMENT));
         statement.setInt(6, profile.getPositivePoints(FacebookActionType.POST));
         statement.setInt(7, profile.getPositivePoints(FacebookActionType.SHARE));
         statement.setInt(8, profile.getNegativePoints(FacebookActionType.LIKE));
         statement.setInt(9, profile.getNegativePoints(FacebookActionType.COMMENT));
         statement.setInt(10, profile.getNegativePoints(FacebookActionType.POST));
         statement.setInt(11, profile.getNegativePoints(FacebookActionType.SHARE));
         statement.executeUpdate();
      } catch (SQLException var33) {
         _log.log(Level.WARNING, "Error while replaceFacebookProfile(" + profile + ")", (Throwable)var33);
      }
   }

   public static ArrayList<FacebookProfile> loadFacebookProfiles() {
      ArrayList<FacebookProfile> profiles = new ArrayList<>();

      String id;
      String name;
      long lastCompletedTaskDate;
      TObjectIntHashMap<FacebookActionType> positivePoints;
      TObjectIntHashMap<FacebookActionType> negativePoints;
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM facebook_profiles");
         ResultSet rset = statement.executeQuery();
      ) {
         for(; rset.next(); profiles.add(new FacebookProfile(id, name, lastCompletedTaskDate, positivePoints, negativePoints))) {
            id = rset.getString("id");
            name = rset.getString("name");
            lastCompletedTaskDate = rset.getLong("last_completed_task_date");
            positivePoints = new TObjectIntHashMap<>(0);
            if (rset.getInt("positive_points_like") > 0) {
               positivePoints.put(FacebookActionType.LIKE, rset.getInt("positive_points_like"));
            }

            if (rset.getInt("positive_points_comment") > 0) {
               positivePoints.put(FacebookActionType.COMMENT, rset.getInt("positive_points_comment"));
            }

            if (rset.getInt("positive_points_post") > 0) {
               positivePoints.put(FacebookActionType.POST, rset.getInt("positive_points_post"));
            }

            if (rset.getInt("positive_points_share") > 0) {
               positivePoints.put(FacebookActionType.SHARE, rset.getInt("positive_points_share"));
            }

            negativePoints = new TObjectIntHashMap<>(0);
            if (rset.getInt("negative_points_like") > 0) {
               negativePoints.put(FacebookActionType.LIKE, rset.getInt("negative_points_like"));
            }

            if (rset.getInt("negative_points_comment") > 0) {
               negativePoints.put(FacebookActionType.COMMENT, rset.getInt("negative_points_comment"));
            }

            if (rset.getInt("negative_points_post") > 0) {
               negativePoints.put(FacebookActionType.POST, rset.getInt("negative_points_post"));
            }

            if (rset.getInt("negative_points_share") > 0) {
               negativePoints.put(FacebookActionType.SHARE, rset.getInt("negative_points_share"));
            }
         }
      } catch (SQLException var64) {
         _log.log(Level.WARNING, "Error while loading Facebook Profiles", (Throwable)var64);
      }

      return profiles;
   }

   public static void replaceOfficialPost(OfficialPost post) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("REPLACE INTO facebook_official_posts VALUES(?,?,?,?,?)");
      ) {
         statement.setString(1, post.getId());
         statement.setInt(2, post.isActionTypeRewarded(FacebookActionType.LIKE) ? 1 : 0);
         statement.setInt(3, post.isActionTypeRewarded(FacebookActionType.COMMENT) ? 1 : 0);
         statement.setInt(4, post.isActionTypeRewarded(FacebookActionType.POST) ? 1 : 0);
         statement.setInt(5, post.isActionTypeRewarded(FacebookActionType.SHARE) ? 1 : 0);
         statement.executeUpdate();
      } catch (SQLException var33) {
         _log.log(Level.WARNING, "Error while replaceOfficialPost(" + post + ")", (Throwable)var33);
      }
   }

   public static void loadOfficialPostData(OfficialPost post) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM facebook_official_posts WHERE post_id = ?");
      ) {
         statement.setString(1, post.getId());

         try (ResultSet rset = statement.executeQuery()) {
            if (rset.next()) {
               EnumSet<FacebookActionType> rewardedActions = EnumSet.noneOf(FacebookActionType.class);
               if (rset.getInt("rewards_like") == 1) {
                  rewardedActions.add(FacebookActionType.LIKE);
               }

               if (rset.getInt("rewards_comment") == 1) {
                  rewardedActions.add(FacebookActionType.COMMENT);
               }

               if (rset.getInt("rewards_post") == 1) {
                  rewardedActions.add(FacebookActionType.POST);
               }

               if (rset.getInt("rewards_share") == 1) {
                  rewardedActions.add(FacebookActionType.SHARE);
               }

               post.setRewardedActions(rewardedActions);
            }
         }
      } catch (SQLException var59) {
         _log.log(Level.WARNING, "Error while loadOfficialPostData(" + post + ")", (Throwable)var59);
      }
   }
}
