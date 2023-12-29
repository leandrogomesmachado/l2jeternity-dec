package l2e.gameserver.handler.itemhandlers.impl;

import java.util.logging.Logger;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public class TempHero implements IItemHandler {
   private static Logger _log = Logger.getLogger(VisualItems.class.getName());

   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         Player player = playable.getActingPlayer();
         if (player.isHero()) {
            player.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
            return false;
         } else {
            int timeLimit = item.getItem().getTimeLimit();
            if (timeLimit == 0) {
               _log.info("Not correct timeLimit for item: " + item.getId());
               return false;
            } else if (!player.destroyItem("tempHero", item.getObjectId(), 1L, player, true)) {
               return false;
            } else {
               long endTime = System.currentTimeMillis() + (long)timeLimit * 60000L;
               player.setVar("tempHero", String.valueOf(endTime), endTime);
               player.setHero(true, false);
               player.startTempHeroTask(endTime);
               if (player.getClan() != null) {
                  player.setPledgeClass(ClanMember.calculatePledgeClass(player));
               } else {
                  player.setPledgeClass(8);
               }

               player.broadcastUserInfo(true);
               return true;
            }
         }
      }
   }
}
