package l2e.gameserver.network.serverpackets;

public class Ex2ndPasswordAck extends GameServerPacket {
   int _response;
   public static int SUCCESS = 0;
   public static int WRONG_PATTERN = 1;

   public Ex2ndPasswordAck(int response) {
      this._response = response;
   }

   @Override
   protected void writeImpl() {
      this.writeC(0);
      this.writeD(this._response == WRONG_PATTERN ? 1 : 0);
      this.writeD(0);
   }
}
