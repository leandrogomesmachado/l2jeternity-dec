package l2e.commons.string;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.mozilla.universalchardet.UniversalDetector;

public class CharsetEncodingDetector {
   private static final UniversalDetector DETECTOR = new UniversalDetector(null);
   private static final String DEFAULT_ENCODING = "UTF-8";

   public static String detectEncoding(File file) throws IOException {
      FileInputStream fis = null;

      String nread;
      try {
         fis = new FileInputStream(file);
         byte[] buf = new byte[4096];

         while((nread = fis.read(buf)) > 0 && !DETECTOR.isDone()) {
            DETECTOR.handleData(buf, 0, nread);
         }

         DETECTOR.dataEnd();
         String encoding = DETECTOR.getDetectedCharset();
         DETECTOR.reset();
         return encoding;
      } catch (Exception var15) {
         nread = "UTF-8";
      } finally {
         try {
            if (fis != null) {
               fis.close();
            }
         } catch (IOException var14) {
         }
      }

      return nread;
   }
}
