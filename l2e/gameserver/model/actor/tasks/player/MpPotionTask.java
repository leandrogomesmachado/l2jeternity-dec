package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.Config;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.handler.itemhandlers.ItemHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.SkillType;

public class MpPotionTask implements Runnable {
   private final Player _player;

   public MpPotionTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player != null) {
         if (!this._player.isDead()
            && !this._player.isAllSkillsDisabled()
            && !this._player.isHealBlocked()
            && !this._player.isCombatFlagEquipped()
            && this._player.getUCState() <= 0
            && !this._player.isCombatFlagEquipped()
            && !this._player.isInFightEvent()
            && !this._player.isInOlympiadMode()
            && (
               !AerialCleftEvent.getInstance().isStarted() && !AerialCleftEvent.getInstance().isRewarding()
                  || !AerialCleftEvent.getInstance().isPlayerParticipant(this._player.getObjectId())
            )) {
            if (this._player.getCastingSkill() == null
               || this._player.getCastingSkill().getSkillType() != SkillType.FUSION
                  && this._player.getCastingSkill().getSkillType() != SkillType.SIGNET_CASTTIME) {
               ItemInstance mpPoint = this._player.getInventory().getItemByItemId(this._player.getVarInt("autoMpItemId", 0));
               if (mpPoint != null) {
                  int mpVar = this._player.getVarInt("mpPercent", Config.DEFAULT_MP_PERCENT);
                  if (this._player.getCurrentMp() < this._player.getMaxMp() * (double)mpVar / 100.0) {
                     IItemHandler handler = ItemHandler.getInstance().getHandler(mpPoint.getEtcItem());
                     if (handler != null) {
                        handler.useItem(this._player, mpPoint, false);
                     }
                  }
               } else if (Config.DISABLE_WITHOUT_POTIONS) {
                  this._player.stopMpPotionTask();
               }
            }
         }
      }
   }
}
