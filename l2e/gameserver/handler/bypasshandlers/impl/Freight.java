package l2e.gameserver.handler.bypasshandlers.impl;

import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.itemcontainer.PcFreight;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.PackageToList;
import l2e.gameserver.network.serverpackets.WareHouseWithdrawList;

public class Freight implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"package_withdraw", "package_deposit"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!target.isNpc()) {
         return false;
      } else {
         if (command.equalsIgnoreCase(COMMANDS[0])) {
            PcFreight freight = activeChar.getFreight();
            if (freight != null) {
               if (freight.getSize() > 0) {
                  activeChar.setActiveWarehouse(freight);
                  activeChar.sendPacket(new WareHouseWithdrawList(activeChar, 1));
               } else {
                  activeChar.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
               }
            }
         } else if (command.equalsIgnoreCase(COMMANDS[1])) {
            if (activeChar.getAccountChars().size() < 1) {
               activeChar.sendPacket(SystemMessageId.CHARACTER_DOES_NOT_EXIST);
            } else {
               activeChar.sendPacket(new PackageToList(activeChar.getAccountChars()));
            }
         }

         return false;
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
