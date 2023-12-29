package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ControllableAirShipInstance;

public class MyTargetSelected extends GameServerPacket {
   private final int _objectId;
   private final int _color;

   public MyTargetSelected(Player player, Creature target) {
      this._objectId = target instanceof ControllableAirShipInstance ? ((ControllableAirShipInstance)target).getHelmObjectId() : target.getObjectId();
      this._color = target.isAutoAttackable(player) ? player.getLevel() - target.getLevel() : 0;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objectId);
      this.writeH(this._color);
      this.writeD(0);
   }
}
