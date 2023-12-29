package l2e.gameserver.network.clientpackets;

import java.util.Arrays;
import java.util.List;
import l2e.commons.util.Broadcast;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.RecipeParser;
import l2e.gameserver.model.RecipeList;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.ManufactureItemTemplate;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.RecipeShopMsg;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;

public final class RequestRecipeShopListSet extends GameClientPacket {
   private static final int BATCH_LENGTH = 12;
   private ManufactureItemTemplate[] _items = null;

   @Override
   protected void readImpl() {
      int count = this.readD();
      if (count > 0 && count <= Config.MAX_ITEM_IN_PACKET && count * 12 == this._buf.remaining()) {
         this._items = new ManufactureItemTemplate[count];

         for(int i = 0; i < count; ++i) {
            int id = this.readD();
            long cost = this.readQ();
            if (cost < 0L) {
               this._items = null;
               return;
            }

            this._items[i] = new ManufactureItemTemplate(id, cost);
         }
      }
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (this._items == null) {
            player.setPrivateStoreType(0);
            player.broadcastCharInfo();
         } else if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) && !player.isInDuel()) {
            if (player.canOpenPrivateStore(false)) {
               List<RecipeList> dwarfRecipes = Arrays.asList(player.getDwarvenRecipeBook());
               List<RecipeList> commonRecipes = Arrays.asList(player.getCommonRecipeBook());
               if (player.hasManufactureShop()) {
                  player.getManufactureItems().clear();
               }

               for(ManufactureItemTemplate i : this._items) {
                  RecipeList list = RecipeParser.getInstance().getRecipeList(i.getRecipeId());
                  if (!dwarfRecipes.contains(list) && !commonRecipes.contains(list)) {
                     Util.handleIllegalPlayerAction(
                        player, "" + player.getName() + " of account " + player.getAccountName() + " tried to set recipe which he dont have."
                     );
                     return;
                  }

                  if (i.getCost() > PcInventory.MAX_ADENA) {
                     Util.handleIllegalPlayerAction(
                        player,
                        ""
                           + player.getName()
                           + " of account "
                           + player.getAccountName()
                           + " tried to set price more than "
                           + PcInventory.MAX_ADENA
                           + " adena in Private Manufacture."
                     );
                     return;
                  }

                  player.getManufactureItems().put(i.getRecipeId(), i);
               }

               player.setStoreName(!player.hasManufactureShop() ? "" : player.getStoreName());
               player.setPrivateStoreType(5);
               player.sitDown();
               player.saveTradeList();
               player.setIsInStoreNow(true);
               player.broadcastCharInfo();
               Broadcast.toSelfAndKnownPlayers(player, new RecipeShopMsg(player));
            }
         } else {
            player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
            player.sendActionFailed();
         }
      }
   }
}
