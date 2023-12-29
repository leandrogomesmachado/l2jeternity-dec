package Interface.impl;

import l2e.gameserver.network.serverpackets.GameServerPacket;

public class ConfigPacket extends GameServerPacket {
   @Override
   public final void writeImpl() {
      this.writeC(254);
      this.writeH(238);
   }
}
