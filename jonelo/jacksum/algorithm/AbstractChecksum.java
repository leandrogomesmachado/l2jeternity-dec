package jonelo.jacksum.algorithm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.Checksum;
import jonelo.jacksum.util.Service;
import jonelo.sugar.util.Base32;
import jonelo.sugar.util.Base64;
import jonelo.sugar.util.BubbleBabble;
import jonelo.sugar.util.EncodingException;
import jonelo.sugar.util.GeneralString;

public abstract class AbstractChecksum implements Checksum {
   public static final String BIN = "bin";
   public static final String DEC = "dec";
   public static final String OCT = "oct";
   public static final String HEX = "hex";
   public static final String HEX_UPPERCASE = "hexup";
   public static final String BASE16 = "base16";
   public static final String BASE32 = "base32";
   public static final String BASE64 = "base64";
   public static final String BUBBLEBABBLE = "bubblebabble";
   public static final int BUFFERSIZE = 8192;
   protected long value = 0L;
   protected long length = 0L;
   protected String separator = "\t";
   protected String filename = null;
   protected String encoding = "";
   protected int group;
   protected char groupChar;
   protected String timestampFormat = null;
   protected Format timestampFormatter = null;
   protected long timestamp = 0L;
   protected String name;

   public AbstractChecksum() {
      this.group = 0;
      this.groupChar = ' ';
      this.name = null;
   }

   public void setName(String var1) {
      this.name = var1;
   }

   public String getName() {
      return this.name;
   }

   public void reset() {
      this.value = 0L;
      this.length = 0L;
   }

   public void update(int var1) {
      ++this.length;
   }

   public void update(byte var1) {
      this.update(var1 & 255);
   }

   public void update(byte[] var1, int var2, int var3) {
      for(int var4 = var2; var4 < var3 + var2; ++var4) {
         this.update(var1[var4]);
      }
   }

   public void update(byte[] var1) {
      this.update(var1, 0, var1.length);
   }

   public long getValue() {
      return this.value;
   }

   public long getLength() {
      return this.length;
   }

   public void setSeparator(String var1) {
      this.separator = var1;
   }

   public String getSeparator() {
      return this.separator;
   }

   public byte[] getByteArray() {
      return new byte[]{(byte)((int)(this.value & 255L))};
   }

   public String toString() {
      return this.getFormattedValue()
         + this.separator
         + this.length
         + this.separator
         + (this.isTimestampWanted() ? this.getTimestampFormatted() + this.separator : "")
         + this.filename;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (var1 instanceof AbstractChecksum) {
         AbstractChecksum var2 = (AbstractChecksum)var1;
         return Arrays.equals(this.getByteArray(), var2.getByteArray());
      } else {
         return false;
      }
   }

   public int hashCode() {
      byte[] var1 = this.getByteArray();
      int var2 = 0;

      for(int var3 = 0; var3 < var1.length; ++var3) {
         var2 = ((var2 << 8) + var1[var3]) % 8388593;
      }

      return var2;
   }

   public String getFormattedValue() {
      if (this.encoding.equalsIgnoreCase("hex")) {
         return Service.format(this.getByteArray(), false, this.group, this.groupChar);
      } else if (this.encoding.equalsIgnoreCase("hexup")) {
         return Service.format(this.getByteArray(), true, this.group, this.groupChar);
      } else if (this.encoding.equalsIgnoreCase("base16")) {
         return Service.format(this.getByteArray(), true, 0, this.groupChar);
      } else if (this.encoding.equalsIgnoreCase("base32")) {
         return Base32.encode(this.getByteArray());
      } else if (this.encoding.equalsIgnoreCase("base64")) {
         return Base64.encodeBytes(this.getByteArray(), 8);
      } else if (this.encoding.equalsIgnoreCase("bubblebabble")) {
         return BubbleBabble.encode(this.getByteArray());
      } else if (this.encoding.equalsIgnoreCase("dec")) {
         BigInteger var2 = new BigInteger(1, this.getByteArray());
         return var2.toString();
      } else if (this.encoding.equalsIgnoreCase("bin")) {
         return Service.formatAsBits(this.getByteArray());
      } else if (this.encoding.equalsIgnoreCase("oct")) {
         BigInteger var1 = new BigInteger(1, this.getByteArray());
         return var1.toString(8);
      } else {
         return Long.toString(this.getValue());
      }
   }

   public void firstFormat(StringBuffer var1) {
      GeneralString.replaceAllStrings(var1, "#FINGERPRINT", "#CHECKSUM");
   }

