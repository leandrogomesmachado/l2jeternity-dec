package l2e.gameserver.model.entity.mods.facebook.action;

import java.sql.ResultSet;
import java.sql.SQLException;
import l2e.gameserver.data.holder.FacebookProfilesHolder;
import l2e.gameserver.model.entity.mods.facebook.FacebookAction;
import l2e.gameserver.model.entity.mods.facebook.FacebookActionType;
import l2e.gameserver.model.entity.mods.facebook.template.FacebookProfile;

public class Post implements FacebookAction {
   private final String _id;
   private final FacebookProfile _executor;
   private String _message;
   private final long _createdTime;
   private final long _extractionDate;

   public Post(String id, FacebookProfile executor, String message, long createdTime, long extractionDate) {
      this._id = id;
      this._executor = executor;
      this._message = message;
      this._createdTime = createdTime;
      this._extractionDate = extractionDate;
   }

   public Post(ResultSet rset) throws SQLException {
      this._id = rset.getString("action_id");
      this._executor = FacebookProfilesHolder.getInstance().getProfileById(rset.getString("executor_id"));
      this._message = rset.getString("message");
      this._createdTime = rset.getLong("created_date");
      this._extractionDate = rset.getLong("extraction_date");
   }

   @Override
   public String getId() {
      return this._id;
   }

   @Override
   public FacebookActionType getActionType() {
      return FacebookActionType.POST;
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
      return null;
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
      return obj instanceof Post && this._id.equals(((Post)obj)._id);
   }

   @Override
   public int hashCode() {
      return this._id.hashCode();
   }

   @Override
   public String toString() {
      return "Post{id='"
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
         + '}';
   }
}
