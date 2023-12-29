package l2e.gameserver.model.actor.tasks.player;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.handler.itemhandlers.ItemHandler;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class PetFeedTask implements Runnable {
   private static final Logger _log = Logger.getLogger(PetFeedTask.class.getName());
   private final Player _player;

   public PetFeedTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player != null) {
         try {
            if (!this._player.isMounted() || this._player.getMountNpcId() == 0 || this._player.getPetData(this._player.getMountNpcId()) == null) {
               this._player.stopFeed();
               return;
            }

            if (this._player.getCurrentFeed() <= this._player.getFeedConsume()) {
               this._player.setCurrentFeed(0);
               this._player.stopFeed();
               this._player.dismount();
               this._player.sendPacket(SystemMessageId.OUT_OF_FEED_MOUNT_CANCELED);
               return;
            }

            this._player.setCurrentFeed(this._player.getCurrentFeed() - this._player.getFeedConsume());
            List<Integer> foodIds = this._player.getPetData(this._player.getMountNpcId()).getFood();
            if (foodIds.isEmpty()) {
               return;
            }

            boolean summonHaveFood = false;
            ItemInstance food = null;
            if (this._player.getSummon() != null) {
               for(int id : foodIds) {
                  food = this._player.getSummon().getInventory().getItemByItemId(id);
                  if (food != null) {
                     summonHaveFood = true;
                     break;
                  }
               }
            }

            if (food == null) {
               for(int id : foodIds) {
                  food = this._player.getInventory().getItemByItemId(id);
                  if (food != null) {
                     break;
                  }
               }
            }

            if (food != null && this._player.isHungry()) {
               IItemHandler handler = ItemHandler.getInstance().getHandler(food.getEtcItem());
               if (handler != null) {
                  handler.useItem((Playable)(summonHaveFood ? this._player.getSummon() : this._player), food, false);
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY);
                  sm.addItemName(food.getId());
                  this._player.sendPacket(sm);
               }
            }
         } catch (Exception var6) {
            _log.log(Level.SEVERE, "Mounted Pet [NpcId: " + this._player.getMountNpcId() + "] a feed task error has occurred", (Throwable)var6);
         }
      }
   }
}
