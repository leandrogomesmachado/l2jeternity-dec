package l2e.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import l2e.commons.net.IPSettings;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.player.HwidTemplate;

public final class DoubleSessionManager {
   private static final Logger _log = Logger.getLogger(DoubleSessionManager.class.getName());
   public static final int GAME_ID = 0;
   public static final int OLYMPIAD_ID = 100;
   public static final int AERIAL_CLEFT_ID = 101;
   private final Map<Integer, Long> _lastDeathTimes = new ConcurrentHashMap<>();
   private final Map<Integer, List<HwidTemplate>> _protectedList = new ConcurrentHashMap<>();

   protected DoubleSessionManager() {
      IPSettings.getInstance().autoIpConfig();
   }

   public final void setLastDeathTime(int var1) {
      this._lastDeathTimes.put(var1, System.currentTimeMillis());
   }

   public final boolean check(Creature var1, Creature var2) {
      if (!Config.DOUBLE_SESSIONS_ENABLE) {
         return true;
      } else if (var2 == null) {
         return false;
      } else {
         Player var3 = var2.getActingPlayer();
         if (var3 == null) {
            return false;
         } else if (Config.DOUBLE_SESSIONS_INTERVAL > 0
            && this._lastDeathTimes.containsKey(var3.getObjectId())
            && System.currentTimeMillis() - this._lastDeathTimes.get(var3.getObjectId()) < (long)Config.DOUBLE_SESSIONS_INTERVAL) {
            if (Config.DEVELOPER) {
               _log.info(this.getClass().getSimpleName() + ": Action block. Fast Interval!");
            }

            return false;
         } else if (var1 != null) {
            Player var4 = var1.getActingPlayer();
            if (var4 == null) {
               return false;
            } else if (var3.getClient() == null
               || var4.getClient() == null
               || var3.getClient() != null && var3.getClient().isDetached()
               || var4.getClient() != null && var4.getClient().isDetached()) {
               return !Config.DOUBLE_SESSIONS_DISCONNECTED;
            } else {
               String var5 = Config.DOUBLE_SESSIONS_HWIDS ? var4.getHWID() : var4.getIPAddress();
               String var6 = Config.DOUBLE_SESSIONS_HWIDS ? var3.getHWID() : var3.getIPAddress();
               boolean var7 = var5.equalsIgnoreCase(var6);
               if (Config.DEVELOPER) {
                  _log.info(this.getClass().getSimpleName() + ": attacker - " + var5);
                  _log.info(this.getClass().getSimpleName() + ": target - " + var6);
                  if (var7) {
                     _log.info(this.getClass().getSimpleName() + ": Not valid conditions!");
                  } else {
                     _log.info(this.getClass().getSimpleName() + ": Valid conditions!");
                  }
               }

               return !var7;
            }
         } else {
            return true;
         }
      }
   }

   public final void clear() {
      this._lastDeathTimes.clear();
   }

   public final void registerEvent(int var1) {
      this._protectedList.putIfAbsent(var1, new ArrayList<>());
   }

   public final boolean tryAddPlayer(int var1, Player var2, int var3) {
      if (!Config.DOUBLE_SESSIONS_ENABLE) {
         return true;
      } else {
         List var4 = this._protectedList.get(var1);
         if (var4 == null) {
            if (Config.DEVELOPER) {
               _log.info("tryAddPlayer: HwidTemplate null!");
            }

            return false;
         } else {
            String var5 = Config.DOUBLE_SESSIONS_HWIDS ? var2.getHWID() : var2.getIPAddress();
            if (var5 == null) {
               if (Config.DEVELOPER) {
                  _log.info("tryAddPlayer: clientInfo null!");
               }

               return false;
            } else {
               if (!var4.isEmpty()) {
                  for(HwidTemplate var7 : var4) {
                     if (var7 != null && var7.getPlayers().get(var5) != null) {
                        if (var7.getAmount() >= var3) {
                           if (Config.DEVELOPER) {
                              _log.info("tryAddPlayer: limit is exceeded!");
                           }

                           return false;
                        }

                        var7.setAmount(true, var2, var5);
                        if (Config.DEVELOPER) {
                           _log.info("tryAddPlayer: " + var2.getName() + " added!");
                        }

                        return true;
                     }
                  }
               }

               var4.add(new HwidTemplate(1, var2, var5));
               if (Config.DEVELOPER) {
                  _log.info("tryAddPlayer: " + var2.getName() + " added!");
               }

               return true;
            }
         }
      }
   }

   public final boolean removePlayer(int var1, Player var2) {
      if (!Config.DOUBLE_SESSIONS_ENABLE) {
         return true;
      } else {
         List var3 = this._protectedList.get(var1);
         if (var3 == null) {
            if (Config.DEVELOPER) {
               _log.info("removePlayer: hwids null!");
            }

            return false;
         } else {
            if (!var3.isEmpty()) {
               HwidTemplate var4 = null;

               for(HwidTemplate var6 : var3) {
                  if (var6 != null) {
                     var6.setAmount(false, var2, null);
                     if (var6.getAmount() <= 0) {
                        var4 = var6;
                     }
                  }
               }

               if (var4 != null) {
                  var3.remove(var4);
               }
            }

            if (Config.DEVELOPER) {
               _log.info("removePlayer: ok!");
            }

            return true;
         }
      }
   }

   public final void onDisconnect(Player var1) {
      if (Config.DOUBLE_SESSIONS_ENABLE) {
         for(int var3 : this._protectedList.keySet()) {
            this.removePlayer(var3, var1);
         }
      }
   }

   public final void clear(int var1) {
      List var2 = this._protectedList.get(var1);
      if (var2 != null) {
         var2.clear();
      }
   }

   public static final DoubleSessionManager getInstance() {
      return DoubleSessionManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final DoubleSessionManager _instance = new DoubleSessionManager();
   }
}
