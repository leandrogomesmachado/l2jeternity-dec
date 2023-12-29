package l2e.gameserver.model.entity.events.model.template;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.commons.util.Rnd;
import l2e.gameserver.data.parser.FightEventMapParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.model.FightEventManager;

public class FightEventGameRoom {
   private final FightEventMap _map;
   private final AbstractFightEvent _event;
   private final int _roomMaxPlayers;
   private final int _teamsCount;
   private final Map<Integer, Player> _players = new ConcurrentHashMap<>();

   public FightEventGameRoom(AbstractFightEvent event) {
      this._event = event;
      String eventName = getChangedEventName(event);
      this._map = Rnd.get(FightEventMapParser.getInstance().getMapsForEvent(eventName));
      this._roomMaxPlayers = this._map.getMaxAllPlayers();
      if (event.isTeamed()) {
         this._teamsCount = Rnd.get(this._map.getTeamCount());
      } else {
         this._teamsCount = 0;
      }
   }

   public void leaveRoom(Player player) {
      this._players.remove(player.getObjectId());
      player.setFightEventGameRoom(null);
   }

   public int getMaxPlayers() {
      return this._roomMaxPlayers;
   }

   public int getTeamsCount() {
      return this._teamsCount;
   }

   public int getSlotsLeft() {
      return this.getMaxPlayers() - this.getPlayersCount();
   }

   public AbstractFightEvent getGame() {
      return this._event;
   }

   public int getPlayersCount() {
      return this._players.size();
   }

   public FightEventMap getMap() {
      return this._map;
   }

   public Collection<Player> getAllPlayers() {
      return this._players.values();
   }

   public synchronized void addAlonePlayer(Player player) {
      player.setFightEventGameRoom(this);
      this.addPlayerToTeam(player);
   }

   public boolean containsPlayer(Player player) {
      return this._players.containsKey(player.getObjectId());
   }

   private void addPlayerToTeam(Player player) {
      this._players.put(player.getObjectId(), player);
   }

   public void cleanUp() {
      this._players.clear();
   }

   public static FightEventManager.CLASSES getPlayerClassGroup(Player player) {
      FightEventManager.CLASSES classType = null;

      for(FightEventManager.CLASSES iClassType : FightEventManager.CLASSES.values()) {
         for(ClassId id : iClassType.getClasses()) {
            if (id == player.getClassId()) {
               classType = iClassType;
            }
         }
      }

      return classType;
   }

   private static String getChangedEventName(AbstractFightEvent event) {
      String eventName = event.getClass().getSimpleName();
      return eventName.substring(0, eventName.length() - 5);
   }
}
