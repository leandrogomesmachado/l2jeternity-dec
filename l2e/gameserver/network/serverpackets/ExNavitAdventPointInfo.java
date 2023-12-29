package l2e.gameserver.network.serverpackets;

public class ExNavitAdventPointInfo extends GameServerPacket {
   private final int _points;

   public ExNavitAdventPointInfo(int points) {
      this._points = points;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._points);
   }
}
