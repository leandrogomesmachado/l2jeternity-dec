package l2e.gameserver.model.petition;

import java.util.HashMap;
import java.util.Map;

public abstract class PetitionGroup {
   private final Map<String, String> _name = new HashMap<>(2);
   private final Map<String, String> _description = new HashMap<>(2);
   private final int _id;

   public PetitionGroup(int id) {
      this._id = id;
   }

   public int getId() {
      return this._id;
   }

   public String getName(String lang) {
      return this._name.get(lang);
   }

   public void setName(String lang, String name) {
      this._name.put(lang, name);
   }

   public String getDescription(String lang) {
      return this._description.get(lang);
   }

   public void setDescription(String lang, String name) {
      this._description.put(lang, name);
   }
}
