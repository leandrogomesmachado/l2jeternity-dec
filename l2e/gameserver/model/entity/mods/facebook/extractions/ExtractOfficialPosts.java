package l2e.gameserver.model.entity.mods.facebook.extractions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import l2e.gameserver.data.dao.FacebookDAO;
import l2e.gameserver.data.holder.FacebookProfilesHolder;
import l2e.gameserver.data.holder.OfficialPostsHolder;
import l2e.gameserver.model.entity.mods.facebook.ActionsExtractor;
import l2e.gameserver.model.entity.mods.facebook.OfficialPost;
import l2e.gameserver.model.entity.mods.facebook.template.FacebookProfile;
import org.json.JSONArray;
import org.json.JSONObject;

public class ExtractOfficialPosts implements ActionsExtractor {
   @Override
   public void extractData(String token) throws IOException {
      List<OfficialPost> recentPosts = OfficialPostsHolder.getInstance().getRecentOfficialPostsForIterate();
      URL apiCallURL = prepareAPICall(token);
      JSONObject extractionResult = this.call(apiCallURL);
      JSONArray data = extractionResult.getJSONArray("data");

      for(int i = 0; i < data.length(); ++i) {
         JSONObject postData = data.getJSONObject(i);

         try {
            String postId = postData.getString("id");
            Optional<OfficialPost> activePost = recentPosts.stream().filter(iPost -> iPost.getId().equals(postId)).findAny();
            if (!activePost.isPresent()) {
               String message = postData.getString("message");
               long createdTime = this.parseFacebookDate(postData.getString("created_time"));
               JSONObject from = postData.getJSONObject("from");
               String executorName = from.getString("name");
               String executorId = from.getString("id");
               FacebookProfile profile = FacebookProfilesHolder.getInstance().loadOrCreateProfile(executorId, executorName);
               long extractionDate = System.currentTimeMillis();
               OfficialPost post = new OfficialPost(postId, profile, message, createdTime, extractionDate);
               FacebookDAO.loadOfficialPostData(post);
               OfficialPostsHolder.getInstance().addNewActivePost(post, true);
            }
         } catch (ParseException var20) {
            _log.log(Level.SEVERE, "Error while parsing created_time of " + postData, (Throwable)var20);
         }
      }
   }

   private static URL prepareAPICall(String token) throws MalformedURLException {
      int limit = OfficialPostsHolder.getInstance().getMinimumPostsToExtract();
      return new URL("https://graph.facebook.com/me/posts?fields=id,message,created_time,from&limit=" + limit + "&access_token=" + token);
   }
}
