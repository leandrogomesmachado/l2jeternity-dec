package l2e.loginserver.network.clientpackets;

import javax.crypto.Cipher;
import l2e.loginserver.Config;
import l2e.loginserver.GameServerManager;
import l2e.loginserver.IpBanManager;
import l2e.loginserver.accounts.Account;
import l2e.loginserver.accounts.SessionManager;
import l2e.loginserver.crypt.PasswordHash;
import l2e.loginserver.network.LoginClient;
import l2e.loginserver.network.communication.GameServer;
import l2e.loginserver.network.communication.loginserverpackets.GetAccountInfo;
import l2e.loginserver.network.serverpackets.LoginFail;
import l2e.loginserver.network.serverpackets.LoginOk;
import l2e.loginserver.network.serverpackets.ServerList;

public class RequestAuthLogin extends LoginClientPacket {
   private final byte[] _raw1 = new byte[128];
   private final byte[] _raw2 = new byte[128];
   private boolean _newAuthMethod = false;

   @Override
   protected void readImpl() {
      if (this._buf.remaining() >= this._raw1.length + this._raw2.length) {
         this._newAuthMethod = true;
         this.readB(this._raw1);
         this.readB(this._raw2);
      }

      if (this._buf.remaining() >= this._raw1.length) {
         this.readB(this._raw1);
         this.readD();
         this.readD();
         this.readD();
         this.readD();
         this.readD();
         this.readD();
         this.readH();
         this.readC();
      }
   }

   @Override
   protected void runImpl() throws Exception {
      LoginClient client = this.getClient();
      byte[] decUser = null;
      byte[] decPass = null;

      try {
         Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
         rsaCipher.init(2, client.getRSAPrivateKey());
         decUser = rsaCipher.doFinal(this._raw1, 0, 128);
         if (this._newAuthMethod) {
            decPass = rsaCipher.doFinal(this._raw2, 0, this._raw2.length);
         }
      } catch (Exception var14) {
         client.closeNow(true);
         return;
      }

      String user = null;
      String password = null;
      if (this._newAuthMethod) {
         user = new String(decUser, 78, 32).trim().toLowerCase();
         password = new String(decPass, 92, 16).trim();
      } else {
         user = new String(decUser, 94, 14).trim().toLowerCase();
         password = new String(decUser, 108, 16).trim();
      }

      int currentTime = (int)(System.currentTimeMillis() / 1000L);
      Account account = new Account(user);
      account.restore();
      String passwordHash = Config.DEFAULT_CRYPT.encrypt(password);
      if (account.getPasswordHash() == null) {
         if (!Config.AUTO_CREATE_ACCOUNTS || !user.matches(Config.ANAME_TEMPLATE) || !password.matches(Config.APASSWD_TEMPLATE)) {
            client.close(LoginFail.LoginFailReason.REASON_USER_OR_PASS_WRONG);
            return;
         }

         account.setAllowedIP("");
         account.setAllowedHwid("");
         account.setPasswordHash(passwordHash);
         account.save();
      }

      boolean passwordCorrect = true;
      if (!account.getPasswordHash().equals(passwordHash) && Config.ALLOW_ENCODE_PASSWORD) {
         passwordCorrect = false;
      }

      if (!account.getPasswordHash().equals(passwordHash) && !account.getPasswordHash().equals(password) && !Config.ALLOW_ENCODE_PASSWORD) {
         passwordCorrect = false;
      }

      if (!passwordCorrect) {
         for(PasswordHash c : Config.LEGACY_CRYPT) {
            if (c.compare(password, account.getPasswordHash())) {
               passwordCorrect = true;
               account.setPasswordHash(passwordHash);
               break;
            }
         }
      }

      if (!IpBanManager.getInstance().tryLogin(client.getIpAddress(), passwordCorrect)) {
         client.closeNow(false);
      } else {
         client.setPasswordCorrect(passwordCorrect);
         if (!Config.CHEAT_PASSWORD_CHECK && !passwordCorrect) {
            client.close(LoginFail.LoginFailReason.REASON_USER_OR_PASS_WRONG);
         } else if (account.getAccessLevel() < 0) {
            client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
         } else if (account.getBanExpire() > currentTime) {
            client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
         } else if (!account.isAllowedIP(client.getIpAddress())) {
            client.close(LoginFail.LoginFailReason.REASON_ATTEMPTED_RESTRICTED_IP);
         } else {
            for(GameServer gs : GameServerManager.getInstance().getGameServers()) {
               if (gs.isAuthed()) {
                  gs.sendPacket(new GetAccountInfo(user));
               }
            }

            account.setLastAccess(currentTime);
            account.setLastIP(client.getIpAddress());
            SessionManager.Session session = SessionManager.getInstance().openSession(account, client.getIpAddress());
            client.setAuthed(true);
            client.setLogin(user);
            client.setAccount(account);
            client.setSessionKey(session.getSessionKey());
            client.setState(LoginClient.LoginClientState.AUTHED);
            client.sendPacket(new LoginOk(client.getSessionKey()));
            if (Config.SHOW_LICENCE) {
               client.sendPacket(new LoginOk(this.getClient().getSessionKey()));
            } else {
               this.getClient().sendPacket(new ServerList(account));
            }
         }
      }
   }
}
