package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class WaterTask implements Runnable {
   private final Player _player;

   public WaterTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player != null) {
         double reduceHp = this._player.getMaxHp() / 100.0;
         if (reduceHp < 1.0) {
            reduceHp = 1.0;
         }

         this._player.reduceCurrentHp(reduceHp, this._player, false, false, null);
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DROWN_DAMAGE_S1);
         sm.addNumber((int)reduceHp);
         this._player.sendPacket(sm);
      }
   }
}
