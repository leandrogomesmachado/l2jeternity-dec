package l2e.gameserver.network.clientpackets;

public final class RequestDeleteMacro extends GameClientPacket {
   private int _id;

   @Override
   protected void readImpl() {
      this._id = this.readD();
   }

   @Override
   protected void runImpl() {
      if (this.getClient().getActiveChar() != null) {
         this.getClient().getActiveChar().deleteMacro(this._id);
      }
   }
}
