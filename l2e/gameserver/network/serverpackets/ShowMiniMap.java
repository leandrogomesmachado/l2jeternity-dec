package l2e.gameserver.network.serverpackets;

import l2e.gameserver.SevenSigns;

public class ShowMiniMap extends GameServerPacket {
   private final int _mapId;

   public ShowMiniMap(int mapId) {
      this._mapId = mapId;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._mapId);
      this.writeC(SevenSigns.getInstance().getCurrentPeriod());
   }
}
