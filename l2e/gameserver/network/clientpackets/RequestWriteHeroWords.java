package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Hero;

public final class RequestWriteHeroWords extends GameClientPacket {
   private String _heroWords;

   @Override
   protected void readImpl() {
      this._heroWords = this.readS();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null && player.isHero()) {
         if (this._heroWords != null && this._heroWords.length() <= 300) {
            Hero.getInstance().setHeroMessage(player, this._heroWords);
         }
      }
   }
}
