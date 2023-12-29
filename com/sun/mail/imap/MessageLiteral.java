package com.sun.mail.imap;

import com.sun.mail.iap.Literal;
import com.sun.mail.util.CRLFOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.mail.Message;
import javax.mail.MessagingException;

class MessageLiteral implements Literal {
   private Message msg;
   private int msgSize = -1;
   private byte[] buf;

   public MessageLiteral(Message msg, int maxsize) throws MessagingException, IOException {
      this.msg = msg;
      LengthCounter lc = new LengthCounter(maxsize);
      OutputStream os = new CRLFOutputStream(lc);
      msg.writeTo(os);
      os.flush();
      this.msgSize = lc.getSize();
      this.buf = lc.getBytes();
   }

   @Override
   public int size() {
      return this.msgSize;
   }

   @Override
   public void writeTo(OutputStream os) throws IOException {
      try {
         if (this.buf != null) {
            os.write(this.buf, 0, this.msgSize);
         } else {
            OutputStream var4 = new CRLFOutputStream(os);
            this.msg.writeTo(var4);
         }
      } catch (MessagingException var3) {
         throw new IOException("MessagingException while appending message: " + var3);
      }
   }
}
