package l2e.gameserver.handler.itemhandlers.impl;

import l2e.gameserver.handler.communityhandlers.CommunityBoardHandler;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public class Community implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         Player player = playable.getActingPlayer();
         if (item.getItem().getShowBoard() != null && !item.getItem().getShowBoard().isEmpty()) {
            ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler(item.getItem().getShowBoard());
            if (handler != null) {
               handler.onBypassCommand(item.getItem().getShowBoard(), player);
            }
         }

         player.sendActionFailed();
         return true;
      }
   }
}
