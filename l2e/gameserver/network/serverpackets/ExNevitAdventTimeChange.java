package l2e.gameserver.network.serverpackets;

public class ExNevitAdventTimeChange extends GameServerPacket {
   private final int _active;
   private final int _time;

   public ExNevitAdventTimeChange(boolean active, int time) {
      this._active = active ? 1 : 0;
      this._time = 14400 - time;
   }

   @Override
   protected void writeImpl() {
      this.writeC(this._active);
      this.writeD(this._time);
   }
}
