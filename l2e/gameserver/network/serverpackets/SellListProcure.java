package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.CropProcureTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;

public class SellListProcure extends GameServerPacket {
   private final Player _activeChar;
   private final long _money;
   private final Map<ItemInstance, Long> _sellList = new HashMap<>();
   private List<CropProcureTemplate> _procureList = new ArrayList<>();
   private final int _castle;

   public SellListProcure(Player player, int castleId) {
      this._money = player.getAdena();
      this._activeChar = player;
      this._castle = castleId;
      this._procureList = CastleManager.getInstance().getCastleById(this._castle).getCropProcure(0);

      for(CropProcureTemplate c : this._procureList) {
         ItemInstance item = this._activeChar.getInventory().getItemByItemId(c.getId());
         if (item != null && c.getAmount() > 0L) {
            this._sellList.put(item, c.getAmount());
         }
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeQ(this._money);
      this.writeD(0);
      this.writeH(this._sellList.size());

      for(ItemInstance item : this._sellList.keySet()) {
         this.writeH(item.getItem().getType1());
         this.writeD(item.getObjectId());
         this.writeD(item.getDisplayId());
         this.writeQ(this._sellList.get(item));
         this.writeH(item.getItem().getType2());
         this.writeH(0);
         this.writeQ(0L);
      }
   }
}
