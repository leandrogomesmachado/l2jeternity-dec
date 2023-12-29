package l2e.gameserver.model.entity.mods.streaming;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import l2e.commons.annotations.Nullable;
import l2e.gameserver.Config;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;

public class StreamTemplate {
   private final String _channelName;
   private String _streamGameName;
   private boolean _isStreamGameNameCorrect;
   private String _streamTitle;
   private boolean _isTitleCorrect;
   private int _viewersCount;
   private long _lastActiveDate;
   private int _attachedPlayerId = -1;
   private String _attachedPlayerServer = "";
   private long _notRewardedSeconds = 0L;
   private long _totalRewardedSecondsToday = 0L;
   private final List<Integer> _idsToApprove = new ArrayList<>();
   private long _punishedUntilDate = -1L;

   public StreamTemplate(
      String channelName,
      String streamGameName,
      boolean isStreamGameNameCorrect,
      String streamTitle,
      boolean isTitleCorrect,
      int viewersCount,
      long lastActiveDate
   ) {
      this._channelName = channelName;
      this._streamGameName = streamGameName;
      this._isStreamGameNameCorrect = isStreamGameNameCorrect;
      this._streamTitle = streamTitle;
      this._isTitleCorrect = isTitleCorrect;
      this._viewersCount = viewersCount;
      this._lastActiveDate = lastActiveDate;
   }

   public String getChannelName() {
      return this._channelName;
   }

   public void setStreamGameName(String streamGameName, boolean isStreamGameNameCorrect) {
      this._streamGameName = streamGameName;
      this._isStreamGameNameCorrect = isStreamGameNameCorrect;
   }

   public String getStreamGameName() {
      return this._streamGameName;
   }

   public boolean isStreamGameNameCorrect() {
      return this._isStreamGameNameCorrect;
   }

   public void setStreamTitle(String streamTitle, boolean isTitleCorrect) {
      this._streamTitle = streamTitle;
      this._isTitleCorrect = isTitleCorrect;
   }

   public String getStreamTitle() {
      return this._streamTitle;
   }

   public boolean isTitleCorrect() {
      return this._isTitleCorrect;
   }

   public void setViewersCount(int viewersCount) {
      this._viewersCount = viewersCount;
   }

   public int getViewersCount() {
      return this._viewersCount;
   }

   public void setLastActiveDate(long lastActiveDate) {
      this._lastActiveDate = lastActiveDate;
   }

   public long getLastActiveDate() {
      return this._lastActiveDate;
   }

   public void setAttachedPlayerId(int playerId, String playerServer) {
      this._attachedPlayerId = playerId;
      this._attachedPlayerServer = playerServer;
   }

   public int getAttachedPlayerId() {
      return this._attachedPlayerId;
   }

   @Nullable
   public Player getStreamingPlayer() {
      return this._attachedPlayerId <= 0 ? null : World.getInstance().getPlayer(this._attachedPlayerId);
   }

   public String getAttachedPlayerServer() {
      return this._attachedPlayerServer;
   }

   public void setNotRewardedSeconds(long notRewardedSeconds) {
      this._notRewardedSeconds = notRewardedSeconds;
   }

   public long incNotRewardedSeconds(long toAdd, boolean addToTotal, boolean checkTotal) {
      if (checkTotal && toAdd + this._totalRewardedSecondsToday > (long)Config.MAX_SEC_TO_REWARD_STREAMERS) {
         long realToAdd = (long)Config.MAX_SEC_TO_REWARD_STREAMERS - this._totalRewardedSecondsToday;
         if (realToAdd == 0L) {
            return 0L;
         } else {
            this._notRewardedSeconds += realToAdd;
            if (addToTotal) {
               this._totalRewardedSecondsToday += realToAdd;
            }

            return realToAdd;
         }
      } else {
         this._notRewardedSeconds += toAdd;
         if (addToTotal) {
            this._totalRewardedSecondsToday += toAdd;
         }

         return toAdd;
      }
   }

   public long getNotRewardedSeconds() {
      return this._notRewardedSeconds;
   }

   public void setTotalRewardedSecondsToday(long totalRewardedSecondsToday) {
      this._totalRewardedSecondsToday = totalRewardedSecondsToday;
   }

   public long getTotalRewardedSecondsToday() {
      return this._totalRewardedSecondsToday;
   }

   public void addIdsToApprove(Collection<Integer> ids) {
      this._idsToApprove.addAll(ids);
   }

   public void addIdToApprove(Integer id) {
      this._idsToApprove.add(id);
   }

   public boolean isOnApprovalList(Integer id) {
      return this._idsToApprove.contains(id);
   }

   public List<Integer> getIdsToApprove() {
      return this._idsToApprove;
   }

   public List<Integer> getIdsToApproveCopy() {
      return new ArrayList<>(this._idsToApprove);
   }

   public void setPunishedUntilDate(long date) {
      this._punishedUntilDate = date;
   }

   public long getPunishedUntilDate() {
      return this._punishedUntilDate;
   }

   public boolean isNowPunished() {
      return this._punishedUntilDate > System.currentTimeMillis();
   }

   public boolean isNowPunished(long currentDate) {
      return this._punishedUntilDate > currentDate;
   }

   @Override
   public String toString() {
      return "Stream{channelName='"
         + this._channelName
         + '\''
         + ", streamGameName='"
         + this._streamGameName
         + '\''
         + ", streamTitle='"
         + this._streamTitle
         + '\''
         + ", attachedPlayerId="
         + this._attachedPlayerId
         + '}';
   }
}
