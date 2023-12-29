package l2e.gameserver.handler.communityhandlers.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.type.FunPvpZone;
import l2e.gameserver.network.serverpackets.ShowBoard;

public abstract class AbstractCommunity {
   protected static final Logger _log = Logger.getLogger(AbstractCommunity.class.getName());

   public static void separateAndSend(String html, Player player) {
      if (!player.isInsideZone(ZoneId.PVP) || !Config.BLOCK_COMMUNITY_IN_PVP_ZONE) {
         if (html != null) {
            html = html.replaceAll("\t", "");
            Pattern p = Pattern.compile("%include\\(([^)]+)\\)%");
            Matcher m = p.matcher(html);

            while(m.find()) {
               html = html.replace(m.group(0), HtmCache.getInstance().getHtm(player, player.getLang(), "data/html/community/" + m.group(1)));
            }

            Pattern ps = Pattern.compile("%msg\\(([^)]+)\\)%");
            Matcher ms = ps.matcher(html);

            while(ms.find()) {
               html = html.replace(ms.group(0), ServerStorage.getInstance().getString(player.getLang(), ms.group(1)));
            }

            html = html.replaceAll("\n", "");
            if (html.length() < 8180) {
               player.sendPacket(new ShowBoard(html, "101", player));
               player.sendPacket(new ShowBoard(null, "102", player));
               player.sendPacket(new ShowBoard(null, "103", player));
            } else if (html.length() < 16360) {
               player.sendPacket(new ShowBoard(html.substring(0, 8180), "101", player));
               player.sendPacket(new ShowBoard(html.substring(8180, html.length()), "102", player));
               player.sendPacket(new ShowBoard(null, "103", player));
            } else if (html.length() < 24540) {
               player.sendPacket(new ShowBoard(html.substring(0, 8180), "101", player));
               player.sendPacket(new ShowBoard(html.substring(8180, 16360), "102", player));
               player.sendPacket(new ShowBoard(html.substring(16360, html.length()), "103", player));
            }
         }
      }
   }

   protected void send1001(String html, Player acha) {
      if (html.length() < 8192) {
         acha.sendPacket(new ShowBoard(html, "1001", acha));
      }
   }

   protected void send1002(Player acha) {
      this.send1002(acha, " ", " ", "0");
   }

   protected void send1002(Player activeChar, String string, String string2, String string3) {
      List<String> _arg = new ArrayList<>();
      _arg.add("0");
      _arg.add("0");
      _arg.add("0");
      _arg.add("0");
      _arg.add("0");
      _arg.add("0");
      _arg.add(activeChar.getName());
      _arg.add(Integer.toString(activeChar.getObjectId()));
      _arg.add(activeChar.getAccountName());
      _arg.add("9");
      _arg.add(string2);
      _arg.add(string2);
      _arg.add(string);
      _arg.add(string3);
      _arg.add(string3);
      _arg.add("0");
      _arg.add("0");
      activeChar.sendPacket(new ShowBoard(_arg));
   }

   public boolean sendHtm(Player player, String path) {
      String oriPath = path;
      if (player.getLang() != null && !player.getLang().equalsIgnoreCase("en") && path.contains("html/")) {
         path = path.replace("html/", "html-" + player.getLang() + "/");
      }

      String content = HtmCache.getInstance().getHtm(player, path);
      if (content == null && !oriPath.equals(path)) {
         content = HtmCache.getInstance().getHtm(player, oriPath);
      }

      if (content == null) {
         return false;
      } else {
         separateAndSend(content, player);
         return true;
      }
   }

   protected static boolean checkCondition(Player player, boolean isBuff, boolean isTeleport) {
      if (player == null) {
         return false;
      } else if (player.getUCState() <= 0
         && !player.isBlocked()
         && !player.isCursedWeaponEquipped()
         && !player.isInDuel()
         && !player.isFlying()
         && !player.isJailed()
         && !player.isInOlympiadMode()
         && !player.inObserverMode()
         && !player.isAlikeDead()
         && !player.isDead()) {
         if ((AerialCleftEvent.getInstance().isStarted() || AerialCleftEvent.getInstance().isRewarding())
            && AerialCleftEvent.getInstance().isPlayerParticipant(player.getObjectId())) {
            player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
            return false;
         } else {
            if (player.isInsideZone(ZoneId.PVP) && !player.isInFightEvent()) {
               if (player.isInsideZone(ZoneId.FUN_PVP)) {
                  FunPvpZone zone = ZoneManager.getInstance().getZone(player, FunPvpZone.class);
                  if (zone != null && (isBuff && zone.canUseCbBuffs() || isTeleport && zone.canUseCbTeleports())) {
                     return true;
                  }
               } else if (isBuff && player.isInsideZone(ZoneId.SIEGE) && !Config.ALLOW_COMMUNITY_BUFF_IN_SIEGE
                  || isTeleport && player.isInsideZone(ZoneId.SIEGE) && !Config.ALLOW_COMMUNITY_TELEPORT_IN_SIEGE) {
                  player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
                  return false;
               }
            }

            if (player.isInCombat() || player.isCastingNow() || player.isAttackingNow()) {
               player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
               return false;
            } else if (Config.ALLOW_COMMUNITY_PEACE_ZONE && !player.isInsideZone(ZoneId.PEACE) && !player.isInFightEvent()) {
               player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
               return false;
            } else {
               return true;
            }
         }
      } else {
         player.sendMessage(new ServerMessage("Community.ALL_DISABLE", player.getLang()).toString());
         return false;
      }
   }
}
