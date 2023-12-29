package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.Config;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.handler.itemhandlers.ItemHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.items.instance.ItemInstance;

public class SoulPotionTask implements Runnable {
   private final Player _player;

   public SoulPotionTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player != null) {
         if (!this._player.isInFightEvent()
            && !this._player.isInOlympiadMode()
            && (
               !AerialCleftEvent.getInstance().isStarted() && !AerialCleftEvent.getInstance().isRewarding()
                  || !AerialCleftEvent.getInstance().isPlayerParticipant(this._player.getObjectId())
            )) {
            ItemInstance soulPoint = this._player.getInventory().getItemByItemId(this._player.getVarInt("autoSoulItemId", 0));
            if (soulPoint != null) {
               int soulVar = this._player.getVarInt("soulPercent", Config.DEFAULT_SOUL_AMOUNT);
               if (this._player.getChargedSouls() < soulVar) {
                  IItemHandler handler = ItemHandler.getInstance().getHandler(soulPoint.getEtcItem());
                  if (handler != null) {
                     handler.useItem(this._player, soulPoint, false);
                  }
               }
            } else if (Config.DISABLE_WITHOUT_POTIONS) {
               this._player.stopSoulPotionTask();
            }
         }
      }
   }
}
