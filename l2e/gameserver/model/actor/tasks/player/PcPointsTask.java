package l2e.gameserver.model.actor.tasks.player;

import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPCCafePointInfo;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class PcPointsTask implements Runnable {
   private final Player _player;

   public PcPointsTask(Player player) {
      this._player = player;
   }

   @Override
   public void run() {
      if (this._player.getLevel() > Config.PC_BANG_MIN_LEVEL && this._player.isOnline() && !this._player.isInOfflineMode()) {
         if (this._player.getPcBangPoints() >= Config.MAX_PC_BANG_POINTS) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_MAXMIMUM_ACCUMULATION_ALLOWED_OF_PC_CAFE_POINTS_HAS_BEEN_EXCEEDED);
            this._player.sendPacket(sm);
            return;
         }

         int _points = this._player.hasPremiumBonus()
            ? Rnd.get(Config.PC_BANG_POINTS_PREMIUM_MIN, Config.PC_BANG_POINTS_PREMIUM_MAX)
            : Rnd.get(Config.PC_BANG_POINTS_MIN, Config.PC_BANG_POINTS_MAX);
         boolean doublepoint = false;
         SystemMessage sm = null;
         if (_points > 0) {
            if (Config.ENABLE_DOUBLE_PC_BANG_POINTS && Rnd.get(100) < Config.DOUBLE_PC_BANG_POINTS_CHANCE) {
               _points *= 2;
               sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_PCPOINT_DOUBLE);
               this._player.broadcastPacket(new MagicSkillUse(this._player, this._player, 2023, 1, 100, 0));
               doublepoint = true;
            } else {
               sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_PC_CAFE_POINTS);
            }

            if (this._player.getPcBangPoints() + _points > Config.MAX_PC_BANG_POINTS) {
               _points = Config.MAX_PC_BANG_POINTS - this._player.getPcBangPoints();
            }

            sm.addNumber(_points);
            this._player.sendPacket(sm);
            if (Config.PC_POINT_ID < 0) {
               this._player.setPcBangPoints(this._player.getPcBangPoints() + _points);
            } else {
               this._player.setPcBangPoints(this._player.getPcBangPoints() + _points);
               this._player.addItem("PcPoints", Config.PC_POINT_ID, (long)_points, this._player, true);
            }

            this._player.sendPacket(new ExPCCafePointInfo(this._player.getPcBangPoints(), _points, true, doublepoint, 1));
         }
      }
   }
}
