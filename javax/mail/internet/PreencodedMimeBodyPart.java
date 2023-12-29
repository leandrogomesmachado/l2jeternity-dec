package javax.mail.internet;

import com.sun.mail.util.LineOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.mail.MessagingException;

public class PreencodedMimeBodyPart extends MimeBodyPart {
   private String encoding;

   public PreencodedMimeBodyPart(String encoding) {
      this.encoding = encoding;
   }

   @Override
   public String getEncoding() throws MessagingException {
      return this.encoding;
   }

   @Override
   public void writeTo(OutputStream os) throws IOException, MessagingException {
      LineOutputStream los = null;
      if (os instanceof LineOutputStream) {
         los = (LineOutputStream)os;
      } else {
         los = new LineOutputStream(os);
      }

      Enumeration hdrLines = this.getAllHeaderLines();

      while(hdrLines.hasMoreElements()) {
         los.writeln((String)hdrLines.nextElement());
      }

      los.writeln();
      this.getDataHandler().writeTo(os);
      os.flush();
   }

   @Override
   protected void updateHeaders() throws MessagingException {
      super.updateHeaders();
      MimeBodyPart.setEncoding(this, this.encoding);
   }
}
