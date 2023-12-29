package l2e.gameserver.network.serverpackets;

import java.util.List;

public class ExGetBossRecord extends GameServerPacket {
   private final List<ExGetBossRecord.BossRecordInfo> _bossRecordInfo;
   private final int _ranking;
   private final int _totalPoints;

   public ExGetBossRecord(int ranking, int totalScore, List<ExGetBossRecord.BossRecordInfo> bossRecordInfo) {
      this._ranking = ranking;
      this._totalPoints = totalScore;
      this._bossRecordInfo = bossRecordInfo;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._ranking);
      this.writeD(this._totalPoints);
      this.writeD(this._bossRecordInfo.size());

      for(ExGetBossRecord.BossRecordInfo w : this._bossRecordInfo) {
         this.writeD(w._bossId);
         this.writeD(w._points);
         this.writeD(w._unk1);
      }
   }

   public static class BossRecordInfo {
      public int _bossId;
      public int _points;
      public int _unk1;

      public BossRecordInfo(int bossId, int points, int unk1) {
         this._bossId = bossId;
         this._points = points;
         this._unk1 = unk1;
      }
   }
}
