package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Enchant implements IAdminCommandHandler {
   private static Logger _log = Logger.getLogger(Enchant.class.getName());
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_seteh",
      "admin_setec",
      "admin_seteg",
      "admin_setel",
      "admin_seteb",
      "admin_setew",
      "admin_setes",
      "admin_setle",
      "admin_setre",
      "admin_setlf",
      "admin_setrf",
      "admin_seten",
      "admin_setun",
      "admin_setba",
      "admin_setbe",
      "admin_enchant"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.equals("admin_enchant")) {
         this.showMainPage(activeChar);
      } else {
         int armorType = -1;
         if (command.startsWith("admin_seteh")) {
            armorType = 1;
         } else if (command.startsWith("admin_setec")) {
            armorType = 6;
         } else if (command.startsWith("admin_seteg")) {
            armorType = 10;
         } else if (command.startsWith("admin_seteb")) {
            armorType = 12;
         } else if (command.startsWith("admin_setel")) {
            armorType = 11;
         } else if (command.startsWith("admin_setew")) {
            armorType = 5;
         } else if (command.startsWith("admin_setes")) {
            armorType = 7;
         } else if (command.startsWith("admin_setle")) {
            armorType = 9;
         } else if (command.startsWith("admin_setre")) {
            armorType = 8;
         } else if (command.startsWith("admin_setlf")) {
            armorType = 14;
         } else if (command.startsWith("admin_setrf")) {
            armorType = 13;
         } else if (command.startsWith("admin_seten")) {
            armorType = 4;
         } else if (command.startsWith("admin_setun")) {
            armorType = 0;
         } else if (command.startsWith("admin_setba")) {
            armorType = 23;
         } else if (command.startsWith("admin_setbe")) {
            armorType = 24;
         }

         if (armorType != -1) {
            try {
               int ench = Integer.parseInt(command.substring(12));
               if (ench >= 0 && ench <= 65535) {
                  this.setEnchant(activeChar, ench, armorType);
               } else {
                  activeChar.sendMessage("You must set the enchant level to be between 0-65535.");
               }
            } catch (StringIndexOutOfBoundsException var5) {
               if (Config.DEVELOPER) {
                  _log.warning("Set enchant error: " + var5);
               }

               activeChar.sendMessage("Please specify a new enchant value.");
            } catch (NumberFormatException var6) {
               if (Config.DEVELOPER) {
                  _log.warning("Set enchant error: " + var6);
               }

               activeChar.sendMessage("Please specify a valid new enchant value.");
            }
         }

         this.showMainPage(activeChar);
      }

      return true;
   }

   private void setEnchant(Player activeChar, int ench, int armorType) {
      GameObject target = activeChar.getTarget();
      if (target == null) {
         target = activeChar;
      }

      Player player = null;
      if (target instanceof Player) {
         player = (Player)target;
         int curEnchant = 0;
         ItemInstance itemInstance = null;
         ItemInstance parmorInstance = player.getInventory().getPaperdollItem(armorType);
         if (parmorInstance != null && parmorInstance.getLocationSlot() == armorType) {
            itemInstance = parmorInstance;
         }

         if (itemInstance != null) {
            curEnchant = itemInstance.getEnchantLevel();
            player.getInventory().unEquipItemInSlot(armorType);
            itemInstance.setEnchantLevel(ench);
            player.getInventory().equipItem(itemInstance);
            InventoryUpdate iu = new InventoryUpdate();
            iu.addModifiedItem(itemInstance);
            player.sendPacket(iu);
            player.broadcastCharInfo();
            activeChar.sendMessage(
               "Changed enchantment of "
                  + player.getName()
                  + "'s "
                  + activeChar.getItemName(itemInstance.getItem())
                  + " from "
                  + curEnchant
                  + " to "
                  + ench
                  + "."
            );
            player.sendMessage(
               "Admin has changed the enchantment of your " + player.getItemName(itemInstance.getItem()) + " from " + curEnchant + " to " + ench + "."
            );
         }
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      }
   }

   private void showMainPage(Player activeChar) {
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/enchant.htm");
      activeChar.sendPacket(adminhtm);
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
