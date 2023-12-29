package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.DuelManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestDuelAnswerStart extends GameClientPacket {
   private int _partyDuel;
   protected int _unk1;
   private int _response;

   @Override
   protected void readImpl() {
      this._partyDuel = this.readD();
      this._unk1 = this.readD();
      this._response = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         Player requestor = player.getActiveRequester();
         if (requestor != null) {
            if (!player.isActionsDisabled()) {
               if (this._response == 1) {
                  SystemMessage msg1 = null;
                  SystemMessage msg2 = null;
                  if (requestor.isInDuel()) {
                     msg1 = SystemMessage.getSystemMessage(SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL);
                     msg1.addString(requestor.getName());
                     player.sendPacket(msg1);
                     return;
                  }

                  if (player.isInDuel()) {
                     player.sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME);
                     return;
                  }

                  if (requestor.isActionsDisabled()) {
                     return;
                  }

                  if (this._partyDuel == 1) {
                     msg1 = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACCEPTED_C1_CHALLENGE_TO_A_PARTY_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
                     msg1.addString(requestor.getName());
                     msg2 = SystemMessage.getSystemMessage(
                        SystemMessageId.S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS
                     );
                     msg2.addString(player.getName());
                  } else {
                     msg1 = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACCEPTED_C1_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
                     msg1.addString(requestor.getName());
                     msg2 = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS);
                     msg2.addString(player.getName());
                  }

                  player.sendPacket(msg1);
                  requestor.sendPacket(msg2);
                  DuelManager.getInstance().addDuel(requestor, player, this._partyDuel);
               } else if (this._response == -1) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_SET_TO_REFUSE_DUEL_REQUEST);
                  sm.addPcName(player);
                  requestor.sendPacket(sm);
               } else {
                  SystemMessage msg = null;
                  if (this._partyDuel == 1) {
                     msg = SystemMessage.getSystemMessage(SystemMessageId.THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL);
                  } else {
                     msg = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL);
                     msg.addPcName(player);
                  }

                  requestor.sendPacket(msg);
               }

               player.setActiveRequester(null);
               requestor.onTransactionResponse();
            }
         }
      }
   }
}
