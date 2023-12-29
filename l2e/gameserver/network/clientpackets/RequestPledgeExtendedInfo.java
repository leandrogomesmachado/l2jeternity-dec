package l2e.gameserver.network.clientpackets;

public class RequestPledgeExtendedInfo extends GameClientPacket {
   protected String _name;

   @Override
   protected void readImpl() {
      this._name = this.readS();
   }

   @Override
   protected void runImpl() {
   }
}
