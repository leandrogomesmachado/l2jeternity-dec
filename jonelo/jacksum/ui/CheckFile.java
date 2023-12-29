package jonelo.jacksum.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;
import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;
import jonelo.jacksum.algorithm.Edonkey;
import jonelo.jacksum.algorithm.MD;
import jonelo.jacksum.algorithm.MDgnu;
import jonelo.jacksum.algorithm.None;
import jonelo.jacksum.algorithm.Read;
import jonelo.sugar.util.EncodingException;
import jonelo.sugar.util.ExitException;
import jonelo.sugar.util.GeneralString;

public class CheckFile {
   public static final String COMMENTDEFAULT = "Jacksum: Comment:";
   private String CSEP = "\t";
   private String checkFile = null;
   private MetaInfo metaInfo = null;
   private AbstractChecksum checksum = null;
   private boolean _l = false;
   private Verbose verbose = null;
   private Summary summary = null;
   private long removed = 0L;
   private long modified = 0L;
   private String workingDir = null;

   public CheckFile(String var1) {
      this.checkFile = var1;
   }

   public void setMetaInfo(MetaInfo var1) {
      this.metaInfo = var1;
   }

   public MetaInfo getMetaInfo() {
      return this.metaInfo;
   }

   public void setVerbose(Verbose var1) {
      this.verbose = var1;
   }

   public Verbose getVerbose() {
      return this.verbose;
   }

   public void setSummary(Summary var1) {
      this.summary = var1;
      var1.setCheck(true);
   }

   public Summary getSummary() {
      return this.summary;
   }

   public void setList(boolean var1) {
      this._l = var1;
   }

   public boolean isList() {
      return this._l;
   }

   public long getModified() {
      return this.modified;
   }

   public long getRemoved() {
      return this.removed;
   }

   public void setWorkingDir(String var1) {
      this.workingDir = var1;
   }

   public String getWorkingDir() {
      return this.workingDir;
   }

