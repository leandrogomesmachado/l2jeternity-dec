package l2e.gameserver.model.spawn;

import l2e.gameserver.model.Location;

public interface SpawnRange {
   Location getRandomLoc(int var1, boolean var2);
}
