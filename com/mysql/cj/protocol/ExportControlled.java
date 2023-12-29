package com.mysql.cj.protocol;

import com.mysql.cj.ServerVersion;
import com.mysql.cj.conf.PropertyDefinitions;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.FeatureNotAvailableException;
import com.mysql.cj.exceptions.RSAException;
import com.mysql.cj.exceptions.SSLParamsException;
import com.mysql.cj.util.Base64Decoder;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.util.Util;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;

public class ExportControlled {
   private static final String TLSv1 = "TLSv1";
   private static final String TLSv1_1 = "TLSv1.1";
   private static final String TLSv1_2 = "TLSv1.2";
   private static final String[] TLS_PROTOCOLS = new String[]{"TLSv1.2", "TLSv1.1", "TLSv1"};

   private ExportControlled() {
   }

   public static boolean enabled() {
      return true;
   }

   private static String[] getAllowedCiphers(PropertySet pset, ServerVersion serverVersion, String[] socketCipherSuites) {
      List<String> allowedCiphers = null;
      String enabledSSLCipherSuites = pset.getStringProperty("enabledSSLCipherSuites").getValue();
      if (!StringUtils.isNullOrEmpty(enabledSSLCipherSuites)) {
         allowedCiphers = new ArrayList<>();
         List<String> availableCiphers = Arrays.asList(socketCipherSuites);

         for(String cipher : enabledSSLCipherSuites.split("\\s*,\\s*")) {
            if (availableCiphers.contains(cipher)) {
               allowedCiphers.add(cipher);
            }
         }
      } else if (serverVersion != null
         && !serverVersion.meetsMinimum(ServerVersion.parseVersion("5.7.6"))
         && (!serverVersion.meetsMinimum(ServerVersion.parseVersion("5.6.26")) || serverVersion.meetsMinimum(ServerVersion.parseVersion("5.7.0")))
         && (!serverVersion.meetsMinimum(ServerVersion.parseVersion("5.5.45")) || serverVersion.meetsMinimum(ServerVersion.parseVersion("5.6.0")))) {
         allowedCiphers = new ArrayList<>();

         for(String cipher : socketCipherSuites) {
            if (cipher.indexOf("_DHE_") == -1 && cipher.indexOf("_DH_") == -1) {
               allowedCiphers.add(cipher);
            }
         }
      }

      return allowedCiphers == null ? null : allowedCiphers.toArray(new String[0]);
   }

   private static String[] getAllowedProtocols(PropertySet pset, ServerVersion serverVersion, String[] socketProtocols) {
      String enabledTLSProtocols = pset.getStringProperty("enabledTLSProtocols").getValue();
      String[] tryProtocols = null;
      if (enabledTLSProtocols != null && enabledTLSProtocols.length() > 0) {
         tryProtocols = enabledTLSProtocols.split("\\s*,\\s*");
      } else if (serverVersion == null
         || !serverVersion.meetsMinimum(ServerVersion.parseVersion("8.0.4"))
            && (!serverVersion.meetsMinimum(ServerVersion.parseVersion("5.6.0")) || !Util.isEnterpriseEdition(serverVersion.toString()))) {
         tryProtocols = new String[]{"TLSv1.1", "TLSv1"};
      } else {
         tryProtocols = TLS_PROTOCOLS;
      }

      List<String> configuredProtocols = new ArrayList<>(Arrays.asList(tryProtocols));
      List<String> jvmSupportedProtocols = Arrays.asList(socketProtocols);
      List<String> allowedProtocols = new ArrayList<>();

      for(String protocol : TLS_PROTOCOLS) {
         if (jvmSupportedProtocols.contains(protocol) && configuredProtocols.contains(protocol)) {
            allowedProtocols.add(protocol);
         }
      }

      return allowedProtocols.toArray(new String[0]);
   }

