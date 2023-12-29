package l2e.gameserver.instancemanager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.DimensionalRiftRoom;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.DimensionalRift;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class DimensionalRiftManager {
   private static Logger _log = Logger.getLogger(DimensionalRiftManager.class.getName());
   private final Map<Byte, Map<Byte, DimensionalRiftRoom>> _rooms = new HashMap<>(7);
   private final int DIMENSIONAL_FRAGMENT_ITEM_ID = 7079;

   public static DimensionalRiftManager getInstance() {
      return DimensionalRiftManager.SingletonHolder._instance;
   }

   protected DimensionalRiftManager() {
      this.loadRooms();
      this.loadSpawns();
   }

   public DimensionalRiftRoom getRoom(byte type, byte room) {
      return this._rooms.get(type) == null ? null : this._rooms.get(type).get(room);
   }

   private void loadRooms() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM dimensional_rift");

         ResultSet rs;
         byte type;
         byte room_id;
         int xMin;
         int xMax;
         int yMin;
         int yMax;
         int z1;
         int z2;
         int xT;
         int yT;
         int zT;
         boolean isBossRoom;
         for(rs = statement.executeQuery();
            rs.next();
            this._rooms.get(type).put(room_id, new DimensionalRiftRoom(type, room_id, xMin, xMax, yMin, yMax, z1, z2, xT, yT, zT, isBossRoom))
         ) {
            type = rs.getByte("type");
            room_id = rs.getByte("room_id");
            xMin = rs.getInt("xMin");
            xMax = rs.getInt("xMax");
            yMin = rs.getInt("yMin");
            yMax = rs.getInt("yMax");
            z1 = rs.getInt("zMin");
            z2 = rs.getInt("zMax");
            xT = rs.getInt("xT");
            yT = rs.getInt("yT");
            zT = rs.getInt("zT");
            isBossRoom = rs.getByte("boss") > 0;
            if (!this._rooms.containsKey(type)) {
               this._rooms.put(type, new HashMap<>(9));
            }
         }

         rs.close();
         statement.close();
      } catch (Exception var27) {
         _log.log(Level.WARNING, "Can't load Dimension Rift zones. " + var27.getMessage(), (Throwable)var27);
      }

      int typeSize = this._rooms.keySet().size();
      int roomSize = 0;

      for(byte b : this._rooms.keySet()) {
         roomSize += this._rooms.get(b).keySet().size();
      }

      _log.info(this.getClass().getSimpleName() + ": Loaded " + typeSize + " room types with " + roomSize + " rooms.");
   }

   public void loadSpawns() {
      int countGood = 0;
      int countBad = 0;

      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         File file = new File(Config.DATAPACK_ROOT, "data/stats/npcs/spawnZones/dimensionalRift.xml");
         if (!file.exists()) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Couldn't find data/" + file.getName());
            return;
         }

         Document doc = factory.newDocumentBuilder().parse(file);

         for(Node rift = doc.getFirstChild(); rift != null; rift = rift.getNextSibling()) {
            if ("rift".equalsIgnoreCase(rift.getNodeName())) {
               for(Node area = rift.getFirstChild(); area != null; area = area.getNextSibling()) {
                  if ("area".equalsIgnoreCase(area.getNodeName())) {
                     NamedNodeMap attrs = area.getAttributes();
                     byte type = Byte.parseByte(attrs.getNamedItem("type").getNodeValue());

                     for(Node room = area.getFirstChild(); room != null; room = room.getNextSibling()) {
                        if ("room".equalsIgnoreCase(room.getNodeName())) {
                           attrs = room.getAttributes();
                           byte roomId = Byte.parseByte(attrs.getNamedItem("id").getNodeValue());

                           for(Node spawn = room.getFirstChild(); spawn != null; spawn = spawn.getNextSibling()) {
                              if ("spawn".equalsIgnoreCase(spawn.getNodeName())) {
                                 attrs = spawn.getAttributes();
                                 int mobId = Integer.parseInt(attrs.getNamedItem("mobId").getNodeValue());
                                 int delay = Integer.parseInt(attrs.getNamedItem("delay").getNodeValue());
                                 int count = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
                                 NpcTemplate template = NpcsParser.getInstance().getTemplate(mobId);
                                 if (template == null) {
                                    _log.warning("Template " + mobId + " not found!");
                                 }

                                 if (!this._rooms.containsKey(type)) {
                                    _log.warning("Type " + type + " not found!");
                                 } else if (!this._rooms.get(type).containsKey(roomId)) {
                                    _log.warning("Room " + roomId + " in Type " + type + " not found!");
                                 }

                                 for(int i = 0; i < count; ++i) {
                                    DimensionalRiftRoom riftRoom = this._rooms.get(type).get(roomId);
                                    int x = riftRoom.getRandomX();
                                    int y = riftRoom.getRandomY();
                                    int z = riftRoom.getTeleportCoorinates()[2];
                                    if (template != null && this._rooms.containsKey(type) && this._rooms.get(type).containsKey(roomId)) {
                                       Spawner spawnDat = new Spawner(template);
                                       spawnDat.setAmount(1);
                                       spawnDat.setX(x);
                                       spawnDat.setY(y);
                                       spawnDat.setZ(z);
                                       spawnDat.setHeading(-1);
                                       spawnDat.setRespawnDelay(delay);
                                       SpawnParser.getInstance().addNewSpawn(spawnDat);
                                       this._rooms.get(type).get(roomId).getSpawns().add(spawnDat);
                                       ++countGood;
                                    } else {
                                       ++countBad;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      } catch (Exception var23) {
         _log.log(Level.WARNING, "Error on loading dimensional rift spawns: " + var23.getMessage(), (Throwable)var23);
      }

      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loaded " + countGood + " dimensional rift spawns, " + countBad + " errors.");
      }
   }

   public void reload() {
      for(byte b : this._rooms.keySet()) {
         for(byte i : this._rooms.get(b).keySet()) {
            this._rooms.get(b).get(i).getSpawns().clear();
         }

         this._rooms.get(b).clear();
      }

      this._rooms.clear();
      this.loadRooms();
      this.loadSpawns();
   }

   public boolean checkIfInRiftZone(int x, int y, int z, boolean ignorePeaceZone) {
      if (ignorePeaceZone) {
         return this._rooms.get((byte)0).get((byte)1).checkIfInZone(x, y, z);
      } else {
         return this._rooms.get((byte)0).get((byte)1).checkIfInZone(x, y, z) && !this._rooms.get((byte)0).get((byte)0).checkIfInZone(x, y, z);
      }
   }

   public boolean checkIfInPeaceZone(int x, int y, int z) {
      return this._rooms.get((byte)0).get((byte)0).checkIfInZone(x, y, z);
   }

   public void teleportToWaitingRoom(Player player) {
      int[] coords = this.getRoom((byte)0, (byte)0).getTeleportCoorinates();
      player.teleToLocation(coords[0], coords[1], coords[2], true);
   }

   public synchronized void start(Player player, byte type, Npc npc) {
      boolean canPass = true;
      if (!player.isInParty()) {
         this.showHtmlFile(player, "data/html/seven_signs/rift/NoParty.htm", npc);
      } else if (player.getParty().getLeaderObjectId() != player.getObjectId()) {
         this.showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
      } else if (player.getParty().isInDimensionalRift()) {
         this.handleCheat(player, npc);
      } else if (player.getParty().getMemberCount() < Config.RIFT_MIN_PARTY_SIZE) {
         NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
         html.setFile(player, player.getLang(), "data/html/seven_signs/rift/SmallParty.htm");
         html.replace("%npc_name%", npc.getName());
         html.replace("%count%", Integer.toString(Config.RIFT_MIN_PARTY_SIZE));
         player.sendPacket(html);
      } else if (!this.isAllowedEnter(type)) {
         player.sendMessage("Rift is full. Try later.");
      } else {
         for(Player p : player.getParty().getMembers()) {
            if (!this.checkIfInPeaceZone(p.getX(), p.getY(), p.getZ())) {
               canPass = false;
               break;
            }
         }

         if (!canPass) {
            this.showHtmlFile(player, "data/html/seven_signs/rift/NotInWaitingRoom.htm", npc);
         } else {
            int count = this.getNeededItems(type);

            for(Player p : player.getParty().getMembers()) {
               ItemInstance i = p.getInventory().getItemByItemId(7079);
               if (i == null) {
                  canPass = false;
                  break;
               }

               if (i.getCount() > 0L) {
                  if (i.getCount() >= (long)this.getNeededItems(type)) {
                     continue;
                  }

                  canPass = false;
                  break;
               }

               canPass = false;
               break;
            }

            if (!canPass) {
               NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
               html.setFile(player, player.getLang(), "data/html/seven_signs/rift/NoFragments.htm");
               html.replace("%npc_name%", npc.getName());
               html.replace("%count%", Integer.toString(count));
               player.sendPacket(html);
            } else {
               for(Player p : player.getParty().getMembers()) {
                  ItemInstance i = p.getInventory().getItemByItemId(7079);
                  if (!p.destroyItem("RiftEntrance", i, (long)count, null, false)) {
                     NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
                     html.setFile(player, player.getLang(), "data/html/seven_signs/rift/NoFragments.htm");
                     html.replace("%npc_name%", npc.getName());
                     html.replace("%count%", Integer.toString(count));
                     player.sendPacket(html);
                     return;
                  }
               }

               byte room;
               do {
                  List<Byte> emptyRooms = this.getFreeRooms(type);
                  room = emptyRooms.get(Rnd.get(1, emptyRooms.size()) - 1);
               } while(this._rooms.get(type).get(room).isPartyInside());

               new DimensionalRift(player.getParty(), type, room);
            }
         }
      }
   }

   public void killRift(DimensionalRift d) {
      if (d.getTeleportTimerTask() != null) {
         d.getTeleportTimerTask().cancel();
      }

      d.setTeleportTimerTask(null);
      if (d.getTeleportTimer() != null) {
         d.getTeleportTimer().cancel();
      }

      d.setTeleportTimer(null);
      if (d.getSpawnTimerTask() != null) {
         d.getSpawnTimerTask().cancel();
      }

      d.setSpawnTimerTask(null);
      if (d.getSpawnTimer() != null) {
         d.getSpawnTimer().cancel();
      }

      d.setSpawnTimer(null);
   }

   private int getNeededItems(byte type) {
      switch(type) {
         case 1:
            return Config.RIFT_ENTER_COST_RECRUIT;
         case 2:
            return Config.RIFT_ENTER_COST_SOLDIER;
         case 3:
            return Config.RIFT_ENTER_COST_OFFICER;
         case 4:
            return Config.RIFT_ENTER_COST_CAPTAIN;
         case 5:
            return Config.RIFT_ENTER_COST_COMMANDER;
         case 6:
            return Config.RIFT_ENTER_COST_HERO;
         default:
            throw new IndexOutOfBoundsException();
      }
   }

   public void showHtmlFile(Player player, String file, Npc npc) {
      NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
      html.setFile(player, player.getLang(), file);
      html.replace("%npc_name%", npc.getName());
      player.sendPacket(html);
   }

   public void handleCheat(Player player, Npc npc) {
      this.showHtmlFile(player, "data/html/seven_signs/rift/Cheater.htm", npc);
      if (!player.isGM()) {
         Util.handleIllegalPlayerAction(player, "" + player.getName() + " tried to cheat in dimensional rift.");
      }
   }

   public boolean isAllowedEnter(byte type) {
      int count = 0;

      for(DimensionalRiftRoom room : this._rooms.get(type).values()) {
         if (room.isPartyInside()) {
            ++count;
         }
      }

      return count < this._rooms.get(type).size() - 1;
   }

   public List<Byte> getFreeRooms(byte type) {
      List<Byte> list = new ArrayList<>();

      for(DimensionalRiftRoom room : this._rooms.get(type).values()) {
         if (!room.isPartyInside()) {
            list.add(room.getRoom());
         }
      }

      return list;
   }

   private static class SingletonHolder {
      protected static final DimensionalRiftManager _instance = new DimensionalRiftManager();
   }
}
