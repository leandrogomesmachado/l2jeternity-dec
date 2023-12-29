package l2e.gameserver.handler.itemhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.data.dao.CharacterPremiumDAO;
import l2e.gameserver.data.parser.PremiumAccountsParser;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.service.premium.PremiumTemplate;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.SystemMessageId;

public class Premium implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else if (!Config.USE_PREMIUMSERVICE) {
         return false;
      } else {
         int id = item.getItem().getPremiumId();
         Player player = playable.getActingPlayer();
         if (player != null && id >= 0) {
            if (player.hasPremiumBonus() && !Config.PREMIUMSERVICE_DOUBLE) {
               player.sendMessage(new ServerMessage("ServiceBBS.PREMIUM_MSG", player.getLang()).toString());
               return false;
            }

            PremiumTemplate template = PremiumAccountsParser.getInstance().getPremiumTemplate(id);
            if (template != null) {
               if (!player.destroyItem("Premium", item.getObjectId(), 1L, player, true)) {
                  return false;
               }

               long time = !template.isOnlineType() ? System.currentTimeMillis() + template.getTime() * 1000L : 0L;
               if (template.isPersonal()) {
                  CharacterPremiumDAO.getInstance().updatePersonal(player, id, time);
               } else {
                  CharacterPremiumDAO.getInstance().update(player, id, time);
               }

               if (player.isInParty()) {
                  player.getParty().recalculatePartyData();
               }
            }
         }

         player.sendActionFailed();
         return true;
      }
   }
}
