package l2e.gameserver.network.serverpackets;

public class GMHide extends GameServerPacket {
   private final int _mode;

   public GMHide(int mode) {
      this._mode = mode;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._mode);
   }
}
