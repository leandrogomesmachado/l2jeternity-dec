package l2e.gameserver.handler.itemhandlers.impl;

import l2e.commons.util.Broadcast;
import l2e.commons.util.Rnd;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.Dice;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class RollingDice implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         Player activeChar = playable.getActingPlayer();
         int itemId = item.getId();
         if (activeChar.isInOlympiadMode()) {
            activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
            return false;
         } else {
            int number = this.rollDice(activeChar);
            if (number == 0) {
               activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER);
               return false;
            } else {
               Broadcast.toSelfAndKnownPlayers(
                  activeChar, new Dice(activeChar.getObjectId(), itemId, number, activeChar.getX() - 30, activeChar.getY() - 30, activeChar.getZ())
               );
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ROLLED_S2);
               sm.addString(activeChar.getName());
               sm.addNumber(number);
               activeChar.sendPacket(sm);
               if (activeChar.isInsideZone(ZoneId.PEACE)) {
                  Broadcast.toKnownPlayers(activeChar, sm);
               } else if (activeChar.isInParty()) {
                  activeChar.getParty().broadcastToPartyMembers(activeChar, sm);
               }

               return true;
            }
         }
      }
   }

   private int rollDice(Player player) {
      return !player.checkFloodProtection("ROLLDICE", "use_rollDice") ? 0 : Rnd.get(1, 6);
   }
}
