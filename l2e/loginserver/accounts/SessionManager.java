package l2e.loginserver.accounts;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import l2e.loginserver.ThreadPoolManager;
import l2e.loginserver.network.SessionKey;

public class SessionManager {
   private static final SessionManager _instance = new SessionManager();
   private final Map<SessionKey, SessionManager.Session> sessions = new HashMap<>();
   private final Lock lock = new ReentrantLock();

   public static final SessionManager getInstance() {
      return _instance;
   }

   private SessionManager() {
      ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
         this.lock.lock();

         try {
            long currentMillis = System.currentTimeMillis();
            Iterator<SessionManager.Session> itr = this.sessions.values().iterator();

            while(itr.hasNext()) {
               SessionManager.Session session = itr.next();
               if (session.getExpireTime() < currentMillis) {
                  itr.remove();
               }
            }
         } finally {
            this.lock.unlock();
         }
      }, 30000L, 30000L);
   }

   public SessionManager.Session openSession(Account account, String ip) {
      this.lock.lock();

      SessionManager.Session var4;
      try {
         SessionManager.Session session = new SessionManager.Session(account, ip);
         this.sessions.put(session.getSessionKey(), session);
         var4 = session;
      } finally {
         this.lock.unlock();
      }

      return var4;
   }

   public SessionManager.Session closeSession(SessionKey skey) {
      this.lock.lock();

      SessionManager.Session var2;
      try {
         var2 = this.sessions.remove(skey);
      } finally {
         this.lock.unlock();
      }

      return var2;
   }

   public SessionManager.Session getSessionByName(String name) {
      for(SessionManager.Session session : this.sessions.values()) {
         if (session._account.getLogin().equalsIgnoreCase(name)) {
            return session;
         }
      }

      return null;
   }

   public final class Session {
      private final Account _account;
      private final SessionKey _skey;
      private final long _expireTime;
      private final String _ip;

      private Session(Account account, String ip) {
         this._account = account;
         this._ip = ip;
         this._skey = SessionKey.create();
         this._expireTime = System.currentTimeMillis() + 60000L;
      }

      public SessionKey getSessionKey() {
         return this._skey;
      }

      public Account getAccount() {
         return this._account;
      }

      public String getIP() {
         return this._ip;
      }

      public long getExpireTime() {
         return this._expireTime;
      }
   }
}
