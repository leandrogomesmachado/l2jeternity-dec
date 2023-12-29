package l2e.gameserver.network.serverpackets;

public class ExBrBuffEventState extends GameServerPacket {
   private final int _type;
   private final int _value;
   private final int _state;
   private final int _endtime;

   public ExBrBuffEventState(int type, int value, int state, int endtime) {
      this._type = type;
      this._value = value;
      this._state = state;
      this._endtime = endtime;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._type);
      this.writeD(this._value);
      this.writeD(this._state);
      this.writeD(this._endtime);
   }
}
