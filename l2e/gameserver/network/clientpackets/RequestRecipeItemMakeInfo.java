package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.RecipeItemMakeInfo;

public final class RequestRecipeItemMakeInfo extends GameClientPacket {
   private int _id;

   @Override
   protected void readImpl() {
      this._id = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         RecipeItemMakeInfo response = new RecipeItemMakeInfo(this._id, player);
         this.sendPacket(response);
      }
   }
}
