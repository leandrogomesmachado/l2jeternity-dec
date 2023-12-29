package l2e.gameserver.handler.communityhandlers.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.FacebookProfilesHolder;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.base.SubClass;
import l2e.gameserver.model.entity.mods.facebook.ActiveTasksHandler;
import l2e.gameserver.model.entity.mods.facebook.CompletedTasksHistory;
import l2e.gameserver.model.entity.mods.facebook.FacebookActionType;
import l2e.gameserver.model.entity.mods.facebook.FacebookIdentityType;
import l2e.gameserver.model.entity.mods.facebook.TaskNoAvailableException;
import l2e.gameserver.model.entity.mods.facebook.template.ActiveTask;
import l2e.gameserver.model.entity.mods.facebook.template.FacebookProfile;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.ShowBoard;

public class CommunityFacebook extends AbstractCommunity implements ICommunityBoardHandler {
   private static final SimpleDateFormat NEXT_CHALLENGE_FORMAT = new SimpleDateFormat("HH:mm dd/MM/yyyy");
   private static final SimpleDateFormat CURRENT_TIME_FORMAT = new SimpleDateFormat("HH:mm");
   private static CommunityFacebook _instance = new CommunityFacebook();

   public CommunityFacebook() {
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all functions.");
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{"_bbsfacebook"};
   }

   private void useFacebookBypass(Player player, String bypass, Object... params) {
      this.onBypassCommand("_bbsfacebook_" + bypass + (params.length > 0 ? "_" : "") + Util.joinArrayWithCharacter(params, "_"), player);
   }

   @Override
   public void onBypassCommand(String bypass, Player player) {
      if (Config.ALLOW_FACEBOOK_SYSTEM) {
         ActiveTask task = ActiveTasksHandler.getInstance().getActiveTaskByPlayer(player);
         if (bypass.startsWith("_bbsfacebook")) {
            StringTokenizer st = new StringTokenizer(bypass, "_");
            st.nextToken();
            if (st.hasMoreTokens()) {
               String var5 = st.nextToken();
               switch(var5) {
                  case "main":
                     if (task == null) {
                        if (player.hasFacebookProfile()) {
                           if (player.getFacebookProfile().hasNegativePoints()) {
                              if (CompletedTasksHistory.getInstance().getAvailableNegativeBalanceTypes(player.getFacebookProfile()).isEmpty()) {
                                 this.useFacebookBypass(player, "noTasksToTake");
                              } else {
                                 this.useFacebookBypass(player, "tasksList");
                              }
                           } else if (player.getFacebookProfile().hasTaskDelay()) {
                              this.useFacebookBypass(player, "taskDelay");
                           } else if (CompletedTasksHistory.getInstance().getAvailableActionTypes(player.getFacebookProfile()).isEmpty()) {
                              this.useFacebookBypass(player, "noTasksToTake");
                           } else {
                              this.useFacebookBypass(player, "tasksList");
                           }
                        } else {
                           this.useFacebookBypass(player, "confirmIdentityStartInfo");
                        }
                     } else {
                        this.useFacebookBypass(player, "activeTaskDetails");
                     }
                     break;
                  case "confirmIdentityStartInfo":
                     this.showConfirmIndentityStartInfoPage(player);
                     break;
                  case "confirmIdentityNonEnglishChars":
                     this.startNewTask(player, FacebookActionType.COMMENT, FacebookIdentityType.NAME_IN_COMMENT, player.getName());
                     break;
                  case "confirmIdentityEnglishChars":
                     String facebookName = st.nextToken().trim();
                     this.startNewTask(player, FacebookActionType.COMMENT, FacebookIdentityType.NAME, facebookName);
                     break;
                  case "tasksList":
                     this.showTaskListPage(player);
                     break;
                  case "startTask":
                     if (player.getFacebookProfile() == null) {
                        this.useFacebookBypass(player, "main");
                        return;
                     }

                     String action = st.nextToken();
                     FacebookActionType actionType = FacebookActionType.valueOf(action);
                     this.startNewTask(player, actionType, FacebookIdentityType.ID, player.getFacebookProfile().getId());
                     break;
                  case "taskDelay":
                     this.showTaskDelayPage(player);
                     break;
                  case "activeTaskDetails":
                     this.showActiveTaskDetailsPage(player);
                     break;
                  case "noTasksToTake":
                     String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/facebookRewards/noTasksToTake.htm");
                     separateAndSend(html, player);
               }
            }
         }
      }
   }

