package l2e.gameserver.network.serverpackets;

import java.util.List;

public class ExCursedWeaponList extends GameServerPacket {
   private final List<Integer> _cursedWeaponIds;

   public ExCursedWeaponList(List<Integer> cursedWeaponIds) {
      this._cursedWeaponIds = cursedWeaponIds;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._cursedWeaponIds.size());

      for(int i : this._cursedWeaponIds) {
         this.writeD(i);
      }
   }
}
