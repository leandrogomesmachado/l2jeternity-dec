package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.handler.admincommandhandlers.AdminCommandHandler;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Menu implements IAdminCommandHandler {
   private static final Logger _log = Logger.getLogger(Menu.class.getName());
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_char_manage",
      "admin_teleport_character_to_menu",
      "admin_recall_char_menu",
      "admin_recall_party_menu",
      "admin_recall_clan_menu",
      "admin_goto_char_menu",
      "admin_kick_menu",
      "admin_kill_menu",
      "admin_ban_menu",
      "admin_unban_menu"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.equals("admin_char_manage")) {
         this.showMainPage(activeChar);
      } else if (command.startsWith("admin_teleport_character_to_menu")) {
         String[] data = command.split(" ");
         if (data.length == 5) {
            String playerName = data[1];
            Player player = World.getInstance().getPlayer(playerName);
            if (player != null) {
               this.teleportCharacter(
                  player, Integer.parseInt(data[2]), Integer.parseInt(data[3]), Integer.parseInt(data[4]), activeChar, "Admin is teleporting you."
               );
            }
         }

         this.showMainPage(activeChar);
      } else if (command.startsWith("admin_recall_char_menu")) {
         try {
            String targetName = command.substring(23);
            Player player = World.getInstance().getPlayer(targetName);
            this.teleportCharacter(player, activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar, "Admin is teleporting you.");
         } catch (StringIndexOutOfBoundsException var12) {
         }
      } else if (command.startsWith("admin_recall_party_menu")) {
         int x = activeChar.getX();
         int y = activeChar.getY();
         int z = activeChar.getZ();

         try {
            String targetName = command.substring(24);
            Player player = World.getInstance().getPlayer(targetName);
            if (player == null) {
               activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
               return true;
            }

            if (!player.isInParty()) {
               activeChar.sendMessage("Player is not in party.");
               this.teleportCharacter(player, x, y, z, activeChar, "Admin is teleporting you.");
               return true;
            }

            for(Player pm : player.getParty().getMembers()) {
               this.teleportCharacter(pm, x, y, z, activeChar, "Your party is being teleported by an Admin.");
            }
         } catch (StringIndexOutOfBoundsException var14) {
            activeChar.sendMessage("Usage: //recall_party_menu <char_name>");
         }
      } else if (command.startsWith("admin_recall_clan_menu")) {
         int x = activeChar.getX();
         int y = activeChar.getY();
         int z = activeChar.getZ();

         try {
            String targetName = command.substring(23);
            Player player = World.getInstance().getPlayer(targetName);
            if (player == null) {
               activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
               return true;
            }

            Clan clan = player.getClan();
            if (clan == null) {
               activeChar.sendMessage("Player is not in a clan.");
               this.teleportCharacter(player, x, y, z, activeChar, "Admin is teleporting you.");
               return true;
            }

            for(Player member : clan.getOnlineMembers(0)) {
               this.teleportCharacter(member, x, y, z, activeChar, "Your clan is being teleported by an Admin.");
            }
         } catch (StringIndexOutOfBoundsException var13) {
            activeChar.sendMessage("Usage: //recall_clan_menu <char_name>");
         }
      } else if (command.startsWith("admin_goto_char_menu")) {
         try {
            String targetName = command.substring(21);
            Player player = World.getInstance().getPlayer(targetName);
            activeChar.setReflectionId(player.getReflectionId());
            this.teleportToCharacter(activeChar, player);
         } catch (StringIndexOutOfBoundsException var11) {
         }
      } else if (command.equals("admin_kill_menu")) {
         this.handleKill(activeChar);
      } else if (command.startsWith("admin_kick_menu")) {
         StringTokenizer st = new StringTokenizer(command);
         if (st.countTokens() > 1) {
            st.nextToken();
            String player = st.nextToken();
            Player plyr = World.getInstance().getPlayer(player);
            String text;
            if (plyr != null) {
               if (plyr.isInOfflineMode()) {
                  plyr.unsetVar("offline");
                  plyr.unsetVar("storemode");
               }

               if (plyr.isSellingBuffs()) {
                  plyr.unsetVar("offlineBuff");
               }

               plyr.logout();
               text = "You kicked " + plyr.getName() + " from the game.";
            } else {
               text = "Player " + player + " was not found in the game.";
            }

            activeChar.sendMessage(text);
         }

         this.showMainPage(activeChar);
      } else if (command.startsWith("admin_ban_menu")) {
         StringTokenizer st = new StringTokenizer(command);
         if (st.countTokens() > 1) {
            String subCommand = "admin_ban_char";
            if (!AdminParser.getInstance().hasAccess("admin_ban_char", activeChar.getAccessLevel())) {
               activeChar.sendMessage("You don't have the access right to use this command!");
               _log.warning("Character " + activeChar.getName() + " tryed to use admin command " + "admin_ban_char" + ", but have no access to it!");
               return false;
            }

            IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler("admin_ban_char");
            ach.useAdminCommand("admin_ban_char" + command.substring(14), activeChar);
         }

         this.showMainPage(activeChar);
      } else if (command.startsWith("admin_unban_menu")) {
         StringTokenizer st = new StringTokenizer(command);
         if (st.countTokens() > 1) {
            String subCommand = "admin_unban_char";
            if (!AdminParser.getInstance().hasAccess("admin_unban_char", activeChar.getAccessLevel())) {
               activeChar.sendMessage("You don't have the access right to use this command!");
               _log.warning("Character " + activeChar.getName() + " tryed to use admin command " + "admin_unban_char" + ", but have no access to it!");
               return false;
            }

            IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler("admin_unban_char");
            ach.useAdminCommand("admin_unban_char" + command.substring(16), activeChar);
         }

         this.showMainPage(activeChar);
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void handleKill(Player activeChar) {
      this.handleKill(activeChar, null);
   }

   private void handleKill(Player activeChar, String player) {
      GameObject obj = activeChar.getTarget();
      Creature target = (Creature)obj;
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/main_menu.htm");
      activeChar.sendPacket(adminhtm);
      if (player != null) {
         Player plyr = World.getInstance().getPlayer(player);
         if (plyr != null) {
            target = plyr;
            activeChar.sendMessage("You killed " + plyr.getName());
         }
      }

      if (target != null) {
         if (target instanceof Player) {
            target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1.0, activeChar, null);
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/charmanage.htm");
            activeChar.sendPacket(adminhtm);
         } else {
            target.reduceCurrentHp(target.getMaxHp() + 1.0, activeChar, null);
         }
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      }
   }

   private void teleportCharacter(Player player, int x, int y, int z, Player activeChar, String message) {
      if (player != null) {
         player.sendMessage(message);
         player.teleToLocation(x, y, z, true);
      }

      this.showMainPage(activeChar);
   }

   private void teleportToCharacter(Player activeChar, GameObject target) {
      Player player = null;
      if (target instanceof Player) {
         player = (Player)target;
         if (player.getObjectId() == activeChar.getObjectId()) {
            player.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
         } else {
            activeChar.setReflectionId(player.getReflectionId());
            activeChar.teleToLocation(player.getX(), player.getY(), player.getZ(), true);
            activeChar.sendMessage("You're teleporting yourself to character " + player.getName());
         }

         this.showMainPage(activeChar);
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      }
   }

   private void showMainPage(Player activeChar) {
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/charmanage.htm");
      activeChar.sendPacket(adminhtm);
   }
}
