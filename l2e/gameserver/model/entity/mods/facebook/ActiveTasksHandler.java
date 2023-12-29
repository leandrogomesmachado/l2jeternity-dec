package l2e.gameserver.model.entity.mods.facebook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Functions;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.dao.CharacterDAO;
import l2e.gameserver.data.dao.FacebookDAO;
import l2e.gameserver.data.holder.FacebookProfilesHolder;
import l2e.gameserver.data.parser.FacebookCommentsParser;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.mods.facebook.template.ActiveTask;
import l2e.gameserver.model.entity.mods.facebook.template.CompletedTask;
import l2e.gameserver.model.entity.mods.facebook.template.FacebookProfile;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.ShowBoard;

public final class ActiveTasksHandler {
   private final CopyOnWriteArrayList<ActiveTask> _activeTasks = new CopyOnWriteArrayList<>();
   protected final ScheduledFuture<?> _expiredTimeThread = ThreadPoolManager.getInstance()
      .scheduleAtFixedDelay(new ActiveTasksHandler.TimeExpiredThread(), (long)Config.FACEBOOK_TIME_LIMIT_DELAY, (long)Config.FACEBOOK_TIME_LIMIT_DELAY);

   private ActiveTasksHandler() {
   }

   public ActiveTask createActiveTask(Player player, FacebookIdentityType identityType, String identityValue, FacebookActionType actionType) throws TaskNoAvailableException {
      FacebookProfile profile = null;
      OfficialPost father = null;
      if (actionType.haveFather()) {
         if (identityType == FacebookIdentityType.ID) {
            profile = FacebookProfilesHolder.getInstance().getProfileById(identityValue);
            if (profile == null) {
               return null;
            }

            if (!profile.equals(player.getFacebookProfile())) {
               return null;
            }
         }

         father = CompletedTasksHistory.getInstance().getFatherAction(profile, actionType);
         if (father == null) {
            throw new TaskNoAvailableException("Couldn't find Father for " + identityType + ", " + identityValue + ", " + actionType + ", " + profile);
         }
      }

      String message = "";
      if (actionType.haveCommentMessage()) {
         message = FacebookCommentsParser.getInstance().getCommentToWrite(father, identityType, identityValue);
      }

      ActiveTask activeTask = new ActiveTask(player.getObjectId(), identityType, identityValue, actionType, father, message, System.currentTimeMillis());
      this.addNewActiveTask(activeTask);
      boolean completed = this.checkTaskCompleted(activeTask);
      return completed ? null : activeTask;
   }

