package l2e.gameserver.network.serverpackets;

public class ExRedSky extends GameServerPacket {
   private final int _duration;

   public ExRedSky(int duration) {
      this._duration = duration;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._duration);
   }
}
