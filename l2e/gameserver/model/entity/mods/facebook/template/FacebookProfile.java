package l2e.gameserver.model.entity.mods.facebook.template;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import l2e.gameserver.Config;
import l2e.gameserver.data.dao.FacebookDAO;
import l2e.gameserver.model.entity.mods.facebook.FacebookActionType;

public class FacebookProfile {
   private final String _id;
   private final String _name;
   private long _lastCompletedTaskDate;
   private final TObjectIntHashMap<FacebookActionType> _positivePoints;
   private final TObjectIntHashMap<FacebookActionType> _negativePoints;

   public FacebookProfile(String id, String name) {
      this._id = id;
      this._name = name;
      this._positivePoints = new TObjectIntHashMap<>(0);
      this._negativePoints = new TObjectIntHashMap<>(0);
   }

   public FacebookProfile(
      String id,
      String name,
      long lastCompletedTaskDate,
      TObjectIntHashMap<FacebookActionType> positivePoints,
      TObjectIntHashMap<FacebookActionType> negativePoints
   ) {
      this._id = id;
      this._name = name;
      this._lastCompletedTaskDate = lastCompletedTaskDate;
      this._positivePoints = positivePoints;
      this._negativePoints = negativePoints;
   }

   public String getId() {
      return this._id;
   }

   public String getName() {
      return this._name;
   }

   public void setLastCompletedTaskDate(long lastCompletedTaskDate) {
      this._lastCompletedTaskDate = lastCompletedTaskDate;
   }

   public long getLastCompletedTaskDate() {
      return this._lastCompletedTaskDate;
   }

   public void addPositivePoint(FacebookActionType type, boolean saveInDatabase) {
      int points = this._positivePoints.get(type);
      if (points == Constants.DEFAULT_INT_NO_ENTRY_VALUE) {
         this._positivePoints.put(type, 1);
      } else {
         this._positivePoints.put(type, points + 1);
      }

      if (saveInDatabase) {
         FacebookDAO.replaceFacebookProfile(this);
      }
   }

   public int getPositivePoints(FacebookActionType type) {
      int value = this._positivePoints.get(type);
      return value == Constants.DEFAULT_INT_NO_ENTRY_VALUE ? 0 : value;
   }

   public TObjectIntHashMap<FacebookActionType> getPositivePointsForIterate() {
      return this._positivePoints;
   }

   public void addNegativePoint(FacebookActionType type, boolean saveInDatabase) {
      int points = this._negativePoints.get(type);
      if (points == Constants.DEFAULT_INT_NO_ENTRY_VALUE) {
         this._negativePoints.put(type, 1);
      } else {
         this._negativePoints.put(type, points + 1);
      }

      if (saveInDatabase) {
         FacebookDAO.replaceFacebookProfile(this);
      }
   }

   public void removeNegativePoint(FacebookActionType type, boolean saveInDatabase) {
      int points = this._negativePoints.get(type);
      if (points != Constants.DEFAULT_INT_NO_ENTRY_VALUE) {
         if (points == 1) {
            this._negativePoints.remove(type);
         } else {
            this._negativePoints.put(type, points - 1);
         }

         if (saveInDatabase) {
            FacebookDAO.replaceFacebookProfile(this);
         }
      }
   }

   public int getNegativePoints(FacebookActionType type) {
      int value = this._negativePoints.get(type);
      return value == Constants.DEFAULT_INT_NO_ENTRY_VALUE ? 0 : value;
   }

   public boolean hasNegativePoints() {
      return !this._negativePoints.isEmpty();
   }

   public Set<FacebookActionType> getNegativePointTypesForIterate() {
      return this._negativePoints.keySet();
   }

   public TObjectIntHashMap<FacebookActionType> getNegativePointsForIterate() {
      return this._negativePoints;
   }

   public long getDelayEndDate() {
      if (this._lastCompletedTaskDate < 0L) {
         return -1L;
      } else {
         long endDate = this._lastCompletedTaskDate + TimeUnit.MILLISECONDS.convert((long)Config.FACEBOOK_DELAY_BETWEEN_TASK, TimeUnit.SECONDS);
         return endDate < System.currentTimeMillis() ? -1L : endDate;
      }
   }

   public boolean hasTaskDelay() {
      return this._lastCompletedTaskDate >= 0L
         && this._lastCompletedTaskDate + TimeUnit.MILLISECONDS.convert((long)Config.FACEBOOK_DELAY_BETWEEN_TASK, TimeUnit.SECONDS)
            > System.currentTimeMillis();
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof FacebookProfile && this._id.equals(((FacebookProfile)obj)._id);
   }

   @Override
   public int hashCode() {
      return this._id.hashCode();
   }

   @Override
   public String toString() {
      return "FacebookProfile{id='"
         + this._id
         + '\''
         + ", name='"
         + this._name
         + '\''
         + ", lastCompletedTaskDate="
         + this._lastCompletedTaskDate
         + ", positivePoints="
         + this._positivePoints
         + ", negativePoints="
         + this._negativePoints
         + '}';
   }
}
