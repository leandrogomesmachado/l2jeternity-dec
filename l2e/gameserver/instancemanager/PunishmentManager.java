package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.punishment.PunishmentAffect;
import l2e.gameserver.model.punishment.PunishmentTemplate;
import l2e.gameserver.model.punishment.PunishmentType;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.serverpackets.EtcStatusUpdate;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class PunishmentManager {
   private static final Logger _log = Logger.getLogger(PunishmentManager.class.getName());
   private final Map<PunishmentType, List<PunishmentTemplate>> _tasks = new ConcurrentHashMap<>();

   protected PunishmentManager() {
      this.load();
   }

   private void load() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         long expireTimeSecs = System.currentTimeMillis();

         try (PreparedStatement statement = con.prepareStatement("DELETE FROM punishments WHERE expiration > 0 AND expiration < ?")) {
            statement.setLong(1, expireTimeSecs);
            statement.executeUpdate();
         }
      } catch (SQLException var139) {
         _log.log(Level.WARNING, "Error while clean up punishments!", (Throwable)var139);
      }

      for(PunishmentType type : PunishmentType.values()) {
         this._tasks.put(type, new ArrayList<>());
      }

      int initiated = 0;
      int expired = 0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         Statement st = con.createStatement();
         ResultSet rset = st.executeQuery("SELECT * FROM punishments");
      ) {
         while(rset.next()) {
            int id = rset.getInt("id");
            String key = rset.getString("key");
            PunishmentAffect affect = PunishmentAffect.getByName(rset.getString("affect"));
            PunishmentType type = PunishmentType.getByName(rset.getString("type"));
            long expirationTime = rset.getLong("expiration");
            String reason = rset.getString("reason");
            String punishedBy = rset.getString("punishedBy");
            if (type != null && affect != null) {
               if (expirationTime > 0L && System.currentTimeMillis() > expirationTime) {
                  ++expired;
               } else {
                  ++initiated;
                  this._tasks.get(type).add(new PunishmentTemplate(id, key, affect, type, expirationTime, reason, punishedBy, true));
               }
            }
         }
      } catch (Exception var135) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Error while loading punishments: ", (Throwable)var135);
      }

      _log.log(Level.INFO, this.getClass().getSimpleName() + ": Loaded " + initiated + " active and " + expired + " expired punishments.");
   }

   public void addPunishment(Player player, PunishmentTemplate task, boolean enableTask) {
      this._tasks.get(task.getType()).add(task);
      if (player != null && task != null) {
         switch(task.getType()) {
            case BAN:
               if (player.isSellingBuffs()) {
                  player.unsetVar("offlineBuff");
               }

               if (player.isInOfflineMode()) {
                  player.unsetVar("offline");
                  player.unsetVar("storemode");
               }

               player.logout();
               break;
            case CHAT_BAN:
               long delay = (task.getExpirationTime() - System.currentTimeMillis()) / 1000L;
               if (delay > 0L) {
                  player.sendMessage("You've been chat banned for " + (delay > 60L ? delay / 60L + " minutes." : delay + " seconds."));
               } else {
                  player.sendMessage("You've been chat banned forever.");
               }

               player.sendPacket(new EtcStatusUpdate(player));
               if (enableTask) {
                  player.startPunishmentTask(task);
               }
               break;
            case PARTY_BAN:
               if (enableTask) {
                  player.startPunishmentTask(task);
               }
               break;
            case JAIL:
               player.startJail();
               NpcHtmlMessage msg = new NpcHtmlMessage(0);
               String content = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/jail_in.htm");
               if (content != null) {
                  content = content.replaceAll("%reason%", task != null ? task.getReason() : "");
                  content = content.replaceAll("%punishedBy%", task != null ? task.getPunishedBy() : "");
                  msg.setHtml(player, content);
               } else {
                  msg.setHtml(player, "<html><body>You have been put in jail by an admin.</body></html>");
               }

               player.sendPacket(msg);
               long delayTime = (task.getExpirationTime() - System.currentTimeMillis()) / 1000L;
               if (delayTime > 0L) {
                  player.sendMessage("You've been jailed for " + (delayTime > 60L ? delayTime / 60L + " minutes." : delayTime + " seconds."));
               } else {
                  player.sendMessage("You've been jailed forever.");
               }

               if (enableTask) {
                  player.startPunishmentTask(task);
               }
         }
      }
   }

   public void stopPunishment(GameClient client, PunishmentType type, PunishmentAffect aff) {
      List<PunishmentTemplate> list = this._tasks.get(type);
      if (list != null && !list.isEmpty() && client != null) {
         PunishmentTemplate task = null;

         for(PunishmentTemplate tpl : list) {
            if (tpl != null && tpl.getAffect() == aff) {
               task = tpl;
               break;
            }
         }

         if (task != null) {
            Player player = client.getActiveChar();
            if (player != null) {
               switch(task.getType()) {
                  case BAN:
                  case PARTY_BAN:
                  default:
                     break;
                  case CHAT_BAN:
                     player.sendMessage("Your Chat ban has been lifted");
                     player.sendPacket(new EtcStatusUpdate(player));
                     break;
                  case JAIL:
                     player.stopJail();
                     NpcHtmlMessage msg = new NpcHtmlMessage(0);
                     String content = HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/jail_out.htm");
                     if (content != null) {
                        msg.setHtml(player, content);
                     } else {
                        msg.setHtml(player, "<html><body>You are free for now, respect server rules!</body></html>");
                     }

                     player.sendPacket(msg);
               }
            }

            this.removeDbInfo(task.getId());
            this._tasks.get(type).remove(task);
         }
      }
   }

   public boolean clearPunishment(String key, PunishmentType type, PunishmentAffect aff) {
      List<PunishmentTemplate> list = this._tasks.get(type);
      if (list != null && !list.isEmpty()) {
         PunishmentTemplate template = null;

         for(PunishmentTemplate tpl : list) {
            if (tpl != null && tpl.getAffect() == aff && tpl.getKey().equals(key)) {
               template = tpl;
            }
         }

         if (template != null) {
            this.removeDbInfo(template.getId());
            this._tasks.get(type).remove(template);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void removeDbInfo(int id) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM punishments WHERE id=?");
      ) {
         statement.setInt(1, id);
         statement.execute();
      } catch (Exception var34) {
         _log.log(Level.WARNING, "Could not delete punishment data: " + var34.getMessage(), (Throwable)var34);
      }
   }

   public boolean hasPunishment(String key, PunishmentType type, PunishmentAffect aff) {
      List<PunishmentTemplate> list = this._tasks.get(type);
      if (list != null && !list.isEmpty()) {
         for(PunishmentTemplate tpl : list) {
            if (tpl != null && tpl.getAffect() == aff && tpl.getKey().equals(key)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public PunishmentTemplate getPunishmentTemplate(String key, PunishmentType type, PunishmentAffect aff) {
      List<PunishmentTemplate> list = this._tasks.get(type);
      if (list != null && !list.isEmpty()) {
         for(PunishmentTemplate tpl : list) {
            if (tpl != null && tpl.getAffect() == aff && tpl.getKey().equals(key)) {
               return tpl;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public boolean checkPunishment(GameClient client, PunishmentType type) {
      List<PunishmentTemplate> list = this._tasks.get(type);
      if (list != null && !list.isEmpty() && client != null) {
         boolean found = false;

         for(PunishmentTemplate tpl : list) {
            if (tpl != null) {
               switch(tpl.getAffect()) {
                  case ACCOUNT:
                     if (client.getLogin().equals(tpl.getKey())) {
                        found = true;
                     }
                     break;
                  case CHARACTER:
                     if (client.getActiveChar() != null && client.getActiveChar().getObjectId() == Integer.parseInt(tpl.getKey())) {
                        found = true;
                     }
                     break;
                  case IP:
                     if (client.getIPAddress().equals(tpl.getKey())) {
                        found = true;
                     }
                     break;
                  case HWID:
                     if (client.getHWID().equals(tpl.getKey())) {
                        found = true;
                     }
               }

               if (found && (tpl.getExpirationTime() == 0L || tpl.getExpirationTime() > System.currentTimeMillis())) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean checkPunishment(GameClient client, PunishmentType type, PunishmentAffect aff) {
      List<PunishmentTemplate> list = this._tasks.get(type);
      if (list != null && !list.isEmpty() && client != null) {
         boolean found = false;

         for(PunishmentTemplate tpl : list) {
            if (tpl != null && tpl.getAffect() == aff) {
               switch(tpl.getAffect()) {
                  case ACCOUNT:
                     if (client.getLogin().equals(tpl.getKey())) {
                        found = true;
                     }
                     break;
                  case CHARACTER:
                     if (client.getActiveChar() != null && client.getActiveChar().getObjectId() == Integer.parseInt(tpl.getKey())) {
                        found = true;
                     }
                     break;
                  case IP:
                     if (client.getIPAddress().equals(tpl.getKey())) {
                        found = true;
                     }
                     break;
                  case HWID:
                     if (client.getHWID().equals(tpl.getKey())) {
                        found = true;
                     }
               }

               if (found && (tpl.getExpirationTime() == 0L || tpl.getExpirationTime() > System.currentTimeMillis())) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static final PunishmentManager getInstance() {
      return PunishmentManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final PunishmentManager _instance = new PunishmentManager();
   }
}
