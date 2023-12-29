package l2e.gameserver.network.clientpackets;

public class RequestExChangeName extends GameClientPacket {
   protected String _newName;
   protected int _type;
   protected int _charSlot;

   @Override
   protected void readImpl() {
      this._type = this.readD();
      this._newName = this.readS();
      this._charSlot = this.readD();
   }

   @Override
   protected void runImpl() {
   }
}
