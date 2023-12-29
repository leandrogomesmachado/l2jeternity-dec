package javax.mail.internet;

import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.FolderClosedIOException;
import com.sun.mail.util.LineOutputStream;
import com.sun.mail.util.MessageRemovedIOException;
import com.sun.mail.util.MimeUtil;
import com.sun.mail.util.PropUtil;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.util.SharedByteArrayInputStream;

public class MimeMessage extends Message implements MimePart {
   protected DataHandler dh;
   protected byte[] content;
   protected InputStream contentStream;
   protected InternetHeaders headers;
   protected Flags flags;
   protected boolean modified = false;
   protected boolean saved = false;
   protected Object cachedContent;
   private static final MailDateFormat mailDateFormat = new MailDateFormat();
   private boolean strict = true;
   private static final Flags answeredFlag = new Flags(Flags.Flag.ANSWERED);

   public MimeMessage(Session session) {
      super(session);
      this.modified = true;
      this.headers = new InternetHeaders();
      this.flags = new Flags();
      this.initStrict();
   }

   public MimeMessage(Session session, InputStream is) throws MessagingException {
      super(session);
      this.flags = new Flags();
      this.initStrict();
      this.parse(is);
      this.saved = true;
   }

   public MimeMessage(MimeMessage source) throws MessagingException {
      super(source.session);
      this.flags = source.getFlags();
      if (this.flags == null) {
         this.flags = new Flags();
      }

      int size = source.getSize();
      ByteArrayOutputStream bos;
      if (size > 0) {
         bos = new ByteArrayOutputStream(size);
      } else {
         bos = new ByteArrayOutputStream();
      }

      try {
         this.strict = source.strict;
         source.writeTo(bos);
         bos.close();
         SharedByteArrayInputStream bis = new SharedByteArrayInputStream(bos.toByteArray());
         this.parse(bis);
         bis.close();
         this.saved = true;
      } catch (IOException var5) {
         throw new MessagingException("IOException while copying message", var5);
      }
   }

   protected MimeMessage(Folder folder, int msgnum) {
      super(folder, msgnum);
      this.flags = new Flags();
      this.saved = true;
      this.initStrict();
   }

   protected MimeMessage(Folder folder, InputStream is, int msgnum) throws MessagingException {
      this(folder, msgnum);
      this.initStrict();
      this.parse(is);
   }

   protected MimeMessage(Folder folder, InternetHeaders headers, byte[] content, int msgnum) throws MessagingException {
      this(folder, msgnum);
      this.headers = headers;
      this.content = content;
      this.initStrict();
   }

   private void initStrict() {
      if (this.session != null) {
         this.strict = PropUtil.getBooleanSessionProperty(this.session, "mail.mime.address.strict", true);
      }
   }

   protected void parse(InputStream is) throws MessagingException {
      if (!(is instanceof ByteArrayInputStream) && !(is instanceof BufferedInputStream) && !(is instanceof SharedInputStream)) {
         is = new BufferedInputStream(is);
      }

      this.headers = this.createInternetHeaders(is);
      if (is instanceof SharedInputStream) {
         SharedInputStream sis = (SharedInputStream)is;
         this.contentStream = sis.newStream(sis.getPosition(), -1L);
      } else {
         try {
            this.content = ASCIIUtility.getBytes(is);
         } catch (IOException var3) {
            throw new MessagingException("IOException", var3);
         }
      }

      this.modified = false;
   }

   @Override
   public Address[] getFrom() throws MessagingException {
      Address[] a = this.getAddressHeader("From");
      if (a == null) {
         a = this.getAddressHeader("Sender");
      }

      return a;
   }

   @Override
   public void setFrom(Address address) throws MessagingException {
      if (address == null) {
         this.removeHeader("From");
      } else {
         this.setHeader("From", address.toString());
      }
   }

   public void setFrom(String address) throws MessagingException {
      if (address == null) {
         this.removeHeader("From");
      } else {
         this.setAddressHeader("From", InternetAddress.parse(address));
      }
   }

