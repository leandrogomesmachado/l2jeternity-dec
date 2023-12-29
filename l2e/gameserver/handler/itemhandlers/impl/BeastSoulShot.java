package l2e.gameserver.handler.itemhandlers.impl;

import java.util.logging.Level;
import l2e.commons.util.Broadcast;
import l2e.gameserver.Config;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class BeastSoulShot implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         Player activeOwner = playable.getActingPlayer();
         if (!activeOwner.hasSummon()) {
            activeOwner.sendPacket(SystemMessageId.PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
            return false;
         } else if (activeOwner.getSummon().isDead()) {
            activeOwner.sendPacket(SystemMessageId.SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET);
            return false;
         } else {
            int itemId = item.getId();
            int shotConsumption = activeOwner.getSummon().getSoulShotsPerHit();
            long shotCount = item.getCount();
            SkillHolder[] skills = item.getItem().getSkills();
            if (skills == null) {
               _log.log(Level.WARNING, this.getClass().getSimpleName() + ": is missing skills!");
               return false;
            } else if (shotCount < (long)shotConsumption) {
               if (!activeOwner.haveAutoShot(itemId)) {
                  activeOwner.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET);
               }

               return false;
            } else if (activeOwner.getSummon().isChargedShot(ShotType.SOULSHOTS)) {
               return false;
            } else if (!Config.INFINITE_BEAST_SOUL_SHOT
               && !activeOwner.destroyItemWithoutTrace("Consume", item.getObjectId(), (long)shotConsumption, null, false)) {
               if (!activeOwner.haveAutoShot(itemId)) {
                  activeOwner.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET);
               }

               return false;
            } else {
               activeOwner.getSummon().setChargedShot(ShotType.SOULSHOTS, true);
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_S1_);
               sm.addItemName(itemId);
               activeOwner.sendPacket(sm);
               activeOwner.sendPacket(SystemMessageId.PET_USE_SPIRITSHOT);
               Broadcast.toSelfAndKnownPlayersInRadius(
                  activeOwner, new MagicSkillUse(activeOwner.getSummon(), activeOwner.getSummon(), skills[0].getId(), skills[0].getLvl(), 0, 0), 600
               );
               return true;
            }
         }
      }
   }
}
