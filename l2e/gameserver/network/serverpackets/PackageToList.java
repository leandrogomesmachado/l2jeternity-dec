package l2e.gameserver.network.serverpackets;

import java.util.Map;
import java.util.Map.Entry;

public class PackageToList extends GameServerPacket {
   private final Map<Integer, String> _players;

   public PackageToList(Map<Integer, String> chars) {
      this._players = chars;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._players.size());

      for(Entry<Integer, String> entry : this._players.entrySet()) {
         this.writeD(entry.getKey());
         this.writeS(entry.getValue());
      }
   }
}
