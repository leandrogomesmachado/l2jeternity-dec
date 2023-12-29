package jonelo.jacksum.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;
import jonelo.jacksum.ui.CheckFile;
import jonelo.jacksum.ui.MetaInfo;
import jonelo.jacksum.ui.MetaInfoVersionException;
import jonelo.jacksum.ui.Summary;
import jonelo.jacksum.ui.Verbose;
import jonelo.jacksum.util.Service;
import jonelo.sugar.util.EncodingException;
import jonelo.sugar.util.ExitException;
import jonelo.sugar.util.GeneralProgram;
import jonelo.sugar.util.GeneralString;

public class Jacksum {
   public static final String DEFAULT = "default";
   public static final String TIMESTAMPFORMAT_DEFAULT = "yyyyMMddHHmmss";
   private AbstractChecksum checksum = null;
   private String checksumArg = null;
   private String expected = null;
   private String format = null;
   private String outputFile = null;
   private String errorFile = null;
   private char fileseparatorChar = '/';
   private char groupingChar = ' ';
   private boolean _f = false;
   private boolean _x = false;
   private boolean _X = false;
   private boolean _r = false;
   private boolean _t = false;
   private boolean _m = false;
   private boolean _p = false;
   private boolean _o = false;
   private boolean _I = false;
   private boolean _O = false;
   private boolean _u = false;
   private boolean _U = false;
   private boolean _l = false;
   private boolean _d = false;
   private boolean _S = false;
   private boolean _e = false;
   private boolean _F = false;
   private boolean _alternate = false;
   private boolean _P = false;
   private boolean _g = false;
   private boolean _G = false;
   private boolean _V = false;
   private boolean _w = false;
   private MetaInfo metaInfo = null;
   private Verbose verbose = null;
   private Summary summary = null;
   private String workingDir = null;
   private int workingdirlen = 0;
   private boolean windows = false;

   public static void main(String[] var0) {
      try {
         new jonelo.jacksum.cli.Jacksum(var0);
      } catch (ExitException var2) {
         if (var2.getMessage() != null) {
            System.err.println(var2.getMessage());
         }

         System.exit(var2.getExitCode());
      }
   }

   private void recursDir(String var1) {
      File var3 = new File(var1);
      if (var3.isDirectory()) {
         if (!this._d || this._d && !Service.isSymbolicLink(var3)) {
            String[] var2 = var3.list();
            if (var2 == null) {
               System.err.println("Jacksum: Can't access file system folder \"" + var3 + "\"");
               this.summary.addErrorDir();
            } else {
               this.summary.addDir();
               if (!this._e && !this._p && !this._S) {
                  String var4 = var3.toString();
                  if (this._w && var4.length() > this.workingdirlen) {
                     var4 = var4.substring(this.workingdirlen);
                  } else if (this._w && var4.length() < this.workingdirlen) {
                     var4 = "";
                  }

                  if (this._P) {
                     System.out.println("\n" + var4.replace(File.separatorChar, this.fileseparatorChar) + ":");
                  } else {
                     System.out.println("\n" + var4 + ":");
                  }
               }

               ArrayList var10 = new ArrayList();
               ArrayList var5 = new ArrayList();
               Arrays.sort(var2, String.CASE_INSENSITIVE_ORDER);
               String var6 = var3.toString();
               boolean var7 = true;
               if (var6.length() > 0 && !var6.endsWith(File.separator)) {
                  if (this.windows && var6.endsWith(":")) {
                     var7 = false;
                  }

                  if (var7) {
                     var6 = var6 + File.separator;
                  }
               }

               for(int var8 = 0; var8 < var2.length; ++var8) {
                  File var9 = new File(var6 + var2[var8]);
                  if (var9.isDirectory()) {
                     var10.add(var2[var8]);
                  } else {
                     var5.add(var2[var8]);
                  }
               }

               if (this.verbose.getDetails() && !this._e && !this._m && !this._p && !this._S) {
                  StringBuffer var11 = new StringBuffer(32);
                  var11.append(var5.size());
                  var11.append(" file");
                  if (var5.size() != 1) {
                     var11.append('s');
                  }

                  if (!this._f) {
                     var11.append(", ");
                     var11.append(var10.size());
                     var11.append(" director");
                     if (var10.size() != 1) {
                        var11.append("ies");
                     } else {
                        var11.append("y");
                     }
                  }

                  System.err.println(var11.toString());
               }

               for(int var12 = 0; var12 < var5.size(); ++var12) {
                  this.recursDir(var6 + var5.get(var12));
               }

               if (!this._f) {
                  for(int var13 = 0; var13 < var10.size(); ++var13) {
                     System.err.println("Jacksum: " + var10.get(var13) + ": Is a directory");
                  }
               }

               for(int var14 = 0; var14 < var10.size(); ++var14) {
                  this.recursDir(var6 + var10.get(var14));
               }
            }
         }
      } else {
         this.processItem(var1);
      }
   }