   @Override
   public void setFrom() throws MessagingException {
      InternetAddress me = null;

      try {
         me = InternetAddress._getLocalAddress(this.session);
      } catch (Exception var3) {
         throw new MessagingException("No From address", var3);
      }

      if (me != null) {
         this.setFrom(me);
      } else {
         throw new MessagingException("No From address");
      }
   }

   @Override
   public void addFrom(Address[] addresses) throws MessagingException {
      this.addAddressHeader("From", addresses);
   }

   public Address getSender() throws MessagingException {
      Address[] a = this.getAddressHeader("Sender");
      return a != null && a.length != 0 ? a[0] : null;
   }

   public void setSender(Address address) throws MessagingException {
      if (address == null) {
         this.removeHeader("Sender");
      } else {
         this.setHeader("Sender", address.toString());
      }
   }

   @Override
   public Address[] getRecipients(Message.RecipientType type) throws MessagingException {
      if (type == MimeMessage.RecipientType.NEWSGROUPS) {
         String s = this.getHeader("Newsgroups", ",");
         return s == null ? null : NewsAddress.parse(s);
      } else {
         return this.getAddressHeader(this.getHeaderName(type));
      }
   }

   @Override
   public Address[] getAllRecipients() throws MessagingException {
      Address[] all = super.getAllRecipients();
      Address[] ng = this.getRecipients(MimeMessage.RecipientType.NEWSGROUPS);
      if (ng == null) {
         return all;
      } else if (all == null) {
         return ng;
      } else {
         Address[] addresses = new Address[all.length + ng.length];
         System.arraycopy(all, 0, addresses, 0, all.length);
         System.arraycopy(ng, 0, addresses, all.length, ng.length);
         return addresses;
      }
   }

   @Override
   public void setRecipients(Message.RecipientType type, Address[] addresses) throws MessagingException {
      if (type == MimeMessage.RecipientType.NEWSGROUPS) {
         if (addresses != null && addresses.length != 0) {
            this.setHeader("Newsgroups", NewsAddress.toString(addresses));
         } else {
            this.removeHeader("Newsgroups");
         }
      } else {
         this.setAddressHeader(this.getHeaderName(type), addresses);
      }
   }

   public void setRecipients(Message.RecipientType type, String addresses) throws MessagingException {
      if (type == MimeMessage.RecipientType.NEWSGROUPS) {
         if (addresses != null && addresses.length() != 0) {
            this.setHeader("Newsgroups", addresses);
         } else {
            this.removeHeader("Newsgroups");
         }
      } else {
         this.setAddressHeader(this.getHeaderName(type), addresses == null ? null : InternetAddress.parse(addresses));
      }
   }

   @Override
   public void addRecipients(Message.RecipientType type, Address[] addresses) throws MessagingException {
      if (type == MimeMessage.RecipientType.NEWSGROUPS) {
         String s = NewsAddress.toString(addresses);
         if (s != null) {
            this.addHeader("Newsgroups", s);
         }
      } else {
         this.addAddressHeader(this.getHeaderName(type), addresses);
      }
   }

   public void addRecipients(Message.RecipientType type, String addresses) throws MessagingException {
      if (type == MimeMessage.RecipientType.NEWSGROUPS) {
         if (addresses != null && addresses.length() != 0) {
            this.addHeader("Newsgroups", addresses);
         }
      } else {
         this.addAddressHeader(this.getHeaderName(type), InternetAddress.parse(addresses));
      }
   }

   @Override
   public Address[] getReplyTo() throws MessagingException {
      Address[] a = this.getAddressHeader("Reply-To");
      if (a == null || a.length == 0) {
         a = this.getFrom();
      }

      return a;
   }

   @Override
   public void setReplyTo(Address[] addresses) throws MessagingException {
      this.setAddressHeader("Reply-To", addresses);
   }

   private Address[] getAddressHeader(String name) throws MessagingException {
      String s = this.getHeader(name, ",");
      return s == null ? null : InternetAddress.parseHeader(s, this.strict);
   }

   private void setAddressHeader(String name, Address[] addresses) throws MessagingException {
      String s = InternetAddress.toString(addresses, name.length() + 2);
      if (s == null) {
         this.removeHeader(name);
      } else {
         this.setHeader(name, s);
      }
   }

