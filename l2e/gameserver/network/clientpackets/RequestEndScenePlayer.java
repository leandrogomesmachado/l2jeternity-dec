package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;

public final class RequestEndScenePlayer extends GameClientPacket {
   private int _movieId;

   @Override
   protected void readImpl() {
      this._movieId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (this._movieId != 0) {
            if (activeChar.getMovieId() != this._movieId) {
               _log.warning("Player " + this.getClient() + " sent EndScenePlayer with wrong movie id: " + this._movieId);
            } else {
               activeChar.setMovieId(0);
               activeChar.setIsTeleporting(true, false);
               activeChar.decayMe();
               activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
               activeChar.setIsTeleporting(false, false);
            }
         }
      }
   }
}
