package l2e.gameserver.model.entity.events.model.template;

import java.util.List;
import java.util.Map;
import l2e.commons.time.cron.SchedulingPattern;
import l2e.gameserver.model.stats.StatsSet;

public class WorldEventTemplate {
   private final int _id;
   private final String _name;
   private final boolean _activate;
   private final SchedulingPattern _timePattern;
   private final int _period;
   private final Map<Integer, List<WorldEventReward>> _variantRequests;
   private final Map<Integer, List<WorldEventReward>> _variantRewards;
   private final Map<Integer, List<WorldEventReward>> _variantRndRewards;
   private final List<WorldEventDrop> _dropList;
   private final List<WorldEventSpawn> _spawnList;
   private final List<WorldEventLocation> _locations;
   private final List<WorldEventTerritory> _territories;
   private final StatsSet _params;

   public WorldEventTemplate(
      int id,
      String name,
      boolean activate,
      SchedulingPattern timePattern,
      int period,
      List<WorldEventDrop> dropList,
      Map<Integer, List<WorldEventReward>> variantRequests,
      Map<Integer, List<WorldEventReward>> variantRewards,
      Map<Integer, List<WorldEventReward>> variantRndRewards,
      List<WorldEventSpawn> spawnList,
      List<WorldEventLocation> locations,
      List<WorldEventTerritory> territories,
      StatsSet params
   ) {
      this._id = id;
      this._name = name;
      this._activate = activate;
      this._timePattern = timePattern;
      this._period = period;
      this._dropList = dropList;
      this._variantRequests = variantRequests;
      this._variantRewards = variantRewards;
      this._variantRndRewards = variantRndRewards;
      this._spawnList = spawnList;
      this._locations = locations;
      this._territories = territories;
      this._params = params;
   }

   public int getId() {
      return this._id;
   }

   public String getName() {
      return this._name;
   }

   public SchedulingPattern getTimePattern() {
      return this._timePattern;
   }

   public int getPeriod() {
      return this._period;
   }

   public List<WorldEventDrop> getDropList() {
      return this._dropList;
   }

   public Map<Integer, List<WorldEventReward>> getVariantRequests() {
      return this._variantRequests;
   }

   public Map<Integer, List<WorldEventReward>> getVariantRewards() {
      return this._variantRewards;
   }

   public Map<Integer, List<WorldEventReward>> getVariantRandomRewards() {
      return this._variantRndRewards;
   }

   public List<WorldEventSpawn> getSpawnList() {
      return this._spawnList;
   }

   public List<WorldEventLocation> getLocations() {
      return this._locations;
   }

   public List<WorldEventTerritory> getTerritories() {
      return this._territories;
   }

   public boolean isActivated() {
      return this._activate;
   }

   public StatsSet getParams() {
      return this._params;
   }
}
