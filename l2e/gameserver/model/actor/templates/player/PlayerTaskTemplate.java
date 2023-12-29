package l2e.gameserver.model.actor.templates.player;

import l2e.gameserver.instancemanager.DailyTaskManager;
import l2e.gameserver.model.actor.templates.daily.DailyTaskTemplate;

public class PlayerTaskTemplate {
   private final int _id;
   private final String _type;
   private final String _sort;
   private int _currentNpcCount = 0;
   private int _currentPvpCount = 0;
   private int _currentPkCount = 0;
   private int _currentOlyMatchCount = 0;
   private int _currentEventsCount = 0;
   private boolean _isComplete = false;
   private boolean _isRewarded = false;
   DailyTaskTemplate _task = null;

   public PlayerTaskTemplate(int id, String type, String sort) {
      this._id = id;
      this._type = type;
      this._sort = sort;
      this._task = DailyTaskManager.getInstance().getDailyTask(id);
   }

   public int getId() {
      return this._id;
   }

   public String getType() {
      return this._type;
   }

   public String getSort() {
      return this._sort;
   }

   public boolean isComplete() {
      return this._isComplete;
   }

   public void setIsComplete(boolean complete) {
      this._isComplete = complete;
   }

   public boolean isRewarded() {
      return this._isRewarded;
   }

   public void setIsRewarded(boolean rewarded) {
      this._isRewarded = rewarded;
   }

   public void setCurrentNpcCount(int count) {
      this._currentNpcCount = count;
      if (this._currentNpcCount >= this._task.getNpcCount()) {
         this._isComplete = true;
      }
   }

   public int getCurrentNpcCount() {
      return this._currentNpcCount;
   }

   public void setCurrentPvpCount(int count) {
      this._currentPvpCount = count;
      if (this._currentPvpCount >= this._task.getPvpCount()) {
         this._isComplete = true;
      }
   }

   public int getCurrentPvpCount() {
      return this._currentPvpCount;
   }

   public void setCurrentPkCount(int count) {
      this._currentPkCount = count;
      if (this._currentPkCount >= this._task.getPkCount()) {
         this._isComplete = true;
      }
   }

   public int getCurrentPkCount() {
      return this._currentPkCount;
   }

   public void setCurrentOlyMatchCount(int count) {
      this._currentOlyMatchCount = count;
      if (this._currentOlyMatchCount >= this._task.getOlyMatchCount()) {
         this._isComplete = true;
      }
   }

   public int getCurrentOlyMatchCount() {
      return this._currentOlyMatchCount;
   }

   public void setCurrentEventsCount(int count) {
      this._currentEventsCount = count;
      if (this._currentEventsCount >= this._task.getEventsCount()) {
         this._isComplete = true;
      }
   }

   public int getCurrentEventsCount() {
      return this._currentEventsCount;
   }
}
