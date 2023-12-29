package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.data.parser.ManorParser;

public class ExShowManorDefaultInfo extends GameServerPacket {
   private List<Integer> _crops = null;

   public ExShowManorDefaultInfo() {
      this._crops = ManorParser.getInstance().getAllCrops();
   }

   @Override
   protected void writeImpl() {
      this.writeC(0);
      this.writeD(this._crops.size());

      for(int cropId : this._crops) {
         this.writeD(cropId);
         this.writeD(ManorParser.getInstance().getSeedLevelByCrop(cropId));
         this.writeD(ManorParser.getInstance().getSeedBasicPriceByCrop(cropId));
         this.writeD(ManorParser.getInstance().getCropBasicPrice(cropId));
         this.writeC(1);
         this.writeD(ManorParser.getInstance().getRewardItem(cropId, 1));
         this.writeC(1);
         this.writeD(ManorParser.getInstance().getRewardItem(cropId, 2));
      }
   }
}