   private void showConfirmIndentityStartInfoPage(Player player) {
      String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/facebookRewards/confirmIdentityStartInfo.htm");
      StringBuilder rewards = new StringBuilder();

      for(Entry<Integer, Long> rewardEntry : FacebookActionType.COMMENT.getRewardForTask().entrySet()) {
         Item itemTemplate = ItemsParser.getInstance().getTemplate(rewardEntry.getKey());
         if (itemTemplate != null) {
            rewards.append("<td width=40>");
            rewards.append("<table cellspacing=0 cellpadding=0 width=32 height=32 background=" + itemTemplate.getIcon() + ">");
            rewards.append("<tr>");
            rewards.append("<td width=32 height=32 align=center valign=top>");
            rewards.append("</td>");
            rewards.append("</tr>");
            rewards.append("</table>");
            rewards.append("</td>");
            rewards.append("<td width=120>");
            rewards.append("<font color=ff8e3b>" + rewardEntry.getValue() + "x " + player.getItemName(itemTemplate) + "</font>");
            rewards.append("</td>");
         }
      }

      html = html.replace("%rewards%", rewards.toString());
      separateAndSend(html, player);
   }

   private void showTaskListPage(Player player) {
      String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/facebookRewards/tasksList.htm");
      if (player.getFacebookProfile().hasNegativePoints()) {
         html = html.replace("%balance%", "Clear Negative Balance");
      } else {
         html = html.replace("%balance%", "Choose your new Task");
      }

      StringBuilder actions = new StringBuilder();
      boolean nextColor = false;
      if (player.getFacebookProfile().hasNegativePoints()) {
         actions.append("<table cellspacing=8></table>");
         actions.append("<table cellspacing=0 cellpadding=2 width=740 height=69 background=l2ui_ct1.Windows_DF_TooltipBG>");
         actions.append("<tr>");
         actions.append("<td>");
         actions.append("<table cellspacing=0 cellpadding=0 width=736 height=65 bgcolor=011118>");
         actions.append("<tr>");
         actions.append("<td align=center>");
         actions.append("<font color=bc2b0e name=hs12>");
         actions.append("Negative Points!");
         actions.append("</font>");
         actions.append("<br>");
         actions.append("<font color=ff8e3b>");
         actions.append("It looks like you have negative points for deleting post/like/comment/share that was rewarded.<br1>");
         actions.append("You need to complete similar tasks before being able to get the rewards again.");
         actions.append("</font>");
         actions.append("</td>");
         actions.append("</tr>");
         actions.append("</table>");
         actions.append("</td>");
         actions.append("</tr>");
         actions.append("</table>");

         for(FacebookActionType negativePointsType : CompletedTasksHistory.getInstance().getAvailableNegativeBalanceTypes(player.getFacebookProfile())) {
            this.makeActionTypeTable(player, actions, negativePointsType, nextColor, false);
            nextColor = !nextColor;
         }
      } else {
         for(FacebookActionType availableActionType : CompletedTasksHistory.getInstance().getAvailableActionTypes(player.getFacebookProfile())) {
            this.makeActionTypeTable(player, actions, availableActionType, nextColor, true);
            nextColor = !nextColor;
         }
      }

      html = html.replace("%actions%", actions.toString());
      separateAndSend(html, player);
   }

   private void makeActionTypeTable(Player player, StringBuilder sb, FacebookActionType actionType, boolean nextColor, boolean rewarded) {
      sb.append("<table cellspacing=8></table>");
      sb.append("<table cellspacing=0 cellpadding=2 width=740 height=79 background=l2ui_ct1.Windows_DF_TooltipBG>");
      sb.append("<tr>");
      sb.append("<td>");
      sb.append("<table cellspacing=0 cellpadding=0 width=736 height=75 bgcolor=" + (nextColor ? "011118" : "00080b") + ">");
      sb.append("<tr>");
      sb.append("<td align=center>");
      sb.append("<font color=bc2b0e name=hs12>");
      switch(actionType) {
         case LIKE:
            sb.append("Like our post on Facebook");
            break;
         case POST:
            sb.append("Post comment on our Facebook Wall");
            break;
         case COMMENT:
            sb.append("Write comment under one of our posts");
            break;
         case SHARE:
            sb.append("Share one of our posts");
      }

      sb.append("</font>");
      sb.append("<br>");
      sb.append("<table cellspacing=0 cellpadding=0 width=736>");
      sb.append("<tr>");
      sb.append("<td width=568>");
      if (rewarded) {
         sb.append("<table cellspacing=0 cellpadding=0>");
         sb.append("<tr>");
         sb.append("<td width=70 align=center>");
         sb.append("<font color=bc2b0e name=hs12>Reward:</font>");
         sb.append("</td>");

         for(Entry<Integer, Long> rewardEntry : actionType.getRewardForTask().entrySet()) {
            Item itemTemplate = ItemsParser.getInstance().getTemplate(rewardEntry.getKey());
            sb.append("<td width=40>");
            sb.append("<table cellspacing=0 cellpadding=0 width=32 height=32 background=" + itemTemplate.getIcon() + ">");
            sb.append("<tr>");
            sb.append("<td width=32 height=32 align=center valign=top>");
            sb.append("<img src=Btns.nice_frame width=32 height=32>");
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("</table>");
            sb.append("</td>");
            sb.append("<td width=120>");
            sb.append("<font color=ff8e3b>" + rewardEntry.getValue() + "x " + player.getItemName(itemTemplate) + "</font>");
            sb.append("</td>");
         }

         sb.append("</tr>");
         sb.append("</table>");
         sb.append("<#else>");
         sb.append("<br>");
      }

      sb.append("</td>");
      sb.append("<td width=168>");
      sb.append("<br>");
      sb.append(
         "<button value=\"Challenge Accepted\" action=\"bypass _bbsfacebook_startTask_"
            + actionType.toString()
            + "\" width=150 height=22 back=Btns.btn_simple_red_150x22_down fore=Btns.btn_simple_red_150x22 />"
      );
      sb.append("</td>");
      sb.append("</tr>");
      sb.append("</table>");
      sb.append("</td>");
      sb.append("</tr>");
      sb.append("</table>");
      sb.append("</td>");
      sb.append("</tr>");
      sb.append("</table>");
   }

