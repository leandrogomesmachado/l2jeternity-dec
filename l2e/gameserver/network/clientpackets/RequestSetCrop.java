package l2e.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.CastleManorManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.CastleChamberlainInstance;
import l2e.gameserver.model.actor.templates.CropProcureTemplate;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.items.itemcontainer.PcInventory;

public class RequestSetCrop extends GameClientPacket {
   private static final int BATCH_LENGTH = 21;
   private int _manorId;
   private RequestSetCrop.Crop[] _items = null;

   @Override
   protected void readImpl() {
      this._manorId = this.readD();
      int count = this.readD();
      if (count > 0 && count <= Config.MAX_ITEM_IN_PACKET && count * 21 == this._buf.remaining()) {
         this._items = new RequestSetCrop.Crop[count];

         for(int i = 0; i < count; ++i) {
            int itemId = this.readD();
            long sales = this.readQ();
            long price = this.readQ();
            int type = this.readC();
            if (itemId < 1 || sales < 0L || price < 0L) {
               this._items = null;
               return;
            }

            this._items[i] = new RequestSetCrop.Crop(itemId, sales, price, type);
         }
      }
   }

   @Override
   protected void runImpl() {
      if (this._items != null) {
         Player player = this.getClient().getActiveChar();
         if (player != null && player.getClan() != null && (player.getClanPrivileges() & 131072) != 0) {
            Castle currentCastle = CastleManager.getInstance().getCastleById(this._manorId);
            if (currentCastle.getOwnerId() == player.getClanId()) {
               GameObject manager = player.getTarget();
               if (!(manager instanceof CastleChamberlainInstance)) {
                  manager = player.getLastFolkNPC();
               }

               if (manager instanceof CastleChamberlainInstance) {
                  if (((CastleChamberlainInstance)manager).getCastle() == currentCastle) {
                     if (player.isInsideRadius(manager, 150, true, false)) {
                        List<CropProcureTemplate> crops = new ArrayList<>(this._items.length);

                        for(RequestSetCrop.Crop i : this._items) {
                           CropProcureTemplate s = i.getCrop();
                           if (s == null) {
                              Util.handleIllegalPlayerAction(
                                 player, "" + player.getName() + " of account " + player.getAccountName() + " tried to overflow while setting manor."
                              );
                              return;
                           }

                           crops.add(s);
                        }

                        currentCastle.setCropProcure(crops, 1);
                        if (Config.ALT_MANOR_SAVE_ALL_ACTIONS) {
                           currentCastle.saveCropData(1);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static class Crop {
      private final int _itemId;
      private final long _sales;
      private final long _price;
      private final int _type;

      public Crop(int id, long s, long p, int t) {
         this._itemId = id;
         this._sales = s;
         this._price = p;
         this._type = t;
      }

      public CropProcureTemplate getCrop() {
         return this._sales != 0L && PcInventory.MAX_ADENA / this._sales < this._price
            ? null
            : CastleManorManager.getInstance().getNewCropProcure(this._itemId, this._sales, this._type, this._price, this._sales);
      }
   }
}
