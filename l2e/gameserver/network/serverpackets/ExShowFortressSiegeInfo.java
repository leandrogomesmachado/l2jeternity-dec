package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.spawn.SpawnFortSiege;

public class ExShowFortressSiegeInfo extends GameServerPacket {
   private final int _fortId;
   private final int _size;
   private final Fort _fort;
   private int _csize;
   private final int _csize2;

   public ExShowFortressSiegeInfo(Fort fort) {
      this._fort = fort;
      this._fortId = fort.getId();
      this._size = fort.getFortSize();
      List<SpawnFortSiege> commanders = FortSiegeManager.getInstance().getCommanderSpawnList(this._fortId);
      if (commanders != null) {
         this._csize = commanders.size();
      }

      this._csize2 = this._fort.getSiege().getCommanders().size();
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._fortId);
      this.writeD(this._size);
      if (this._csize > 0) {
         switch(this._csize) {
            case 3:
               switch(this._csize2) {
                  case 0:
                     this.writeD(3);
                     return;
                  case 1:
                     this.writeD(2);
                     return;
                  case 2:
                     this.writeD(1);
                     return;
                  case 3:
                     this.writeD(0);
                     return;
                  default:
                     return;
               }
            case 4:
               switch(this._csize2) {
                  case 0:
                     this.writeD(5);
                     break;
                  case 1:
                     this.writeD(4);
                     break;
                  case 2:
                     this.writeD(3);
                     break;
                  case 3:
                     this.writeD(2);
                     break;
                  case 4:
                     this.writeD(1);
               }
         }
      } else {
         for(int i = 0; i < this._size; ++i) {
            this.writeD(0);
         }
      }
   }
}
