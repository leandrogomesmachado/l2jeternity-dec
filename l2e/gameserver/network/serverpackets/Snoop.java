package l2e.gameserver.network.serverpackets;

public class Snoop extends GameServerPacket {
   private final int _convoId;
   private final String _name;
   private final int _type;
   private final String _speaker;
   private final String _msg;

   public Snoop(int id, String name, int type, String speaker, String msg) {
      this._convoId = id;
      this._name = name;
      this._type = type;
      this._speaker = speaker;
      this._msg = msg;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._convoId);
      this.writeS(this._name);
      this.writeD(0);
      this.writeD(this._type);
      this.writeS(this._speaker);
      this.writeS(this._msg);
   }
}
