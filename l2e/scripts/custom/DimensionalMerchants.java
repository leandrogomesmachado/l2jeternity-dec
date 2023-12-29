package l2e.scripts.custom;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.handler.itemhandlers.ItemHandler;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExGetPremiumItemList;
import l2e.scripts.ai.AbstractNpcAI;

public final class DimensionalMerchants extends AbstractNpcAI {
   private static final Map<String, Integer> MINION_EXCHANGE = new HashMap<>();

   public DimensionalMerchants(String name, String descr) {
      super(name, descr);
      MINION_EXCHANGE.put("whiteWeasel", 13017);
      MINION_EXCHANGE.put("fairyPrincess", 13018);
      MINION_EXCHANGE.put("wildBeast", 13019);
      MINION_EXCHANGE.put("foxShaman", 13020);
      MINION_EXCHANGE.put("toyKnight", 14061);
      MINION_EXCHANGE.put("spiritShaman", 14062);
      MINION_EXCHANGE.put("turtleAscetic", 14064);
      MINION_EXCHANGE.put("desheloph", 20915);
      MINION_EXCHANGE.put("hyum", 20916);
      MINION_EXCHANGE.put("lekang", 20917);
      MINION_EXCHANGE.put("lilias", 20918);
      MINION_EXCHANGE.put("lapham", 20919);
      MINION_EXCHANGE.put("mafum", 20920);
      this.addStartNpc(32478);
      this.addFirstTalkId(32478);
      this.addTalkId(32478);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = null;
      switch(event) {
         case "32478.htm":
         case "32478-01.htm":
         case "32478-02.htm":
         case "32478-03.htm":
         case "32478-04.htm":
         case "32478-05.htm":
         case "32478-06.htm":
         case "32478-07.htm":
         case "32478-08.htm":
         case "32478-09.htm":
         case "32478-10.htm":
         case "32478-11.htm":
         case "32478-12.htm":
         case "32478-13.htm":
         case "32478-14.htm":
         case "32478-15.htm":
         case "32478-16.htm":
         case "32478-17.htm":
         case "32478-18.htm":
         case "32478-19.htm":
         case "32478-20.htm":
            htmltext = event;
            break;
         case "getDimensonalItem":
            if (player.getPremiumItemList().isEmpty()) {
               player.sendPacket(SystemMessageId.THERE_ARE_NO_MORE_VITAMIN_ITEMS_TO_BE_FOUND);
            } else {
               player.sendPacket(new ExGetPremiumItemList(player));
            }
            break;
         case "whiteWeasel":
         case "fairyPrincess":
         case "wildBeast":
         case "foxShaman":
            htmltext = this.giveMinion(player, event, 13273, 13383);
            break;
         case "toyKnight":
         case "spiritShaman":
         case "turtleAscetic":
            htmltext = this.giveMinion(player, event, 14065, 14074);
            break;
         case "desheloph":
         case "hyum":
         case "lekang":
         case "lilias":
         case "lapham":
         case "mafum":
            htmltext = this.giveMinion(player, event, 20914, 22240);
      }

      return htmltext;
   }

   private String giveMinion(Player player, String event, int couponId, int eventCouponId) {
      if (this.hasAtLeastOneQuestItem(player, new int[]{couponId, eventCouponId})) {
         takeItems(player, hasQuestItems(player, eventCouponId) ? eventCouponId : couponId, 1L);
         int minionId = MINION_EXCHANGE.get(event);
         giveItems(player, minionId, 1L);
         ItemInstance summonItem = player.getInventory().getItemByItemId(minionId);
         IItemHandler handler = ItemHandler.getInstance().getHandler(summonItem.getEtcItem());
         if (handler != null && !player.hasPet()) {
            handler.useItem(player, summonItem, true);
         }

         return "32478-08.htm";
      } else {
         return "32478-07.htm";
      }
   }

   public static void main(String[] args) {
      new DimensionalMerchants(DimensionalMerchants.class.getSimpleName(), "custom");
   }
}
