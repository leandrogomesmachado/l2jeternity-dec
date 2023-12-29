package jonelo.sugar.util;

import java.util.StringTokenizer;

public class Version implements Comparable {
   private int major;
   private int sub;
   private int minor;

   public Version(int var1, int var2) {
      this(var1, var2, 0);
   }

   public Version(int var1, int var2, int var3) {
      this.major = var1;
      this.sub = var2;
      this.minor = var3;
   }

   public Version(String var1) {
      this.major = 0;
      this.sub = 0;
      this.minor = 0;
      StringTokenizer var2 = new StringTokenizer(var1, ".");
      if (var2.hasMoreTokens()) {
         this.major = Integer.parseInt(var2.nextToken());
      }

      if (var2.hasMoreTokens()) {
         this.sub = Integer.parseInt(var2.nextToken());
      }

      if (var2.hasMoreTokens()) {
         this.minor = Integer.parseInt(var2.nextToken());
      }
   }

   public String toString() {
      StringBuffer var1 = new StringBuffer(8);
      var1.append(this.major);
      var1.append('.');
      var1.append(this.sub);
      var1.append('.');
      var1.append(this.minor);
      return var1.toString();
   }

   public int getMajor() {
      return this.major;
   }

   public int getSub() {
      return this.sub;
   }

   public int getMinor() {
      return this.minor;
   }

   public int compareTo(Object var1) {
      Version var2 = (Version)var1;
      if (this.equals(var2)) {
         return 0;
      } else {
         return this.major <= var2.getMajor()
               && (this.major != var2.getMajor() || this.sub <= var2.getSub())
               && (this.major != var2.getMajor() || this.sub != var2.getSub() || this.minor <= var2.getMinor())
            ? -1
            : 1;
      }
   }

   public boolean equals(Object var1) {
      if (!(var1 instanceof Version)) {
         return false;
      } else {
         Version var2 = (Version)var1;
         return this.major == var2.getMajor() && this.sub == var2.getSub() && this.minor == var2.getMinor();
      }
   }

   public int hashCode() {
      return this.major * 10000 + this.sub * 100 + this.minor;
   }
}
