package l2e.loginserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import l2e.commons.util.Files;
import l2e.commons.util.LoginSettings;
import l2e.commons.util.Rnd;
import l2e.loginserver.crypt.PasswordHash;
import l2e.loginserver.crypt.ScrambledKeyPair;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Config {
   private static final Logger _log = Logger.getLogger(Config.class.getName());
   public static final String PERSONAL_FILE = "./config/network/personal.ini";
   public static final String LOGIN_CONFIGURATION_FILE = "./config/network/loginserver.ini";
   public static final String SERVER_NAMES_FILE = "./config/servername.xml";
   private static final HashMap<String, String> _personalConfigs = new HashMap<>();
   public static String LOGIN_HOST;
   public static int PORT_LOGIN;
   public static File DATAPACK_ROOT;
   public static String GAME_SERVER_LOGIN_HOST;
   public static int GAME_SERVER_LOGIN_PORT;
   public static long GAME_SERVER_PING_DELAY;
   public static int GAME_SERVER_PING_RETRY;
   public static String DATABASE_DRIVER;
   public static int DATABASE_MAX_CONNECTIONS;
   public static int DATABASE_MAX_IDLE_TIMEOUT;
   public static int DATABASE_IDLE_TEST_PERIOD;
   public static String DATABASE_URL;
   public static String DATABASE_LOGIN;
   public static String DATABASE_PASSWORD;
   public static String DEFAULT_PASSWORD_HASH;
   public static String LEGACY_PASSWORD_HASH;
   public static int LOGIN_BLOWFISH_KEYS;
   public static int LOGIN_RSA_KEYPAIRS;
   public static boolean ACCEPT_NEW_GAMESERVER;
   public static boolean AUTO_CREATE_ACCOUNTS;
   public static String ANAME_TEMPLATE;
   public static String APASSWD_TEMPLATE;
   public static final Map<Integer, String> SERVER_NAMES = new HashMap<>();
   public static final long LOGIN_TIMEOUT = 60000L;
   public static int LOGIN_TRY_BEFORE_BAN;
   public static long LOGIN_TRY_TIMEOUT;
   public static long IP_BAN_TIME;
   private static ScrambledKeyPair[] _keyPairs;
   private static byte[][] _blowfishKeys;
   public static PasswordHash DEFAULT_CRYPT;
   public static PasswordHash[] LEGACY_CRYPT;
   public static boolean CHEAT_PASSWORD_CHECK;
   public static boolean ALLOW_ENCODE_PASSWORD;
   public static boolean SHOW_LICENCE;
   public static boolean LOGIN_SERVER_SCHEDULE_RESTART;
   public static long LOGIN_SERVER_SCHEDULE_RESTART_TIME;
   public static Config.ProxyServerConfig[] PROXY_SERVERS_CONFIGS;

   public static void load() {
      _log.info("Loading configs...");
      InputStream is = null;

      try {
         loadPersonalSettings(_personalConfigs);
         loadConfiguration(is);
         loadServerProxies();
         loadServerNames();
      } finally {
         try {
            if (is != null) {
               is.close();
            }
         } catch (Exception var7) {
         }
      }
   }

   public static final void initCrypt() throws Exception {
      DEFAULT_CRYPT = new PasswordHash(DEFAULT_PASSWORD_HASH);
      List<PasswordHash> legacy = new ArrayList<>();

      for(String method : LEGACY_PASSWORD_HASH.split(";")) {
         if (!method.equalsIgnoreCase(DEFAULT_PASSWORD_HASH)) {
            legacy.add(new PasswordHash(method));
         }
      }

      LEGACY_CRYPT = legacy.toArray(new PasswordHash[legacy.size()]);
      _log.info("Loaded " + DEFAULT_PASSWORD_HASH + " as default crypt.");
      _keyPairs = new ScrambledKeyPair[LOGIN_RSA_KEYPAIRS];
      KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
      RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
      keygen.initialize(spec);

      for(int i = 0; i < _keyPairs.length; ++i) {
         _keyPairs[i] = new ScrambledKeyPair(keygen.generateKeyPair());
      }

      _log.info("Cached " + _keyPairs.length + " KeyPairs for RSA communication...");
      _blowfishKeys = new byte[LOGIN_BLOWFISH_KEYS][16];

      for(int i = 0; i < _blowfishKeys.length; ++i) {
         for(int j = 0; j < _blowfishKeys[i].length; ++j) {
            _blowfishKeys[i][j] = (byte)(Rnd.get(255) + 1);
         }
      }

      _log.info("Restored " + _blowfishKeys.length + " keys for Blowfish communication...");
   }

   private static void loadPersonalSettings(HashMap<String, String> map) {
      map.clear();
      Pattern LINE_PATTERN = Pattern.compile("^(((?!=).)+)=(.*?)$");
      Scanner scanner = null;

      try {
         File file = new File("./config/network/personal.ini");
         String content = Files.readFile(file);
         scanner = new Scanner(content);

         while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!line.startsWith("#")) {
               Matcher m = LINE_PATTERN.matcher(line);
               if (m.find()) {
                  String name = m.group(1).replaceAll(" ", "");
                  String value = m.group(3).replaceAll(" ", "");
                  map.put(name, value);
               }
            }
         }
      } catch (IOException var16) {
         _log.warning("Config: " + var16.getMessage());
         throw new Error("Failed to Load ./config/network/personal.ini File.");
      } finally {
         try {
            scanner.close();
         } catch (Exception var15) {
         }
      }
   }

   public static final void loadServerNames() {
      SERVER_NAMES.clear();

      try {
         File file = new File("./config/servername.xml");
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         Document doc1 = factory.newDocumentBuilder().parse(file);

         for(Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
            if ("list".equalsIgnoreCase(n1.getNodeName())) {
               for(Node list = n1.getFirstChild(); list != null; list = list.getNextSibling()) {
                  if (list.getNodeName().equalsIgnoreCase("server")) {
                     Integer id = Integer.valueOf(list.getAttributes().getNamedItem("id").getNodeValue());
                     String name = list.getAttributes().getNamedItem("name").getNodeValue();
                     SERVER_NAMES.put(id, name);
                  }
               }
            }
         }

         _log.info("Loaded " + SERVER_NAMES.size() + " server name(s).");
      } catch (DOMException | ParserConfigurationException | SAXException | NumberFormatException var7) {
         _log.log(Level.WARNING, "servername.xml could not be initialized.", (Throwable)var7);
      } catch (IllegalArgumentException | IOException var8) {
         _log.log(Level.WARNING, "IOException or IllegalArgumentException.", (Throwable)var8);
      }
   }

   public static void loadConfiguration(InputStream is) {
      try {
         LoginSettings serverSettings = new LoginSettings();
         InputStream var5 = new FileInputStream(new File("./config/network/loginserver.ini"));
         serverSettings.load(var5);
         LOGIN_HOST = serverSettings.getProperty("LoginserverHostname", "127.0.0.1");
         PORT_LOGIN = Integer.parseInt(serverSettings.getProperty("LoginserverPort", "2106"));

         try {
            DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".").replaceAll("\\\\", "/")).getCanonicalFile();
         } catch (IOException var3) {
            _log.log(Level.WARNING, "Error setting datapack root!", (Throwable)var3);
            DATAPACK_ROOT = new File(".");
         }

         GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHost", "127.0.0.1");
         GAME_SERVER_LOGIN_PORT = Integer.parseInt(serverSettings.getProperty("LoginPort", "9014"));
         DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.cj.jdbc.Driver");
         DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2e?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC");
         DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
         DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
         DATABASE_MAX_CONNECTIONS = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "3"));
         DATABASE_MAX_IDLE_TIMEOUT = Integer.parseInt(serverSettings.getProperty("MaxIdleConnectionTimeout", "600"));
         DATABASE_IDLE_TEST_PERIOD = Integer.parseInt(serverSettings.getProperty("IdleConnectionTestPeriod", "60"));
         LOGIN_BLOWFISH_KEYS = Integer.parseInt(serverSettings.getProperty("BlowFishKeys", "20"));
         LOGIN_RSA_KEYPAIRS = Integer.parseInt(serverSettings.getProperty("RSAKeyPairs", "10"));
         ACCEPT_NEW_GAMESERVER = Boolean.parseBoolean(serverSettings.getProperty("AcceptNewGameServer", "true"));
         DEFAULT_PASSWORD_HASH = serverSettings.getProperty("PasswordHash", "whirlpool2");
         LEGACY_PASSWORD_HASH = serverSettings.getProperty("LegacyPasswordHash", "sha1");
         AUTO_CREATE_ACCOUNTS = Boolean.parseBoolean(serverSettings.getProperty("AutoCreateAccounts", "true"));
         ANAME_TEMPLATE = serverSettings.getProperty("AccountTemplate", "[A-Za-z0-9]{4,14}");
         APASSWD_TEMPLATE = serverSettings.getProperty("PasswordTemplate", "[A-Za-z0-9]{4,16}");
         LOGIN_TRY_BEFORE_BAN = Integer.parseInt(serverSettings.getProperty("LoginTryBeforeBan", "10"));
         LOGIN_TRY_TIMEOUT = Long.parseLong(serverSettings.getProperty("LoginTryTimeout", "5")) * 1000L;
         IP_BAN_TIME = Long.parseLong(serverSettings.getProperty("IpBanTime", "300")) * 1000L;
         GAME_SERVER_PING_DELAY = Long.parseLong(serverSettings.getProperty("GameServerPingDelay", "30")) * 1000L;
         GAME_SERVER_PING_RETRY = Integer.parseInt(serverSettings.getProperty("GameServerPingRetry", "4"));
         CHEAT_PASSWORD_CHECK = Boolean.parseBoolean(serverSettings.getProperty("CheatPasswordCheck", "false"));
         ALLOW_ENCODE_PASSWORD = Boolean.parseBoolean(serverSettings.getProperty("AllowEncodePasswords", "True"));
         SHOW_LICENCE = Boolean.parseBoolean(serverSettings.getProperty("ShowLicence", "true"));
         LOGIN_SERVER_SCHEDULE_RESTART = Boolean.parseBoolean(serverSettings.getProperty("LoginRestartSchedule", "False"));
         LOGIN_SERVER_SCHEDULE_RESTART_TIME = Long.parseLong(serverSettings.getProperty("LoginRestartTime", "24"));
      } catch (Exception var4) {
         _log.warning("Config: " + var4.getMessage());
         throw new Error("Failed to Load ./config/network/loginserver.iniFile.");
      }
   }

   public static ScrambledKeyPair getScrambledRSAKeyPair() {
      return _keyPairs[Rnd.get(_keyPairs.length)];
   }

   public static byte[] getBlowfishKey() {
      return _blowfishKeys[Rnd.get(_blowfishKeys.length)];
   }

   public static HashMap<String, String> getPersonalConfigs() {
      return _personalConfigs;
   }

   private static void loadServerProxies() {
      List<Config.ProxyServerConfig> proxyServersConfigs = new ArrayList<>();

      try {
         File file = new File("./config/proxyservers.xml");
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         Document doc1 = factory.newDocumentBuilder().parse(file);

         for(Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
            if ("list".equalsIgnoreCase(n1.getNodeName())) {
               for(Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling()) {
                  if ("proxyServer".equalsIgnoreCase(d1.getNodeName())) {
                     int origSrvId = Integer.parseInt(d1.getAttributes().getNamedItem("origId").getNodeValue());
                     int proxySrvId = Integer.parseInt(d1.getAttributes().getNamedItem("proxyId").getNodeValue());
                     String proxyHost = d1.getAttributes().getNamedItem("proxyHost").getNodeValue();
                     int proxyPort = Integer.parseInt(d1.getAttributes().getNamedItem("proxyPort").getNodeValue());
                     Config.ProxyServerConfig psc = new Config.ProxyServerConfig(origSrvId, proxySrvId, proxyHost, proxyPort);
                     proxyServersConfigs.add(psc);
                  }
               }
            }
         }
      } catch (Exception var11) {
         _log.log(Level.WARNING, "Can't load proxy server's config", (Throwable)var11);
      }

      PROXY_SERVERS_CONFIGS = proxyServersConfigs.toArray(new Config.ProxyServerConfig[proxyServersConfigs.size()]);
   }

   public static class ProxyServerConfig {
      private final int _origServerId;
      private final int _proxyServerId;
      private final String _porxyHost;
      private final int _proxyPort;

      public ProxyServerConfig(int origServerId, int proxyServerId, String porxyHost, int proxyPort) {
         this._origServerId = origServerId;
         this._proxyServerId = proxyServerId;
         this._porxyHost = porxyHost;
         this._proxyPort = proxyPort;
      }

      public int getOrigServerId() {
         return this._origServerId;
      }

      public int getProxyId() {
         return this._proxyServerId;
      }

      public String getPorxyHost() {
         return this._porxyHost;
      }

      public int getProxyPort() {
         return this._proxyPort;
      }
   }
}
