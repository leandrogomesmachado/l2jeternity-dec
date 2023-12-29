package l2e.gameserver.model.entity.mods.facebook.extractions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Level;
import l2e.commons.annotations.Nullable;
import l2e.gameserver.data.holder.FacebookProfilesHolder;
import l2e.gameserver.data.holder.OfficialPostsHolder;
import l2e.gameserver.model.entity.mods.facebook.ActionsAwaitingOwner;
import l2e.gameserver.model.entity.mods.facebook.ActionsExtractingManager;
import l2e.gameserver.model.entity.mods.facebook.ActionsExtractor;
import l2e.gameserver.model.entity.mods.facebook.CompletedTasksHistory;
import l2e.gameserver.model.entity.mods.facebook.FacebookAction;
import l2e.gameserver.model.entity.mods.facebook.FacebookActionType;
import l2e.gameserver.model.entity.mods.facebook.OfficialPost;
import l2e.gameserver.model.entity.mods.facebook.action.Comment;
import l2e.gameserver.model.entity.mods.facebook.template.FacebookProfile;
import org.json.JSONArray;
import org.json.JSONObject;

public class ExtractComments implements ActionsExtractor {
   @Override
   public void extractData(String token) throws IOException {
      long currentTime = -1L;

      for(OfficialPost activePost : OfficialPostsHolder.getInstance().getActivePostsForIterate(FacebookActionType.COMMENT)) {
         URL apiCallURL = prepareAPICall(activePost.getId(), token);
         JSONObject extractionResult = this.call(apiCallURL);
         JSONArray comments = extractionResult.getJSONArray("data");
         ArrayList<FacebookAction> finishedActions = CompletedTasksHistory.getInstance().getCompletedTasks(activePost, FacebookActionType.COMMENT);
         ArrayList<FacebookAction> awaitingActions = ActionsAwaitingOwner.getInstance().getActionsCopy(activePost, FacebookActionType.COMMENT);

         for(int x = 0; x < comments.length(); ++x) {
            JSONObject commentData = comments.getJSONObject(x);
            String id = commentData.getString("id");
            FacebookAction existingAction = findExistingAction(finishedActions, id);
            boolean isCompletedTask;
            if (existingAction == null) {
               existingAction = findExistingAction(awaitingActions, id);
               if (existingAction != null) {
                  awaitingActions.remove(existingAction);
               }

               isCompletedTask = false;
            } else {
               finishedActions.remove(existingAction);
               isCompletedTask = true;
            }

            if (existingAction == null) {
               try {
                  if (!commentData.isNull("from")) {
                     String message = commentData.getString("message");
                     long createdTime = this.parseFacebookDate(commentData.getString("created_time"));
                     JSONObject from = commentData.getJSONObject("from");
                     String executorName = from.getString("name");
                     String executorId = from.getString("id");
                     FacebookProfile profile = FacebookProfilesHolder.getInstance().loadOrCreateProfile(executorId, executorName);
                     if (currentTime < 0L) {
                        currentTime = System.currentTimeMillis();
                     }

                     ActionsExtractingManager.getInstance().onActionExtracted(new Comment(id, profile, message, createdTime, currentTime, activePost));
                  }
               } catch (ParseException var24) {
                  _log.log(Level.SEVERE, "Error while parsing created_time of " + commentData, (Throwable)var24);
               }
            } else if (!existingAction.getMessage().equals(commentData.getString("message")) && isCompletedTask) {
               CompletedTasksHistory.getInstance().setMessageChanged(existingAction, commentData.getString("message"), true);
            }
         }

         for(FacebookAction removedAction : finishedActions) {
            ActionsExtractingManager.onActionDisappeared(removedAction, true);
         }

         for(FacebookAction removedAction : awaitingActions) {
            ActionsExtractingManager.onActionDisappeared(removedAction, false);
         }
      }
   }

   @Nullable
   private static FacebookAction findExistingAction(Iterable<FacebookAction> list, String commentId) {
      for(FacebookAction action : list) {
         if (action.getId().equals(commentId)) {
            return action;
         }
      }

      return null;
   }

   private static URL prepareAPICall(String postId, String token) throws MalformedURLException {
      return new URL("https://graph.facebook.com/" + postId + "/comments?fields=id,message,created_time,from&limit=1000&access_token=" + token);
   }
}