   private void showTaskDelayPage(Player player) {
      String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/facebookRewards/taskDelay.htm");
      html = html.replace("%nextChallenge%", NEXT_CHALLENGE_FORMAT.format(Long.valueOf(player.getFacebookProfile().getDelayEndDate())));
      html = html.replace("%currentTime%", CURRENT_TIME_FORMAT.format(Long.valueOf(Calendar.getInstance().getTimeInMillis())));
      separateAndSend(html, player);
   }

   private void showActiveTaskDetailsPage(Player player) {
      ActiveTask activeTask = ActiveTasksHandler.getInstance().getActiveTaskByPlayer(player);
      String html = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/facebookRewards/activeTaskDetails.htm");
      switch(activeTask.getActionType()) {
         case LIKE:
            html = html.replace("%actionComment%", "Like our post on Facebook");
            break;
         case POST:
            html = html.replace("%actionComment%", "Post comment on our Facebook Wall");
            break;
         case COMMENT:
            html = html.replace("%actionComment%", "Write comment under one of our posts");
            break;
         case SHARE:
            html = html.replace("%actionComment%", "Share one of our posts");
      }

      StringBuilder messages = new StringBuilder();
      if (activeTask.getActionType().haveCommentMessage()) {
         messages.append("<tr>");
         messages.append("<td align=center height=50>");
         messages.append(
            "<font color=bc2b0e name=hs12>Comment to write "
               + (
                  activeTask.getIdentityType() != FacebookIdentityType.ID && Config.ALLOW_REG_EXACT_COMMENTS
                     ? "<font color=ff0000 name=hs12>(COMMENT MUST BE 100% THE SAME!!!)</font>"
                     : "different comment will have to be approved"
               )
               + ":"
         );
         messages.append("</font><br1>");
         messages.append("<font color=ff8e3b>");
         messages.append(activeTask.getRequestedMessage());
         messages.append("</font>");
         messages.append("</td>");
         messages.append("</tr>");
         messages.append("<tr>");
         messages.append("<td align=center height=100>");
         messages.append("<font color=bc2b0e name=hs12>Under official post:</font><br1>");
         messages.append("<font color=ff8e3b>");
         messages.append(activeTask.getFather().getMessage().replace("\n", "<br1>"));
         messages.append("</font>");
         messages.append("</td>");
         messages.append("</tr>");
      } else {
         messages.append("<tr>");
         messages.append("<td align=center height=150>");
         messages.append("<font color=bc2b0e name=hs12>Post Message:</font><br1>");
         messages.append("<font color=ff8e3b>");
         messages.append(activeTask.getFather().getMessage().replace("\n", "<br1>"));
         messages.append("</font>");
         messages.append("</td>");
         messages.append("</tr>");
      }

      StringBuilder rewards = new StringBuilder();
      if (player.getFacebookProfile() != null && player.getFacebookProfile().hasNegativePoints()) {
         rewards.append("<tr>");
         rewards.append("<td align=center height=40>");
         rewards.append("<font color=ff8e3b>");
         rewards.append("Task is not rewarded because You have Negative Points.");
         rewards.append("</font>");
         rewards.append("</td>");
         rewards.append("</tr>");
      } else {
         rewards.append("<tr>");
         rewards.append("<td align=center height=40>");
         rewards.append("<table cellspacing=0 cellpadding=0>");
         rewards.append("<tr>");

         for(Entry<Integer, Long> rewardEntry : activeTask.getActionType().getRewardForTask().entrySet()) {
            Item itemTemplate = ItemsParser.getInstance().getTemplate(rewardEntry.getKey());
            rewards.append("<td width=40>");
            rewards.append("<table cellspacing=0 cellpadding=0 width=32 height=32 background=" + itemTemplate.getIcon() + ">");
            rewards.append("<tr>");
            rewards.append("<td width=32 height=32 align=center valign=top>");
            rewards.append("<img src=Btns.nice_frame width=32 height=32>");
            rewards.append("</td>");
            rewards.append("</tr>");
            rewards.append("</table>");
            rewards.append("</td>");
            rewards.append("<td width=120>");
            rewards.append("<font color=ff8e3b>" + rewardEntry.getValue() + "x " + player.getItemName(itemTemplate) + "</font>");
            rewards.append("</td>");
         }

         rewards.append("</tr>");
         rewards.append("</table>");
         rewards.append("</td>");
         rewards.append("</tr>");
      }

      html = html.replace("%messages%", messages.toString());
      html = html.replace("%rewards%", rewards.toString());
      html = html.replace("%timeLimit%", CURRENT_TIME_FORMAT.format(Long.valueOf(activeTask.getTimeLimitDate())));
      html = html.replace("%currentTime%", CURRENT_TIME_FORMAT.format(Long.valueOf(Calendar.getInstance().getTimeInMillis())));
      html = html.replace("%linkToAction%", activeTask.getLinkToAction());
      separateAndSend(html, player);
   }

