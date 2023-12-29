package l2e.commons.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.instancemanager.PunishmentManager;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.punishment.PunishmentAffect;
import l2e.gameserver.model.punishment.PunishmentTemplate;
import l2e.gameserver.model.punishment.PunishmentType;
import l2e.gameserver.network.GameClient;

public final class FloodProtectorAction {
   private static final Logger _log = Logger.getLogger(FloodProtectorAction.class.getName());
   private final GameClient _client;
   private final FloodProtectorConfig _config;
   private volatile long _nextTime = System.currentTimeMillis();
   private final AtomicInteger _count = new AtomicInteger(0);
   private boolean _logged;
   private volatile boolean _punishmentInProgress;

   public FloodProtectorAction(GameClient client, FloodProtectorConfig config) {
      this._client = client;
      this._config = config;
   }

   public boolean tryPerformAction(String command) {
      long curTime = System.currentTimeMillis();
      if (this._client.getActiveChar() != null && this._client.getActiveChar().canOverrideCond(PcCondOverride.FLOOD_CONDITIONS)) {
         return true;
      } else if (curTime >= this._nextTime && !this._punishmentInProgress) {
         if (this._count.get() > 0 && this._config.LOG_FLOODING && _log.isLoggable(Level.WARNING)) {
            this.log(" issued ", String.valueOf(this._count), " extra requests within ~", String.valueOf(this._config.FLOOD_PROTECTION_INTERVAL * 100), " ms");
         }

         this._nextTime = curTime + (long)this._config.FLOOD_PROTECTION_INTERVAL;
         this._logged = false;
         this._count.set(0);
         return true;
      } else {
         if (this._config.LOG_FLOODING && !this._logged && _log.isLoggable(Level.WARNING)) {
            this.log(
               " called command ",
               command,
               " ~",
               String.valueOf((long)this._config.FLOOD_PROTECTION_INTERVAL - (this._nextTime - curTime)),
               " ms after previous command"
            );
            this._logged = true;
         }

         this._count.incrementAndGet();
         if (!this._punishmentInProgress
            && this._config.PUNISHMENT_LIMIT > 0
            && this._count.get() >= this._config.PUNISHMENT_LIMIT
            && this._config.PUNISHMENT_TYPE != null) {
            this._punishmentInProgress = true;
            if ("kick".equals(this._config.PUNISHMENT_TYPE)) {
               this.kickPlayer();
            } else if ("ban".equals(this._config.PUNISHMENT_TYPE)) {
               this.banAccount();
            } else if ("jail".equals(this._config.PUNISHMENT_TYPE)) {
               this.jailChar();
            }

            this._punishmentInProgress = false;
         }

         return false;
      }
   }

   private void kickPlayer() {
      if (this._client.getActiveChar() != null) {
         this._client.getActiveChar().logout(false);
      } else {
         this._client.closeNow();
      }

      if (_log.isLoggable(Level.WARNING)) {
         this.log("kicked for flooding");
      }
   }

   private void banAccount() {
      if (this._client != null && this._client.getActiveChar() != null) {
         PunishmentManager.getInstance()
            .addPunishment(
               this._client.getActiveChar(),
               new PunishmentTemplate(
                  this._client.getLogin(),
                  PunishmentAffect.ACCOUNT,
                  PunishmentType.BAN,
                  System.currentTimeMillis() + this._config.PUNISHMENT_TIME,
                  this.getClass().getSimpleName(),
                  this._client.getActiveChar().getName()
               ),
               true
            );
      }

      if (_log.isLoggable(Level.WARNING)) {
         this.log(" banned for flooding ", this._config.PUNISHMENT_TIME <= 0L ? "forever" : "for " + this._config.PUNISHMENT_TIME + " mins");
      }
   }

   private void jailChar() {
      if (this._client != null && this._client.getActiveChar() != null) {
         int charId = this._client.getActiveChar().getObjectId();
         if (charId > 0) {
            PunishmentManager.getInstance()
               .addPunishment(
                  this._client.getActiveChar(),
                  new PunishmentTemplate(
                     String.valueOf(charId),
                     PunishmentAffect.CHARACTER,
                     PunishmentType.JAIL,
                     System.currentTimeMillis() + this._config.PUNISHMENT_TIME,
                     this.getClass().getSimpleName(),
                     this._client.getActiveChar().getName()
                  ),
                  true
               );
         }

         if (_log.isLoggable(Level.WARNING)) {
            this.log(" jailed for flooding ", this._config.PUNISHMENT_TIME <= 0L ? "forever" : "for " + this._config.PUNISHMENT_TIME + " mins");
         }
      }
   }

   private void log(String... lines) {
      StringBuilder output = StringUtil.startAppend(100, this._config.FLOOD_PROTECTOR_TYPE, ": ");
      String address = null;

      try {
         if (!this._client.isDetached()) {
            address = this._client.getConnection().getSocket().getInetAddress().getHostAddress();
         }
      } catch (Exception var5) {
      }

      switch(this._client.getState()) {
         case IN_GAME:
            if (this._client.getActiveChar() != null) {
               StringUtil.append(output, this._client.getActiveChar().getName());
               StringUtil.append(output, "(", String.valueOf(this._client.getActiveChar().getObjectId()), ") ");
            }
            break;
         case AUTHED:
            if (this._client.getLogin() != null) {
               StringUtil.append(output, this._client.getLogin(), " ");
            }
            break;
         case CONNECTED:
            if (address != null) {
               StringUtil.append(output, address);
            }
            break;
         default:
            throw new IllegalStateException("Missing state on switch");
      }

      StringUtil.append(output, lines);
      _log.warning(output.toString());
   }
}
