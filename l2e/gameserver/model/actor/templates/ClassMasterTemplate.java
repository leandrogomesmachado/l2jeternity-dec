package l2e.gameserver.model.actor.templates;

import java.util.Map;

public class ClassMasterTemplate {
   private final Map<Integer, Long> _requestItems;
   private final Map<Integer, Long> _rewardItems;
   private final boolean _allowedClassChange;

   public ClassMasterTemplate(Map<Integer, Long> requestItems, Map<Integer, Long> rewardItems, boolean allowedClassChange) {
      this._requestItems = requestItems;
      this._rewardItems = rewardItems;
      this._allowedClassChange = allowedClassChange;
   }

   public Map<Integer, Long> getRequestItems() {
      return this._requestItems;
   }

   public Map<Integer, Long> getRewardItems() {
      return this._rewardItems;
   }

   public boolean isAllowedChangeClass() {
      return this._allowedClassChange;
   }
}
