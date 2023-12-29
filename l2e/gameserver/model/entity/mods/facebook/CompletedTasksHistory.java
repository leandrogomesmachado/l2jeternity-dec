package l2e.gameserver.model.entity.mods.facebook;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import l2e.commons.annotations.Nullable;
import l2e.gameserver.data.dao.FacebookDAO;
import l2e.gameserver.data.holder.OfficialPostsHolder;
import l2e.gameserver.model.entity.mods.facebook.template.ActiveTask;
import l2e.gameserver.model.entity.mods.facebook.template.CompletedTask;
import l2e.gameserver.model.entity.mods.facebook.template.FacebookProfile;

public final class CompletedTasksHistory {
   private final Map<FacebookProfile, ArrayList<CompletedTask>> _completedTasks;

   private CompletedTasksHistory() {
      ArrayList<CompletedTask> loadedCompletedTasks = FacebookDAO.loadCompletedTasks();
      this._completedTasks = new ConcurrentHashMap<>();

      for(CompletedTask task : loadedCompletedTasks) {
         ArrayList<CompletedTask> tasksPerProfile = this._completedTasks.get(task.getExecutor());
         if (tasksPerProfile == null) {
            tasksPerProfile = new ArrayList<>();
            tasksPerProfile.add(task);
            this._completedTasks.put(task.getExecutor(), tasksPerProfile);
         } else {
            tasksPerProfile.add(task);
         }
      }
   }

   public void addCompletedTask(
      ActiveTask task,
      FacebookAction action,
      CompletedTask.CommentApprovalType commentApprovalType,
      boolean isRewarded,
      boolean saveNewDelay,
      boolean saveInDatabase
   ) {
      List<CompletedTask> existingTasks = this._completedTasks.get(action.getExecutor());
      CompletedTask completedTask = new CompletedTask(task, action, commentApprovalType, isRewarded);
      if (existingTasks != null) {
         existingTasks.add(completedTask);
      } else {
         ArrayList<CompletedTask> newTasksList = new ArrayList<>(3);
         newTasksList.add(completedTask);
         this._completedTasks.put(action.getExecutor(), newTasksList);
      }

      if (saveNewDelay) {
         action.getExecutor().setLastCompletedTaskDate(System.currentTimeMillis());
         if (saveInDatabase) {
            FacebookDAO.replaceFacebookProfile(action.getExecutor());
         }
      }

      if (saveInDatabase) {
         FacebookDAO.replaceCompletedTask(completedTask);
      }
   }

   public void removeCompletedTask(FacebookAction action, boolean removeFromDatabase) {
      List<CompletedTask> existingTasks = this._completedTasks.get(action.getExecutor());
      if (existingTasks != null) {
         for(CompletedTask existingTask : existingTasks) {
            if (existingTask.equals(action) || existingTask.getAction().equals(action)) {
               existingTasks.remove(existingTask);
               if (removeFromDatabase) {
                  FacebookDAO.deleteCompletedTask(existingTask);
               }
            }
         }
      }
   }

   public ArrayList<CompletedTask> getCompletedTasksForIterate(FacebookProfile facebookProfile) {
      ArrayList<CompletedTask> tasks = this._completedTasks.get(facebookProfile);
      return tasks == null ? new ArrayList<>(0) : tasks;
   }

   public ArrayList<CompletedTask> getCompletedTasksCopy(FacebookProfile facebookProfile) {
      ArrayList<CompletedTask> tasks = this._completedTasks.get(facebookProfile);
      return tasks == null ? new ArrayList<>(0) : new ArrayList<>(tasks);
   }

   public CompletedTask getCompletedTask(FacebookAction action) {
      ArrayList<CompletedTask> tasks = this._completedTasks.get(action.getExecutor());
      if (tasks == null) {
         return null;
      } else {
         for(CompletedTask task : tasks) {
            if (task.equals(action) || task.getAction().equals(action)) {
               return task;
            }
         }

         return null;
      }
   }

   public CompletedTask getCompletedTask(String actionId) {
      for(ArrayList<CompletedTask> tasks : this._completedTasks.values()) {
         for(CompletedTask task : tasks) {
            if (actionId.equals(task.getId())) {
               return task;
            }
         }
      }

      return null;
   }

   public ArrayList<FacebookAction> getCompletedTasks(OfficialPost post, FacebookActionType actionType) {
      ArrayList<FacebookAction> results = new ArrayList<>();
      if (post == null) {
         for(ArrayList<CompletedTask> actionsPerProfile : this._completedTasks.values()) {
            for(FacebookAction action : actionsPerProfile) {
               if (action.getActionType() == actionType && action.getFather() == null) {
                  results.add(action);
               }
            }
         }
      } else {
         for(ArrayList<CompletedTask> actionsPerProfile : this._completedTasks.values()) {
            for(FacebookAction action : actionsPerProfile) {
               if (action.getActionType() == actionType && post.equals(action.getFather())) {
                  results.add(action);
               }
            }
         }
      }

      return results;
   }

   public ArrayList<CompletedTask> getTasksThatNeedsApproval() {
      ArrayList<CompletedTask> result = new ArrayList<>(10);

      for(ArrayList<CompletedTask> taskList : this._completedTasks.values()) {
         for(CompletedTask task : taskList) {
            if (task.getCommentApprovalType() == CompletedTask.CommentApprovalType.NOT_YET_CHECKED) {
               result.add(task);
            }
         }
      }

      return result;
   }

   public int countTasksThatNeedsApproval() {
      int count = 0;

      for(ArrayList<CompletedTask> taskList : this._completedTasks.values()) {
         for(CompletedTask task : taskList) {
            if (task.getCommentApprovalType() == CompletedTask.CommentApprovalType.NOT_YET_CHECKED) {
               ++count;
            }
         }
      }

      return count;
   }

