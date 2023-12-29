package l2e.gameserver.model.entity.mods.facebook.action;

import java.sql.ResultSet;
import java.sql.SQLException;
import l2e.gameserver.data.holder.FacebookProfilesHolder;
import l2e.gameserver.data.holder.OfficialPostsHolder;
import l2e.gameserver.model.entity.mods.facebook.FacebookAction;
import l2e.gameserver.model.entity.mods.facebook.FacebookActionType;
import l2e.gameserver.model.entity.mods.facebook.template.FacebookProfile;

public class Comment implements FacebookAction {
   private final String _id;
   private final FacebookProfile _executor;
   private String _message;
   private final long _createdTime;
   private final long _extractionDate;
   private final FacebookAction _fatherAction;

   public Comment(String id, FacebookProfile executor, String message, long createdTime, long extractionDate, FacebookAction fatherAction) {
      this._id = id;
      this._executor = executor;
      this._message = message;
      this._createdTime = createdTime;
      this._extractionDate = extractionDate;
      this._fatherAction = fatherAction;
   }

   public Comment(ResultSet rset) throws SQLException {
      this._id = rset.getString("action_id");
      this._executor = FacebookProfilesHolder.getInstance().getProfileById(rset.getString("executor_id"));
      this._message = rset.getString("message");
      this._createdTime = rset.getLong("created_date");
      this._extractionDate = rset.getLong("extraction_date");
      this._fatherAction = OfficialPostsHolder.getInstance().getOfficialPost(rset.getString("father_id"));
   }

   @Override
   public String getId() {
      return this._id;
   }

   @Override
   public FacebookActionType getActionType() {
      return FacebookActionType.COMMENT;
   }

   @Override
   public FacebookProfile getExecutor() {
      return this._executor;
   }

   @Override
   public long getCreatedDate() {
      return this._createdTime;
   }

   @Override
   public long getExtractionDate() {
      return this._extractionDate;
   }

   @Override
   public String getMessage() {
      return this._message;
   }

   @Override
   public void changeMessage(String newMessage) {
      this._message = newMessage;
   }

   @Override
   public FacebookAction getFather() {
      return this._fatherAction;
   }

   @Override
   public boolean canBeRemoved() {
      return false;
   }

   @Override
   public void remove() {
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof Comment && this._id.equals(((Comment)obj)._id);
   }

   @Override
   public int hashCode() {
      return this._id.hashCode();
   }

   @Override
   public String toString() {
      return "Comment{id='"
         + this._id
         + '\''
         + ", executor="
         + this._executor
         + ", message='"
         + this._message
         + '\''
         + ", createdTime="
         + this._createdTime
         + ", extractionDate="
         + this._extractionDate
         + ", fatherAction="
         + this._fatherAction
         + '}';
   }
}
