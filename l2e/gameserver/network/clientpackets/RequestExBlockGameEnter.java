package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.HandysBlockCheckerManager;
import l2e.gameserver.model.actor.Player;

public final class RequestExBlockGameEnter extends GameClientPacket {
   int _arena;
   int _team;

   @Override
   protected void readImpl() {
      this._arena = this.readD() + 1;
      this._team = this.readD();
   }

   @Override
   public void runImpl() {
      if (!HandysBlockCheckerManager.getInstance().arenaIsBeingUsed(this._arena)) {
         Player player = this.getClient().getActiveChar();
         switch(this._team) {
            case -1:
               int team = HandysBlockCheckerManager.getInstance().getHolder(this._arena).getPlayerTeam(player);
               if (team > -1) {
                  HandysBlockCheckerManager.getInstance().removePlayer(player, this._arena, team);
               }
               break;
            case 0:
            case 1:
               HandysBlockCheckerManager.getInstance().changePlayerToTeam(player, this._arena, this._team);
               break;
            default:
               _log.warning("Wrong Cube Game Team ID: " + this._team);
         }
      }
   }
}
