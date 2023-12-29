package l2e.gameserver.network.serverpackets;

public class ExEventMatchMessage extends GameServerPacket {
   private final int _type;
   private final String _message;

   public ExEventMatchMessage(int type, String message) {
      this._type = type;
      this._message = message;
   }

   @Override
   protected void writeImpl() {
      this.writeC(this._type);
      this.writeS(this._message);
   }
}
