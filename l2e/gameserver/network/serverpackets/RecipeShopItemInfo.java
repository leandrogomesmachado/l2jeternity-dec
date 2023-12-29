package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;

public class RecipeShopItemInfo extends GameServerPacket {
   private final Player _player;
   private final int _recipeId;

   public RecipeShopItemInfo(Player player, int recipeId) {
      this._player = player;
      this._recipeId = recipeId;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._player.getObjectId());
      this.writeD(this._recipeId);
      this.writeD((int)this._player.getCurrentMp());
      this.writeD((int)this._player.getMaxMp());
      this.writeD(-1);
   }
}