   public boolean checkTaskCompleted(FacebookAction action) {
      for(ActiveTask task : this._activeTasks) {
         if (isWantedAction(task, action)) {
            boolean continueLooking = this.onWantedActionFound(task, action);
            if (!continueLooking) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean checkTaskCompleted(ActiveTask task) {
      for(FacebookAction action : ActionsAwaitingOwner.getInstance().getActionsForIterate(task.getFather(), task.getActionType())) {
         if (isWantedAction(task, action)) {
            boolean continueLooking = this.onWantedActionFound(task, action);
            if (!continueLooking) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean onWantedActionFound(ActiveTask task, FacebookAction action) {
      if (CompletedTasksHistory.getInstance().getCompletedTask(action.getExecutor(), task.getFather(), task.getActionType()) != null) {
         this.abortTask(task, action, ActiveTasksHandler.AbortTaskReason.PROFILE_DONE_SIMILAR_TASK);
         return false;
      } else {
         FacebookCommentsParser.CommentMatchType commentMatchType = FacebookCommentsParser.getInstance().checkCommentMatches(task, action);
         if (commentMatchType == FacebookCommentsParser.CommentMatchType.FULL_MATCH) {
            this.onTaskCompleted(task, action, true);
            return false;
         } else if (commentMatchType != FacebookCommentsParser.CommentMatchType.COMMENT_NOT_MATCHES
            || task.getIdentityType() != FacebookIdentityType.ID && Config.ALLOW_REG_EXACT_COMMENTS) {
            return true;
         } else {
            this.onTaskCompleted(task, action, false);
            return false;
         }
      }
   }

   private void onTaskCompleted(ActiveTask finishedTask, FacebookAction action, boolean commentApproved) {
      boolean hadNegativePoints = action.getExecutor().hasNegativePoints();
      if (commentApproved) {
         if (hadNegativePoints) {
            action.getExecutor().removeNegativePoint(action.getActionType(), true);
            sendMailNegativeRemoved(finishedTask.getPlayerId(), action);
         } else {
            action.getExecutor().addPositivePoint(action.getActionType(), true);
            sendReward(finishedTask.getPlayerId(), finishedTask.getTakenDate(), action);
         }
      } else {
         sendMailMsgToVerification(finishedTask);
         notifyGMsMsgsToVerify(true);
      }

      if (finishedTask.getIdentityType() != FacebookIdentityType.ID) {
         attachFacebookId(finishedTask.getPlayerId(), action.getExecutor());
      }

      ActionsAwaitingOwner.getInstance().removeAction(action);
      CompletedTask.CommentApprovalType approvalType = commentApproved
         ? CompletedTask.CommentApprovalType.APPROVED
         : CompletedTask.CommentApprovalType.NOT_YET_CHECKED;
      CompletedTasksHistory.getInstance().addCompletedTask(finishedTask, action, approvalType, commentApproved, !hadNegativePoints, true);
      this._activeTasks.remove(finishedTask);
   }

   public void abortTask(ActiveTask task, FacebookAction action, ActiveTasksHandler.AbortTaskReason reason) {
      switch(reason) {
         case PROFILE_DONE_SIMILAR_TASK:
            sendMailSimilarTaskDoneAleady(task.getPlayerId());
         default:
            this._activeTasks.remove(task);
      }
   }

   public static void manageMessageApproval(CompletedTask task, boolean approved) {
      task.setCommentApproved(approved ? CompletedTask.CommentApprovalType.APPROVED : CompletedTask.CommentApprovalType.NOT_APPROVED);
      FacebookDAO.replaceCompletedTask(task);
      if (task.isRewarded()) {
         if (!approved) {
            task.setRewarded(false);
            task.getExecutor().addNegativePoint(task.getActionType(), true);
         }
      } else if (approved) {
         task.setRewarded(true);
         FacebookDAO.replaceCompletedTask(task);
         boolean hadNegativePoints = task.getExecutor().hasNegativePoints();
         if (hadNegativePoints) {
            task.getExecutor().removeNegativePoint(task.getActionType(), true);
            sendMailNegativeRemoved(task.getPlayerId(), task);
         } else {
            task.getExecutor().addPositivePoint(task.getActionType(), true);
            sendReward(task.getPlayerId(), task.getTakenDate(), task);
         }
      } else {
         sendMailMsgNotApproved(task.getPlayerId());
      }
   }

   public static void notifyGMsMsgsToVerify(boolean incrementNotApprovedCountByOne) {
      int notApprovedMsgs = CompletedTasksHistory.getInstance().countTasksThatNeedsApproval();
      if (incrementNotApprovedCountByOne) {
         ++notApprovedMsgs;
      }

      ServerMessage msgEn;
      ServerMessage msgRu;
      if (notApprovedMsgs == 1) {
         msgEn = new ServerMessage("Facebook.GMNotification.MsgToVerify.One", "en");
         msgRu = new ServerMessage("Facebook.GMNotification.MsgToVerify.One", "ru");
      } else {
         msgEn = new ServerMessage("Facebook.GMNotification.MsgToVerify.Multiple", "en");
         msgEn.add(notApprovedMsgs);
         msgRu = new ServerMessage("Facebook.GMNotification.MsgToVerify.Multiple", "ru");
         msgRu.add(notApprovedMsgs);
      }

      CreatureSay gmNotificationmsgEn = new CreatureSay(0, 15, "Facebook", msgEn.toString());
      CreatureSay gmNotificationmsgRu = new CreatureSay(0, 15, "Facebook", msgRu.toString());

      for(Player gm : World.getInstance().getAllGMs()) {
         if (gm != null) {
            gm.sendPacket(gm.getLang().equalsIgnoreCase("ru") ? gmNotificationmsgRu : gmNotificationmsgEn);
         }
      }
   }

   private static void sendReward(int playerId, long taskTakenDate, FacebookAction action) {
      long closestDateToCreation;
      if (action.getCreatedDate() > 0L) {
         closestDateToCreation = action.getCreatedDate();
      } else {
         closestDateToCreation = action.getExtractionDate();
      }

      Map<Integer, Long> reward;
      String messageBodyAddress;
      if (closestDateToCreation < taskTakenDate) {
         reward = action.getActionType().getRewardForNoTask();
         messageBodyAddress = "Facebook.Reward.Body.NoTask";
      } else {
         reward = action.getActionType().getRewardForTask();
         messageBodyAddress = "Facebook.Reward.Body.Task";
      }

      Player onlinePlayer = World.getInstance().getPlayer(playerId);
      if (onlinePlayer == null) {
         ServerMessage messageTitle = new ServerMessage("Facebook.Reward.Title", Config.MULTILANG_DEFAULT);
         ServerMessage messageBody = new ServerMessage(messageBodyAddress, Config.MULTILANG_DEFAULT);
         Functions.sendSystemMail(playerId, messageTitle.toString(), messageBody.toString(), reward);
      } else {
         ServerMessage messageTitle = new ServerMessage("Facebook.Reward.Title", onlinePlayer.getLang());
         ServerMessage messageBody = new ServerMessage(messageBodyAddress, onlinePlayer.getLang());
         Functions.sendSystemMail(onlinePlayer, messageTitle.toString(), messageBody.toString(), reward);
         onlinePlayer.sendPacket(new ShowBoard());
      }
   }

   private static void sendMailNegativeRemoved(int playerId, FacebookAction action) {
      String msgTitle;
      String msgBody;
      if (action.getExecutor().hasNegativePoints()) {
         msgTitle = "Facebook.NegativeRemoved.StillNegativeBalance.Title";
         msgBody = "Facebook.NegativeRemoved.StillNegativeBalance.Body";
      } else {
         msgTitle = "Facebook.NegativeRemoved.ClearAccount.Title";
         msgBody = "Facebook.NegativeRemoved.ClearAccount.Body";
      }

      Player onlinePlayer = World.getInstance().getPlayer(playerId);
      if (onlinePlayer == null) {
         ServerMessage messageTitle = new ServerMessage(msgTitle, Config.MULTILANG_DEFAULT);
         ServerMessage messageBody = new ServerMessage(msgBody, Config.MULTILANG_DEFAULT);
         Functions.sendSystemMail(playerId, messageTitle.toString(), messageBody.toString(), Collections.emptyMap());
      } else {
         ServerMessage messageTitle = new ServerMessage(msgTitle, onlinePlayer.getLang());
         ServerMessage messageBody = new ServerMessage(msgBody, onlinePlayer.getLang());
         Functions.sendSystemMail(onlinePlayer, messageTitle.toString(), messageBody.toString(), Collections.emptyMap());
         onlinePlayer.sendPacket(new ShowBoard());
      }
   }

   private static void sendMailMsgToVerification(ActiveTask finishedTask) {
      Player onlinePlayer = World.getInstance().getPlayer(finishedTask.getPlayerId());
      if (onlinePlayer == null) {
         ServerMessage messageTitle = new ServerMessage("Facebook.MsgToVerification.Title", Config.MULTILANG_DEFAULT);
         ServerMessage messageBody = new ServerMessage("Facebook.MsgToVerification.Body", Config.MULTILANG_DEFAULT);
         Functions.sendSystemMail(finishedTask.getPlayerId(), messageTitle.toString(), messageBody.toString(), Collections.emptyMap());
      } else {
         ServerMessage messageTitle = new ServerMessage("Facebook.MsgToVerification.Title", onlinePlayer.getLang());
         ServerMessage messageBody = new ServerMessage("Facebook.MsgToVerification.Body", onlinePlayer.getLang());
         Functions.sendSystemMail(onlinePlayer, messageTitle.toString(), messageBody.toString(), Collections.emptyMap());
         onlinePlayer.sendPacket(new ShowBoard());
      }
   }

   private static void sendMailMsgNotApproved(int playerId) {
      Player onlinePlayer = World.getInstance().getPlayer(playerId);
      if (onlinePlayer == null) {
         ServerMessage messageTitle = new ServerMessage("Facebook.MsgNotApproved.Title", Config.MULTILANG_DEFAULT);
         ServerMessage messageBody = new ServerMessage("Facebook.MsgNotApproved.Body", Config.MULTILANG_DEFAULT);
         Functions.sendSystemMail(playerId, messageTitle.toString(), messageBody.toString(), Collections.emptyMap());
      } else {
         ServerMessage messageTitle = new ServerMessage("Facebook.MsgNotApproved.Title", onlinePlayer.getLang());
         ServerMessage messageBody = new ServerMessage("Facebook.MsgNotApproved.Body", onlinePlayer.getLang());
         Functions.sendSystemMail(onlinePlayer, messageTitle.toString(), messageBody.toString(), Collections.emptyMap());
         onlinePlayer.sendPacket(new ShowBoard());
      }
   }

   private static void sendMailSimilarTaskDoneAleady(int playerId) {
      Player onlinePlayer = World.getInstance().getPlayer(playerId);
      if (onlinePlayer == null) {
         ServerMessage messageTitle = new ServerMessage("Facebook.SimilarTaskDoneAlready.Title", Config.MULTILANG_DEFAULT);
         ServerMessage messageBody = new ServerMessage("Facebook.SimilarTaskDoneAlready.Body", Config.MULTILANG_DEFAULT);
         Functions.sendSystemMail(playerId, messageTitle.toString(), messageBody.toString(), Collections.emptyMap());
      } else {
         ServerMessage messageTitle = new ServerMessage("Facebook.SimilarTaskDoneAlready.Title", onlinePlayer.getLang());
         ServerMessage messageBody = new ServerMessage("Facebook.SimilarTaskDoneAlready.Body", onlinePlayer.getLang());
         Functions.sendSystemMail(onlinePlayer, messageTitle.toString(), messageBody.toString(), Collections.emptyMap());
         onlinePlayer.sendPacket(new ShowBoard());
      }
   }

   private static void attachFacebookId(int playerId, FacebookProfile profile) {
      Player player = World.getInstance().getPlayer(playerId);
      if (player == null) {
         CharacterDAO.getInstance().setFacebookId(playerId, profile.getId());
      } else {
         player.setFacebookProfile(profile);
      }
   }

   public void addNewActiveTask(ActiveTask task) {
      this._activeTasks.add(task);
   }

   public List<ActiveTask> getActiveTasksForIterate() {
      return this._activeTasks;
   }

   public List<ActiveTask> getActiveTasksCopy(boolean concurrent) {
      return (List<ActiveTask>)(concurrent ? new ArrayList<>(this._activeTasks) : new CopyOnWriteArrayList<>(this._activeTasks));
   }

   public ActiveTask getActiveTaskByPlayer(Player player) {
      for(ActiveTask task : this._activeTasks) {
         if (task.getPlayerId() == player.getObjectId()) {
            return task;
         }
      }

      return null;
   }

   public ActiveTask getActiveTaskByFacebookId(String facebookId) {
      for(ActiveTask task : this._activeTasks) {
         if (task.getIdentityType() == FacebookIdentityType.ID && task.getIdentityValue().equals(facebookId)) {
            return task;
         }
      }

      return null;
   }

   public ActiveTask getActiveTask(FacebookIdentityType identityType, String identityValue) {
      if (identityType == FacebookIdentityType.NAME_IN_COMMENT) {
         return null;
      } else {
         String identityToCompare = identityValue;
         if (identityType == FacebookIdentityType.NAME) {
            identityToCompare = identityValue.toLowerCase().replace(" ", "");
         }

         for(ActiveTask task : this._activeTasks) {
            if (task.getIdentityType() == FacebookIdentityType.ID) {
               if (task.getIdentityValue().equals(identityToCompare)) {
                  return task;
               }
            } else if (task.getIdentityType() == FacebookIdentityType.NAME && task.getIdentityValue().toLowerCase().replace(" ", "").equals(identityToCompare)
               )
             {
               return task;
            }
         }

         return null;
      }
   }

   private static boolean isWantedAction(ActiveTask active, FacebookAction action) {
      if (active.getActionType() != action.getActionType()) {
         return false;
      } else {
         switch(active.getIdentityType()) {
            case NAME:
               if (!compareFacebookNames(active.getIdentityValue(), action.getExecutor().getName())) {
                  return false;
               }
               break;
            case ID:
               if (!active.getIdentityValue().equals(action.getExecutor().getId())) {
                  return false;
               }
         }

         return (active.getFather() != null || action.getFather() == null) && (active.getFather() == null || active.getFather().equals(action.getFather()));
      }
   }

   private static boolean compareFacebookNames(String nameInAction, String nameWroteByPlayer) {
      return nameInAction.replace(" ", "").equalsIgnoreCase(nameWroteByPlayer.replace(" ", ""));
   }

   public void forceExpireTask(ActiveTask task) {
      this.onTaskExpired(task);
   }

   private void onTaskExpired(ActiveTask task) {
      ServerMessage title = new ServerMessage("Facebook.TaskExpired.Title", Config.MULTILANG_DEFAULT);
      ServerMessage body = new ServerMessage("Facebook.TaskExpired.Body", Config.MULTILANG_DEFAULT);
      Functions.sendSystemMail(task.getPlayerId(), title.toString(), body.toString(), Collections.emptyMap());
      this._activeTasks.remove(task);
   }

   private void checkForExpiredTasks() {
      long currentDate = System.currentTimeMillis();

      for(ActiveTask task : this._activeTasks) {
         if (task.getTimeLimitDate() < currentDate) {
            this.onTaskExpired(task);
         }
      }
   }

   @Override
   public String toString() {
      return "ActiveTasksHandler{activeTasks=" + this._activeTasks + '}';
   }

   public static ActiveTasksHandler getInstance() {
      return ActiveTasksHandler.SingletonHolder.INSTANCE;
   }

   private static enum AbortTaskReason {
      PROFILE_DONE_SIMILAR_TASK;
   }

   private static class SingletonHolder {
      private static final ActiveTasksHandler INSTANCE = new ActiveTasksHandler();
   }

   private static class TimeExpiredThread extends RunnableImpl {
      private TimeExpiredThread() {
      }

      @Override
      public void runImpl() {
         ActiveTasksHandler.getInstance().checkForExpiredTasks();
      }
   }
}
