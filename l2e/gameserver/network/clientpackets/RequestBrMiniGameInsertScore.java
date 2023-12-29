package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.games.MiniGameScoreManager;
import l2e.gameserver.model.actor.Player;

public class RequestBrMiniGameInsertScore extends GameClientPacket {
   private int _score;

   @Override
   protected void readImpl() {
      this._score = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         MiniGameScoreManager.getInstance().addScore(player, this._score);
      }
   }
}
