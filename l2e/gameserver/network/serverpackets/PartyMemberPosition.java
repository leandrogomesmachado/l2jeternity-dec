package l2e.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;

public class PartyMemberPosition extends GameServerPacket {
   private final Map<Integer, Location> _locations = new HashMap<>();

   public PartyMemberPosition(Party party) {
      this.reuse(party);
   }

   public void reuse(Party party) {
      this._locations.clear();

      for(Player member : party.getMembers()) {
         if (member != null) {
            this._locations.put(member.getObjectId(), member.getLocation());
         }
      }
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._locations.size());

      for(Entry<Integer, Location> entry : this._locations.entrySet()) {
         Location loc = entry.getValue();
         this.writeD(entry.getKey());
         this.writeD(loc.getX());
         this.writeD(loc.getY());
         this.writeD(loc.getZ());
      }
   }
}
