package com.sun.mail.pop3;

import com.sun.mail.util.ReadableMime;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.Enumeration;
import java.util.logging.Level;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.IllegalWriteException;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.SharedInputStream;

public class POP3Message extends MimeMessage implements ReadableMime {
   static final String UNKNOWN = "UNKNOWN";
   private POP3Folder folder;
   private int hdrSize = -1;
   private int msgSize = -1;
   String uid = "UNKNOWN";
   private SoftReference rawData = new SoftReference(null);

   public POP3Message(Folder folder, int msgno) throws MessagingException {
      super(folder, msgno);

      assert folder instanceof POP3Folder;

      this.folder = (POP3Folder)folder;
   }

   @Override
   public synchronized void setFlags(Flags newFlags, boolean set) throws MessagingException {
      Flags oldFlags = (Flags)this.flags.clone();
      super.setFlags(newFlags, set);
      if (!this.flags.equals(oldFlags)) {
         this.folder.notifyMessageChangedListeners(1, this);
      }
   }

   @Override
   public int getSize() throws MessagingException {
      try {
         synchronized(this) {
            if (this.msgSize > 0) {
               return this.msgSize;
            }
         }

         if (this.headers == null) {
            this.loadHeaders();
         }

         synchronized(this) {
            if (this.msgSize < 0) {
               this.msgSize = this.folder.getProtocol().list(this.msgnum) - this.hdrSize;
            }

            return this.msgSize;
         }
      } catch (EOFException var6) {
         this.folder.close(false);
         throw new FolderClosedException(this.folder, var6.toString());
      } catch (IOException var7) {
         throw new MessagingException("error getting size", var7);
      }
   }

   private InputStream getRawStream(boolean skipHeader) throws MessagingException {
      InputStream rawcontent = null;

      try {
         synchronized(this) {
            rawcontent = (InputStream)this.rawData.get();
            if (rawcontent == null) {
               TempFile cache = this.folder.getFileCache();
               if (cache != null) {
                  if (this.folder.logger.isLoggable(Level.FINE)) {
                     this.folder.logger.fine("caching message #" + this.msgnum + " in temp file");
                  }

                  AppendStream os = cache.getAppendStream();
                  BufferedOutputStream bos = new BufferedOutputStream(os);

                  try {
                     this.folder.getProtocol().retr(this.msgnum, bos);
                  } finally {
                     bos.close();
                  }

                  rawcontent = os.getInputStream();
               } else {
                  rawcontent = this.folder.getProtocol().retr(this.msgnum, this.msgSize > 0 ? this.msgSize + this.hdrSize : 0);
               }

               if (rawcontent == null) {
                  this.expunged = true;
                  throw new MessageRemovedException("can't retrieve message #" + this.msgnum + " in POP3Message.getContentStream");
               }

               if (this.headers != null && !((POP3Store)this.folder.getStore()).forgetTopHeaders) {
                  int offset = 0;

                  int c1;
                  int len;
                  do {
                     for(len = 0; (c1 = rawcontent.read()) >= 0 && c1 != 10; ++len) {
                        if (c1 == 13) {
                           if (rawcontent.available() > 0) {
                              rawcontent.mark(1);
                              if (rawcontent.read() != 10) {
                                 rawcontent.reset();
                              }
                           }
                           break;
                        }
                     }
                  } while(rawcontent.available() != 0 && len != 0);

                  this.hdrSize = (int)((SharedInputStream)rawcontent).getPosition();
               } else {
                  this.headers = new InternetHeaders(rawcontent);
                  this.hdrSize = (int)((SharedInputStream)rawcontent).getPosition();
               }

               this.msgSize = rawcontent.available();
               this.rawData = new SoftReference<>(rawcontent);
            }
         }
      } catch (EOFException var16) {
         this.folder.close(false);
         throw new FolderClosedException(this.folder, var16.toString());
      } catch (IOException var17) {
         throw new MessagingException("error fetching POP3 content", var17);
      }

      return ((SharedInputStream)rawcontent).newStream(skipHeader ? (long)this.hdrSize : 0L, -1L);
   }

   @Override
   protected synchronized InputStream getContentStream() throws MessagingException {
      if (this.contentStream != null) {
         return ((SharedInputStream)this.contentStream).newStream(0L, -1L);
      } else {
         InputStream cstream = this.getRawStream(true);
         TempFile cache = this.folder.getFileCache();
         if (cache != null || ((POP3Store)this.folder.getStore()).keepMessageContent) {
            this.contentStream = ((SharedInputStream)cstream).newStream(0L, -1L);
         }

         return cstream;
      }
   }

   @Override
   public InputStream getMimeStream() throws MessagingException {
      return this.getRawStream(false);
   }

   public synchronized void invalidate(boolean invalidateHeaders) {
      this.content = null;
      InputStream rstream = (InputStream)this.rawData.get();
      if (rstream != null) {
         try {
            rstream.close();
         } catch (IOException var5) {
         }

         this.rawData = new SoftReference(null);
      }

      if (this.contentStream != null) {
         try {
            this.contentStream.close();
         } catch (IOException var4) {
         }

         this.contentStream = null;
      }

      this.msgSize = -1;
      if (invalidateHeaders) {
         this.headers = null;
         this.hdrSize = -1;
      }
   }

