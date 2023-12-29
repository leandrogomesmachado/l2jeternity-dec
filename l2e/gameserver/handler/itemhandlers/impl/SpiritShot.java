package l2e.gameserver.handler.itemhandlers.impl;

import java.util.logging.Level;
import l2e.commons.util.Broadcast;
import l2e.gameserver.Config;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.type.ActionType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class SpiritShot implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         Player activeChar = (Player)playable;
         ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
         Weapon weaponItem = activeChar.getActiveWeaponItem();
         SkillHolder[] skills = item.getItem().getSkills();
         int itemId = item.getId();
         if (skills == null) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": is missing skills!");
            return false;
         } else if (weaponInst == null || weaponItem.getSpiritShotCount() == 0) {
            if (!activeChar.getAutoSoulShot().contains(itemId)) {
               activeChar.sendPacket(SystemMessageId.CANNOT_USE_SPIRITSHOTS);
            }

            return false;
         } else if (activeChar.isChargedShot(ShotType.SPIRITSHOTS)) {
            return false;
         } else {
            boolean gradeCheck = item.isEtcItem()
               && item.getEtcItem().getDefaultAction() == ActionType.spiritshot
               && weaponInst.getItem().getItemGradeSPlus() == item.getItem().getItemGradeSPlus();
            if (!gradeCheck) {
               return false;
            } else if (!Config.INFINITE_SPIRIT_SHOT
               && !activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), (long)weaponItem.getSpiritShotCount(), null, false)) {
               if (!activeChar.haveAutoShot(itemId)) {
                  activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS);
               }

               return false;
            } else {
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_S1_);
               sm.addItemName(itemId);
               activeChar.sendPacket(sm);
               activeChar.setChargedShot(ShotType.SPIRITSHOTS, true);
               activeChar.sendPacket(SystemMessageId.ENABLED_SPIRITSHOT);
               Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, skills[0].getId(), skills[0].getLvl(), 0, 0), 600);
               return true;
            }
         }
      }
   }
}
