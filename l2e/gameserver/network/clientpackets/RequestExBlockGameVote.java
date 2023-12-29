package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.HandysBlockCheckerManager;
import l2e.gameserver.model.actor.Player;

public final class RequestExBlockGameVote extends GameClientPacket {
   int _arena;
   int _answer;

   @Override
   protected void readImpl() {
      this._arena = this.readD() + 1;
      this._answer = this.readD();
   }

   @Override
   public void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         switch(this._answer) {
            case 0:
               break;
            case 1:
               HandysBlockCheckerManager.getInstance().increaseArenaVotes(this._arena);
               break;
            default:
               _log.warning("Unknown Cube Game Answer ID: " + this._answer);
         }
      }
   }
}
