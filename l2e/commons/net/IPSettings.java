package l2e.commons.net;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.File;
import java.util.logging.Level;
import l2e.gameserver.Config;
import l2e.gameserver.data.DocumentParser;
import org.HostInfo;
import org.w3c.dom.Node;

public class IPSettings extends DocumentParser {
   private HostInfo _authServerHost;
   private final TIntObjectMap<HostInfo> _gameServerHosts = new TIntObjectHashMap<>();

   public IPSettings() {
      this.load();
   }

   @Override
   public void load() {
      File f = new File("./config/ipconfig.xml");
      if (f.exists()) {
         this._log.log(Level.INFO, "Network Config: ipconfig.xml exists using manual configuration...");
         this.parseFile(new File("./config/ipconfig.xml"), false);
      } else {
         this.autoIpConfig();
      }
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
               if ("authserver".equalsIgnoreCase(c.getNodeName())) {
                  String address = c.getAttributes().getNamedItem("address").getNodeValue();
                  int port = Integer.parseInt(c.getAttributes().getNamedItem("port").getNodeValue());
                  this.setAuthServerHost(new HostInfo(address, port));
               } else if ("gameserver".equalsIgnoreCase(c.getNodeName())) {
                  for(Node d = c.getFirstChild(); d != null; d = d.getNextSibling()) {
                     if ("host".equalsIgnoreCase(d.getNodeName())) {
                        int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
                        Config.REQUEST_ID = id;
                        String address = d.getAttributes().getNamedItem("address") != null
                           ? d.getAttributes().getNamedItem("address").getNodeValue()
                           : "127.0.0.1";
                        int port = Integer.parseInt(d.getAttributes().getNamedItem("port").getNodeValue());
                        String key = d.getAttributes().getNamedItem("key").getNodeValue();
                        HostInfo hostInfo = new HostInfo(id, address, port, key);

                        for(Node s = d.getFirstChild(); s != null; s = s.getNextSibling()) {
                           if ("advanced".equalsIgnoreCase(s.getNodeName())) {
                              String advanced_address = s.getAttributes().getNamedItem("address").getNodeValue();
                              String advanced_subnet = s.getAttributes().getNamedItem("subnet").getNodeValue();
                              hostInfo.addSubnet(advanced_address, advanced_subnet);
                           }
                        }

                        this.addGameServerHost(hostInfo);
                     }
                  }
               }
            }
         }
      }
   }

   public void autoIpConfig() {
      this._gameServerHosts.clear();

      try {
         HostInfo hostInfo = new HostInfo(1, "127.0.0.1", 7777, "ENTER_RANDOM_KEY");
         Config.REQUEST_ID = hostInfo.getId();
         hostInfo.addSubnet("127.0.0.1", "127.0.0.0/8");
         hostInfo.addSubnet("10.0.0.0", "10.0.0.0/8");
         hostInfo.addSubnet("172.16.0.0", "172.16.0.0/12");
         hostInfo.addSubnet("192.168.0.0", "192.168.0.0/16");
         hostInfo.addSubnet("169.254.0.0", "169.254.0.0/16");
         this.addGameServerHost(hostInfo);
      } catch (Exception var2) {
      }

      Config.isActivate = false;
   }

   public void setAuthServerHost(HostInfo host) {
      this._authServerHost = host;
   }

   public HostInfo getAuthServerHost() {
      return this._authServerHost;
   }

   public void addGameServerHost(HostInfo host) {
      if (!this._gameServerHosts.containsKey(host.getId())) {
         if (this._gameServerHosts.isEmpty()) {
            Config.REQUEST_ID = host.getId();
            Config.EXTERNAL_HOSTNAME = host.getAddress();
            if (host.getAddress().equalsIgnoreCase("*")) {
               int i = 1;

               for(String ip : host.getSubnets().values()) {
                  if (i == host.getSubnets().size()) {
                     Config.EXTERNAL_HOSTNAME = ip;
                  } else {
                     ++i;
                  }
               }
            }

            Config.PORT_GAME = host.getPort();
         }

         this._gameServerHosts.put(host.getId(), host);
      }
   }

   public HostInfo[] getGameServerHosts() {
      return this._gameServerHosts.values(new HostInfo[this._gameServerHosts.size()]);
   }

   public static IPSettings getInstance() {
      return IPSettings.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final IPSettings _instance = new IPSettings();
   }
}