   public void perform() throws FileNotFoundException, IOException, MetaInfoVersionException, ExitException {
      FileInputStream var1 = null;
      InputStreamReader var2 = null;
      BufferedReader var3 = null;

      try {
         var1 = new FileInputStream(this.checkFile);
         var2 = new InputStreamReader(var1);
         var3 = new BufferedReader(var2);
         var3.mark(1024);
         Object var4 = null;
         int var5 = 2;
         String var6 = null;
         String var23;
         if ((var23 = var3.readLine()) == null) {
            throw new ExitException("File is empty.\nExit.", 3);
         }

         if (var23.startsWith("Jacksum: Meta-Info: ") && !var23.startsWith(this.metaInfo.getCommentchars())) {
            this.metaInfo = new MetaInfo(var23);
         } else {
            if (this.verbose.getWarnings()) {
               System.err.println("Jacksum: Warning: file does not contain meta information. Please set suitable command line parameters.");
            }

            var3.reset();
         }

         try {
            this.checksum = JacksumAPI.getChecksumInstance(this.metaInfo.getAlgorithm(), this.metaInfo.isAlternate());
         } catch (NoSuchAlgorithmException var18) {
            throw new ExitException(var18.getMessage(), 3);
         }

         if (this.checksum instanceof MD || this.checksum instanceof MDgnu || this.checksum instanceof Edonkey) {
            --var5;
         }

         if (this.checksum instanceof None || this.checksum instanceof Read) {
            --var5;
         }

         if (this.metaInfo.isSeparatorWanted()) {
            this.CSEP = this.metaInfo.getSeparator();
            this.checksum.setSeparator(this.CSEP);
         } else {
            this.CSEP = this.checksum.getSeparator();
         }

         if (this.metaInfo.isTimestampFormat()) {
            ++var5;
            this.checksum.setTimestampFormat(this.metaInfo.getTimestampFormat());
            String[] var7 = GeneralString.split(this.metaInfo.getTimestampFormat(), this.CSEP);
            var5 += var7.length - 1;
         } else {
            this.checksum.setTimestampFormat(null);
         }

         if (this.metaInfo.isGrouping()) {
            this.checksum.setGroup(this.metaInfo.getGrouping());
            this.checksum.setGroupChar(this.metaInfo.getGroupChar());
         } else {
            this.checksum.setGroup(0);
         }

         if (this.metaInfo.isEncoding()) {
            try {
               this.checksum.setEncoding(this.metaInfo.getEncoding());
            } catch (EncodingException var19) {
               if (this.verbose.getWarnings()) {
                  System.err.println("Jacksum: Warning: " + var19.getMessage());
               }
            }
         }

         int var26 = 0;
         if (this.checksum.getEncoding().length() != 0
            && !this.checksum.getEncoding().equalsIgnoreCase("dec")
            && !this.checksum.getEncoding().equalsIgnoreCase("oct")
            && !(this.checksum instanceof None)
            && !(this.checksum instanceof Read)) {
            var26 = this.checksum.getFormattedValue().length();
            --var5;
         } else {
            var26 = 0;
         }

         String var8 = "";
         boolean var9 = true;

         while((var23 = var3.readLine()) != null) {
            if (!var23.startsWith("Jacksum: Comment:") && !var23.startsWith(this.metaInfo.getCommentchars())) {
               if (var23.length() == 0) {
                  var9 = true;
               } else if (var23.startsWith("Jacksum")) {
                  if (this.verbose.getWarnings()) {
                     System.err.println("Jacksum: Warning: Ignoring unknown directive.");
                  }
               } else if (var9 && this.metaInfo.isRecursive() && !this.metaInfo.isPathInfo() && var23.endsWith(":")) {
                  var9 = false;
                  var8 = var23.substring(0, var23.length() - 1);
                  if (this.workingDir != null && this.workingDir.length() > 0) {
                     var8 = this.workingDir + this.metaInfo.getFilesep() + var8;
                  }

                  if (File.separatorChar != this.metaInfo.getFilesep()) {
                     var8 = var8.replace(this.metaInfo.getFilesep(), File.separatorChar);
                  }

                  if (!this._l) {
                     System.out.println("\n" + var8 + ":");
                  }

                  if (var8.length() > 0) {
                     var8 = var8 + File.separator;
                  }
               } else {
                  try {
                     var6 = this.parseFilename(var23, var5, var26);
                     int var10 = var6.length();
                     if (this.metaInfo.isPathInfo() && this.workingDir != null && this.workingDir.length() > 0) {
                        var8 = this.workingDir + this.metaInfo.getFilesep();
                        var10 += var8.length();
                     }

                     if (File.separatorChar != this.metaInfo.getFilesep()) {
                        var6 = var6.replace(this.metaInfo.getFilesep(), File.separatorChar);
                     }

                     if (this._l) {
                        this.skipOkFiles(var8 + var6, var23, var10);
                     } else {
                        System.out.print(this.whatChanged(var8 + var6, var23, var10));
                        System.out.println(var6);
                     }
                  } catch (NoSuchElementException var20) {
                     if (this.verbose.getWarnings()) {
                        System.err.println("Jacksum: Warning: Invalid entry: " + var23);
                     }
                  } catch (IOException var21) {
                     this.summary.addErrorFile();
                     Object var11 = null;
                     String var28;
                     if (this.verbose.getDetails()) {
                        var28 = var6 + " [" + var21.getMessage() + "]";
                     } else {
                        var28 = var6;
                     }

                     System.err.println("Jacksum: Error: " + var28);
                  }
               }
            }
         }
      } finally {
         this.summary.setRemovedFiles(this.removed);
         this.summary.setModifiedFiles(this.modified);
         if (var3 != null) {
            var3.close();
         }

         if (var2 != null) {
            var2.close();
         }

         if (var1 != null) {
            var1.close();
         }
      }
   }

   private void skipOkFiles(String var1, String var2, int var3) throws IOException {
      boolean var4 = false;
      if (!new File(var1).exists()) {
         ++this.removed;
         var4 = true;
      } else {
         String var5 = this.getChecksumOutput(var1);
         if (!var5.regionMatches(0, var2, 0, var5.length() - var3)) {
            var4 = true;
            ++this.modified;
         }
      }

      if (var4) {
         System.out.println(var1);
      }

      this.summary.addFile();
   }

   private String whatChanged(String var1, String var2, int var3) throws IOException {
      if (!new File(var1).exists()) {
         ++this.removed;
         this.summary.addFile();
         return "[REMOVED] ";
      } else {
         String var4 = this.getChecksumOutput(var1);
         if (!var4.regionMatches(0, var2, 0, var4.length() - var3)) {
            ++this.modified;
            this.summary.addFile();
            return "[FAILED]  ";
         } else {
            this.summary.addFile();
            return "[OK]      ";
         }
      }
   }

   private String parseFilename(String var1, int var2, int var3) throws NoSuchElementException {
      if (var3 > 0) {
         var1 = var1.substring(var3 + this.CSEP.length());
      }

      StringBuffer var4 = new StringBuffer();
      String[] var5 = GeneralString.split(var1, this.CSEP);
      var4.append(var5[var2]);

      for(int var6 = var2 + 1; var6 < var5.length; ++var6) {
         var4.append(this.CSEP);
         var4.append(var5[var6]);
      }

      return var4.toString();
   }

   private String getChecksumOutput(String var1) throws IOException {
      this.summary.addBytes(this.checksum.readFile(var1, true));
      File var2 = new File(var1);
      if (this.metaInfo.isRecursive() && !this.metaInfo.isPathInfo()) {
         this.checksum.setFilename(var2.getName());
      } else {
         this.checksum.setFilename(var1);
      }

      return this.checksum.toString();
   }
}