   public InputStream top(int n) throws MessagingException {
      try {
         synchronized(this) {
            return this.folder.getProtocol().top(this.msgnum, n);
         }
      } catch (EOFException var5) {
         this.folder.close(false);
         throw new FolderClosedException(this.folder, var5.toString());
      } catch (IOException var6) {
         throw new MessagingException("error getting size", var6);
      }
   }

   @Override
   public String[] getHeader(String name) throws MessagingException {
      if (this.headers == null) {
         this.loadHeaders();
      }

      return this.headers.getHeader(name);
   }

   @Override
   public String getHeader(String name, String delimiter) throws MessagingException {
      if (this.headers == null) {
         this.loadHeaders();
      }

      return this.headers.getHeader(name, delimiter);
   }

   @Override
   public void setHeader(String name, String value) throws MessagingException {
      throw new IllegalWriteException("POP3 messages are read-only");
   }

   @Override
   public void addHeader(String name, String value) throws MessagingException {
      throw new IllegalWriteException("POP3 messages are read-only");
   }

   @Override
   public void removeHeader(String name) throws MessagingException {
      throw new IllegalWriteException("POP3 messages are read-only");
   }

   @Override
   public Enumeration getAllHeaders() throws MessagingException {
      if (this.headers == null) {
         this.loadHeaders();
      }

      return this.headers.getAllHeaders();
   }

   @Override
   public Enumeration getMatchingHeaders(String[] names) throws MessagingException {
      if (this.headers == null) {
         this.loadHeaders();
      }

      return this.headers.getMatchingHeaders(names);
   }

   @Override
   public Enumeration getNonMatchingHeaders(String[] names) throws MessagingException {
      if (this.headers == null) {
         this.loadHeaders();
      }

      return this.headers.getNonMatchingHeaders(names);
   }

   @Override
   public void addHeaderLine(String line) throws MessagingException {
      throw new IllegalWriteException("POP3 messages are read-only");
   }

   @Override
   public Enumeration getAllHeaderLines() throws MessagingException {
      if (this.headers == null) {
         this.loadHeaders();
      }

      return this.headers.getAllHeaderLines();
   }

   @Override
   public Enumeration getMatchingHeaderLines(String[] names) throws MessagingException {
      if (this.headers == null) {
         this.loadHeaders();
      }

      return this.headers.getMatchingHeaderLines(names);
   }

   @Override
   public Enumeration getNonMatchingHeaderLines(String[] names) throws MessagingException {
      if (this.headers == null) {
         this.loadHeaders();
      }

      return this.headers.getNonMatchingHeaderLines(names);
   }

   @Override
   public void saveChanges() throws MessagingException {
      throw new IllegalWriteException("POP3 messages are read-only");
   }

   @Override
   public synchronized void writeTo(OutputStream os, String[] ignoreList) throws IOException, MessagingException {
      InputStream rawcontent = (InputStream)this.rawData.get();
      if (rawcontent == null && ignoreList == null && !((POP3Store)this.folder.getStore()).cacheWriteTo) {
         if (this.folder.logger.isLoggable(Level.FINE)) {
            this.folder.logger.fine("streaming msg " + this.msgnum);
         }

         if (!this.folder.getProtocol().retr(this.msgnum, os)) {
            this.expunged = true;
            throw new MessageRemovedException("can't retrieve message #" + this.msgnum + " in POP3Message.writeTo");
         }
      } else if (rawcontent != null && ignoreList == null) {
         InputStream in = ((SharedInputStream)rawcontent).newStream(0L, -1L);

         try {
            byte[] buf = new byte[16384];

            int len;
            while((len = in.read(buf)) > 0) {
               os.write(buf, 0, len);
            }
         } finally {
            try {
               if (in != null) {
                  in.close();
               }
            } catch (IOException var13) {
            }
         }
      } else {
         super.writeTo(os, ignoreList);
      }
   }

   private void loadHeaders() throws MessagingException {
      assert !Thread.holdsLock(this);

      try {
         boolean fetchContent = false;
         synchronized(this) {
            if (this.headers != null) {
               return;
            }

            InputStream hdrs = null;
            if (!((POP3Store)this.folder.getStore()).disableTop && (hdrs = this.folder.getProtocol().top(this.msgnum, 0)) != null) {
               try {
                  this.hdrSize = hdrs.available();
                  this.headers = new InternetHeaders(hdrs);
               } finally {
                  hdrs.close();
               }
            } else {
               fetchContent = true;
            }
         }

         if (fetchContent) {
            InputStream cs = null;

            try {
               cs = this.getContentStream();
            } finally {
               if (cs != null) {
                  cs.close();
               }
            }
         }
      } catch (EOFException var22) {
         this.folder.close(false);
         throw new FolderClosedException(this.folder, var22.toString());
      } catch (IOException var23) {
         throw new MessagingException("error loading POP3 headers", var23);
      }
   }
}
