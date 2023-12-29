package l2e.commons.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPSubnet {
   final byte[] _addr;
   final byte[] _mask;
   final boolean _isIPv4;

   public IPSubnet(String input) throws UnknownHostException, NumberFormatException, ArrayIndexOutOfBoundsException {
      int idx = input.indexOf("/");
      if (idx > 0) {
         this._addr = InetAddress.getByName(input.substring(0, idx)).getAddress();
         this._mask = getMask(Integer.parseInt(input.substring(idx + 1)), this._addr.length);
         this._isIPv4 = this._addr.length == 4;
         if (!this.applyMask(this._addr)) {
            throw new UnknownHostException(input);
         }
      } else {
         this._addr = InetAddress.getByName(input).getAddress();
         this._mask = getMask(this._addr.length * 8, this._addr.length);
         this._isIPv4 = this._addr.length == 4;
      }
   }

   public IPSubnet(InetAddress addr, int mask) throws UnknownHostException {
      this._addr = addr.getAddress();
      this._isIPv4 = this._addr.length == 4;
      this._mask = getMask(mask, this._addr.length);
      if (!this.applyMask(this._addr)) {
         throw new UnknownHostException(addr.toString() + "/" + mask);
      }
   }

   public byte[] getAddress() {
      return this._addr;
   }

   public boolean applyMask(byte[] addr) {
      if (this._isIPv4 == (addr.length == 4)) {
         for(int i = 0; i < this._addr.length; ++i) {
            if ((addr[i] & this._mask[i]) != this._addr[i]) {
               return false;
            }
         }
      } else if (this._isIPv4) {
         for(int i = 0; i < this._addr.length; ++i) {
            if ((addr[i + 12] & this._mask[i]) != this._addr[i]) {
               return false;
            }
         }
      } else {
         for(int i = 0; i < this._addr.length; ++i) {
            if ((addr[i] & this._mask[i + 12]) != this._addr[i + 12]) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public String toString() {
      int size = 0;

      for(int i = 0; i < this._mask.length; ++i) {
         size += Integer.bitCount(this._mask[i] & 255);
      }

      try {
         return InetAddress.getByAddress(this._addr).toString() + "/" + size;
      } catch (UnknownHostException var3) {
         return "Invalid";
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o instanceof IPSubnet) {
         return this.applyMask(((IPSubnet)o).getAddress());
      } else {
         return o instanceof InetAddress ? this.applyMask(((InetAddress)o).getAddress()) : false;
      }
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
}
