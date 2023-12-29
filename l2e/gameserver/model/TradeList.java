package l2e.gameserver.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import l2e.commons.math.SafeMath;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.entity.auction.AuctionsManager;
import l2e.gameserver.model.items.ItemRequest;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class TradeList {
   private static final Logger _log = Logger.getLogger(TradeList.class.getName());
   private final Player _owner;
   private Player _partner;
   private final List<TradeItem> _items = new CopyOnWriteArrayList<>();
   private String _title;
   private boolean _packaged;
   private boolean _confirmed = false;
   private boolean _locked = false;

   public TradeList(Player owner) {
      this._owner = owner;
   }

   public Player getOwner() {
      return this._owner;
   }

   public void setPartner(Player partner) {
      this._partner = partner;
   }

   public Player getPartner() {
      return this._partner;
   }

   public void setTitle(String title) {
      this._title = title;
   }

   public String getTitle() {
      return this._title;
   }

   public boolean isLocked() {
      return this._locked;
   }

   public boolean isConfirmed() {
      return this._confirmed;
   }

   public boolean isPackaged() {
      return this._packaged;
   }

   public void setPackaged(boolean value) {
      this._packaged = value;
   }

   public TradeItem[] getItems() {
      return this._items.toArray(new TradeItem[this._items.size()]);
   }

   public List<TradeItem> getAvailableItems(PcInventory inventory) {
      List<TradeItem> list = new LinkedList<>();

      for(TradeItem item : this._items) {
         item = new TradeItem(item, item.getCount(), item.getPrice());
         inventory.adjustAvailableItem(item);
         list.add(item);
      }

      return list;
   }

   public int getItemCount() {
      return this._items.size();
   }

   public TradeItem adjustAvailableItem(ItemInstance item) {
      if (item.isStackable()) {
         for(TradeItem exclItem : this._items) {
            if (exclItem.getItem().getId() == item.getId()) {
               if (item.getCount() <= exclItem.getCount()) {
                  return null;
               }

               return new TradeItem(item, item.getCount() - exclItem.getCount(), (long)item.getReferencePrice());
            }
         }
      }

      return new TradeItem(item, item.getCount(), (long)item.getReferencePrice());
   }

   public void adjustItemRequest(ItemRequest item) {
      for(TradeItem filtItem : this._items) {
         if (filtItem.getObjectId() == item.getObjectId()) {
            if (filtItem.getCount() < item.getCount()) {
               item.setCount(filtItem.getCount());
            }

            return;
         }
      }

      item.setCount(0L);
   }

   public TradeItem addItem(int objectId, long count) {
      return this.addItem(objectId, count, 0L);
   }

   public synchronized TradeItem addItem(int objectId, long count, long price) {
      if (this.isLocked()) {
         _log.warning(this._owner.getName() + ": Attempt to modify locked TradeList!");
         return null;
      } else {
         GameObject o = World.getInstance().findObject(objectId);
         if (!(o instanceof ItemInstance)) {
            _log.warning(this._owner.getName() + ": Trying to add something other than an item!");
            return null;
         } else {
            ItemInstance item = (ItemInstance)o;
            if ((item.isTradeable() || this.getOwner().isGM() && Config.GM_TRADE_RESTRICTED_ITEMS) && !item.isQuestItem()) {
               if (!this.getOwner().getInventory().canManipulateWithItemId(item.getId())) {
                  _log.warning(this._owner.getName() + ": Attempt to add an item that can't manipualte!");
                  return null;
               } else if (count <= 0L || count > item.getCount()) {
                  _log.warning(this._owner.getName() + ": Attempt to add an item with invalid item count!");
                  return null;
               } else if (!item.isStackable() && count > 1L) {
                  _log.warning(this._owner.getName() + ": Attempt to add non-stackable item to TradeList with count > 1!");
                  return null;
               } else if (PcInventory.MAX_ADENA / count < price) {
                  _log.warning(this._owner.getName() + ": Attempt to overflow adena !");
                  return null;
               } else {
                  long amount = Math.min(count, item.getCount());

                  try {
                     for(TradeItem ti : this._items) {
                        if (ti.getObjectId() == objectId) {
                           amount = SafeMath.addAndCheck(amount, ti.getCount());
                           amount = Math.min(amount, item.getCount());
                           ti.setCount(amount);
                           return ti;
                        }
                     }
                  } catch (ArithmeticException var12) {
                     return null;
                  }

                  TradeItem titem = new TradeItem(item, amount, price);
                  this._items.add(titem);
                  this.invalidateConfirmation();
                  return titem;
               }
            } else {
               _log.warning(this._owner.getName() + ": Attempt to add a restricted item!");
               return null;
            }
         }
      }
   }

   public synchronized TradeItem addItemByItemId(int itemId, int enchant, long count, long price, int elemAtkType, int elemAtkPower, int[] elemDefAttr) {
      if (this.isLocked()) {
         _log.warning(this._owner.getName() + ": Attempt to modify locked TradeList!");
         return null;
      } else {
         Item item = ItemsParser.getInstance().getTemplate(itemId);
         if (item == null) {
            _log.warning(this._owner.getName() + ": Attempt to add invalid item to TradeList!");
            return null;
         } else if (!item.isTradeable() || item.isQuestItem()) {
            return null;
         } else if (!item.isStackable() && count > 1L) {
            _log.warning(this._owner.getName() + ": Attempt to add non-stackable item to TradeList with count > 1!");
            return null;
         } else if (PcInventory.MAX_ADENA / count < price) {
            _log.warning(this._owner.getName() + ": Attempt to overflow adena !");
            return null;
         } else {
            if (elemAtkType == 65534) {
               if (item.isWeapon()) {
                  elemAtkType = -1;
               } else {
                  elemAtkType = -2;
               }
            }

            TradeItem titem = new TradeItem(item, enchant, count, price, elemAtkType, elemAtkPower, elemDefAttr);
            this._items.add(titem);
            this.invalidateConfirmation();
            return titem;
         }
      }
   }

   public synchronized TradeItem removeItem(int objectId, int itemId, long count) {
      if (this.isLocked()) {
         _log.warning(this._owner.getName() + ": Attempt to modify locked TradeList!");
         return null;
      } else {
         for(TradeItem titem : this._items) {
            if (titem.getObjectId() == objectId || titem.getItem().getId() == itemId) {
               if (this._partner != null) {
                  TradeList partnerList = this._partner.getActiveTradeList();
                  if (partnerList == null) {
                     _log.warning(this._partner.getName() + ": Trading partner (" + this._partner.getName() + ") is invalid in this trade!");
                     return null;
                  }

                  partnerList.invalidateConfirmation();
               }

               if (count != -1L && titem.getCount() > count) {
                  int curCount = (int)(titem.getCount() - count);
                  titem.setCount(titem.getCount() - count);
                  AuctionsManager.getInstance().setNewCount(titem.getAuctionId(), (long)curCount);
               } else {
                  this._items.remove(titem);
                  AuctionsManager.getInstance().removeStore(this._owner, titem.getAuctionId());
               }

               return titem;
            }
         }

         return null;
      }
   }

   public synchronized void updateItems() {
      for(TradeItem titem : this._items) {
         ItemInstance item = this._owner.getInventory().getItemByObjectId(titem.getObjectId());
         if (item == null || titem.getCount() < 1L) {
            this.removeItem(titem.getObjectId(), -1, -1L);
         } else if (item.getCount() < titem.getCount()) {
            titem.setCount(item.getCount());
         }
      }
   }

   public void lock() {
      this._locked = true;
   }

   public synchronized void clear() {
      this._items.clear();
      this._locked = false;
   }

   public boolean confirm() {
      if (this._confirmed) {
         return true;
      } else if (this._partner != null) {
         TradeList partnerList = this._partner.getActiveTradeList();
         if (partnerList == null) {
            _log.warning(this._partner.getName() + ": Trading partner (" + this._partner.getName() + ") is invalid in this trade!");
            return false;
         } else {
            TradeList sync1;
            TradeList sync2;
            if (this.getOwner().getObjectId() > partnerList.getOwner().getObjectId()) {
               sync1 = partnerList;
               sync2 = this;
            } else {
               sync1 = this;
               sync2 = partnerList;
            }

            synchronized(sync1) {
               boolean var10000;
               synchronized(sync2) {
                  this._confirmed = true;
                  if (partnerList.isConfirmed()) {
                     partnerList.lock();
                     this.lock();
                     if (!partnerList.validate()) {
                        return false;
                     }

                     if (!this.validate()) {
                        var10000 = false;
                     } else {
                        this.doExchange(partnerList);
                        return this._confirmed;
                     }
                  } else {
                     this._partner.onTradeConfirm(this._owner);
                     return this._confirmed;
                  }
               }

               return var10000;
            }
         }
      } else {
         this._confirmed = true;
         return this._confirmed;
      }
   }

   public void invalidateConfirmation() {
      this._confirmed = false;
   }

   private boolean validate() {
      if (this._owner != null && World.getInstance().getPlayer(this._owner.getObjectId()) != null) {
         for(TradeItem titem : this._items) {
            ItemInstance item = this._owner.checkItemManipulation(titem.getObjectId(), titem.getCount(), "transfer");
            if (item == null || item.getCount() < 1L) {
               _log.warning(this._owner.getName() + ": Invalid Item in TradeList");
               return false;
            }
         }

         return true;
      } else {
         _log.warning("Invalid owner of TradeList");
         return false;
      }
   }

   private boolean TransferItems(Player partner, InventoryUpdate ownerIU, InventoryUpdate partnerIU) {
      for(TradeItem titem : this._items) {
         ItemInstance oldItem = this._owner.getInventory().getItemByObjectId(titem.getObjectId());
         if (oldItem == null) {
            return false;
         }

         ItemInstance newItem = this._owner
            .getInventory()
            .transferItem("Trade", titem.getObjectId(), titem.getCount(), partner.getInventory(), this._owner, this._partner);
         if (newItem == null) {
            return false;
         }

         if (ownerIU != null) {
            if (oldItem.getCount() > 0L && oldItem != newItem) {
               ownerIU.addModifiedItem(oldItem);
            } else {
               ownerIU.addRemovedItem(oldItem);
            }
         }

         if (partnerIU != null) {
            if (newItem.getCount() > titem.getCount()) {
               partnerIU.addModifiedItem(newItem);
            } else {
               partnerIU.addNewItem(newItem);
            }
         }
      }

      return true;
   }

   public int countItemsSlots(Player partner) {
      int slots = 0;

      for(TradeItem item : this._items) {
         if (item != null) {
            Item template = ItemsParser.getInstance().getTemplate(item.getItem().getId());
            if (template != null) {
               if (!template.isStackable()) {
                  slots = (int)((long)slots + item.getCount());
               } else if (partner.getInventory().getItemByItemId(item.getItem().getId()) == null) {
                  ++slots;
               }
            }
         }
      }

      return slots;
   }

   public int calcItemsWeight() {
      long weight = 0L;

      for(TradeItem item : this._items) {
         if (item != null) {
            Item template = ItemsParser.getInstance().getTemplate(item.getItem().getId());
            if (template != null) {
               weight += item.getCount() * (long)template.getWeight();
            }
         }
      }

      return (int)Math.min(weight, 2147483647L);
   }

   private void doExchange(TradeList partnerList) {
      boolean success = false;
      if (!this.getOwner().getInventory().validateWeight((long)partnerList.calcItemsWeight())
         || !partnerList.getOwner().getInventory().validateWeight((long)this.calcItemsWeight())) {
         partnerList.getOwner().sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
         this.getOwner().sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
      } else if (this.getOwner().getInventory().validateCapacity((long)partnerList.countItemsSlots(this.getOwner()))
         && partnerList.getOwner().getInventory().validateCapacity((long)this.countItemsSlots(partnerList.getOwner()))) {
         InventoryUpdate ownerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
         InventoryUpdate partnerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
         partnerList.TransferItems(this.getOwner(), partnerIU, ownerIU);
         this.TransferItems(partnerList.getOwner(), ownerIU, partnerIU);
         if (ownerIU != null) {
            this._owner.sendPacket(ownerIU);
         } else {
            this._owner.sendItemList(false);
         }

         if (partnerIU != null) {
            this._partner.sendPacket(partnerIU);
         } else {
            this._partner.sendItemList(false);
         }

         StatusUpdate playerSU = new StatusUpdate(this._owner);
         playerSU.addAttribute(14, this._owner.getCurrentLoad());
         this._owner.sendPacket(playerSU);
         playerSU = new StatusUpdate(this._partner);
         playerSU.addAttribute(14, this._partner.getCurrentLoad());
         this._partner.sendPacket(playerSU);
         success = true;
      } else {
         partnerList.getOwner().sendPacket(SystemMessageId.SLOTS_FULL);
         this.getOwner().sendPacket(SystemMessageId.SLOTS_FULL);
      }

      partnerList.getOwner().onTradeFinish(success);
      this.getOwner().onTradeFinish(success);
   }

   public synchronized int privateStoreBuy(Player player, Set<ItemRequest> items) {
      if (this._locked) {
         return 1;
      } else if (!this.validate()) {
         this.lock();
         return 1;
      } else if (this._owner.isOnline() && player.isOnline()) {
         int slots = 0;
         int weight = 0;
         long totalPrice = 0L;
         PcInventory ownerInventory = this._owner.getInventory();
         PcInventory playerInventory = player.getInventory();

         for(ItemRequest item : items) {
            boolean found = false;

            for(TradeItem ti : this._items) {
               if (ti.getObjectId() == item.getObjectId()) {
                  if (ti.getPrice() == item.getPrice()) {
                     if (ti.getCount() < item.getCount()) {
                        item.setCount(ti.getCount());
                     }

                     found = true;
                  }
                  break;
               }
            }

            if (found) {
               if (PcInventory.MAX_ADENA / item.getCount() < item.getPrice()) {
                  this.lock();
                  return 1;
               }

               totalPrice += item.getCount() * item.getPrice();
               if (PcInventory.MAX_ADENA >= totalPrice && totalPrice >= 0L) {
                  ItemInstance oldItem = this._owner.checkItemManipulation(item.getObjectId(), item.getCount(), "sell");
                  if (oldItem != null && oldItem.isTradeable()) {
                     Item template = ItemsParser.getInstance().getTemplate(item.getId());
                     if (template != null) {
                        weight = (int)((long)weight + item.getCount() * (long)template.getWeight());
                        if (!template.isStackable()) {
                           slots = (int)((long)slots + item.getCount());
                        } else if (playerInventory.getItemByItemId(item.getId()) == null) {
                           ++slots;
                        }
                     }
                     continue;
                  }

                  this.lock();
                  return 2;
               }

               this.lock();
               return 1;
            } else {
               if (this.isPackaged()) {
                  Util.handleIllegalPlayerAction(player, "" + player.getName() + " tried to cheat the package sell and buy only a part of the package!");
                  return 2;
               }

               item.setCount(0L);
            }
         }

         if (totalPrice > playerInventory.getAdena()) {
            player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
            return 1;
         } else if (!playerInventory.validateWeight((long)weight)) {
            player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
            return 1;
         } else if (!playerInventory.validateCapacity((long)slots)) {
            player.sendPacket(SystemMessageId.SLOTS_FULL);
            return 1;
         } else {
            InventoryUpdate ownerIU = new InventoryUpdate();
            InventoryUpdate playerIU = new InventoryUpdate();
            ItemInstance adenaItem = playerInventory.getAdenaInstance();
            if (!playerInventory.reduceAdena("PrivateStore", totalPrice, player, this._owner)) {
               player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
               return 1;
            } else {
               playerIU.addItem(adenaItem);
               ownerInventory.addAdena("PrivateStore", totalPrice, this._owner, player);
               boolean ok = true;

               for(ItemRequest item : items) {
                  if (item.getCount() != 0L) {
                     ItemInstance oldItem = this._owner.checkItemManipulation(item.getObjectId(), item.getCount(), "sell");
                     if (oldItem == null) {
                        this.lock();
                        ok = false;
                        break;
                     }

                     ItemInstance newItem = ownerInventory.transferItem(
                        "PrivateStore", item.getObjectId(), item.getCount(), playerInventory, this._owner, player
                     );
                     if (newItem == null) {
                        ok = false;
                        break;
                     }

                     this.removeItem(item.getObjectId(), -1, item.getCount());
                     if (oldItem.getCount() > 0L && oldItem != newItem) {
                        ownerIU.addModifiedItem(oldItem);
                     } else {
                        ownerIU.addRemovedItem(oldItem);
                     }

                     if (newItem.getCount() > item.getCount()) {
                        playerIU.addModifiedItem(newItem);
                     } else {
                        playerIU.addNewItem(newItem);
                     }

                     if (newItem.isStackable()) {
                        SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_PURCHASED_S3_S2_S);
                        msg.addString(player.getName());
                        msg.addItemName(newItem);
                        msg.addItemNumber(item.getCount());
                        this._owner.sendPacket(msg);
                        msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S3_S2_S_FROM_C1);
                        msg.addString(this._owner.getName());
                        msg.addItemName(newItem);
                        msg.addItemNumber(item.getCount());
                        player.sendPacket(msg);
                     } else {
                        SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_PURCHASED_S2);
                        msg.addString(player.getName());
                        msg.addItemName(newItem);
                        this._owner.sendPacket(msg);
                        msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S2_FROM_C1);
                        msg.addString(this._owner.getName());
                        msg.addItemName(newItem);
                        player.sendPacket(msg);
                     }
                  }
               }

               this._owner.sendPacket(ownerIU);
               this._owner.saveTradeList();
               player.sendPacket(playerIU);
               return ok ? 0 : 2;
            }
         }
      } else {
         return 1;
      }
   }

   public synchronized boolean privateStoreSell(Player player, ItemRequest[] items) {
      if (this._locked) {
         return false;
      } else if (this._owner.isOnline() && player.isOnline()) {
         boolean ok = false;
         PcInventory ownerInventory = this._owner.getInventory();
         PcInventory playerInventory = player.getInventory();
         InventoryUpdate ownerIU = new InventoryUpdate();
         InventoryUpdate playerIU = new InventoryUpdate();
         long totalPrice = 0L;

         for(ItemRequest item : items) {
            boolean found = false;

            for(TradeItem ti : this._items) {
               if (ti.getItem().getId() == item.getId()) {
                  if (ti.getPrice() == item.getPrice()) {
                     if (ti.getCount() < item.getCount()) {
                        item.setCount(ti.getCount());
                     }

                     found = item.getCount() > 0L;
                  }
                  break;
               }
            }

            if (found) {
               if (PcInventory.MAX_ADENA / item.getCount() < item.getPrice()) {
                  this.lock();
                  break;
               }

               long _totalPrice = totalPrice + item.getCount() * item.getPrice();
               if (PcInventory.MAX_ADENA < _totalPrice || _totalPrice < 0L) {
                  this.lock();
                  break;
               }

               if (ownerInventory.getAdena() >= _totalPrice) {
                  int objectId = item.getObjectId();
                  ItemInstance oldItem = player.checkItemManipulation(objectId, item.getCount(), "sell");
                  if (oldItem == null) {
                     oldItem = playerInventory.getItemByItemId(item.getId());
                     if (oldItem == null) {
                        continue;
                     }

                     objectId = oldItem.getObjectId();
                     oldItem = player.checkItemManipulation(objectId, item.getCount(), "sell");
                     if (oldItem == null) {
                        continue;
                     }
                  }

                  if (oldItem.getId() != item.getId()) {
                     Util.handleIllegalPlayerAction(player, "" + player.getName() + " is cheating with sell items");
                     return false;
                  }

                  if (oldItem.isTradeable()) {
                     ItemInstance newItem = playerInventory.transferItem("PrivateStore", objectId, item.getCount(), ownerInventory, player, this._owner);
                     if (newItem != null) {
                        this.removeItem(-1, item.getId(), item.getCount());
                        ok = true;
                        totalPrice = _totalPrice;
                        if (oldItem.getCount() > 0L && oldItem != newItem) {
                           playerIU.addModifiedItem(oldItem);
                        } else {
                           playerIU.addRemovedItem(oldItem);
                        }

                        if (newItem.getCount() > item.getCount()) {
                           ownerIU.addModifiedItem(newItem);
                        } else {
                           ownerIU.addNewItem(newItem);
                        }

                        if (newItem.isStackable()) {
                           SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S3_S2_S_FROM_C1);
                           msg.addString(player.getName());
                           msg.addItemName(newItem);
                           msg.addItemNumber(item.getCount());
                           this._owner.sendPacket(msg);
                           msg = SystemMessage.getSystemMessage(SystemMessageId.C1_PURCHASED_S3_S2_S);
                           msg.addString(this._owner.getName());
                           msg.addItemName(newItem);
                           msg.addItemNumber(item.getCount());
                           player.sendPacket(msg);
                        } else {
                           SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S2_FROM_C1);
                           msg.addString(player.getName());
                           msg.addItemName(newItem);
                           this._owner.sendPacket(msg);
                           msg = SystemMessage.getSystemMessage(SystemMessageId.C1_PURCHASED_S2);
                           msg.addString(this._owner.getName());
                           msg.addItemName(newItem);
                           player.sendPacket(msg);
                        }
                     }
                  }
               }
            }
         }

         if (totalPrice > 0L) {
            if (totalPrice > ownerInventory.getAdena()) {
               return false;
            }

            ItemInstance adenaItem = ownerInventory.getAdenaInstance();
            ownerInventory.reduceAdena("PrivateStore", totalPrice, this._owner, player);
            ownerIU.addItem(adenaItem);
            playerInventory.addAdena("PrivateStore", totalPrice, player, this._owner);
            playerIU.addItem(playerInventory.getAdenaInstance());
         }

         if (ok) {
            this._owner.sendPacket(ownerIU);
            this._owner.saveTradeList();
            player.sendPacket(playerIU);
         }

         return ok;
      } else {
         return false;
      }
   }
}
