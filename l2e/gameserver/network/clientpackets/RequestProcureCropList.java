package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.data.parser.ManorParser;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ManorManagerInstance;
import l2e.gameserver.model.actor.templates.CropProcureTemplate;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class RequestProcureCropList extends GameClientPacket {
   private static final int BATCH_LENGTH = 20;
   private RequestProcureCropList.Crop[] _items = null;

   @Override
   protected void readImpl() {
      int count = this.readD();
      if (count > 0 && count <= Config.MAX_ITEM_IN_PACKET && count * 20 == this._buf.remaining()) {
         this._items = new RequestProcureCropList.Crop[count];

         for(int i = 0; i < count; ++i) {
            int objId = this.readD();
            int itemId = this.readD();
            int manorId = this.readD();
            long cnt = this.readQ();
            if (objId < 1 || itemId < 1 || manorId < 0 || cnt < 0L) {
               this._items = null;
               return;
            }

            this._items[i] = new RequestProcureCropList.Crop(objId, itemId, manorId, cnt);
         }
      }
   }

   @Override
   protected void runImpl() {
      if (this._items != null) {
         Player player = this.getClient().getActiveChar();
         if (player != null) {
            if (player.isActionsDisabled()) {
               player.sendActionFailed();
            } else {
               GameObject manager = player.getTarget();
               if (!(manager instanceof ManorManagerInstance)) {
                  manager = player.getLastFolkNPC();
               }

               if (manager instanceof ManorManagerInstance) {
                  if (player.isInsideRadius(manager, 150, false, false)) {
                     int castleId = ((ManorManagerInstance)manager).getCastle().getId();
                     int slots = 0;
                     int weight = 0;

                     for(RequestProcureCropList.Crop i : this._items) {
                        if (i.getCrop()) {
                           Item template = ItemsParser.getInstance().getTemplate(i.getReward());
                           weight = (int)((long)weight + i.getCount() * (long)template.getWeight());
                           if (!template.isStackable()) {
                              slots = (int)((long)slots + i.getCount());
                           } else if (player.getInventory().getItemByItemId(i.getId()) == null) {
                              ++slots;
                           }
                        }
                     }

                     if (!player.getInventory().validateWeight((long)weight)) {
                        player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
                     } else if (!player.getInventory().validateCapacity((long)slots)) {
                        player.sendPacket(SystemMessageId.SLOTS_FULL);
                     } else {
                        for(RequestProcureCropList.Crop i : this._items) {
                           if (i.getReward() != 0) {
                              long fee = i.getFee(castleId);
                              long rewardPrice = (long)ItemsParser.getInstance().getTemplate(i.getReward()).getReferencePrice();
                              if (rewardPrice != 0L) {
                                 long rewardItemCount = i.getPrice() / rewardPrice;
                                 if (rewardItemCount < 1L) {
                                    SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1);
                                    sm.addItemName(i.getId());
                                    sm.addItemNumber(i.getCount());
                                    player.sendPacket(sm);
                                 } else if (player.getAdena() < fee) {
                                    SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1);
                                    sm.addItemName(i.getId());
                                    sm.addItemNumber(i.getCount());
                                    player.sendPacket(sm);
                                    sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
                                    player.sendPacket(sm);
                                 } else {
                                    ItemInstance item = player.getInventory().getItemByObjectId(i.getObjectId());
                                    if (item != null
                                       && item.getCount() >= i.getCount()
                                       && i.setCrop()
                                       && (fee <= 0L || player.reduceAdena("Manor", fee, manager, true))
                                       && player.destroyItem("Manor", i.getObjectId(), i.getCount(), manager, true)) {
                                       player.addItem("Manor", i.getReward(), rewardItemCount, manager, true);
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static class Crop {
      private final int _objectId;
      private final int _itemId;
      private final int _manorId;
      private final long _count;
      private int _reward = 0;
      private CropProcureTemplate _crop = null;

      public Crop(int obj, int id, int m, long num) {
         this._objectId = obj;
         this._itemId = id;
         this._manorId = m;
         this._count = num;
      }

      public int getObjectId() {
         return this._objectId;
      }

      public int getId() {
         return this._itemId;
      }

      public long getCount() {
         return this._count;
      }

      public int getReward() {
         return this._reward;
      }

      public long getPrice() {
         return this._crop.getPrice() * this._count;
      }

      public long getFee(int castleId) {
         return this._manorId == castleId ? 0L : this.getPrice() / 100L * 5L;
      }

      public boolean getCrop() {
         try {
            this._crop = CastleManager.getInstance().getCastleById(this._manorId).getCrop(this._itemId, 0);
         } catch (NullPointerException var2) {
            return false;
         }

         if (this._crop == null || this._crop.getId() == 0 || this._crop.getPrice() == 0L || this._count == 0L) {
            return false;
         } else if (this._count > this._crop.getAmount()) {
            return false;
         } else if (PcInventory.MAX_ADENA / this._count < this._crop.getPrice()) {
            return false;
         } else {
            this._reward = ManorParser.getInstance().getRewardItem(this._itemId, this._crop.getReward());
            return true;
         }
      }

      public boolean setCrop() {
         synchronized(this._crop) {
            long amount = this._crop.getAmount();
            if (this._count > amount) {
               return false;
            }

            this._crop.setAmount(amount - this._count);
         }

         if (Config.ALT_MANOR_SAVE_ALL_ACTIONS) {
            CastleManager.getInstance().getCastleById(this._manorId).updateCrop(this._itemId, this._crop.getAmount(), 0);
         }

         return true;
      }
   }
}
