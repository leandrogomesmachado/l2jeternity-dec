package l2e.gameserver.model.zone.type;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.zone.ZoneType;

public class RespawnZone extends ZoneType {
   private final Map<Race, String> _raceRespawnPoint = new HashMap<>();

   public RespawnZone(int id) {
      super(id);
   }

   @Override
   protected void onEnter(Creature character) {
   }

   @Override
   protected void onExit(Creature character) {
   }

   public void addRaceRespawnPoint(String race, String point) {
      this._raceRespawnPoint.put(Race.valueOf(race), point);
   }

   public Map<Race, String> getAllRespawnPoints() {
      return this._raceRespawnPoint;
   }

   public String getRespawnPoint(Player activeChar) {
      return this._raceRespawnPoint.get(activeChar.getRace());
   }
}
