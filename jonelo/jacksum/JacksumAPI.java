package jonelo.jacksum;

import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import jonelo.jacksum.algorithm.AbstractChecksum;
import jonelo.jacksum.algorithm.Adler32;
import jonelo.jacksum.algorithm.Adler32alt;
import jonelo.jacksum.algorithm.Cksum;
import jonelo.jacksum.algorithm.CombinedChecksum;
import jonelo.jacksum.algorithm.Crc16;
import jonelo.jacksum.algorithm.Crc32;
import jonelo.jacksum.algorithm.Crc32Mpeg2;
import jonelo.jacksum.algorithm.Crc64;
import jonelo.jacksum.algorithm.Crc8;
import jonelo.jacksum.algorithm.CrcGeneric;
import jonelo.jacksum.algorithm.Edonkey;
import jonelo.jacksum.algorithm.Elf;
import jonelo.jacksum.algorithm.FCS16;
import jonelo.jacksum.algorithm.FCS32;
import jonelo.jacksum.algorithm.MD;
import jonelo.jacksum.algorithm.MDTree;
import jonelo.jacksum.algorithm.MDbouncycastle;
import jonelo.jacksum.algorithm.MDgnu;
import jonelo.jacksum.algorithm.None;
import jonelo.jacksum.algorithm.Read;
import jonelo.jacksum.algorithm.Sum16;
import jonelo.jacksum.algorithm.Sum24;
import jonelo.jacksum.algorithm.Sum32;
import jonelo.jacksum.algorithm.Sum8;
import jonelo.jacksum.algorithm.SumBSD;
import jonelo.jacksum.algorithm.SumSysV;
import jonelo.jacksum.algorithm.Xor8;
import jonelo.sugar.util.GeneralProgram;
import jonelo.sugar.util.GeneralString;
import jonelo.sugar.util.Version;

public class JacksumAPI {
   public static final String NAME = "Jacksum";
   public static final String VERSION = "1.7.0";

   public static final Version getVersion() {
      return new Version("1.7.0");
   }

   public static final String getVersionString() {
      return "1.7.0";
   }

   public static final String getName() {
      return "Jacksum";
   }

   public static void runCLI(String[] var0) {
      jonelo.jacksum.cli.Jacksum.main(var0);
   }

   public static Map getAvailableEncodings() {
      TreeMap var0 = new TreeMap();
      var0.put("", "Default");
      var0.put("bin", "Binary");
      var0.put("dec", "Decimal");
      var0.put("oct", "Octal");
      var0.put("hex", "Hexadecimal (lowercase)");
      var0.put("hexup", "Hexadecimal (uppercase)");
      var0.put("base16", "Base 16");
      var0.put("base32", "Base 32");
      var0.put("base64", "Base 64");
      var0.put("bubblebabble", "BubbleBabble");
      return var0;
   }

