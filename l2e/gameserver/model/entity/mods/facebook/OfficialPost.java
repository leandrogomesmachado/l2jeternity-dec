package l2e.gameserver.model.entity.mods.facebook;

import java.util.EnumSet;
import l2e.gameserver.model.entity.mods.facebook.action.Post;
import l2e.gameserver.model.entity.mods.facebook.template.FacebookProfile;

public class OfficialPost extends Post {
   private final EnumSet<FacebookActionType> _rewardedActions;

   public OfficialPost(String id, FacebookProfile executor, String message, long createdTime, long extractionDate, EnumSet<FacebookActionType> rewardedActions) {
      super(id, executor, message, createdTime, extractionDate);
      this._rewardedActions = rewardedActions;
   }

   public OfficialPost(String id, FacebookProfile executor, String message, long createdTime, long extractionDate) {
      super(id, executor, message, createdTime, extractionDate);
      this._rewardedActions = EnumSet.noneOf(FacebookActionType.class);
   }

   public boolean isActionTypeRewarded(FacebookActionType type) {
      return this._rewardedActions.contains(type);
   }

   public boolean isAnyActionTypeRewarded() {
      return !this._rewardedActions.isEmpty();
   }

   public EnumSet<FacebookActionType> getRewardedActionsForIterate() {
      return this._rewardedActions;
   }

   public void setRewardedActions(EnumSet<FacebookActionType> rewardedActions) {
      this._rewardedActions.clear();
      this._rewardedActions.addAll(rewardedActions);
   }

   @Override
   public boolean canBeRemoved() {
      return false;
   }

   @Override
   public void remove() {
   }

   @Override
   public String toString() {
      return "OfficialPost{id='"
         + this.getId()
         + '\''
         + ", executor="
         + this.getExecutor()
         + ", message='"
         + this.getMessage()
         + '\''
         + ", createdTime="
         + this.getCreatedDate()
         + ", extractionDate="
         + this.getExtractionDate()
         + ", rewardedActions="
         + this._rewardedActions
         + '}';
   }
}
