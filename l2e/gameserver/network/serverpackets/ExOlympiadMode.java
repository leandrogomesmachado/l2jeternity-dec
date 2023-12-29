package l2e.gameserver.network.serverpackets;

public class ExOlympiadMode extends GameServerPacket {
   private final int _mode;

   public ExOlympiadMode(int mode) {
      this._mode = mode;
   }

   @Override
   protected final void writeImpl() {
      this.writeC(this._mode);
   }
}
