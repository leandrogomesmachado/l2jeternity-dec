package l2e.loginserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import l2e.loginserver.database.DatabaseFactory;
import l2e.loginserver.network.LoginClient;
import l2e.loginserver.network.LoginPacketHandler;
import l2e.loginserver.network.SelectorHelper;
import l2e.loginserver.network.communication.GameServerCommunication;
import org.nio.impl.SelectorConfig;
import org.nio.impl.SelectorStats;
import org.nio.impl.SelectorThread;

public class LoginServer {
   private static final Logger _log = Logger.getLogger(LoginServer.class.getName());
   public static final int AUTH_SERVER_PROTOCOL = 4;
   private final GameServerCommunication _gameServerListener;
   private final SelectorThread<LoginClient> _selectorThread;
   private Thread _restartLoginServer;
   private static LoginServer _loginServer;

   public LoginServer() throws Exception {
      String LOG_FOLDER = "log";
      String LOG_NAME = "./config/log.ini";
      File logFolder = new File(Config.DATAPACK_ROOT, "log");
      logFolder.mkdir();

      try (InputStream is = new FileInputStream(new File("./config/log.ini"))) {
         LogManager.getLogManager().readConfiguration(is);
      } catch (IOException var17) {
         _log.warning(this.getClass().getSimpleName() + ": " + var17.getMessage());
      }

      Config.load();
      checkFreePorts();
      Class.forName(Config.DATABASE_DRIVER).newInstance();
      DatabaseFactory.getInstance().getConnection().close();
      Config.initCrypt();
      GameServerManager.getInstance();
      if (Config.LOGIN_SERVER_SCHEDULE_RESTART) {
         _log.info("Scheduled Login Server Restart at: " + Config.LOGIN_SERVER_SCHEDULE_RESTART_TIME + " hour(s).");
         this._restartLoginServer = new LoginServer.LoginServerRestart();
         this._restartLoginServer.setDaemon(true);
         this._restartLoginServer.start();
      }

      LoginPacketHandler lph = new LoginPacketHandler();
      SelectorHelper sh = new SelectorHelper();
      SelectorConfig sc = new SelectorConfig();
      sc.AUTH_TIMEOUT = 60000L;
      SelectorStats sts = new SelectorStats();
      this._selectorThread = new SelectorThread<>(sc, sts, lph, sh, sh, sh);
      this._gameServerListener = GameServerCommunication.getInstance();
      this._gameServerListener
         .openServerSocket(
            Config.GAME_SERVER_LOGIN_HOST.equals("*") ? null : InetAddress.getByName(Config.GAME_SERVER_LOGIN_HOST), Config.GAME_SERVER_LOGIN_PORT
         );
      this._gameServerListener.start();
      _log.info("Listening for Gameservers on " + Config.GAME_SERVER_LOGIN_HOST + ": " + Config.GAME_SERVER_LOGIN_PORT);
      this._selectorThread.openServerSocket(Config.LOGIN_HOST.equals("*") ? null : InetAddress.getByName(Config.LOGIN_HOST), Config.PORT_LOGIN);
      this._selectorThread.start();
      _log.info("Listening for Clients on " + Config.LOGIN_HOST + ": " + Config.PORT_LOGIN);
   }

   public GameServerCommunication getGameServerListener() {
      return this._gameServerListener;
   }

   private static void checkFreePorts() {
      boolean binded = false;

      while(!binded) {
         try {
            ServerSocket ss;
            if (Config.LOGIN_HOST.equalsIgnoreCase("*")) {
               ss = new ServerSocket(Config.PORT_LOGIN);
            } else {
               ss = new ServerSocket(Config.PORT_LOGIN, 50, InetAddress.getByName(Config.LOGIN_HOST));
            }

            ss.close();
            binded = true;
         } catch (Exception var4) {
            _log.warning("Port " + Config.PORT_LOGIN + " is already binded. Please free it and restart server!");
            binded = false;

            try {
               Thread.sleep(1000L);
            } catch (InterruptedException var3) {
            }
         }
      }
   }

   public void shutdown(boolean restart) {
      Runtime.getRuntime().exit(restart ? 2 : 0);
   }

   public static LoginServer getInstance() {
      return _loginServer;
   }

   public static void main(String[] args) throws Exception {
      _loginServer = new LoginServer();
   }

   class LoginServerRestart extends Thread {
      public LoginServerRestart() {
         this.setName("LoginServerRestart");
      }

      @Override
      public void run() {
         for(; !this.isInterrupted(); LoginServer.this.shutdown(true)) {
            try {
               Thread.sleep(Config.LOGIN_SERVER_SCHEDULE_RESTART_TIME * 3600000L);
            } catch (InterruptedException var2) {
               return;
            }
         }
      }
   }
}