   private void oneDir(String var1) {
      File var3 = new File(var1);
      if (var3.isDirectory()) {
         String[] var2 = var3.list();
         if (var2 == null) {
            System.err.println("Jacksum: Can't access file system folder \"" + var3 + "\"");
            this.summary.addErrorDir();
         } else {
            this.summary.addDir();
            Arrays.sort(var2, String.CASE_INSENSITIVE_ORDER);

            for(int var4 = 0; var4 < var2.length; ++var4) {
               String var5 = var1 + (var1.endsWith(File.separator) ? "" : File.separator) + var2[var4];
               File var6 = new File(var5);
               if (var6.isDirectory()) {
                  if (!this._f) {
                     System.err.println("Jacksum: " + var2[var4] + ": Is a directory");
                  }
               } else {
                  this.processItem(var5);
               }
            }
         }
      } else {
         this.processItem(var1);
      }
   }

   private void processItem(String var1) {
      File var2 = new File(var1);
      if (var2.isFile()) {
         if (this._o || this._O) {
            try {
               if (new File(this.outputFile).getCanonicalPath().equals(var2.getCanonicalPath())) {
                  return;
               }
            } catch (Exception var7) {
               System.err.println("Jacksum: Error: " + var7);
            }
         }

         if (this._u || this._U) {
            try {
               if (new File(this.errorFile).getCanonicalPath().equals(var2.getCanonicalPath())) {
                  return;
               }
            } catch (Exception var6) {
               System.err.println("Jacksum: Error: " + var6);
            }
         }

         try {
            if (this._S) {
               long var3 = this.checksum.readFile(var1, false);
               if (this.checksum.isTimestampWanted()) {
                  this.checksum.update(this.checksum.getTimestampFormatted().getBytes("ISO-8859-1"));
               }

               String var5 = this._w ? var1.substring(this.workingdirlen) : var1;
               if (File.separatorChar != '/') {
                  var5 = var5.replace(File.separatorChar, '/');
               }

               this.checksum.update(var5.getBytes("ISO-8859-1"));
               this.summary.addBytes(var3);
            } else {
               if (this._e) {
                  this.checksum.readFile(var1, true);
                  this.expectationContinue(this.checksum, this.expected);
               } else {
                  String var9 = this.getChecksumOutput(var1);
                  if (var9 != null) {
                     if (this._P && File.separatorChar != this.fileseparatorChar) {
                        var9 = var9.replace(File.separatorChar, this.fileseparatorChar);
                     }

                     System.out.println(var9);
                  }
               }

               this.summary.addBytes(this.checksum.getLength());
            }

            this.summary.addFile();
         } catch (Exception var8) {
            this.summary.addErrorFile();
            Object var4 = null;
            String var10;
            if (this.verbose.getDetails()) {
               var10 = var1 + " [" + var8.getMessage() + "]";
            } else {
               var10 = var1;
            }

            System.err.println("Jacksum: Error: " + var10);
         }
      } else if (!this._f) {
         this.summary.addErrorFile();
         System.err.println("Jacksum: " + var1 + ": Is not a regular file");
      }
   }

