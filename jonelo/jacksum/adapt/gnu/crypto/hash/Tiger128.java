package jonelo.jacksum.adapt.gnu.crypto.hash;

import jonelo.jacksum.adapt.gnu.crypto.util.Util;

public class Tiger128 extends Tiger {
   private static final String DIGEST0 = "3293AC630C13F0245F92BBB1766E1616";

   public Tiger128() {
      this.name = "tiger-128";
   }

   private Tiger128(Tiger128 var1) {
      this();
      this.a = var1.a;
      this.b = var1.b;
      this.c = var1.c;
      this.count = var1.count;
      this.buffer = var1.buffer != null ? (byte[])var1.buffer.clone() : null;
   }

   public Object clone() {
      return new Tiger128(this);
   }

   public boolean selfTest() {
      if (valid == null) {
         valid = new Boolean("3293AC630C13F0245F92BBB1766E1616".equals(Util.toString(new Tiger128().digest())));
      }

      return valid;
   }

   protected byte[] getResult() {
      return new byte[]{
         (byte)((int)this.a),
         (byte)((int)(this.a >>> 8)),
         (byte)((int)(this.a >>> 16)),
         (byte)((int)(this.a >>> 24)),
         (byte)((int)(this.a >>> 32)),
         (byte)((int)(this.a >>> 40)),
         (byte)((int)(this.a >>> 48)),
         (byte)((int)(this.a >>> 56)),
         (byte)((int)this.b),
         (byte)((int)(this.b >>> 8)),
         (byte)((int)(this.b >>> 16)),
         (byte)((int)(this.b >>> 24)),
         (byte)((int)(this.b >>> 32)),
         (byte)((int)(this.b >>> 40)),
         (byte)((int)(this.b >>> 48)),
         (byte)((int)(this.b >>> 56))
      };
   }
}
