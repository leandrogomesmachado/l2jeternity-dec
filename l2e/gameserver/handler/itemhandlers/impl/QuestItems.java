package l2e.gameserver.handler.itemhandlers.impl;

import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Item;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.SystemMessageId;

public class QuestItems implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceuse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         Player player = playable.getActingPlayer();
         if (!player.destroyItem("Item Handler - QuestItems", item, player, true)) {
            return false;
         } else {
            Item itm = item.getItem();
            if (itm.getQuestEvents() == null) {
               _log.warning(QuestItems.class.getSimpleName() + ": Null list for item handler QuestItems!");
               return false;
            } else {
               for(Quest quest : itm.getQuestEvents()) {
                  QuestState state = player.getQuestState(quest.getName());
                  if (state != null && state.isStarted()) {
                     quest.notifyItemUse(itm, player);
                  }
               }

               return true;
            }
         }
      }
   }
}
