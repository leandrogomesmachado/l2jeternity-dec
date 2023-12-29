package l2e.loginserver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public class IpBanManager {
   private static final Logger _log = Logger.getLogger(IpBanManager.class.getName());
   private static final IpBanManager _instance = new IpBanManager();
   private final Map<String, IpBanManager.IpSession> ips = new HashMap<>();
   private final ReadWriteLock lock = new ReentrantReadWriteLock();
   private final Lock readLock = this.lock.readLock();
   private final Lock writeLock = this.lock.writeLock();

   public static final IpBanManager getInstance() {
      return _instance;
   }

   private IpBanManager() {
      ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
         long currentMillis = System.currentTimeMillis();
         this.writeLock.lock();

         try {
            Iterator<IpBanManager.IpSession> itr = this.ips.values().iterator();

            while(itr.hasNext()) {
               IpBanManager.IpSession session = itr.next();
               if (session.banExpire < currentMillis && session.lastTry < currentMillis - Config.LOGIN_TRY_TIMEOUT) {
                  itr.remove();
               }
            }
         } finally {
            this.writeLock.unlock();
         }
      }, 1000L, 1000L);
   }

   public boolean isIpBanned(String ip) {
      this.readLock.lock();

      boolean var3;
      try {
         IpBanManager.IpSession ipsession;
         if ((ipsession = this.ips.get(ip)) != null) {
            return ipsession.banExpire > System.currentTimeMillis();
         }

         var3 = false;
      } finally {
         this.readLock.unlock();
      }

      return var3;
   }

   public boolean tryLogin(String ip, boolean success) {
      this.writeLock.lock();

      boolean var6;
      try {
         IpBanManager.IpSession ipsession;
         if ((ipsession = this.ips.get(ip)) == null) {
            this.ips.put(ip, ipsession = new IpBanManager.IpSession());
         }

         long currentMillis = System.currentTimeMillis();
         if (currentMillis - ipsession.lastTry < Config.LOGIN_TRY_TIMEOUT) {
            success = false;
         }

         if (success) {
            if (ipsession.tryCount > 0) {
               --ipsession.tryCount;
            }
         } else if (ipsession.tryCount < Config.LOGIN_TRY_BEFORE_BAN) {
            ++ipsession.tryCount;
         }

         ipsession.lastTry = currentMillis;
         if (ipsession.tryCount != Config.LOGIN_TRY_BEFORE_BAN) {
            return true;
         }

         _log.warning("IpBanManager: " + ip + " banned for " + Config.IP_BAN_TIME / 1000L + " seconds.");
         ipsession.banExpire = currentMillis + Config.IP_BAN_TIME;
         var6 = false;
      } finally {
         this.writeLock.unlock();
      }

      return var6;
   }

   private class IpSession {
      public int tryCount;
      public long lastTry;
      public long banExpire;

      private IpSession() {
      }
   }
}
