package l2e.gameserver.handler.bypasshandlers.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import l2e.gameserver.Config;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.instancemanager.ItemAuctionManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.itemauction.ItemAuction;
import l2e.gameserver.model.items.itemauction.ItemAuctionInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExItemAuctionInfo;

public class ItemAuctionLink implements IBypassHandler {
   private static final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
   private static final String[] COMMANDS = new String[]{"ItemAuction"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!target.isNpc()) {
         return false;
      } else if (!Config.ALT_ITEM_AUCTION_ENABLED) {
         activeChar.sendPacket(SystemMessageId.NO_AUCTION_PERIOD);
         return true;
      } else {
         ItemAuctionInstance au = ItemAuctionManager.getInstance().getManagerInstance(((Npc)target).getId());
         if (au == null) {
            return false;
         } else {
            try {
               StringTokenizer st = new StringTokenizer(command);
               st.nextToken();
               if (!st.hasMoreTokens()) {
                  return false;
               }

               String cmd = st.nextToken();
               if ("show".equalsIgnoreCase(cmd)) {
                  if (activeChar.isItemAuctionPolling()) {
                     return false;
                  }

                  ItemAuction currentAuction = au.getCurrentAuction();
                  ItemAuction nextAuction = au.getNextAuction();
                  if (currentAuction == null) {
                     activeChar.sendPacket(SystemMessageId.NO_AUCTION_PERIOD);
                     if (nextAuction != null) {
                        activeChar.sendMessage("The next auction will begin on the " + fmt.format(new Date(nextAuction.getStartingTime())) + ".");
                     }

                     return true;
                  }

                  activeChar.sendPacket(new ExItemAuctionInfo(false, currentAuction, nextAuction));
               } else {
                  if (!"cancel".equalsIgnoreCase(cmd)) {
                     return false;
                  }

                  ItemAuction[] auctions = au.getAuctionsByBidder(activeChar.getObjectId());
                  boolean returned = false;

                  for(ItemAuction auction : auctions) {
                     if (auction.cancelBid(activeChar)) {
                        returned = true;
                     }
                  }

                  if (!returned) {
                     activeChar.sendPacket(SystemMessageId.NO_OFFERINGS_OWN_OR_MADE_BID_FOR);
                  }
               }
            } catch (Exception var13) {
               _log.log(Level.WARNING, "Exception in " + this.getClass().getSimpleName(), (Throwable)var13);
            }

            return true;
         }
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
