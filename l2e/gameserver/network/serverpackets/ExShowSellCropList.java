package l2e.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.data.parser.ManorParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.CropProcureTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;

public class ExShowSellCropList extends GameServerPacket {
   private int _manorId = 1;
   private final Map<Integer, ItemInstance> _cropsItems = new HashMap<>();
   private final Map<Integer, CropProcureTemplate> _castleCrops = new HashMap<>();

   public ExShowSellCropList(Player player, int manorId, List<CropProcureTemplate> crops) {
      this._manorId = manorId;

      for(int cropId : ManorParser.getInstance().getAllCrops()) {
         ItemInstance item = player.getInventory().getItemByItemId(cropId);
         if (item != null) {
            this._cropsItems.put(cropId, item);
         }
      }

      for(CropProcureTemplate crop : crops) {
         if (this._cropsItems.containsKey(crop.getId()) && crop.getAmount() > 0L) {
            this._castleCrops.put(crop.getId(), crop);
         }
      }
   }

   @Override
   public void writeImpl() {
      this.writeD(this._manorId);
      this.writeD(this._cropsItems.size());

      for(ItemInstance item : this._cropsItems.values()) {
         this.writeD(item.getObjectId());
         this.writeD(item.getDisplayId());
         this.writeD(ManorParser.getInstance().getSeedLevelByCrop(item.getId()));
         this.writeC(1);
         this.writeD(ManorParser.getInstance().getRewardItem(item.getId(), 1));
         this.writeC(1);
         this.writeD(ManorParser.getInstance().getRewardItem(item.getId(), 2));
         if (this._castleCrops.containsKey(item.getId())) {
            CropProcureTemplate crop = this._castleCrops.get(item.getId());
            this.writeD(this._manorId);
            this.writeQ(crop.getAmount());
            this.writeQ(crop.getPrice());
            this.writeC(crop.getReward());
         } else {
            this.writeD(-1);
            this.writeQ(0L);
            this.writeQ(0L);
            this.writeC(0);
         }

         this.writeQ(item.getCount());
      }
   }
}
