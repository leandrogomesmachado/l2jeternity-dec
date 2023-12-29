package l2e.gameserver.handler.itemhandlers.impl;

import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public class Disguise implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         Player activeChar = playable.getActingPlayer();
         int regId = TerritoryWarManager.getInstance().getRegisteredTerritoryId(activeChar);
         if (regId > 0 && regId == item.getId() - 13596) {
            if (activeChar.getClan() != null && activeChar.getClan().getCastleId() > 0) {
               activeChar.sendPacket(SystemMessageId.TERRITORY_OWNING_CLAN_CANNOT_USE_DISGUISE_SCROLL);
               return false;
            } else {
               TerritoryWarManager.getInstance().addDisguisedPlayer(activeChar.getObjectId());
               activeChar.broadcastUserInfo(true);
               playable.destroyItem("Consume", item.getObjectId(), 1L, null, false);
               return true;
            }
         } else if (regId > 0) {
            activeChar.sendPacket(SystemMessageId.THE_DISGUISE_SCROLL_MEANT_FOR_DIFFERENT_TERRITORY);
            return false;
         } else {
            activeChar.sendPacket(SystemMessageId.TERRITORY_WAR_SCROLL_CAN_NOT_USED_NOW);
            return false;
         }
      }
   }
}