   private String getChecksumOutput(String var1) throws IOException {
      this.checksum.readFile(var1, true);
      File var2 = new File(var1);
      if (this._r && !this._p) {
         this.checksum.setFilename(var2.getName());
      } else {
         if (this._w) {
            var1 = var1.substring(this.workingdirlen);
         }

         this.checksum.setFilename(var1);
      }

      return this._F ? this.checksum.format(this.format) : this.checksum.toString();
   }

   private void expectation(AbstractChecksum var1, String var2) throws ExitException {
      String var3 = var1.getFormattedValue();
      if (var1.getEncoding().equalsIgnoreCase("base64") ? !var3.equals(var2) : !var3.equalsIgnoreCase(var2)) {
         System.out.println("[MISMATCH]");
         throw new ExitException(null, 1);
      } else {
         System.out.println("[OK]");
         throw new ExitException(null, 0);
      }
   }

   private void expectationContinue(AbstractChecksum var1, String var2) {
      String var3 = var1.getFormattedValue();
      if (var1.getEncoding().equalsIgnoreCase("base64") ? var3.equals(var2) : var3.equalsIgnoreCase(var2)) {
         System.out.println(this._F ? var1.format(this.format) : var1.toString());
      }
   }

   private static String decodeQuoteAndSeparator(String var0, String var1) {
      String var2 = var0;
      if (var1 != null) {
         var2 = GeneralString.replaceAllStrings(var0, "#SEPARATOR", var1);
      }

      return GeneralString.replaceAllStrings(var2, "#QUOTE", "\"");
   }

