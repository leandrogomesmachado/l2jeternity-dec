package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.data.parser.HennaParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Henna;

public class HennaEquipList extends GameServerPacket {
   private final Player _player;
   private final List<Henna> _hennaEquipList;

   public HennaEquipList(Player player) {
      this._player = player;
      this._hennaEquipList = HennaParser.getInstance().getHennaList(player.getClassId());
   }

   public HennaEquipList(Player player, List<Henna> list) {
      this._player = player;
      this._hennaEquipList = list;
   }

   @Override
   protected final void writeImpl() {
      this.writeQ(this._player.getAdena());
      this.writeD(3);
      this.writeD(this._hennaEquipList.size());

      for(Henna henna : this._hennaEquipList) {
         if (this._player.getInventory().getItemByItemId(henna.getDyeItemId()) != null) {
            this.writeD(henna.getDyeId());
            this.writeD(henna.getDyeItemId());
            this.writeQ((long)henna.getWearCount());
            this.writeQ((long)henna.getWearFee());
            this.writeD(henna.isAllowedClass(this._player.getClassId()) ? 1 : 0);
         }
      }
   }
}
