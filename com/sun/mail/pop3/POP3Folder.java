package com.sun.mail.pop3;

import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.MailLogger;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.UIDFolder;

public class POP3Folder extends Folder {
   private String name;
   private POP3Store store;
   private volatile Protocol port;
   private int total;
   private int size;
   private boolean exists = false;
   private volatile boolean opened = false;
   private Vector message_cache;
   private boolean doneUidl = false;
   private volatile TempFile fileCache = null;
   MailLogger logger;

   POP3Folder(POP3Store store, String name) {
      super(store);
      this.name = name;
      this.store = store;
      if (name.equalsIgnoreCase("INBOX")) {
         this.exists = true;
      }

      this.logger = new MailLogger(this.getClass(), "DEBUG POP3", store.getSession());
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public String getFullName() {
      return this.name;
   }

   @Override
   public Folder getParent() {
      return new DefaultFolder(this.store);
   }

   @Override
   public boolean exists() {
      return this.exists;
   }

   @Override
   public Folder[] list(String pattern) throws MessagingException {
      throw new MessagingException("not a directory");
   }

   @Override
   public char getSeparator() {
      return '\u0000';
   }

   @Override
   public int getType() {
      return 1;
   }

   @Override
   public boolean create(int type) throws MessagingException {
      return false;
   }

   @Override
   public boolean hasNewMessages() throws MessagingException {
      return false;
   }

   @Override
   public Folder getFolder(String name) throws MessagingException {
      throw new MessagingException("not a directory");
   }

   @Override
   public boolean delete(boolean recurse) throws MessagingException {
      throw new MethodNotSupportedException("delete");
   }

   @Override
   public boolean renameTo(Folder f) throws MessagingException {
      throw new MethodNotSupportedException("renameTo");
   }

   @Override
   public synchronized void open(int mode) throws MessagingException {
      this.checkClosed();
      if (!this.exists) {
         throw new FolderNotFoundException(this, "folder is not INBOX");
      } else {
         try {
            this.port = this.store.getPort(this);
            Status s = this.port.stat();
            this.total = s.total;
            this.size = s.size;
            this.mode = mode;
            if (this.store.useFileCache) {
               try {
                  this.fileCache = new TempFile(this.store.fileCacheDir);
               } catch (IOException var12) {
                  this.logger.log(Level.FINE, "failed to create file cache", (Throwable)var12);
                  throw var12;
               }
            }

            this.opened = true;
         } catch (IOException var13) {
            try {
               if (this.port != null) {
                  this.port.quit();
               }
            } catch (IOException var10) {
            } finally {
               this.port = null;
               this.store.closePort(this);
            }

            throw new MessagingException("Open failed", var13);
         }

         this.message_cache = new Vector(this.total);
         this.message_cache.setSize(this.total);
         this.doneUidl = false;
         this.notifyConnectionListeners(1);
      }
   }

   @Override
   public synchronized void close(boolean expunge) throws MessagingException {
      this.checkOpen();

      try {
         if (this.store.rsetBeforeQuit) {
            this.port.rset();
         }

         if (expunge && this.mode == 2) {
            for(int i = 0; i < this.message_cache.size(); ++i) {
               POP3Message m;
               if ((m = (POP3Message)this.message_cache.elementAt(i)) != null && m.isSet(Flags.Flag.DELETED)) {
                  try {
                     this.port.dele(i + 1);
                  } catch (IOException var10) {
                     throw new MessagingException("Exception deleting messages during close", var10);
                  }
               }
            }
         }

         for(int i = 0; i < this.message_cache.size(); ++i) {
            POP3Message m;
            if ((m = (POP3Message)this.message_cache.elementAt(i)) != null) {
               m.invalidate(true);
            }
         }

         this.port.quit();
      } catch (IOException var11) {
      } finally {
         this.port = null;
         this.store.closePort(this);
         this.message_cache = null;
         this.opened = false;
         this.notifyConnectionListeners(3);
         if (this.fileCache != null) {
            this.fileCache.close();
            this.fileCache = null;
         }
      }
   }

   @Override
   public synchronized boolean isOpen() {
      if (!this.opened) {
         return false;
      } else {
         try {
            if (!this.port.noop()) {
               throw new IOException("NOOP failed");
            } else {
               return true;
            }
         } catch (IOException var10) {
            try {
               this.close(false);
            } catch (MessagingException var8) {
            } finally {
               return false;
            }

            return false;
         }
      }
   }

   @Override
   public Flags getPermanentFlags() {
      return new Flags();
   }

   @Override
   public synchronized int getMessageCount() throws MessagingException {
      if (!this.opened) {
         return -1;
      } else {
         this.checkReadable();
         return this.total;
      }
   }

   @Override
   public synchronized Message getMessage(int msgno) throws MessagingException {
      this.checkOpen();
      POP3Message m;
      if ((m = (POP3Message)this.message_cache.elementAt(msgno - 1)) == null) {
         m = this.createMessage(this, msgno);
         this.message_cache.setElementAt(m, msgno - 1);
      }

      return m;
   }

   protected POP3Message createMessage(Folder f, int msgno) throws MessagingException {
      POP3Message m = null;
      Constructor cons = this.store.messageConstructor;
      if (cons != null) {
         try {
            Object[] o = new Object[]{this, msgno};
            m = (POP3Message)cons.newInstance(o);
         } catch (Exception var6) {
         }
      }

      if (m == null) {
         m = new POP3Message(this, msgno);
      }

      return m;
   }

   @Override
   public void appendMessages(Message[] msgs) throws MessagingException {
      throw new MethodNotSupportedException("Append not supported");
   }

   @Override
   public Message[] expunge() throws MessagingException {
      throw new MethodNotSupportedException("Expunge not supported");
   }

   @Override
   public synchronized void fetch(Message[] msgs, FetchProfile fp) throws MessagingException {
      this.checkReadable();
      if (!this.doneUidl && this.store.supportsUidl && fp.contains(UIDFolder.FetchProfileItem.UID)) {
         String[] uids = new String[this.message_cache.size()];

         try {
            if (!this.port.uidl(uids)) {
               return;
            }
         } catch (EOFException var7) {
            this.close(false);
            throw new FolderClosedException(this, var7.toString());
         } catch (IOException var8) {
            throw new MessagingException("error getting UIDL", var8);
         }

         for(int i = 0; i < uids.length; ++i) {
            if (uids[i] != null) {
               POP3Message m = (POP3Message)this.getMessage(i + 1);
               m.uid = uids[i];
            }
         }

         this.doneUidl = true;
      }

      if (fp.contains(FetchProfile.Item.ENVELOPE)) {
         for(int i = 0; i < msgs.length; ++i) {
            try {
               POP3Message msg = (POP3Message)msgs[i];
               msg.getHeader("");
               msg.getSize();
            } catch (MessageRemovedException var6) {
            }
         }
      }
   }

   public synchronized String getUID(Message msg) throws MessagingException {
      this.checkOpen();
      if (!(msg instanceof POP3Message)) {
         throw new MessagingException("message is not a POP3Message");
      } else {
         POP3Message m = (POP3Message)msg;

         try {
            if (!this.store.supportsUidl) {
               return null;
            } else {
               if (m.uid == "UNKNOWN") {
                  m.uid = this.port.uidl(m.getMessageNumber());
               }

               return m.uid;
            }
         } catch (EOFException var4) {
            this.close(false);
            throw new FolderClosedException(this, var4.toString());
         } catch (IOException var5) {
            throw new MessagingException("error getting UIDL", var5);
         }
      }
   }

   public synchronized int getSize() throws MessagingException {
      this.checkOpen();
      return this.size;
   }

   public synchronized int[] getSizes() throws MessagingException {
      this.checkOpen();
      int[] sizes = new int[this.total];
      InputStream is = null;
      LineInputStream lis = null;

      try {
         is = this.port.list();
         lis = new LineInputStream(is);

         String line;
         while((line = lis.readLine()) != null) {
            try {
               StringTokenizer st = new StringTokenizer(line);
               int msgnum = Integer.parseInt(st.nextToken());
               int size = Integer.parseInt(st.nextToken());
               if (msgnum > 0 && msgnum <= this.total) {
                  sizes[msgnum - 1] = size;
               }
            } catch (RuntimeException var22) {
            }
         }
      } catch (IOException var23) {
      } finally {
         try {
            if (lis != null) {
               lis.close();
            }
         } catch (IOException var21) {
         }

         try {
            if (is != null) {
               is.close();
            }
         } catch (IOException var20) {
         }
      }

      return sizes;
   }

   public synchronized InputStream listCommand() throws MessagingException, IOException {
      this.checkOpen();
      return this.port.list();
   }

   @Override
   protected void finalize() throws Throwable {
      super.finalize();
      this.close(false);
   }

   private void checkOpen() throws IllegalStateException {
      if (!this.opened) {
         throw new IllegalStateException("Folder is not Open");
      }
   }

   private void checkClosed() throws IllegalStateException {
      if (this.opened) {
         throw new IllegalStateException("Folder is Open");
      }
   }

   private void checkReadable() throws IllegalStateException {
      if (!this.opened || this.mode != 1 && this.mode != 2) {
         throw new IllegalStateException("Folder is not Readable");
      }
   }

   Protocol getProtocol() throws MessagingException {
      Protocol p = this.port;
      this.checkOpen();
      return p;
   }

   @Override
   protected void notifyMessageChangedListeners(int type, Message m) {
      super.notifyMessageChangedListeners(type, m);
   }

   TempFile getFileCache() {
      return this.fileCache;
   }
}
