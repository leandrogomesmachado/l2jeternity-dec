package l2e.gameserver.model.actor.templates.promocode;

import java.util.List;
import l2e.gameserver.model.actor.templates.promocode.impl.AbstractCodeReward;

public class PromoCodeTemplate {
   private final String _name;
   private final int _minLvl;
   private final int _maxLvl;
   private final boolean _canUseSubClass;
   private final long _fromDate;
   private final long _toDate;
   private final int _limit;
   private int _curLimit = 0;
   private final List<AbstractCodeReward> _rewards;
   private final boolean _limitByAccount;
   private final boolean _limitHWID;

   public PromoCodeTemplate(
      String name,
      int minLvl,
      int maxLvl,
      boolean canUseSubClass,
      long fromDate,
      long toDate,
      int limit,
      List<AbstractCodeReward> rewards,
      boolean limitByAccount,
      boolean limitHWID
   ) {
      this._name = name;
      this._minLvl = minLvl;
      this._maxLvl = maxLvl;
      this._canUseSubClass = canUseSubClass;
      this._fromDate = fromDate;
      this._toDate = toDate;
      this._limit = limit;
      this._rewards = rewards;
      this._limitByAccount = limitByAccount;
      this._limitHWID = limitHWID;
   }

   public String getName() {
      return this._name;
   }

   public int getMinLvl() {
      return this._minLvl;
   }

   public int getMaxLvl() {
      return this._maxLvl;
   }

   public boolean canUseSubClass() {
      return this._canUseSubClass;
   }

   public long getStartDate() {
      return this._fromDate;
   }

   public long getEndDate() {
      return this._toDate;
   }

   public int getLimit() {
      return this._limit;
   }

   public int getCurLimit() {
      return this._curLimit;
   }

   public void setCurLimit(int limit) {
      this._curLimit = limit;
   }

   public List<AbstractCodeReward> getRewards() {
      return this._rewards;
   }

   public boolean isLimitByAccount() {
      return this._limitByAccount;
   }

   public boolean isLimitHWID() {
      return this._limitHWID;
   }
}