   private static ExportControlled.KeyStoreConf getTrustStoreConf(
      PropertySet propertySet, String keyStoreUrlPropertyName, String keyStorePasswordPropertyName, String keyStoreTypePropertyName, boolean required
   ) {
      String trustStoreUrl = propertySet.getStringProperty(keyStoreUrlPropertyName).getValue();
      String trustStorePassword = propertySet.getStringProperty(keyStorePasswordPropertyName).getValue();
      String trustStoreType = propertySet.getStringProperty(keyStoreTypePropertyName).getValue();
      if (StringUtils.isNullOrEmpty(trustStoreUrl)) {
         trustStoreUrl = System.getProperty("javax.net.ssl.trustStore");
         trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
         trustStoreType = System.getProperty("javax.net.ssl.trustStoreType");
         if (StringUtils.isNullOrEmpty(trustStoreType)) {
            trustStoreType = propertySet.getStringProperty(keyStoreTypePropertyName).getInitialValue();
         }

         if (!StringUtils.isNullOrEmpty(trustStoreUrl)) {
            try {
               new URL(trustStoreUrl);
            } catch (MalformedURLException var9) {
               trustStoreUrl = "file:" + trustStoreUrl;
            }
         }
      }

      if (required && StringUtils.isNullOrEmpty(trustStoreUrl)) {
         throw new CJCommunicationsException("No truststore provided to verify the Server certificate.");
      } else {
         return new ExportControlled.KeyStoreConf(trustStoreUrl, trustStorePassword, trustStoreType);
      }
   }

   private static ExportControlled.KeyStoreConf getKeyStoreConf(
      PropertySet propertySet, String keyStoreUrlPropertyName, String keyStorePasswordPropertyName, String keyStoreTypePropertyName
   ) {
      String keyStoreUrl = propertySet.getStringProperty(keyStoreUrlPropertyName).getValue();
      String keyStorePassword = propertySet.getStringProperty(keyStorePasswordPropertyName).getValue();
      String keyStoreType = propertySet.getStringProperty(keyStoreTypePropertyName).getValue();
      if (StringUtils.isNullOrEmpty(keyStoreUrl)) {
         keyStoreUrl = System.getProperty("javax.net.ssl.keyStore");
         keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
         keyStoreType = System.getProperty("javax.net.ssl.keyStoreType");
         if (StringUtils.isNullOrEmpty(keyStoreType)) {
            keyStoreType = propertySet.getStringProperty(keyStoreTypePropertyName).getInitialValue();
         }

         if (!StringUtils.isNullOrEmpty(keyStoreUrl)) {
            try {
               new URL(keyStoreUrl);
            } catch (MalformedURLException var8) {
               keyStoreUrl = "file:" + keyStoreUrl;
            }
         }
      }

      return new ExportControlled.KeyStoreConf(keyStoreUrl, keyStorePassword, keyStoreType);
   }

   public static Socket performTlsHandshake(Socket rawSocket, SocketConnection socketConnection, ServerVersion serverVersion) throws IOException, SSLParamsException, FeatureNotAvailableException {
      PropertySet pset = socketConnection.getPropertySet();
      boolean verifyServerCert = pset.getBooleanProperty("verifyServerCertificate").getValue();
      PropertyDefinitions.SslMode sslMode = pset.<PropertyDefinitions.SslMode>getEnumProperty("xdevapi.ssl-mode").getValue();
      ExportControlled.KeyStoreConf trustStore = !verifyServerCert
         ? new ExportControlled.KeyStoreConf()
         : getTrustStoreConf(
            pset, "trustCertificateKeyStoreUrl", "trustCertificateKeyStorePassword", "trustCertificateKeyStoreType", verifyServerCert && serverVersion == null
         );
      ExportControlled.KeyStoreConf keyStore = getKeyStoreConf(
         pset, "clientCertificateKeyStoreUrl", "clientCertificateKeyStorePassword", "clientCertificateKeyStoreType"
      );
      SSLSocketFactory socketFactory = getSSLContext(
            keyStore.keyStoreUrl,
            keyStore.keyStoreType,
            keyStore.keyStorePassword,
            trustStore.keyStoreUrl,
            trustStore.keyStoreType,
            trustStore.keyStorePassword,
            serverVersion != null,
            verifyServerCert,
            sslMode == PropertyDefinitions.SslMode.VERIFY_IDENTITY ? socketConnection.getHost() : null,
            socketConnection.getExceptionInterceptor()
         )
         .getSocketFactory();
      SSLSocket sslSocket = (SSLSocket)socketFactory.createSocket(rawSocket, socketConnection.getHost(), socketConnection.getPort(), true);
      sslSocket.setEnabledProtocols(getAllowedProtocols(pset, serverVersion, sslSocket.getSupportedProtocols()));
      String[] allowedCiphers = getAllowedCiphers(pset, serverVersion, sslSocket.getEnabledCipherSuites());
      if (allowedCiphers != null) {
         sslSocket.setEnabledCipherSuites(allowedCiphers);
      }

      sslSocket.startHandshake();
      return sslSocket;
   }

