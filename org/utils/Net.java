package org.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Net implements Comparable<Net> {
   private final byte[] _address;
   private final byte[] _mask;
   private final boolean _isIPv4;

   private Net(byte[] address, byte[] mask) {
      this._address = address;
      this._mask = mask;
      this._isIPv4 = this._address.length == 4;
   }

   public byte[] getAddress() {
      return this._address;
   }

   public byte[] getMask() {
      return this._mask;
   }

   public boolean applyMask(byte[] addr) {
      if (this._isIPv4 == (addr.length == 4)) {
         for(int i = 0; i < this._address.length; ++i) {
            if ((addr[i] & this._mask[i]) != this._address[i]) {
               return false;
            }
         }
      } else if (this._isIPv4) {
         for(int i = 0; i < this._address.length; ++i) {
            if ((addr[i + 12] & this._mask[i]) != this._address[i]) {
               return false;
            }
         }
      } else {
         for(int i = 0; i < this._address.length; ++i) {
            if ((addr[i] & this._mask[i + 12]) != this._address[i + 12]) {
               return false;
            }
         }
      }

      return true;
   }

   public boolean matches(Object o) {
      return this.equals(o);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o instanceof Net) {
         return this.applyMask(((Net)o).getAddress());
      } else if (o instanceof InetAddress) {
         return this.applyMask(((InetAddress)o).getAddress());
      } else {
         if (o instanceof String) {
            try {
               return this.applyMask(InetAddress.getByName((String)o).getAddress());
            } catch (UnknownHostException var3) {
            }
         }

         return false;
      }
   }

   @Override
   public int hashCode() {
      return this._address.hashCode() + this._mask.hashCode() * 13;
   }

   @Override
   public String toString() {
      int size = 0;

      for(byte element : this._mask) {
         size += Integer.bitCount(element & 255);
      }

      try {
         return InetAddress.getByAddress(this._address).toString() + "/" + size;
      } catch (UnknownHostException var6) {
         return "Invalid";
      }
   }

   public int compareTo(Net o) {
      long m1 = parseLong(this.getMask());
      long m2 = parseLong(o.getMask());
      if (m1 == m2) {
         long a1 = parseLong(this.getAddress());
         long a2 = parseLong(o.getAddress());
         return Long.compare(a1, a2);
      } else {
         return Long.compare(m1, m2);
      }
   }

   public static long parseLong(byte[] bytes) {
      long result = 0L;

      for(byte b : bytes) {
         result = result * 256L + (long)b;
      }

      return result;
   }

   private static final byte[] getMask(int n, int maxLength) throws UnknownHostException {
      if (n <= maxLength << 3 && n >= 0) {
         byte[] result = new byte[maxLength];

         for(int i = 0; i < maxLength; ++i) {
            result[i] = -1;
         }

         for(int i = (maxLength << 3) - 1; i >= n; --i) {
            result[i >> 3] = (byte)(result[i >> 3] << 1);
         }

         return result;
      } else {
         throw new UnknownHostException("Invalid netmask: " + n);
      }
   }

   public static Net valueOf(String input) throws UnknownHostException {
      int idx = input.indexOf("/");
      byte[] address;
      byte[] mask;
      if (idx > 0) {
         address = InetAddress.getByName(input.substring(0, idx)).getAddress();
         mask = getMask(Integer.parseInt(input.substring(idx + 1)), address.length);
      } else {
         address = InetAddress.getByName(input).getAddress();
         mask = getMask(address.length * 8, address.length);
      }

      Net net = new Net(address, mask);
      if (!net.applyMask(address)) {
         throw new UnknownHostException(input);
      } else {
         return net;
      }
   }

   public static Net valueOf(byte[] address, byte[] mask) throws UnknownHostException {
      Net net = new Net(address, mask);
      if (!net.applyMask(address)) {
         throw new UnknownHostException(net.toString());
      } else {
         return net;
      }
   }

   public static Net valueOf(InetAddress addr, int mask) throws UnknownHostException {
      byte[] address = addr.getAddress();
      Net net = new Net(address, getMask(mask, address.length));
      if (!net.applyMask(address)) {
         throw new UnknownHostException(net.toString());
      } else {
         return net;
      }
   }
}
