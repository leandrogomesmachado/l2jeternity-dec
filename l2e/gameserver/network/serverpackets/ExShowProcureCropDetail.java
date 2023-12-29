package l2e.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.actor.templates.CropProcureTemplate;
import l2e.gameserver.model.entity.Castle;

public class ExShowProcureCropDetail extends GameServerPacket {
   private final int _cropId;
   private final Map<Integer, CropProcureTemplate> _castleCrops;

   public ExShowProcureCropDetail(int cropId) {
      this._cropId = cropId;
      this._castleCrops = new HashMap<>();

      for(Castle c : CastleManager.getInstance().getCastles()) {
         CropProcureTemplate cropItem = c.getCrop(this._cropId, 0);
         if (cropItem != null && cropItem.getAmount() > 0L) {
            this._castleCrops.put(c.getId(), cropItem);
         }
      }
   }

   @Override
   public void writeImpl() {
      this.writeD(this._cropId);
      this.writeD(this._castleCrops.size());

      for(int manorId : this._castleCrops.keySet()) {
         CropProcureTemplate crop = this._castleCrops.get(manorId);
         this.writeD(manorId);
         this.writeQ(crop.getAmount());
         this.writeQ(crop.getPrice());
         this.writeC(crop.getReward());
      }
   }
}
