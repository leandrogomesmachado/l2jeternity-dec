package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;

public class ExPrivateStorePackageMsg extends GameServerPacket {
   private final int _objectId;
   private final String _msg;

   public ExPrivateStorePackageMsg(Player player, String msg) {
      this._objectId = player.getObjectId();
      this._msg = msg;
   }

   public ExPrivateStorePackageMsg(Player player) {
      this(player, player.getSellList().getTitle());
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._objectId);
      this.writeS(this._msg);
   }
}
