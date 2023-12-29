package jonelo.jacksum.algorithm;

import java.security.NoSuchAlgorithmException;
import java.util.Vector;
import jonelo.jacksum.JacksumAPI;
import jonelo.sugar.util.EncodingException;
import jonelo.sugar.util.GeneralString;

public class CombinedChecksum extends AbstractChecksum {
   private Vector algorithms;

   public CombinedChecksum() {
      this.init();
   }

   public CombinedChecksum(String[] var1, boolean var2) throws NoSuchAlgorithmException {
      this.init();
      this.setAlgorithms(var1, var2);
   }

   private void init() {
      this.algorithms = new Vector();
      this.length = 0L;
      this.filename = null;
      this.separator = " ";
      this.encoding = "hex";
   }

   public void addAlgorithm(String var1, boolean var2) throws NoSuchAlgorithmException {
      AbstractChecksum var3 = JacksumAPI.getChecksumInstance(var1, var2);
      var3.setName(var1);
      this.algorithms.add(var3);
   }

   public void setAlgorithms(String[] var1, boolean var2) throws NoSuchAlgorithmException {
      for(int var3 = 0; var3 < var1.length; ++var3) {
         this.addAlgorithm(var1[var3], var2);
      }
   }

   public void reset() {
      for(int var1 = 0; var1 < this.algorithms.size(); ++var1) {
         ((AbstractChecksum)this.algorithms.elementAt(var1)).reset();
      }

      this.length = 0L;
   }

   public void update(int var1) {
      for(int var2 = 0; var2 < this.algorithms.size(); ++var2) {
         ((AbstractChecksum)this.algorithms.elementAt(var2)).update(var2);
      }

      ++this.length;
   }

   public void update(byte var1) {
      for(int var2 = 0; var2 < this.algorithms.size(); ++var2) {
         ((AbstractChecksum)this.algorithms.elementAt(var2)).update(var1);
      }

      ++this.length;
   }

   public void update(byte[] var1, int var2, int var3) {
      for(int var4 = 0; var4 < this.algorithms.size(); ++var4) {
         ((AbstractChecksum)this.algorithms.elementAt(var4)).update(var1, var2, var3);
      }

      this.length += (long)var3;
   }

   public void update(byte[] var1) {
      for(int var2 = 0; var2 < this.algorithms.size(); ++var2) {
         ((AbstractChecksum)this.algorithms.elementAt(var2)).update(var1);
      }

      this.length += (long)var1.length;
   }

   public byte[] getByteArray() {
      Vector var1 = new Vector();
      int var2 = 0;

      for(int var3 = 0; var3 < this.algorithms.size(); ++var3) {
         byte[] var4 = ((AbstractChecksum)this.algorithms.elementAt(var3)).getByteArray();
         var1.add(var4);
         var2 += var4.length;
      }

      byte[] var7 = new byte[var2];
      int var8 = 0;

      for(int var5 = 0; var5 < var1.size(); ++var5) {
         byte[] var6 = (byte[])var1.elementAt(var5);
         System.arraycopy(var6, 0, var7, var8, var6.length);
         var8 += var6.length;
      }

      return var7;
   }

   public void firstFormat(StringBuffer var1) {
      GeneralString.replaceAllStrings(var1, "#FINGERPRINT", "#CHECKSUM");
      this.setEncoding(this.encoding);
      StringBuffer var2 = new StringBuffer();
      String var3 = var1.toString();
      if (var3.indexOf("#CHECKSUM{i}") <= -1 && var3.indexOf("#ALGONAME{i}") <= -1) {
         var2.append(var3);
      } else {
         for(int var4 = 0; var4 < this.algorithms.size(); ++var4) {
            StringBuffer var5 = new StringBuffer(var3);
            GeneralString.replaceAllStrings(var5, "#CHECKSUM{i}", ((AbstractChecksum)this.algorithms.elementAt(var4)).getFormattedValue());
            GeneralString.replaceAllStrings(var5, "#ALGONAME{i}", ((AbstractChecksum)this.algorithms.elementAt(var4)).getName());
            var2.append(var5);
            if (this.algorithms.size() > 1) {
               var2.append("\n");
            }
         }
      }

      if (var2.toString().indexOf("#CHECKSUM{") > -1) {
         for(int var6 = 0; var6 < this.algorithms.size(); ++var6) {
            GeneralString.replaceAllStrings(var2, "#CHECKSUM{" + var6 + "}", ((AbstractChecksum)this.algorithms.elementAt(var6)).getFormattedValue());
         }
      }

      if (var2.toString().indexOf("#ALGONAME{") > -1) {
         for(int var7 = 0; var7 < this.algorithms.size(); ++var7) {
            GeneralString.replaceAllStrings(var2, "#ALGONAME{" + var7 + "}", ((AbstractChecksum)this.algorithms.elementAt(var7)).getName());
         }
      }

      var1.setLength(0);
      var1.append(var2.toString());
   }

   public void setEncoding(String var1) throws EncodingException {
      for(int var2 = 0; var2 < this.algorithms.size(); ++var2) {
         ((AbstractChecksum)this.algorithms.elementAt(var2)).setEncoding(var1);
      }

      this.encoding = ((AbstractChecksum)this.algorithms.elementAt(0)).getEncoding();
   }
}
