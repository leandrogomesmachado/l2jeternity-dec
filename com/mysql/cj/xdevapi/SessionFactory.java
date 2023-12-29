package com.mysql.cj.xdevapi;

import com.mysql.cj.conf.ConnectionUrl;
import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.InvalidConnectionAttributeException;
import java.util.Properties;

public class SessionFactory {
   private ConnectionUrl parseUrl(String url) {
      ConnectionUrl connUrl = ConnectionUrl.getConnectionUrlInstance(url, null);
      if (connUrl != null && connUrl.getType() == ConnectionUrl.Type.XDEVAPI_SESSION) {
         return connUrl;
      } else {
         throw (InvalidConnectionAttributeException)ExceptionFactory.createException(
            InvalidConnectionAttributeException.class, "Initialization via URL failed for \"" + url + "\""
         );
      }
   }

   public Session getSession(String url) {
      CJCommunicationsException latestException = null;
      ConnectionUrl connUrl = this.parseUrl(url);

      for(HostInfo hi : connUrl.getHostsList()) {
         try {
            return new SessionImpl(hi);
         } catch (CJCommunicationsException var7) {
            latestException = var7;
         }
      }

      if (latestException != null) {
         throw latestException;
      } else {
         return null;
      }
   }

   public Session getSession(Properties properties) {
      ConnectionUrl connUrl = ConnectionUrl.getConnectionUrlInstance(ConnectionUrl.Type.XDEVAPI_SESSION.getScheme(), properties);
      return new SessionImpl(connUrl.getMainHost());
   }
}
