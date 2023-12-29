package l2e.gameserver.network.serverpackets;

public class ExBrBroadcastEventState extends GameServerPacket {
   private final int _eventId;
   private final int _eventState;
   private int _param0;
   private int _param1;
   private int _param2;
   private int _param3;
   private int _param4;
   private String _param5;
   private String _param6;
   public static final int APRIL_FOOLS = 20090401;
   public static final int EVAS_INFERNO = 20090801;
   public static final int HALLOWEEN_EVENT = 20091031;
   public static final int RAISING_RUDOLPH = 20091225;
   public static final int LOVERS_JUBILEE = 20100214;

   public ExBrBroadcastEventState(int eventId, int eventState) {
      this._eventId = eventId;
      this._eventState = eventState;
   }

   public ExBrBroadcastEventState(int eventId, int eventState, int param0, int param1, int param2, int param3, int param4, String param5, String param6) {
      this._eventId = eventId;
      this._eventState = eventState;
      this._param0 = param0;
      this._param1 = param1;
      this._param2 = param2;
      this._param3 = param3;
      this._param4 = param4;
      this._param5 = param5;
      this._param6 = param6;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._eventId);
      this.writeD(this._eventState);
      this.writeD(this._param0);
      this.writeD(this._param1);
      this.writeD(this._param2);
      this.writeD(this._param3);
      this.writeD(this._param4);
      this.writeS(this._param5);
      this.writeS(this._param6);
   }
}
