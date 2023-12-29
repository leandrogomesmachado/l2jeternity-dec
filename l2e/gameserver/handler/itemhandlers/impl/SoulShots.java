package l2e.gameserver.handler.itemhandlers.impl;

import java.util.logging.Level;
import l2e.commons.util.Broadcast;
import l2e.commons.util.Rnd;
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

public class SoulShots implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         Player activeChar = playable.getActingPlayer();
         ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
         Weapon weaponItem = activeChar.getActiveWeaponItem();
         SkillHolder[] skills = item.getItem().getSkills();
         int itemId = item.getId();
         if (skills == null) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": is missing skills!");
            return false;
         } else if (weaponInst != null && weaponItem.getSoulShotCount() != 0) {
            boolean gradeCheck = item.isEtcItem()
               && item.getEtcItem().getDefaultAction() == ActionType.soulshot
               && weaponInst.getItem().getItemGradeSPlus() == item.getItem().getItemGradeSPlus();
            if (!gradeCheck) {
               return false;
            } else {
               activeChar.soulShotLock.lock();

               label139: {
                  boolean var11;
                  try {
                     if (activeChar.isChargedShot(ShotType.SOULSHOTS)) {
                        return false;
                     }

                     int SSCount = weaponItem.getSoulShotCount();
                     if (weaponItem.getReducedSoulShot() > 0 && Rnd.get(100) < weaponItem.getReducedSoulShotChance()) {
                        SSCount = weaponItem.getReducedSoulShot();
                     }

                     if (Config.INFINITE_SOUL_SHOT || activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), (long)SSCount, null, false)) {
                        weaponInst.setChargedShot(ShotType.SOULSHOTS, true);
                        break label139;
                     }

                     if (!activeChar.haveAutoShot(itemId)) {
                        activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS);
                     }

                     var11 = false;
                  } finally {
                     activeChar.soulShotLock.unlock();
                  }

                  return var11;
               }

               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_S1_);
               sm.addItemName(itemId);
               activeChar.sendPacket(sm);
               activeChar.sendPacket(SystemMessageId.ENABLED_SOULSHOT);
               Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, skills[0].getId(), skills[0].getLvl(), 0, 0), 600);
               return true;
            }
         } else {
            if (!activeChar.getAutoSoulShot().contains(itemId)) {
               activeChar.sendPacket(SystemMessageId.CANNOT_USE_SOULSHOTS);
            }

            return false;
         }
      }
   }
}
