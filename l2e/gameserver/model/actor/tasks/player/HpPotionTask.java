package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.Config;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.handler.itemhandlers.ItemHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.SkillType;

public class HpPotionTask implements Runnable {
   private final Player _player;

   public HpPotionTask(Player player) {
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
               ItemInstance hpPoint = this._player.getInventory().getItemByItemId(this._player.getVarInt("autoHpItemId", 0));
               if (hpPoint != null) {
                  int hpVar = this._player.getVarInt("hpPercent", Config.DEFAULT_HP_PERCENT);
                  if (this._player.getCurrentHp() < this._player.getMaxHp() * (double)hpVar / 100.0) {
                     IItemHandler handler = ItemHandler.getInstance().getHandler(hpPoint.getEtcItem());
                     if (handler != null) {
                        handler.useItem(this._player, hpPoint, false);
                     }
                  }
               } else if (Config.DISABLE_WITHOUT_POTIONS) {
                  this._player.stopHpPotionTask();
               }
            }
         }
      }
   }
}
