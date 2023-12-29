package l2e.gameserver.model.holders;

import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.model.base.AttackType;

public class ClassBalanceHolder {
   private final int _activeClass;
   private final int _targetClass;
   private final Map<AttackType, Double> _normalBalance = new ConcurrentHashMap<>();
   private final Map<AttackType, Double> _olyBalance = new ConcurrentHashMap<>();

   public ClassBalanceHolder(int activeClass, int target) {
      this._activeClass = activeClass;
      this._targetClass = target;
   }

   public void addNormalBalance(AttackType type, double value) {
      this._normalBalance.put(type, value);
   }

   public void addOlyBalance(AttackType type, double value) {
      this._olyBalance.put(type, value);
   }

   public int getTargetClass() {
      return this._targetClass;
   }

   public int getActiveClass() {
      return this._activeClass;
   }

   public Map<AttackType, Double> getNormalBalance() {
      Map<AttackType, Double> map = new TreeMap<>(new ClassBalanceHolder.AttackTypeComparator());
      map.putAll(this._normalBalance);
      return map;
   }

   public void removeOlyBalance(AttackType type) {
      if (this._olyBalance.containsKey(type)) {
         this._olyBalance.remove(type);
      }
   }

   public double getOlyBalanceValue(AttackType type) {
      return !this._olyBalance.containsKey(type) ? 1.0 : this._olyBalance.get(type);
   }

   public double getBalanceValue(AttackType type) {
      return !this._normalBalance.containsKey(type) ? 1.0 : this._normalBalance.get(type);
   }

   public void remove(AttackType type) {
      if (this._normalBalance.containsKey(type)) {
         this._normalBalance.remove(type);
      }
   }

   public Map<AttackType, Double> getOlyBalance() {
      Map<AttackType, Double> map = new TreeMap<>(new ClassBalanceHolder.AttackTypeComparator());
      map.putAll(this._olyBalance);
      return map;
   }

   private class AttackTypeComparator implements Comparator<AttackType> {
      public AttackTypeComparator() {
      }

      public int compare(AttackType l, AttackType r) {
         int left = l.getId();
         int right = r.getId();
         if (left > right) {
            return 1;
         } else if (left < right) {
            return -1;
         } else {
            Random rnd = new Random();
            return rnd.nextInt(2) == 1 ? 1 : 1;
         }
      }
   }
}
