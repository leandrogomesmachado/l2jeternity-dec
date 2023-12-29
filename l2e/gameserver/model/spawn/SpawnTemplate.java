package l2e.gameserver.model.spawn;

import java.util.ArrayList;
import java.util.List;

public class SpawnTemplate {
   private final String _periodOfDay;
   private final int _count;
   private final int _respawn;
   private final int _respawnRandom;
   private final List<SpawnNpcInfo> _npcList = new ArrayList<>(1);
   private final List<SpawnRange> _spawnRangeList = new ArrayList<>(1);

   public SpawnTemplate(String periodOfDay, int count, int respawn, int respawnRandom) {
      this._periodOfDay = periodOfDay;
      this._count = count;
      this._respawn = respawn;
      this._respawnRandom = respawnRandom;
   }

   public void addSpawnRange(SpawnRange range) {
      this._spawnRangeList.add(range);
   }

   public SpawnRange getSpawnRange(int index) {
      return this._spawnRangeList.get(index);
   }

   public void addNpc(SpawnNpcInfo info) {
      this._npcList.add(info);
   }

   public SpawnNpcInfo getNpcId(int index) {
      return this._npcList.get(index);
   }

   public int getNpcSize() {
      return this._npcList.size();
   }

   public int getSpawnRangeSize() {
      return this._spawnRangeList.size();
   }

   public int getCount() {
      return this._count;
   }

   public int getRespawn() {
      return this._respawn;
   }

   public int getRespawnRandom() {
      return this._respawnRandom;
   }

   public String getPeriodOfDay() {
      return this._periodOfDay;
   }

   public List<SpawnRange> getSpawnRangeList() {
      return this._spawnRangeList;
   }
}
