package com.mchange.v2.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class LocalHostManager {
   Set localAddresses;
   Set knownGoodNames;
   Set knownBadNames;

   public synchronized void update() throws SocketException {
      HashSet var1 = new HashSet();
      Enumeration var2 = NetworkInterface.getNetworkInterfaces();

      while(var2.hasMoreElements()) {
         NetworkInterface var3 = (NetworkInterface)var2.nextElement();
         Enumeration var4 = var3.getInetAddresses();

         while(var4.hasMoreElements()) {
            var1.add(var4.nextElement());
         }
      }

      this.localAddresses = Collections.unmodifiableSet(var1);
      this.knownGoodNames = new HashSet();
      this.knownBadNames = new HashSet();
   }

   public synchronized Set getLocalAddresses() {
      return this.localAddresses;
   }

   public synchronized boolean isLocalAddress(InetAddress var1) {
      return this.localAddresses.contains(var1);
   }

   public synchronized boolean isLocalHostName(String var1) {
      if (this.knownGoodNames.contains(var1)) {
         return true;
      } else if (this.knownGoodNames.contains(var1)) {
         return false;
      } else {
         try {
            InetAddress var2 = InetAddress.getByName(var1);
            if (this.localAddresses.contains(var2)) {
               this.knownGoodNames.add(var1);
               return true;
            } else {
               this.knownBadNames.add(var1);
               return false;
            }
         } catch (UnknownHostException var3) {
            this.knownBadNames.add(var1);
            return false;
         }
      }
   }

   public LocalHostManager() throws SocketException {
      this.update();
   }

   public static void main(String[] var0) {
      try {
         LocalHostManager var1 = new LocalHostManager();
         System.out.println(var1.getLocalAddresses());
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }
}
