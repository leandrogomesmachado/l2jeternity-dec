package org;

import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.utils.Net;

public class HostInfo {
   private static final Logger _log = Logger.getLogger(HostInfo.class.getName());
   private final int _id;
   private final String _address;
   private final int _port;
   private final String _key;
   private final Map<Net, String> _subnets = new TreeMap<>();

   public HostInfo(int id, String address, int port, String key) {
      this._id = id;
      this._address = address;
      this._port = port;
      this._key = key;
   }

   public HostInfo(String address, int port) {
      this._id = 0;
      this._address = address;
      this._port = port;
      this._key = null;
   }

   public int getId() {
      return this._id;
   }

   public String getAddress() {
      return this._address;
   }

   public int getPort() {
      return this._port;
   }

   public String getKey() {
      return this._key;
   }

   public void addSubnet(String address, String subnet) {
      try {
         this._subnets.put(Net.valueOf(subnet), address);
      } catch (Exception var4) {
         _log.log(Level.SEVERE, "", (Throwable)var4);
      }
   }

   public void addSubnet(String address, byte[] subnetAddress, byte[] subnetMask) {
      try {
         this._subnets.put(Net.valueOf(subnetAddress, subnetMask), address);
      } catch (Exception var5) {
         _log.log(Level.SEVERE, "", (Throwable)var5);
      }
   }

   public Map<Net, String> getSubnets() {
      return this._subnets;
   }

   public String checkAddress(String address) {
      for(Entry<Net, String> m : this.getSubnets().entrySet()) {
         if (m.getKey().matches(address)) {
            return m.getValue();
         }
      }

      return this.getAddress();
   }
}
