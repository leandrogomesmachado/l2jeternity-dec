package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.logging.Level;
import l2e.gameserver.Config;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SortedWareHouseWithdrawalList;
import l2e.gameserver.network.serverpackets.WareHouseDepositList;
import l2e.gameserver.network.serverpackets.WareHouseWithdrawList;

public class PrivateWarehouse implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"withdrawp", "withdrawsortedp", "depositp"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!target.isNpc()) {
         return false;
      } else if (activeChar.isEnchanting()) {
         return false;
      } else {
         try {
            if (command.toLowerCase().startsWith(COMMANDS[0])) {
               if (Config.ENABLE_WAREHOUSESORTING_PRIVATE) {
                  NpcHtmlMessage msg = new NpcHtmlMessage(((Npc)target).getObjectId());
                  msg.setFile(activeChar, activeChar.getLang(), "data/html/mods/WhSortedP.htm");
                  msg.replace("%objectId%", String.valueOf(((Npc)target).getObjectId()));
                  activeChar.sendPacket(msg);
               } else {
                  showWithdrawWindow(activeChar, null, (byte)0);
               }

               return true;
            } else if (command.toLowerCase().startsWith(COMMANDS[1])) {
               String[] param = command.split(" ");
               if (param.length > 2) {
                  showWithdrawWindow(
                     activeChar, SortedWareHouseWithdrawalList.WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.getOrder(param[2])
                  );
               } else if (param.length > 1) {
                  showWithdrawWindow(activeChar, SortedWareHouseWithdrawalList.WarehouseListType.valueOf(param[1]), (byte)1);
               } else {
                  showWithdrawWindow(activeChar, SortedWareHouseWithdrawalList.WarehouseListType.ALL, (byte)1);
               }

               return true;
            } else if (command.toLowerCase().startsWith(COMMANDS[2])) {
               activeChar.sendActionFailed();
               activeChar.setActiveWarehouse(activeChar.getWarehouse());
               activeChar.setInventoryBlockingStatus(true);
               activeChar.sendPacket(new WareHouseDepositList(activeChar, 1));
               return true;
            } else {
               return false;
            }
         } catch (Exception var5) {
            _log.log(Level.WARNING, "Exception in " + this.getClass().getSimpleName(), (Throwable)var5);
            return false;
         }
      }
   }

   private static final void showWithdrawWindow(Player player, SortedWareHouseWithdrawalList.WarehouseListType itemtype, byte sortorder) {
      player.sendActionFailed();
      player.setActiveWarehouse(player.getWarehouse());
      if (player.getActiveWarehouse().getSize() == 0) {
         player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
      } else {
         if (itemtype != null) {
            player.sendPacket(new SortedWareHouseWithdrawalList(player, 1, itemtype, sortorder));
         } else {
            player.sendPacket(new WareHouseWithdrawList(player, 1));
         }
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
