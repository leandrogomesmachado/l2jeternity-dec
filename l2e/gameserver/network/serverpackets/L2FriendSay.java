package l2e.gameserver.network.serverpackets;

public class L2FriendSay extends GameServerPacket {
   private final String _sender;
   private final String _receiver;
   private final String _message;

   public L2FriendSay(String sender, String reciever, String message) {
      this._sender = sender;
      this._receiver = reciever;
      this._message = message;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(0);
      this.writeS(this._receiver);
      this.writeS(this._sender);
      this.writeS(this._message);
   }
}