   private void addAddressHeader(String name, Address[] addresses) throws MessagingException {
      if (addresses != null && addresses.length != 0) {
         Address[] a = this.getAddressHeader(name);
         Address[] anew;
         if (a != null && a.length != 0) {
            anew = new Address[a.length + addresses.length];
            System.arraycopy(a, 0, anew, 0, a.length);
            System.arraycopy(addresses, 0, anew, a.length, addresses.length);
         } else {
            anew = addresses;
         }

         String s = InternetAddress.toString(anew, name.length() + 2);
         if (s != null) {
            this.setHeader(name, s);
         }
      }
   }

   @Override
   public String getSubject() throws MessagingException {
      String rawvalue = this.getHeader("Subject", null);
      if (rawvalue == null) {
         return null;
      } else {
         try {
            return MimeUtility.decodeText(MimeUtility.unfold(rawvalue));
         } catch (UnsupportedEncodingException var3) {
            return rawvalue;
         }
      }
   }

   @Override
   public void setSubject(String subject) throws MessagingException {
      this.setSubject(subject, null);
   }

   public void setSubject(String subject, String charset) throws MessagingException {
      if (subject == null) {
         this.removeHeader("Subject");
      } else {
         try {
            this.setHeader("Subject", MimeUtility.fold(9, MimeUtility.encodeText(subject, charset, null)));
         } catch (UnsupportedEncodingException var4) {
            throw new MessagingException("Encoding error", var4);
         }
      }
   }

