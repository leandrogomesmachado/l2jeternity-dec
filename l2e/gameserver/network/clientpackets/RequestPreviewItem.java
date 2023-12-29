package l2e.gameserver.network.clientpackets;

import java.util.HashMap;
import java.util.Map;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.BuyListParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MerchantInstance;
import l2e.gameserver.model.actor.tasks.player.RemoveWearItemsTask;
import l2e.gameserver.model.actor.templates.items.Armor;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.items.buylist.Product;
import l2e.gameserver.model.items.buylist.ProductList;
import l2e.gameserver.model.items.itemcontainer.Inventory;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.model.items.type.ArmorType;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ShopPreviewInfo;

public final class RequestPreviewItem extends GameClientPacket {
   protected Player _activeChar;
   protected int _unk;
   private int _listId;
   private int _count;
   private int[] _items;

   @Override
   protected void readImpl() {
      this._unk = this.readD();
      this._listId = this.readD();
      this._count = this.readD();
      if (this._count < 0) {
         this._count = 0;
      }

      if (this._count <= 100) {
         this._items = new int[this._count];

         for(int i = 0; i < this._count; ++i) {
            this._items[i] = this.readD();
         }
      }
   }

   @Override
   protected void runImpl() {
      if (this._items != null) {
         this._activeChar = this.getClient().getActiveChar();
         if (this._activeChar != null) {
            if (this._activeChar.isActionsDisabled() || this._activeChar.getActiveTradeList() != null) {
               this._activeChar.sendActionFailed();
            } else if (Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP || this._activeChar.getKarma() <= 0) {
               GameObject target = this._activeChar.getTarget();
               if (this._activeChar.isGM()
                  || target != null && target instanceof MerchantInstance && this._activeChar.isInsideRadius(target, 150, false, false)) {
                  if (this._count >= 1 && this._listId < 4000000) {
                     MerchantInstance merchant = target instanceof MerchantInstance ? (MerchantInstance)target : null;
                     if (merchant == null) {
                        _log.warning(this.getClass().getName() + " Null merchant!");
                     } else {
                        ProductList buyList = BuyListParser.getInstance().getBuyList(this._listId);
                        if (buyList == null) {
                           Util.handleIllegalPlayerAction(
                              this._activeChar,
                              ""
                                 + this._activeChar.getName()
                                 + " of account "
                                 + this._activeChar.getAccountName()
                                 + " sent a false BuyList list_id "
                                 + this._listId
                           );
                        } else {
                           long totalPrice = 0L;
                           Map<Integer, Integer> itemList = new HashMap<>();

                           for(int i = 0; i < this._count; ++i) {
                              int itemId = this._items[i];
                              Product product = buyList.getProductByItemId(itemId);
                              if (product == null) {
                                 Util.handleIllegalPlayerAction(
                                    this._activeChar,
                                    ""
                                       + this._activeChar.getName()
                                       + " of account "
                                       + this._activeChar.getAccountName()
                                       + " sent a false BuyList list_id "
                                       + this._listId
                                       + " and item_id "
                                       + itemId
                                 );
                                 return;
                              }

                              Item template = product.getItem();
                              if (template != null) {
                                 int slot = Inventory.getPaperdollIndex(template.getBodyPart());
                                 if (slot >= 0
                                    && (
                                       template instanceof Weapon
                                          ? this._activeChar.getRace().ordinal() != 5
                                             || template.getItemType() != WeaponType.NONE
                                                && template.getItemType() != WeaponType.RAPIER
                                                && template.getItemType() != WeaponType.CROSSBOW
                                                && template.getItemType() != WeaponType.ANCIENTSWORD
                                          : !(template instanceof Armor)
                                             || this._activeChar.getRace().ordinal() != 5
                                             || template.getItemType() != ArmorType.HEAVY && template.getItemType() != ArmorType.MAGIC
                                    )) {
                                    if (itemList.containsKey(slot)) {
                                       this._activeChar.sendPacket(SystemMessageId.YOU_CAN_NOT_TRY_THOSE_ITEMS_ON_AT_THE_SAME_TIME);
                                       return;
                                    }

                                    itemList.put(slot, itemId);
                                    totalPrice += (long)Config.WEAR_PRICE;
                                    if (totalPrice > PcInventory.MAX_ADENA) {
                                       Util.handleIllegalPlayerAction(
                                          this._activeChar,
                                          ""
                                             + this._activeChar.getName()
                                             + " of account "
                                             + this._activeChar.getAccountName()
                                             + " tried to purchase over "
                                             + PcInventory.MAX_ADENA
                                             + " adena worth of goods."
                                       );
                                       return;
                                    }
                                 }
                              }
                           }

                           if (totalPrice >= 0L && this._activeChar.reduceAdena("Wear", totalPrice, this._activeChar.getLastFolkNPC(), true)) {
                              if (!itemList.isEmpty()) {
                                 this._activeChar.sendPacket(new ShopPreviewInfo(itemList));
                                 ThreadPoolManager.getInstance().schedule(new RemoveWearItemsTask(this._activeChar), (long)(Config.WEAR_DELAY * 1000));
                              }
                           } else {
                              this._activeChar.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
                           }
                        }
                     }
                  } else {
                     this.sendActionFailed();
                  }
               }
            }
         }
      }
   }
}
