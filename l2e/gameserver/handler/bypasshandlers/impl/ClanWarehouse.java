package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.logging.Level;
import l2e.gameserver.Config;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ClanHallManagerInstance;
import l2e.gameserver.model.actor.instance.WarehouseInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SortedWareHouseWithdrawalList;
import l2e.gameserver.network.serverpackets.WareHouseDepositList;
import l2e.gameserver.network.serverpackets.WareHouseWithdrawList;

public class ClanWarehouse implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"withdrawc", "withdrawsortedc", "depositc"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!(target instanceof WarehouseInstance) && !(target instanceof ClanHallManagerInstance)) {
         return false;
      } else if (activeChar.isEnchanting()) {
         return false;
      } else if (activeChar.getClan() == null) {
         activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
         return false;
      } else if (activeChar.getClan().getLevel() == 0) {
         activeChar.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
         return false;
      } else {
         try {
            if (command.toLowerCase().startsWith(COMMANDS[0])) {
               if (Config.ENABLE_WAREHOUSESORTING_CLAN) {
                  NpcHtmlMessage msg = new NpcHtmlMessage(((Npc)target).getObjectId());
                  msg.setFile(activeChar, activeChar.getLang(), "data/html/mods/WhSortedC.htm");
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
               activeChar.setActiveWarehouse(activeChar.getClan().getWarehouse());
               activeChar.setInventoryBlockingStatus(true);
               if (Config.DEBUG) {
                  _log.fine(
                     "Source: L2WarehouseInstance.java; Player: "
                        + activeChar.getName()
                        + "; Command: showDepositWindowClan; Message: Showing items to deposit."
                  );
               }

               activeChar.sendPacket(new WareHouseDepositList(activeChar, 4));
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
      if ((player.getClanPrivileges() & 8) != 8) {
         player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
      } else {
         player.setActiveWarehouse(player.getClan().getWarehouse());
         if (player.getActiveWarehouse().getSize() == 0) {
            player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
         } else {
            if (itemtype != null) {
               player.sendPacket(new SortedWareHouseWithdrawalList(player, 4, itemtype, sortorder));
            } else {
               player.sendPacket(new WareHouseWithdrawList(player, 4));
            }

            if (Config.DEBUG) {
               _log.fine("Source: L2WarehouseInstance.java; Player: " + player.getName() + "; Command: showRetrieveWindowClan; Message: Showing stored items.");
            }
         }
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