   public static Map getAvailableAlgorithms() {
      TreeMap var0 = new TreeMap();
      var0.put("adler32", "Adler 32");
      var0.put("cksum", "cksum (Unix)");
      var0.put("crc8", "CRC-8 (FLAC)");
      var0.put("crc16", "CRC-16 (LHA/ARC)");
      var0.put("crc24", "CRC-24 (Open PGP)");
      var0.put("crc64", "CRC-64 (ISO 3309)");
      var0.put("crc32", "CRC-32 (FCS-32)");
      var0.put("crc32_mpeg2", "CRC-32 (MPEG-2)");
      var0.put("crc32_bzip2", "CRC-32 (BZIP2)");
      var0.put("ed2k", "ed2k");
      var0.put("elf", "Elf");
      var0.put("fcs16", "FCS-16");
      var0.put("gost", "GOST (R 34.11-94)");
      var0.put("has160", "HAS-160");
      var0.put("haval_128_3", "HAVAL 128 (3 rounds)");
      var0.put("haval_128_4", "HAVAL 128 (4 rounds)");
      var0.put("haval_128_5", "HAVAL 128 (5 rounds)");
      var0.put("haval_160_3", "HAVAL 160 (3 rounds)");
      var0.put("haval_160_4", "HAVAL 160 (4 rounds)");
      var0.put("haval_160_5", "HAVAL 160 (5 rounds)");
      var0.put("haval_192_3", "HAVAL 192 (3 rounds)");
      var0.put("haval_192_4", "HAVAL 192 (4 rounds)");
      var0.put("haval_192_5", "HAVAL 192 (5 rounds)");
      var0.put("haval_224_3", "HAVAL 224 (3 rounds)");
      var0.put("haval_224_4", "HAVAL 224 (4 rounds)");
      var0.put("haval_224_5", "HAVAL 224 (5 rounds)");
      var0.put("haval_256_3", "HAVAL 256 (3 rounds)");
      var0.put("haval_256_4", "HAVAL 256 (4 rounds)");
      var0.put("haval_256_5", "HAVAL 256 (5 rounds)");
      var0.put("md2", "MD2");
      var0.put("md4", "MD4");
      var0.put("md5", "MD5");
      var0.put("ripemd128", "RIPEMD-128");
      var0.put("ripemd160", "RIPEMD-160");
      var0.put("ripemd256", "RIPEMD-256");
      var0.put("ripemd320", "RIPEMD-320");
      var0.put("sha0", "SHA-0");
      var0.put("sha1", "SHA-1 (SHA-160)");
      var0.put("sha224", "SHA-2 (SHA-224)");
      var0.put("sha256", "SHA-2 (SHA-256)");
      var0.put("sha384", "SHA-2 (SHA-384)");
      var0.put("sha512", "SHA-2 (SHA-512)");
      var0.put("sumbsd", "sum (BSD Unix)");
      var0.put("sumsysv", "sum (System V Unix)");
      var0.put("sum8", "sum 8");
      var0.put("sum16", "sum 16");
      var0.put("sum24", "sum 24");
      var0.put("sum32", "sum 32");
      var0.put("tiger128", "Tiger/128");
      var0.put("tiger160", "Tiger/160");
      var0.put("tiger", "Tiger (Tiger/192)");
      var0.put("tiger2", "Tiger2");
      var0.put("tree:tiger", "Tiger Tree Hash");
      var0.put("tree:tiger2", "Tiger2 Tree Hash");
      var0.put("whirlpool0", "Whirlpool-0");
      var0.put("whirlpool1", "Whirlpool-1");
      var0.put("whirlpool2", "Whirlpool");
      var0.put("xor8", "XOR 8");
      return var0;
   }

   public static AbstractChecksum getChecksumInstance(String var0) throws NoSuchAlgorithmException {
      return getChecksumInstance(var0, false);
   }