   private void startNewTask(Player player, FacebookActionType actionType, FacebookIdentityType identityType, String identityValue) {
      if (this.getBiggestLevel(player) < Config.FACEBOOK_MIN_LVL) {
         ServerMessage msg = new ServerMessage("Facebook.StartNewTask.Fail.TooLowLevel", player.getLang());
         msg.add(Config.FACEBOOK_MIN_LVL);
         sendErrorMessage(player, msg.toString(), true);
      } else {
         FacebookProfile profile = null;
         if (identityType == FacebookIdentityType.ID) {
            profile = player.getFacebookProfile();
         }

         if (identityType == FacebookIdentityType.NAME) {
            profile = FacebookProfilesHolder.getInstance().getProfileByName(identityValue, true, true);
         }

         if (profile != null && !profile.hasNegativePoints()) {
            if (!CompletedTasksHistory.getInstance().isActionTypeAvailable(profile, actionType)) {
               sendErrorMessage(player, new ServerMessage("Facebook.StartNewTask.Fail.TaskNotAvailable", player.getLang()).toString(), true);
               return;
            }

            if (profile.hasTaskDelay()) {
               sendErrorMessage(player, new ServerMessage("Facebook.StartNewTask.Fail.TaskNotAvailable", player.getLang()).toString(), true);
               return;
            }
         }

         if (ActiveTasksHandler.getInstance().getActiveTask(identityType, identityValue) != null) {
            sendErrorMessage(player, new ServerMessage("Facebook.StartNewTask.Fail.TaskAlreadyActive", player.getLang()).toString(), true);
         } else {
            try {
               ActiveTask taskToComplete = ActiveTasksHandler.getInstance().createActiveTask(player, identityType, identityValue, actionType);
               if (taskToComplete == null) {
                  player.sendPacket(new ShowBoard());
               } else {
                  this.useFacebookBypass(player, "activeTaskDetails");
               }
            } catch (TaskNoAvailableException var7) {
               this.useFacebookBypass(player, "noTasksToTake");
            }
         }
      }
   }

   private int getBiggestLevel(Player player) {
      int biggest = Integer.MIN_VALUE;

      for(SubClass sub : player.getSubClasses().values()) {
         if (sub.getLevel() > biggest) {
            biggest = sub.getLevel();
         }
      }

      return biggest;
   }

   protected static void sendErrorMessage(Player player, String msg, boolean closeBoard) {
      player.sendPacket(new CreatureSay(player.getObjectId(), 15, ServerStorage.getInstance().getString(player.getLang(), "CommunityBuffer.ERROR"), msg));
      if (closeBoard) {
         player.sendPacket(new ShowBoard());
      }
   }

   @Override
   public void onWriteCommand(String command, String ar1, String ar2, String ar3, String ar4, String ar5, Player activeChar) {
   }

   public static CommunityFacebook getInstance() {
      if (_instance == null) {
         _instance = new CommunityFacebook();
      }

      return _instance;
   }
}
