package jonelo.jacksum.adapt.gnu.crypto.hash;

public abstract class BaseHash implements IMessageDigest {
   protected String name;
   protected int hashSize;
   protected int blockSize;
   protected long count;
   protected byte[] buffer;

   protected BaseHash(String var1, int var2, int var3) {
      this.name = var1;
      this.hashSize = var2;
      this.blockSize = var3;
      this.buffer = new byte[var3];
      this.resetContext();
   }

   public String name() {
      return this.name;
   }

   public int hashSize() {
      return this.hashSize;
   }

   public int blockSize() {
      return this.blockSize;
   }

   public void update(byte var1) {
      int var2 = (int)(this.count % (long)this.blockSize);
      ++this.count;
      this.buffer[var2] = var1;
      if (var2 == this.blockSize - 1) {
         this.transform(this.buffer, 0);
      }
   }

   public void update(byte[] var1, int var2, int var3) {
      int var4 = (int)(this.count % (long)this.blockSize);
      this.count += (long)var3;
      int var5 = this.blockSize - var4;
      int var6 = 0;
      if (var3 >= var5) {
         System.arraycopy(var1, var2, this.buffer, var4, var5);
         this.transform(this.buffer, 0);

         for(var6 = var5; var6 + this.blockSize - 1 < var3; var6 += this.blockSize) {
            this.transform(var1, var2 + var6);
         }

         var4 = 0;
      }

      if (var6 < var3) {
         System.arraycopy(var1, var2 + var6, this.buffer, var4, var3 - var6);
      }
   }

   public byte[] digest() {
      byte[] var1 = this.padBuffer();
      this.update(var1, 0, var1.length);
      byte[] var2 = this.getResult();
      this.reset();
      return var2;
   }

   public void reset() {
      this.count = 0L;
      int var1 = 0;

      while(var1 < this.blockSize) {
         this.buffer[var1++] = 0;
      }

      this.resetContext();
   }

   public abstract Object clone();

   public abstract boolean selfTest();

   protected abstract byte[] padBuffer();

   protected abstract byte[] getResult();

   protected abstract void resetContext();

   protected abstract void transform(byte[] var1, int var2);
}
