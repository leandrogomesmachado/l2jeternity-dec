package com.sun.mail.imap;

import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseHandler;
import com.sun.mail.imap.protocol.FLAGS;
import com.sun.mail.imap.protocol.FetchItem;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.ListInfo;
import com.sun.mail.imap.protocol.MODSEQ;
import com.sun.mail.imap.protocol.MailboxInfo;
import com.sun.mail.imap.protocol.MessageSet;
import com.sun.mail.imap.protocol.Status;
import com.sun.mail.imap.protocol.UID;
import com.sun.mail.imap.protocol.UIDSet;
import com.sun.mail.util.MailLogger;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
import javax.mail.Quota;
import javax.mail.ReadOnlyFolderException;
import javax.mail.StoreClosedException;
import javax.mail.UIDFolder;
import javax.mail.event.MailEvent;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchException;
import javax.mail.search.SearchTerm;

public class IMAPFolder extends Folder implements UIDFolder, ResponseHandler {
   protected String fullName;
   protected String name;
   protected int type;
   protected char separator;
   protected Flags availableFlags;
   protected Flags permanentFlags;
   protected volatile boolean exists;
   protected boolean isNamespace = false;
   protected volatile String[] attributes;
   protected volatile IMAPProtocol protocol;
   protected MessageCache messageCache;
   protected final Object messageCacheLock = new Object();
   protected Hashtable uidTable;
   protected static final char UNKNOWN_SEPARATOR = '\uffff';
   private volatile boolean opened = false;
   private boolean reallyClosed = true;
   private static final int RUNNING = 0;
   private static final int IDLE = 1;
   private static final int ABORTING = 2;
   private int idleState = 0;
   private volatile IdleManager idleManager;
   private volatile int total = -1;
   private volatile int recent = -1;
   private int realTotal = -1;
   private long uidvalidity = -1L;
   private long uidnext = -1L;
   private volatile long highestmodseq = -1L;
   private boolean doExpungeNotification = true;
   private Status cachedStatus = null;
   private long cachedStatusTime = 0L;
   private boolean hasMessageCountListener = false;
   protected MailLogger logger;
   private MailLogger connectionPoolLogger;

   protected IMAPFolder(String fullName, char separator, IMAPStore store, Boolean isNamespace) {
      super(store);
      if (fullName == null) {
         throw new NullPointerException("Folder name is null");
      } else {
         this.fullName = fullName;
         this.separator = separator;
         this.logger = new MailLogger(this.getClass(), "DEBUG IMAP", store.getSession());
         this.connectionPoolLogger = store.getConnectionPoolLogger();
         this.isNamespace = false;
         if (separator != '\uffff' && separator != 0) {
            int i = this.fullName.indexOf(separator);
            if (i > 0 && i == this.fullName.length() - 1) {
               this.fullName = this.fullName.substring(0, i);
               this.isNamespace = true;
            }
         }

         if (isNamespace != null) {
            this.isNamespace = isNamespace;
         }
      }
   }

   protected IMAPFolder(ListInfo li, IMAPStore store) {
      this(li.name, li.separator, store, null);
      if (li.hasInferiors) {
         this.type |= 2;
      }

      if (li.canOpen) {
         this.type |= 1;
      }

      this.exists = true;
      this.attributes = li.attrs;
   }

   protected void checkExists() throws MessagingException {
      if (!this.exists && !this.exists()) {
         throw new FolderNotFoundException(this, this.fullName + " not found");
      }
   }

   protected void checkClosed() {
      if (this.opened) {
         throw new IllegalStateException("This operation is not allowed on an open folder");
      }
   }

   protected void checkOpened() throws FolderClosedException {
      assert Thread.holdsLock(this);

      if (!this.opened) {
         if (this.reallyClosed) {
            throw new IllegalStateException("This operation is not allowed on a closed folder");
         } else {
            throw new FolderClosedException(this, "Lost folder connection to server");
         }
      }
   }

   protected void checkRange(int msgno) throws MessagingException {
      if (msgno < 1) {
         throw new IndexOutOfBoundsException("message number < 1");
      } else if (msgno > this.total) {
         synchronized(this.messageCacheLock) {
            try {
               this.keepConnectionAlive(false);
            } catch (ConnectionException var5) {
               throw new FolderClosedException(this, var5.getMessage());
            } catch (ProtocolException var6) {
               throw new MessagingException(var6.getMessage(), var6);
            }
         }

         if (msgno > this.total) {
            throw new IndexOutOfBoundsException(msgno + " > " + this.total);
         }
      }
   }

   private void checkFlags(Flags flags) throws MessagingException {
      assert Thread.holdsLock(this);

      if (this.mode != 2) {
         throw new IllegalStateException("Cannot change flags on READ_ONLY folder: " + this.fullName);
      }
   }

   @Override
   public synchronized String getName() {
      if (this.name == null) {
         try {
            this.name = this.fullName.substring(this.fullName.lastIndexOf(this.getSeparator()) + 1);
         } catch (MessagingException var2) {
         }
      }

      return this.name;
   }

   @Override
   public synchronized String getFullName() {
      return this.fullName;
   }

   @Override
   public synchronized Folder getParent() throws MessagingException {
      char c = this.getSeparator();
      int index;
      return (Folder)((index = this.fullName.lastIndexOf(c)) != -1
         ? ((IMAPStore)this.store).newIMAPFolder(this.fullName.substring(0, index), c)
         : new DefaultFolder((IMAPStore)this.store));
   }

   @Override
   public synchronized boolean exists() throws MessagingException {
      ListInfo[] li = null;
      final String lname;
      if (this.isNamespace && this.separator != 0) {
         lname = this.fullName + this.separator;
      } else {
         lname = this.fullName;
      }

      li = (ListInfo[])this.doCommand(new IMAPFolder.ProtocolCommand() {
         @Override
         public Object doCommand(IMAPProtocol p) throws ProtocolException {
            return p.list("", lname);
         }
      });
      if (li != null) {
         int i = this.findName(li, lname);
         this.fullName = li[i].name;
         this.separator = li[i].separator;
         int len = this.fullName.length();
         if (this.separator != 0 && len > 0 && this.fullName.charAt(len - 1) == this.separator) {
            this.fullName = this.fullName.substring(0, len - 1);
         }

         this.type = 0;
         if (li[i].hasInferiors) {
            this.type |= 2;
         }

         if (li[i].canOpen) {
            this.type |= 1;
         }

         this.exists = true;
         this.attributes = li[i].attrs;
      } else {
         this.exists = this.opened;
         this.attributes = null;
      }

      return this.exists;
   }

   private int findName(ListInfo[] li, String lname) {
      int i = 0;

      while(i < li.length && !li[i].name.equals(lname)) {
         ++i;
      }

      if (i >= li.length) {
         i = 0;
      }

      return i;
   }

   @Override
   public Folder[] list(String pattern) throws MessagingException {
      return this.doList(pattern, false);
   }

   @Override
   public Folder[] listSubscribed(String pattern) throws MessagingException {
      return this.doList(pattern, true);
   }

