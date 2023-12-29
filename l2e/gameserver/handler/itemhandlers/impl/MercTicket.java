package l2e.gameserver.handler.itemhandlers.impl;

import l2e.gameserver.SevenSigns;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.MercTicketManager;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public class MercTicket implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         int itemId = item.getId();
         Player activeChar = (Player)playable;
         Castle castle = CastleManager.getInstance().getCastle(activeChar);
         int castleId = -1;
         if (castle != null) {
            castleId = castle.getId();
         }

         if (MercTicketManager.getInstance().getTicketCastleId(itemId) != castleId) {
            activeChar.sendPacket(SystemMessageId.MERCENARIES_CANNOT_BE_POSITIONED_HERE);
            return false;
         } else if (!activeChar.isCastleLord(castleId)) {
            activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_AUTHORITY_TO_POSITION_MERCENARIES);
            return false;
         } else if (castle != null && castle.getSiege().getIsInProgress()) {
            activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
            return false;
         } else if (SevenSigns.getInstance().getCurrentPeriod() != 3) {
            activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
            return false;
         } else {
            switch(SevenSigns.getInstance().getSealOwner(3)) {
               case 0:
                  if (SevenSigns.getInstance().checkIsDawnPostingTicket(itemId)) {
                     activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
                     return false;
                  }
                  break;
               case 1:
                  if (!SevenSigns.getInstance().checkIsRookiePostingTicket(itemId)) {
                     activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
                     return false;
                  }
               case 2:
            }

            if (MercTicketManager.getInstance().isAtCasleLimit(item.getId())) {
               activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
               return false;
            } else if (MercTicketManager.getInstance().isAtTypeLimit(item.getId())) {
               activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
               return false;
            } else if (MercTicketManager.getInstance().isTooCloseToAnotherTicket(activeChar.getX(), activeChar.getY(), activeChar.getZ())) {
               activeChar.sendPacket(SystemMessageId.POSITIONING_CANNOT_BE_DONE_BECAUSE_DISTANCE_BETWEEN_MERCENARIES_TOO_SHORT);
               return false;
            } else {
               MercTicketManager.getInstance().addTicket(item.getId(), activeChar);
               activeChar.destroyItem("Consume", item.getObjectId(), 1L, null, false);
               activeChar.sendPacket(SystemMessageId.PLACE_CURRENT_LOCATION_DIRECTION);
               return true;
            }
         }
      }
   }
}
