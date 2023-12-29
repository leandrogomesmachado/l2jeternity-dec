package l2e.loginserver.network.communication.loginserverpackets;

import l2e.loginserver.network.communication.SendablePacket;

public class GetAccountInfo extends SendablePacket {
   private final String _name;

   public GetAccountInfo(String name) {
      this._name = name;
   }

   @Override
   protected void writeImpl() {
      this.writeC(4);
      this.writeS(this._name);
   }
}
