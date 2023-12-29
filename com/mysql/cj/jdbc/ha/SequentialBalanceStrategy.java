package com.mysql.cj.jdbc.ha;

import com.mysql.cj.jdbc.ConnectionImpl;
import com.mysql.cj.jdbc.JdbcConnection;
import java.lang.reflect.InvocationHandler;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SequentialBalanceStrategy implements BalanceStrategy {
   private int currentHostIndex = -1;

   public ConnectionImpl pickConnection(
      InvocationHandler proxy, List<String> configuredHosts, Map<String, JdbcConnection> liveConnections, long[] responseTimes, int numRetries
   ) throws SQLException {
      int numHosts = configuredHosts.size();
      SQLException ex = null;
      Map<String, Long> blackList = ((LoadBalancedConnectionProxy)proxy).getGlobalBlacklist();
      int attempts = 0;

      ConnectionImpl conn;
      while(true) {
         while(true) {
            if (attempts >= numRetries) {
               if (ex != null) {
                  throw ex;
               }

               return null;
            }

            if (numHosts == 1) {
               this.currentHostIndex = 0;
               break;
            }

            if (this.currentHostIndex == -1) {
               int random = (int)Math.floor(Math.random() * (double)numHosts);

               for(int i = random; i < numHosts; ++i) {
                  if (!blackList.containsKey(configuredHosts.get(i))) {
                     this.currentHostIndex = i;
                     break;
                  }
               }

               if (this.currentHostIndex == -1) {
                  for(int i = 0; i < random; ++i) {
                     if (!blackList.containsKey(configuredHosts.get(i))) {
                        this.currentHostIndex = i;
                        break;
                     }
                  }
               }

               if (this.currentHostIndex != -1) {
                  break;
               }

               blackList = ((LoadBalancedConnectionProxy)proxy).getGlobalBlacklist();

               try {
                  Thread.sleep(250L);
               } catch (InterruptedException var16) {
               }
            } else {
               int i = this.currentHostIndex + 1;

               for(foundGoodHost = false; i < numHosts; ++i) {
                  if (!blackList.containsKey(configuredHosts.get(i))) {
                     this.currentHostIndex = i;
                     foundGoodHost = true;
                     break;
                  }
               }

               if (!foundGoodHost) {
                  for(int var18 = 0; var18 < this.currentHostIndex; ++var18) {
                     if (!blackList.containsKey(configuredHosts.get(var18))) {
                        this.currentHostIndex = var18;
                        foundGoodHost = true;
                        break;
                     }
                  }
               }

               if (foundGoodHost) {
                  break;
               }

               blackList = ((LoadBalancedConnectionProxy)proxy).getGlobalBlacklist();

               try {
                  Thread.sleep(250L);
               } catch (InterruptedException var15) {
               }
            }
         }

         String hostPortSpec = configuredHosts.get(this.currentHostIndex);
         conn = (ConnectionImpl)liveConnections.get(hostPortSpec);
         if (conn != null) {
            break;
         }

         try {
            conn = ((LoadBalancedConnectionProxy)proxy).createConnectionForHost(hostPortSpec);
            break;
         } catch (SQLException var17) {
            ex = var17;
            if (!((LoadBalancedConnectionProxy)proxy).shouldExceptionTriggerConnectionSwitch(var17)) {
               throw var17;
            }

            ((LoadBalancedConnectionProxy)proxy).addToGlobalBlacklist(hostPortSpec);

            try {
               Thread.sleep(250L);
            } catch (InterruptedException var14) {
            }
         }
      }

      return conn;
   }
}
