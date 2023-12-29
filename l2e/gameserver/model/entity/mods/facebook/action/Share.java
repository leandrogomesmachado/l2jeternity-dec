package l2e.gameserver.model.entity.mods.facebook.action;

import java.sql.ResultSet;
import java.sql.SQLException;
import l2e.gameserver.data.holder.FacebookProfilesHolder;
import l2e.gameserver.data.holder.OfficialPostsHolder;
import l2e.gameserver.model.entity.mods.facebook.FacebookAction;
import l2e.gameserver.model.entity.mods.facebook.FacebookActionType;
import l2e.gameserver.model.entity.mods.facebook.template.FacebookProfile;

public class Share implements FacebookAction {
   private final FacebookProfile _profile;
   private final long _extractionDate;
   private final FacebookAction _fatherAction;

   public Share(FacebookProfile profile, long extractionDate, FacebookAction fatherAction) {
      this._profile = profile;
      this._extractionDate = extractionDate;
      this._fatherAction = fatherAction;
   }

   public Share(ResultSet rset) throws SQLException {
      this._profile = FacebookProfilesHolder.getInstance().getProfileById(rset.getString("executor_id"));
      this._extractionDate = rset.getLong("extraction_date");
      this._fatherAction = OfficialPostsHolder.getInstance().getOfficialPost(rset.getString("father_id"));
   }

   @Override
   public String getId() {
      return null;
   }

   @Override
   public FacebookActionType getActionType() {
      return FacebookActionType.SHARE;
   }

   @Override
   public FacebookProfile getExecutor() {
      return this._profile;
   }

   @Override
   public long getCreatedDate() {
      return -1L;
   }

   @Override
   public long getExtractionDate() {
      return this._extractionDate;
   }

   @Override
   public String getMessage() {
      return "";
   }

   @Override
   public void changeMessage(String newMessage) {
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
      return obj instanceof Share && this._profile.equals(((Share)obj)._profile) && this._fatherAction.equals(((Share)obj)._fatherAction);
   }

   @Override
   public int hashCode() {
      int result = this._profile.hashCode();
      return 31 * result + this._fatherAction.hashCode();
   }

   @Override
   public String toString() {
      return "Share{profile=" + this._profile + ", extractionDate=" + this._extractionDate + ", fatherAction=" + this._fatherAction + '}';
   }
}
