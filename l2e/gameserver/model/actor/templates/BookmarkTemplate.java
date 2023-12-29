package l2e.gameserver.model.actor.templates;

import l2e.gameserver.model.Location;

public class BookmarkTemplate extends Location {
   private final int _id;
   private int _icon;
   private String _name;
   private String _tag;

   public BookmarkTemplate(int id, int x, int y, int z, int icon, String tag, String name) {
      super(x, y, z);
      this._id = id;
      this._icon = icon;
      this._name = name;
      this._tag = tag;
   }

   public String getName() {
      return this._name;
   }

   public void setName(String name) {
      this._name = name;
   }

   public int getId() {
      return this._id;
   }

   public int getIcon() {
      return this._icon;
   }

   public void setIcon(int icon) {
      this._icon = icon;
   }

   public String getTag() {
      return this._tag;
   }

   public void setTag(String tag) {
      this._tag = tag;
   }
}