   private synchronized Folder[] doList(final String pattern, final boolean subscribed) throws MessagingException {
      this.checkExists();
      if (this.attributes != null && !this.isDirectory()) {
         return new Folder[0];
      } else {
         final char c = this.getSeparator();
         ListInfo[] li = (ListInfo[])this.doCommandIgnoreFailure(new IMAPFolder.ProtocolCommand() {
            @Override
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
               return subscribed ? p.lsub("", IMAPFolder.this.fullName + c + pattern) : p.list("", IMAPFolder.this.fullName + c + pattern);
            }
         });
         if (li == null) {
            return new Folder[0];
         } else {
            int start = 0;
            if (li.length > 0 && li[0].name.equals(this.fullName + c)) {
               start = 1;
            }

            IMAPFolder[] folders = new IMAPFolder[li.length - start];
            IMAPStore st = (IMAPStore)this.store;

            for(int i = start; i < li.length; ++i) {
               folders[i - start] = st.newIMAPFolder(li[i]);
            }

            return folders;
         }
      }
   }

   @Override
   public synchronized char getSeparator() throws MessagingException {
      if (this.separator == '\uffff') {
         ListInfo[] li = null;
         li = (ListInfo[])this.doCommand(new IMAPFolder.ProtocolCommand() {
            @Override
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
               return p.isREV1() ? p.list(IMAPFolder.this.fullName, "") : p.list("", IMAPFolder.this.fullName);
            }
         });
         if (li != null) {
            this.separator = li[0].separator;
         } else {
            this.separator = '/';
         }
      }

      return this.separator;
   }

   @Override
   public synchronized int getType() throws MessagingException {
      if (this.opened) {
         if (this.attributes == null) {
            this.exists();
         }
      } else {
         this.checkExists();
      }

      return this.type;
   }

   @Override
   public synchronized boolean isSubscribed() {
      ListInfo[] li = null;
      final String lname;
      if (this.isNamespace && this.separator != 0) {
         lname = this.fullName + this.separator;
      } else {
         lname = this.fullName;
      }

      try {
         li = (ListInfo[])this.doProtocolCommand(new IMAPFolder.ProtocolCommand() {
            @Override
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
               return p.lsub("", lname);
            }
         });
      } catch (ProtocolException var4) {
      }

      if (li != null) {
         int i = this.findName(li, lname);
         return li[i].canOpen;
      } else {
         return false;
      }
   }

   @Override
   public synchronized void setSubscribed(final boolean subscribe) throws MessagingException {
      this.doCommandIgnoreFailure(new IMAPFolder.ProtocolCommand() {
         @Override
         public Object doCommand(IMAPProtocol p) throws ProtocolException {
            if (subscribe) {
               p.subscribe(IMAPFolder.this.fullName);
            } else {
               p.unsubscribe(IMAPFolder.this.fullName);
            }

            return null;
         }
      });
   }

   @Override
   public synchronized boolean create(final int type) throws MessagingException {
      char c = 0;
      if ((type & 1) == 0) {
         c = this.getSeparator();
      }

      final char sep = c;
      Object ret = this.doCommandIgnoreFailure(new IMAPFolder.ProtocolCommand() {
         @Override
         public Object doCommand(IMAPProtocol p) throws ProtocolException {
            if ((type & 1) == 0) {
               p.create(IMAPFolder.this.fullName + sep);
            } else {
               p.create(IMAPFolder.this.fullName);
               if ((type & 2) != 0) {
                  ListInfo[] li = p.list("", IMAPFolder.this.fullName);
                  if (li != null && !li[0].hasInferiors) {
                     p.delete(IMAPFolder.this.fullName);
                     throw new ProtocolException("Unsupported type");
                  }
               }
            }

            return Boolean.TRUE;
         }
      });
      if (ret == null) {
         return false;
      } else {
         boolean retb = this.exists();
         if (retb) {
            this.notifyFolderListeners(1);
         }

         return retb;
      }
   }

   @Override
   public synchronized boolean hasNewMessages() throws MessagingException {
      if (this.opened) {
         synchronized(this.messageCacheLock) {
            try {
               this.keepConnectionAlive(true);
            } catch (ConnectionException var5) {
               throw new FolderClosedException(this, var5.getMessage());
            } catch (ProtocolException var6) {
               throw new MessagingException(var6.getMessage(), var6);
            }

            return this.recent > 0;
         }
      } else {
         ListInfo[] li = null;
         final String lname;
         if (this.isNamespace && this.separator != 0) {
            lname = this.fullName + this.separator;
         } else {
            lname = this.fullName;
         }

         li = (ListInfo[])this.doCommandIgnoreFailure(new IMAPFolder.ProtocolCommand() {
            @Override
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
               return p.list("", lname);
            }
         });
         if (li == null) {
            throw new FolderNotFoundException(this, this.fullName + " not found");
         } else {
            int i = this.findName(li, lname);
            if (li[i].changeState == 1) {
               return true;
            } else if (li[i].changeState == 2) {
               return false;
            } else {
               try {
                  Status status = this.getStatus();
                  return status.recent > 0;
               } catch (BadCommandException var8) {
                  return false;
               } catch (ConnectionException var9) {
                  throw new StoreClosedException(this.store, var9.getMessage());
               } catch (ProtocolException var10) {
                  throw new MessagingException(var10.getMessage(), var10);
               }
            }
         }
      }
   }

   @Override
   public synchronized Folder getFolder(String name) throws MessagingException {
      if (this.attributes != null && !this.isDirectory()) {
         throw new MessagingException("Cannot contain subfolders");
      } else {
         char c = this.getSeparator();
         return ((IMAPStore)this.store).newIMAPFolder(this.fullName + c + name, c);
      }
   }

   @Override
   public synchronized boolean delete(boolean recurse) throws MessagingException {
      this.checkClosed();
      if (recurse) {
         Folder[] f = this.list();

         for(int i = 0; i < f.length; ++i) {
            f[i].delete(recurse);
         }
      }

      Object ret = this.doCommandIgnoreFailure(new IMAPFolder.ProtocolCommand() {
         @Override
         public Object doCommand(IMAPProtocol p) throws ProtocolException {
            p.delete(IMAPFolder.this.fullName);
            return Boolean.TRUE;
         }
      });
      if (ret == null) {
         return false;
      } else {
         this.exists = false;
         this.attributes = null;
         this.notifyFolderListeners(2);
         return true;
      }
   }

   @Override
   public synchronized boolean renameTo(final Folder f) throws MessagingException {
      this.checkClosed();
      this.checkExists();
      if (f.getStore() != this.store) {
         throw new MessagingException("Can't rename across Stores");
      } else {
         Object ret = this.doCommandIgnoreFailure(new IMAPFolder.ProtocolCommand() {
            @Override
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
               p.rename(IMAPFolder.this.fullName, f.getFullName());
               return Boolean.TRUE;
            }
         });
         if (ret == null) {
            return false;
         } else {
            this.exists = false;
            this.attributes = null;
            this.notifyFolderRenamedListeners(f);
            return true;
         }
      }
   }

   @Override
   public synchronized void open(int mode) throws MessagingException {
      this.open(mode, null);
   }

   public synchronized List<MailEvent> open(int mode, ResyncData rd) throws MessagingException {
      this.checkClosed();
      MailboxInfo mi = null;
      this.protocol = ((IMAPStore)this.store).getProtocol(this);
      List<MailEvent> openEvents = null;
      synchronized(this.messageCacheLock) {
         this.protocol.addResponseHandler(this);

         try {
            if (rd != null) {
               if (rd == ResyncData.CONDSTORE) {
                  if (!this.protocol.isEnabled("CONDSTORE") && !this.protocol.isEnabled("QRESYNC")) {
                     if (this.protocol.hasCapability("CONDSTORE")) {
                        this.protocol.enable("CONDSTORE");
                     } else {
                        this.protocol.enable("QRESYNC");
                     }
                  }
               } else if (!this.protocol.isEnabled("QRESYNC")) {
                  this.protocol.enable("QRESYNC");
               }
            }

            if (mode == 1) {
               mi = this.protocol.examine(this.fullName, rd);
            } else {
               mi = this.protocol.select(this.fullName, rd);
            }
         } catch (CommandFailedException var63) {
            CommandFailedException cex = var63;

            try {
               this.checkExists();
               if ((this.type & 1) == 0) {
                  throw new MessagingException("folder cannot contain messages");
               }

               throw new MessagingException(cex.getMessage(), cex);
            } finally {
               this.exists = false;
               this.attributes = null;
               this.type = 0;
               this.releaseProtocol(true);
            }
         } catch (ProtocolException var64) {
            label408:
            try {
               try {
                  this.protocol.logout();
               } catch (ProtocolException var56) {
               }
            } finally {
               break label408;
            }

            this.releaseProtocol(false);
            throw new MessagingException(var64.getMessage(), var64);
         }

         if (mi.mode != mode && (mode != 2 || mi.mode != 1 || !((IMAPStore)this.store).allowReadOnlySelect())) {
            try {
               this.protocol.close();
               this.releaseProtocol(true);
            } catch (ProtocolException var60) {
               try {
                  this.protocol.logout();
               } catch (ProtocolException var58) {
               } finally {
                  this.releaseProtocol(false);
               }
            } finally {
               throw new ReadOnlyFolderException(this, "Cannot open in desired mode");
            }

            throw new ReadOnlyFolderException(this, "Cannot open in desired mode");
         }

         this.opened = true;
         this.reallyClosed = false;
         this.mode = mi.mode;
         this.availableFlags = mi.availableFlags;
         this.permanentFlags = mi.permanentFlags;
         this.total = this.realTotal = mi.total;
         this.recent = mi.recent;
         this.uidvalidity = mi.uidvalidity;
         this.uidnext = mi.uidnext;
         this.highestmodseq = mi.highestmodseq;
         this.messageCache = new MessageCache(this, (IMAPStore)this.store, this.total);
         if (mi.responses != null) {
            openEvents = new ArrayList<>();

            for(IMAPResponse ir : mi.responses) {
               if (ir.keyEquals("VANISHED")) {
                  String[] s = ir.readAtomStringList();
                  if (s != null && s.length == 1 && s[0].equalsIgnoreCase("EARLIER")) {
                     String uids = ir.readAtom();
                     UIDSet[] uidset = UIDSet.parseUIDSets(uids);
                     long[] luid = UIDSet.toArray(uidset, this.uidnext);
                     if (luid != null && luid.length > 0) {
                        openEvents.add(new MessageVanishedEvent(this, luid));
                     }
                  }
               } else if (ir.keyEquals("FETCH")) {
                  assert ir instanceof FetchResponse : "!ir instanceof FetchResponse";

                  Message msg = this.processFetchResponse((FetchResponse)ir);
                  if (msg != null) {
                     openEvents.add(new MessageChangedEvent(this, 1, msg));
                  }
               }
            }
         }
      }

      this.exists = true;
      this.attributes = null;
      this.type = 1;
      this.notifyConnectionListeners(1);
      return openEvents;
   }

   @Override
   public synchronized void fetch(Message[] msgs, FetchProfile fp) throws MessagingException {
      this.checkOpened();
      StringBuffer command = new StringBuffer();
      boolean first = true;
      boolean allHeaders = false;
      if (fp.contains(FetchProfile.Item.ENVELOPE)) {
         command.append(this.getEnvelopeCommand());
         first = false;
      }

      if (fp.contains(FetchProfile.Item.FLAGS)) {
         command.append(first ? "FLAGS" : " FLAGS");
         first = false;
      }

      if (fp.contains(FetchProfile.Item.CONTENT_INFO)) {
         command.append(first ? "BODYSTRUCTURE" : " BODYSTRUCTURE");
         first = false;
      }

      if (fp.contains(UIDFolder.FetchProfileItem.UID)) {
         command.append(first ? "UID" : " UID");
         first = false;
      }

      if (fp.contains(IMAPFolder.FetchProfileItem.HEADERS)) {
         allHeaders = true;
         if (this.protocol.isREV1()) {
            command.append(first ? "BODY.PEEK[HEADER]" : " BODY.PEEK[HEADER]");
         } else {
            command.append(first ? "RFC822.HEADER" : " RFC822.HEADER");
         }

         first = false;
      }

      if (fp.contains(IMAPFolder.FetchProfileItem.MESSAGE)) {
         allHeaders = true;
         if (this.protocol.isREV1()) {
            command.append(first ? "BODY.PEEK[]" : " BODY.PEEK[]");
         } else {
            command.append(first ? "RFC822" : " RFC822");
         }

         first = false;
      }

      if (fp.contains(FetchProfile.Item.SIZE) || fp.contains(IMAPFolder.FetchProfileItem.SIZE)) {
         command.append(first ? "RFC822.SIZE" : " RFC822.SIZE");
         first = false;
      }

      String[] hdrs = null;
      if (!allHeaders) {
         hdrs = fp.getHeaderNames();
         if (hdrs.length > 0) {
            if (!first) {
               command.append(" ");
            }

            command.append(this.createHeaderCommand(hdrs));
         }
      }

      FetchItem[] fitems = this.protocol.getFetchItems();

      for(int i = 0; i < fitems.length; ++i) {
         if (fp.contains(fitems[i].getFetchProfileItem())) {
            if (command.length() != 0) {
               command.append(" ");
            }

            command.append(fitems[i].getName());
         }
      }

      Utility.Condition condition = new IMAPMessage.FetchProfileCondition(fp, fitems);
      synchronized(this.messageCacheLock) {
         MessageSet[] msgsets = Utility.toMessageSet(msgs, condition);
         if (msgsets != null) {
            Response[] r = null;
            Vector v = new Vector();

            try {
               r = this.getProtocol().fetch(msgsets, command.toString());
            } catch (ConnectionException var21) {
               throw new FolderClosedException(this, var21.getMessage());
            } catch (CommandFailedException var22) {
            } catch (ProtocolException var23) {
               throw new MessagingException(var23.getMessage(), var23);
            }

            if (r != null) {
               for(int i = 0; i < r.length; ++i) {
                  if (r[i] != null) {
                     if (!(r[i] instanceof FetchResponse)) {
                        v.addElement(r[i]);
                     } else {
                        FetchResponse f = (FetchResponse)r[i];
                        IMAPMessage msg = this.getMessageBySeqNumber(f.getNumber());
                        int count = f.getItemCount();
                        boolean unsolicitedFlags = false;

                        for(int j = 0; j < count; ++j) {
                           Item item = f.getItem(j);
                           if (!(item instanceof Flags) || fp.contains(FetchProfile.Item.FLAGS) && msg != null) {
                              if (msg != null) {
                                 msg.handleFetchItem(item, hdrs, allHeaders);
                              }
                           } else {
                              unsolicitedFlags = true;
                           }
                        }

                        if (msg != null) {
                           msg.handleExtensionFetchItems(f.getExtensionItems());
                        }

                        if (unsolicitedFlags) {
                           v.addElement(f);
                        }
                     }
                  }
               }

               int size = v.size();
               if (size != 0) {
                  Response[] responses = new Response[size];
                  v.copyInto(responses);
                  this.handleResponses(responses);
               }
            }
         }
      }
   }

   protected String getEnvelopeCommand() {
      return "ENVELOPE INTERNALDATE RFC822.SIZE";
   }

   protected IMAPMessage newIMAPMessage(int msgnum) {
      return new IMAPMessage(this, msgnum);
   }

   private String createHeaderCommand(String[] hdrs) {
      StringBuffer sb;
      if (this.protocol.isREV1()) {
         sb = new StringBuffer("BODY.PEEK[HEADER.FIELDS (");
      } else {
         sb = new StringBuffer("RFC822.HEADER.LINES (");
      }

      for(int i = 0; i < hdrs.length; ++i) {
         if (i > 0) {
            sb.append(" ");
         }

         sb.append(hdrs[i]);
      }

      if (this.protocol.isREV1()) {
         sb.append(")]");
      } else {
         sb.append(")");
      }

      return sb.toString();
   }

   @Override
   public synchronized void setFlags(Message[] msgs, Flags flag, boolean value) throws MessagingException {
      this.checkOpened();
      this.checkFlags(flag);
      if (msgs.length != 0) {
         synchronized(this.messageCacheLock) {
            try {
               IMAPProtocol p = this.getProtocol();
               MessageSet[] ms = Utility.toMessageSet(msgs, null);
               if (ms == null) {
                  throw new MessageRemovedException("Messages have been removed");
               }

               p.storeFlags(ms, flag, value);
            } catch (ConnectionException var8) {
               throw new FolderClosedException(this, var8.getMessage());
            } catch (ProtocolException var9) {
               throw new MessagingException(var9.getMessage(), var9);
            }
         }
      }
   }

   @Override
   public synchronized void setFlags(int start, int end, Flags flag, boolean value) throws MessagingException {
      this.checkOpened();
      Message[] msgs = new Message[end - start + 1];
      int i = 0;

      for(int n = start; n <= end; ++n) {
         msgs[i++] = this.getMessage(n);
      }

      this.setFlags(msgs, flag, value);
   }

   @Override
   public synchronized void setFlags(int[] msgnums, Flags flag, boolean value) throws MessagingException {
      this.checkOpened();
      Message[] msgs = new Message[msgnums.length];

      for(int i = 0; i < msgnums.length; ++i) {
         msgs[i] = this.getMessage(msgnums[i]);
      }

      this.setFlags(msgs, flag, value);
   }

   @Override
   public synchronized void close(boolean expunge) throws MessagingException {
      this.close(expunge, false);
   }

   public synchronized void forceClose() throws MessagingException {
      this.close(false, true);
   }

   private void close(boolean expunge, boolean force) throws MessagingException {
      assert Thread.holdsLock(this);

      synchronized(this.messageCacheLock) {
         if (!this.opened && this.reallyClosed) {
            throw new IllegalStateException("This operation is not allowed on a closed folder");
         } else {
            this.reallyClosed = true;
            if (this.opened) {
               boolean reuseProtocol = true;

               try {
                  this.waitIfIdle();
                  if (force) {
                     this.logger.log(Level.FINE, "forcing folder {0} to close", this.fullName);
                     if (this.protocol != null) {
                        this.protocol.disconnect();
                     }
                  } else if (((IMAPStore)this.store).isConnectionPoolFull()) {
                     this.logger.fine("pool is full, not adding an Authenticated connection");
                     if (expunge && this.protocol != null) {
                        this.protocol.close();
                     }

                     if (this.protocol != null) {
                        this.protocol.logout();
                     }
                  } else if (!expunge && this.mode == 2) {
                     try {
                        if (this.protocol != null && this.protocol.hasCapability("UNSELECT")) {
                           this.protocol.unselect();
                        } else if (this.protocol != null) {
                           boolean selected = true;

                           try {
                              this.protocol.examine(this.fullName);
                           } catch (CommandFailedException var15) {
                              selected = false;
                           }

                           if (selected && this.protocol != null) {
                              this.protocol.close();
                           }
                        }
                     } catch (ProtocolException var16) {
                        reuseProtocol = false;
                     }
                  } else if (this.protocol != null) {
                     this.protocol.close();
                  }
               } catch (ProtocolException var17) {
                  throw new MessagingException(var17.getMessage(), var17);
               } finally {
                  if (this.opened) {
                     this.cleanup(reuseProtocol);
                  }
               }
            }
         }
      }
   }

   private void cleanup(boolean returnToPool) {
      assert Thread.holdsLock(this.messageCacheLock);

      this.releaseProtocol(returnToPool);
      this.messageCache = null;
      this.uidTable = null;
      this.exists = false;
      this.attributes = null;
      this.opened = false;
      this.idleState = 0;
      this.notifyConnectionListeners(3);
   }

   @Override
   public synchronized boolean isOpen() {
      synchronized(this.messageCacheLock) {
         if (this.opened) {
            try {
               this.keepConnectionAlive(false);
            } catch (ProtocolException var4) {
            }
         }
      }

      return this.opened;
   }

   @Override
   public synchronized Flags getPermanentFlags() {
      return this.permanentFlags == null ? null : (Flags)this.permanentFlags.clone();
   }

   @Override
   public synchronized int getMessageCount() throws MessagingException {
      if (!this.opened) {
         this.checkExists();

         try {
            Status status = this.getStatus();
            return status.total;
         } catch (BadCommandException var18) {
            IMAPProtocol p = null;

            int var4;
            try {
               p = this.getStoreProtocol();
               MailboxInfo minfo = p.examine(this.fullName);
               p.close();
               var4 = minfo.total;
            } catch (ProtocolException var16) {
               throw new MessagingException(var16.getMessage(), var16);
            } finally {
               this.releaseStoreProtocol(p);
            }

            return var4;
         } catch (ConnectionException var19) {
            throw new StoreClosedException(this.store, var19.getMessage());
         } catch (ProtocolException var20) {
            throw new MessagingException(var20.getMessage(), var20);
         }
      } else {
         synchronized(this.messageCacheLock) {
            int var10000;
            try {
               this.keepConnectionAlive(true);
               var10000 = this.total;
            } catch (ConnectionException var21) {
               throw new FolderClosedException(this, var21.getMessage());
            } catch (ProtocolException var22) {
               throw new MessagingException(var22.getMessage(), var22);
            }

            return var10000;
         }
      }
   }

   @Override
   public synchronized int getNewMessageCount() throws MessagingException {
      if (!this.opened) {
         this.checkExists();

         try {
            Status status = this.getStatus();
            return status.recent;
         } catch (BadCommandException var18) {
            IMAPProtocol p = null;

            int var4;
            try {
               p = this.getStoreProtocol();
               MailboxInfo minfo = p.examine(this.fullName);
               p.close();
               var4 = minfo.recent;
            } catch (ProtocolException var16) {
               throw new MessagingException(var16.getMessage(), var16);
            } finally {
               this.releaseStoreProtocol(p);
            }

            return var4;
         } catch (ConnectionException var19) {
            throw new StoreClosedException(this.store, var19.getMessage());
         } catch (ProtocolException var20) {
            throw new MessagingException(var20.getMessage(), var20);
         }
      } else {
         synchronized(this.messageCacheLock) {
            int var10000;
            try {
               this.keepConnectionAlive(true);
               var10000 = this.recent;
            } catch (ConnectionException var21) {
               throw new FolderClosedException(this, var21.getMessage());
            } catch (ProtocolException var22) {
               throw new MessagingException(var22.getMessage(), var22);
            }

            return var10000;
         }
      }
   }

   @Override
   public synchronized int getUnreadMessageCount() throws MessagingException {
      if (!this.opened) {
         this.checkExists();

         try {
            Status status = this.getStatus();
            return status.unseen;
         } catch (BadCommandException var5) {
            return -1;
         } catch (ConnectionException var6) {
            throw new StoreClosedException(this.store, var6.getMessage());
         } catch (ProtocolException var7) {
            throw new MessagingException(var7.getMessage(), var7);
         }
      } else {
         Flags f = new Flags();
         f.add(Flags.Flag.SEEN);

         try {
            synchronized(this.messageCacheLock) {
               int[] matches = this.getProtocol().search(new FlagTerm(f, false));
               return matches.length;
            }
         } catch (ConnectionException var9) {
            throw new FolderClosedException(this, var9.getMessage());
         } catch (ProtocolException var10) {
            throw new MessagingException(var10.getMessage(), var10);
         }
      }
   }

   @Override
   public synchronized int getDeletedMessageCount() throws MessagingException {
      if (!this.opened) {
         this.checkExists();
         return -1;
      } else {
         Flags f = new Flags();
         f.add(Flags.Flag.DELETED);

         try {
            synchronized(this.messageCacheLock) {
               int[] matches = this.getProtocol().search(new FlagTerm(f, true));
               return matches.length;
            }
         } catch (ConnectionException var6) {
            throw new FolderClosedException(this, var6.getMessage());
         } catch (ProtocolException var7) {
            throw new MessagingException(var7.getMessage(), var7);
         }
      }
   }

   private Status getStatus() throws ProtocolException {
      int statusCacheTimeout = ((IMAPStore)this.store).getStatusCacheTimeout();
      if (statusCacheTimeout > 0 && this.cachedStatus != null && System.currentTimeMillis() - this.cachedStatusTime < (long)statusCacheTimeout) {
         return this.cachedStatus;
      } else {
         IMAPProtocol p = null;

         Status var4;
         try {
            p = this.getStoreProtocol();
            Status s = p.status(this.fullName, null);
            if (statusCacheTimeout > 0) {
               this.cachedStatus = s;
               this.cachedStatusTime = System.currentTimeMillis();
            }

            var4 = s;
         } finally {
            this.releaseStoreProtocol(p);
         }

         return var4;
      }
   }

   @Override
   public synchronized Message getMessage(int msgnum) throws MessagingException {
      this.checkOpened();
      this.checkRange(msgnum);
      return this.messageCache.getMessage(msgnum);
   }

   @Override
   public synchronized void appendMessages(Message[] msgs) throws MessagingException {
      this.checkExists();
      int maxsize = ((IMAPStore)this.store).getAppendBufferSize();

      for(int i = 0; i < msgs.length; ++i) {
         Message m = msgs[i];
         final Date d = m.getReceivedDate();
         if (d == null) {
            d = m.getSentDate();
         }

         final Flags f = m.getFlags();

         final MessageLiteral mos;
         try {
            mos = new MessageLiteral(m, m.getSize() > maxsize ? 0 : maxsize);
         } catch (IOException var10) {
            throw new MessagingException("IOException while appending messages", var10);
         } catch (MessageRemovedException var11) {
            continue;
         }

         this.doCommand(new IMAPFolder.ProtocolCommand() {
            @Override
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
               p.append(IMAPFolder.this.fullName, f, d, mos);
               return null;
            }
         });
      }
   }

   public synchronized AppendUID[] appendUIDMessages(Message[] msgs) throws MessagingException {
      this.checkExists();
      int maxsize = ((IMAPStore)this.store).getAppendBufferSize();
      AppendUID[] uids = new AppendUID[msgs.length];

      for(int i = 0; i < msgs.length; ++i) {
         Message m = msgs[i];

         final MessageLiteral mos;
         try {
            mos = new MessageLiteral(m, m.getSize() > maxsize ? 0 : maxsize);
         } catch (IOException var11) {
            throw new MessagingException("IOException while appending messages", var11);
         } catch (MessageRemovedException var12) {
            continue;
         }

         final Date d = m.getReceivedDate();
         if (d == null) {
            d = m.getSentDate();
         }

         final Flags f = m.getFlags();
         AppendUID auid = (AppendUID)this.doCommand(new IMAPFolder.ProtocolCommand() {
            @Override
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
               return p.appenduid(IMAPFolder.this.fullName, f, d, mos);
            }
         });
         uids[i] = auid;
      }

      return uids;
   }

   public synchronized Message[] addMessages(Message[] msgs) throws MessagingException {
      this.checkOpened();
      Message[] rmsgs = new MimeMessage[msgs.length];
      AppendUID[] uids = this.appendUIDMessages(msgs);

      for(int i = 0; i < uids.length; ++i) {
         AppendUID auid = uids[i];
         if (auid != null && auid.uidvalidity == this.uidvalidity) {
            try {
               rmsgs[i] = this.getMessageByUID(auid.uid);
            } catch (MessagingException var7) {
            }
         }
      }

      return rmsgs;
   }

   @Override
   public synchronized void copyMessages(Message[] msgs, Folder folder) throws MessagingException {
      this.checkOpened();
      if (msgs.length != 0) {
         if (folder.getStore() == this.store) {
            synchronized(this.messageCacheLock) {
               try {
                  IMAPProtocol p = this.getProtocol();
                  MessageSet[] ms = Utility.toMessageSet(msgs, null);
                  if (ms == null) {
                     throw new MessageRemovedException("Messages have been removed");
                  }

                  p.copy(ms, folder.getFullName());
               } catch (CommandFailedException var7) {
                  if (var7.getMessage().indexOf("TRYCREATE") != -1) {
                     throw new FolderNotFoundException(folder, folder.getFullName() + " does not exist");
                  }

                  throw new MessagingException(var7.getMessage(), var7);
               } catch (ConnectionException var8) {
                  throw new FolderClosedException(this, var8.getMessage());
               } catch (ProtocolException var9) {
                  throw new MessagingException(var9.getMessage(), var9);
               }
            }
         } else {
            super.copyMessages(msgs, folder);
         }
      }
   }

   public synchronized AppendUID[] copyUIDMessages(Message[] msgs, Folder folder) throws MessagingException {
      this.checkOpened();
      if (msgs.length == 0) {
         return null;
      } else if (folder.getStore() == this.store) {
         synchronized(this.messageCacheLock) {
            AppendUID[] var10000;
            try {
               IMAPProtocol p = this.getProtocol();
               MessageSet[] ms = Utility.toMessageSet(msgs, null);
               if (ms == null) {
                  throw new MessageRemovedException("Messages have been removed");
               }

               CopyUID cuid = p.copyuid(ms, folder.getFullName());
               long[] srcuids = UIDSet.toArray(cuid.src);
               long[] dstuids = UIDSet.toArray(cuid.dst);
               Message[] srcmsgs = this.getMessagesByUID(srcuids);
               AppendUID[] result = new AppendUID[msgs.length];

               label66:
               for(int i = 0; i < srcmsgs.length; ++i) {
                  int j = i;

                  while(msgs[j] != srcmsgs[i]) {
                     if (++j >= msgs.length) {
                        j = 0;
                     }

                     if (j == i) {
                        continue label66;
                     }
                  }

                  result[j] = new AppendUID(cuid.uidvalidity, dstuids[i]);
               }

               label82:
               for(int i = 0; i < msgs.length; ++i) {
                  int j = i;

                  while(msgs[i] != srcmsgs[j]) {
                     if (++j >= msgs.length) {
                        j = 0;
                     }

                     if (j == i) {
                        continue label82;
                     }
                  }

                  result[i] = new AppendUID(cuid.uidvalidity, dstuids[j]);
               }

               var10000 = result;
            } catch (CommandFailedException var14) {
               if (var14.getMessage().indexOf("TRYCREATE") != -1) {
                  throw new FolderNotFoundException(folder, folder.getFullName() + " does not exist");
               }

               throw new MessagingException(var14.getMessage(), var14);
            } catch (ConnectionException var15) {
               throw new FolderClosedException(this, var15.getMessage());
            } catch (ProtocolException var16) {
               throw new MessagingException(var16.getMessage(), var16);
            }

            return var10000;
         }
      } else {
         throw new MessagingException("can't copyUIDMessages to a different store");
      }
   }

   @Override
   public synchronized Message[] expunge() throws MessagingException {
      return this.expunge(null);
   }

   public synchronized Message[] expunge(Message[] msgs) throws MessagingException {
      this.checkOpened();
      if (msgs != null) {
         FetchProfile fp = new FetchProfile();
         fp.add(UIDFolder.FetchProfileItem.UID);
         this.fetch(msgs, fp);
      }

      IMAPMessage[] rmsgs;
      synchronized(this.messageCacheLock) {
         this.doExpungeNotification = false;

         try {
            IMAPProtocol p = this.getProtocol();
            if (msgs != null) {
               p.uidexpunge(Utility.toUIDSet(msgs));
            } else {
               p.expunge();
            }
         } catch (CommandFailedException var14) {
            if (this.mode != 2) {
               throw new IllegalStateException("Cannot expunge READ_ONLY folder: " + this.fullName);
            }

            throw new MessagingException(var14.getMessage(), var14);
         } catch (ConnectionException var15) {
            throw new FolderClosedException(this, var15.getMessage());
         } catch (ProtocolException var16) {
            throw new MessagingException(var16.getMessage(), var16);
         } finally {
            this.doExpungeNotification = true;
         }

         if (msgs != null) {
            rmsgs = this.messageCache.removeExpungedMessages(msgs);
         } else {
            rmsgs = this.messageCache.removeExpungedMessages();
         }

         if (this.uidTable != null) {
            for(int i = 0; i < rmsgs.length; ++i) {
               IMAPMessage m = rmsgs[i];
               long uid = m.getUID();
               if (uid != -1L) {
                  this.uidTable.remove(uid);
               }
            }
         }

         this.total = this.messageCache.size();
      }

      if (rmsgs.length > 0) {
         this.notifyMessageRemovedListeners(true, rmsgs);
      }

      return rmsgs;
   }

   @Override
   public synchronized Message[] search(SearchTerm term) throws MessagingException {
      this.checkOpened();

      try {
         Message[] matchMsgs = null;
         synchronized(this.messageCacheLock) {
            int[] matches = this.getProtocol().search(term);
            if (matches != null) {
               matchMsgs = new IMAPMessage[matches.length];
               int size = this.messageCache.size();

               for(int i = 0; i < matches.length; ++i) {
                  if (matches[i] > size) {
                     if (this.logger.isLoggable(Level.FINE)) {
                        this.logger.fine("ignoring message number " + matches[i] + " in search results, " + "outside range " + size);
                     }
                  } else {
                     matchMsgs[i] = this.getMessageBySeqNumber(matches[i]);
                  }
               }
            }
         }

         return matchMsgs;
      } catch (CommandFailedException var9) {
         return super.search(term);
      } catch (SearchException var10) {
         if (((IMAPStore)this.store).throwSearchException()) {
            throw var10;
         } else {
            return super.search(term);
         }
      } catch (ConnectionException var11) {
         throw new FolderClosedException(this, var11.getMessage());
      } catch (ProtocolException var12) {
         throw new MessagingException(var12.getMessage(), var12);
      }
   }

   @Override
   public synchronized Message[] search(SearchTerm term, Message[] msgs) throws MessagingException {
      this.checkOpened();
      if (msgs.length == 0) {
         return msgs;
      } else {
         try {
            Message[] matchMsgs = null;
            synchronized(this.messageCacheLock) {
               IMAPProtocol p = this.getProtocol();
               MessageSet[] ms = Utility.toMessageSet(msgs, null);
               if (ms == null) {
                  throw new MessageRemovedException("Messages have been removed");
               }

               int[] matches = p.search(ms, term);
               if (matches != null) {
                  matchMsgs = new IMAPMessage[matches.length];

                  for(int i = 0; i < matches.length; ++i) {
                     matchMsgs[i] = this.getMessageBySeqNumber(matches[i]);
                  }
               }
            }

            return matchMsgs;
         } catch (CommandFailedException var11) {
            return super.search(term, msgs);
         } catch (SearchException var12) {
            return super.search(term, msgs);
         } catch (ConnectionException var13) {
            throw new FolderClosedException(this, var13.getMessage());
         } catch (ProtocolException var14) {
            throw new MessagingException(var14.getMessage(), var14);
         }
      }
   }

   public synchronized Message[] getSortedMessages(SortTerm[] term) throws MessagingException {
      return this.getSortedMessages(term, null);
   }

   public synchronized Message[] getSortedMessages(SortTerm[] term, SearchTerm sterm) throws MessagingException {
      this.checkOpened();

      try {
         Message[] matchMsgs = null;
         synchronized(this.messageCacheLock) {
            int[] matches = this.getProtocol().sort(term, sterm);
            if (matches != null) {
               matchMsgs = new IMAPMessage[matches.length];

               for(int i = 0; i < matches.length; ++i) {
                  matchMsgs[i] = this.getMessageBySeqNumber(matches[i]);
               }
            }
         }

         return matchMsgs;
      } catch (CommandFailedException var9) {
         throw new MessagingException(var9.getMessage(), var9);
      } catch (SearchException var10) {
         throw new MessagingException(var10.getMessage(), var10);
      } catch (ConnectionException var11) {
         throw new FolderClosedException(this, var11.getMessage());
      } catch (ProtocolException var12) {
         throw new MessagingException(var12.getMessage(), var12);
      }
   }

   @Override
   public synchronized void addMessageCountListener(MessageCountListener l) {
      super.addMessageCountListener(l);
      this.hasMessageCountListener = true;
   }

   @Override
   public synchronized long getUIDValidity() throws MessagingException {
      if (this.opened) {
         return this.uidvalidity;
      } else {
         IMAPProtocol p = null;
         Status status = null;

         try {
            p = this.getStoreProtocol();
            String[] item = new String[]{"UIDVALIDITY"};
            status = p.status(this.fullName, item);
         } catch (BadCommandException var10) {
            throw new MessagingException("Cannot obtain UIDValidity", var10);
         } catch (ConnectionException var11) {
            this.throwClosedException(var11);
         } catch (ProtocolException var12) {
            throw new MessagingException(var12.getMessage(), var12);
         } finally {
            this.releaseStoreProtocol(p);
         }

         return status.uidvalidity;
      }
   }

   public synchronized long getUIDNext() throws MessagingException {
      if (this.opened) {
         return this.uidnext;
      } else {
         IMAPProtocol p = null;
         Status status = null;

         try {
            p = this.getStoreProtocol();
            String[] item = new String[]{"UIDNEXT"};
            status = p.status(this.fullName, item);
         } catch (BadCommandException var10) {
            throw new MessagingException("Cannot obtain UIDNext", var10);
         } catch (ConnectionException var11) {
            this.throwClosedException(var11);
         } catch (ProtocolException var12) {
            throw new MessagingException(var12.getMessage(), var12);
         } finally {
            this.releaseStoreProtocol(p);
         }

         return status.uidnext;
      }
   }

   @Override
   public synchronized Message getMessageByUID(long uid) throws MessagingException {
      this.checkOpened();
      IMAPMessage m = null;

      try {
         synchronized(this.messageCacheLock) {
            Long l = uid;
            if (this.uidTable != null) {
               m = (IMAPMessage)this.uidTable.get(l);
               if (m != null) {
                  return m;
               }
            } else {
               this.uidTable = new Hashtable();
            }

            UID u = this.getProtocol().fetchSequenceNumber(uid);
            if (u != null && u.seqnum <= this.total) {
               m = this.getMessageBySeqNumber(u.seqnum);
               if (m != null) {
                  m.setUID(u.uid);
                  this.uidTable.put(l, m);
               }
            }

            return m;
         }
      } catch (ConnectionException var9) {
         throw new FolderClosedException(this, var9.getMessage());
      } catch (ProtocolException var10) {
         throw new MessagingException(var10.getMessage(), var10);
      }
   }

   @Override
   public synchronized Message[] getMessagesByUID(long start, long end) throws MessagingException {
      this.checkOpened();

      try {
         synchronized(this.messageCacheLock) {
            if (this.uidTable == null) {
               this.uidTable = new Hashtable();
            }

            UID[] ua = this.getProtocol().fetchSequenceNumbers(start, end);
            Message[] msgs = new Message[ua.length];

            for(int i = 0; i < ua.length; ++i) {
               IMAPMessage m = this.getMessageBySeqNumber(ua[i].seqnum);
               if (m != null) {
                  m.setUID(ua[i].uid);
                  msgs[i] = m;
                  this.uidTable.put(ua[i].uid, m);
               }
            }

            return msgs;
         }
      } catch (ConnectionException var12) {
         throw new FolderClosedException(this, var12.getMessage());
      } catch (ProtocolException var13) {
         throw new MessagingException(var13.getMessage(), var13);
      }
   }

   @Override
   public synchronized Message[] getMessagesByUID(long[] uids) throws MessagingException {
      this.checkOpened();

      try {
         synchronized(this.messageCacheLock) {
            long[] unavailUids = uids;
            if (this.uidTable != null) {
               Vector v = new Vector();

               for(int i = 0; i < uids.length; ++i) {
                  Long l;
                  if (!this.uidTable.containsKey(l = uids[i])) {
                     v.addElement(l);
                  }
               }

               int vsize = v.size();
               unavailUids = new long[vsize];

               for(int i = 0; i < vsize; ++i) {
                  unavailUids[i] = v.elementAt(i);
               }
            } else {
               this.uidTable = new Hashtable();
            }

            if (unavailUids.length > 0) {
               UID[] ua = this.getProtocol().fetchSequenceNumbers(unavailUids);

               for(int i = 0; i < ua.length; ++i) {
                  IMAPMessage m = this.getMessageBySeqNumber(ua[i].seqnum);
                  if (m != null) {
                     m.setUID(ua[i].uid);
                     this.uidTable.put(ua[i].uid, m);
                  }
               }
            }

            Message[] msgs = new Message[uids.length];

            for(int i = 0; i < uids.length; ++i) {
               msgs[i] = (Message)this.uidTable.get(uids[i]);
            }

            return msgs;
         }
      } catch (ConnectionException var10) {
         throw new FolderClosedException(this, var10.getMessage());
      } catch (ProtocolException var11) {
         throw new MessagingException(var11.getMessage(), var11);
      }
   }

   @Override
   public synchronized long getUID(Message message) throws MessagingException {
      if (message.getFolder() != this) {
         throw new NoSuchElementException("Message does not belong to this folder");
      } else {
         this.checkOpened();
         if (!(message instanceof IMAPMessage)) {
            throw new MessagingException("message is not an IMAPMessage");
         } else {
            IMAPMessage m = (IMAPMessage)message;
            long uid;
            if ((uid = m.getUID()) != -1L) {
               return uid;
            } else {
               synchronized(this.messageCacheLock) {
                  try {
                     IMAPProtocol p = this.getProtocol();
                     m.checkExpunged();
                     UID u = p.fetchUID(m.getSequenceNumber());
                     if (u != null) {
                        uid = u.uid;
                        m.setUID(uid);
                        if (this.uidTable == null) {
                           this.uidTable = new Hashtable();
                        }

                        this.uidTable.put(uid, m);
                     }
                  } catch (ConnectionException var9) {
                     throw new FolderClosedException(this, var9.getMessage());
                  } catch (ProtocolException var10) {
                     throw new MessagingException(var10.getMessage(), var10);
                  }

                  return uid;
               }
            }
         }
      }
   }

   private Message[] createMessagesForUIDs(long[] uids) {
      IMAPMessage[] msgs = new IMAPMessage[uids.length];

      for(int i = 0; i < uids.length; ++i) {
         IMAPMessage m = null;
         if (this.uidTable != null) {
            m = (IMAPMessage)this.uidTable.get(uids[i]);
         }

         if (m == null) {
            m = this.newIMAPMessage(-1);
            m.setUID(uids[i]);
            m.setExpunged(true);
         }

         msgs[i++] = m;
      }

      return msgs;
   }

   public synchronized long getHighestModSeq() throws MessagingException {
      if (this.opened) {
         return this.highestmodseq;
      } else {
         IMAPProtocol p = null;
         Status status = null;

         try {
            p = this.getStoreProtocol();
            if (!p.hasCapability("CONDSTORE")) {
               throw new BadCommandException("CONDSTORE not supported");
            }

            String[] item = new String[]{"HIGHESTMODSEQ"};
            status = p.status(this.fullName, item);
         } catch (BadCommandException var10) {
            throw new MessagingException("Cannot obtain HIGHESTMODSEQ", var10);
         } catch (ConnectionException var11) {
            this.throwClosedException(var11);
         } catch (ProtocolException var12) {
            throw new MessagingException(var12.getMessage(), var12);
         } finally {
            this.releaseStoreProtocol(p);
         }

         return status.highestmodseq;
      }
   }

   public synchronized Message[] getMessagesByUIDChangedSince(long start, long end, long modseq) throws MessagingException {
      this.checkOpened();

      try {
         synchronized(this.messageCacheLock) {
            IMAPProtocol p = this.getProtocol();
            if (!p.hasCapability("CONDSTORE")) {
               throw new BadCommandException("CONDSTORE not supported");
            } else {
               int[] nums = p.uidfetchChangedSince(start, end, modseq);
               Message[] msgs = new Message[nums.length];

               for(int i = 0; i < nums.length; ++i) {
                  msgs[i] = this.getMessageBySeqNumber(nums[i]);
               }

               return msgs;
            }
         }
      } catch (ConnectionException var14) {
         throw new FolderClosedException(this, var14.getMessage());
      } catch (ProtocolException var15) {
         throw new MessagingException(var15.getMessage(), var15);
      }
   }

   public Quota[] getQuota() throws MessagingException {
      return (Quota[])this.doOptionalCommand("QUOTA not supported", new IMAPFolder.ProtocolCommand() {
         @Override
         public Object doCommand(IMAPProtocol p) throws ProtocolException {
            return p.getQuotaRoot(IMAPFolder.this.fullName);
         }
      });
   }

   public void setQuota(final Quota quota) throws MessagingException {
      this.doOptionalCommand("QUOTA not supported", new IMAPFolder.ProtocolCommand() {
         @Override
         public Object doCommand(IMAPProtocol p) throws ProtocolException {
            p.setQuota(quota);
            return null;
         }
      });
   }

   public ACL[] getACL() throws MessagingException {
      return (ACL[])this.doOptionalCommand("ACL not supported", new IMAPFolder.ProtocolCommand() {
         @Override
         public Object doCommand(IMAPProtocol p) throws ProtocolException {
            return p.getACL(IMAPFolder.this.fullName);
         }
      });
   }

   public void addACL(ACL acl) throws MessagingException {
      this.setACL(acl, '\u0000');
   }

   public void removeACL(final String name) throws MessagingException {
      this.doOptionalCommand("ACL not supported", new IMAPFolder.ProtocolCommand() {
         @Override
         public Object doCommand(IMAPProtocol p) throws ProtocolException {
            p.deleteACL(IMAPFolder.this.fullName, name);
            return null;
         }
      });
   }

   public void addRights(ACL acl) throws MessagingException {
      this.setACL(acl, '+');
   }

   public void removeRights(ACL acl) throws MessagingException {
      this.setACL(acl, '-');
   }

   public Rights[] listRights(final String name) throws MessagingException {
      return (Rights[])this.doOptionalCommand("ACL not supported", new IMAPFolder.ProtocolCommand() {
         @Override
         public Object doCommand(IMAPProtocol p) throws ProtocolException {
            return p.listRights(IMAPFolder.this.fullName, name);
         }
      });
   }

   public Rights myRights() throws MessagingException {
      return (Rights)this.doOptionalCommand("ACL not supported", new IMAPFolder.ProtocolCommand() {
         @Override
         public Object doCommand(IMAPProtocol p) throws ProtocolException {
            return p.myRights(IMAPFolder.this.fullName);
         }
      });
   }

   private void setACL(final ACL acl, final char mod) throws MessagingException {
      this.doOptionalCommand("ACL not supported", new IMAPFolder.ProtocolCommand() {
         @Override
         public Object doCommand(IMAPProtocol p) throws ProtocolException {
            p.setACL(IMAPFolder.this.fullName, mod, acl);
            return null;
         }
      });
   }

   public synchronized String[] getAttributes() throws MessagingException {
      this.checkExists();
      if (this.attributes == null) {
         this.exists();
      }

      return this.attributes == null ? new String[0] : (String[])this.attributes.clone();
   }

   public void idle() throws MessagingException {
      this.idle(false);
   }

   public void idle(boolean once) throws MessagingException {
      synchronized(this) {
         if (this.protocol != null && this.protocol.getChannel() != null) {
            throw new MessagingException("idle method not supported with SocketChannels");
         }
      }

      this.startIdle(null);

      while(this.handleIdle(once)) {
      }

      int minidle = ((IMAPStore)this.store).getMinIdleTime();
      if (minidle > 0) {
         try {
            Thread.sleep((long)minidle);
         } catch (InterruptedException var4) {
         }
      }
   }

   void startIdle(IdleManager im) throws MessagingException {
      assert !Thread.holdsLock(this);

      this.idleManager = im;
      synchronized(this) {
         this.checkOpened();
         Boolean started = (Boolean)this.doOptionalCommand("IDLE not supported", new IMAPFolder.ProtocolCommand() {
            @Override
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
               if (IMAPFolder.this.idleState == 0) {
                  p.idleStart();
                  IMAPFolder.this.idleState = 1;
                  return Boolean.TRUE;
               } else {
                  try {
                     IMAPFolder.this.messageCacheLock.wait();
                  } catch (InterruptedException var3) {
                  }

                  return Boolean.FALSE;
               }
            }
         });
         if (started) {
            ;
         }
      }
   }

   boolean handleIdle(boolean once) throws MessagingException {
      Response r = this.protocol.readIdleResponse();

      try {
         synchronized(this.messageCacheLock) {
            try {
               if (r == null || this.protocol == null || !this.protocol.processIdleResponse(r)) {
                  this.idleState = 0;
                  this.messageCacheLock.notifyAll();
                  return false;
               }
            } catch (ProtocolException var6) {
               this.idleState = 0;
               this.messageCacheLock.notifyAll();
               throw var6;
            }

            if (once && this.idleState == 1) {
               this.protocol.idleAbort();
               this.idleState = 2;
            }

            return true;
         }
      } catch (ConnectionException var8) {
         this.throwClosedException(var8);
         return true;
      } catch (ProtocolException var9) {
         throw new MessagingException(var9.getMessage(), var9);
      }
   }

   void waitIfIdle() throws ProtocolException {
      assert Thread.holdsLock(this.messageCacheLock);

      while(this.idleState != 0) {
         if (this.idleState == 1) {
            IdleManager im = this.idleManager;
            if (im != null) {
               im.requestAbort(this);
            } else {
               this.protocol.idleAbort();
            }

            this.idleState = 2;
         }

         try {
            this.messageCacheLock.wait();
         } catch (InterruptedException var2) {
         }
      }
   }

   void idleAbort() {
      this.idleManager = null;
      if (this.protocol != null) {
         this.protocol.idleAbort();
      }
   }

   SocketChannel getChannel() {
      return this.protocol != null ? this.protocol.getChannel() : null;
   }

   public Map<String, String> id(final Map<String, String> clientParams) throws MessagingException {
      this.checkOpened();
      return (Map<String, String>)this.doOptionalCommand("ID not supported", new IMAPFolder.ProtocolCommand() {
         @Override
         public Object doCommand(IMAPProtocol p) throws ProtocolException {
            return p.id(clientParams);
         }
      });
   }

   public long getStatusItem(String item) throws MessagingException {
      if (!this.opened) {
         this.checkExists();
         IMAPProtocol p = null;
         Status status = null;

         long var5;
         try {
            p = this.getStoreProtocol();
            String[] items = new String[]{item};
            status = p.status(this.fullName, items);
            return status.getItem(item);
         } catch (BadCommandException var13) {
            var5 = -1L;
         } catch (ConnectionException var14) {
            throw new StoreClosedException(this.store, var14.getMessage());
         } catch (ProtocolException var15) {
            throw new MessagingException(var15.getMessage(), var15);
         } finally {
            this.releaseStoreProtocol(p);
         }

         return var5;
      } else {
         return -1L;
      }
   }

   @Override
   public void handleResponse(Response r) {
      assert Thread.holdsLock(this.messageCacheLock);

      if (r.isOK() || r.isNO() || r.isBAD() || r.isBYE()) {
         ((IMAPStore)this.store).handleResponseCode(r);
      }

      if (r.isBYE()) {
         if (this.opened) {
            this.cleanup(false);
         }
      } else if (r.isOK()) {
         r.skipSpaces();
         if (r.readByte() == 91) {
            String s = r.readAtom();
            if (s.equalsIgnoreCase("HIGHESTMODSEQ")) {
               this.highestmodseq = r.readLong();
            }
         }

         r.reset();
      } else if (r.isUnTagged()) {
         if (!(r instanceof IMAPResponse)) {
            this.logger.fine("UNEXPECTED RESPONSE : " + r.toString());
         } else {
            IMAPResponse ir = (IMAPResponse)r;
            if (ir.keyEquals("EXISTS")) {
               int exists = ir.getNumber();
               if (exists <= this.realTotal) {
                  return;
               }

               int count = exists - this.realTotal;
               Message[] msgs = new Message[count];
               this.messageCache.addMessages(count, this.realTotal + 1);
               int oldtotal = this.total;
               this.realTotal += count;
               this.total += count;
               if (this.hasMessageCountListener) {
                  for(int i = 0; i < count; ++i) {
                     msgs[i] = this.messageCache.getMessage(++oldtotal);
                  }

                  this.notifyMessageAddedListeners(msgs);
               }
            } else if (ir.keyEquals("EXPUNGE")) {
               int seqnum = ir.getNumber();
               Message[] msgs = null;
               if (this.doExpungeNotification && this.hasMessageCountListener) {
                  msgs = new Message[]{this.getMessageBySeqNumber(seqnum)};
               }

               this.messageCache.expungeMessage(seqnum);
               --this.realTotal;
               if (msgs != null) {
                  this.notifyMessageRemovedListeners(false, msgs);
               }
            } else if (ir.keyEquals("VANISHED")) {
               String[] s = ir.readAtomStringList();
               if (s == null) {
                  String uids = ir.readAtom();
                  UIDSet[] uidset = UIDSet.parseUIDSets(uids);
                  this.realTotal = (int)((long)this.realTotal - UIDSet.size(uidset));
                  long[] luid = UIDSet.toArray(uidset);
                  Message[] msgs = this.createMessagesForUIDs(luid);

                  for(Message m : msgs) {
                     if (m.getMessageNumber() > 0) {
                        this.messageCache.expungeMessage(m.getMessageNumber());
                     }
                  }

                  if (this.doExpungeNotification && this.hasMessageCountListener) {
                     this.notifyMessageRemovedListeners(true, msgs);
                  }
               }
            } else if (ir.keyEquals("FETCH")) {
               assert ir instanceof FetchResponse : "!ir instanceof FetchResponse";

               Message msg = this.processFetchResponse((FetchResponse)ir);
               if (msg != null) {
                  this.notifyMessageChangedListeners(1, msg);
               }
            } else if (ir.keyEquals("RECENT")) {
               this.recent = ir.getNumber();
            }
         }
      }
   }

   private Message processFetchResponse(FetchResponse fr) {
      IMAPMessage msg = this.getMessageBySeqNumber(fr.getNumber());
      if (msg != null) {
         boolean notify = false;
         UID uid = fr.getItem(UID.class);
         if (uid != null && msg.getUID() != uid.uid) {
            msg.setUID(uid.uid);
            if (this.uidTable == null) {
               this.uidTable = new Hashtable();
            }

            this.uidTable.put(uid.uid, msg);
            notify = true;
         }

         MODSEQ modseq = fr.getItem(MODSEQ.class);
         if (modseq != null && msg._getModSeq() != modseq.modseq) {
            msg.setModSeq(modseq.modseq);
            notify = true;
         }

         FLAGS flags = fr.getItem(FLAGS.class);
         if (flags != null) {
            msg._setFlags(flags);
            notify = true;
         }

         if (!notify) {
            msg = null;
         }
      }

      return msg;
   }

   void handleResponses(Response[] r) {
      for(int i = 0; i < r.length; ++i) {
         if (r[i] != null) {
            this.handleResponse(r[i]);
         }
      }
   }

   protected synchronized IMAPProtocol getStoreProtocol() throws ProtocolException {
      this.connectionPoolLogger.fine("getStoreProtocol() borrowing a connection");
      return ((IMAPStore)this.store).getFolderStoreProtocol();
   }

   protected synchronized void throwClosedException(ConnectionException cex) throws FolderClosedException, StoreClosedException {
      if ((this.protocol == null || cex.getProtocol() != this.protocol) && (this.protocol != null || this.reallyClosed)) {
         throw new StoreClosedException(this.store, cex.getMessage());
      } else {
         throw new FolderClosedException(this, cex.getMessage());
      }
   }

   protected IMAPProtocol getProtocol() throws ProtocolException {
      assert Thread.holdsLock(this.messageCacheLock);

      this.waitIfIdle();
      return this.protocol;
   }

   public Object doCommand(IMAPFolder.ProtocolCommand cmd) throws MessagingException {
      try {
         return this.doProtocolCommand(cmd);
      } catch (ConnectionException var3) {
         this.throwClosedException(var3);
         return null;
      } catch (ProtocolException var4) {
         throw new MessagingException(var4.getMessage(), var4);
      }
   }

   public Object doOptionalCommand(String err, IMAPFolder.ProtocolCommand cmd) throws MessagingException {
      try {
         return this.doProtocolCommand(cmd);
      } catch (BadCommandException var4) {
         throw new MessagingException(err, var4);
      } catch (ConnectionException var5) {
         this.throwClosedException(var5);
         return null;
      } catch (ProtocolException var6) {
         throw new MessagingException(var6.getMessage(), var6);
      }
   }

   public Object doCommandIgnoreFailure(IMAPFolder.ProtocolCommand cmd) throws MessagingException {
      try {
         return this.doProtocolCommand(cmd);
      } catch (CommandFailedException var3) {
         return null;
      } catch (ConnectionException var4) {
         this.throwClosedException(var4);
         return null;
      } catch (ProtocolException var5) {
         throw new MessagingException(var5.getMessage(), var5);
      }
   }

   protected Object doProtocolCommand(IMAPFolder.ProtocolCommand cmd) throws ProtocolException {
      synchronized(this) {
         if (this.protocol != null) {
            Object var10000;
            synchronized(this.messageCacheLock) {
               var10000 = cmd.doCommand(this.getProtocol());
            }

            return var10000;
         }
      }

      IMAPProtocol p = null;

      Object var3;
      try {
         p = this.getStoreProtocol();
         var3 = cmd.doCommand(p);
      } finally {
         this.releaseStoreProtocol(p);
      }

      return var3;
   }

   protected synchronized void releaseStoreProtocol(IMAPProtocol p) {
      if (p != this.protocol) {
         ((IMAPStore)this.store).releaseFolderStoreProtocol(p);
      } else {
         this.logger.fine("releasing our protocol as store protocol?");
      }
   }

   protected void releaseProtocol(boolean returnToPool) {
      if (this.protocol != null) {
         this.protocol.removeResponseHandler(this);
         if (returnToPool) {
            ((IMAPStore)this.store).releaseProtocol(this, this.protocol);
         } else {
            this.protocol.disconnect();
            ((IMAPStore)this.store).releaseProtocol(this, null);
         }

         this.protocol = null;
      }
   }

   protected void keepConnectionAlive(boolean keepStoreAlive) throws ProtocolException {
      if (System.currentTimeMillis() - this.protocol.getTimestamp() > 1000L) {
         this.waitIfIdle();
         if (this.protocol != null) {
            this.protocol.noop();
         }
      }

      if (keepStoreAlive && ((IMAPStore)this.store).hasSeparateStoreConnection()) {
         IMAPProtocol p = null;

         try {
            p = ((IMAPStore)this.store).getFolderStoreProtocol();
            if (System.currentTimeMillis() - p.getTimestamp() > 1000L) {
               p.noop();
            }
         } finally {
            ((IMAPStore)this.store).releaseFolderStoreProtocol(p);
         }
      }
   }

   protected IMAPMessage getMessageBySeqNumber(int seqnum) {
      return this.messageCache.getMessageBySeqnum(seqnum);
   }

   private boolean isDirectory() {
      return (this.type & 2) != 0;
   }

   public static class FetchProfileItem extends FetchProfile.Item {
      public static final IMAPFolder.FetchProfileItem HEADERS = new IMAPFolder.FetchProfileItem("HEADERS");
      /** @deprecated */
      public static final IMAPFolder.FetchProfileItem SIZE = new IMAPFolder.FetchProfileItem("SIZE");
      public static final IMAPFolder.FetchProfileItem MESSAGE = new IMAPFolder.FetchProfileItem("MESSAGE");

      protected FetchProfileItem(String name) {
         super(name);
      }
   }

   public interface ProtocolCommand {
      Object doCommand(IMAPProtocol var1) throws ProtocolException;
   }
}
