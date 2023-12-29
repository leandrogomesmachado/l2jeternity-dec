package l2e.gameserver.model.skills.conditions;

import java.util.ArrayList;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.stats.Env;

public class ConditionAgathionItemId extends Condition {
   private final ArrayList<Integer> _itemId;

   public ConditionAgathionItemId(ArrayList<Integer> itemId) {
      this._itemId = itemId;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() != null) {
         ItemInstance item = env.getPlayer().getInventory().getPaperdollItem(15);
         if (item != null) {
            return this._itemId.contains(item.getId());
         }
      }

      return false;
   }
}
