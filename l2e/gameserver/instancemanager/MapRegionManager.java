package l2e.gameserver.instancemanager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.SiegeFlagInstance;
import l2e.gameserver.model.actor.templates.MapRegionTemplate;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.ClanHall;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.zone.type.ClanHallZone;
import l2e.gameserver.model.zone.type.RespawnZone;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class MapRegionManager extends DocumentParser {
   private static final Map<String, MapRegionTemplate> _regions = new HashMap<>();
   private static final String defaultRespawn = "talking_island_town";

   protected MapRegionManager() {
      this.load();
   }

   @Override
   public void load() {
      _regions.clear();
      this.parseDirectory(new File(Config.DATAPACK_ROOT, "data/stats/regions/mapregion/"));
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + _regions.size() + " map regions.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("region".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap attrs = d.getAttributes();
                  String name = attrs.getNamedItem("name").getNodeValue();
                  String town = attrs.getNamedItem("town").getNodeValue();
                  int locId = parseInt(attrs, "locId");
                  int castle = parseInt(attrs, "castle");
                  int bbs = parseInt(attrs, "bbs");
                  MapRegionTemplate region = new MapRegionTemplate(name, town, locId, castle, bbs);

                  for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
                     attrs = c.getAttributes();
                     if ("respawnPoint".equalsIgnoreCase(c.getNodeName())) {
                        int spawnX = parseInt(attrs, "X");
                        int spawnY = parseInt(attrs, "Y");
                        int spawnZ = parseInt(attrs, "Z");
                        boolean other = parseBoolean(attrs, "isOther");
                        boolean chaotic = parseBoolean(attrs, "isChaotic");
                        boolean banish = parseBoolean(attrs, "isBanish");
                        if (other) {
                           region.addOtherSpawn(spawnX, spawnY, spawnZ);
                        } else if (chaotic) {
                           region.addChaoticSpawn(spawnX, spawnY, spawnZ);
                        } else if (banish) {
                           region.addBanishSpawn(spawnX, spawnY, spawnZ);
                        } else {
                           region.addSpawn(spawnX, spawnY, spawnZ);
                        }
                     } else if ("map".equalsIgnoreCase(c.getNodeName())) {
                        region.addMap(parseInt(attrs, "X"), parseInt(attrs, "Y"));
                     } else if ("banned".equalsIgnoreCase(c.getNodeName())) {
                        region.addBannedRace(attrs.getNamedItem("race").getNodeValue(), attrs.getNamedItem("point").getNodeValue());
                     }
                  }

                  _regions.put(name, region);
               }
            }
         }
      }
   }

   public final MapRegionTemplate getMapRegion(int locX, int locY) {
      for(MapRegionTemplate region : _regions.values()) {
         if (region.isZoneInRegion(this.getMapRegionX(locX), this.getMapRegionY(locY))) {
            return region;
         }
      }

      return null;
   }

   public final int getMapRegionLocId(int locX, int locY) {
      MapRegionTemplate region = this.getMapRegion(locX, locY);
      return region != null ? region.getLocId() : 0;
   }

   public final MapRegionTemplate getMapRegion(GameObject obj) {
      return this.getMapRegion(obj.getX(), obj.getY());
   }

   public final int getMapRegionLocId(GameObject obj) {
      return this.getMapRegionLocId(obj.getX(), obj.getY());
   }

   public final int getMapRegionX(int posX) {
      return (posX >> 15) + 9 + 11;
   }

   public final int getMapRegionY(int posY) {
      return (posY >> 15) + 10 + 8;
   }

   public String getClosestTownName(Creature activeChar) {
      MapRegionTemplate region = this.getMapRegion(activeChar);
      return region == null ? "Aden Castle Town" : region.getTown();
   }

   public int getAreaCastle(Creature activeChar) {
      MapRegionTemplate region = this.getMapRegion(activeChar);
      return region == null ? 0 : region.getCastle();
   }

   public Location getTeleToLocation(Creature activeChar, TeleportWhereType teleportWhere) {
      if (activeChar.isPlayer()) {
         Player player = (Player)activeChar;
         Castle castle = null;
         Fort fort = null;
         ClanHall clanhall = null;
         if (player.getClan() != null && !player.isFlyingMounted() && !player.isFlying()) {
            if (teleportWhere == TeleportWhereType.CLANHALL) {
               clanhall = ClanHallManager.getInstance().getAbstractHallByOwner(player.getClan());
               if (clanhall != null) {
                  ClanHallZone zone = clanhall.getZone();
                  if (zone != null && !player.isFlyingMounted()) {
                     return zone.getSpawnLoc();
                  }
               }
            }

            if (teleportWhere == TeleportWhereType.CASTLE) {
               castle = CastleManager.getInstance().getCastleByOwner(player.getClan());
               if (castle == null) {
                  castle = CastleManager.getInstance().getCastle(player);
                  if (castle == null || !castle.getSiege().getIsInProgress() || castle.getSiege().getDefenderClan(player.getClan()) == null) {
                     castle = null;
                  }
               }

               if (castle != null && castle.getId() > 0) {
                  return castle.getCastleZone().getSpawnLoc();
               }
            }

            if (teleportWhere == TeleportWhereType.FORTRESS) {
               fort = FortManager.getInstance().getFortByOwner(player.getClan());
               if (fort == null) {
                  fort = FortManager.getInstance().getFort(player);
                  if (fort == null || !fort.getSiege().getIsInProgress() || fort.getOwnerClan() != player.getClan()) {
                     fort = null;
                  }
               }

               if (fort != null && fort.getId() > 0) {
                  return fort.getFortZone().getSpawnLoc();
               }
            }

            if (teleportWhere == TeleportWhereType.SIEGEFLAG) {
               castle = CastleManager.getInstance().getCastle(player);
               fort = FortManager.getInstance().getFort(player);
               clanhall = ClanHallManager.getInstance().getNearbyAbstractHall(activeChar.getX(), activeChar.getY(), 10000);
               SiegeFlagInstance tw_flag = TerritoryWarManager.getInstance().getHQForClan(player.getClan());
               if (tw_flag == null) {
                  tw_flag = TerritoryWarManager.getInstance().getFlagForClan(player.getClan());
               }

               if (tw_flag != null) {
                  return new Location(tw_flag.getX(), tw_flag.getY(), tw_flag.getZ());
               }

               if (castle != null) {
                  if (castle.getSiege().getIsInProgress()) {
                     List<Npc> flags = castle.getSiege().getFlag(player.getClan());
                     if (flags != null && !flags.isEmpty()) {
                        Npc flag = flags.get(0);
                        return new Location(flag.getX(), flag.getY(), flag.getZ());
                     }
                  }
               } else if (fort != null) {
                  if (fort.getSiege().getIsInProgress()) {
                     List<Npc> flags = fort.getSiege().getFlag(player.getClan());
                     if (flags != null && !flags.isEmpty()) {
                        Npc flag = flags.get(0);
                        return new Location(flag.getX(), flag.getY(), flag.getZ());
                     }
                  }
               } else if (clanhall != null && clanhall.isSiegableHall()) {
                  SiegableHall sHall = (SiegableHall)clanhall;
                  List<Npc> flags = sHall.getSiege().getFlag(player.getClan());
                  if (flags != null && !flags.isEmpty()) {
                     Npc flag = flags.get(0);
                     return new Location(flag.getX(), flag.getY(), flag.getZ());
                  }
               }
            }
         }

         if (teleportWhere == TeleportWhereType.CASTLE_BANISH) {
            castle = CastleManager.getInstance().getCastle(player);
            if (castle != null) {
               return castle.getCastleZone().getBanishSpawnLoc();
            }
         } else if (teleportWhere == TeleportWhereType.FORTRESS_BANISH) {
            fort = FortManager.getInstance().getFort(activeChar);
            if (fort != null) {
               return fort.getFortZone().getBanishSpawnLoc();
            }
         } else if (teleportWhere == TeleportWhereType.CLANHALL_BANISH) {
            clanhall = ClanHallManager.getInstance().getClanHall(activeChar);
            if (clanhall != null) {
               return clanhall.getZone().getBanishSpawnLoc();
            }
         }

         if (player.getKarma() > 0) {
            try {
               RespawnZone zone = ZoneManager.getInstance().getZone(player, RespawnZone.class);
               if (zone != null) {
                  return this.getRestartRegion(activeChar, zone.getRespawnPoint((Player)activeChar)).getChaoticSpawnLoc();
               }

               return this.getMapRegion(activeChar).getChaoticSpawnLoc();
            } catch (Exception var13) {
               if (player.isFlyingMounted()) {
                  return _regions.get("union_base_of_kserth").getChaoticSpawnLoc();
               }

               return _regions.get("talking_island_town").getChaoticSpawnLoc();
            }
         }

         castle = CastleManager.getInstance().getCastle(player);
         if (castle != null
            && castle.getSiege().getIsInProgress()
            && (castle.getSiege().checkIsDefender(player.getClan()) || castle.getSiege().checkIsAttacker(player.getClan()))
            && SevenSigns.getInstance().getSealOwner(3) == 2) {
            return castle.getCastleZone().getOtherSpawnLoc();
         }

         if (player.getReflectionId() > 0) {
            Reflection inst = ReflectionManager.getInstance().getReflection(player.getReflectionId());
            if (inst != null) {
               Location loc = inst.getReturnLoc();
               if (loc != null) {
                  return loc;
               }
            }
         }
      }

      try {
         RespawnZone zone = ZoneManager.getInstance().getZone(activeChar, RespawnZone.class);
         if (zone != null) {
            return this.getRestartRegion(activeChar, zone.getRespawnPoint((Player)activeChar)).getSpawnLoc();
         } else {
            MapRegionTemplate region = this.getMapRegion(activeChar);
            if (activeChar.isPlayer() && region.getBannedRace().containsKey(activeChar.getActingPlayer().getRace())) {
               region = _regions.get(region.getBannedRace().get(activeChar.getActingPlayer().getRace()));
            }

            return region.getSpawnLoc();
         }
      } catch (Exception var12) {
         return _regions.get("talking_island_town").getSpawnLoc();
      }
   }

   public MapRegionTemplate getRestartRegion(Creature activeChar, String point) {
      try {
         Player player = (Player)activeChar;
         MapRegionTemplate region = _regions.get(point);
         if (region.getBannedRace().containsKey(player.getRace())) {
            region = _regions.get(region.getBannedRace().get(player.getRace()));
         }

         return region;
      } catch (Exception var5) {
         return _regions.get("talking_island_town");
      }
   }

   public int getBBs(Location loc) {
      MapRegionTemplate region = this.getMapRegion(loc.getX(), loc.getY());
      return region != null ? region.getBbs() : _regions.get("talking_island_town").getBbs();
   }

   public MapRegionTemplate getMapRegionByName(String regionName) {
      return _regions.get(regionName);
   }

   public static MapRegionManager getInstance() {
      return MapRegionManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final MapRegionManager _instance = new MapRegionManager();
   }
}
