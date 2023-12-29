package l2e.gameserver.model.actor.instance;

import l2e.gameserver.data.parser.BuyListParser;
import l2e.gameserver.data.parser.MerchantPriceParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.buylist.ProductList;
import l2e.gameserver.network.serverpackets.BuyList;
import l2e.gameserver.network.serverpackets.ExBuySellList;

public class MerchantInstance extends NpcInstance {
   private MerchantPriceParser.MerchantPrice _mpc;

   public MerchantInstance(int objectId, NpcTemplate template) {
      super(objectId, template);
      this.setInstanceType(GameObject.InstanceType.MerchantInstance);
   }

   @Override
   public void onSpawn() {
      super.onSpawn();
      this._mpc = MerchantPriceParser.getInstance().getMerchantPrice(this);
   }

   @Override
   public String getHtmlPath(int npcId, int val) {
      String pom = "";
      if (val == 0) {
         pom = "" + npcId;
      } else {
         pom = npcId + "-" + val;
      }

      return "data/html/merchant/" + pom + ".htm";
   }

   public MerchantPriceParser.MerchantPrice getMpc() {
      return this._mpc;
   }

   public final void showBuyWindow(Player player, int val) {
      this.showBuyWindow(player, val, true);
   }

   public final void showBuyWindow(Player player, int val, boolean applyTax) {
      ProductList buyList = BuyListParser.getInstance().getBuyList(val);
      if (buyList == null) {
         _log.warning("BuyList not found! BuyListId:" + val);
         player.sendActionFailed();
      } else if (!buyList.isNpcAllowed(this.getId())) {
         _log.warning("Npc not allowed in BuyList! BuyListId:" + val + " NpcId:" + this.getId());
         player.sendActionFailed();
      } else {
         double taxRate = applyTax ? this.getMpc().getTotalTaxRate() : 0.0;
         player.setInventoryBlockingStatus(true);
         player.sendPacket(new BuyList(buyList, player.getAdena(), taxRate));
         player.sendPacket(new ExBuySellList(player, false));
         player.sendActionFailed();
         if (player.isGM()) {
            player.sendMessage("BuyList: " + val + ".xml Tax: " + taxRate + "%");
         }
      }
   }
}
