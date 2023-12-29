package javax.mail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.concurrent.Executor;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.event.MailEvent;

public abstract class Service {
   protected Session session;
   protected URLName url = null;
   protected boolean debug = false;
   private boolean connected = false;
   private final Vector connectionListeners = new Vector();
   private final EventQueue q;

   protected Service(Session session, URLName urlname) {
      this.session = session;
      this.debug = session.getDebug();
      this.url = urlname;
      String protocol = null;
      String host = null;
      int port = -1;
      String user = null;
      String password = null;
      String file = null;
      if (this.url != null) {
         protocol = this.url.getProtocol();
         host = this.url.getHost();
         port = this.url.getPort();
         user = this.url.getUsername();
         password = this.url.getPassword();
         file = this.url.getFile();
      }

      if (protocol != null) {
         if (host == null) {
            host = session.getProperty("mail." + protocol + ".host");
         }

         if (user == null) {
            user = session.getProperty("mail." + protocol + ".user");
         }
      }

      if (host == null) {
         host = session.getProperty("mail.host");
      }

      if (user == null) {
         user = session.getProperty("mail.user");
      }

      if (user == null) {
         try {
            user = System.getProperty("user.name");
         } catch (SecurityException var11) {
         }
      }

      this.url = new URLName(protocol, host, port, file, user, password);
      String scope = session.getProperties().getProperty("mail.event.scope", "folder");
      Executor executor = (Executor)session.getProperties().get("mail.event.executor");
      if (scope.equalsIgnoreCase("application")) {
         this.q = EventQueue.getApplicationEventQueue(executor);
      } else if (scope.equalsIgnoreCase("session")) {
         this.q = session.getEventQueue();
      } else {
         this.q = new EventQueue(executor);
      }
   }

   public void connect() throws MessagingException {
      this.connect(null, null, null);
   }

   public void connect(String host, String user, String password) throws MessagingException {
      this.connect(host, -1, user, password);
   }

   public void connect(String user, String password) throws MessagingException {
      this.connect(null, user, password);
   }

   public synchronized void connect(String host, int port, String user, String password) throws MessagingException {
      if (this.isConnected()) {
         throw new IllegalStateException("already connected");
      } else {
         boolean connected = false;
         boolean save = false;
         String protocol = null;
         String file = null;
         if (this.url != null) {
            protocol = this.url.getProtocol();
            if (host == null) {
               host = this.url.getHost();
            }

            if (port == -1) {
               port = this.url.getPort();
            }

            if (user == null) {
               user = this.url.getUsername();
               if (password == null) {
                  password = this.url.getPassword();
               }
            } else if (password == null && user.equals(this.url.getUsername())) {
               password = this.url.getPassword();
            }

            file = this.url.getFile();
         }

         if (protocol != null) {
            if (host == null) {
               host = this.session.getProperty("mail." + protocol + ".host");
            }

            if (user == null) {
               user = this.session.getProperty("mail." + protocol + ".user");
            }
         }

         if (host == null) {
            host = this.session.getProperty("mail.host");
         }

         if (user == null) {
            user = this.session.getProperty("mail.user");
         }

         if (user == null) {
            try {
               user = System.getProperty("user.name");
            } catch (SecurityException var15) {
            }
         }

         if (password == null && this.url != null) {
            this.setURLName(new URLName(protocol, host, port, file, user, null));
            PasswordAuthentication pw = this.session.getPasswordAuthentication(this.getURLName());
            if (pw != null) {
               if (user == null) {
                  user = pw.getUserName();
                  password = pw.getPassword();
               } else if (user.equals(pw.getUserName())) {
                  password = pw.getPassword();
               }
            } else {
               save = true;
            }
         }

         AuthenticationFailedException authEx = null;

         try {
            connected = this.protocolConnect(host, port, user, password);
         } catch (AuthenticationFailedException var14) {
            authEx = var14;
         }

         if (!connected) {
            InetAddress addr;
            try {
               addr = InetAddress.getByName(host);
            } catch (UnknownHostException var13) {
               addr = null;
            }

            PasswordAuthentication pw = this.session.requestPasswordAuthentication(addr, port, protocol, null, user);
            if (pw != null) {
               user = pw.getUserName();
               password = pw.getPassword();
               connected = this.protocolConnect(host, port, user, password);
            }
         }

         if (!connected) {
            if (authEx != null) {
               throw authEx;
            } else if (user == null) {
               throw new AuthenticationFailedException("failed to connect, no user name specified?");
            } else if (password == null) {
               throw new AuthenticationFailedException("failed to connect, no password specified?");
            } else {
               throw new AuthenticationFailedException("failed to connect");
            }
         } else {
            this.setURLName(new URLName(protocol, host, port, file, user, password));
            if (save) {
               this.session.setPasswordAuthentication(this.getURLName(), new PasswordAuthentication(user, password));
            }

            this.setConnected(true);
            this.notifyConnectionListeners(1);
         }
      }
   }

   protected boolean protocolConnect(String host, int port, String user, String password) throws MessagingException {
      return false;
   }

   public synchronized boolean isConnected() {
      return this.connected;
   }

   protected synchronized void setConnected(boolean connected) {
      this.connected = connected;
   }

   public synchronized void close() throws MessagingException {
      this.setConnected(false);
      this.notifyConnectionListeners(3);
   }

   public synchronized URLName getURLName() {
      return this.url == null || this.url.getPassword() == null && this.url.getFile() == null
         ? this.url
         : new URLName(this.url.getProtocol(), this.url.getHost(), this.url.getPort(), null, this.url.getUsername(), null);
   }

   protected synchronized void setURLName(URLName url) {
      this.url = url;
   }

   public void addConnectionListener(ConnectionListener l) {
      this.connectionListeners.addElement(l);
   }

   public void removeConnectionListener(ConnectionListener l) {
      this.connectionListeners.removeElement(l);
   }

   protected void notifyConnectionListeners(int type) {
      if (this.connectionListeners.size() > 0) {
         ConnectionEvent e = new ConnectionEvent(this, type);
         this.queueEvent(e, this.connectionListeners);
      }

      if (type == 3) {
         this.q.terminateQueue();
      }
   }

   @Override
   public String toString() {
      URLName url = this.getURLName();
      return url != null ? url.toString() : super.toString();
   }

   protected void queueEvent(MailEvent event, Vector vector) {
      Vector v = (Vector)vector.clone();
      this.q.enqueue(event, v);
   }

   @Override
   protected void finalize() throws Throwable {
      super.finalize();
      this.q.terminateQueue();
   }

   Session getSession() {
      return this.session;
   }

   EventQueue getEventQueue() {
      return this.q;
   }
}
