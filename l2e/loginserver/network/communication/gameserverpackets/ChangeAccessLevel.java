package l2e.loginserver.network.communication.gameserverpackets;

import l2e.loginserver.accounts.Account;
import l2e.loginserver.network.communication.ReceivablePacket;

public class ChangeAccessLevel extends ReceivablePacket {
   private String _account;
   private int _level;
   private int _banExpire;

   @Override
   protected void readImpl() {
      this._account = this.readS();
      this._level = this.readD();
      this._banExpire = this.readD();
   }

   @Override
   protected void runImpl() {
      Account acc = new Account(this._account);
      acc.restore();
      acc.setAccessLevel(this._level);
      acc.setBanExpire(this._banExpire);
      acc.update();
   }
}
