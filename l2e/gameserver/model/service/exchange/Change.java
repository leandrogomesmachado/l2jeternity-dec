package l2e.gameserver.model.service.exchange;

import java.util.List;

public class Change {
   final int _id;
   final String _name;
   final String _icon;
   final int _cost_id;
   final long _cost_count;
   final boolean _attribute_change;
   final boolean _is_upgrade;
   final List<Variant> _variants;

   public Change(int id, String name, String icon, int cost_id, long cost_count, boolean attribute_change, boolean is_upgrade, List<Variant> variants) {
      this._id = id;
      this._name = name;
      this._icon = icon;
      this._cost_id = cost_id;
      this._cost_count = cost_count;
      this._attribute_change = attribute_change;
      this._is_upgrade = is_upgrade;
      this._variants = variants;
   }

   public int getId() {
      return this._id;
   }

   public String getName() {
      return this._name;
   }

   public String getIcon() {
      return this._icon;
   }

   public int getCostId() {
      return this._cost_id;
   }

   public long getCostCount() {
      return this._cost_count;
   }

   public boolean attChange() {
      return this._attribute_change;
   }

   public boolean isUpgrade() {
      return this._is_upgrade;
   }

   public List<Variant> getList() {
      return this._variants;
   }

   public Variant getVariant(int id) {
      for(Variant var : this._variants) {
         if (var.getNumber() == id) {
            return var;
         }
      }

      return null;
   }
}
