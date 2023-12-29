package l2e.gameserver.model.petition;

import java.util.Collection;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

public class PetitionMainGroup extends PetitionGroup {
   private final IntObjectMap<PetitionSection> _subGroups = new HashIntObjectMap<>();

   public PetitionMainGroup(int id) {
      super(id);
   }

   public void addSubGroup(PetitionSection subGroup) {
      this._subGroups.put(subGroup.getId(), subGroup);
   }

   public PetitionSection getSubGroup(int val) {
      return this._subGroups.get(val);
   }

   public Collection<PetitionSection> getSubGroups() {
      return this._subGroups.valueCollection();
   }
}
