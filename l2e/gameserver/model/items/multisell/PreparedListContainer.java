package l2e.gameserver.model.items.multisell;

import java.util.ArrayList;
import java.util.LinkedList;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Armor;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.items.instance.ItemInstance;

public class PreparedListContainer extends ListContainer {
   private int _npcObjectId = 0;

   public PreparedListContainer(ListContainer template, boolean inventoryOnly, Player player, Npc npc) {
      super(template.getListId());
      this._maintainEnchantment = template.getMaintainEnchantment();
      this._applyTaxes = false;
      double taxRate = 0.0;
      if (npc != null) {
         this._npcObjectId = npc.getObjectId();
         if (template.getApplyTaxes() && npc.getIsInTown() && npc.getCastle().getOwnerId() > 0) {
            this._applyTaxes = true;
            taxRate = npc.getCastle().getTaxRate();
         }
      }

      if (inventoryOnly) {
         if (player == null) {
            return;
         }

         ItemInstance[] items;
         if (this._maintainEnchantment) {
            items = player.getInventory().getUniqueItemsByEnchantLevel(false, false, false);
         } else {
            items = player.getInventory().getUniqueItems(false, false, false);
         }

         this._entries = new LinkedList<>();

         for(ItemInstance item : items) {
            if (!item.isEquipped() && (item.getItem() instanceof Armor || item.getItem() instanceof Weapon)) {
               for(Entry ent : template.getEntries()) {
                  for(Ingredient ing : ent.getIngredients()) {
                     if (item.getId() == ing.getId()) {
                        this._entries.add(new PreparedEntry(ent, item, this._applyTaxes, this._maintainEnchantment, taxRate));
                        break;
                     }
                  }
               }
            }
         }
      } else {
         this._entries = new ArrayList<>(template.getEntries().size());

         for(Entry ent : template.getEntries()) {
            this._entries.add(new PreparedEntry(ent, null, this._applyTaxes, false, taxRate));
         }
      }
   }

   public final boolean checkNpcObjectId(int npcObjectId) {
      return this._npcObjectId != 0 ? this._npcObjectId == npcObjectId : true;
   }
}
