package l2e.gameserver.model.holders;

import l2e.gameserver.model.Location;

public class SpawnsHolder {
   protected final int npcId;
   protected final Location loc;

   public SpawnsHolder(int _npcId, Location _spawnLoc) {
      this.npcId = _npcId;
      this.loc = _spawnLoc;
   }

   public int getX() {
      return this.loc.getX();
   }

   public int getY() {
      return this.loc.getY();
   }

   public int getZ() {
      return this.loc.getZ();
   }

   public int getHeading() {
      return this.loc.getHeading();
   }
}
