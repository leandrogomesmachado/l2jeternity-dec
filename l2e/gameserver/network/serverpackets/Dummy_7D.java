package l2e.gameserver.network.serverpackets;

public class Dummy_7D extends GameServerPacket {
   private final Dummy_7D.ServerRequest _serverRequest;
   private String _drawText;
   private String _url;
   private Dummy_7D.WarnWindowType _warnWindowType;

   public Dummy_7D(String drawText) {
      this._serverRequest = Dummy_7D.ServerRequest.SC_SERVER_REQUEST_SET_DRAW_TEXT;
      this._drawText = drawText;
   }

   public Dummy_7D(String strValue, Dummy_7D.ServerRequest serverRequest) {
      this._serverRequest = serverRequest;
      if (serverRequest == Dummy_7D.ServerRequest.SC_SERVER_REQUEST_OPEN_URL) {
         this._url = strValue;
      } else if (serverRequest == Dummy_7D.ServerRequest.SC_SERVER_REQUEST_SET_DRAW_TEXT) {
         this._drawText = strValue;
      }
   }

   public Dummy_7D(Dummy_7D.WarnWindowType warnWindowType, String warnMessage) {
      this._serverRequest = Dummy_7D.ServerRequest.SC_SERVER_REQUEST_SHOW_CUSTOM_WARN_MESSAGE;
      this._warnWindowType = warnWindowType;
      this._drawText = warnMessage;
   }

   @Override
   protected final void writeImpl() {
      this.writeC(this._serverRequest.ordinal());
      switch(this._serverRequest) {
         case SC_SERVER_REQUEST_SET_DRAW_TEXT:
            this.writeS(this._drawText);
            break;
         case SC_SERVER_REQUEST_SHOW_CUSTOM_WARN_MESSAGE:
            this.writeC(this._warnWindowType.ordinal());
            this.writeS(this._drawText);
            break;
         case SC_SERVER_REQUEST_OPEN_URL:
            this.writeS(this._url);
      }
   }

   public static enum ServerRequest {
      SC_SERVER_REQUEST_SET_DRAW_TEXT,
      SC_SERVER_REQUEST_SHOW_CUSTOM_WARN_MESSAGE,
      SC_SERVER_REQUEST_OPEN_URL;
   }

   public static enum WarnWindowType {
      UL2CW_DEFAULT,
      UL2CW_CLOSE_WINDOW;
   }
}
