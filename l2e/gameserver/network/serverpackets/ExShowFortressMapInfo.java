package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.instancemanager.FortSiegeManager;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.spawn.SpawnFortSiege;
import l2e.gameserver.model.spawn.Spawner;

public class ExShowFortressMapInfo extends GameServerPacket {
   private final Fort _fortress;

   public ExShowFortressMapInfo(Fort fortress) {
      this._fortress = fortress;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._fortress.getId());
      this.writeD(this._fortress.getSiege().getIsInProgress() ? 1 : 0);
      this.writeD(this._fortress.getFortSize());
      List<SpawnFortSiege> commanders = FortSiegeManager.getInstance().getCommanderSpawnList(this._fortress.getId());
      if (commanders != null && commanders.size() != 0 && this._fortress.getSiege().getIsInProgress()) {
         switch(commanders.size()) {
            case 3:
               for(SpawnFortSiege spawn : commanders) {
                  if (this.isSpawned(spawn.getId())) {
                     this.writeD(0);
                  } else {
                     this.writeD(1);
                  }
               }
               break;
            case 4:
               int count = 0;

               for(SpawnFortSiege spawn : commanders) {
                  if (++count == 4) {
                     this.writeD(1);
                  }

                  if (this.isSpawned(spawn.getId())) {
                     this.writeD(0);
                  } else {
                     this.writeD(1);
                  }
               }
         }
      } else {
         for(int i = 0; i < this._fortress.getFortSize(); ++i) {
            this.writeD(0);
         }
      }
   }

   private boolean isSpawned(int npcId) {
      boolean ret = false;

      for(Spawner spawn : this._fortress.getSiege().getCommanders()) {
         if (spawn.getId() == npcId) {
            ret = true;
            break;
         }
      }

      return ret;
   }
}
