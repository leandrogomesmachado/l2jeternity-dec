package jonelo.jacksum.adapt.com.bitzi.util;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Vector;
import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;

public class TigerTree extends MessageDigest {
   private static final int BLOCKSIZE = 1024;
   private static final int HASHSIZE = 24;
   private final byte[] buffer = new byte[1024];
   private int bufferOffset = 0;
   private long byteCount = 0L;
   private AbstractChecksum tiger;
   private Vector nodes;

   public TigerTree(String var1) throws NoSuchAlgorithmException {
      super(var1);
      this.tiger = JacksumAPI.getChecksumInstance(var1);
      this.nodes = new Vector();
   }

   protected int engineGetDigestLength() {
      return 24;
   }

   protected void engineUpdate(byte var1) {
      ++this.byteCount;
      this.buffer[this.bufferOffset++] = var1;
      if (this.bufferOffset == 1024) {
         this.blockUpdate();
         this.bufferOffset = 0;
      }
   }

   protected void engineUpdate(byte[] var1, int var2, int var3) {
      int var4;
      for(this.byteCount += (long)var3; var3 >= (var4 = 1024 - this.bufferOffset); this.bufferOffset = 0) {
         System.arraycopy(var1, var2, this.buffer, this.bufferOffset, var4);
         this.bufferOffset += var4;
         this.blockUpdate();
         var3 -= var4;
         var2 += var4;
      }

      System.arraycopy(var1, var2, this.buffer, this.bufferOffset, var3);
      this.bufferOffset += var3;
   }

   protected byte[] engineDigest() {
      byte[] var1 = new byte[24];

      try {
         this.engineDigest(var1, 0, 24);
         return var1;
      } catch (DigestException var3) {
         return null;
      }
   }

   protected int engineDigest(byte[] var1, int var2, int var3) throws DigestException {
      if (var3 < 24) {
         throw new DigestException();
      } else {
         this.blockUpdate();

         while(this.nodes.size() > 1) {
            Vector var4 = new Vector();
            Enumeration var5 = this.nodes.elements();

            while(var5.hasMoreElements()) {
               byte[] var6 = (byte[])var5.nextElement();
               if (var5.hasMoreElements()) {
                  byte[] var7 = (byte[])var5.nextElement();
                  this.tiger.reset();
                  this.tiger.update((byte)1);
                  this.tiger.update(var6);
                  this.tiger.update(var7);
                  var4.addElement(this.tiger.getByteArray());
               } else {
                  var4.addElement(var6);
               }
            }

            this.nodes = var4;
         }

         System.arraycopy(this.nodes.elementAt(0), 0, var1, var2, 24);
         this.engineReset();
         return 24;
      }
   }

   protected void engineReset() {
      this.bufferOffset = 0;
      this.byteCount = 0L;
      this.nodes = new Vector();
      this.tiger.reset();
   }

   public Object clone() throws CloneNotSupportedException {
      throw new CloneNotSupportedException();
   }

   protected void blockUpdate() {
      this.tiger.reset();
      this.tiger.update((byte)0);
      this.tiger.update(this.buffer, 0, this.bufferOffset);
      if (this.bufferOffset != 0 || this.nodes.size() <= 0) {
         this.nodes.addElement(this.tiger.getByteArray());
      }
   }
}
