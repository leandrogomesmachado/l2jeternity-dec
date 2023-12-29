package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;

public class Element implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_setlh", "admin_setlc", "admin_setll", "admin_setlg", "admin_setlb", "admin_setlw", "admin_setls"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      int armorType = -1;
      if (command.startsWith("admin_setlh")) {
         armorType = 1;
      } else if (command.startsWith("admin_setlc")) {
         armorType = 6;
      } else if (command.startsWith("admin_setlg")) {
         armorType = 10;
      } else if (command.startsWith("admin_setlb")) {
         armorType = 12;
      } else if (command.startsWith("admin_setll")) {
         armorType = 11;
      } else if (command.startsWith("admin_setlw")) {
         armorType = 5;
      } else if (command.startsWith("admin_setls")) {
         armorType = 7;
      }

      if (armorType != -1) {
         try {
            String[] args = command.split(" ");
            byte element = Elementals.getElementId(args[1]);
            int value = Integer.parseInt(args[2]);
            if (element < -1 || element > 5 || value < 0 || value > 450) {
               activeChar.sendMessage("Usage: //setlh/setlc/setlg/setlb/setll/setlw/setls <element> <value>[0-450]");
               return false;
            }

            this.setElement(activeChar, element, value, armorType);
         } catch (Exception var7) {
            activeChar.sendMessage("Usage: //setlh/setlc/setlg/setlb/setll/setlw/setls <element>[0-5] <value>[0-450]");
            return false;
         }
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void setElement(Player activeChar, byte type, int value, int armorType) {
      GameObject target = activeChar.getTarget();
      if (target == null) {
         target = activeChar;
      }

      Player player = null;
      if (target instanceof Player) {
         player = (Player)target;
         ItemInstance itemInstance = null;
         ItemInstance parmorInstance = player.getInventory().getPaperdollItem(armorType);
         if (parmorInstance != null && parmorInstance.getLocationSlot() == armorType) {
            itemInstance = parmorInstance;
         }

         if (itemInstance != null) {
            Elementals element = itemInstance.getElemental(type);
            String old;
            if (element == null) {
               old = "None";
            } else {
               old = element.toString();
            }

            player.getInventory().unEquipItemInSlot(armorType);
            if (type == -1) {
               itemInstance.clearElementAttr(type);
            } else {
               itemInstance.setElementAttr(type, value);
            }

            player.getInventory().equipItem(itemInstance);
            String current;
            if (itemInstance.getElementals() == null) {
               current = "None";
            } else {
               current = itemInstance.getElemental(type).toString();
            }

            InventoryUpdate iu = new InventoryUpdate();
            iu.addModifiedItem(itemInstance);
            player.sendPacket(iu);
            activeChar.sendMessage(
               "Changed elemental power of "
                  + player.getName()
                  + "'s "
                  + activeChar.getItemName(itemInstance.getItem())
                  + " from "
                  + old
                  + " to "
                  + current
                  + "."
            );
            if (player != activeChar) {
               player.sendMessage(
                  activeChar.getName()
                     + " has changed the elemental power of your "
                     + player.getItemName(itemInstance.getItem())
                     + " from "
                     + old
                     + " to "
                     + current
                     + "."
               );
            }
         }
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      }
   }
}
