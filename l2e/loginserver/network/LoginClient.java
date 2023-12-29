package l2e.loginserver.network;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.interfaces.RSAPrivateKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.loginserver.Config;
import l2e.loginserver.accounts.Account;
import l2e.loginserver.crypt.LoginCrypt;
import l2e.loginserver.crypt.ScrambledKeyPair;
import l2e.loginserver.network.serverpackets.AccountKicked;
import l2e.loginserver.network.serverpackets.LoginFail;
import l2e.loginserver.network.serverpackets.LoginServerPacket;
import org.nio.impl.MMOClient;
import org.nio.impl.MMOConnection;

public final class LoginClient extends MMOClient<MMOConnection<LoginClient>> {
   private static final Logger _log = Logger.getLogger(LoginClient.class.getName());
   private static final int PROTOCOL_VERSION = 50721;
   private LoginClient.LoginClientState _state = LoginClient.LoginClientState.CONNECTED;
   private LoginCrypt _loginCrypt;
   private ScrambledKeyPair _scrambledPair = Config.getScrambledRSAKeyPair();
   private byte[] _blowfishKey = Config.getBlowfishKey();
   private String _login;
   private SessionKey _skey;
   private Account _account;
   private final String _ipAddr;
   private int _sessionId;
   private boolean _passwordCorrect;

   public LoginClient(MMOConnection<LoginClient> con) {
      super(con);
      this._loginCrypt = new LoginCrypt();
      this._loginCrypt.setKey(this._blowfishKey);
      this._sessionId = con.hashCode();
      this._ipAddr = this.getConnection().getSocket().getInetAddress().getHostAddress();
      this._passwordCorrect = false;
   }

   @Override
   public boolean decrypt(ByteBuffer buf, int size) {
      boolean ret;
      try {
         ret = this._loginCrypt.decrypt(buf.array(), buf.position(), size);
      } catch (IOException var5) {
         _log.log(Level.WARNING, "", (Throwable)var5);
         this.closeNow(true);
         return false;
      }

      if (!ret) {
         this.closeNow(true);
      }

      return ret;
   }

   @Override
   public boolean encrypt(ByteBuffer buf, int size) {
      int offset = buf.position();

      try {
         size = this._loginCrypt.encrypt(buf.array(), offset, size);
      } catch (IOException var5) {
         _log.log(Level.WARNING, "", (Throwable)var5);
         return false;
      }

      ((Buffer)buf).position(offset + size);
      return true;
   }

   public LoginClient.LoginClientState getState() {
      return this._state;
   }

   public void setState(LoginClient.LoginClientState state) {
      this._state = state;
   }

   public byte[] getBlowfishKey() {
      return this._blowfishKey;
   }

   public byte[] getScrambledModulus() {
      return this._scrambledPair.getScrambledModulus();
   }

   public RSAPrivateKey getRSAPrivateKey() {
      return (RSAPrivateKey)this._scrambledPair.getKeyPair().getPrivate();
   }

   public String getLogin() {
      return this._login;
   }

   public void setLogin(String login) {
      this._login = login;
   }

   public Account getAccount() {
      return this._account;
   }

   public void setAccount(Account account) {
      this._account = account;
   }

   public SessionKey getSessionKey() {
      return this._skey;
   }

   public void setSessionKey(SessionKey skey) {
      this._skey = skey;
   }

   public void setSessionId(int val) {
      this._sessionId = val;
   }

   public int getSessionId() {
      return this._sessionId;
   }

   public void setPasswordCorrect(boolean val) {
      this._passwordCorrect = val;
   }

   public boolean isPasswordCorrect() {
      return this._passwordCorrect;
   }

   public void sendPacket(LoginServerPacket lsp) {
      if (this.isConnected()) {
         this.getConnection().sendPacket(lsp);
      }
   }

   public void close(LoginFail.LoginFailReason reason) {
      if (this.isConnected()) {
         this.getConnection().close(new LoginFail(reason));
      }
   }

   public void close(AccountKicked.AccountKickedReason reason) {
      if (this.isConnected()) {
         this.getConnection().close(new AccountKicked(reason));
      }
   }

   public void close(LoginServerPacket lsp) {
      if (this.isConnected()) {
         this.getConnection().close(lsp);
      }
   }

   @Override
   public void onDisconnection() {
      this._state = LoginClient.LoginClientState.DISCONNECTED;
      this._skey = null;
      this._loginCrypt = null;
      this._scrambledPair = null;
      this._blowfishKey = null;
   }

   @Override
   public String toString() {
      switch(this._state) {
         case AUTHED:
            return "[ Account : " + this.getLogin() + " IP: " + this.getIpAddress() + "]";
         default:
            return "[ State : " + this.getState() + " IP: " + this.getIpAddress() + "]";
      }
   }

   public String getIpAddress() {
      return this._ipAddr;
   }

   @Override
   protected void onForcedDisconnection() {
   }

   public int getProtocol() {
      return 50721;
   }

   public static enum LoginClientState {
      CONNECTED,
      AUTHED_GG,
      AUTHED,
      DISCONNECTED;
   }
}