   public static SSLContext getSSLContext(
      String clientCertificateKeyStoreUrl,
      String clientCertificateKeyStoreType,
      String clientCertificateKeyStorePassword,
      String trustCertificateKeyStoreUrl,
      String trustCertificateKeyStoreType,
      String trustCertificateKeyStorePassword,
      boolean fallbackToDefaultTrustStore,
      boolean verifyServerCert,
      String hostName,
      ExceptionInterceptor exceptionInterceptor
   ) throws SSLParamsException {
      TrustManagerFactory tmf = null;
      KeyManagerFactory kmf = null;
      KeyManager[] kms = null;
      List<TrustManager> tms = new ArrayList<>();

      try {
         tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
         kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      } catch (NoSuchAlgorithmException var74) {
         throw (SSLParamsException)ExceptionFactory.createException(
            SSLParamsException.class,
            "Default algorithm definitions for TrustManager and/or KeyManager are invalid.  Check java security properties file.",
            var74,
            exceptionInterceptor
         );
      }

      if (!StringUtils.isNullOrEmpty(clientCertificateKeyStoreUrl)) {
         InputStream ksIS = null;

         try {
            if (!StringUtils.isNullOrEmpty(clientCertificateKeyStoreType)) {
               KeyStore clientKeyStore = KeyStore.getInstance(clientCertificateKeyStoreType);
               URL ksURL = new URL(clientCertificateKeyStoreUrl);
               char[] password = clientCertificateKeyStorePassword == null ? new char[0] : clientCertificateKeyStorePassword.toCharArray();
               ksIS = ksURL.openStream();
               clientKeyStore.load(ksIS, password);
               kmf.init(clientKeyStore, password);
               kms = kmf.getKeyManagers();
            }
         } catch (UnrecoverableKeyException var68) {
            throw (SSLParamsException)ExceptionFactory.createException(
               SSLParamsException.class, "Could not recover keys from client keystore.  Check password?", var68, exceptionInterceptor
            );
         } catch (NoSuchAlgorithmException var69) {
            throw (SSLParamsException)ExceptionFactory.createException(
               SSLParamsException.class, "Unsupported keystore algorithm [" + var69.getMessage() + "]", var69, exceptionInterceptor
            );
         } catch (KeyStoreException var70) {
            throw (SSLParamsException)ExceptionFactory.createException(
               SSLParamsException.class, "Could not create KeyStore instance [" + var70.getMessage() + "]", var70, exceptionInterceptor
            );
         } catch (CertificateException var71) {
            throw (SSLParamsException)ExceptionFactory.createException(
               SSLParamsException.class,
               "Could not load client" + clientCertificateKeyStoreType + " keystore from " + clientCertificateKeyStoreUrl,
               var71,
               exceptionInterceptor
            );
         } catch (MalformedURLException var72) {
            throw (SSLParamsException)ExceptionFactory.createException(
               SSLParamsException.class, clientCertificateKeyStoreUrl + " does not appear to be a valid URL.", var72, exceptionInterceptor
            );
         } catch (IOException var73) {
            throw (SSLParamsException)ExceptionFactory.createException(
               SSLParamsException.class, "Cannot open " + clientCertificateKeyStoreUrl + " [" + var73.getMessage() + "]", var73, exceptionInterceptor
            );
         } finally {
            if (ksIS != null) {
               try {
                  ksIS.close();
               } catch (IOException var65) {
               }
            }
         }
      }

      InputStream trustStoreIS = null;

      try {
         String trustStoreType = "";
         char[] trustStorePassword = null;
         KeyStore trustKeyStore = null;
         if (!StringUtils.isNullOrEmpty(trustCertificateKeyStoreUrl) && !StringUtils.isNullOrEmpty(trustCertificateKeyStoreType)) {
            trustStorePassword = trustCertificateKeyStorePassword == null ? new char[0] : trustCertificateKeyStorePassword.toCharArray();
            trustStoreIS = new URL(trustCertificateKeyStoreUrl).openStream();
            trustKeyStore = KeyStore.getInstance(trustCertificateKeyStoreType);
            trustKeyStore.load(trustStoreIS, trustStorePassword);
         }

         if (trustKeyStore != null || fallbackToDefaultTrustStore) {
            tmf.init(trustKeyStore);
            TrustManager[] origTms = tmf.getTrustManagers();

            for(TrustManager tm : origTms) {
               tms.add(
                  (TrustManager)(tm instanceof X509TrustManager
                     ? new ExportControlled.X509TrustManagerWrapper((X509TrustManager)tm, verifyServerCert, hostName)
                     : tm)
               );
            }
         }
      } catch (MalformedURLException var76) {
         throw (SSLParamsException)ExceptionFactory.createException(
            SSLParamsException.class, trustCertificateKeyStoreUrl + " does not appear to be a valid URL.", var76, exceptionInterceptor
         );
      } catch (NoSuchAlgorithmException var77) {
         throw (SSLParamsException)ExceptionFactory.createException(
            SSLParamsException.class, "Unsupported keystore algorithm [" + var77.getMessage() + "]", var77, exceptionInterceptor
         );
      } catch (KeyStoreException var78) {
         throw (SSLParamsException)ExceptionFactory.createException(
            SSLParamsException.class, "Could not create KeyStore instance [" + var78.getMessage() + "]", var78, exceptionInterceptor
         );
      } catch (CertificateException var79) {
         throw (SSLParamsException)ExceptionFactory.createException(
            SSLParamsException.class,
            "Could not load trust" + trustCertificateKeyStoreType + " keystore from " + trustCertificateKeyStoreUrl,
            var79,
            exceptionInterceptor
         );
      } catch (IOException var80) {
         throw (SSLParamsException)ExceptionFactory.createException(
            SSLParamsException.class, "Cannot open " + trustCertificateKeyStoreUrl + " [" + var80.getMessage() + "]", var80, exceptionInterceptor
         );
      } finally {
         if (trustStoreIS != null) {
            try {
               trustStoreIS.close();
            } catch (IOException var64) {
            }
         }
      }

      if (tms.size() == 0) {
         tms.add(new ExportControlled.X509TrustManagerWrapper(verifyServerCert, hostName));
      }

      try {
         SSLContext sslContext = SSLContext.getInstance("TLS");
         sslContext.init(kms, tms.toArray(new TrustManager[tms.size()]), null);
         return sslContext;
      } catch (NoSuchAlgorithmException var66) {
         throw new SSLParamsException("TLS is not a valid SSL protocol.", var66);
      } catch (KeyManagementException var67) {
         throw new SSLParamsException("KeyManagementException: " + var67.getMessage(), var67);
      }
   }

