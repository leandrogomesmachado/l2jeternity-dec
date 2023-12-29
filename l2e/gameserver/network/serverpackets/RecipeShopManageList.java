package l2e.gameserver.network.serverpackets;

import java.util.Iterator;
import l2e.gameserver.model.RecipeList;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.ManufactureItemTemplate;

public class RecipeShopManageList extends GameServerPacket {
   private final Player _seller;
   private final boolean _isDwarven;
   private RecipeList[] _recipes;

   public RecipeShopManageList(Player seller, boolean isDwarven) {
      this._seller = seller;
      this._isDwarven = isDwarven;
      if (this._isDwarven && this._seller.hasDwarvenCraft()) {
         this._recipes = this._seller.getDwarvenRecipeBook();
      } else {
         this._recipes = this._seller.getCommonRecipeBook();
      }

      if (this._seller.hasManufactureShop()) {
         Iterator<ManufactureItemTemplate> it = this._seller.getManufactureItems().values().iterator();

         while(it.hasNext()) {
            ManufactureItemTemplate item = it.next();
            if (item.isDwarven() != this._isDwarven || !seller.hasRecipeList(item.getRecipeId())) {
               it.remove();
            }
         }
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._isDwarven ? 0 : 1);
      this.writeD((int)this._seller.getAdena());
      this.writeD(this._seller.getObjectId());
      if (this._recipes == null) {
         this.writeD(0);
      } else {
         this.writeD(this._recipes.length);

         for(int i = 0; i < this._recipes.length; ++i) {
            RecipeList temp = this._recipes[i];
            this.writeD(temp.getId());
            this.writeD(i + 1);
         }
      }

      if (!this._seller.hasManufactureShop()) {
         this.writeD(0);
      } else {
         this.writeD(this._seller.getManufactureItems().size());

         for(ManufactureItemTemplate item : this._seller.getManufactureItems().values()) {
            this.writeD(item.getRecipeId());
            this.writeD(0);
            this.writeQ(item.getCost());
         }
      }
   }
}
