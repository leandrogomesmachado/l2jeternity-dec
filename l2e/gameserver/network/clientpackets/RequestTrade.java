package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.model.BlockedList;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.network.serverpackets.TradeRequest;

public final class RequestTrade extends GameClientPacket {
   private int _objectId;

   @Override
   protected void readImpl() {
      this._objectId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getActiveChar();
      if (player != null) {
         if (!player.isInFightEvent() || player.getFightEvent().canOpenStore(player)) {
            if (!player.getAccessLevel().allowTransaction()) {
               player.sendMessage("Transactions are disabled for your current Access Level.");
               this.sendActionFailed();
            } else {
               Effect ef = null;
               if ((ef = player.getFirstEffect(EffectType.ACTION_BLOCK)) != null && !ef.checkCondition(-2)) {
                  player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_SO_ACTIONS_NOT_ALLOWED);
                  player.sendActionFailed();
               } else {
                  GameObject target = World.getInstance().findObject(this._objectId);
                  if (target != null
                     && World.getInstance().getAroundPlayers(player).contains(target)
                     && (target.getReflectionId() == player.getReflectionId() || player.getReflectionId() == -1)) {
                     if (target.getObjectId() == player.getObjectId()) {
                        player.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                     } else if (!target.isPlayer()) {
                        player.sendPacket(SystemMessageId.INCORRECT_TARGET);
                     } else {
                        Player partner = target.getActingPlayer();
                        if (partner.isInOlympiadMode() || player.isInOlympiadMode()) {
                           player.sendMessage("A user currently participating in the Olympiad cannot accept or request a trade.");
                        } else if ((ef = partner.getFirstEffect(EffectType.ACTION_BLOCK)) != null && !ef.checkCondition(-2)) {
                           SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_REPORTED_AND_IS_BEING_INVESTIGATED);
                           sm.addCharName(partner);
                           player.sendPacket(sm);
                           player.sendActionFailed();
                        } else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && player.getKarma() > 0) {
                           player.sendMessage("You cannot trade while you are in a chaotic state.");
                        } else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && partner.getKarma() > 0) {
                           player.sendMessage("You cannot request a trade while your target is in a chaotic state.");
                        } else if (player.isJailed() || partner.isJailed()) {
                           player.sendMessage("You cannot trade while you are in in Jail.");
                        } else if (player.getPrivateStoreType() != 0 || partner.getPrivateStoreType() != 0) {
                           player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
                        } else if (player.isProcessingTransaction()) {
                           if (Config.DEBUG) {
                              _log.fine("Already trading with someone else.");
                           }

                           player.sendPacket(SystemMessageId.ALREADY_TRADING);
                        } else if (!partner.isProcessingRequest() && !partner.isProcessingTransaction()) {
                           if (partner.getTradeRefusal()) {
                              player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addString(partner.getName()));
                           } else if (BlockedList.isBlocked(partner, player)) {
                              SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
                              sm.addCharName(partner);
                              player.sendPacket(sm);
                           } else if (Util.calculateDistance(player, partner, true) > 150.0) {
                              player.sendPacket(SystemMessageId.TARGET_TOO_FAR);
                           } else {
                              player.onTransactionRequest(partner);
                              partner.sendPacket(new TradeRequest(player.getObjectId()));
                              SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REQUEST_C1_FOR_TRADE);
                              sm.addString(partner.getName());
                              player.sendPacket(sm);
                           }
                        } else {
                           if (Config.DEBUG) {
                              _log.info("Transaction already in progress.");
                           }

                           SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
                           sm.addString(partner.getName());
                           player.sendPacket(sm);
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
