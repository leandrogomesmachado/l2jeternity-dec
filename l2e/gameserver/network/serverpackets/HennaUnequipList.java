package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Henna;

public class HennaUnequipList extends GameServerPacket {
   private final Player _player;

   public HennaUnequipList(Player player) {
      this._player = player;
   }

   @Override
   protected final void writeImpl() {
      this.writeQ(this._player.getAdena());
      this.writeD(0);
      this.writeD(3 - this._player.getHennaEmptySlots());

      for(Henna henna : this._player.getHennaList()) {
         if (henna != null) {
            this.writeD(henna.getDyeId());
            this.writeD(henna.getDyeItemId());
            this.writeD(henna.getCancelCount());
            this.writeD(0);
            this.writeD(henna.getCancelFee());
            this.writeD(0);
            this.writeD(1);
         }
      }
   }
}
