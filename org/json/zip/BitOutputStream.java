package org.json.zip;

import java.io.IOException;
import java.io.OutputStream;

public class BitOutputStream implements BitWriter {
   private long nrBits = 0L;
   private OutputStream out;
   private int unwritten;
   private int vacant = 8;

   public BitOutputStream(OutputStream out) {
      this.out = out;
   }

   public long nrBits() {
      return this.nrBits;
   }

   @Override
   public void one() throws IOException {
      this.write(1, 1);
   }

   @Override
   public void pad(int width) throws IOException {
      int gap = (int)this.nrBits % width;
      if (gap < 0) {
         gap += width;
      }

      if (gap != 0) {
         for(int padding = width - gap; padding > 0; --padding) {
            this.zero();
         }
      }

      this.out.flush();
   }

   @Override
   public void write(int bits, int width) throws IOException {
      if (bits != 0 || width != 0) {
         if (width > 0 && width <= 32) {
            while(width > 0) {
               int actual = width;
               if (width > this.vacant) {
                  actual = this.vacant;
               }

               this.unwritten |= (bits >>> width - actual & (1 << actual) - 1) << this.vacant - actual;
               width -= actual;
               this.nrBits += (long)actual;
               this.vacant -= actual;
               if (this.vacant == 0) {
                  this.out.write(this.unwritten);
                  this.unwritten = 0;
                  this.vacant = 8;
               }
            }
         } else {
            throw new IOException("Bad write width.");
         }
      }
   }

   @Override
   public void zero() throws IOException {
      this.write(0, 1);
   }
}
