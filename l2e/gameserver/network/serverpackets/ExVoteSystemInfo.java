package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;

public class ExVoteSystemInfo extends GameServerPacket {
   private final int _recHave;
   private final int _recLeft;
   private final int _time;
   private final int _bonus;
   private final int _paused;

   public ExVoteSystemInfo(Player player) {
      this._recHave = player.getRecommendation().getRecomHave();
      this._recLeft = player.getRecommendation().getRecomLeft();
      this._time = player.getRecommendation().isHourglassBonusActive() > 0L
         ? (int)player.getRecommendation().isHourglassBonusActive()
         : player.getRecommendation().getRecomTimeLeft();
      this._bonus = player.getRecommendation().getRecomExpBonus();
      this._paused = this._time != 0 && !player.getRecommendation().isRecBonusActive() && player.getRecommendation().isHourglassBonusActive() <= 0L ? 1 : 0;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._recLeft);
      this.writeD(this._recHave);
      this.writeD(this._time);
      this.writeD(this._bonus);
      this.writeD(this._paused);
   }
}
