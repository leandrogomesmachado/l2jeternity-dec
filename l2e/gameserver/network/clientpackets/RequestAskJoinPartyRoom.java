package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.matching.MatchingRoom;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExAskJoinPartyRoom;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class RequestAskJoinPartyRoom extends GameClientPacket {
   private static String _name;

   @Override
   protected void readImpl() {
      _name = this.readS();
   }

   @Override
   protected void runImpl() {
      Player player = this.getActiveChar();
      if (player != null) {
         player.isntAfk();
         Player target = World.getInstance().getPlayer(_name);
         if (target != null) {
            if (player.isInFightEvent() && !player.getFightEvent().canJoinParty(player, target)) {
               player.sendMessage("You cannot do that on Fight Club!");
               return;
            }

            if (!target.isProcessingRequest()) {
               if (target.getMatchingRoom() != null) {
                  return;
               }

               MatchingRoom room = player.getMatchingRoom();
               if (room == null || room.getType() != MatchingRoom.PARTY_MATCHING) {
                  return;
               }

               if (room.getPlayers().size() >= room.getMaxMembersSize()) {
                  player.sendPacket(SystemMessageId.PARTY_ROOM_FULL);
                  return;
               }

               player.onTransactionRequest(target);
               target.sendPacket(new ExAskJoinPartyRoom(player.getName(), room.getTopic()));
               player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_INVITED_YOU_TO_PARTY_ROOM).addPcName(player));
               target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_INVITED_YOU_TO_PARTY_ROOM).addPcName(player));
            } else {
               player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addPcName(target));
            }
         } else {
            player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
         }
      }
   }
}
