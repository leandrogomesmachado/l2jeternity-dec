package l2e.gameserver.network.serverpackets;

import java.util.Collections;
import java.util.Set;

public class ExMpccPartymasterList extends GameServerPacket {
   private Set<String> _members = Collections.emptySet();

   public ExMpccPartymasterList(Set<String> s) {
      this._members = s;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._members.size());

      for(String t : this._members) {
         this.writeS(t);
      }
   }
}
