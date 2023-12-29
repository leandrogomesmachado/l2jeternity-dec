package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;

public class RequestEx2ndPasswordVerify extends GameClientPacket {
   private String _password;

   @Override
   protected void readImpl() {
      this._password = this.readS();
   }

   @Override
   protected void runImpl() {
      if (Config.SECOND_AUTH_ENABLED) {
         this.getClient().getSecondaryAuth().checkPassword(this._password, false);
      }
   }
}
