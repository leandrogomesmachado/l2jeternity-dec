package l2e.gameserver.model.skills.conditions;

import java.util.ArrayList;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.stats.Env;

public class ConditionPlayerHasPet extends Condition {
   private final ArrayList<Integer> _controlItemIds;

   public ConditionPlayerHasPet(ArrayList<Integer> itemIds) {
      if (itemIds.size() == 1 && itemIds.get(0) == 0) {
         this._controlItemIds = null;
      } else {
         this._controlItemIds = itemIds;
      }
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() == null || !(env.getPlayer().getSummon() instanceof PetInstance)) {
         return false;
      } else if (this._controlItemIds == null) {
         return true;
      } else {
         ItemInstance controlItem = ((PetInstance)env.getPlayer().getSummon()).getControlItem();
         return controlItem != null && this._controlItemIds.contains(controlItem.getId());
      }
   }
}
