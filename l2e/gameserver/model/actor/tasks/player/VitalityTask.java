package l2e.gameserver.model.actor.tasks.player;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.stat.PcStat;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.serverpackets.ExVitalityPointInfo;

public class VitalityTask implements Runnable {
   private final Player _player;

   public VitalityTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player.isInsideZone(ZoneId.PEACE)) {
         if (this._player.getVitalityPoints() < PcStat.MAX_VITALITY_POINTS) {
            this._player.updateVitalityPoints((double)Config.RATE_RECOVERY_VITALITY_PEACE_ZONE, false, false);
            this._player.sendPacket(new ExVitalityPointInfo(this._player.getVitalityPoints()));
         }
      }
   }
}
