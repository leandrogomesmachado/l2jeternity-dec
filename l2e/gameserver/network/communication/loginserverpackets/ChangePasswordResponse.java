package l2e.gameserver.network.communication.loginserverpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.communication.AuthServerCommunication;
import l2e.gameserver.network.communication.ReceivablePacket;

public class ChangePasswordResponse extends ReceivablePacket {
   public String _account;
   public boolean _changed;

   @Override
   protected void readImpl() {
      this._account = this.readS();
      this._changed = this.readD() == 1;
   }

   @Override
   protected void runImpl() {
      GameClient client = AuthServerCommunication.getInstance().getAuthedClient(this._account);
      if (client != null) {
         Player activeChar = client.getActiveChar();
         if (activeChar != null) {
            if (this._changed) {
               activeChar.sendMessage("Password changed!");
            } else {
               activeChar.sendMessage("Password not changed!");
            }
         }
      }
   }
}
