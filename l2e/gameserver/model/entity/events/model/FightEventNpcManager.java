package l2e.gameserver.model.entity.events.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.spawn.Spawner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class FightEventNpcManager {
   private static final Logger _log = Logger.getLogger(FightEventNpcManager.class.getName());
   private static FightEventNpcManager _instance;
   private final List<Npc> _npclist = new ArrayList<>();
   private final List<Location> _locations = new ArrayList<>();
   private boolean _isSpawned = false;

   public FightEventNpcManager() {
      this._npclist.clear();
      this._locations.clear();
      this.parseLocations();
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._locations.size() + " locations for event manager.");
   }

   public void reload() {
      this._npclist.clear();
      this._locations.clear();
      this.parseLocations();
      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._locations.size() + " locations for event manager.");
   }

   private void parseLocations() {
      File spawnFile = new File("data/stats/events/manager/spawnlist.xml");

      try {
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         Document doc = db.parse(spawnFile);
         if (!doc.getDocumentElement().getNodeName().equalsIgnoreCase("list")) {
            throw new NullPointerException("WARNING!!! stats/events/manager/spawnlist.xml bad spawn file!");
         }

         Node first = doc.getDocumentElement().getFirstChild();

         for(Node n = first; n != null; n = n.getNextSibling()) {
            if (n.getNodeName().equalsIgnoreCase("manager")) {
               for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                  if (d.getNodeName().equalsIgnoreCase("loc")) {
                     try {
                        int x = Integer.parseInt(d.getAttributes().getNamedItem("x").getNodeValue());
                        int y = Integer.parseInt(d.getAttributes().getNamedItem("y").getNodeValue());
                        int z = Integer.parseInt(d.getAttributes().getNamedItem("z").getNodeValue());
                        int h = d.getAttributes().getNamedItem("heading").getNodeValue() != null
                           ? Integer.parseInt(d.getAttributes().getNamedItem("heading").getNodeValue())
                           : 0;
                        this._locations.add(new Location(x, y, z, h));
                     } catch (NumberFormatException var12) {
                        _log.warning("Wrong number format in stats/events/manager/spawnlist.xml");
                     }
                  }
               }
            }
         }
      } catch (Exception var13) {
         _log.log(Level.WARNING, "FightEventNpcManager: error reading " + spawnFile.getAbsolutePath() + " ! " + var13.getMessage(), (Throwable)var13);
      }
   }

   public void trySpawnRegNpc() {
      if (!this.isSpawned()) {
         for(Location loc : this._locations) {
            if (loc != null) {
               try {
                  NpcTemplate template = NpcsParser.getInstance().getTemplate(53015);
                  if (template != null) {
                     Spawner spawn = new Spawner(template);
                     spawn.setX(loc.getX());
                     spawn.setY(loc.getY());
                     spawn.setZ(loc.getZ());
                     spawn.setHeading(loc.getHeading());
                     spawn.setAmount(1);
                     spawn.setRespawnDelay(0);
                     spawn.setReflectionId(0);
                     spawn.stopRespawn();
                     spawn.init();
                     this._npclist.add(spawn.getLastSpawn());
                  }
               } catch (Exception var5) {
               }
            }
         }

         this._isSpawned = true;
      }
   }

   public void tryUnspawnRegNpc() {
      if (this.isSpawned() && !this.isAcviteRegister() && !this._npclist.isEmpty()) {
         for(Npc _npc : this._npclist) {
            if (_npc != null) {
               _npc.deleteMe();
            }
         }

         this._isSpawned = false;
      }
   }

   private boolean isSpawned() {
      return this._isSpawned;
   }

   private boolean isAcviteRegister() {
      boolean activeReg = false;

      for(AbstractFightEvent event : FightEventManager.getInstance().getActiveEvents().values()) {
         if (event != null && FightEventManager.getInstance().isRegistrationOpened(event)) {
            activeReg = true;
            break;
         }
      }

      return activeReg;
   }

   public static FightEventNpcManager getInstance() {
      if (_instance == null) {
         _instance = new FightEventNpcManager();
      }

      return _instance;
   }
}
