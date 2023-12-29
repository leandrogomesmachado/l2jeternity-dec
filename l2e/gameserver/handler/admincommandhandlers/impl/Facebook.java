package l2e.gameserver.handler.admincommandhandlers.impl;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.data.dao.FacebookDAO;
import l2e.gameserver.data.holder.OfficialPostsHolder;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.handler.communityhandlers.impl.AbstractCommunity;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.mods.facebook.ActionsExtractingManager;
import l2e.gameserver.model.entity.mods.facebook.ActiveTasksHandler;
import l2e.gameserver.model.entity.mods.facebook.CompletedTasksHistory;
import l2e.gameserver.model.entity.mods.facebook.FacebookActionType;
import l2e.gameserver.model.entity.mods.facebook.OfficialPost;
import l2e.gameserver.model.entity.mods.facebook.template.ActiveTask;
import l2e.gameserver.model.entity.mods.facebook.template.CompletedTask;

public class Facebook implements IAdminCommandHandler {
   private static Logger _log = Logger.getLogger(Facebook.class.getName());
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_facebook",
      "admin_fb_set_message_approval",
      "admin_fb_official_posts",
      "admin_fb_official_post_edit_panel",
      "admin_fb_add_rewarded_action",
      "admin_fb_remove_rewarded_action",
      "admin_reset_facebook_delay",
      "admin_recheck_task_completed",
      "admin_has_fb_task",
      "admin_expire_fb_task",
      "admin_clear_negative_balance",
      "admin_reload_fb_posts"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (!Config.ALLOW_FACEBOOK_SYSTEM) {
         return false;
      } else {
         if (command.equalsIgnoreCase("admin_facebook")) {
            String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/admin/facebook/messagesToApprove.htm");
            StringBuilder sb = new StringBuilder();
            int index = 0;
            boolean nextColor = false;

            for(CompletedTask task : CompletedTasksHistory.getInstance().getTasksThatNeedsApproval()) {
               if (++index > 5) {
                  break;
               }

               sb.append("<table cellspacing=5></table>");
               sb.append("<table cellspacing=0 cellpadding=2 fixwidth=740 height=79 background=l2ui_ct1.Windows_DF_TooltipBG>");
               sb.append("<tr>");
               sb.append("<td>");
               sb.append("<table cellspacing=0 cellpadding=0 fixwidth=736 height=75 bgcolor=" + (nextColor ? "011118" : "00080b") + ">");
               sb.append("<tr>");
               sb.append("<td fixwidth=568>");
               sb.append("<table cellspacing=0 cellpadding=4 fixwidth=568>");
               sb.append("<tr>");
               sb.append("<td align=center width=60>");
               sb.append("<font color=bc2b0e>Message:</font>");
               sb.append("</td>");
               sb.append("<td fixwidth=508>");
               sb.append("<font color=ff8e3b>" + task.getMessage().replace("\n", "<br1>") + "</font");
               sb.append("</td>");
               sb.append("</tr>");
               sb.append("</table>");
               sb.append("</td>");
               sb.append("<td width=168>");
               sb.append("<br>");
               sb.append(
                  "<button value=\"Approve\" action=\"bypass -h admin_fb_set_message_approval "
                     + task.getId()
                     + " True\" width=120 height=25 back=\"cb.mx_button_down\" fore=\"cb.mx_button\" />"
               );
               sb.append("<br>");
               sb.append(
                  "<button value=\"NOT Approve\" action=\"bypass -h admin_fb_set_message_approval "
                     + task.getId()
                     + " False\" width=120 height=25 back=\"cb.mx_button_down\" fore=\"cb.mx_button\" />"
               );
               sb.append("</td>");
               sb.append("</tr>");
               sb.append("</table>");
               sb.append("</td>");
               sb.append("</tr>");
               sb.append("</table>");
               nextColor = !nextColor;
            }

            html = html.replace("%list%", sb.toString());
            AbstractCommunity.separateAndSend(html, activeChar);
         } else if (command.startsWith("admin_fb_set_message_approval")) {
            String actionId = null;
            boolean approved = true;
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.hasMoreTokens()) {
               actionId = st.nextToken();
            }

            if (st.hasMoreTokens()) {
               approved = Boolean.parseBoolean(st.nextToken());
            }

            if (actionId != null) {
               CompletedTask task = CompletedTasksHistory.getInstance().getCompletedTask(actionId);
               ActiveTasksHandler.manageMessageApproval(task, approved);
            }
         } else if (command.equalsIgnoreCase("admin_fb_official_posts")) {
            String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/admin/facebook/officialPosts.htm");
            StringBuilder sb = new StringBuilder();
            boolean nextColor = false;

            for(OfficialPost officialPost : OfficialPostsHolder.getInstance().getRecentOfficialPostsForIterate()) {
               sb.append("<table cellspacing=5></table>");
               sb.append("<table cellspacing=0 cellpadding=2 fixwidth=740 height=79>");
               sb.append("<tr>");
               sb.append("<td>");
               sb.append("<table cellspacing=0 cellpadding=0 fixwidth=736 height=75 bgcolor=" + (nextColor ? "011118" : "00080b") + ">");
               sb.append("<tr>");
               sb.append("<td fixwidth=568>");
               sb.append("<table cellspacing=0 cellpadding=4 fixwidth=636>");
               sb.append("<tr>");
               sb.append("<td align=center width=60>");
               sb.append("<font color=bc2b0e>");
               if (officialPost.getRewardedActionsForIterate().isEmpty()) {
                  sb.append("NOT Active");
               } else {
                  sb.append("Active");
               }

               sb.append("</font>");
               sb.append("</td>");
               sb.append("<td fixwidth=576>");
               sb.append("<font color=ff8e3b>" + officialPost.getMessage().replace("\n", "<br1>") + "</font>");
               sb.append("</td>");
               sb.append("</tr>");
               sb.append("</table>");
               sb.append("</td>");
               sb.append("<td width=100>");
               sb.append("<br><br><br>");
               sb.append(
                  "<button value=\"Setup\" action=\"bypass -h admin_fb_official_post_edit_panel "
                     + officialPost.getId()
                     + "\" width=120 height=25 back=\"cb.mx_button_down\" fore=\"cb.mx_button\" />"
               );
               sb.append("</td>");
               sb.append("</tr>");
               sb.append("</table>");
               sb.append("</td>");
               sb.append("</tr>");
               sb.append("</table>");
               nextColor = !nextColor;
            }

            html = html.replace("%list%", sb.toString());
            AbstractCommunity.separateAndSend(html, activeChar);
         } else if (command.startsWith("admin_fb_official_post_edit_panel")) {
            String postId = null;
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.hasMoreTokens()) {
               postId = st.nextToken();
            }

