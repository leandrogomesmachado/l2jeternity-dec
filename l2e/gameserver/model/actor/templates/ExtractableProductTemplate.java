package l2e.gameserver.model.actor.templates;

public class ExtractableProductTemplate {
   private final int _id;
   private final int _min;
   private final int _max;
   private final int _chance;

   public ExtractableProductTemplate(int id, int min, int max, double chance) {
      this._id = id;
      this._min = min;
      this._max = max;
      this._chance = (int)(chance * 1000.0);
   }

   public int getId() {
      return this._id;
   }

   public int getMin() {
      return this._min;
   }

   public int getMax() {
      return this._max;
   }

   public int getChance() {
      return this._chance;
   }
}
