package l2e.gameserver.model.entity.mods.facebook.template;

import l2e.gameserver.model.entity.mods.facebook.FacebookAction;
import l2e.gameserver.model.entity.mods.facebook.FacebookActionType;

public class CompletedTask implements FacebookAction {
   private final int _playerId;
   private final long _takenDate;
   private final FacebookAction _action;
   private CompletedTask.CommentApprovalType _commentApprovalType;
   private boolean _isRewarded;

   public CompletedTask(ActiveTask task, FacebookAction action, CompletedTask.CommentApprovalType commentApprovalType, boolean isRewarded) {
      this._playerId = task.getPlayerId();
      this._takenDate = task.getTakenDate();
      this._action = action;
      this._commentApprovalType = commentApprovalType;
      this._isRewarded = isRewarded;
   }

   public CompletedTask(int playerId, long takenDate, FacebookAction action, CompletedTask.CommentApprovalType commentApprovalType, boolean isRewarded) {
      this._playerId = playerId;
      this._takenDate = takenDate;
      this._action = action;
      this._commentApprovalType = commentApprovalType;
      this._isRewarded = isRewarded;
   }

   public int getPlayerId() {
      return this._playerId;
   }

   public long getTakenDate() {
      return this._takenDate;
   }

   public FacebookAction getAction() {
      return this._action;
   }

   public void setCommentApproved(CompletedTask.CommentApprovalType commentApprovalType) {
      this._commentApprovalType = commentApprovalType;
   }

   public CompletedTask.CommentApprovalType getCommentApprovalType() {
      return this._commentApprovalType;
   }

   public void setRewarded(boolean isRewarded) {
      this._isRewarded = isRewarded;
   }

   public boolean isRewarded() {
      return this._isRewarded;
   }

   @Override
   public String getId() {
      return this._action.getId();
   }

   @Override
   public FacebookActionType getActionType() {
      return this._action.getActionType();
   }

   @Override
   public FacebookProfile getExecutor() {
      return this._action.getExecutor();
   }

   @Override
   public long getCreatedDate() {
      return this._action.getCreatedDate();
   }

   @Override
   public long getExtractionDate() {
      return this._action.getExtractionDate();
   }

   @Override
   public String getMessage() {
      return this._action.getMessage();
   }

   @Override
   public void changeMessage(String newMessage) {
      this._action.changeMessage(newMessage);
   }

   @Override
   public FacebookAction getFather() {
      return this._action.getFather();
   }

   @Override
   public boolean canBeRemoved() {
      return this._action.canBeRemoved();
   }

   @Override
   public void remove() {
      this._action.remove();
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof CompletedTask && this._playerId == ((CompletedTask)obj)._playerId && this._action.equals(((CompletedTask)obj)._action);
   }

   @Override
   public int hashCode() {
      int result = this._playerId;
      return 31 * result + this._action.hashCode();
   }

   @Override
   public String toString() {
      return "CompletedTask{playerId="
         + this._playerId
         + ", takenDate="
         + this._takenDate
         + ", action="
         + this._action
         + ", commentApprovalType="
         + this._commentApprovalType
         + ", isRewarded="
         + this._isRewarded
         + '}';
   }

   public static enum CommentApprovalType {
      NOT_YET_CHECKED,
      NOT_APPROVED,
      APPROVED;
   }
}
