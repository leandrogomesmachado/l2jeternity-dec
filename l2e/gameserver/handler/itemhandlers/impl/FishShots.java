package l2e.gameserver.handler.itemhandlers.impl;

import java.util.logging.Level;
import l2e.commons.util.Broadcast;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.type.ActionType;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public class FishShots implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         Player activeChar = playable.getActingPlayer();
         ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
         Weapon weaponItem = activeChar.getActiveWeaponItem();
         if (weaponInst == null || weaponItem.getItemType() != WeaponType.FISHINGROD) {
            return false;
         } else if (activeChar.isChargedShot(ShotType.FISH_SOULSHOTS)) {
            return false;
         } else {
            long count = item.getCount();
            SkillHolder[] skills = item.getItem().getSkills();
            if (skills == null) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": is missing skills!");
               return false;
            } else {
               boolean gradeCheck = item.isEtcItem()
                  && item.getEtcItem().getDefaultAction() == ActionType.fishingshot
                  && weaponInst.getItem().getItemGradeSPlus() == item.getItem().getItemGradeSPlus();
               if (!gradeCheck) {
                  activeChar.sendPacket(SystemMessageId.WRONG_FISHINGSHOT_GRADE);
                  return false;
               } else if (count < 1L) {
                  return false;
               } else {
                  activeChar.setChargedShot(ShotType.FISH_SOULSHOTS, true);
                  activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1L, null, false);
                  GameObject oldTarget = activeChar.getTarget();
                  activeChar.setTarget(activeChar);
                  Broadcast.toSelfAndKnownPlayers(activeChar, new MagicSkillUse(activeChar, skills[0].getId(), skills[0].getLvl(), 0, 0));
                  activeChar.setTarget(oldTarget);
                  return true;
               }
            }
         }
      }
   }
}