   public static boolean isSSLEstablished(Socket socket) {
      return SSLSocket.class.isAssignableFrom(socket.getClass());
   }

   public static RSAPublicKey decodeRSAPublicKey(String key) throws RSAException {
      if (key == null) {
         throw (RSAException)ExceptionFactory.createException(RSAException.class, "Key parameter is null");
      } else {
         int offset = key.indexOf("\n") + 1;
         int len = key.indexOf("-----END PUBLIC KEY-----") - offset;
         byte[] certificateData = Base64Decoder.decode(key.getBytes(), offset, len);
         X509EncodedKeySpec spec = new X509EncodedKeySpec(certificateData);

         try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey)kf.generatePublic(spec);
         } catch (InvalidKeySpecException | NoSuchAlgorithmException var6) {
            throw (RSAException)ExceptionFactory.createException(RSAException.class, "Unable to decode public key", var6);
         }
      }
   }

   public static byte[] encryptWithRSAPublicKey(byte[] source, RSAPublicKey key, String transformation) throws RSAException {
      try {
         Cipher cipher = Cipher.getInstance(transformation);
         cipher.init(1, key);
         return cipher.doFinal(source);
      } catch (NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException var4) {
         throw (RSAException)ExceptionFactory.createException(RSAException.class, var4.getMessage(), var4);
      }
   }

   public static byte[] encryptWithRSAPublicKey(byte[] source, RSAPublicKey key) throws RSAException {
      return encryptWithRSAPublicKey(source, key, "RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
   }

   public static AsynchronousSocketChannel startTlsOnAsynchronousChannel(AsynchronousSocketChannel channel, SocketConnection socketConnection) throws SSLException {
      PropertySet propertySet = socketConnection.getPropertySet();
      PropertyDefinitions.SslMode sslMode = propertySet.<PropertyDefinitions.SslMode>getEnumProperty("xdevapi.ssl-mode").getValue();
      boolean verifyServerCert = sslMode == PropertyDefinitions.SslMode.VERIFY_CA || sslMode == PropertyDefinitions.SslMode.VERIFY_IDENTITY;
      ExportControlled.KeyStoreConf trustStore = !verifyServerCert
         ? new ExportControlled.KeyStoreConf()
         : getTrustStoreConf(propertySet, "xdevapi.ssl-truststore", "xdevapi.ssl-truststore-password", "xdevapi.ssl-truststore-type", true);
      ExportControlled.KeyStoreConf keyStore = getKeyStoreConf(
         propertySet, "clientCertificateKeyStoreUrl", "clientCertificateKeyStorePassword", "clientCertificateKeyStoreType"
      );
      SSLContext sslContext = getSSLContext(
         keyStore.keyStoreUrl,
         keyStore.keyStoreType,
         keyStore.keyStorePassword,
         trustStore.keyStoreUrl,
         trustStore.keyStoreType,
         trustStore.keyStorePassword,
         false,
         verifyServerCert,
         sslMode == PropertyDefinitions.SslMode.VERIFY_IDENTITY ? socketConnection.getHost() : null,
         null
      );
      SSLEngine sslEngine = sslContext.createSSLEngine();
      sslEngine.setUseClientMode(true);
      sslEngine.setEnabledProtocols(getAllowedProtocols(propertySet, null, sslEngine.getSupportedProtocols()));
      String[] allowedCiphers = getAllowedCiphers(propertySet, null, sslEngine.getEnabledCipherSuites());
      if (allowedCiphers != null) {
         sslEngine.setEnabledCipherSuites(allowedCiphers);
      }

      performTlsHandshake(sslEngine, channel);
      return new TlsAsynchronousSocketChannel(channel, sslEngine);
   }

   private static void performTlsHandshake(SSLEngine sslEngine, AsynchronousSocketChannel channel) throws SSLException {
      sslEngine.beginHandshake();
      HandshakeStatus handshakeStatus = sslEngine.getHandshakeStatus();
      int packetBufferSize = sslEngine.getSession().getPacketBufferSize();
      ByteBuffer myNetData = ByteBuffer.allocate(packetBufferSize);
      ByteBuffer peerNetData = ByteBuffer.allocate(packetBufferSize);
      int appBufferSize = sslEngine.getSession().getApplicationBufferSize();
      ByteBuffer myAppData = ByteBuffer.allocate(appBufferSize);
      ByteBuffer peerAppData = ByteBuffer.allocate(appBufferSize);
      SSLEngineResult res = null;

      while(handshakeStatus != HandshakeStatus.FINISHED && handshakeStatus != HandshakeStatus.NOT_HANDSHAKING) {
         switch(handshakeStatus) {
            case NEED_WRAP:
               ((Buffer)myNetData).clear();
               res = sslEngine.wrap(myAppData, myNetData);
               handshakeStatus = res.getHandshakeStatus();
               switch(res.getStatus()) {
                  case OK:
                     ((Buffer)myNetData).flip();
                     write(channel, myNetData);
                     continue;
                  case BUFFER_OVERFLOW:
                  case BUFFER_UNDERFLOW:
                  case CLOSED:
                     throw new CJCommunicationsException("Unacceptable SSLEngine result: " + res);
                  default:
                     continue;
               }
            case NEED_UNWRAP:
               ((Buffer)peerNetData).flip();
               res = sslEngine.unwrap(peerNetData, peerAppData);
               handshakeStatus = res.getHandshakeStatus();
               switch(res.getStatus()) {
                  case OK:
                     peerNetData.compact();
                     continue;
                  case BUFFER_OVERFLOW:
                     int newPeerAppDataSize = sslEngine.getSession().getApplicationBufferSize();
                     if (newPeerAppDataSize > peerAppData.capacity()) {
                        ByteBuffer newPeerAppData = ByteBuffer.allocate(newPeerAppDataSize);
                        newPeerAppData.put(peerAppData);
                        ((Buffer)newPeerAppData).flip();
                        peerAppData = newPeerAppData;
                     } else {
                        peerAppData.compact();
                     }
                     continue;
                  case BUFFER_UNDERFLOW:
                     int newPeerNetDataSize = sslEngine.getSession().getPacketBufferSize();
                     if (newPeerNetDataSize > peerNetData.capacity()) {
                        ByteBuffer newPeerNetData = ByteBuffer.allocate(newPeerNetDataSize);
                        newPeerNetData.put(peerNetData);
                        ((Buffer)newPeerNetData).flip();
                        peerNetData = newPeerNetData;
                     } else {
                        peerNetData.compact();
                     }

                     if (read(channel, peerNetData) < 0) {
                        throw new CJCommunicationsException("Server does not provide enough data to proceed with SSL handshake.");
                     }
                     continue;
                  case CLOSED:
                     throw new CJCommunicationsException("Unacceptable SSLEngine result: " + res);
                  default:
                     continue;
               }
            case NEED_TASK:
               sslEngine.getDelegatedTask().run();
               handshakeStatus = sslEngine.getHandshakeStatus();
            case FINISHED:
            case NOT_HANDSHAKING:
         }
      }
   }

   private static void write(final AsynchronousSocketChannel channel, final ByteBuffer data) {
      final CompletableFuture<Void> f = new CompletableFuture<>();
      final int bytesToWrite = data.limit();
      CompletionHandler<Integer, Void> handler = new CompletionHandler<Integer, Void>() {
         public void completed(Integer bytesWritten, Void nothing) {
            if (bytesWritten < bytesToWrite) {
               channel.write(data, null, this);
            } else {
               f.complete(null);
            }
         }

         public void failed(Throwable exc, Void nothing) {
            f.completeExceptionally(exc);
         }
      };
      channel.write(data, null, handler);

      try {
         f.get();
      } catch (ExecutionException | InterruptedException var6) {
         throw new CJCommunicationsException(var6);
      }
   }

   private static Integer read(AsynchronousSocketChannel channel, ByteBuffer data) {
      Future<Integer> f = channel.read(data);

      try {
         return f.get();
      } catch (ExecutionException | InterruptedException var4) {
         throw new CJCommunicationsException(var4);
      }
   }

   private static class KeyStoreConf {
      public String keyStoreUrl = null;
      public String keyStorePassword = null;
      public String keyStoreType = "JKS";

      public KeyStoreConf() {
      }

      public KeyStoreConf(String keyStoreUrl, String keyStorePassword, String keyStoreType) {
         this.keyStoreUrl = keyStoreUrl;
         this.keyStorePassword = keyStorePassword;
         this.keyStoreType = keyStoreType;
      }
   }

   public static class X509TrustManagerWrapper implements X509TrustManager {
      private X509TrustManager origTm = null;
      private boolean verifyServerCert = false;
      private String hostName = null;
      private CertificateFactory certFactory = null;
      private PKIXParameters validatorParams = null;
      private CertPathValidator validator = null;

      public X509TrustManagerWrapper(X509TrustManager tm, boolean verifyServerCertificate, String hostName) throws CertificateException {
         this.origTm = tm;
         this.verifyServerCert = verifyServerCertificate;
         this.hostName = hostName;
         if (verifyServerCertificate) {
            try {
               Set<TrustAnchor> anch = Arrays.stream(tm.getAcceptedIssuers()).map(c -> new TrustAnchor(c, null)).collect(Collectors.toSet());
               this.validatorParams = new PKIXParameters(anch);
               this.validatorParams.setRevocationEnabled(false);
               this.validator = CertPathValidator.getInstance("PKIX");
               this.certFactory = CertificateFactory.getInstance("X.509");
            } catch (Exception var5) {
               throw new CertificateException(var5);
            }
         }
      }

      public X509TrustManagerWrapper(boolean verifyServerCertificate, String hostName) {
         this.verifyServerCert = verifyServerCertificate;
         this.hostName = hostName;
      }

      @Override
      public X509Certificate[] getAcceptedIssuers() {
         return this.origTm != null ? this.origTm.getAcceptedIssuers() : new X509Certificate[0];
      }

      @Override
      public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
         for(int i = 0; i < chain.length; ++i) {
            chain[i].checkValidity();
         }

         if (this.validatorParams != null) {
            X509CertSelector certSelect = new X509CertSelector();
            certSelect.setSerialNumber(chain[0].getSerialNumber());

            try {
               CertPath certPath = this.certFactory.generateCertPath(Arrays.asList(chain));
               CertPathValidatorResult result = this.validator.validate(certPath, this.validatorParams);
               ((PKIXCertPathValidatorResult)result).getTrustAnchor().getTrustedCert().checkValidity();
            } catch (InvalidAlgorithmParameterException var8) {
               throw new CertificateException(var8);
            } catch (CertPathValidatorException var9) {
               throw new CertificateException(var9);
            }
         }

         if (this.verifyServerCert) {
            if (this.origTm == null) {
               throw new CertificateException("Can't verify server certificate because no trust manager is found.");
            }

            this.origTm.checkServerTrusted(chain, authType);
            if (this.hostName != null) {
               String dn = chain[0].getSubjectX500Principal().getName("RFC2253");
               String cn = null;

               try {
                  LdapName ldapDN = new LdapName(dn);

                  for(Rdn rdn : ldapDN.getRdns()) {
                     if (rdn.getType().equalsIgnoreCase("CN")) {
                        cn = rdn.getValue().toString();
                        break;
                     }
                  }
               } catch (InvalidNameException var10) {
                  throw new CertificateException("Failed to retrieve the Common Name (CN) from the server certificate.");
               }

               if (!this.hostName.equalsIgnoreCase(cn)) {
                  throw new CertificateException(
                     "Server certificate identity check failed. The certificate Common Name '" + cn + "' does not match with '" + this.hostName + "'."
                  );
               }
            }
         }
      }

      @Override
      public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
         this.origTm.checkClientTrusted(chain, authType);
      }
   }
}
