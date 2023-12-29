package Interface.impl;

import l2e.gameserver.network.serverpackets.GameServerPacket;

public class KeyPacket extends GameServerPacket {
   public KeyPacket sendKey(byte[] key, int size) {
      return this;
   }

   @Override
   public final void writeImpl() {
      this.writeC(254);
      this.writeH(239);
   }
}
