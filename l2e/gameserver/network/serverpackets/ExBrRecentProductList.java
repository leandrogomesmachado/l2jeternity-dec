package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.data.parser.ProductItemParser;
import l2e.gameserver.model.ProductItem;

public class ExBrRecentProductList extends GameServerPacket {
   List<ProductItem> list;

   public ExBrRecentProductList(int objId) {
      this.list = ProductItemParser.getInstance().getRecentListByOID(objId);
   }

   @Override
   protected void writeImpl() {
      this.writeD(this.list.size());

      for(ProductItem template : this.list) {
         this.writeD(template.getProductId());
         this.writeH(template.getCategory());
         this.writeD(template.getPoints());
         this.writeD(template.getTabId());
         this.writeD((int)(template.getStartTimeSale() / 1000L));
         this.writeD((int)(template.getEndTimeSale() / 1000L));
         this.writeC(template.getDaysOfWeek());
         this.writeC(template.getStartHour());
         this.writeC(template.getStartMin());
         this.writeC(template.getEndHour());
         this.writeC(template.getEndMin());
         this.writeD(template.getStock());
         this.writeD(template.getTotal());
      }
   }
}
