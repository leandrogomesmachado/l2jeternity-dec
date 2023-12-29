package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class FameTask implements Runnable {
   private final Player _player;
   private final int _value;

   public FameTask(Player player, int value) {
      this._player = player;
      this._value = value;
   }

   @Override
   public void run() {
      if (this._player != null && (!this._player.isDead() || Config.FAME_FOR_DEAD_PLAYERS)) {
         if (this._player.getClient() != null && !this._player.getClient().isDetached() || Config.OFFLINE_FAME) {
            int value = (int)((double)this._value * this._player.getPremiumBonus().getFameBonus());
            this._player.setFame(this._player.getFame() + value);
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_REPUTATION_SCORE);
            sm.addNumber(value);
            this._player.sendPacket(sm);
            this._player.sendUserInfo();
         }
      }
   }
}
