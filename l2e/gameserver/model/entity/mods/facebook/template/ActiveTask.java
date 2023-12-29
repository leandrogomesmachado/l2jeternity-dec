package l2e.gameserver.model.entity.mods.facebook.template;

import java.util.concurrent.TimeUnit;
import l2e.commons.annotations.NotNull;
import l2e.commons.annotations.Nullable;
import l2e.gameserver.Config;
import l2e.gameserver.model.entity.mods.facebook.FacebookActionType;
import l2e.gameserver.model.entity.mods.facebook.FacebookIdentityType;
import l2e.gameserver.model.entity.mods.facebook.OfficialPost;

public class ActiveTask {
   private final int _playerId;
   private final FacebookIdentityType _identityType;
   private final String _identityValue;
   private final FacebookActionType _actionType;
   private final OfficialPost _father;
   private final String _requestedMessage;
   private final long _takenDate;

   public ActiveTask(
      int playerId,
      FacebookIdentityType identityType,
      String identityValue,
      FacebookActionType actionType,
      @Nullable OfficialPost father,
      @NotNull String requestedMessage,
      long takenDate
   ) {
      this._playerId = playerId;
      this._identityType = identityType;
      this._identityValue = identityValue;
      this._actionType = actionType;
      this._father = father;
      this._requestedMessage = requestedMessage;
      this._takenDate = takenDate;
   }

   public int getPlayerId() {
      return this._playerId;
   }

   public FacebookIdentityType getIdentityType() {
      return this._identityType;
   }

   public String getIdentityValue() {
      return this._identityValue;
   }

   public FacebookActionType getActionType() {
      return this._actionType;
   }

   public OfficialPost getFather() {
      return this._father;
   }

   @NotNull
   public String getRequestedMessage() {
      return this._requestedMessage;
   }

   public long getTakenDate() {
      return this._takenDate;
   }

   public long getTimeLimitDate() {
      return this._takenDate + TimeUnit.MILLISECONDS.convert((long)Config.FACEBOOK_TIME_LIMIT, TimeUnit.SECONDS);
   }

   public String getLinkToAction() {
      if (this._father == null) {
         return "https://www.facebook.com/" + Config.FACEBOOK_PAGE_NAME;
      } else {
         String[] ids = this._father.getId().split("_");
         return "https://www.facebook.com/" + ids[0] + "/posts/" + ids[1];
      }
   }

   @Override
   public String toString() {
      return "ActiveTask{playerId="
         + this._playerId
         + ", identityType="
         + this._identityType
         + ", identityValue='"
         + this._identityValue
         + '\''
         + ", actionType="
         + this._actionType
         + ", father="
         + this._father
         + ", requestedMessage='"
         + this._requestedMessage
         + '\''
         + ", takenDate="
         + this._takenDate
         + '}';
   }
}