   public void setMessageChanged(FacebookAction existingAction, String newMessage, boolean messageGMs) {
      CompletedTask task = this.getCompletedTask(existingAction);
      if (task != null) {
         task.changeMessage(newMessage);
         task.setCommentApproved(CompletedTask.CommentApprovalType.NOT_YET_CHECKED);
         FacebookDAO.replaceCompletedTask(task);
         if (messageGMs) {
            ActiveTasksHandler.notifyGMsMsgsToVerify(false);
         }
      }
   }

   public EnumSet<FacebookActionType> getAvailableNegativeBalanceTypes(FacebookProfile facebookProfile) {
      EnumSet<FacebookActionType> result = EnumSet.noneOf(FacebookActionType.class);

      for(FacebookActionType type : facebookProfile.getNegativePointTypesForIterate()) {
         if (type.haveFather()) {
            if (getInstance().getFatherAction(facebookProfile, type) != null) {
               result.add(type);
            }
         } else {
            result.add(type);
         }
      }

      return result;
   }

   public EnumSet<FacebookActionType> getAvailableActionTypes(FacebookProfile facebookProfile) {
      List<CompletedTask> facebookIdCompletedTasks = this.getCompletedTasksForIterate(facebookProfile);
      EnumSet<FacebookActionType> notAvailableTaskTypes = EnumSet.allOf(FacebookActionType.class);

      for(OfficialPost activePost : OfficialPostsHolder.getInstance().getActivePostsForIterate()) {
         for(FacebookActionType notAvailableTaskType : notAvailableTaskTypes) {
            if (activePost.isActionTypeRewarded(notAvailableTaskType) && getCompletedTask(facebookIdCompletedTasks, activePost, notAvailableTaskType) == null) {
               notAvailableTaskTypes.remove(notAvailableTaskType);
            }
         }
      }

      return EnumSet.complementOf(notAvailableTaskTypes);
   }

   public boolean isActionTypeAvailable(FacebookProfile facebookProfile, FacebookActionType type) {
      List<CompletedTask> facebookIdCompletedTasks = this.getCompletedTasksForIterate(facebookProfile);

      for(OfficialPost activePost : OfficialPostsHolder.getInstance().getActivePostsForIterate()) {
         if (activePost.isActionTypeRewarded(type) && getCompletedTask(facebookIdCompletedTasks, activePost, type) == null) {
            return true;
         }
      }

      return false;
   }

   public OfficialPost getFatherAction(@Nullable FacebookProfile facebookTakingAction, FacebookActionType childActionType) {
      if (facebookTakingAction != null) {
         List<CompletedTask> facebookIdCompletedTasks = this.getCompletedTasksForIterate(facebookTakingAction);

         for(OfficialPost activePost : OfficialPostsHolder.getInstance().getActivePostsForIterate()) {
            if (activePost.isActionTypeRewarded(childActionType) && getCompletedTask(facebookIdCompletedTasks, activePost, childActionType) == null) {
               return activePost;
            }
         }

         return null;
      } else {
         return OfficialPostsHolder.getInstance().getActivePostsForIterate().isEmpty()
            ? null
            : OfficialPostsHolder.getInstance().getActivePostsForIterate().get(0);
      }
   }

   private static CompletedTask getCompletedTask(List<CompletedTask> tasks, OfficialPost father, FacebookActionType type) {
      for(CompletedTask task : tasks) {
         if (task.getActionType() == type && task.hasSameFather(father)) {
            return task;
         }
      }

      return null;
   }

   public CompletedTask getCompletedTask(FacebookProfile profile, @Nullable OfficialPost father, FacebookActionType type) {
      for(CompletedTask task : this.getCompletedTasksForIterate(profile)) {
         if (task.getActionType() == type && task.hasSameFather(father)) {
            return task;
         }
      }

      return null;
   }

   public CompletedTask getCompletedTask(
      FacebookIdentityType identityType, String identityValue, @Nullable OfficialPost father, FacebookActionType type, boolean skipSpacesInIdentity
   ) {
      if (identityType == FacebookIdentityType.NAME_IN_COMMENT) {
         return null;
      } else {
         for(Entry<FacebookProfile, ArrayList<CompletedTask>> tasksPerProfile : this._completedTasks.entrySet()) {
            if (checkIdentityMatches(tasksPerProfile.getKey(), identityType, identityValue, skipSpacesInIdentity)) {
               for(CompletedTask task : tasksPerProfile.getValue()) {
                  if (task.getActionType() == type && task.hasSameFather(father)) {
                     return task;
                  }
               }

               return null;
            }
         }

         return null;
      }
   }

   private static boolean checkIdentityMatches(FacebookProfile profile, FacebookIdentityType identityType, String identityValue, boolean skipSpacesInIdentity) {
      switch(identityType) {
         case NAME:
            if (skipSpacesInIdentity) {
               return profile.getName().replace(" ", "").equalsIgnoreCase(identityValue.replace(" ", ""));
            }

            return profile.getName().equalsIgnoreCase(identityValue);
         case ID:
            return profile.getId().equals(identityValue);
         case NONE:
         case NAME_IN_COMMENT:
            return false;
         default:
            return false;
      }
   }

   @Override
   public String toString() {
      return "CompletedTasksHistory{completedTasks=" + this._completedTasks + '}';
   }

   public static CompletedTasksHistory getInstance() {
      return CompletedTasksHistory.SingletonHolder.INSTANCE;
   }

   private static class SingletonHolder {
      private static final CompletedTasksHistory INSTANCE = new CompletedTasksHistory();
   }
}