   public String format(String var1) {
      StringBuffer var2 = new StringBuffer(var1);
      this.firstFormat(var2);
      GeneralString.replaceAllStrings(var2, "#CHECKSUM{i}", "#CHECKSUM");
      GeneralString.replaceAllStrings(var2, "#ALGONAME{i}", "#ALGONAME");
      GeneralString.replaceAllStrings(var2, "#ALGONAME", this.getName());
      GeneralString.replaceAllStrings(var2, "#CHECKSUM", this.getFormattedValue());
      GeneralString.replaceAllStrings(var2, "#FILESIZE", Long.toString(this.length));
      if (var2.toString().indexOf("#FILENAME{") > -1) {
         File var3 = new File(this.filename);
         GeneralString.replaceAllStrings(var2, "#FILENAME{NAME}", var3.getName());
         String var4 = var3.getParent();
         if (var4 == null) {
            var4 = "";
         } else if (!var4.endsWith(File.separator) && !var4.endsWith(":") && System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            var4 = var4 + File.separator;
         }

         GeneralString.replaceAllStrings(var2, "#FILENAME{PATH}", var4);
      }

      GeneralString.replaceAllStrings(var2, "#FILENAME", this.filename);
      if (this.isTimestampWanted()) {
         GeneralString.replaceAllStrings(var2, "#TIMESTAMP", this.getTimestampFormatted());
      }

      GeneralString.replaceAllStrings(var2, "#SEPARATOR", this.separator);
      GeneralString.replaceAllStrings(var2, "#QUOTE", "\"");
      return var2.toString();
   }

   public void setFilename(String var1) {
      this.filename = var1;
   }

   public String getFilename() {
      return this.filename;
   }

   public void setEncoding(String var1) throws EncodingException {
      if (var1 == null) {
         this.encoding = "";
      } else if (var1.equalsIgnoreCase("bb")) {
         this.encoding = "bubblebabble";
      } else {
         if (var1.length() != 0
            && !var1.equalsIgnoreCase("hex")
            && !var1.equalsIgnoreCase("hexup")
            && !var1.equalsIgnoreCase("dec")
            && !var1.equalsIgnoreCase("bin")
            && !var1.equalsIgnoreCase("oct")
            && !var1.equalsIgnoreCase("base16")
            && !var1.equalsIgnoreCase("base32")
            && !var1.equalsIgnoreCase("base64")
            && !var1.equalsIgnoreCase("bubblebabble")) {
            throw new EncodingException("Encoding is not supported");
         }

         this.encoding = var1;
      }
   }

   public String getEncoding() {
      return this.encoding;
   }

   public boolean isGroupWanted() {
      return this.group > 0;
   }

   public void setGroup(int var1) {
      this.group = var1;
   }

   public int getGroup() {
      return this.group;
   }

   public void setGroupChar(char var1) {
      this.groupChar = var1;
   }

   public char getGroupChar() {
      return this.groupChar;
   }

   public void setGrouping(int var1, char var2) {
      this.setGroup(var1);
      this.setGroupChar(var2);
   }

   /** @deprecated */
   public void setHex(boolean var1) {
      this.encoding = var1 ? "hex" : "";
   }

   /** @deprecated */
   public void setUpperCase(boolean var1) {
      this.encoding = var1 ? "hexup" : "hex";
   }

   /** @deprecated */
   public String getHexValue() {
      return Service.format(this.getByteArray(), this.encoding.equalsIgnoreCase("hexup"), this.group, this.groupChar);
   }

   public void setTimestamp(String var1) {
      File var2 = new File(var1);
      this.timestamp = var2.lastModified();
   }

   public long getTimestamp() {
      return this.timestamp;
   }

   public void setTimestampFormat(String var1) {
      this.timestampFormat = var1;
   }

   public String getTimestampFormat() {
      return this.timestampFormat;
   }

   public String getTimestampFormatted() {
      if (this.timestampFormatter == null) {
         this.timestampFormatter = new SimpleDateFormat(this.timestampFormat);
      }

      return this.timestampFormatter.format(new Date(this.timestamp));
   }

   public boolean isTimestampWanted() {
      return this.timestampFormat != null;
   }

   public long readFile(String var1) throws IOException {
      return this.readFile(var1, true);
   }

   public long readFile(String var1, boolean var2) throws IOException {
      this.filename = var1;
      if (this.isTimestampWanted()) {
         this.setTimestamp(var1);
      }

      FileInputStream var3 = null;
      BufferedInputStream var4 = null;
      long var5 = 0L;

      try {
         var3 = new FileInputStream(var1);
         var4 = new BufferedInputStream(var3);
         if (var2) {
            this.reset();
         }

         var5 = this.length;
         int var7 = 0;
         byte[] var8 = new byte[8192];

         while((var7 = var4.read(var8)) > -1) {
            this.update(var8, 0, var7);
         }
      } finally {
         if (var4 != null) {
            var4.close();
         }

         if (var3 != null) {
            var3.close();
         }
      }

      return this.length - var5;
   }
}