   public Jacksum(String[] var1) throws ExitException {
      GeneralProgram.requiresMinimumJavaVersion("1.3.1");
      boolean var2 = false;
      boolean var3 = false;
      boolean var4 = false;
      boolean var5 = false;
      boolean var6 = false;
      boolean var7 = false;
      Object var8 = null;
      String var9 = null;
      String var10 = null;
      String var11 = null;
      String var12 = null;
      String var13 = null;
      int var14 = 0;
      this.metaInfo = new MetaInfo();
      this.verbose = new Verbose();
      this.summary = new Summary();
      if (var1.length == 0) {
         JacksumHelp.printHelpShort();
      } else if (var1.length > 0) {
         while(var14 < var1.length && var1[var14].startsWith("-")) {
            String var42 = var1[var14++];
            if (var42.equals("-a")) {
               if (var14 >= var1.length) {
                  throw new ExitException("Option -a requires an algorithm. Use -h for help. Exit.", 2);
               }

               var42 = var1[var14++].toLowerCase();
               this.checksumArg = var42;
            } else if (var42.equals("-s")) {
               if (var14 >= var1.length) {
                  throw new ExitException("Option -s requires a separator string. Use -h for help. Exit.", 2);
               }

               var3 = true;
               var42 = var1[var14++];
               this.metaInfo.setSeparator(GeneralString.translateEscapeSequences(var42));
            } else if (var42.equals("-f")) {
               this._f = true;
            } else if (var42.equals("-")) {
               var2 = true;
            } else if (var42.equals("-r")) {
               this._r = true;
            } else if (var42.equals("-x")) {
               this._x = true;
            } else if (var42.equals("-X")) {
               this._X = true;
            } else if (var42.equals("-m")) {
               this._m = true;
            } else if (var42.equals("-p")) {
               this._p = true;
            } else if (var42.equals("-l")) {
               this._l = true;
            } else if (var42.equals("-d")) {
               this._d = true;
            } else if (var42.equals("-E")) {
               if (var14 >= var1.length) {
                  throw new ExitException("Option -b requires an argument", 2);
               }

               var7 = true;
               var42 = var1[var14++];
               var13 = var42;
            } else if (var42.equals("-A")) {
               this._alternate = true;
            } else if (var42.equals("-S")) {
               this._S = true;
            } else if (var42.equals("-q")) {
               if (var14 >= var1.length) {
                  throw new ExitException("Option -q requires a hex sequence argument", 2);
               }

               var5 = true;
               var42 = var1[var14++];
               var10 = var42;
            } else if (var42.equals("-g")) {
               if (var14 >= var1.length) {
                  throw new ExitException("Option -g requires an integer argument", 2);
               }

               this._g = true;
               var42 = var1[var14++];
               var12 = var42;
            } else if (var42.equals("-G")) {
               if (var14 >= var1.length) {
                  throw new ExitException("Option -G requires an argument", 2);
               }

               this._G = true;
               var42 = var1[var14++];
               if (var42.length() != 1) {
                  throw new ExitException("Option -G requires exactly one character", 2);
               }

               this.groupingChar = var42.charAt(0);
            } else if (var42.equals("-P")) {
               if (var14 >= var1.length) {
                  throw new ExitException("Option -P requires an argument", 2);
               }

               this._P = true;
               var42 = var1[var14++];
               if (var42.length() != 1) {
                  throw new ExitException("Option -P requires exactly one character", 2);
               }

               if (var42.charAt(0) != '/' && var42.charAt(0) != '\\') {
                  throw new ExitException("Option -P requires / or \\", 2);
               }

               this.fileseparatorChar = var42.charAt(0);
            } else if (var42.equals("-F")) {
               if (var14 >= var1.length) {
                  throw new ExitException("Option -F requires an argument", 2);
               }

               this._F = true;
               var42 = var1[var14++];
               this.format = var42;
            } else if (var42.equals("-w")) {
               if (var14 >= var1.length) {
                  throw new ExitException("Option -w requires a directory parameter", 2);
               }

               this._w = true;
               this.workingDir = var1[var14];
               if (var14 + 1 < var1.length) {
                  throw new ExitException("Option -w <directory> has to be the last parameter", 2);
               }
            } else if (var42.equals("-c")) {
               if (var14 >= var1.length) {
                  throw new ExitException("Option -c requires a filename parameter", 2);
               }

               var6 = true;
               var42 = var1[var14++];
               var11 = var42;
            } else if (var42.equals("-e")) {
               if (var14 >= var1.length) {
                  throw new ExitException("Option -e requires an argument", 2);
               }

               this._e = true;
               this._f = true;
               var42 = var1[var14++];
               this.expected = var42;
            } else if (!var42.equals("-h")) {
               if (var42.equals("-t")) {
                  this._t = true;
                  if (var14 >= var1.length) {
                     throw new ExitException("Option -t requires a format string. Use -h for help. Exit.", 2);
                  }

                  var9 = var1[var14++];
                  if (var9.equals("default")) {
                     var9 = "yyyyMMddHHmmss";
                  }
               } else {
                  if (var42.equals("-v")) {
                     JacksumHelp.printVersion();
                     throw new ExitException(null, 0);
                  }

                  if (var42.equals("-V")) {
                     this._V = true;
                     if (var14 < var1.length) {
                        String var55 = var1[var14++];
                        if (!var55.equals("default")) {
                           StringTokenizer var57 = new StringTokenizer(var55, ",");

                           while(var57.hasMoreTokens()) {
                              String var17 = var57.nextToken();
                              if (var17.equals("warnings")) {
                                 this.verbose.setWarnings(true);
                              } else if (var17.equals("nowarnings")) {
                                 this.verbose.setWarnings(false);
                              } else if (var17.equals("details")) {
                                 this.verbose.setDetails(true);
                              } else if (var17.equals("nodetails")) {
                                 this.verbose.setDetails(false);
                              } else if (var17.equals("summary")) {
                                 this.verbose.setSummary(true);
                              } else {
                                 if (!var17.equals("nosummary")) {
                                    throw new ExitException("Option -V requires valid parameters. Use -h for help. Exit.", 2);
                                 }

                                 this.verbose.setSummary(false);
                              }
                           }
                        }
                     }
                  } else if (var42.equals("-o")) {
                     this._o = true;
                     if (var14 >= var1.length) {
                        throw new ExitException("Option -o requires a parameter. Use -h for help. Exit.", 2);
                     }

                     this.outputFile = var1[var14++];
                  } else if (var42.equals("-O")) {
                     this._O = true;
                     if (var14 >= var1.length) {
                        throw new ExitException("Option -O requires a parameter. Use -h for help. Exit.", 2);
                     }

                     this.outputFile = var1[var14++];
                  } else if (var42.equals("-u")) {
                     this._u = true;
                     if (var14 >= var1.length) {
                        throw new ExitException("Option -u requires a parameter. Use -h for help. Exit.", 2);
                     }

                     this.errorFile = var1[var14++];
                  } else if (var42.equals("-U")) {
                     this._U = true;
                     if (var14 >= var1.length) {
                        throw new ExitException("Option -U requires a parameter. Use -h for help. Exit.", 2);
                     }

                     this.errorFile = var1[var14++];
                  } else {
                     if (!var42.equals("-I")) {
                        throw new ExitException("Unknown argument. Use -h for help. Exit.", 2);
                     }

                     this._I = true;
                     if (var14 >= var1.length) {
                        throw new ExitException("Option -I requires a parameter. Use -h for help. Exit.", 2);
                     }

                     this.metaInfo.setCommentchars(var1[var14++]);
                  }
               }
            } else {
               String var15 = "en";
               String var16 = null;
               if (var14 < var1.length) {
                  var15 = var1[var14++].toLowerCase();
                  if (!var15.equals("en") && !var15.equals("de")) {
                     var16 = var15;
                     var15 = "en";
                  } else if (var14 < var1.length) {
                     var16 = var1[var14++].toLowerCase();
                  }
               }

               JacksumHelp.help(var15, var16);
            }
         }
      }

      if (this._V && var1.length == 1) {
         JacksumHelp.printVersion();
         throw new ExitException(null, 0);
      } else {
         this.windows = System.getProperty("os.name").toLowerCase(Locale.US).startsWith("windows");
         PrintStream var56 = null;
         boolean var58 = false;
         if ((this._o || this._O) && (this._u || this._U) && this.outputFile.equals(this.errorFile)) {
            if (this._m) {
               throw new ExitException("Jacksum: Error: stdout and stderr may not equal if -m is wanted.", 2);
            }

            try {
               var56 = new PrintStream(new FileOutputStream(this.outputFile));
               var58 = true;
            } catch (Exception var39) {
               throw new ExitException(var39.getMessage(), 4);
            }
         }

         if (this._o || this._O) {
            try {
               File var59 = new File(this.outputFile);
               if (!this._O && var59.exists()) {
                  throw new ExitException("Jacksum: Error: the file " + var59 + " already exists. Specify the file by -O to overwrite it.", 4);
               }

               if (var58) {
                  System.setOut(var56);
               } else {
                  PrintStream var18 = new PrintStream(new FileOutputStream(this.outputFile));
                  System.setOut(var18);
               }
            } catch (Exception var38) {
               throw new ExitException(var38.getMessage(), 4);
            }
         }

         if (this._u || this._U) {
            try {
               File var60 = new File(this.errorFile);
               if (!this._U && var60.exists()) {
                  throw new ExitException("Jacksum: Error: the file " + var60 + " already exists. Specify the file by -U to overwrite it.", 4);
               }

               if (var58) {
                  System.setErr(var56);
               } else {
                  PrintStream var65 = new PrintStream(new FileOutputStream(this.errorFile));
                  System.setErr(var65);
               }
            } catch (Exception var37) {
               throw new ExitException(var37.getMessage(), 4);
            }
         }

         if (this.checksumArg == null) {
            this.checksumArg = "sha1";
         }

         if (this._e && this.checksumArg.equals("none")) {
            throw new ExitException("-a none and -e cannot go together.", 2);
         } else {
            if (!this._alternate && !GeneralProgram.isJ2SEcompatible()) {
               this._alternate = true;
            }

            try {
               this.checksum = JacksumAPI.getChecksumInstance(this.checksumArg, this._alternate);
            } catch (NoSuchAlgorithmException var36) {
               throw new ExitException(
                  var36.getMessage() + "\nUse -a <code> to specify a valid one.\nFor help and a list of all supported algorithms use -h.\nExit.", 2
               );
            }

            this.summary.setEnabled(this.verbose.getSummary());
            if (var3) {
               this.checksum.setSeparator(this.metaInfo.getSeparator());
            }

            if (this._g) {
               try {
                  int var61 = Integer.parseInt(var12);
                  if (var61 > 0) {
                     this.checksum.setEncoding("hex");
                     this.checksum.setGroup(var61);
                     if (this._G) {
                        this.checksum.setGroupChar(this.groupingChar);
                     }
                  } else if (this.verbose.getWarnings()) {
                     System.err.println("Jacksum: Warning: Ignoring -g, because parameter is not greater than 0.");
                  }
               } catch (NumberFormatException var35) {
                  throw new ExitException(var12 + " is not a decimal number.", 2);
               }
            }

            if (this._x) {
               this.checksum.setEncoding("hex");
            }

            if (this._X) {
               this.checksum.setEncoding("hexup");
            }

            if (var7) {
               try {
                  if (var13.length() == 0) {
                     throw new EncodingException("Encoding not supported");
                  }

                  this.checksum.setEncoding(var13);
               } catch (EncodingException var34) {
                  throw new ExitException("Jacksum: " + var34.getMessage(), 2);
               }
            }

            if (this._t && !var5) {
               try {
                  var9 = decodeQuoteAndSeparator(var9, this.metaInfo.getSeparator());
                  SimpleDateFormat var62 = new SimpleDateFormat(var9);
                  var62.format(new Date());
                  this.checksum.setTimestampFormat(var9);
               } catch (IllegalArgumentException var33) {
                  throw new ExitException("Option -t is wrong (" + var33.getMessage() + ")", 2);
               }
            }

            if (this._m && this._S) {
               throw new ExitException("Jacksum: -S and -m can't go together, it is not supported.", 2);
            } else if (this._m && this._G && this.groupingChar == ';') {
               throw new ExitException("Jacksum: Option -G doesn't allow a semicolon when -m has been specified", 2);
            } else {
               if (this._m || var6) {
                  if (this._F && this.verbose.getWarnings()) {
                     System.err.println("Jacksum: Warning: Ignoring -F, because -m or -c has been specified.");
                  }

                  this.metaInfo.setVersion("1.7.0");
                  this.metaInfo.setRecursive(this._r);
                  this.metaInfo.setEncoding(this.checksum.getEncoding());
                  this.metaInfo.setPathInfo(this._p);
                  this.metaInfo.setTimestampFormat(this._t ? this.checksum.getTimestampFormat() : null);
                  this.metaInfo.setFilesep(this._P ? this.fileseparatorChar : File.separatorChar);
                  this.metaInfo.setGrouping(this._g ? this.checksum.getGroup() : 0);
                  if (this._g && this._G) {
                     this.metaInfo.setGroupChar(this.checksum.getGroupChar());
                  }

                  this.metaInfo.setAlgorithm(this.checksum.getName());
                  this.metaInfo.setAlternate(this._alternate);
               }

               if (this._m) {
                  if (this._t && var9.indexOf(";") > -1) {
                     throw new ExitException("Option -t contains a semicolon. This is not supported with -m.", 2);
                  }

                  if (this._I) {
                     if (this.metaInfo.getCommentchars().length() == 0) {
                        throw new ExitException("Option -I has been set to an empty string. This is not supported with -m.", 2);
                     }

                     if (this.metaInfo.getCommentchars().indexOf(";") > -1) {
                        throw new ExitException("Option -I contains a semicolon. This is not supported with -m.", 2);
                     }
                  }

                  if (var3) {
                     if (this.metaInfo.getSeparator().indexOf(";") > -1) {
                        throw new ExitException("Option -s contains a semicolon. This is not supported with -m.", 2);
                     }

                     this.checksum.setSeparator(this.metaInfo.getSeparator());
                  }

                  this._F = false;
                  System.out.println(this.metaInfo);
                  System.out.println(this.metaInfo.getComment());
               }

               Object var63 = null;
               Object var66 = null;
               if (var5) {
                  if (this._t) {
                     if (this.verbose.getWarnings()) {
                        System.err.println("Jacksum: Warning: Option -t will be ignored, because option -q is used.");
                     }

                     this._t = false;
                     this.checksum.setTimestampFormat(null);
                  }

                  Object var74 = null;
                  this.checksum.setFilename("");
                  String var80 = var10.toLowerCase();
                  byte[] var75;
                  if (var80.startsWith("txt:")) {
                     var10 = var10.substring(4);
                     var75 = var10.getBytes();
                  } else if (var80.startsWith("dec:")) {
                     var10 = var10.substring(4);
                     if (var10.length() == 0) {
                        var75 = var10.getBytes();
                     } else {
                        int var82 = GeneralString.countChar(var10, ',');
                        var75 = new byte[var82 + 1];
                        StringTokenizer var22 = new StringTokenizer(var10, ",");

                        int var86;
                        for(int var23 = 0; var22.hasMoreTokens(); var75[var23++] = (byte)var86) {
                           boolean var24 = false;
                           String var25 = null;

                           try {
                              var25 = var22.nextToken();
                              var86 = Integer.parseInt(var25);
                           } catch (NumberFormatException var27) {
                              throw new ExitException(var25 + " is not a decimal number.", 2);
                           }

                           if (var86 < 0 || var86 > 255) {
                              throw new ExitException("The number " + var86 + " is out of range.", 2);
                           }
                        }
                     }
                  } else {
                     if (var80.startsWith("hex:")) {
                        var10 = var10.substring(4);
                     }

                     if (var10.length() % 2 == 1) {
                        throw new ExitException("An even number of nibbles was expected.\nExit.", 2);
                     }

                     try {
                        var75 = new byte[var10.length() / 2];
                        int var83 = 0;

                        String var85;
                        for(int var84 = 0; var84 < var10.length(); var75[var83++] = (byte)Integer.parseInt(var85, 16)) {
                           int var102 = var84;
                           var84 += 2;
                           var85 = var10.substring(var102, var84);
                        }
                     } catch (NumberFormatException var40) {
                        throw new ExitException("Not a hex number. " + var40.getMessage(), 2);
                     }
                  }

                  this.checksum.update(var75);
                  if (this._e) {
                     this.expectation(this.checksum, this.expected);
                  } else {
                     System.out.println(this._F ? this.checksum.format(this.format) : this.checksum.toString());
                  }

                  throw new ExitException(null, 0);
               } else if (var6) {
                  this._F = false;
                  File var73 = new File(var11);
                  if (!var73.exists()) {
                     throw new ExitException("Jacksum: " + var11 + ": No such file or directory. Exit.", 4);
                  } else {
                     byte var79 = 0;
                     if (var73.isDirectory()) {
                        throw new ExitException("Parameter is a directory, but a filename was expected. Exit.", 2);
                     } else {
                        CheckFile var81 = null;

                        try {
                           var81 = new CheckFile(var11);
                           if (this._w) {
                              var81.setWorkingDir(this.workingDir);
                           }

                           var81.setMetaInfo(this.metaInfo);
                           var81.setVerbose(this.verbose);
                           var81.setSummary(this.summary);
                           var81.setList(this._l);
                           var81.perform();
                        } catch (MetaInfoVersionException var28) {
                           throw new ExitException(var28.getMessage(), 3);
                        } catch (ExitException var29) {
                           throw new ExitException(var29.getMessage(), var29.getExitCode());
                        } catch (Exception var30) {
                           var79 = 3;
                           System.err.println(var30);
                        }

                        if (var79 == 0 && var81.getRemoved() + var81.getModified() > 0L) {
                           var79 = 1;
                        }

                        this.summary.print();
                        throw new ExitException(null, var79);
                     }
                  }
               } else {
                  if (var1.length - var14 == 1) {
                     String var19 = var1[var14];
                     File var20 = new File(var19);
                     if (!var20.exists()) {
                        throw new ExitException("Jacksum: " + var19 + ": No such file or directory. Exit.", 4);
                     }

                     if (var20.isDirectory()) {
                        var4 = true;
                     } else {
                        if (!var20.isFile()) {
                           throw new ExitException("Jacksum: \"" + var19 + "\" is not a normal file", 4);
                        }

                        if (this._e) {
                           try {
                              this.checksum.readFile(var19);
                              this.expectation(this.checksum, this.expected);
                           } catch (IOException var32) {
                              throw new ExitException(var32.getMessage(), 4);
                           }
                        }
                     }
                  }

                  if (this._r || var4) {
                     Object var71 = null;
                     String var72;
                     if (var1.length - var14 == 1) {
                        var72 = var1[var14];
                     } else {
                        if (var1.length != var14) {
                           throw new ExitException("Too many parameters. One directory was expeced. Exit.", 2);
                        }

                        var72 = ".";
                     }

                     File var78 = new File(var72);
                     if (!var78.exists()) {
                        throw new ExitException("Jacksum: " + var72 + ": No such file or directory. Exit.", 4);
                     }

                     if (!var78.isDirectory()) {
                        throw new ExitException("Parameter is a file, but a directory was expected. Exit.", 2);
                     }

                     if (this._m) {
                        System.out.println(this.metaInfo.getCommentchars() + " param dir=" + var72);
                     }

                     if (this._w) {
                        this.workingdirlen = this.getWorkingdirLength(var78.toString());
                     }

                     if (this._r) {
                        this.recursDir(var78.toString());
                     } else {
                        this.oneDir(var78.toString());
                     }

                     if (this._S) {
                        this.printS();
                     }
                  } else if (!var2 && var14 != var1.length) {
                     for(int var70 = var14; var70 < var1.length; ++var70) {
                        String var67 = var1[var70];

                        try {
                           File var77 = new File(var67);
                           String var64 = null;
                           if (!var77.exists()) {
                              var64 = "Jacksum: " + var67 + ": No such file or directory";
                           } else if (var77.isDirectory()) {
                              if (!this._f) {
                                 var64 = "Jacksum: " + var67 + ": Is a directory";
                              }
                           } else {
                              this.processItem(var67);
                           }

                           if (var64 != null) {
                              System.err.println(var64);
                           }
                        } catch (Exception var31) {
                           System.err.println(var31);
                        }
                     }

                     if (this._S) {
                        this.printS();
                     }
                  } else {
                     if (this._t) {
                        if (this.verbose.getWarnings()) {
                           System.err.println("Jacksum: Warning: Option -t will be ignored, because standard input is used.");
                        }

                        this._t = false;
                        this.checksum.setTimestampFormat(null);
                     }

                     this.checksum.setFilename("");
                     String var68 = null;
                     BufferedReader var76 = new BufferedReader(new InputStreamReader(System.in));

                     try {
                        while(true) {
                           var68 = var76.readLine();
                           if (var68 != null) {
                              StringBuffer var21 = new StringBuffer(var68.length() + 1);
                              var21.insert(0, var68);
                              var21.insert(var68.length(), '\n');
                              this.checksum.update(var21.toString().getBytes());
                           }

                           if (var68 == null) {
                              this.summary.addBytes(this.checksum.getLength());
                              if (this._e) {
                                 this.expectation(this.checksum, this.expected);
                              } else {
                                 System.out.println(this.checksum.toString());
                              }
                              break;
                           }
                        }
                     } catch (Exception var41) {
                        var41.printStackTrace();
                     }
                  }

                  this.summary.print();
               }
            }
         }
      }
   }

   private int getWorkingdirLength(String var1) {
      if (var1 == null) {
         return 0;
      } else {
         boolean var2 = true;
         if (!var1.endsWith(File.separator)) {
            if (this.windows && var1.endsWith(":")) {
               var2 = false;
            }

            if (var2) {
               var1 = var1 + File.separator;
            }
         }

         return var1.length();
      }
   }

   private void printS() throws ExitException {
      this.checksum.setFilename("");
      this.checksum.setTimestampFormat("");
      this.checksum.setSeparator("");
      if (this._e) {
         this.expectation(this.checksum, this.expected);
      } else {
         System.out.println(this._F ? this.checksum.format(this.format) : this.checksum.format("#CHECKSUM"));
      }
   }
}
