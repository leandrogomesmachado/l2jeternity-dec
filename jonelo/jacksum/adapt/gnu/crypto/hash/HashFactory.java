package jonelo.jacksum.adapt.gnu.crypto.hash;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import jonelo.jacksum.adapt.gnu.crypto.Registry;

public class HashFactory implements Registry {
   private HashFactory() {
   }

   public static IMessageDigest getInstance(String var0) {
      if (var0 == null) {
         return null;
      } else {
         var0 = var0.trim();
         Object var1 = null;
         if (var0.equalsIgnoreCase("whirlpool")) {
            var1 = new Whirlpool();
         } else if (var0.equalsIgnoreCase("whirlpool_2000")) {
            var1 = new Whirlpool2000();
         } else if (var0.equalsIgnoreCase("whirlpool_2003")) {
            var1 = new Whirlpool2003();
         } else if (var0.equalsIgnoreCase("ripemd128") || var0.equalsIgnoreCase("ripemd-128")) {
            var1 = new RipeMD128();
         } else if (var0.equalsIgnoreCase("ripemd160") || var0.equalsIgnoreCase("ripemd-160")) {
            var1 = new RipeMD160();
         } else if (var0.equalsIgnoreCase("sha-160") || var0.equalsIgnoreCase("sha-1") || var0.equalsIgnoreCase("sha1") || var0.equalsIgnoreCase("sha")) {
            var1 = new Sha160();
         } else if (var0.equalsIgnoreCase("sha-224")) {
            var1 = new Sha224();
         } else if (var0.equalsIgnoreCase("sha-384")) {
            var1 = new Sha384();
         } else if (var0.equalsIgnoreCase("sha-256")) {
            var1 = new Sha256();
         } else if (var0.equalsIgnoreCase("sha-512")) {
            var1 = new Sha512();
         } else if (var0.equalsIgnoreCase("tiger")) {
            var1 = new Tiger();
         } else if (var0.equalsIgnoreCase("tiger2")) {
            var1 = new Tiger2();
         } else if (var0.equalsIgnoreCase("tiger-160")) {
            var1 = new Tiger160();
         } else if (var0.equalsIgnoreCase("tiger-128")) {
            var1 = new Tiger128();
         } else if (var0.equalsIgnoreCase("haval")) {
            var1 = new Haval();
         } else if (var0.equalsIgnoreCase("md5")) {
            var1 = new MD5();
         } else if (var0.equalsIgnoreCase("md4")) {
            var1 = new MD4();
         } else if (var0.equalsIgnoreCase("md2")) {
            var1 = new MD2();
         } else if (var0.equalsIgnoreCase("haval")) {
            var1 = new Haval();
         } else if (var0.equalsIgnoreCase("haval_128_3")) {
            var1 = new Haval(16, 3);
         } else if (var0.equalsIgnoreCase("haval_128_4")) {
            var1 = new Haval(16, 4);
         } else if (var0.equalsIgnoreCase("haval_128_5")) {
            var1 = new Haval(16, 5);
         } else if (var0.equalsIgnoreCase("haval_160_3")) {
            var1 = new Haval(20, 3);
         } else if (var0.equalsIgnoreCase("haval_160_4")) {
            var1 = new Haval(20, 4);
         } else if (var0.equalsIgnoreCase("haval_160_5")) {
            var1 = new Haval(20, 5);
         } else if (var0.equalsIgnoreCase("haval_192_3")) {
            var1 = new Haval(24, 3);
         } else if (var0.equalsIgnoreCase("haval_192_4")) {
            var1 = new Haval(24, 4);
         } else if (var0.equalsIgnoreCase("haval_192_5")) {
            var1 = new Haval(24, 5);
         } else if (var0.equalsIgnoreCase("haval_224_3")) {
            var1 = new Haval(28, 3);
         } else if (var0.equalsIgnoreCase("haval_224_4")) {
            var1 = new Haval(28, 4);
         } else if (var0.equalsIgnoreCase("haval_224_5")) {
            var1 = new Haval(28, 5);
         } else if (var0.equalsIgnoreCase("haval_256_3")) {
            var1 = new Haval(32, 3);
         } else if (var0.equalsIgnoreCase("haval_256_4")) {
            var1 = new Haval(32, 4);
         } else if (var0.equalsIgnoreCase("haval_256_5")) {
            var1 = new Haval(32, 5);
         } else if (var0.equalsIgnoreCase("sha-0")) {
            var1 = new Sha0();
         } else if (var0.equalsIgnoreCase("has-160")) {
            var1 = new Has160();
         }

         return (IMessageDigest)var1;
      }
   }

   public static final Set getNames() {
      HashSet var0 = new HashSet();
      var0.add("whirlpool");
      var0.add("ripemd128");
      var0.add("ripemd160");
      var0.add("sha-160");
      var0.add("sha-224");
      var0.add("sha-256");
      var0.add("sha-384");
      var0.add("sha-512");
      var0.add("tiger");
      var0.add("haval");
      var0.add("md5");
      var0.add("md4");
      var0.add("md2");
      var0.add("sha-0");
      return Collections.unmodifiableSet(var0);
   }
}
