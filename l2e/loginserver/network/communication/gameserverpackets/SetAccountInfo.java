package l2e.loginserver.network.communication.gameserverpackets;

import l2e.loginserver.accounts.SessionManager;
import l2e.loginserver.network.communication.GameServer;
import l2e.loginserver.network.communication.ReceivablePacket;
import org.HostInfo;
import org.apache.commons.lang3.ArrayUtils;

public class SetAccountInfo extends ReceivablePacket {
   private String _account;
   private int _size;
   private int[] _deleteChars;

   @Override
   protected void readImpl() {
      this._account = this.readS();
      this._size = this.readC();
      int size = this.readD();
      if (size <= 7 && size > 0) {
         this._deleteChars = new int[size];

         for(int i = 0; i < this._deleteChars.length; ++i) {
            this._deleteChars[i] = this.readD();
         }
      } else {
         this._deleteChars = ArrayUtils.EMPTY_INT_ARRAY;
      }
   }

   @Override
   protected void runImpl() {
      GameServer gs = this.getGameServer();
      if (gs.isAuthed()) {
         SessionManager.Session session = SessionManager.getInstance().getSessionByName(this._account);
         if (session == null) {
            return;
         }

         for(HostInfo host : gs.getHosts()) {
            session.getAccount().addAccountInfo(host.getId(), this._size, this._deleteChars);
         }
      }
   }
}
