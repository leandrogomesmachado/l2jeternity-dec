package l2e.gameserver.network.serverpackets;

import l2e.gameserver.instancemanager.SoDManager;
import l2e.gameserver.instancemanager.SoIManager;
import l2e.gameserver.model.Location;

public class ExShowSeedMapInfo extends GameServerPacket {
   public static final ExShowSeedMapInfo STATIC_PACKET = new ExShowSeedMapInfo();
   private static final Location[] ENTRANCES = new Location[]{new Location(-246857, 251960, 4331, 1), new Location(-213770, 210760, 4400, 2)};

   private ExShowSeedMapInfo() {
   }

   @Override
   protected void writeImpl() {
      this.writeD(ENTRANCES.length);

      for(Location loc : ENTRANCES) {
         this.writeD(loc.getX());
         this.writeD(loc.getY());
         this.writeD(loc.getZ());
         switch(loc.getHeading()) {
            case 1:
               if (SoDManager.isAttackStage()) {
                  this.writeD(2771);
               } else {
                  this.writeD(2772);
               }
               break;
            case 2:
               this.writeD(SoIManager.getCurrentStage() + 2765);
         }
      }
   }
}
