package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.data.parser.ManorParser;
import l2e.gameserver.model.actor.templates.CropProcureTemplate;

public class ExShowCropInfo extends GameServerPacket {
   private final List<CropProcureTemplate> _crops;
   private final int _manorId;

   public ExShowCropInfo(int manorId, List<CropProcureTemplate> crops) {
      this._manorId = manorId;
      this._crops = crops;
   }

   @Override
   protected void writeImpl() {
      this.writeC(0);
      this.writeD(this._manorId);
      this.writeD(0);
      if (this._crops == null) {
         this.writeD(0);
      } else {
         this.writeD(this._crops.size());

         for(CropProcureTemplate crop : this._crops) {
            this.writeD(crop.getId());
            this.writeQ(crop.getAmount());
            this.writeQ(crop.getStartAmount());
            this.writeQ(crop.getPrice());
            this.writeC(crop.getReward());
            this.writeD(ManorParser.getInstance().getSeedLevelByCrop(crop.getId()));
            this.writeC(1);
            this.writeD(ManorParser.getInstance().getRewardItem(crop.getId(), 1));
            this.writeC(1);
            this.writeD(ManorParser.getInstance().getRewardItem(crop.getId(), 2));
         }
      }
   }
}
