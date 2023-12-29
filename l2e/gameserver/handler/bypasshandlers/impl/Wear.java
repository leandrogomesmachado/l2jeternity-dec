package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.StringTokenizer;
import java.util.logging.Level;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.BuyListParser;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.buylist.ProductList;
import l2e.gameserver.network.serverpackets.ShopPreviewList;

public class Wear implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"Wear"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!target.isNpc()) {
         return false;
      } else if (!Config.ALLOW_WEAR) {
         return false;
      } else {
         try {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.countTokens() < 1) {
               return false;
            } else {
               showWearWindow(activeChar, Integer.parseInt(st.nextToken()));
               return true;
            }
         } catch (Exception var5) {
            _log.log(Level.WARNING, "Exception in " + this.getClass().getSimpleName(), (Throwable)var5);
            return false;
         }
      }
   }

   private static final void showWearWindow(Player player, int val) {
      ProductList buyList = BuyListParser.getInstance().getBuyList(val);
      if (buyList == null) {
         _log.warning("BuyList not found! BuyListId:" + val);
         player.sendActionFailed();
      } else {
         player.setInventoryBlockingStatus(true);
         player.sendPacket(new ShopPreviewList(buyList, player.getAdena(), player.getExpertiseLevel()));
         if (player.isGM()) {
            player.sendMessage("BuyList: " + val + ".xml ");
         }
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
