package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import l2e.commons.collections.MultiValueSet;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.entity.events.model.template.FightEventMap;
import l2e.gameserver.model.zone.ZoneType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class FightEventMapParser extends DocumentParser {
   private static final Logger _log = Logger.getLogger(FightEventMapParser.class.getName());
   private final List<FightEventMap> _maps = new ArrayList<>();

   protected FightEventMapParser() {
      this.load();
   }

   public void reload() {
      this.load();
   }

   @Override
   public final void load() {
      this._maps.clear();
      this.parseDirectory("data/stats/events/maps", false);
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._maps.size() + " map templates.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node c = this.getCurrentDocument().getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("list".equalsIgnoreCase(c.getNodeName())) {
            for(Node map = c.getFirstChild(); map != null; map = map.getNextSibling()) {
               if ("map".equalsIgnoreCase(map.getNodeName())) {
                  NamedNodeMap attrs = map.getAttributes();
                  Map<Integer, Location[]> teamSpawns = null;
                  Map<Integer, Map<String, ZoneType>> territories = null;
                  Map<Integer, Map<Integer, Location[]>> npcWaypath = null;
                  Location[] keyLocations = null;
                  Location[] defLocations = null;
                  String name = attrs.getNamedItem("name").getNodeValue();
                  MultiValueSet<String> set = new MultiValueSet<>();
                  set.set("name", name);

                  for(Node par = map.getFirstChild(); par != null; par = par.getNextSibling()) {
                     if ("parameter".equalsIgnoreCase(par.getNodeName())) {
                        attrs = par.getAttributes();
                        set.set(attrs.getNamedItem("name").getNodeValue(), attrs.getNamedItem("value").getNodeValue());
                     } else if ("objects".equalsIgnoreCase(par.getNodeName())) {
                        attrs = par.getAttributes();
                        String objectsName = attrs.getNamedItem("name").getNodeValue();
                        int team = attrs.getNamedItem("team") != null ? Integer.parseInt(attrs.getNamedItem("team").getNodeValue()) : -1;
                        int index = attrs.getNamedItem("index") != null ? Integer.parseInt(attrs.getNamedItem("index").getNodeValue()) : -1;
                        if (objectsName.equals("teamSpawns")) {
                           if (teamSpawns == null) {
                              teamSpawns = new HashMap<>();
                           }

                           teamSpawns.put(team, this.parseLocations(par));
                        } else if (objectsName.equals("territory")) {
                           if (territories == null) {
                              territories = new HashMap<>();
                           }

                           territories.put(team, this.parseTerritory(par));
                        } else if (objectsName.equals("npcWaypath")) {
                           if (npcWaypath == null) {
                              npcWaypath = new HashMap<>();
                           }

                           if (npcWaypath.get(team) == null) {
                              npcWaypath.put(team, new HashMap<>());
                           }

                           npcWaypath.get(team).put(index, this.parseLocations(par));
                        } else if (objectsName.equals("keyLocations")) {
                           keyLocations = this.parseLocations(par);
                        } else if (objectsName.equals("defLocations")) {
                           defLocations = this.parseLocations(par);
                        }
                     }
                  }

                  this.addMap(new FightEventMap(set, teamSpawns, territories, npcWaypath, keyLocations, defLocations));
               }
            }
         }
      }
   }

   private Location[] parseLocations(Node node) {
      List<Location> locs = new ArrayList<>();

      for(Node loc = node.getFirstChild(); loc != null; loc = loc.getNextSibling()) {
         if ("point".equalsIgnoreCase(loc.getNodeName())) {
            NamedNodeMap attrs = loc.getAttributes();
            int x = Integer.parseInt(attrs.getNamedItem("x").getNodeValue());
            int y = Integer.parseInt(attrs.getNamedItem("y").getNodeValue());
            int z = Integer.parseInt(attrs.getNamedItem("z").getNodeValue());
            locs.add(new Location(x, y, z));
         }
      }

      Location[] locArray = new Location[locs.size()];

      for(int i = 0; i < locs.size(); ++i) {
         locArray[i] = locs.get(i);
      }

      return locArray;
   }

   private Map<String, ZoneType> parseTerritory(Node node) {
      Map<String, ZoneType> territories = new HashMap<>();

      for(Node zone = node.getFirstChild(); zone != null; zone = zone.getNextSibling()) {
         if ("zone".equalsIgnoreCase(zone.getNodeName())) {
            NamedNodeMap attrs = zone.getAttributes();
            int zoneId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
            ZoneType type = ZoneManager.getInstance().getZoneById(zoneId);
            if (type != null) {
               territories.put(type.getName(), type);
            } else {
               _log.warning(this.getClass().getSimpleName() + ": Unable to find zoneId: " + zoneId + "");
            }
         }
      }

      return territories;
   }

   public void addMap(FightEventMap map) {
      this._maps.add(map);
   }

   public List<FightEventMap> getMapsForEvent(String eventName) {
      List<FightEventMap> maps = new ArrayList<>();

      for(FightEventMap map : this._maps) {
         for(String possibleName : map.getEvents()) {
            if (possibleName.equalsIgnoreCase(eventName)) {
               maps.add(map);
            }
         }
      }

      return maps;
   }

   public List<Integer> getTeamPossibilitiesForEvent(String eventName) {
      List<FightEventMap> allMaps = this.getMapsForEvent(eventName);
      List<Integer> teams = new ArrayList<>();

      for(FightEventMap map : allMaps) {
         for(int possibility : map.getTeamCount()) {
            if (!teams.contains(possibility)) {
               teams.add(possibility);
            }
         }
      }

      Collections.sort(teams);
      return teams;
   }

   public int getMinPlayersForEvent(String eventName) {
      List<FightEventMap> allMaps = this.getMapsForEvent(eventName);
      int minPlayers = Integer.MAX_VALUE;

      for(FightEventMap map : allMaps) {
         int newMin = map.getMinAllPlayers();
         if (newMin < minPlayers) {
            minPlayers = newMin;
         }
      }

      return minPlayers;
   }

   public int getMaxPlayersForEvent(String eventName) {
      List<FightEventMap> allMaps = this.getMapsForEvent(eventName);
      int maxPlayers = 0;

      for(FightEventMap map : allMaps) {
         int newMax = map.getMaxAllPlayers();
         if (newMax > maxPlayers) {
            maxPlayers = newMax;
         }
      }

      return maxPlayers;
   }

   public static FightEventMapParser getInstance() {
      return FightEventMapParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final FightEventMapParser _instance = new FightEventMapParser();
   }
}
