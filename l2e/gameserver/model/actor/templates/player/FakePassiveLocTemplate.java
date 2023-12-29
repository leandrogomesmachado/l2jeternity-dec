package l2e.gameserver.model.actor.templates.player;

import java.util.List;
import l2e.gameserver.model.spawn.SpawnTerritory;

public class FakePassiveLocTemplate {
   private final int _id;
   private final int _amount;
   private final SpawnTerritory _territory;
   private final int _minLvl;
   private final int _maxLvl;
   private List<Integer> _classes = null;
   private final long _minDelay;
   private final long _maxDelay;
   private final long _minRespawn;
   private final long _maxRepsawn;
   private int _currectAmount;

   public FakePassiveLocTemplate(
      int id,
      int amount,
      SpawnTerritory territory,
      List<Integer> classes,
      int minLvl,
      int maxLvl,
      long minDelay,
      long maxDelay,
      long minRespawn,
      long maxRepsawn
   ) {
      this._id = id;
      this._amount = amount;
      this._territory = territory;
      this._classes = classes;
      this._minLvl = minLvl;
      this._maxLvl = maxLvl;
      this._minDelay = minDelay;
      this._maxDelay = maxDelay;
      this._minRespawn = minRespawn;
      this._maxRepsawn = maxRepsawn;
      this._currectAmount = 0;
   }

   public int getId() {
      return this._id;
   }

   public int getAmount() {
      return this._amount;
   }

   public SpawnTerritory getTerritory() {
      return this._territory;
   }

   public List<Integer> getClasses() {
      return this._classes;
   }

   public int getMinLvl() {
      return this._minLvl;
   }

   public int getMaxLvl() {
      return this._maxLvl;
   }

   public void setCurrentAmount(int val) {
      this._currectAmount = val;
   }

   public int getCurrentAmount() {
      return this._currectAmount;
   }

   public long getMinDelay() {
      return this._minDelay;
   }

   public long getMaxDelay() {
      return this._maxDelay;
   }

   public long getMinRespawn() {
      return this._minRespawn;
   }

   public long getMaxRespawn() {
      return this._maxRepsawn;
   }
}
