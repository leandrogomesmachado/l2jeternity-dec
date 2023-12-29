package l2e.gameserver.model.items.enchant;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.model.holders.RangeChanceHolder;

public final class EnchantItemGroup {
   private static final Logger _log = Logger.getLogger(EnchantItemGroup.class.getName());
   private final List<RangeChanceHolder> _chances = new ArrayList<>();
   private final String _name;

   public EnchantItemGroup(String name) {
      this._name = name;
   }

   public String getName() {
      return this._name;
   }

   public void addChance(RangeChanceHolder holder) {
      this._chances.add(holder);
   }

   public double getChance(int index) {
      if (!this._chances.isEmpty()) {
         for(RangeChanceHolder holder : this._chances) {
            if (holder.getMin() <= index && holder.getMax() >= index) {
               return holder.getChance();
            }
         }

         _log.log(
            Level.WARNING,
            this.getClass().getSimpleName() + ": Couldn't match proper chance for item group: " + this._name,
            (Throwable)(new IllegalStateException())
         );
         return this._chances.get(this._chances.size() - 1).getChance();
      } else {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": item group: " + this._name + " doesn't have any chances!");
         return -1.0;
      }
   }
}
