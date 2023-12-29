package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ManorManagerInstance;
import l2e.gameserver.model.actor.templates.SeedTemplate;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.items.itemcontainer.PcInventory;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class RequestBuySeed extends GameClientPacket {
   private static final int BATCH_LENGTH = 12;
   private int _manorId;
   private RequestBuySeed.Seed[] _seeds = null;

   @Override
   protected void readImpl() {
      this._manorId = this.readD();
      int count = this.readD();
      if (count > 0 && count <= Config.MAX_ITEM_IN_PACKET && count * 12 == this._buf.remaining()) {
         this._seeds = new RequestBuySeed.Seed[count];

         for(int i = 0; i < count; ++i) {
            int itemId = this.readD();
            long cnt = this.readQ();
            if (cnt < 1L) {
               this._seeds = null;
               return;
            }

            this._seeds[i] = new RequestBuySeed.Seed(itemId, cnt);
         }
      }
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (player.isActionsDisabled()) {
            this.sendActionFailed();
         } else if (this._seeds == null) {
            this.sendActionFailed();
         } else {
            GameObject manager = player.getTarget();
            if (!(manager instanceof ManorManagerInstance)) {
               manager = player.getLastFolkNPC();
            }

            if (manager instanceof ManorManagerInstance) {
               if (player.isInsideRadius(manager, 150, true, false)) {
                  Castle castle = CastleManager.getInstance().getCastleById(this._manorId);
                  long totalPrice = 0L;
                  int slots = 0;
                  int totalWeight = 0;

                  for(RequestBuySeed.Seed i : this._seeds) {
                     if (!i.setProduction(castle)) {
                        return;
                     }

                     totalPrice += i.getPrice();
                     if (totalPrice > PcInventory.MAX_ADENA) {
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

                     Item template = ItemsParser.getInstance().getTemplate(i.getSeedId());
                     totalWeight = (int)((long)totalWeight + i.getCount() * (long)template.getWeight());
                     if (!template.isStackable()) {
                        slots = (int)((long)slots + i.getCount());
                     } else if (player.getInventory().getItemByItemId(i.getSeedId()) == null) {
                        ++slots;
                     }
                  }

                  if (!player.getInventory().validateWeight((long)totalWeight)) {
                     player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
                  } else if (!player.getInventory().validateCapacity((long)slots)) {
                     player.sendPacket(SystemMessageId.SLOTS_FULL);
                  } else if (totalPrice >= 0L && player.getAdena() >= totalPrice) {
                     for(RequestBuySeed.Seed i : this._seeds) {
                        if (player.reduceAdena("Buy", i.getPrice(), player, false) && i.updateProduction(castle)) {
                           player.addItem("Buy", i.getSeedId(), i.getCount(), manager, true);
                        } else {
                           totalPrice -= i.getPrice();
                        }
                     }

                     if (totalPrice > 0L) {
                        castle.addToTreasuryNoTax(totalPrice);
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED_ADENA);
                        sm.addItemNumber(totalPrice);
                        player.sendPacket(sm);
                     }
                  } else {
                     player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
                  }
               }
            }
         }
      }
   }

   private static class Seed {
      private final int _seedId;
      private final long _count;
      SeedTemplate _seed;

      public Seed(int id, long num) {
         this._seedId = id;
         this._count = num;
      }

      public int getSeedId() {
         return this._seedId;
      }

      public long getCount() {
         return this._count;
      }

      public long getPrice() {
         return this._seed.getPrice() * this._count;
      }

      public boolean setProduction(Castle c) {
         this._seed = c.getSeed(this._seedId, 0);
         if (this._seed.getPrice() <= 0L) {
            return false;
         } else if (this._seed.getCanProduce() < this._count) {
            return false;
         } else {
            return PcInventory.MAX_ADENA / this._count >= this._seed.getPrice();
         }
      }

      public boolean updateProduction(Castle c) {
         synchronized(this._seed) {
            long amount = this._seed.getCanProduce();
            if (this._count > amount) {
               return false;
            }

            this._seed.setCanProduce(amount - this._count);
         }

         if (Config.ALT_MANOR_SAVE_ALL_ACTIONS) {
            c.updateSeed(this._seedId, this._seed.getCanProduce(), 0);
         }

         return true;
      }
   }
}
