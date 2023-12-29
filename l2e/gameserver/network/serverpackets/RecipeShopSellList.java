package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.ManufactureItemTemplate;

public class RecipeShopSellList extends GameServerPacket {
   private final Player _buyer;
   private final Player _manufacturer;

   public RecipeShopSellList(Player buyer, Player manufacturer) {
      this._buyer = buyer;
      this._manufacturer = manufacturer;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._manufacturer.getObjectId());
      this.writeD((int)this._manufacturer.getCurrentMp());
      this.writeD((int)this._manufacturer.getMaxMp());
      this.writeQ(this._buyer.getAdena());
      if (!this._manufacturer.hasManufactureShop()) {
         this.writeD(0);
      } else {
         this.writeD(this._manufacturer.getManufactureItems().size());

         for(ManufactureItemTemplate temp : this._manufacturer.getManufactureItems().values()) {
            this.writeD(temp.getRecipeId());
            this.writeD(0);
            this.writeQ(temp.getCost());
         }
      }
   }
}
