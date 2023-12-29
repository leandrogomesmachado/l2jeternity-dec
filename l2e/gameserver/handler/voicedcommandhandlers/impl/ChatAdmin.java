package l2e.gameserver.handler.voicedcommandhandlers.impl;

import java.util.StringTokenizer;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.instancemanager.PunishmentManager;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.punishment.PunishmentAffect;
import l2e.gameserver.model.punishment.PunishmentTemplate;
import l2e.gameserver.model.punishment.PunishmentType;

public class ChatAdmin implements IVoicedCommandHandler {
   private static final String[] VOICED_COMMANDS = new String[]{"banchat", "unbanchat"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String params) {
      if (Config.CHAT_ADMIN && AdminParser.getInstance().hasAccess(command, activeChar.getAccessLevel())) {
         if (command.equals(VOICED_COMMANDS[0])) {
            if (params == null) {
               activeChar.sendMessage("Usage: .banchat name [minutes]");
               return true;
            }

            StringTokenizer st = new StringTokenizer(params);
            if (st.hasMoreTokens()) {
               String name = st.nextToken();
               long expirationTime = 0L;
               if (st.hasMoreTokens()) {
                  String token = st.nextToken();
                  if (Util.isDigit(token)) {
                     expirationTime = System.currentTimeMillis() + (long)(Integer.parseInt(st.nextToken()) * 60 * 1000);
                  }
               }

               int objId = CharNameHolder.getInstance().getIdByName(name);
               if (objId <= 0) {
                  activeChar.sendMessage("Player not found !");
                  return false;
               }

               Player player = World.getInstance().getPlayer(objId);
               if (player == null || !player.isOnline()) {
                  activeChar.sendMessage("Player not online !");
                  return false;
               }

               if (player.isChatBanned()) {
                  activeChar.sendMessage("Player is already punished !");
                  return false;
               }

               if (player == activeChar) {
                  activeChar.sendMessage("You can't ban yourself !");
                  return false;
               }

               if (player.isGM()) {
                  activeChar.sendMessage("You can't ban GM !");
                  return false;
               }

               if (AdminParser.getInstance().hasAccess(command, player.getAccessLevel())) {
                  activeChar.sendMessage("You can't ban moderator !");
                  return false;
               }

               boolean enableTask = player != null && expirationTime > 0L;
               PunishmentManager.getInstance()
                  .addPunishment(
                     player,
                     new PunishmentTemplate(
                        String.valueOf(objId),
                        PunishmentAffect.CHARACTER,
                        PunishmentType.JAIL,
                        expirationTime,
                        "Chat banned by moderator",
                        activeChar.getName()
                     ),
                     enableTask
                  );
               player.sendMessage("Chat banned by moderator " + activeChar.getName());
               if (expirationTime > 0L) {
                  activeChar.sendMessage("Player " + player.getName() + " chat banned for " + expirationTime + " minutes.");
               } else {
                  activeChar.sendMessage("Player " + player.getName() + " chat banned forever.");
               }
            }
         } else if (command.equals(VOICED_COMMANDS[1])) {
            if (params == null) {
               activeChar.sendMessage("Usage: .unbanchat name");
               return true;
            }

            StringTokenizer st = new StringTokenizer(params);
            if (st.hasMoreTokens()) {
               String name = st.nextToken();
               int objId = CharNameHolder.getInstance().getIdByName(name);
               if (objId <= 0) {
                  activeChar.sendMessage("Player not found !");
                  return false;
               }

               Player player = World.getInstance().getPlayer(objId);
               if (player == null || !player.isOnline()) {
                  activeChar.sendMessage("Player not online !");
                  return false;
               }

               if (!player.isChatBanned()) {
                  activeChar.sendMessage("Player is not chat banned !");
                  return false;
               }

               PunishmentManager.getInstance().stopPunishment(player.getClient(), PunishmentType.CHAT_BAN, PunishmentAffect.CHARACTER);
               activeChar.sendMessage("Player " + player.getName() + " chat unbanned.");
               player.sendMessage("Chat unbanned by moderator " + activeChar.getName());
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return VOICED_COMMANDS;
   }
}
