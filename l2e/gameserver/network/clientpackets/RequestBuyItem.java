package l2e.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.BuyListParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MerchantInstance;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.items.buylist.Product;
import l2e.gameserver.model.items.buylist.ProductList;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExBuySellList;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestBuyItem extends GameClientPacket {
   private static final int BATCH_LENGTH = 12;
   private int _listId;
   private List<ItemHolder> _items = null;

   @Override
   protected void readImpl() {
      this._listId = this.readD();
      int size = this.readD();
      if (size > 0 && size <= Config.MAX_ITEM_IN_PACKET && size * 12 == this._buf.remaining()) {
         this._items = new ArrayList<>(size);

         for(int i = 0; i < size; ++i) {
            int itemId = this.readD();
            long count = this.readQ();
            if (itemId < 1 || count < 1L) {
               this._items = null;
               return;
            }

            this._items.add(new ItemHolder(itemId, count));
         }
      }
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (this._items == null) {
            this.sendActionFailed();
         } else if (player.isActionsDisabled()) {
            this.sendActionFailed();
         } else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0) {
            this.sendActionFailed();
         } else {
            GameObject target = player.getTarget();
            Creature merchant = null;
            if (!player.isGM()) {
               if (target == null || !player.isInsideRadius(target, 150, true, false) || player.getReflectionId() != target.getReflectionId()) {
                  this.sendActionFailed();
                  return;
               }

               if (!(target instanceof MerchantInstance)) {
                  this.sendActionFailed();
                  return;
               }

               merchant = (Creature)target;
            }

            double castleTaxRate = 0.0;
            double baseTaxRate = 0.0;
            if (merchant == null && !player.isGM()) {
               this.sendActionFailed();
            } else {
               ProductList buyList = BuyListParser.getInstance().getBuyList(this._listId);
               if (buyList == null) {
                  Util.handleIllegalPlayerAction(
                     player, "" + player.getName() + " of account " + player.getAccountName() + " sent a false BuyList list_id " + this._listId
                  );
               } else {
                  if (merchant != null) {
                     if (merchant instanceof MerchantInstance) {
                        if (!buyList.isNpcAllowed(((MerchantInstance)merchant).getId())) {
                           this.sendActionFailed();
                           return;
                        }

                        castleTaxRate = ((MerchantInstance)merchant).getMpc().getCastleTaxRate();
                        baseTaxRate = ((MerchantInstance)merchant).getMpc().getBaseTaxRate();
                     } else {
                        baseTaxRate = 50.0;
                     }
                  }

                  long subTotal = 0L;
                  long slots = 0L;
                  long weight = 0L;

                  for(ItemHolder i : this._items) {
                     long price = -1L;
                     Product product = buyList.getProductByItemId(i.getId());
                     if (product == null) {
                        Util.handleIllegalPlayerAction(
                           player,
                           ""
                              + player.getName()
                              + " of account "
                              + player.getAccountName()
                              + " sent a false BuyList list_id "
                              + this._listId
                              + " and item_id "
                              + i.getId()
                        );
                        return;
                     }

                     if (!product.getItem().isStackable() && i.getCount() > 1L) {
                        Util.handleIllegalPlayerAction(
                           player,
                           "" + player.getName() + " of account " + player.getAccountName() + " tried to purchase invalid quantity of items at the same time."
                        );
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
                        this.sendPacket(sm);
                        sm = null;
                        return;
                     }

                     price = product.getPrice();
                     if (product.getId() >= 3960 && product.getId() <= 4026) {
                        price = (long)((float)price * Config.RATE_SIEGE_GUARDS_PRICE);
                     }

                     if (price < 0L) {
                        _log.warning("ERROR, no price found .. wrong buylist ??");
                        this.sendActionFailed();
                        return;
                     }

                     if (price == 0L && !player.isGM() && Config.ONLY_GM_ITEMS_FREE) {
                        player.sendMessage("Ohh Cheat dont work? You have a problem now!");
                        Util.handleIllegalPlayerAction(
                           player, "" + player.getName() + " of account " + player.getAccountName() + " tried buy item for 0 adena."
                        );
                        return;
                     }

                     if (product.hasLimitedStock() && i.getCount() > product.getCount()) {
                        this.sendActionFailed();
                        return;
                     }

                     if (PcInventory.MAX_ADENA / i.getCount() < price) {
                        Util.handleIllegalPlayerAction(
                           player,
                           ""
                              + player.getName()
                              + " of account "
                              + player.getAccountName()
                              + " tried to purchase over "
                              + PcInventory.MAX_ADENA
                              + " adena worth of goods."
                        );
                        return;
                     }

                     price = (long)((double)price * (1.0 + castleTaxRate + baseTaxRate));
                     subTotal += i.getCount() * price;
                     if (subTotal > PcInventory.MAX_ADENA) {
                        Util.handleIllegalPlayerAction(
                           player,
                           ""
                              + player.getName()
                              + " of account "
                              + player.getAccountName()
                              + " tried to purchase over "
                              + PcInventory.MAX_ADENA
                              + " adena worth of goods."
                        );
                        return;
                     }

                     weight += i.getCount() * (long)product.getItem().getWeight();
                     if (player.getInventory().getItemByItemId(product.getId()) == null) {
                        ++slots;
                     }
                  }

                  if (!player.isGM() && (weight > 2147483647L || weight < 0L || !player.getInventory().validateWeight((long)((int)weight)))) {
                     player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
                     this.sendActionFailed();
                  } else if (!player.isGM() && (slots > 2147483647L || slots < 0L || !player.getInventory().validateCapacity((long)((int)slots)))) {
                     player.sendPacket(SystemMessageId.SLOTS_FULL);
                     this.sendActionFailed();
                  } else if (subTotal >= 0L && player.reduceAdena("Buy", subTotal, player.getLastFolkNPC(), false)) {
                     for(ItemHolder i : this._items) {
                        Product product = buyList.getProductByItemId(i.getId());
                        if (product == null) {
                           Util.handleIllegalPlayerAction(
                              player,
                              ""
                                 + player.getName()
                                 + " of account "
                                 + player.getAccountName()
                                 + " sent a false BuyList list_id "
                                 + this._listId
                                 + " and item_id "
                                 + i.getId()
                           );
                        } else if (product.hasLimitedStock()) {
                           if (product.decreaseCount(i.getCount())) {
                              player.getInventory().addItem("Buy", i.getId(), i.getCount(), player, merchant);
                           }
                        } else {
                           player.getInventory().addItem("Buy", i.getId(), i.getCount(), player, merchant);
                        }
                     }

                     if (merchant instanceof MerchantInstance) {
                        ((MerchantInstance)merchant).getCastle().addToTreasury((long)((double)subTotal * castleTaxRate));
                     }

                     StatusUpdate su = new StatusUpdate(player);
                     su.addAttribute(14, player.getCurrentLoad());
                     player.sendPacket(su);
                     player.sendPacket(new ExBuySellList(player, true));
                  } else {
                     player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
                     this.sendActionFailed();
                  }
               }
            }
         }
      }
   }
}
