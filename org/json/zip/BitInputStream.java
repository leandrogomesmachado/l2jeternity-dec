package org.json.zip;

import java.io.IOException;
import java.io.InputStream;

public class BitInputStream implements BitReader {
   private int available = 0;
   private int unread = 0;
   private InputStream in;
   private long nrBits = 0L;

   public BitInputStream(InputStream in) {
      this.in = in;
   }

   @Override
   public boolean bit() throws IOException {
      return this.read(1) != 0;
   }

   @Override
   public long nrBits() {
      return this.nrBits;
   }

   @Override
   public boolean pad(int width) throws IOException {
      boolean result = true;
      int gap = (int)this.nrBits % width;
      if (gap < 0) {
         gap += width;
      }

      if (gap != 0) {
         for(int padding = width - gap; padding > 0; --padding) {
            if (this.bit()) {
               result = false;
            }
         }
      }

      return result;
   }

   @Override
   public int read(int width) throws IOException {
      if (width == 0) {
         return 0;
      } else if (width >= 0 && width <= 32) {
         int result;
         int take;
         for(result = 0; width > 0; width -= take) {
            if (this.available == 0) {
               this.unread = this.in.read();
               if (this.unread < 0) {
                  throw new IOException("Attempt to read past end.");
               }

               this.available = 8;
            }

            take = width;
            if (width > this.available) {
               take = this.available;
            }

            result |= (this.unread >>> this.available - take & (1 << take) - 1) << width - take;
            this.nrBits += (long)take;
            this.available -= take;
         }

         return result;
      } else {
         throw new IOException("Bad read width.");
      }
   }
}
