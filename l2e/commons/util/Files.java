package l2e.commons.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.string.CharsetEncodingDetector;
import org.apache.commons.io.FileUtils;

public class Files {
   private static final Logger _log = Logger.getLogger(Files.class.getName());

   public static String readFile(File file, String outputEncode) throws IOException {
      String content = FileUtils.readFileToString(file, CharsetEncodingDetector.detectEncoding(file));
      return new String(content.getBytes(outputEncode));
   }

   public static String readFile(File file) throws IOException {
      return readFile(file, "UTF-8");
   }

   public static void writeFile(String path, String string) {
      try {
         FileUtils.writeStringToFile(new File(path), string, "UTF-8");
      } catch (IOException var3) {
         _log.log(Level.SEVERE, "Error while saving file : " + path, (Throwable)var3);
      }
   }

   public static boolean copyFile(String srcFile, String destFile) {
      try {
         FileUtils.copyFile(new File(srcFile), new File(destFile), false);
         return true;
      } catch (IOException var3) {
         _log.log(Level.SEVERE, "Error while copying file : " + srcFile + " to " + destFile, (Throwable)var3);
         return false;
      }
   }

   public static String read(String name) {
      if (name == null) {
         return null;
      } else {
         File file = new File("./" + name);
         if (!file.exists()) {
            return null;
         } else {
            String content = null;
            BufferedReader br = null;

            try {
               br = new BufferedReader(new UnicodeReader(new FileInputStream(file), "UTF-8"));
               StringBuffer sb = new StringBuffer();
               String s = "";

               while((s = br.readLine()) != null) {
                  sb.append(s).append("\n");
               }

               content = sb.toString();
               Object var16 = null;
            } catch (Exception var14) {
            } finally {
               try {
                  if (br != null) {
                     br.close();
                  }
               } catch (Exception var13) {
               }
            }

            return content;
         }
      }
   }
}
