package l2e.gameserver.model.entity.events.model.template;

import l2e.gameserver.model.spawn.SpawnTerritory;

public class WorldEventTerritory {
   private final String _name;
   private final SpawnTerritory _territory;

   public WorldEventTerritory(String name, SpawnTerritory territory) {
      this._name = name;
      this._territory = territory;
   }

   public String getName() {
      return this._name;
   }

   public SpawnTerritory getTerritory() {
      return this._territory;
   }
}
