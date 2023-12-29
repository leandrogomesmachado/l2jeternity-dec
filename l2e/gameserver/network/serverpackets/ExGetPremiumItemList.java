package l2e.gameserver.network.serverpackets;

import java.util.Map;
import java.util.Map.Entry;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.PremiumItemTemplate;

public class ExGetPremiumItemList extends GameServerPacket {
   private final Player _activeChar;
   private final Map<Integer, PremiumItemTemplate> _map;

   public ExGetPremiumItemList(Player activeChar) {
      this._activeChar = activeChar;
      this._map = this._activeChar.getPremiumItemList();
   }

   @Override
   protected void writeImpl() {
      if (!this._map.isEmpty()) {
         this.writeD(this._map.size());

         for(Entry<Integer, PremiumItemTemplate> entry : this._map.entrySet()) {
            PremiumItemTemplate item = entry.getValue();
            this.writeD(entry.getKey());
            this.writeD(this._activeChar.getObjectId());
            this.writeD(item.getId());
            this.writeQ(item.getCount());
            this.writeD(0);
            this.writeS(item.getSender());
         }
      } else {
         this.writeD(0);
      }
   }
}
