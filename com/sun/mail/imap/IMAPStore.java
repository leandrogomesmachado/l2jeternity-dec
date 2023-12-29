package com.sun.mail.imap;

import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseHandler;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.ListInfo;
import com.sun.mail.imap.protocol.Namespaces;
import com.sun.mail.util.MailConnectException;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.PropUtil;
import com.sun.mail.util.SocketConnectException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Quota;
import javax.mail.QuotaAwareStore;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import javax.mail.URLName;

public class IMAPStore extends Store implements QuotaAwareStore, ResponseHandler {
   public static final int RESPONSE = 1000;
   public static final String ID_NAME = "name";
   public static final String ID_VERSION = "version";
   public static final String ID_OS = "os";
   public static final String ID_OS_VERSION = "os-version";
   public static final String ID_VENDOR = "vendor";
   public static final String ID_SUPPORT_URL = "support-url";
   public static final String ID_ADDRESS = "address";
   public static final String ID_DATE = "date";
   public static final String ID_COMMAND = "command";
   public static final String ID_ARGUMENTS = "arguments";
   public static final String ID_ENVIRONMENT = "environment";
   protected final String name;
   protected final int defaultPort;
   protected final boolean isSSL;
   private final int blksize;
   private boolean ignoreSize;
   private final int statusCacheTimeout;
   private final int appendBufferSize;
   private final int minIdleTime;
   private volatile int port = -1;
   protected String host;
   protected String user;
   protected String password;
   protected String proxyAuthUser;
   protected String authorizationID;
   protected String saslRealm;
   private Namespaces namespaces;
   private boolean disableAuthLogin = false;
   private boolean disableAuthPlain = false;
   private boolean disableAuthNtlm = false;
   private boolean enableStartTLS = false;
   private boolean requireStartTLS = false;
   private boolean usingSSL = false;
   private boolean enableSASL = false;
   private String[] saslMechanisms;
   private boolean forcePasswordRefresh = false;
   private boolean enableResponseEvents = false;
   private boolean enableImapEvents = false;
   private String guid;
   private boolean throwSearchException = false;
   private boolean peek = false;
   private volatile boolean connectionFailed = false;
   private volatile boolean forceClose = false;
   private final Object connectionFailedLock = new Object();
   private boolean debugusername;
   private boolean debugpassword;
   protected MailLogger logger;
   private boolean messageCacheDebug;
   private volatile Constructor folderConstructor = null;
   private volatile Constructor folderConstructorLI = null;
   private final IMAPStore.ConnectionPool pool;
   private ResponseHandler nonStoreResponseHandler = new ResponseHandler() {
      @Override
      public void handleResponse(Response r) {
         if (r.isOK() || r.isNO() || r.isBAD() || r.isBYE()) {
            IMAPStore.this.handleResponseCode(r);
         }

         if (r.isBYE()) {
            IMAPStore.this.logger.fine("IMAPStore non-store connection dead");
         }
      }
   };

   public IMAPStore(Session session, URLName url) {
      this(session, url, "imap", false);
   }

   protected IMAPStore(Session session, URLName url, String name, boolean isSSL) {
      super(session, url);
      if (url != null) {
         name = url.getProtocol();
      }

      this.name = name;
      if (!isSSL) {
         isSSL = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".ssl.enable", false);
      }

      if (isSSL) {
         this.defaultPort = 993;
      } else {
         this.defaultPort = 143;
      }

