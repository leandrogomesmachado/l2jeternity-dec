package l2e.fake.ai.addon;

import l2e.fake.FakePlayer;
import l2e.gameserver.model.items.instance.ItemInstance;

public interface IConsumableSpender {
   default void handleConsumable(FakePlayer fakePlayer, int consumableId) {
      if (fakePlayer.getFakeAi().getArrowTime() <= System.currentTimeMillis() && consumableId != 0) {
         if (fakePlayer.getInventory().getItemByItemId(consumableId) != null) {
            if (fakePlayer.getInventory().getItemByItemId(consumableId).getCount() <= 20L) {
               fakePlayer.getInventory().addItem("", consumableId, 500L, fakePlayer, null);
            }
         } else {
            fakePlayer.getInventory().addItem("", consumableId, 500L, fakePlayer, null);
            ItemInstance consumable = fakePlayer.getInventory().getItemByItemId(consumableId);
            if (consumable.isEquipable()) {
               fakePlayer.getInventory().equipItem(consumable);
            }

            ItemInstance weapon = fakePlayer.getInventory().getPaperdollItem(5);
            if (weapon != null) {
               fakePlayer.getInventory().unEquipItem(weapon);
               fakePlayer.getInventory().equipItem(weapon);
            }
         }

         fakePlayer.getFakeAi().setArrowTime(System.currentTimeMillis() + 300000L);
      }
   }
}
