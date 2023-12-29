package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class FallingTask implements Runnable {
   private final Player _player;
   private final int _damage;

   public FallingTask(Player player, int damage) {
      this._player = player;
      this._damage = damage;
   }

   @Override
   public void run() {
      if (this._player != null) {
         if (!this._player.isDead()) {
            if (GeoEngine.hasGeo(this._player.getX(), this._player.getY(), this._player.getGeoIndex())) {
               int z = GeoEngine.getHeight(this._player.getX(), this._player.getY(), this._player.getZ(), this._player.getGeoIndex());
               this._player.setClientZ(z);
               this._player.setZ(z);
            }

            this._player.reduceCurrentHp(Math.min((double)this._damage, this._player.getCurrentHp() - 1.0), null, false, true, null);
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FALL_DAMAGE_S1);
            sm.addNumber(this._damage);
            this._player.sendPacket(sm);
         }
      }
   }
}
