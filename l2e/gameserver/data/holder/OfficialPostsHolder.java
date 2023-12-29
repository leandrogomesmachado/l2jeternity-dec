package l2e.gameserver.data.holder;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.Config;
import l2e.gameserver.data.dao.FacebookDAO;
import l2e.gameserver.model.entity.mods.facebook.ActionsAwaitingOwner;
import l2e.gameserver.model.entity.mods.facebook.FacebookActionType;
import l2e.gameserver.model.entity.mods.facebook.OfficialPost;

public final class OfficialPostsHolder {
   private final List<OfficialPost> _recentOfficialPosts = new ArrayList<>();
   private final List<OfficialPost> _activePosts;
   private final Map<FacebookActionType, ArrayList<OfficialPost>> _activePostsPerActionType = new EnumMap<>(FacebookActionType.class);

   private OfficialPostsHolder() {
      this._activePosts = new ArrayList<>(Config.FACEBOOK_VALID_POST_COUNT);
   }

   public ArrayList<OfficialPost> getActivePostsForIterate(FacebookActionType type) {
      ArrayList<OfficialPost> posts = this._activePostsPerActionType.getOrDefault(type, null);
      if (posts == null) {
         posts = this.calculatePostsPerActionType(type);
      }

      return posts;
   }

   public List<OfficialPost> getActivePostsCopy(FacebookActionType type) {
      List<OfficialPost> posts = this._activePostsPerActionType.getOrDefault(type, null);
      if (posts == null) {
         posts = this.calculatePostsPerActionType(type);
      }

      return new ArrayList<>(posts);
   }

   public List<OfficialPost> getRecentOfficialPostsForIterate() {
      return this._recentOfficialPosts;
   }

   public List<OfficialPost> getActivePostsForIterate() {
      return this._activePosts;
   }

   public void addNewActivePost(OfficialPost post, boolean justExtracted) {
      if (post.isAnyActionTypeRewarded()) {
         this._activePostsPerActionType.clear();
         this._activePosts.add(post);
         ActionsAwaitingOwner.getInstance().addNewFather(post);
      }

      if (justExtracted) {
         this._recentOfficialPosts.add(post);
      }

      if (!justExtracted) {
         ;
      }
   }

   public OfficialPost getOfficialPost(String postId) {
      for(OfficialPost post : this._recentOfficialPosts) {
         if (post.getId().equals(postId)) {
            return post;
         }
      }

      return null;
   }

   public void addNewRewardedAction(String officialPostId, FacebookActionType actionType) {
      OfficialPost post = this.getOfficialPost(officialPostId);
      if (post != null) {
         this.addNewRewardedAction(post, actionType);
      }
   }

   public void addNewRewardedAction(OfficialPost post, FacebookActionType actionType) {
      if (!post.isActionTypeRewarded(actionType)) {
         boolean newActivePost = post.getRewardedActionsForIterate().isEmpty();
         post.getRewardedActionsForIterate().add(actionType);
         this._activePostsPerActionType.clear();
         ActionsAwaitingOwner.getInstance().addNewFather(post);
         if (newActivePost && this._activePosts.size() >= Config.FACEBOOK_VALID_POST_COUNT) {
            this._activePosts.remove(0);
            this._activePosts.add(post);
         }

         FacebookDAO.replaceOfficialPost(post);
      }
   }

   public void removeNewRewardedAction(String officialPostId, FacebookActionType actionType) {
      OfficialPost post = this.getOfficialPost(officialPostId);
      if (post != null) {
         this.removeRewardedAction(post, actionType);
      }
   }

   public void removeRewardedAction(OfficialPost post, FacebookActionType actionType) {
      post.getRewardedActionsForIterate().remove(actionType);
      this._activePostsPerActionType.clear();
      FacebookDAO.replaceOfficialPost(post);
   }

   public int getMinimumPostsToExtract() {
      for(int i = this._recentOfficialPosts.size() - 1; i > Config.FACEBOOK_VALID_POST_COUNT; ++i) {
         if (this._activePosts.contains(this._recentOfficialPosts.get(i))) {
            return i;
         }
      }

      return Config.FACEBOOK_VALID_POST_COUNT;
   }

   private ArrayList<OfficialPost> calculatePostsPerActionType(FacebookActionType type) {
      ArrayList<OfficialPost> posts = new ArrayList<>(this._activePosts.size());

      for(OfficialPost post : this._activePosts) {
         if (post.isActionTypeRewarded(type)) {
            posts.add(post);
         }
      }

      posts.trimToSize();
      this._activePostsPerActionType.put(type, posts);
      return posts;
   }

   public static OfficialPostsHolder getInstance() {
      return OfficialPostsHolder.SingletonHolder.instance;
   }

   private static class SingletonHolder {
      private static final OfficialPostsHolder instance = new OfficialPostsHolder();
   }
}
