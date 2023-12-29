package l2e.gameserver.model.service.exchange;

public class Variant {
   final int _number;
   final int _id;
   final String _name;
   final String _icon;

   public Variant(int number, int id, String name, String icon) {
      this._number = number;
      this._id = id;
      this._name = name;
      this._icon = icon;
   }

   public int getNumber() {
      return this._number;
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
}