   @Override
   public Date getSentDate() throws MessagingException {
      String s = this.getHeader("Date", null);
      if (s != null) {
         try {
            synchronized(mailDateFormat) {
               return mailDateFormat.parse(s);
            }
         } catch (java.text.ParseException var5) {
            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public void setSentDate(Date d) throws MessagingException {
      if (d == null) {
         this.removeHeader("Date");
      } else {
         synchronized(mailDateFormat) {
            this.setHeader("Date", mailDateFormat.format(d));
         }
      }
   }

   @Override
   public Date getReceivedDate() throws MessagingException {
      return null;
   }

   @Override
   public int getSize() throws MessagingException {
      if (this.content != null) {
         return this.content.length;
      } else {
         if (this.contentStream != null) {
            try {
               int size = this.contentStream.available();
               if (size > 0) {
                  return size;
               }
            } catch (IOException var2) {
            }
         }

         return -1;
      }
   }

   @Override
   public int getLineCount() throws MessagingException {
      return -1;
   }

   @Override
   public String getContentType() throws MessagingException {
      String s = this.getHeader("Content-Type", null);
      s = MimeUtil.cleanContentType(this, s);
      return s == null ? "text/plain" : s;
   }

   @Override
   public boolean isMimeType(String mimeType) throws MessagingException {
      return MimeBodyPart.isMimeType(this, mimeType);
   }

   @Override
   public String getDisposition() throws MessagingException {
      return MimeBodyPart.getDisposition(this);
   }

   @Override
   public void setDisposition(String disposition) throws MessagingException {
      MimeBodyPart.setDisposition(this, disposition);
   }

   @Override
   public String getEncoding() throws MessagingException {
      return MimeBodyPart.getEncoding(this);
   }

   @Override
   public String getContentID() throws MessagingException {
      return this.getHeader("Content-Id", null);
   }

   public void setContentID(String cid) throws MessagingException {
      if (cid == null) {
         this.removeHeader("Content-ID");
      } else {
         this.setHeader("Content-ID", cid);
      }
   }

   @Override
   public String getContentMD5() throws MessagingException {
      return this.getHeader("Content-MD5", null);
   }

   @Override
   public void setContentMD5(String md5) throws MessagingException {
      this.setHeader("Content-MD5", md5);
   }

   @Override
   public String getDescription() throws MessagingException {
      return MimeBodyPart.getDescription(this);
   }

   @Override
   public void setDescription(String description) throws MessagingException {
      this.setDescription(description, null);
   }

   public void setDescription(String description, String charset) throws MessagingException {
      MimeBodyPart.setDescription(this, description, charset);
   }

   @Override
   public String[] getContentLanguage() throws MessagingException {
      return MimeBodyPart.getContentLanguage(this);
   }

   @Override
   public void setContentLanguage(String[] languages) throws MessagingException {
      MimeBodyPart.setContentLanguage(this, languages);
   }

   public String getMessageID() throws MessagingException {
      return this.getHeader("Message-ID", null);
   }

   @Override
   public String getFileName() throws MessagingException {
      return MimeBodyPart.getFileName(this);
   }

   @Override
   public void setFileName(String filename) throws MessagingException {
      MimeBodyPart.setFileName(this, filename);
   }

   private String getHeaderName(Message.RecipientType type) throws MessagingException {
      String headerName;
      if (type == Message.RecipientType.TO) {
         headerName = "To";
      } else if (type == Message.RecipientType.CC) {
         headerName = "Cc";
      } else if (type == Message.RecipientType.BCC) {
         headerName = "Bcc";
      } else {
         if (type != MimeMessage.RecipientType.NEWSGROUPS) {
            throw new MessagingException("Invalid Recipient Type");
         }

         headerName = "Newsgroups";
      }

      return headerName;
   }

   @Override
   public InputStream getInputStream() throws IOException, MessagingException {
      return this.getDataHandler().getInputStream();
   }

   protected InputStream getContentStream() throws MessagingException {
      if (this.contentStream != null) {
         return ((SharedInputStream)this.contentStream).newStream(0L, -1L);
      } else if (this.content != null) {
         return new SharedByteArrayInputStream(this.content);
      } else {
         throw new MessagingException("No MimeMessage content");
      }
   }

   public InputStream getRawInputStream() throws MessagingException {
      return this.getContentStream();
   }

   @Override
   public synchronized DataHandler getDataHandler() throws MessagingException {
      if (this.dh == null) {
         this.dh = new MimeBodyPart.MimePartDataHandler(this);
      }

      return this.dh;
   }

   @Override
   public Object getContent() throws IOException, MessagingException {
      if (this.cachedContent != null) {
         return this.cachedContent;
      } else {
         Object c;
         try {
            c = this.getDataHandler().getContent();
         } catch (FolderClosedIOException var3) {
            throw new FolderClosedException(var3.getFolder(), var3.getMessage());
         } catch (MessageRemovedIOException var4) {
            throw new MessageRemovedException(var4.getMessage());
         }

         if (MimeBodyPart.cacheMultipart && (c instanceof Multipart || c instanceof Message) && (this.content != null || this.contentStream != null)) {
            this.cachedContent = c;
            if (c instanceof MimeMultipart) {
               ((MimeMultipart)c).parse();
            }
         }

         return c;
      }
   }

   @Override
   public synchronized void setDataHandler(DataHandler dh) throws MessagingException {
      this.dh = dh;
      this.cachedContent = null;
      MimeBodyPart.invalidateContentHeaders(this);
   }

   @Override
   public void setContent(Object o, String type) throws MessagingException {
      if (o instanceof Multipart) {
         this.setContent((Multipart)o);
      } else {
         this.setDataHandler(new DataHandler(o, type));
      }
   }

   @Override
   public void setText(String text) throws MessagingException {
      this.setText(text, null);
   }

   @Override
   public void setText(String text, String charset) throws MessagingException {
      MimeBodyPart.setText(this, text, charset, "plain");
   }

   @Override
   public void setText(String text, String charset, String subtype) throws MessagingException {
      MimeBodyPart.setText(this, text, charset, subtype);
   }

   @Override
   public void setContent(Multipart mp) throws MessagingException {
      this.setDataHandler(new DataHandler(mp, mp.getContentType()));
      mp.setParent(this);
   }

   @Override
   public Message reply(boolean replyToAll) throws MessagingException {
      return this.reply(replyToAll, true);
   }

   public Message reply(boolean replyToAll, boolean setAnswered) throws MessagingException {
      MimeMessage reply = this.createMimeMessage(this.session);
      String subject = this.getHeader("Subject", null);
      if (subject != null) {
         if (!subject.regionMatches(true, 0, "Re: ", 0, 4)) {
            subject = "Re: " + subject;
         }

         reply.setHeader("Subject", subject);
      }

      Address[] a = this.getReplyTo();
      reply.setRecipients(Message.RecipientType.TO, a);
      if (replyToAll) {
         Vector v = new Vector();
         InternetAddress me = InternetAddress.getLocalAddress(this.session);
         if (me != null) {
            v.addElement(me);
         }

         String alternates = null;
         if (this.session != null) {
            alternates = this.session.getProperty("mail.alternates");
         }

         if (alternates != null) {
            this.eliminateDuplicates(v, InternetAddress.parse(alternates, false));
         }

         String replyallccStr = null;
         boolean replyallcc = false;
         if (this.session != null) {
            replyallcc = PropUtil.getBooleanSessionProperty(this.session, "mail.replyallcc", false);
         }

         this.eliminateDuplicates(v, a);
         a = this.getRecipients(Message.RecipientType.TO);
         a = this.eliminateDuplicates(v, a);
         if (a != null && a.length > 0) {
            if (replyallcc) {
               reply.addRecipients(Message.RecipientType.CC, a);
            } else {
               reply.addRecipients(Message.RecipientType.TO, a);
            }
         }

         a = this.getRecipients(Message.RecipientType.CC);
         a = this.eliminateDuplicates(v, a);
         if (a != null && a.length > 0) {
            reply.addRecipients(Message.RecipientType.CC, a);
         }

         a = this.getRecipients(MimeMessage.RecipientType.NEWSGROUPS);
         if (a != null && a.length > 0) {
            reply.setRecipients(MimeMessage.RecipientType.NEWSGROUPS, a);
         }
      }

      String msgId = this.getHeader("Message-Id", null);
      if (msgId != null) {
         reply.setHeader("In-Reply-To", msgId);
      }

      String refs = this.getHeader("References", " ");
      if (refs == null) {
         refs = this.getHeader("In-Reply-To", " ");
      }

      if (msgId != null) {
         if (refs != null) {
            refs = MimeUtility.unfold(refs) + " " + msgId;
         } else {
            refs = msgId;
         }
      }

      if (refs != null) {
         reply.setHeader("References", MimeUtility.fold(12, refs));
      }

      if (setAnswered) {
         try {
            this.setFlags(answeredFlag, true);
         } catch (MessagingException var11) {
         }
      }

      return reply;
   }

   private Address[] eliminateDuplicates(Vector v, Address[] addrs) {
      if (addrs == null) {
         return null;
      } else {
         int gone = 0;

         for(int i = 0; i < addrs.length; ++i) {
            boolean found = false;

            for(int j = 0; j < v.size(); ++j) {
               if (((InternetAddress)v.elementAt(j)).equals(addrs[i])) {
                  found = true;
                  ++gone;
                  addrs[i] = null;
                  break;
               }
            }

            if (!found) {
               v.addElement(addrs[i]);
            }
         }

         if (gone != 0) {
            Address[] a;
            if (addrs instanceof InternetAddress[]) {
               a = new InternetAddress[addrs.length - gone];
            } else {
               a = new Address[addrs.length - gone];
            }

            int i = 0;

            for(int j = 0; i < addrs.length; ++i) {
               if (addrs[i] != null) {
                  a[j++] = addrs[i];
               }
            }

            addrs = a;
         }

         return addrs;
      }
   }

   @Override
   public void writeTo(OutputStream os) throws IOException, MessagingException {
      this.writeTo(os, null);
   }

   public void writeTo(OutputStream os, String[] ignoreList) throws IOException, MessagingException {
      if (!this.saved) {
         this.saveChanges();
      }

      if (this.modified) {
         MimeBodyPart.writeTo(this, os, ignoreList);
      } else {
         Enumeration hdrLines = this.getNonMatchingHeaderLines(ignoreList);
         LineOutputStream los = new LineOutputStream(os);

         while(hdrLines.hasMoreElements()) {
            los.writeln((String)hdrLines.nextElement());
         }

         los.writeln();
         if (this.content == null) {
            InputStream is = null;
            byte[] buf = new byte[8192];

            try {
               is = this.getContentStream();

               int len;
               while((len = is.read(buf)) > 0) {
                  os.write(buf, 0, len);
               }
            } finally {
               if (is != null) {
                  is.close();
               }

               byte[] var12 = null;
            }
         } else {
            os.write(this.content);
         }

         os.flush();
      }
   }

   @Override
   public String[] getHeader(String name) throws MessagingException {
      return this.headers.getHeader(name);
   }

   @Override
   public String getHeader(String name, String delimiter) throws MessagingException {
      return this.headers.getHeader(name, delimiter);
   }

   @Override
   public void setHeader(String name, String value) throws MessagingException {
      this.headers.setHeader(name, value);
   }

   @Override
   public void addHeader(String name, String value) throws MessagingException {
      this.headers.addHeader(name, value);
   }

   @Override
   public void removeHeader(String name) throws MessagingException {
      this.headers.removeHeader(name);
   }

   @Override
   public Enumeration getAllHeaders() throws MessagingException {
      return this.headers.getAllHeaders();
   }

   @Override
   public Enumeration getMatchingHeaders(String[] names) throws MessagingException {
      return this.headers.getMatchingHeaders(names);
   }

   @Override
   public Enumeration getNonMatchingHeaders(String[] names) throws MessagingException {
      return this.headers.getNonMatchingHeaders(names);
   }

   @Override
   public void addHeaderLine(String line) throws MessagingException {
      this.headers.addHeaderLine(line);
   }

   @Override
   public Enumeration getAllHeaderLines() throws MessagingException {
      return this.headers.getAllHeaderLines();
   }

   @Override
   public Enumeration getMatchingHeaderLines(String[] names) throws MessagingException {
      return this.headers.getMatchingHeaderLines(names);
   }

   @Override
   public Enumeration getNonMatchingHeaderLines(String[] names) throws MessagingException {
      return this.headers.getNonMatchingHeaderLines(names);
   }

   @Override
   public synchronized Flags getFlags() throws MessagingException {
      return (Flags)this.flags.clone();
   }

   @Override
   public synchronized boolean isSet(Flags.Flag flag) throws MessagingException {
      return this.flags.contains(flag);
   }

   @Override
   public synchronized void setFlags(Flags flag, boolean set) throws MessagingException {
      if (set) {
         this.flags.add(flag);
      } else {
         this.flags.remove(flag);
      }
   }

   @Override
   public void saveChanges() throws MessagingException {
      this.modified = true;
      this.saved = true;
      this.updateHeaders();
   }

   protected void updateMessageID() throws MessagingException {
      this.setHeader("Message-ID", "<" + UniqueValue.getUniqueMessageIDValue(this.session) + ">");
   }

   protected synchronized void updateHeaders() throws MessagingException {
      MimeBodyPart.updateHeaders(this);
      this.setHeader("MIME-Version", "1.0");
      this.updateMessageID();
      if (this.cachedContent != null) {
         this.dh = new DataHandler(this.cachedContent, this.getContentType());
         this.cachedContent = null;
         this.content = null;
         if (this.contentStream != null) {
            try {
               this.contentStream.close();
            } catch (IOException var2) {
            }
         }

         this.contentStream = null;
      }
   }

   protected InternetHeaders createInternetHeaders(InputStream is) throws MessagingException {
      return new InternetHeaders(is);
   }

   protected MimeMessage createMimeMessage(Session session) throws MessagingException {
      return new MimeMessage(session);
   }

   public static class RecipientType extends Message.RecipientType {
      private static final long serialVersionUID = -5468290701714395543L;
      public static final MimeMessage.RecipientType NEWSGROUPS = new MimeMessage.RecipientType("Newsgroups");

      protected RecipientType(String type) {
         super(type);
      }

      @Override
      protected Object readResolve() throws ObjectStreamException {
         return this.type.equals("Newsgroups") ? NEWSGROUPS : super.readResolve();
      }
   }
}
