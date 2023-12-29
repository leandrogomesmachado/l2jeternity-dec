package info.tak11.subnet;

import info.tak11.subnet.util.ANDing;
import info.tak11.subnet.util.Conversion;
import java.util.HashMap;

public class Subnet {
   private String[] ip_blocks;
   private char net_class;
   private String ip_addr;
   private String subnet_addr;
   private String broadcast_addr;
   private int host_bits;
   private String sub_mask;
   private int sub_bits;
   private int total_subnets;
   private int masked_bits;
   private int total_hosts;
   private String min_host_range;
   private String max_host_range;
   private HashMap<Character, Integer> class_subnets = new HashMap<>();

   public void setSubnetMask(String ip) {
      this.sub_mask = ip;
      this.subnet_addr = ANDing.and(this.ip_addr, this.sub_mask);
      this.broadcast_addr = ANDing.broadcast(this.subnet_addr, this.sub_mask);
      this.calculateBitInformation(this.sub_mask, this.getNetworkClass());
      String[] minimumHostAddress = this.subnet_addr.split("[.]");
      minimumHostAddress[3] = Integer.toString(Integer.parseInt(minimumHostAddress[3]) + 1);
      this.min_host_range = minimumHostAddress[0] + "." + minimumHostAddress[1] + "." + minimumHostAddress[2] + "." + minimumHostAddress[3];
      String[] maximumHostAddress = this.broadcast_addr.split("[.]");
      maximumHostAddress[3] = Integer.toString(Integer.parseInt(maximumHostAddress[3]) - 1);
      this.max_host_range = maximumHostAddress[0] + "." + maximumHostAddress[1] + "." + maximumHostAddress[2] + "." + maximumHostAddress[3];
   }

   public void calculateBitInformation(String sub_mask, char network_class) {
      char[][] bin_sub_mask = Conversion.ipToBin(sub_mask);
      int num_bits = 0;

      for(int i = 0; i < 4; ++i) {
         for(int n = 0; n < 8; ++n) {
            if (bin_sub_mask[i][n] == '1') {
               ++num_bits;
            }
         }
      }

      int subnet_bits = num_bits - this.class_subnets.get(network_class);
      this.sub_bits = subnet_bits;
      this.masked_bits = num_bits;
      this.total_subnets = (int)Math.pow(2.0, (double)subnet_bits);
      int host_bits = 32 - num_bits;
      this.host_bits = host_bits;
      this.total_hosts = (int)Math.pow(2.0, (double)host_bits);
   }

   public void setSubnetBits(int subnetBits) {
      this.sub_bits = subnetBits;
      int bits = subnetBits + this.class_subnets.get(this.net_class);
      int re = 32 - bits;
      int mb = 32;
      String strt = "00000000000000000000000000000000";
      char[] b = strt.toCharArray();

      for(int n = 0; n <= re; ++n) {
         --mb;
      }

      for(int i = 0; i <= mb; ++i) {
         b[i] = '1';
      }

      String s = new String(b);
      char[][] ip = new char[][]{
         s.substring(0, 8).toCharArray(), s.substring(8, 16).toCharArray(), s.substring(16, 24).toCharArray(), s.substring(24, 32).toCharArray()
      };
      String mask = Integer.toString(Integer.parseInt(new String(ip[0]), 2))
         + "."
         + Integer.toString(Integer.parseInt(new String(ip[1]), 2))
         + "."
         + Integer.toString(Integer.parseInt(new String(ip[2]), 2))
         + "."
         + Integer.toString(Integer.parseInt(new String(ip[3]), 2));
      this.setSubnetMask(mask);
   }

   public void setIPAddress(String ip) {
      this.ip_addr = ip;
      this.ip_blocks = ip.split("[.]");
      int f = Integer.parseInt(this.ip_blocks[0]);
      if (f > 255) {
         System.err.print("Not a binary octet");
      } else {
         if (f <= 127) {
            this.net_class = 'a';
         }

         if (f <= 191 && f >= 128) {
            this.net_class = 'b';
         }

         if (f <= 223 && f >= 192) {
            this.net_class = 'c';
         }

         if (f <= 239 && f >= 224) {
            this.net_class = 'd';
         }

         if (f <= 255 && f >= 240) {
            this.net_class = 'e';
         }
      }

      this.class_subnets.put('a', 8);
      this.class_subnets.put('b', 16);
      this.class_subnets.put('c', 24);
      this.class_subnets.put('d', 3);
      this.class_subnets.put('e', 4);
   }

   public void setTotalSubnets(int totalSubnets) {
      this.total_subnets = totalSubnets;
      int subnetBits = (int)(Math.log((double)totalSubnets) / Math.log(2.0));
      this.setSubnetBits(subnetBits);
   }

   public void setTotalHosts(int totalHosts) {
      this.total_hosts = totalHosts;
      int hostBits = (int)(Math.log((double)totalHosts) / Math.log(2.0));
      int subnetBits = 32 - (hostBits + this.class_subnets.get(this.net_class));
      this.setSubnetBits(subnetBits);
   }

   public void setMaskedBits(int maskedBits) {
      this.masked_bits = maskedBits;
      int subnetBits = this.masked_bits - this.class_subnets.get(this.net_class);
      this.setSubnetBits(subnetBits);
   }

   public String getSubnetAddress() {
      return this.subnet_addr;
   }

   public char getNetworkClass() {
      return this.net_class;
   }

   public String getBroadcastAddress() {
      return this.broadcast_addr;
   }

   public int getSubnetBits() {
      return this.sub_bits;
   }

   public int getTotalSubnets() {
      return this.total_subnets;
   }

   public int getUsableSubnets() {
      return this.total_subnets - 2;
   }

   public int getMaskedBits() {
      return this.masked_bits;
   }

   public int getTotalHosts() {
      return this.total_hosts;
   }

   public int getUsableHosts() {
      return this.total_hosts - 2;
   }

   public String getMinimumHostAddressRange() {
      return this.min_host_range;
   }

   public String getMaximumHostAddressRange() {
      return this.max_host_range;
   }

   public String getSubnetMask() {
      return this.sub_mask;
   }

   public int getHostBits() {
      return this.host_bits;
   }

   public String getIPAddress() {
      return this.ip_addr;
   }
}
