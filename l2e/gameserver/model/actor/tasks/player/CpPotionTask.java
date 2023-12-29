package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.Config;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.handler.itemhandlers.ItemHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.SkillType;

public class CpPotionTask implements Runnable {
   private final Player _player;

   public CpPotionTask(Player player) {
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
            && !this._player.isInFightEvent()
            && !this._player.isInOlympiadMode()
            && (
               !AerialCleftEvent.getInstance().isStarted() && !AerialCleftEvent.getInstance().isRewarding()
                  || !AerialCleftEvent.getInstance().isPlayerParticipant(this._player.getObjectId())
            )) {
            if (this._player.getCastingSkill() == null
               || this._player.getCastingSkill().getSkillType() != SkillType.FUSION
                  && this._player.getCastingSkill().getSkillType() != SkillType.SIGNET_CASTTIME) {
               ItemInstance cpPoint = this._player.getInventory().getItemByItemId(this._player.getVarInt("autoCpItemId", 0));
               if (cpPoint != null) {
                  int cpVar = this._player.getVarInt("cpPercent", Config.DEFAULT_CP_PERCENT);
                  if (this._player.getCurrentCp() < this._player.getMaxCp() * (double)cpVar / 100.0) {
                     IItemHandler handler = ItemHandler.getInstance().getHandler(cpPoint.getEtcItem());
                     if (handler != null) {
                        handler.useItem(this._player, cpPoint, false);
                     }
                  }
               } else if (Config.DISABLE_WITHOUT_POTIONS) {
                  this._player.stopCpPotionTask();
               }
            }
         }
      }
   }
}
