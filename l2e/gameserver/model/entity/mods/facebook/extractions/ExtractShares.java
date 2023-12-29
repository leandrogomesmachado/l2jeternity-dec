package l2e.gameserver.model.entity.mods.facebook.extractions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
import l2e.gameserver.model.entity.mods.facebook.action.Share;
import l2e.gameserver.model.entity.mods.facebook.template.FacebookProfile;
import org.json.JSONArray;
import org.json.JSONObject;

public class ExtractShares implements ActionsExtractor {
   @Override
   public void extractData(String token) throws IOException {
      long currentTime = -1L;

      for(OfficialPost activePost : OfficialPostsHolder.getInstance().getActivePostsForIterate(FacebookActionType.SHARE)) {
         URL apiCallURL = prepareAPICall(activePost.getId(), token);
         JSONObject extractionResult = this.call(apiCallURL);
         JSONArray share = extractionResult.getJSONArray("data");
         ArrayList<FacebookAction> finishedActions = CompletedTasksHistory.getInstance().getCompletedTasks(activePost, FacebookActionType.SHARE);
         ArrayList<FacebookAction> awaitingActions = ActionsAwaitingOwner.getInstance().getActionsCopy(activePost, FacebookActionType.SHARE);

         for(int i = 0; i < share.length(); ++i) {
            JSONObject shareData = share.getJSONObject(i);
            String executorId = shareData.getString("id");
            FacebookAction existingAction = findExistingAction(finishedActions, executorId);
            if (existingAction == null) {
               existingAction = findExistingAction(awaitingActions, executorId);
               if (existingAction != null) {
                  awaitingActions.remove(existingAction);
               }
            } else {
               finishedActions.remove(existingAction);
            }

            if (existingAction == null) {
               if (currentTime < 0L) {
                  currentTime = System.currentTimeMillis();
               }

               FacebookProfile executor = FacebookProfilesHolder.getInstance().loadOrCreateProfile(executorId, shareData.getString("name"));
               ActionsExtractingManager.getInstance().onActionExtracted(new Share(executor, currentTime, activePost));
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
   private static FacebookAction findExistingAction(Iterable<FacebookAction> list, String executorId) {
      for(FacebookAction action : list) {
         if (action.getExecutor().getId().equals(executorId)) {
            return action;
         }
      }

      return null;
   }

   private static URL prepareAPICall(String postId, String token) throws MalformedURLException {
      return new URL("https://graph.facebook.com/" + postId + "/sharedposts?fields=name,id&limit=1000&access_token=" + token);
   }
}