      this.isSSL = isSSL;
      this.debug = session.getDebug();
      this.debugusername = PropUtil.getBooleanSessionProperty(session, "mail.debug.auth.username", true);
      this.debugpassword = PropUtil.getBooleanSessionProperty(session, "mail.debug.auth.password", false);
      this.logger = new MailLogger(this.getClass(), "DEBUG " + name.toUpperCase(Locale.ENGLISH), session);
      boolean partialFetch = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".partialfetch", true);
      if (!partialFetch) {
         this.blksize = -1;
         this.logger.config("mail.imap.partialfetch: false");
      } else {
         this.blksize = PropUtil.getIntSessionProperty(session, "mail." + name + ".fetchsize", 16384);
         if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("mail.imap.fetchsize: " + this.blksize);
         }
      }

      this.ignoreSize = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".ignorebodystructuresize", false);
      if (this.logger.isLoggable(Level.CONFIG)) {
         this.logger.config("mail.imap.ignorebodystructuresize: " + this.ignoreSize);
      }

      this.statusCacheTimeout = PropUtil.getIntSessionProperty(session, "mail." + name + ".statuscachetimeout", 1000);
      if (this.logger.isLoggable(Level.CONFIG)) {
         this.logger.config("mail.imap.statuscachetimeout: " + this.statusCacheTimeout);
      }

      this.appendBufferSize = PropUtil.getIntSessionProperty(session, "mail." + name + ".appendbuffersize", -1);
      if (this.logger.isLoggable(Level.CONFIG)) {
         this.logger.config("mail.imap.appendbuffersize: " + this.appendBufferSize);
      }

      this.minIdleTime = PropUtil.getIntSessionProperty(session, "mail." + name + ".minidletime", 10);
      if (this.logger.isLoggable(Level.CONFIG)) {
         this.logger.config("mail.imap.minidletime: " + this.minIdleTime);
      }

      String s = session.getProperty("mail." + name + ".proxyauth.user");
      if (s != null) {
         this.proxyAuthUser = s;
         if (this.logger.isLoggable(Level.CONFIG)) {
            this.logger.config("mail.imap.proxyauth.user: " + this.proxyAuthUser);
         }
      }

      this.disableAuthLogin = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".auth.login.disable", false);
      if (this.disableAuthLogin) {
         this.logger.config("disable AUTH=LOGIN");
      }

      this.disableAuthPlain = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".auth.plain.disable", false);
      if (this.disableAuthPlain) {
         this.logger.config("disable AUTH=PLAIN");
      }

      this.disableAuthNtlm = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".auth.ntlm.disable", false);
      if (this.disableAuthNtlm) {
         this.logger.config("disable AUTH=NTLM");
      }

      this.enableStartTLS = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".starttls.enable", false);
      if (this.enableStartTLS) {
         this.logger.config("enable STARTTLS");
      }

      this.requireStartTLS = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".starttls.required", false);
      if (this.requireStartTLS) {
         this.logger.config("require STARTTLS");
      }

      this.enableSASL = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".sasl.enable", false);
      if (this.enableSASL) {
         this.logger.config("enable SASL");
      }

      if (this.enableSASL) {
         s = session.getProperty("mail." + name + ".sasl.mechanisms");
         if (s != null && s.length() > 0) {
            if (this.logger.isLoggable(Level.CONFIG)) {
               this.logger.config("SASL mechanisms allowed: " + s);
            }

            Vector v = new Vector(5);
            StringTokenizer st = new StringTokenizer(s, " ,");

            while(st.hasMoreTokens()) {
               String m = st.nextToken();
               if (m.length() > 0) {
                  v.addElement(m);
               }
            }

            this.saslMechanisms = new String[v.size()];
            v.copyInto(this.saslMechanisms);
         }
      }

      s = session.getProperty("mail." + name + ".sasl.authorizationid");
      if (s != null) {
         this.authorizationID = s;
         this.logger.log(Level.CONFIG, "mail.imap.sasl.authorizationid: {0}", this.authorizationID);
      }

      s = session.getProperty("mail." + name + ".sasl.realm");
      if (s != null) {
         this.saslRealm = s;
         this.logger.log(Level.CONFIG, "mail.imap.sasl.realm: {0}", this.saslRealm);
      }

      this.forcePasswordRefresh = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".forcepasswordrefresh", false);
      if (this.forcePasswordRefresh) {
         this.logger.config("enable forcePasswordRefresh");
      }

      this.enableResponseEvents = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".enableresponseevents", false);
      if (this.enableResponseEvents) {
         this.logger.config("enable IMAP response events");
      }

      this.enableImapEvents = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".enableimapevents", false);
      if (this.enableImapEvents) {
         this.logger.config("enable IMAP IDLE events");
      }

      this.messageCacheDebug = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".messagecache.debug", false);
      this.guid = session.getProperty("mail." + name + ".yahoo.guid");
      if (this.guid != null) {
         this.logger.log(Level.CONFIG, "mail.imap.yahoo.guid: {0}", this.guid);
      }

      this.throwSearchException = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".throwsearchexception", false);
      if (this.throwSearchException) {
         this.logger.config("throw SearchException");
      }

      this.peek = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".peek", false);
      if (this.peek) {
         this.logger.config("peek");
      }

      s = session.getProperty("mail." + name + ".folder.class");
      if (s != null) {
         this.logger.log(Level.CONFIG, "IMAP: folder class: {0}", s);

         try {
            ClassLoader cl = this.getClass().getClassLoader();
            Class folderClass = null;

            try {
               folderClass = Class.forName(s, false, cl);
            } catch (ClassNotFoundException var11) {
               folderClass = Class.forName(s);
            }

            Class[] c = new Class[]{String.class, Character.TYPE, IMAPStore.class, Boolean.class};
            this.folderConstructor = folderClass.getConstructor(c);
            Class[] c2 = new Class[]{ListInfo.class, IMAPStore.class};
            this.folderConstructorLI = folderClass.getConstructor(c2);
         } catch (Exception var12) {
            this.logger.log(Level.CONFIG, "IMAP: failed to load folder class", (Throwable)var12);
         }
      }

      this.pool = new IMAPStore.ConnectionPool(name, this.logger, session);
   }

   @Override
   protected synchronized boolean protocolConnect(String host, int pport, String user, String password) throws MessagingException {
      IMAPProtocol protocol = null;
      if (host != null && password != null && user != null) {
         if (pport != -1) {
            this.port = pport;
         } else {
            this.port = PropUtil.getIntSessionProperty(this.session, "mail." + this.name + ".port", this.port);
         }

         if (this.port == -1) {
            this.port = this.defaultPort;
         }

         try {
            boolean poolEmpty;
            synchronized(this.pool) {
               poolEmpty = this.pool.authenticatedConnections.isEmpty();
            }

            if (poolEmpty) {
               if (this.logger.isLoggable(Level.FINE)) {
                  this.logger.fine("trying to connect to host \"" + host + "\", port " + this.port + ", isSSL " + this.isSSL);
               }

               protocol = this.newIMAPProtocol(host, this.port);
               if (this.logger.isLoggable(Level.FINE)) {
                  this.logger.fine("protocolConnect login, host=" + host + ", user=" + this.traceUser(user) + ", password=" + this.tracePassword(password));
               }

               protocol.addResponseHandler(this.nonStoreResponseHandler);
               this.login(protocol, user, password);
               protocol.removeResponseHandler(this.nonStoreResponseHandler);
               protocol.addResponseHandler(this);
               this.usingSSL = protocol.isSSL();
               this.host = host;
               this.user = user;
               this.password = password;
               synchronized(this.pool) {
                  this.pool.authenticatedConnections.addElement(protocol);
               }
            }

            return true;
         } catch (CommandFailedException var12) {
            if (protocol != null) {
               protocol.disconnect();
            }

            IMAPProtocol var17 = null;
            throw new AuthenticationFailedException(var12.getResponse().getRest());
         } catch (ProtocolException var13) {
            if (protocol != null) {
               protocol.disconnect();
            }

            IMAPProtocol var16 = null;
            throw new MessagingException(var13.getMessage(), var13);
         } catch (SocketConnectException var14) {
            throw new MailConnectException(var14);
         } catch (IOException var15) {
            throw new MessagingException(var15.getMessage(), var15);
         }
      } else {
         if (this.logger.isLoggable(Level.FINE)) {
            this.logger
               .fine("protocolConnect returning false, host=" + host + ", user=" + this.traceUser(user) + ", password=" + this.tracePassword(password));
         }

         return false;
      }
   }

   protected IMAPProtocol newIMAPProtocol(String host, int port) throws IOException, ProtocolException {
      return new IMAPProtocol(this.name, host, port, this.session.getProperties(), this.isSSL, this.logger);
   }

   private void login(IMAPProtocol p, String u, String pw) throws ProtocolException {
      if ((this.enableStartTLS || this.requireStartTLS) && !p.isSSL()) {
         if (p.hasCapability("STARTTLS")) {
            p.startTLS();
            p.capability();
         } else if (this.requireStartTLS) {
            this.logger.fine("STARTTLS required but not supported by server");
            throw new ProtocolException("STARTTLS required but not supported by server");
         }
      }

      if (!p.isAuthenticated()) {
         this.preLogin(p);
         if (this.guid != null) {
            Map<String, String> gmap = new HashMap<>();
            gmap.put("GUID", this.guid);
            p.id(gmap);
         }

         p.getCapabilities().put("__PRELOGIN__", "");
         String authzid;
         if (this.authorizationID != null) {
            authzid = this.authorizationID;
         } else if (this.proxyAuthUser != null) {
            authzid = this.proxyAuthUser;
         } else {
            authzid = null;
         }

         if (this.enableSASL) {
            try {
               p.sasllogin(this.saslMechanisms, this.saslRealm, authzid, u, pw);
               if (!p.isAuthenticated()) {
                  throw new CommandFailedException("SASL authentication failed");
               }
            } catch (UnsupportedOperationException var8) {
            }
         }

         if (!p.isAuthenticated()) {
            if (p.hasCapability("AUTH=PLAIN") && !this.disableAuthPlain) {
               p.authplain(authzid, u, pw);
            } else if ((p.hasCapability("AUTH-LOGIN") || p.hasCapability("AUTH=LOGIN")) && !this.disableAuthLogin) {
               p.authlogin(u, pw);
            } else if (p.hasCapability("AUTH=NTLM") && !this.disableAuthNtlm) {
               p.authntlm(authzid, u, pw);
            } else {
               if (p.hasCapability("LOGINDISABLED")) {
                  throw new ProtocolException("No login methods supported!");
               }

               p.login(u, pw);
            }
         }

         if (this.proxyAuthUser != null) {
            p.proxyauth(this.proxyAuthUser);
         }

         if (p.hasCapability("__PRELOGIN__")) {
            try {
               p.capability();
            } catch (ConnectionException var6) {
               throw var6;
            } catch (ProtocolException var7) {
            }
         }
      }
   }

   protected void preLogin(IMAPProtocol p) throws ProtocolException {
   }

   public synchronized boolean isSSL() {
      return this.usingSSL;
   }

   public synchronized void setUsername(String user) {
      this.user = user;
   }

   public synchronized void setPassword(String password) {
      this.password = password;
   }

   IMAPProtocol getProtocol(IMAPFolder folder) throws MessagingException {
      IMAPProtocol p = null;

      while(p == null) {
         synchronized(this.pool) {
            if (this.pool.authenticatedConnections.isEmpty()
               || this.pool.authenticatedConnections.size() == 1 && (this.pool.separateStoreConnection || this.pool.storeConnectionInUse)) {
               this.logger.fine("no connections in the pool, creating a new one");

               try {
                  if (this.forcePasswordRefresh) {
                     this.refreshPassword();
                  }

                  p = this.newIMAPProtocol(this.host, this.port);
                  p.addResponseHandler(this.nonStoreResponseHandler);
                  this.login(p, this.user, this.password);
                  p.removeResponseHandler(this.nonStoreResponseHandler);
               } catch (Exception var31) {
                  if (p != null) {
                     try {
                        p.disconnect();
                     } catch (Exception var26) {
                     }
                  }

                  p = null;
               }

               if (p == null) {
                  throw new MessagingException("connection failure");
               }
            } else {
               if (this.logger.isLoggable(Level.FINE)) {
                  this.logger.fine("connection available -- size: " + this.pool.authenticatedConnections.size());
               }

               p = (IMAPProtocol)this.pool.authenticatedConnections.lastElement();
               this.pool.authenticatedConnections.removeElement(p);
               long lastUsed = System.currentTimeMillis() - p.getTimestamp();
               if (lastUsed > this.pool.serverTimeoutInterval) {
                  try {
                     p.removeResponseHandler(this);
                     p.addResponseHandler(this.nonStoreResponseHandler);
                     p.noop();
                     p.removeResponseHandler(this.nonStoreResponseHandler);
                     p.addResponseHandler(this);
                  } catch (ProtocolException var30) {
                     label215:
                     try {
                        p.removeResponseHandler(this.nonStoreResponseHandler);
                        p.disconnect();
                     } finally {
                        break label215;
                     }

                     p = null;
                     continue;
                  }
               }

               if (this.proxyAuthUser != null && !this.proxyAuthUser.equals(p.getProxyAuthUser()) && p.hasCapability("X-UNAUTHENTICATE")) {
                  try {
                     p.removeResponseHandler(this);
                     p.addResponseHandler(this.nonStoreResponseHandler);
                     p.unauthenticate();
                     this.login(p, this.user, this.password);
                     p.removeResponseHandler(this.nonStoreResponseHandler);
                     p.addResponseHandler(this);
                  } catch (ProtocolException var28) {
                     label206:
                     try {
                        p.removeResponseHandler(this.nonStoreResponseHandler);
                        p.disconnect();
                     } finally {
                        break label206;
                     }

                     p = null;
                     continue;
                  }
               }

               p.removeResponseHandler(this);
            }

            this.timeoutConnections();
            if (folder != null) {
               if (this.pool.folders == null) {
                  this.pool.folders = new Vector();
               }

               this.pool.folders.addElement(folder);
            }
         }
      }

      return p;
   }

   private IMAPProtocol getStoreProtocol() throws ProtocolException {
      IMAPProtocol p = null;

      while(p == null) {
         synchronized(this.pool) {
            this.waitIfIdle();
            if (this.pool.authenticatedConnections.isEmpty()) {
               this.pool.logger.fine("getStoreProtocol() - no connections in the pool, creating a new one");

               try {
                  if (this.forcePasswordRefresh) {
                     this.refreshPassword();
                  }

                  p = this.newIMAPProtocol(this.host, this.port);
                  this.login(p, this.user, this.password);
               } catch (Exception var8) {
                  if (p != null) {
                     try {
                        p.logout();
                     } catch (Exception var7) {
                     }
                  }

                  p = null;
               }

               if (p == null) {
                  throw new ConnectionException("failed to create new store connection");
               }

               p.addResponseHandler(this);
               this.pool.authenticatedConnections.addElement(p);
            } else {
               if (this.pool.logger.isLoggable(Level.FINE)) {
                  this.pool.logger.fine("getStoreProtocol() - connection available -- size: " + this.pool.authenticatedConnections.size());
               }

               p = (IMAPProtocol)this.pool.authenticatedConnections.firstElement();
               if (this.proxyAuthUser != null && !this.proxyAuthUser.equals(p.getProxyAuthUser()) && p.hasCapability("X-UNAUTHENTICATE")) {
                  p.unauthenticate();
                  this.login(p, this.user, this.password);
               }
            }

            if (this.pool.storeConnectionInUse) {
               try {
                  p = null;
                  this.pool.wait();
               } catch (InterruptedException var6) {
               }
            } else {
               this.pool.storeConnectionInUse = true;
               this.pool.logger.fine("getStoreProtocol() -- storeConnectionInUse");
            }

            this.timeoutConnections();
         }
      }

      return p;
   }

   IMAPProtocol getFolderStoreProtocol() throws ProtocolException {
      IMAPProtocol p = this.getStoreProtocol();
      p.removeResponseHandler(this);
      p.addResponseHandler(this.nonStoreResponseHandler);
      return p;
   }

   private void refreshPassword() {
      if (this.logger.isLoggable(Level.FINE)) {
         this.logger.fine("refresh password, user: " + this.traceUser(this.user));
      }

      InetAddress addr;
      try {
         addr = InetAddress.getByName(this.host);
      } catch (UnknownHostException var3) {
         addr = null;
      }

      PasswordAuthentication pa = this.session.requestPasswordAuthentication(addr, this.port, this.name, null, this.user);
      if (pa != null) {
         this.user = pa.getUserName();
         this.password = pa.getPassword();
      }
   }

   boolean allowReadOnlySelect() {
      return PropUtil.getBooleanSessionProperty(this.session, "mail." + this.name + ".allowreadonlyselect", false);
   }

   boolean hasSeparateStoreConnection() {
      return this.pool.separateStoreConnection;
   }

   MailLogger getConnectionPoolLogger() {
      return this.pool.logger;
   }

   boolean getMessageCacheDebug() {
      return this.messageCacheDebug;
   }

   boolean isConnectionPoolFull() {
      synchronized(this.pool) {
         if (this.pool.logger.isLoggable(Level.FINE)) {
            this.pool.logger.fine("connection pool current size: " + this.pool.authenticatedConnections.size() + "   pool size: " + this.pool.poolSize);
         }

         return this.pool.authenticatedConnections.size() >= this.pool.poolSize;
      }
   }

   void releaseProtocol(IMAPFolder folder, IMAPProtocol protocol) {
      synchronized(this.pool) {
         if (protocol != null) {
            if (!this.isConnectionPoolFull()) {
               protocol.addResponseHandler(this);
               this.pool.authenticatedConnections.addElement(protocol);
               if (this.logger.isLoggable(Level.FINE)) {
                  this.logger.fine("added an Authenticated connection -- size: " + this.pool.authenticatedConnections.size());
               }
            } else {
               this.logger.fine("pool is full, not adding an Authenticated connection");

               try {
                  protocol.logout();
               } catch (ProtocolException var6) {
               }
            }
         }

         if (this.pool.folders != null) {
            this.pool.folders.removeElement(folder);
         }

         this.timeoutConnections();
      }
   }

   private void releaseStoreProtocol(IMAPProtocol protocol) {
      if (protocol == null) {
         this.cleanup();
      } else {
         boolean failed;
         synchronized(this.connectionFailedLock) {
            failed = this.connectionFailed;
            this.connectionFailed = false;
         }

         synchronized(this.pool) {
            this.pool.storeConnectionInUse = false;
            this.pool.notifyAll();
            this.pool.logger.fine("releaseStoreProtocol()");
            this.timeoutConnections();
         }

         assert !Thread.holdsLock(this.pool);

         if (failed) {
            this.cleanup();
         }
      }
   }

   void releaseFolderStoreProtocol(IMAPProtocol protocol) {
      if (protocol != null) {
         protocol.removeResponseHandler(this.nonStoreResponseHandler);
         protocol.addResponseHandler(this);
         synchronized(this.pool) {
            this.pool.storeConnectionInUse = false;
            this.pool.notifyAll();
            this.pool.logger.fine("releaseFolderStoreProtocol()");
            this.timeoutConnections();
         }
      }
   }

   private void emptyConnectionPool(boolean force) {
      synchronized(this.pool) {
         for(int index = this.pool.authenticatedConnections.size() - 1; index >= 0; --index) {
            try {
               IMAPProtocol p = (IMAPProtocol)this.pool.authenticatedConnections.elementAt(index);
               p.removeResponseHandler(this);
               if (force) {
                  p.disconnect();
               } else {
                  p.logout();
               }
            } catch (ProtocolException var6) {
            }
         }

         this.pool.authenticatedConnections.removeAllElements();
      }

      this.pool.logger.fine("removed all authenticated connections from pool");
   }

   private void timeoutConnections() {
      synchronized(this.pool) {
         if (System.currentTimeMillis() - this.pool.lastTimePruned > this.pool.pruningInterval && this.pool.authenticatedConnections.size() > 1) {
            if (this.pool.logger.isLoggable(Level.FINE)) {
               this.pool.logger.fine("checking for connections to prune: " + (System.currentTimeMillis() - this.pool.lastTimePruned));
               this.pool.logger.fine("clientTimeoutInterval: " + this.pool.clientTimeoutInterval);
            }

            for(int index = this.pool.authenticatedConnections.size() - 1; index > 0; --index) {
               IMAPProtocol p = (IMAPProtocol)this.pool.authenticatedConnections.elementAt(index);
               if (this.pool.logger.isLoggable(Level.FINE)) {
                  this.pool.logger.fine("protocol last used: " + (System.currentTimeMillis() - p.getTimestamp()));
               }

               if (System.currentTimeMillis() - p.getTimestamp() > this.pool.clientTimeoutInterval) {
                  this.pool.logger.fine("authenticated connection timed out, logging out the connection");
                  p.removeResponseHandler(this);
                  this.pool.authenticatedConnections.removeElementAt(index);

                  try {
                     p.logout();
                  } catch (ProtocolException var6) {
                  }
               }
            }

            this.pool.lastTimePruned = System.currentTimeMillis();
         }
      }
   }

   int getFetchBlockSize() {
      return this.blksize;
   }

   boolean ignoreBodyStructureSize() {
      return this.ignoreSize;
   }

   @Override
   Session getSession() {
      return this.session;
   }

   int getStatusCacheTimeout() {
      return this.statusCacheTimeout;
   }

   int getAppendBufferSize() {
      return this.appendBufferSize;
   }

   int getMinIdleTime() {
      return this.minIdleTime;
   }

   boolean throwSearchException() {
      return this.throwSearchException;
   }

   boolean getPeek() {
      return this.peek;
   }

   public synchronized boolean hasCapability(String capability) throws MessagingException {
      IMAPProtocol p = null;

      boolean pex;
      try {
         p = this.getStoreProtocol();
         pex = p.hasCapability(capability);
      } catch (ProtocolException var8) {
         throw new MessagingException(var8.getMessage(), var8);
      } finally {
         this.releaseStoreProtocol(p);
      }

      return pex;
   }

   public void setProxyAuthUser(String user) {
      this.proxyAuthUser = user;
   }

   public String getProxyAuthUser() {
      return this.proxyAuthUser;
   }

   @Override
   public synchronized boolean isConnected() {
      if (!super.isConnected()) {
         return false;
      } else {
         IMAPProtocol p = null;

         try {
            p = this.getStoreProtocol();
            p.noop();
         } catch (ProtocolException var7) {
         } finally {
            this.releaseStoreProtocol(p);
         }

         return super.isConnected();
      }
   }

   @Override
   public synchronized void close() throws MessagingException {
      if (super.isConnected()) {
         IMAPProtocol protocol = null;

         try {
            boolean isEmpty;
            synchronized(this.pool) {
               isEmpty = this.pool.authenticatedConnections.isEmpty();
            }

            if (!isEmpty) {
               protocol = this.getStoreProtocol();
               synchronized(this.pool) {
                  this.pool.authenticatedConnections.removeElement(protocol);
               }

               protocol.logout();
               return;
            }

            this.pool.logger.fine("close() - no connections ");
            this.cleanup();
         } catch (ProtocolException var14) {
            throw new MessagingException(var14.getMessage(), var14);
         } finally {
            this.releaseStoreProtocol(protocol);
         }
      }
   }

   @Override
   protected void finalize() throws Throwable {
      super.finalize();
      this.close();
   }

   private synchronized void cleanup() {
      if (!super.isConnected()) {
         this.logger.fine("IMAPStore cleanup, not connected");
      } else {
         boolean force;
         synchronized(this.connectionFailedLock) {
            force = this.forceClose;
            this.forceClose = false;
            this.connectionFailed = false;
         }

         if (this.logger.isLoggable(Level.FINE)) {
            this.logger.fine("IMAPStore cleanup, force " + force);
         }

         Vector foldersCopy = null;
         boolean done = true;

         while(true) {
            synchronized(this.pool) {
               if (this.pool.folders != null) {
                  done = false;
                  foldersCopy = this.pool.folders;
                  this.pool.folders = null;
               } else {
                  done = true;
               }
            }

            if (done) {
               synchronized(this.pool) {
                  this.emptyConnectionPool(force);
               }

               try {
                  super.close();
               } catch (MessagingException var9) {
               }

               this.logger.fine("IMAPStore cleanup done");
               return;
            }

            int i = 0;

            for(int fsize = foldersCopy.size(); i < fsize; ++i) {
               IMAPFolder f = (IMAPFolder)foldersCopy.elementAt(i);

               try {
                  if (force) {
                     this.logger.fine("force folder to close");
                     f.forceClose();
                  } else {
                     this.logger.fine("close folder");
                     f.close(false);
                  }
               } catch (MessagingException var11) {
               } catch (IllegalStateException var12) {
               }
            }
         }
      }
   }

   @Override
   public synchronized Folder getDefaultFolder() throws MessagingException {
      this.checkConnected();
      return new DefaultFolder(this);
   }

   @Override
   public synchronized Folder getFolder(String name) throws MessagingException {
      this.checkConnected();
      return this.newIMAPFolder(name, '\uffff');
   }

   @Override
   public synchronized Folder getFolder(URLName url) throws MessagingException {
      this.checkConnected();
      return this.newIMAPFolder(url.getFile(), '\uffff');
   }

   protected IMAPFolder newIMAPFolder(String fullName, char separator, Boolean isNamespace) {
      IMAPFolder f = null;
      if (this.folderConstructor != null) {
         try {
            Object[] o = new Object[]{fullName, separator, this, isNamespace};
            f = (IMAPFolder)this.folderConstructor.newInstance(o);
         } catch (Exception var6) {
            this.logger.log(Level.FINE, "exception creating IMAPFolder class", (Throwable)var6);
         }
      }

      if (f == null) {
         f = new IMAPFolder(fullName, separator, this, isNamespace);
      }

      return f;
   }

   protected IMAPFolder newIMAPFolder(String fullName, char separator) {
      return this.newIMAPFolder(fullName, separator, null);
   }

   protected IMAPFolder newIMAPFolder(ListInfo li) {
      IMAPFolder f = null;
      if (this.folderConstructorLI != null) {
         try {
            Object[] o = new Object[]{li, this};
            f = (IMAPFolder)this.folderConstructorLI.newInstance(o);
         } catch (Exception var4) {
            this.logger.log(Level.FINE, "exception creating IMAPFolder class LI", (Throwable)var4);
         }
      }

      if (f == null) {
         f = new IMAPFolder(li, this);
      }

      return f;
   }

   @Override
   public Folder[] getPersonalNamespaces() throws MessagingException {
      Namespaces ns = this.getNamespaces();
      return ns != null && ns.personal != null ? this.namespaceToFolders(ns.personal, null) : super.getPersonalNamespaces();
   }

   @Override
   public Folder[] getUserNamespaces(String user) throws MessagingException {
      Namespaces ns = this.getNamespaces();
      return ns != null && ns.otherUsers != null ? this.namespaceToFolders(ns.otherUsers, user) : super.getUserNamespaces(user);
   }

   @Override
   public Folder[] getSharedNamespaces() throws MessagingException {
      Namespaces ns = this.getNamespaces();
      return ns != null && ns.shared != null ? this.namespaceToFolders(ns.shared, null) : super.getSharedNamespaces();
   }

   private synchronized Namespaces getNamespaces() throws MessagingException {
      this.checkConnected();
      IMAPProtocol p = null;
      if (this.namespaces == null) {
         try {
            p = this.getStoreProtocol();
            this.namespaces = p.namespace();
         } catch (BadCommandException var9) {
         } catch (ConnectionException var10) {
            throw new StoreClosedException(this, var10.getMessage());
         } catch (ProtocolException var11) {
            throw new MessagingException(var11.getMessage(), var11);
         } finally {
            this.releaseStoreProtocol(p);
         }
      }

      return this.namespaces;
   }

   private Folder[] namespaceToFolders(Namespaces.Namespace[] ns, String user) {
      Folder[] fa = new Folder[ns.length];

      for(int i = 0; i < fa.length; ++i) {
         String name = ns[i].prefix;
         if (user == null) {
            int len = name.length();
            if (len > 0 && name.charAt(len - 1) == ns[i].delimiter) {
               name = name.substring(0, len - 1);
            }
         } else {
            name = name + user;
         }

         fa[i] = this.newIMAPFolder(name, ns[i].delimiter, user == null);
      }

      return fa;
   }

   @Override
   public synchronized Quota[] getQuota(String root) throws MessagingException {
      this.checkConnected();
      Quota[] qa = null;
      IMAPProtocol p = null;

      try {
         p = this.getStoreProtocol();
         qa = p.getQuotaRoot(root);
      } catch (BadCommandException var11) {
         throw new MessagingException("QUOTA not supported", var11);
      } catch (ConnectionException var12) {
         throw new StoreClosedException(this, var12.getMessage());
      } catch (ProtocolException var13) {
         throw new MessagingException(var13.getMessage(), var13);
      } finally {
         this.releaseStoreProtocol(p);
      }

      return qa;
   }

   @Override
   public synchronized void setQuota(Quota quota) throws MessagingException {
      this.checkConnected();
      IMAPProtocol p = null;

      try {
         p = this.getStoreProtocol();
         p.setQuota(quota);
      } catch (BadCommandException var10) {
         throw new MessagingException("QUOTA not supported", var10);
      } catch (ConnectionException var11) {
         throw new StoreClosedException(this, var11.getMessage());
      } catch (ProtocolException var12) {
         throw new MessagingException(var12.getMessage(), var12);
      } finally {
         this.releaseStoreProtocol(p);
      }
   }

   private void checkConnected() {
      assert Thread.holdsLock(this);

      if (!super.isConnected()) {
         throw new IllegalStateException("Not connected");
      }
   }

   @Override
   public void handleResponse(Response r) {
      if (r.isOK() || r.isNO() || r.isBAD() || r.isBYE()) {
         this.handleResponseCode(r);
      }

      if (r.isBYE()) {
         this.logger.fine("IMAPStore connection dead");
         synchronized(this.connectionFailedLock) {
            this.connectionFailed = true;
            if (r.isSynthetic()) {
               this.forceClose = true;
            }
         }
      }
   }

   public void idle() throws MessagingException {
      IMAPProtocol p = null;

      assert !Thread.holdsLock(this.pool);

      synchronized(this) {
         this.checkConnected();
      }

      boolean needNotification = false;

      try {
         synchronized(this.pool) {
            p = this.getStoreProtocol();
            if (this.pool.idleState != 0) {
               try {
                  this.pool.wait();
               } catch (InterruptedException var24) {
               }

               return;
            }

            p.idleStart();
            needNotification = true;
            this.pool.idleState = 1;
            this.pool.idleProtocol = p;
         }

         while(true) {
            Response r;
            label222: {
               r = p.readIdleResponse();
               synchronized(this.pool) {
                  if (r != null && p.processIdleResponse(r)) {
                     break label222;
                  }

                  this.pool.idleState = 0;
                  this.pool.idleProtocol = null;
                  this.pool.notifyAll();
                  needNotification = false;
               }

               int minidle = this.getMinIdleTime();
               if (minidle > 0) {
                  try {
                     Thread.sleep((long)minidle);
                  } catch (InterruptedException var25) {
                  }
                  break;
               }

               return;
            }

            if (this.enableImapEvents && r.isUnTagged()) {
               this.notifyStoreListeners(1000, r.toString());
            }
         }
      } catch (BadCommandException var29) {
         throw new MessagingException("IDLE not supported", var29);
      } catch (ConnectionException var30) {
         throw new StoreClosedException(this, var30.getMessage());
      } catch (ProtocolException var31) {
         throw new MessagingException(var31.getMessage(), var31);
      } finally {
         if (needNotification) {
            synchronized(this.pool) {
               this.pool.idleState = 0;
               this.pool.idleProtocol = null;
               this.pool.notifyAll();
            }
         }

         this.releaseStoreProtocol(p);
      }
   }

   private void waitIfIdle() throws ProtocolException {
      assert Thread.holdsLock(this.pool);

      while(this.pool.idleState != 0) {
         if (this.pool.idleState == 1) {
            this.pool.idleProtocol.idleAbort();
            this.pool.idleState = 2;
         }

         try {
            this.pool.wait();
         } catch (InterruptedException var2) {
         }
      }
   }

   public synchronized Map<String, String> id(Map<String, String> clientParams) throws MessagingException {
      this.checkConnected();
      Map<String, String> serverParams = null;
      IMAPProtocol p = null;

      try {
         p = this.getStoreProtocol();
         serverParams = p.id(clientParams);
      } catch (BadCommandException var11) {
         throw new MessagingException("ID not supported", var11);
      } catch (ConnectionException var12) {
         throw new StoreClosedException(this, var12.getMessage());
      } catch (ProtocolException var13) {
         throw new MessagingException(var13.getMessage(), var13);
      } finally {
         this.releaseStoreProtocol(p);
      }

      return serverParams;
   }

   void handleResponseCode(Response r) {
      if (this.enableResponseEvents) {
         this.notifyStoreListeners(1000, r.toString());
      }

      String s = r.getRest();
      boolean isAlert = false;
      if (s.startsWith("[")) {
         int i = s.indexOf(93);
         if (i > 0 && s.substring(0, i + 1).equalsIgnoreCase("[ALERT]")) {
            isAlert = true;
         }

         s = s.substring(i + 1).trim();
      }

      if (isAlert) {
         this.notifyStoreListeners(1, s);
      } else if (r.isUnTagged() && s.length() > 0) {
         this.notifyStoreListeners(2, s);
      }
   }

   private String traceUser(String user) {
      return this.debugusername ? user : "<user name suppressed>";
   }

   private String tracePassword(String password) {
      return this.debugpassword ? password : (password == null ? "<null>" : "<non-null>");
   }

   static class ConnectionPool {
      private Vector authenticatedConnections = new Vector();
      private Vector folders;
      private boolean storeConnectionInUse = false;
      private long lastTimePruned;
      private final boolean separateStoreConnection;
      private final long clientTimeoutInterval;
      private final long serverTimeoutInterval;
      private final int poolSize;
      private final long pruningInterval;
      private final MailLogger logger;
      private static final int RUNNING = 0;
      private static final int IDLE = 1;
      private static final int ABORTING = 2;
      private int idleState = 0;
      private IMAPProtocol idleProtocol;

      ConnectionPool(String name, MailLogger plogger, Session session) {
         this.lastTimePruned = System.currentTimeMillis();
         boolean debug = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".connectionpool.debug", false);
         this.logger = plogger.getSubLogger("connectionpool", "DEBUG IMAP CP", debug);
         int size = PropUtil.getIntSessionProperty(session, "mail." + name + ".connectionpoolsize", -1);
         if (size > 0) {
            this.poolSize = size;
            if (this.logger.isLoggable(Level.CONFIG)) {
               this.logger.config("mail.imap.connectionpoolsize: " + this.poolSize);
            }
         } else {
            this.poolSize = 1;
         }

         int connectionPoolTimeout = PropUtil.getIntSessionProperty(session, "mail." + name + ".connectionpooltimeout", -1);
         if (connectionPoolTimeout > 0) {
            this.clientTimeoutInterval = (long)connectionPoolTimeout;
            if (this.logger.isLoggable(Level.CONFIG)) {
               this.logger.config("mail.imap.connectionpooltimeout: " + this.clientTimeoutInterval);
            }
         } else {
            this.clientTimeoutInterval = 45000L;
         }

         int serverTimeout = PropUtil.getIntSessionProperty(session, "mail." + name + ".servertimeout", -1);
         if (serverTimeout > 0) {
            this.serverTimeoutInterval = (long)serverTimeout;
            if (this.logger.isLoggable(Level.CONFIG)) {
               this.logger.config("mail.imap.servertimeout: " + this.serverTimeoutInterval);
            }
         } else {
            this.serverTimeoutInterval = 1800000L;
         }

         int pruning = PropUtil.getIntSessionProperty(session, "mail." + name + ".pruninginterval", -1);
         if (pruning > 0) {
            this.pruningInterval = (long)pruning;
            if (this.logger.isLoggable(Level.CONFIG)) {
               this.logger.config("mail.imap.pruninginterval: " + this.pruningInterval);
            }
         } else {
            this.pruningInterval = 60000L;
         }

         this.separateStoreConnection = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".separatestoreconnection", false);
         if (this.separateStoreConnection) {
            this.logger.config("dedicate a store connection");
         }
      }
   }
}