   public static AbstractChecksum getChecksumInstance(String var0, boolean var1) throws NoSuchAlgorithmException {
      Object var2 = null;
      if (var0.indexOf("+") > -1) {
         String[] var3 = GeneralString.split(var0, "+");
         var2 = new CombinedChecksum(var3, var1);
      } else if (!var0.equals("sha1") && !var0.equals("sha") && !var0.equals("sha-1") && !var0.equals("sha160") && !var0.equals("sha-160")) {
         if (!var0.equals("crc32") && !var0.equals("crc-32") && !var0.equals("fcs32") && !var0.equals("fcs-32")) {
            if (!var0.equals("md5") && !var0.equals("md5sum")) {
               if (var0.equals("cksum")) {
                  var2 = new Cksum();
               } else if (var0.equals("sumbsd") || var0.equals("bsd") || var0.equals("bsdsum")) {
                  var2 = new SumBSD();
               } else if (var0.equals("sumsysv") || var0.equals("sysv") || var0.equals("sysvsum")) {
                  var2 = new SumSysV();
               } else if (!var0.equals("adler32") && !var0.equals("adler-32")) {
                  if (var0.equals("crc32_mpeg2") || var0.equals("crc-32_mpeg-2")) {
                     var2 = new Crc32Mpeg2();
                  } else if (!var0.equals("sha256") && !var0.equals("sha-256")) {
                     if (!var0.equals("sha384") && !var0.equals("sha-384")) {
                        if (!var0.equals("sha512") && !var0.equals("sha-512")) {
                           if (var0.equals("sha224") || var0.equals("sha-224")) {
                              var2 = new MDgnu("sha-224");
                           } else if (var0.equals("tiger") || var0.equals("tiger192") || var0.equals("tiger-192")) {
                              var2 = new MDgnu("tiger");
                           } else if (var0.equals("tree:tiger")) {
                              var2 = new MDTree("tiger");
                           } else if (var0.equals("tree:tiger2")) {
                              var2 = new MDTree("tiger2");
                           } else if (var0.equals("tiger160") || var0.equals("tiger-160")) {
                              var2 = new MDgnu("tiger-160");
                           } else if (var0.equals("tiger128") || var0.equals("tiger-128")) {
                              var2 = new MDgnu("tiger-128");
                           } else if (var0.equals("tiger2")) {
                              var2 = new MDgnu("tiger2");
                           } else if (var0.startsWith("haval")) {
                              var2 = new MDgnu(var0);
                           } else if (var0.equals("crc16") || var0.equals("crc-16")) {
                              var2 = new Crc16();
                           } else if (var0.equals("ripemd160")
                              || var0.equals("ripemd-160")
                              || var0.equals("ripe-md160")
                              || var0.equals("rmd160")
                              || var0.equals("rmd-160")) {
                              var2 = new MDgnu("ripemd160");
                           } else if (var0.equals("ripemd128")
                              || var0.equals("ripemd-128")
                              || var0.equals("ripe-md128")
                              || var0.equals("rmd128")
                              || var0.equals("rmd-128")) {
                              var2 = new MDgnu("ripemd128");
                           } else if (var0.equals("ripemd256")
                              || var0.equals("ripemd-256")
                              || var0.equals("ripe-md256")
                              || var0.equals("rmd256")
                              || var0.equals("rmd-256")) {
                              var2 = new MDbouncycastle("ripemd256");
                           } else if (var0.equals("ripemd320")
                              || var0.equals("ripemd-320")
                              || var0.equals("ripe-md320")
                              || var0.equals("rmd320")
                              || var0.equals("rmd-320")) {
                              var2 = new MDbouncycastle("ripemd320");
                           } else if (var0.equals("whirlpool0") || var0.equals("whirlpool-0")) {
                              var2 = new MDgnu("whirlpool_2000");
                           } else if (var0.equals("whirlpool1") || var0.equals("whirlpool-1")) {
                              var2 = new MDgnu("whirlpool");
                           } else if (var0.equals("whirlpool2") || var0.equals("whirlpool-2") || var0.equals("whirlpool")) {
                              var2 = new MDgnu("whirlpool_2003");
                           } else if (var0.equals("crc64") || var0.equals("crc-64")) {
                              var2 = new Crc64();
                           } else if (var0.equals("ed2k") || var0.equals("emule") || var0.equals("edonkey")) {
                              var2 = new Edonkey();
                           } else if (var0.equals("md4") || var0.equals("md4sum")) {
                              var2 = new MDgnu("md4");
                           } else if (var0.equals("md2") || var0.equals("md2sum")) {
                              var2 = new MDgnu("md2");
                           } else if (var0.equals("sha0") || var0.equals("sha-0")) {
                              var2 = new MDgnu("sha-0");
                           } else if (var0.equals("elf") || var0.equals("elf32") || var0.equals("elf-32")) {
                              var2 = new Elf();
                           } else if (var0.equals("fcs16") || var0.equals("fcs-16") || var0.equals("crc16_x25") || var0.equals("crc-16_x-25")) {
                              var2 = new FCS16();
                           } else if (var0.equals("crc8") || var0.equals("crc-8")) {
                              var2 = new Crc8();
                           } else if (var0.equals("crc24") || var0.equals("crc-24")) {
                              var2 = new CrcGeneric(24, 8801531L, 11994318L, false, false, 0L);
                           } else if (var0.equals("sum8") || var0.equals("sum-8")) {
                              var2 = new Sum8();
                           } else if (var0.equals("sum16") || var0.equals("sum-16")) {
                              var2 = new Sum16();
                           } else if (var0.equals("sum24") || var0.equals("sum-24")) {
                              var2 = new Sum24();
                           } else if (var0.equals("sum32") || var0.equals("sum-32")) {
                              var2 = new Sum32();
                           } else if (var0.equals("xor8") || var0.equals("xor-8")) {
                              var2 = new Xor8();
                           } else if (var0.equals("gost")) {
                              var2 = new MDbouncycastle("gost");
                           } else if (var0.equals("crc32_bzip2") || var0.equals("crc-32_bzip-2")) {
                              var2 = new CrcGeneric(32, 79764919L, 4294967295L, false, false, 4294967295L);
                           } else if (var0.equals("has160") || var0.equals("has-160")) {
                              var2 = new MDgnu("has-160");
                           } else if (var0.equals("none")) {
                              var2 = new None();
                           } else if (var0.equals("read")) {
                              var2 = new Read();
                           } else if (var0.startsWith("crc:")) {
                              var2 = new CrcGeneric(var0.substring(4));
                           } else {
                              if (!var0.equals("all")) {
                                 throw new NoSuchAlgorithmException(var0 + " is an unknown algorithm.");
                              }

                              Map var11 = getAvailableAlgorithms();
                              Iterator var4 = var11.entrySet().iterator();
                              String[] var5 = new String[var11.entrySet().size()];
                              int var6 = 0;

                              StringBuffer var7;
                              String var9;
                              for(var7 = new StringBuffer(); var4.hasNext(); var5[var6++] = var9) {
                                 Entry var8 = (Entry)var4.next();
                                 var9 = (String)var8.getKey();
                                 var7.append(var9);
                                 var7.append("+");
                              }

                              var2 = new CombinedChecksum(var5, var1);
                              var7.deleteCharAt(var7.length() - 1);
                              var0 = var7.toString();
                           }
                        } else if (var1) {
                           var2 = new MDgnu("sha-512");
                        } else if (GeneralProgram.isSupportFor("1.4.2")) {
                           var2 = new MD("SHA-512");
                        } else {
                           var2 = new MDgnu("sha-512");
                        }
                     } else if (var1) {
                        var2 = new MDgnu("sha-384");
                     } else if (GeneralProgram.isSupportFor("1.4.2")) {
                        var2 = new MD("SHA-384");
                     } else {
                        var2 = new MDgnu("sha-384");
                     }
                  } else if (var1) {
                     var2 = new MDgnu("sha-256");
                  } else if (GeneralProgram.isSupportFor("1.4.2")) {
                     var2 = new MD("SHA-256");
                  } else {
                     var2 = new MDgnu("sha-256");
                  }
               } else if (var1) {
                  var2 = new Adler32alt();
               } else {
                  var2 = new Adler32();
               }
            } else if (var1) {
               var2 = new MDgnu("md5");
            } else {
               var2 = new MD("MD5");
            }
         } else if (var1) {
            var2 = new FCS32();
         } else {
            var2 = new Crc32();
         }
      } else if (var1) {
         var2 = new MDgnu("sha-160");
      } else {
         var2 = new MD("SHA-1");
      }

      ((AbstractChecksum)var2).setName(var0);
      return (AbstractChecksum)var2;
   }
}
