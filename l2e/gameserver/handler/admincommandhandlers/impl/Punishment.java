package l2e.gameserver.handler.admincommandhandlers.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import l2e.commons.util.GMAudit;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.PunishmentManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.punishment.PunishmentAffect;
import l2e.gameserver.model.punishment.PunishmentTemplate;
import l2e.gameserver.model.punishment.PunishmentType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Punishment implements IAdminCommandHandler {
   private static final Logger _log = Logger.getLogger(Punishment.class.getName());
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_punishment", "admin_punishment_add", "admin_punishment_remove", "admin_punishment_ban"};
   private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      StringTokenizer st = new StringTokenizer(command, " ");
      if (!st.hasMoreTokens()) {
         return false;
      } else {
         String cmd = st.nextToken();
         switch(cmd) {
            case "admin_punishment":
               if (!st.hasMoreTokens()) {
                  String content = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/admin/punishment.htm");
                  if (content != null) {
                     content = content.replaceAll("%punishments%", Util.implode(PunishmentType.values(), ";"));
                     content = content.replaceAll("%affects%", Util.implode(PunishmentAffect.values(), ";"));
                     activeChar.sendPacket(new NpcHtmlMessage(activeChar, 5, 5, content));
                  } else {
                     _log.log(java.util.logging.Level.WARNING, this.getClass().getSimpleName() + ": data/html/admin/punishment.htm is missing");
                  }
               } else {
                  String subcmd = st.nextToken();
                  switch(subcmd) {
                     case "info":
                        String key = st.hasMoreTokens() ? st.nextToken() : null;
                        String af = st.hasMoreTokens() ? st.nextToken() : null;
                        String name = key;
                        if (key != null && af != null) {
                           PunishmentAffect affect = PunishmentAffect.getByName(af);
                           if (affect == null) {
                              activeChar.sendMessage("Incorrect value specified for affect type!");
                              return true;
                           } else {
                              if (affect == PunishmentAffect.CHARACTER) {
                                 key = findCharId(key);
                              }

                              String content = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/admin/punishment-info.htm");
                              if (content != null) {
                                 StringBuilder sb = new StringBuilder();

                                 for(PunishmentType type : PunishmentType.values()) {
                                    if (PunishmentManager.getInstance().hasPunishment(key, type, affect)) {
                                       PunishmentTemplate tpl = PunishmentManager.getInstance().getPunishmentTemplate(key, type, affect);
                                       if (tpl != null) {
                                          String expire = "never";
                                          if (tpl.getExpirationTime() > 0L) {
                                             synchronized(DATE_FORMATTER) {
                                                expire = DATE_FORMATTER.format(new Date(tpl.getExpirationTime()));
                                             }
                                          }

                                          sb.append(
                                             "<tr><td><font color=\"LEVEL\">"
                                                + type
                                                + "</font></td><td>"
                                                + expire
                                                + "</td><td><a action=\"bypass -h admin_punishment_remove "
                                                + name
                                                + " "
                                                + affect
                                                + " "
                                                + type
                                                + "\">Remove</a></td></tr>"
                                          );
                                       }
                                    }
                                 }

                                 if (name.length() > 10) {
                                    name = name.substring(0, 10) + "...";
                                 }

                                 content = content.replaceAll("%player_name%", name);
                                 content = content.replaceAll("%punishments%", sb.toString());
                                 content = content.replaceAll("%affects%", Util.implode(PunishmentAffect.values(), ";"));
                                 content = content.replaceAll("%affect_type%", affect.name());
                                 activeChar.sendPacket(new NpcHtmlMessage(activeChar, 5, 5, content));
                              } else {
                                 _log.log(
                                    java.util.logging.Level.WARNING, this.getClass().getSimpleName() + ": data/html/admin/punishment-info.htm is missing"
                                 );
                              }

                              return true;
                           }
                        } else {
                           activeChar.sendMessage("Not enough data specified!");
                           return true;
                        }
                     case "player":
                        if (activeChar.getTarget() != null && activeChar.getTarget().isPlayer()) {
                           Player target = activeChar.getTarget().getActingPlayer();
                           String hwidInfo = target.getHWID();
                           if (hwidInfo.length() > 25) {
                              hwidInfo = hwidInfo.substring(0, 25) + "...";
                           }

                           String hwidInfoShort = target.getHWID();
                           if (hwidInfoShort.length() > 15) {
                              hwidInfoShort = hwidInfoShort.substring(0, 15) + "...";
                           }

                           String content = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/admin/punishment-player.htm");
                           if (content != null) {
                              content = content.replaceAll("%player_name%", target.getName());
                              content = content.replaceAll("%punishments%", Util.implode(PunishmentType.values(), ";"));
                              content = content.replaceAll("%acc%", target.getAccountName());
                              content = content.replaceAll("%char%", target.getName());
                              content = content.replaceAll("%ip%", target.getIPAddress());
                              content = content.replaceAll("%hwid%", target.getHWID());
                              content = content.replaceAll("%hwidInfo%", hwidInfo);
                              content = content.replaceAll("%hwidShort%", hwidInfoShort);
                              activeChar.sendPacket(new NpcHtmlMessage(activeChar, 5, 5, content));
                           } else {
                              _log.log(java.util.logging.Level.WARNING, this.getClass().getSimpleName() + ": data/html/admin/punishment-player.htm is missing");
                           }
                        } else {
                           activeChar.sendMessage("You must target player!");
                        }
                  }
               }
               break;
            case "admin_punishment_add":
               String key = st.hasMoreTokens() ? st.nextToken() : null;
               String af = st.hasMoreTokens() ? st.nextToken() : null;
               String t = st.hasMoreTokens() ? st.nextToken() : null;
               String exp = st.hasMoreTokens() ? st.nextToken() : null;
               String reason = st.hasMoreTokens() ? st.nextToken() : null;
               if (reason != null) {
                  while(st.hasMoreTokens()) {
                     reason = reason + " " + st.nextToken();
                  }

                  if (!reason.isEmpty()) {
                     reason = reason.replaceAll("\\$", "\\\\\\$");
                     reason = reason.replaceAll("\r\n", "<br1>");
                     reason = reason.replace("<", "&lt;");
                     reason = reason.replace(">", "&gt;");
                  }
               }

               String name = key;
               if (key == null || af == null || t == null || exp == null || reason == null) {
                  activeChar.sendMessage("Please fill all the fields!");
               } else if (!Util.isDigit(exp) && !exp.equals("-1")) {
                  activeChar.sendMessage("Incorrect value specified for expiration time!");
               } else {
                  long expirationTime = (long)Integer.parseInt(exp);
                  if (expirationTime > 0L) {
                     expirationTime = System.currentTimeMillis() + expirationTime * 60L * 1000L;
                  }

                  PunishmentAffect affect = PunishmentAffect.getByName(af);
                  PunishmentType type = PunishmentType.getByName(t);
                  if (affect != null && type != null) {
                     if (affect == PunishmentAffect.CHARACTER) {
                        key = findCharId(key);
                     } else if (affect == PunishmentAffect.IP) {
                        try {
                           InetAddress addr = InetAddress.getByName(key);
                           if (Config.EXTERNAL_HOSTNAME.equals(addr.getHostAddress())) {
                              throw new UnknownHostException("You cannot ban your gameserver's address!");
                           }
                        } catch (UnknownHostException var25) {
                           activeChar.sendMessage("You've entered an incorrect IP address!");
                           activeChar.sendMessage(var25.getMessage());
                           break;
                        }
                     }

                     if (!PunishmentManager.getInstance().hasPunishment(key, type, affect)) {
                        Player player = null;

                        for(Player pl : World.getInstance().getAllPlayers()) {
                           if (pl != null && pl.isOnline() && pl.getClient() != null) {
                              switch(affect) {
                                 case ACCOUNT:
                                    if (pl.getAccountName().equals(key)) {
                                       player = pl;
                                    }
                                    break;
                                 case CHARACTER:
                                    if (pl.getObjectId() == Integer.parseInt(key)) {
                                       player = pl;
                                    }
                                    break;
                                 case IP:
                                    if (pl.getIPAddress().equals(key)) {
                                       player = pl;
                                    }
                                    break;
                                 case HWID:
                                    if (pl.getHWID().equals(key)) {
                                       player = pl;
                                    }
                              }
                           }
                        }

                        boolean enableTask = player != null && expirationTime > 0L && type != PunishmentType.BAN;
                        PunishmentManager.getInstance()
                           .addPunishment(player, new PunishmentTemplate(key, affect, type, expirationTime, reason, activeChar.getName()), enableTask);
                        activeChar.sendMessage("Punishment " + type.name() + " have been applied to: " + affect + " " + name + "!");
                        GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", cmd, affect.name(), name);
                        return this.useAdminCommand("admin_punishment info " + name + " " + affect.name(), activeChar);
                     }

                     activeChar.sendMessage("Target is already affected by that punishment.");
                  } else {
                     activeChar.sendMessage("Incorrect value specified for affect/punishment type!");
                  }
               }
               break;
            case "admin_punishment_remove":
               String key = st.hasMoreTokens() ? st.nextToken() : null;
               String af = st.hasMoreTokens() ? st.nextToken() : null;
               String t = st.hasMoreTokens() ? st.nextToken() : null;
               String name = key;
               if (key != null && af != null && t != null) {
                  PunishmentAffect affect = PunishmentAffect.getByName(af);
                  PunishmentType type = PunishmentType.getByName(t);
                  if (affect != null && type != null) {
                     if (affect == PunishmentAffect.CHARACTER) {
                        key = findCharId(key);
                     }

                     if (PunishmentManager.getInstance().hasPunishment(key, type, affect)) {
                        Player player = null;

                        for(Player pl : World.getInstance().getAllPlayers()) {
                           if (pl != null && pl.isOnline() && pl.getClient() != null) {
                              switch(affect) {
                                 case ACCOUNT:
                                    if (pl.getAccountName().equals(key)) {
                                       player = pl;
                                    }
                                    break;
                                 case CHARACTER:
                                    if (pl.getObjectId() == Integer.parseInt(key)) {
                                       player = pl;
                                    }
                                    break;
                                 case IP:
                                    if (pl.getIPAddress().equals(key)) {
                                       player = pl;
                                    }
                                    break;
                                 case HWID:
                                    if (pl.getHWID().equals(key)) {
                                       player = pl;
                                    }
                              }
                           }
                        }

                        boolean sucsess = false;
                        if (player != null) {
                           PunishmentManager.getInstance().stopPunishment(player.getClient(), type, affect);
                           sucsess = true;
                        } else if (PunishmentManager.getInstance().clearPunishment(key, type, affect)) {
                           sucsess = true;
                        }

                        if (sucsess) {
                           activeChar.sendMessage("Punishment " + type.name() + " have been stopped to: " + affect + " " + name + "!");
                           GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", cmd, affect.name(), name);
                        }

                        return this.useAdminCommand("admin_punishment info " + name + " " + affect.name(), activeChar);
                     }

                     activeChar.sendMessage("Target is not affected by that punishment!");
                  } else {
                     activeChar.sendMessage("Incorrect value specified for affect/punishment type!");
                  }
               } else {
                  activeChar.sendMessage("Not enough data specified!");
               }
               break;
            case "admin_punishment_ban":
               int time = Integer.parseInt(st.nextToken());
               GameObject targetChar = activeChar.getTarget();
               if (targetChar == null || !(targetChar instanceof Player)) {
                  activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                  return false;
               }

               Player targetPlayer = (Player)targetChar;
               if (targetPlayer != null && targetPlayer.getHWID() != null) {
                  long expirationTime = (long)time;
                  if (expirationTime > 0L) {
                     expirationTime = System.currentTimeMillis() + expirationTime * 60L * 1000L;
                  }

                  boolean enableTask = expirationTime > 0L;
                  PunishmentManager.getInstance()
                     .addPunishment(
                        targetPlayer,
                        new PunishmentTemplate(
                           targetPlayer.getHWID(), PunishmentAffect.HWID, PunishmentType.BAN, expirationTime, "by GM!", activeChar.getName()
                        ),
                        enableTask
                     );
                  activeChar.sendMessage(
                     "Punishment " + PunishmentType.BAN.name() + " have been applied to: " + PunishmentAffect.HWID.name() + " " + targetPlayer.getName() + "!"
                  );
                  GMAudit.auditGMAction(
                     activeChar.getName() + " [" + activeChar.getObjectId() + "]", cmd, PunishmentAffect.HWID.name(), targetPlayer.getHWID()
                  );
               }
         }

         return true;
      }
   }

   private static final String findCharId(String key) {
      int charId = CharNameHolder.getInstance().getIdByName(key);
      return charId > 0 ? Integer.toString(charId) : key;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
