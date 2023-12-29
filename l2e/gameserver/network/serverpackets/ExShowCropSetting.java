package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.data.parser.ManorParser;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.actor.templates.CropProcureTemplate;
import l2e.gameserver.model.entity.Castle;

public class ExShowCropSetting extends GameServerPacket {
   private final int _manorId;
   private final int _count;
   private final long[] _cropData;

   public ExShowCropSetting(int manorId) {
      this._manorId = manorId;
      Castle c = CastleManager.getInstance().getCastleById(this._manorId);
      List<Integer> crops = ManorParser.getInstance().getCropsForCastle(this._manorId);
      this._count = crops.size();
      this._cropData = new long[this._count * 14];
      int i = 0;

      for(int cr : crops) {
         this._cropData[i * 14 + 0] = (long)cr;
         this._cropData[i * 14 + 1] = (long)ManorParser.getInstance().getSeedLevelByCrop(cr);
         this._cropData[i * 14 + 2] = (long)ManorParser.getInstance().getRewardItem(cr, 1);
         this._cropData[i * 14 + 3] = (long)ManorParser.getInstance().getRewardItem(cr, 2);
         this._cropData[i * 14 + 4] = (long)ManorParser.getInstance().getCropPuchaseLimit(cr);
         this._cropData[i * 14 + 5] = 0L;
         this._cropData[i * 14 + 6] = (long)(ManorParser.getInstance().getCropBasicPrice(cr) * 60 / 100);
         this._cropData[i * 14 + 7] = (long)(ManorParser.getInstance().getCropBasicPrice(cr) * 10);
         CropProcureTemplate cropPr = c.getCrop(cr, 0);
         if (cropPr != null) {
            this._cropData[i * 14 + 8] = cropPr.getStartAmount();
            this._cropData[i * 14 + 9] = cropPr.getPrice();
            this._cropData[i * 14 + 10] = (long)cropPr.getReward();
         } else {
            this._cropData[i * 14 + 8] = 0L;
            this._cropData[i * 14 + 9] = 0L;
            this._cropData[i * 14 + 10] = 0L;
         }

         cropPr = c.getCrop(cr, 1);
         if (cropPr != null) {
            this._cropData[i * 14 + 11] = cropPr.getStartAmount();
            this._cropData[i * 14 + 12] = cropPr.getPrice();
            this._cropData[i * 14 + 13] = (long)cropPr.getReward();
         } else {
            this._cropData[i * 14 + 11] = 0L;
            this._cropData[i * 14 + 12] = 0L;
            this._cropData[i * 14 + 13] = 0L;
         }

         ++i;
      }
   }

   @Override
   public void writeImpl() {
      this.writeD(this._manorId);
      this.writeD(this._count);

      for(int i = 0; i < this._count; ++i) {
         this.writeD((int)this._cropData[i * 14 + 0]);
         this.writeD((int)this._cropData[i * 14 + 1]);
         this.writeC(1);
         this.writeD((int)this._cropData[i * 14 + 2]);
         this.writeC(1);
         this.writeD((int)this._cropData[i * 14 + 3]);
         this.writeD((int)this._cropData[i * 14 + 4]);
         this.writeD((int)this._cropData[i * 14 + 5]);
         this.writeD((int)this._cropData[i * 14 + 6]);
         this.writeD((int)this._cropData[i * 14 + 7]);
         this.writeQ(this._cropData[i * 14 + 8]);
         this.writeQ(this._cropData[i * 14 + 9]);
         this.writeC((int)this._cropData[i * 14 + 10]);
         this.writeQ(this._cropData[i * 14 + 11]);
         this.writeQ(this._cropData[i * 14 + 12]);
         this.writeC((int)this._cropData[i * 14 + 13]);
      }
   }
}
