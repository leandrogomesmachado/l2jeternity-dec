package l2e.gameserver.model.entity.mods.facebook;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import l2e.gameserver.Config;
import l2e.gameserver.model.entity.mods.facebook.action.Comment;
import l2e.gameserver.model.entity.mods.facebook.action.Like;
import l2e.gameserver.model.entity.mods.facebook.action.Post;
import l2e.gameserver.model.strings.server.ServerMessage;

public enum FacebookActionType {
   LIKE("Facebook.Action.Like", Config.FACEBOOK_REWARD_LIKE_TASK, Config.FACEBOOK_REWARD_LIKE_NO_TASK, true, false),
   COMMENT("Facebook.Action.Comment", Config.FACEBOOK_REWARD_COMMENT_TASK, Config.FACEBOOK_REWARD_COMMENT_NO_TASK, true, true),
   POST("Facebook.Action.Post", null, null, false, false),
   SHARE("Facebook.Action.Share", null, null, true, false);

   private final String _actionNameAddress;
   private final Map<Integer, Long> _rewardTaskConfig;
   private final Map<Integer, Long> _rewardNoTaskConfig;
   private final boolean _haveFather;
   private final boolean _haveCommentMessage;

   private FacebookActionType(
      String actionNameAddress, Map<Integer, Long> rewardTaskConfig, Map<Integer, Long> rewardNoTaskConfig, boolean haveFather, boolean haveCommentMessage
   ) {
      this._actionNameAddress = actionNameAddress;
      this._rewardTaskConfig = rewardTaskConfig;
      this._rewardNoTaskConfig = rewardNoTaskConfig;
      this._haveFather = haveFather;
      this._haveCommentMessage = haveCommentMessage;
   }

   public String getActionName(String language) {
      return new ServerMessage(this._actionNameAddress, language).toString();
   }

   public boolean isRewarded() {
      return this._rewardTaskConfig != null && this._rewardNoTaskConfig != null;
   }

   public Map<Integer, Long> getRewardForTask() {
      return this._rewardTaskConfig;
   }

   public Map<Integer, Long> getRewardForNoTask() {
      return this._rewardNoTaskConfig;
   }

   public boolean haveFather() {
      return this._haveFather;
   }

   public boolean haveCommentMessage() {
      return this._haveCommentMessage;
   }

   public FacebookAction createInstance(ResultSet rset) throws SQLException {
      switch(this) {
         case LIKE:
            return new Like(rset);
         case COMMENT:
            return new Comment(rset);
         case POST:
            return new Post(rset);
         default:
            return null;
      }
   }
}