            if (postId != null) {
               String html = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/admin/facebook/editOfficialPost.htm");
               OfficialPost officialPost = OfficialPostsHolder.getInstance().getOfficialPost(postId);
               StringBuilder sb = new StringBuilder();

               for(FacebookActionType actionType : FacebookActionType.values()) {
                  if (actionType.isRewarded()) {
                     if (officialPost.isActionTypeRewarded(actionType)) {
                        sb.append(
                           "<button value=\""
                              + actionType.toString()
                              + "\" action=\"bypass -h admin_fb_remove_rewarded_action "
                              + officialPost.getId()
                              + " "
                              + actionType.toString()
                              + "\" width=120 height=25 back=\"cb.mx_button_down\" fore=\"cb.mx_button\" />"
                        );
                     } else {
                        sb.append(
                           "<button value=\""
                              + actionType.toString()
                              + "\" action=\"bypass -h admin_fb_add_rewarded_action "
                              + officialPost.getId()
                              + " "
                              + actionType.toString()
                              + "\" width=120 height=25 back=\"cb.mx_button_down\" fore=\"cb.mx_button\" />"
                        );
                     }
                  }
               }

               html = html.replace("%message%", officialPost.getMessage().replace("\n", "<br1>"));
               html = html.replace("%list%", sb.toString());
               AbstractCommunity.separateAndSend(html, activeChar);
            }
         } else if (command.startsWith("admin_fb_add_rewarded_action")) {
            String postId = null;
            String actionName = null;
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.hasMoreTokens()) {
               postId = st.nextToken();
            }

            if (st.hasMoreTokens()) {
               actionName = st.nextToken();
            }

            if (postId != null && actionName != null) {
               OfficialPostsHolder.getInstance().addNewRewardedAction(postId, FacebookActionType.valueOf(actionName));
               this.useAdminCommand("admin_fb_official_post_edit_panel " + postId, activeChar);
            }
         } else if (command.startsWith("admin_fb_remove_rewarded_action")) {
            String postId = null;
            String actionName = null;
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.hasMoreTokens()) {
               postId = st.nextToken();
            }

            if (st.hasMoreTokens()) {
               actionName = st.nextToken();
            }

            if (postId != null && actionName != null) {
               OfficialPostsHolder.getInstance().removeNewRewardedAction(postId, FacebookActionType.valueOf(actionName));
               this.useAdminCommand("admin_fb_official_post_edit_panel " + postId, activeChar);
            }
         } else if (command.startsWith("admin_reset_facebook_delay")) {
            String targetName = null;
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.hasMoreTokens()) {
               targetName = st.nextToken();
            }

            if (targetName != null) {
               Player target = World.getInstance().getPlayer(targetName);
               if (target == null || target.getFacebookProfile() == null) {
                  activeChar.sendMessage(target.toString() + " doesn't have Facebook Profile.");
                  return false;
               }

               target.getFacebookProfile().setLastCompletedTaskDate(-1L);
               FacebookDAO.replaceFacebookProfile(target.getFacebookProfile());
            }
         } else if (command.startsWith("admin_recheck_task_completed")) {
            String targetName = null;
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.hasMoreTokens()) {
               targetName = st.nextToken();
            }

            if (targetName != null) {
               Player target = World.getInstance().getPlayer(targetName);
               if (target == null) {
                  return false;
               }

               ActiveTask task = ActiveTasksHandler.getInstance().getActiveTaskByPlayer(target);
               if (task == null) {
                  activeChar.sendMessage(target.getName() + " has no active task!");
                  return false;
               }

               boolean completed = ActiveTasksHandler.getInstance().checkTaskCompleted(task);
               activeChar.sendMessage("Result: " + completed);
            }
         } else if (command.startsWith("admin_has_fb_task")) {
            String targetName = null;
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.hasMoreTokens()) {
               targetName = st.nextToken();
            }

            if (targetName != null) {
               Player target = World.getInstance().getPlayer(targetName);
               if (target == null) {
                  return false;
               }

               ActiveTask task = ActiveTasksHandler.getInstance().getActiveTaskByPlayer(target);
               if (task == null) {
                  activeChar.sendMessage(target.getName() + " has NO active task!");
               } else {
                  activeChar.sendMessage(target.getName() + " HAS active task!");
               }
            }
         } else if (command.startsWith("admin_expire_fb_task")) {
            String targetName = null;
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.hasMoreTokens()) {
               targetName = st.nextToken();
            }

            if (targetName != null) {
               Player target = World.getInstance().getPlayer(targetName);
               if (target == null) {
                  return false;
               }

               ActiveTask task = ActiveTasksHandler.getInstance().getActiveTaskByPlayer(target);
               if (task == null) {
                  activeChar.sendMessage(target.getName() + " has no active task!");
                  return false;
               }

               ActiveTasksHandler.getInstance().forceExpireTask(task);
               activeChar.sendMessage("Task has expired!");
            }
         } else if (command.startsWith("admin_clear_negative_balance")) {
            String targetName = null;
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.hasMoreTokens()) {
               targetName = st.nextToken();
            }

            if (targetName != null) {
               Player target = World.getInstance().getPlayer(targetName);
               if (target == null) {
                  return false;
               }

               if (target.getFacebookProfile() == null) {
                  activeChar.sendMessage(target.getName() + " has no Facebook Profile attached!");
                  return false;
               }

               if (target.getFacebookProfile().getNegativePointTypesForIterate().isEmpty()) {
                  activeChar.sendMessage(target.getName() + " doesnt have Negative Balance!");
                  return false;
               }

               target.getFacebookProfile().getNegativePointTypesForIterate().clear();
               FacebookDAO.replaceFacebookProfile(target.getFacebookProfile());
            }
         } else if (command.equalsIgnoreCase("admin_reload_fb_posts")) {
            OfficialPostsHolder.getInstance().getRecentOfficialPostsForIterate().clear();
            OfficialPostsHolder.getInstance().getActivePostsForIterate().clear();

            try {
               ActionsExtractingManager.getInstance().getExtractor("ExtractOfficialPosts").extractData(Config.FACEBOOK_TOKEN);
            } catch (IOException var12) {
               _log.log(java.util.logging.Level.WARNING, "Error while extracting Official Posts!", (Throwable)var12);
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
