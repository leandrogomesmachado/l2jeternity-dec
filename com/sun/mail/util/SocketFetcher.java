package com.sun.mail.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.Proxy.Type;
import java.nio.channels.SocketChannel;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.SocketFactory;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SocketFetcher {
   private static MailLogger logger = new MailLogger(
      SocketFetcher.class, "socket", "DEBUG SocketFetcher", PropUtil.getBooleanSystemProperty("mail.socket.debug", false), System.out
   );

   private SocketFetcher() {
   }

   public static Socket getSocket(String host, int port, Properties props, String prefix, boolean useSSL) throws IOException {
      if (logger.isLoggable(Level.FINER)) {
         logger.finer("getSocket, host " + host + ", port " + port + ", prefix " + prefix + ", useSSL " + useSSL);
      }

      if (prefix == null) {
         prefix = "socket";
      }

      if (props == null) {
         props = new Properties();
      }

      int cto = PropUtil.getIntProperty(props, prefix + ".connectiontimeout", -1);
      Socket socket = null;
      String localaddrstr = props.getProperty(prefix + ".localaddress", null);
      InetAddress localaddr = null;
      if (localaddrstr != null) {
         localaddr = InetAddress.getByName(localaddrstr);
      }

      int localport = PropUtil.getIntProperty(props, prefix + ".localport", 0);
      boolean fb = PropUtil.getBooleanProperty(props, prefix + ".socketFactory.fallback", true);
      int sfPort = -1;
      String sfErr = "unknown socket factory";
      int to = PropUtil.getIntProperty(props, prefix + ".timeout", -1);

      try {
         SocketFactory sf = null;
         String sfPortName = null;
         if (useSSL) {
            Object sfo = props.get(prefix + ".ssl.socketFactory");
            if (sfo instanceof SocketFactory) {
               sf = (SocketFactory)sfo;
               sfErr = "SSL socket factory instance " + sf;
            }

            if (sf == null) {
               String sfClass = props.getProperty(prefix + ".ssl.socketFactory.class");
               sf = getSocketFactory(sfClass);
               sfErr = "SSL socket factory class " + sfClass;
            }

            sfPortName = ".ssl.socketFactory.port";
         }

         if (sf == null) {
            Object sfo = props.get(prefix + ".socketFactory");
            if (sfo instanceof SocketFactory) {
               sf = (SocketFactory)sfo;
               sfErr = "socket factory instance " + sf;
            }

            if (sf == null) {
               String sfClass = props.getProperty(prefix + ".socketFactory.class");
               sf = getSocketFactory(sfClass);
               sfErr = "socket factory class " + sfClass;
            }

            sfPortName = ".socketFactory.port";
         }

         if (sf != null) {
            sfPort = PropUtil.getIntProperty(props, prefix + sfPortName, -1);
            if (sfPort == -1) {
               sfPort = port;
            }

            socket = createSocket(localaddr, localport, host, sfPort, cto, to, props, prefix, sf, useSSL);
         }
      } catch (SocketTimeoutException var18) {
         throw var18;
      } catch (Exception var19) {
         Exception ex = var19;
         if (!fb) {
            if (var19 instanceof InvocationTargetException) {
               Throwable t = ((InvocationTargetException)var19).getTargetException();
               if (t instanceof Exception) {
                  ex = (Exception)t;
               }
            }

            if (ex instanceof IOException) {
               throw (IOException)ex;
            }

            throw new SocketConnectException("Using " + sfErr, ex, host, sfPort, cto);
         }
      }

      if (socket == null) {
         socket = createSocket(localaddr, localport, host, port, cto, to, props, prefix, null, useSSL);
      } else if (to >= 0) {
         socket.setSoTimeout(to);
      }

      return socket;
   }

   public static Socket getSocket(String host, int port, Properties props, String prefix) throws IOException {
      return getSocket(host, port, props, prefix, false);
   }

   private static Socket createSocket(
      InetAddress localaddr, int localport, String host, int port, int cto, int to, Properties props, String prefix, SocketFactory sf, boolean useSSL
   ) throws IOException {
      Socket socket = null;
      String socksHost = props.getProperty(prefix + ".socks.host", null);
      int socksPort = 1080;
      String err = null;
      if (socksHost != null) {
         int i = socksHost.indexOf(58);
         if (i >= 0) {
            socksHost = socksHost.substring(0, i);

            try {
               socksPort = Integer.parseInt(socksHost.substring(i + 1));
            } catch (NumberFormatException var21) {
            }
         }

         socksPort = PropUtil.getIntProperty(props, prefix + ".socks.port", socksPort);
         err = "Using SOCKS host, port: " + socksHost + ", " + socksPort;
         if (logger.isLoggable(Level.FINER)) {
            logger.finer("socks host " + socksHost + ", port " + socksPort);
         }
      }

      if (sf != null) {
         socket = sf.createSocket();
      }

      if (socket == null) {
         if (socksHost != null) {
            socket = new Socket(new Proxy(Type.SOCKS, new InetSocketAddress(socksHost, socksPort)));
         } else if (PropUtil.getBooleanProperty(props, prefix + ".usesocketchannels", false)) {
            logger.finer("using SocketChannels");
            socket = SocketChannel.open().socket();
         } else {
            socket = new Socket();
         }
      }

      if (to >= 0) {
         socket.setSoTimeout(to);
      }

      int writeTimeout = PropUtil.getIntProperty(props, prefix + ".writetimeout", -1);
      if (writeTimeout != -1) {
         socket = new WriteTimeoutSocket(socket, writeTimeout);
      }

      if (localaddr != null) {
         socket.bind(new InetSocketAddress(localaddr, localport));
      }

      try {
         if (cto >= 0) {
            socket.connect(new InetSocketAddress(host, port), cto);
         } else {
            socket.connect(new InetSocketAddress(host, port));
         }
      } catch (IOException var20) {
         throw new SocketConnectException(err, var20, host, port, cto);
      }

      if (useSSL && !(socket instanceof SSLSocket)) {
         String trusted;
         SSLSocketFactory ssf;
         if ((trusted = props.getProperty(prefix + ".ssl.trust")) != null) {
            try {
               MailSSLSocketFactory msf = new MailSSLSocketFactory();
               if (trusted.equals("*")) {
                  msf.setTrustAllHosts(true);
               } else {
                  msf.setTrustedHosts(trusted.split("\\s+"));
               }

               ssf = msf;
            } catch (GeneralSecurityException var19) {
               IOException ioex = new IOException("Can't create MailSSLSocketFactory");
               ioex.initCause(var19);
               throw ioex;
            }
         } else {
            ssf = (SSLSocketFactory)SSLSocketFactory.getDefault();
         }

         socket = ssf.createSocket(socket, host, port, true);
         sf = ssf;
      }

      configureSSLSocket(socket, host, props, prefix, sf);
      return socket;
   }

   private static SocketFactory getSocketFactory(String sfClass) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      if (sfClass != null && sfClass.length() != 0) {
         ClassLoader cl = getContextClassLoader();
         Class clsSockFact = null;
         if (cl != null) {
            try {
               clsSockFact = Class.forName(sfClass, false, cl);
            } catch (ClassNotFoundException var5) {
            }
         }

         if (clsSockFact == null) {
            clsSockFact = Class.forName(sfClass);
         }

         Method mthGetDefault = clsSockFact.getMethod("getDefault");
         return (SocketFactory)mthGetDefault.invoke(new Object());
      } else {
         return null;
      }
   }

   public static Socket startTLS(Socket socket) throws IOException {
      return startTLS(socket, new Properties(), "socket");
   }

   public static Socket startTLS(Socket socket, Properties props, String prefix) throws IOException {
      InetAddress a = socket.getInetAddress();
      String host = a.getHostName();
      return startTLS(socket, host, props, prefix);
   }

   public static Socket startTLS(Socket socket, String host, Properties props, String prefix) throws IOException {
      int port = socket.getPort();
      if (logger.isLoggable(Level.FINER)) {
         logger.finer("startTLS host " + host + ", port " + port);
      }

      String sfErr = "unknown socket factory";

      try {
         SSLSocketFactory ssf = null;
         SocketFactory sf = null;
         Object sfo = props.get(prefix + ".ssl.socketFactory");
         if (sfo instanceof SocketFactory) {
            sf = (SocketFactory)sfo;
            sfErr = "SSL socket factory instance " + sf;
         }

         if (sf == null) {
            String sfClass = props.getProperty(prefix + ".ssl.socketFactory.class");
            sf = getSocketFactory(sfClass);
            sfErr = "SSL socket factory class " + sfClass;
         }

         if (sf != null && sf instanceof SSLSocketFactory) {
            ssf = (SSLSocketFactory)sf;
         }

         if (ssf == null) {
            sfo = props.get(prefix + ".socketFactory");
            if (sfo instanceof SocketFactory) {
               sf = (SocketFactory)sfo;
               sfErr = "socket factory instance " + sf;
            }

            if (sf == null) {
               String sfClass = props.getProperty(prefix + ".socketFactory.class");
               sf = getSocketFactory(sfClass);
               sfErr = "socket factory class " + sfClass;
            }

            if (sf != null && sf instanceof SSLSocketFactory) {
               ssf = (SSLSocketFactory)sf;
            }
         }

         label50:
         if (ssf != null) {
            socket = ssf.createSocket(socket, host, port, true);
            configureSSLSocket(socket, host, props, prefix, ssf);
            return socket;
         } else {
            String trusted;
            if ((trusted = props.getProperty(prefix + ".ssl.trust")) != null) {
               try {
                  MailSSLSocketFactory msf = new MailSSLSocketFactory();
                  if (trusted.equals("*")) {
                     msf.setTrustAllHosts(true);
                  } else {
                     msf.setTrustedHosts(trusted.split("\\s+"));
                  }

                  ssf = msf;
                  sfErr = "mail SSL socket factory";
                  break label50;
               } catch (GeneralSecurityException var12) {
                  IOException ioex = new IOException("Can't create MailSSLSocketFactory");
                  ioex.initCause(var12);
                  throw ioex;
               }
            } else {
               ssf = (SSLSocketFactory)SSLSocketFactory.getDefault();
               sfErr = "default SSL socket factory";
               break label50;
            }
         }
      } catch (Exception var13) {
         Exception ex = var13;
         if (var13 instanceof InvocationTargetException) {
            Throwable t = ((InvocationTargetException)var13).getTargetException();
            if (t instanceof Exception) {
               ex = (Exception)t;
            }
         }

         if (ex instanceof IOException) {
            throw (IOException)ex;
         } else {
            IOException ioex = new IOException("Exception in startTLS using " + sfErr + ": host, port: " + host + ", " + port + "; Exception: " + ex);
            ioex.initCause(ex);
            throw ioex;
         }
      }
   }

   private static void configureSSLSocket(Socket socket, String host, Properties props, String prefix, SocketFactory sf) throws IOException {
      if (socket instanceof SSLSocket) {
         SSLSocket sslsocket = (SSLSocket)socket;
         String protocols = props.getProperty(prefix + ".ssl.protocols", null);
         if (protocols != null) {
            sslsocket.setEnabledProtocols(stringArray(protocols));
         } else {
            sslsocket.setEnabledProtocols(new String[]{"TLSv1"});
         }

         String ciphers = props.getProperty(prefix + ".ssl.ciphersuites", null);
         if (ciphers != null) {
            sslsocket.setEnabledCipherSuites(stringArray(ciphers));
         }

         if (logger.isLoggable(Level.FINER)) {
            logger.finer("SSL protocols after " + Arrays.asList(sslsocket.getEnabledProtocols()));
            logger.finer("SSL ciphers after " + Arrays.asList(sslsocket.getEnabledCipherSuites()));
         }

         sslsocket.startHandshake();
         boolean idCheck = PropUtil.getBooleanProperty(props, prefix + ".ssl.checkserveridentity", false);
         if (idCheck) {
            checkServerIdentity(host, sslsocket);
         }

         if (sf instanceof MailSSLSocketFactory) {
            MailSSLSocketFactory msf = (MailSSLSocketFactory)sf;
            if (!msf.isServerTrusted(host, sslsocket)) {
               try {
                  sslsocket.close();
               } finally {
                  throw new IOException("Server is not trusted: " + host);
               }

               throw new IOException("Server is not trusted: " + host);
            }
         }
      }
   }

   private static void checkServerIdentity(String server, SSLSocket sslSocket) throws IOException {
      try {
         Certificate[] certChain = sslSocket.getSession().getPeerCertificates();
         if (certChain != null && certChain.length > 0 && certChain[0] instanceof X509Certificate && matchCert(server, (X509Certificate)certChain[0])) {
            return;
         }
      } catch (SSLPeerUnverifiedException var4) {
         sslSocket.close();
         IOException ioex = new IOException("Can't verify identity of server: " + server);
         ioex.initCause(var4);
         throw ioex;
      }

      sslSocket.close();
      throw new IOException("Can't verify identity of server: " + server);
   }

   private static boolean matchCert(String server, X509Certificate cert) {
      if (logger.isLoggable(Level.FINER)) {
         logger.finer("matchCert server " + server + ", cert " + cert);
      }

      try {
         Class hnc = Class.forName("sun.security.util.HostnameChecker");
         Method getInstance = hnc.getMethod("getInstance", Byte.TYPE);
         Object hostnameChecker = getInstance.invoke(new Object(), (byte)2);
         if (logger.isLoggable(Level.FINER)) {
            logger.finer("using sun.security.util.HostnameChecker");
         }

         Method match = hnc.getMethod("match", String.class, X509Certificate.class);

         try {
            match.invoke(hostnameChecker, server, cert);
            return true;
         } catch (InvocationTargetException var8) {
            logger.log(Level.FINER, "FAIL", (Throwable)var8);
            return false;
         }
      } catch (Exception var10) {
         logger.log(Level.FINER, "NO sun.security.util.HostnameChecker", (Throwable)var10);

         try {
            Collection names = cert.getSubjectAlternativeNames();
            if (names != null) {
               boolean foundName = false;

               for(List nameEnt : names) {
                  Integer type = (Integer)nameEnt.get(0);
                  if (type == 2) {
                     foundName = true;
                     String name = (String)nameEnt.get(1);
                     if (logger.isLoggable(Level.FINER)) {
                        logger.finer("found name: " + name);
                     }

                     if (matchServer(server, name)) {
                        return true;
                     }
                  }
               }

               if (foundName) {
                  return false;
               }
            }
         } catch (CertificateParsingException var9) {
         }

         Pattern p = Pattern.compile("CN=([^,]*)");
         Matcher m = p.matcher(cert.getSubjectX500Principal().getName());
         return m.find() && matchServer(server, m.group(1).trim());
      }
   }

   private static boolean matchServer(String server, String name) {
      if (logger.isLoggable(Level.FINER)) {
         logger.finer("match server " + server + " with " + name);
      }

      if (!name.startsWith("*.")) {
         return server.equalsIgnoreCase(name);
      } else {
         String tail = name.substring(2);
         if (tail.length() == 0) {
            return false;
         } else {
            int off = server.length() - tail.length();
            if (off < 1) {
               return false;
            } else {
               return server.charAt(off - 1) == '.' && server.regionMatches(true, off, tail, 0, tail.length());
            }
         }
      }
   }

   private static String[] stringArray(String s) {
      StringTokenizer st = new StringTokenizer(s);
      List tokens = new ArrayList();

      while(st.hasMoreTokens()) {
         tokens.add(st.nextToken());
      }

      return tokens.toArray(new String[tokens.size()]);
   }

   private static ClassLoader getContextClassLoader() {
      return AccessController.doPrivileged(new PrivilegedAction() {
         @Override
         public Object run() {
            ClassLoader cl = null;

            try {
               cl = Thread.currentThread().getContextClassLoader();
            } catch (SecurityException var3) {
            }

            return cl;
         }
      });
   }
}
