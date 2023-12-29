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
import l2e.gameserver.model.actor.templates.SeedTemplate;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.items.itemcontainer.PcInventory;

public class RequestSetSeed extends GameClientPacket {
   private static final int BATCH_LENGTH = 20;
   private int _manorId;
   private RequestSetSeed.Seed[] _items = null;

   @Override
   protected void readImpl() {
      this._manorId = this.readD();
      int count = this.readD();
      if (count > 0 && count <= Config.MAX_ITEM_IN_PACKET && count * 20 == this._buf.remaining()) {
         this._items = new RequestSetSeed.Seed[count];

         for(int i = 0; i < count; ++i) {
            int itemId = this.readD();
            long sales = this.readQ();
            long price = this.readQ();
            if (itemId < 1 || sales < 0L || price < 0L) {
               this._items = null;
               return;
            }

            this._items[i] = new RequestSetSeed.Seed(itemId, sales, price);
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
                        List<SeedTemplate> seeds = new ArrayList<>(this._items.length);

                        for(RequestSetSeed.Seed i : this._items) {
                           SeedTemplate s = i.getSeed();
                           if (s == null) {
                              Util.handleIllegalPlayerAction(
                                 player, "" + player.getName() + " of account " + player.getAccountName() + " tried to overflow while setting manor."
                              );
                              return;
                           }

                           seeds.add(s);
                        }

                        currentCastle.setSeedProduction(seeds, 1);
                        if (Config.ALT_MANOR_SAVE_ALL_ACTIONS) {
                           currentCastle.saveSeedData(1);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static class Seed {
      private final int _itemId;
      private final long _sales;
      private final long _price;

      public Seed(int id, long s, long p) {
         this._itemId = id;
         this._sales = s;
         this._price = p;
      }

      public SeedTemplate getSeed() {
         return this._sales != 0L && PcInventory.MAX_ADENA / this._sales < this._price
            ? null
            : CastleManorManager.getInstance().getNewSeedProduction(this._itemId, this._sales, this._price, this._sales);
      }
   }
}
