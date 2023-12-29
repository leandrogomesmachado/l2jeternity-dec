package l2e.gameserver.network.serverpackets;

import java.util.Collection;
import l2e.gameserver.data.parser.ProductItemParser;
import l2e.gameserver.model.ProductItem;

public class ExBrProductList extends GameServerPacket {
   @Override
   protected void writeImpl() {
      Collection<ProductItem> items = ProductItemParser.getInstance().getAllItems();
      this.writeD(items.size());

      for(ProductItem template : items) {
         if (System.currentTimeMillis() >= template.getStartTimeSale() && System.currentTimeMillis() <= template.getEndTimeSale()) {
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
}
