package l2e.gameserver.network.clientpackets;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import l2e.commons.util.GMAudit;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.handler.admincommandhandlers.AdminCommandHandler;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.handler.bypasshandlers.BypassHandler;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.handler.communityhandlers.CommunityBoardHandler;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import l2e.gameserver.listener.events.RequestBypassToServerEvent;
import l2e.gameserver.listener.talk.RequestBypassToServerListener;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Hero;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public final class RequestBypassToServer extends GameClientPacket {
   private static final List<RequestBypassToServerListener> _listeners = new LinkedList<>();
   private String _command;

   @Override
   protected void readImpl() {
      this._command = this.readS();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (this._command.isEmpty()) {
            activeChar.logout();
         } else {
            try {
               if (this._command.startsWith("admin_")) {
                  String command = this._command.split(" ")[0];
                  IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);
                  if (ach == null) {
                     if (activeChar.isGM()) {
                        activeChar.sendMessage("The command " + command.substring(6) + " does not exist!");
                     }

                     _log.warning(activeChar + " requested not registered admin command '" + command + "'");
                     return;
                  }

                  if (!AdminParser.getInstance().hasAccess(command, activeChar.getAccessLevel())) {
                     activeChar.sendMessage("You don't have the access rights to use this command!");
                     _log.warning("Character " + activeChar.getName() + " tried to use admin command " + command + ", without proper access level!");
                     return;
                  }

                  if (AdminParser.getInstance().requireConfirm(command)) {
                     activeChar.setAdminConfirmCmd(this._command);
                     l2e.gameserver.network.serverpackets.ConfirmDlg dlg = new l2e.gameserver.network.serverpackets.ConfirmDlg(SystemMessageId.S1);
                     dlg.addString("Are you sure you want execute command " + this._command.substring(6) + " ?");
                     activeChar.sendPacket(dlg);
                  } else {
                     if (Config.GMAUDIT) {
                        GMAudit.auditGMAction(
                           activeChar.getName() + " [" + activeChar.getObjectId() + "]",
                           this._command,
                           activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target"
                        );
                     }

                     ach.useAdminCommand(this._command, activeChar);
                  }
               } else if (this._command.startsWith(".")) {
                  String command = this._command.substring(1).split(" ")[0];
                  String params = this._command.substring(1).split(" ").length > 1 ? this._command.substring(1).split(" ")[1] : "";
                  IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler(command);
                  if (vch != null) {
                     vch.useVoicedCommand(command, activeChar, params);
                  }
               } else if (this._command.startsWith("voiced_")) {
                  String command = this._command.split(" ")[0];
                  command = command.substring(7);
                  IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler(command);
                  if (vch != null) {
                     vch.useVoicedCommand(this._command.substring(7), activeChar, null);
                  }
               } else if (this._command.equals("come_here") && activeChar.isGM()) {
                  comeHere(activeChar);
               } else if (this._command.startsWith("npc_")) {
                  int endOfId = this._command.indexOf(95, 5);
                  String id;
                  if (endOfId > 0) {
                     id = this._command.substring(4, endOfId);
                  } else {
                     id = this._command.substring(4);
                  }

                  if (Util.isDigit(id)) {
                     GameObject object = World.getInstance().findObject(Integer.parseInt(id));
                     if (object != null && object.isNpc() && endOfId > 0 && activeChar.isInsideRadius(object, 150, false, false)) {
                        ((Npc)object).onBypassFeedback(activeChar, this._command.substring(endOfId + 1));
                     }
                  }

                  activeChar.sendActionFailed();
               } else if (this._command.startsWith("item_")) {
                  int endOfId = this._command.indexOf(95, 5);
                  String id;
                  if (endOfId > 0) {
                     id = this._command.substring(5, endOfId);
                  } else {
                     id = this._command.substring(5);
                  }

                  try {
                     ItemInstance item = activeChar.getInventory().getItemByObjectId(Integer.parseInt(id));
                     if (item != null && endOfId > 0) {
                        item.onBypassFeedback(activeChar, this._command.substring(endOfId + 1));
                     }

                     activeChar.sendActionFailed();
                  } catch (NumberFormatException var8) {
                  }
               } else if (this._command.startsWith("manor_menu_select")) {
                  IBypassHandler manor = BypassHandler.getInstance().getHandler("manor_menu_select");
                  if (manor != null) {
                     manor.useBypass(this._command, activeChar, null);
                  }
               } else if (this._command.startsWith("_bbs")) {
                  if (this._command.equalsIgnoreCase("_bbshome")) {
                     String bypass = !this._command.equalsIgnoreCase(Config.BBS_HOME_PAGE) ? Config.BBS_HOME_PAGE : this._command;
                     ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(bypass);
                     if (handler != null) {
                        handler.onBypassCommand(bypass, activeChar);
                     }
                  } else if (this._command.equalsIgnoreCase("_bbsgetfav")) {
                     String bypass = !this._command.equalsIgnoreCase(Config.BBS_FAVORITE_PAGE) ? Config.BBS_FAVORITE_PAGE : this._command;
                     ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(bypass);
                     if (handler != null) {
                        handler.onBypassCommand(bypass, activeChar);
                     }
                  } else if (this._command.equalsIgnoreCase("_bbslink")) {
                     String bypass = !this._command.equalsIgnoreCase(Config.BBS_LINK_PAGE) ? Config.BBS_LINK_PAGE : this._command;
                     ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(bypass);
                     if (handler != null) {
                        handler.onBypassCommand(bypass, activeChar);
                     }
                  } else if (this._command.equalsIgnoreCase("_bbsloc")) {
                     String bypass = !this._command.equalsIgnoreCase(Config.BBS_REGION_PAGE) ? Config.BBS_REGION_PAGE : this._command;
                     ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(bypass);
                     if (handler != null) {
                        handler.onBypassCommand(bypass, activeChar);
                     }
                  } else if (this._command.equalsIgnoreCase("_bbsclan")) {
                     String bypass = !this._command.equalsIgnoreCase(Config.BBS_CLAN_PAGE) ? Config.BBS_CLAN_PAGE : this._command;
                     ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(bypass);
                     if (handler != null) {
                        handler.onBypassCommand(bypass, activeChar);
                     }
                  } else if (this._command.equalsIgnoreCase("_bbsmemo")) {
                     String bypass = !this._command.equalsIgnoreCase(Config.BBS_MEMO_PAGE) ? Config.BBS_MEMO_PAGE : this._command;
                     ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(bypass);
                     if (handler != null) {
                        handler.onBypassCommand(bypass, activeChar);
                     }
                  } else if (this._command.equalsIgnoreCase("_bbsaddfav")) {
                     String bypass = !this._command.equalsIgnoreCase(Config.BBS_ADDFAV_PAGE) ? Config.BBS_ADDFAV_PAGE : this._command;
                     ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(bypass);
                     if (handler != null) {
                        handler.onBypassCommand(bypass, activeChar);
                     }
                  } else {
                     ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(this._command);
                     if (handler != null) {
                        handler.onBypassCommand(this._command, activeChar);
                     }
                  }
               } else if (this._command.startsWith("bbs")) {
                  ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(this._command);
                  if (handler != null) {
                     handler.onBypassCommand(this._command, activeChar);
                  }
               } else if (this._command.startsWith("_friendlist_0_")) {
                  if (this._command.equalsIgnoreCase("_friendlist_0_")) {
                     String bypass = !this._command.equalsIgnoreCase(Config.BBS_FRIENDS_PAGE) ? Config.BBS_FRIENDS_PAGE : this._command;
                     ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(bypass);
                     if (handler != null) {
                        handler.onBypassCommand(bypass, activeChar);
                     }
                  }
               } else if (this._command.startsWith("_maillist_0_1_0_")) {
                  if (this._command.equalsIgnoreCase("_maillist_0_1_0_")) {
                     String bypass = !this._command.equalsIgnoreCase(Config.BBS_MAIL_PAGE) ? Config.BBS_MAIL_PAGE : this._command;
                     ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(bypass);
                     if (handler != null) {
                        handler.onBypassCommand(bypass, activeChar);
                     }
                  }
               } else if (this._command.startsWith("Quest ")) {
                  String p = this._command.substring(6).trim();
                  int idx = p.indexOf(32);
                  if (idx < 0) {
                     activeChar.processQuestEvent(p, "");
                  } else {
                     activeChar.processQuestEvent(p.substring(0, idx), p.substring(idx).trim());
                  }
               } else if (this._command.startsWith("_match")) {
                  String params = this._command.substring(this._command.indexOf("?") + 1);
                  StringTokenizer st = new StringTokenizer(params, "&");
                  int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
                  int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
                  int heroid = Hero.getInstance().getHeroByClass(heroclass);
                  if (heroid > 0) {
                     Hero.getInstance().showHeroFights(activeChar, heroclass, heroid, heropage);
                  }
               } else if (this._command.startsWith("_diary")) {
                  String params = this._command.substring(this._command.indexOf("?") + 1);
                  StringTokenizer st = new StringTokenizer(params, "&");
                  int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
                  int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
                  int heroid = Hero.getInstance().getHeroByClass(heroclass);
                  if (heroid > 0) {
                     Hero.getInstance().showHeroDiary(activeChar, heroclass, heroid, heropage);
                  }
               } else if (this._command.startsWith("_olympiad?command")) {
                  int arenaId = Integer.parseInt(this._command.split("=")[2]);
                  IBypassHandler handler = BypassHandler.getInstance().getHandler("arenachange");
                  if (handler != null) {
                     handler.useBypass("arenachange " + (arenaId - 1), activeChar, null);
                  }
               } else {
                  IBypassHandler handler = BypassHandler.getInstance().getHandler(this._command);
                  if (handler != null) {
                     handler.useBypass(this._command, activeChar, null);
                  }
               }
            } catch (Exception var9) {
               if (activeChar.isGM()) {
                  _log.log(Level.WARNING, this.getClient() + " sent bad RequestBypassToServer: \"" + this._command + "\"", (Throwable)var9);
                  StringBuilder sb = new StringBuilder(200);
                  sb.append("<html><body>");
                  sb.append("Bypass error: " + var9 + "<br1>");
                  sb.append("Bypass command: " + this._command + "<br1>");
                  sb.append("StackTrace:<br1>");

                  for(StackTraceElement ste : var9.getStackTrace()) {
                     sb.append(ste.toString() + "<br1>");
                  }

                  sb.append("</body></html>");
                  NpcHtmlMessage msg = new NpcHtmlMessage(0, 12807);
                  msg.setHtml(activeChar, sb.toString());
                  activeChar.sendPacket(msg);
               }
            }

            this.fireBypassListeners();
         }
      }
   }

   private static void comeHere(Player activeChar) {
      GameObject obj = activeChar.getTarget();
      if (obj != null) {
         if (obj instanceof Npc) {
            Npc temp = (Npc)obj;
            temp.setTarget(activeChar);
            temp.getAI().setIntention(CtrlIntention.MOVING, activeChar.getLocation());
         }
      }
   }

   private void fireBypassListeners() {
      RequestBypassToServerEvent event = new RequestBypassToServerEvent();
      event.setActiveChar(this.getActiveChar());
      event.setCommand(this._command);

      for(RequestBypassToServerListener listener : _listeners) {
         listener.onRequestBypassToServer(event);
      }
   }

   public static void addBypassListener(RequestBypassToServerListener listener) {
      if (!_listeners.contains(listener)) {
         _listeners.add(listener);
      }
   }

   public static void removeBypassListener(RequestBypassToServerListener listener) {
      if (_listeners.contains(listener)) {
         _listeners.remove(listener);
      }
   }
}
