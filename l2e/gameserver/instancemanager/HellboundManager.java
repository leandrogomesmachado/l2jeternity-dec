package l2e.gameserver.instancemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.commons.geometry.Polygon;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.instance.RaidBossInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.spawn.SpawnRange;
import l2e.gameserver.model.spawn.SpawnTemplate;
import l2e.gameserver.model.spawn.SpawnTerritory;
import l2e.gameserver.model.spawn.Spawner;
import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class HellboundManager {
   private static final Logger _log = Logger.getLogger(HellboundManager.class.getName());
   private int _level = 0;
   private int _trust = 0;
   private int _maxTrust = 0;
   private int _minTrust = 0;
   private ScheduledFuture<?> _engine = null;
   private final List<HellboundManager.HellboundSpawn> _population = new ArrayList<>();

   protected HellboundManager() {
      this.loadData();
      this.loadSpawns();
   }

   public final int getLevel() {
      return this._level;
   }

   public final synchronized void updateTrust(int t, boolean useRates) {
      if (!this.isLocked()) {
         int reward = t;
         if (useRates) {
            reward = (int)(t > 0 ? Config.RATE_HB_TRUST_INCREASE * (double)t : Config.RATE_HB_TRUST_DECREASE * (double)t);
         }

         int trust = Math.max(this._trust + reward, this._minTrust);
         if (this._maxTrust > 0) {
            this._trust = Math.min(trust, this._maxTrust);
         } else {
            this._trust = trust;
         }
      }
   }

   public final void setLevel(int lvl) {
      this._level = lvl;
   }

   public final int getTrust() {
      return this._trust;
   }

   public final int getMaxTrust() {
      return this._maxTrust;
   }

   public final int getMinTrust() {
      return this._minTrust;
   }

   public final void setMaxTrust(int trust) {
      this._maxTrust = trust;
      if (this._maxTrust > 0 && this._trust > this._maxTrust) {
         this._trust = this._maxTrust;
      }
   }

   public final void setMinTrust(int trust) {
      this._minTrust = trust;
      if (this._trust >= this._maxTrust) {
         this._trust = this._minTrust;
      }
   }

   public final boolean isLocked() {
      return this._level == 0;
   }

   public final void unlock() {
      if (this._level == 0) {
         this.setLevel(1);
      }
   }

   public final void registerEngine(Runnable r, int interval) {
      if (this._engine != null) {
         this._engine.cancel(false);
      }

      this._engine = ThreadPoolManager.getInstance().scheduleAtFixedRate(r, (long)interval, (long)interval);
   }

   public final void doSpawn() {
      int added = 0;
      int deleted = 0;

      for(HellboundManager.HellboundSpawn spawnDat : this._population) {
         try {
            if (spawnDat != null) {
               Npc npc = spawnDat.getLastSpawn();
               if (ArrayUtils.contains(spawnDat.getStages(), this._level)) {
                  if (npc instanceof RaidBossInstance) {
                     npc = spawnDat.getLastSpawn();
                     if (npc == null) {
                        RaidBossSpawnManager.getInstance().addNewSpawn(spawnDat, false);
                        ++added;
                     }
                  } else {
                     spawnDat.startRespawn();
                     npc = spawnDat.getLastSpawn();
                     if (npc == null) {
                        npc = spawnDat.doSpawn();
                        ++added;
                        SpawnParser.getInstance().addRandomSpawnByNpc(spawnDat, npc.getTemplate());
                     } else {
                        if (npc.isDecayed()) {
                           npc.setDecayed(false);
                        }

                        if (npc.isDead()) {
                           npc.doRevive();
                        }

                        if (!npc.isVisible()) {
                           ++added;
                        }

                        npc.setCurrentHp(npc.getMaxHp());
                        npc.setCurrentMp(npc.getMaxMp());
                     }
                  }
               } else {
                  spawnDat.stopRespawn();
                  if (npc != null && npc.isVisible()) {
                     SpawnParser.getInstance().removeRandomSpawnByNpc(npc);
                     npc.deleteMe();
                     ++deleted;
                  }
               }
            }
         } catch (Exception var6) {
            _log.warning(this.getClass().getSimpleName() + ": " + var6.getMessage());
         }
      }

      if (added > 0 && Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Spawned " + added + " NPCs.");
      }

      if (deleted > 0 && Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Removed " + deleted + " NPCs.");
      }
   }

   public final void cleanUp() {
      this.saveData();
      if (this._engine != null) {
         this._engine.cancel(true);
         this._engine = null;
      }

      this._population.clear();
   }

   private final void loadData() {
      if (GlobalVariablesManager.getInstance().isVariableStored("HBLevel")) {
         this._level = Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("HBLevel"));
         this._trust = Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("HBTrust"));
      } else {
         this.saveData();
      }
   }

   public final void saveData() {
      GlobalVariablesManager.getInstance().storeVariable("HBLevel", String.valueOf(this._level));
      GlobalVariablesManager.getInstance().storeVariable("HBTrust", String.valueOf(this._trust));
   }

   protected void loadSpawns() {
      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setIgnoringComments(true);
         File file = new File(Config.DATAPACK_ROOT + "/data/stats/npcs/spawnZones/hellbound_spawnlist.xml");
         if (!file.exists()) {
            _log.severe("[HellboundManager] Missing hellbound_spawnlist.xml. The quest wont work without it!");
            return;
         }

         Document doc = factory.newDocumentBuilder().parse(file);
         Node first = doc.getFirstChild();
         if (first != null && "list".equalsIgnoreCase(first.getNodeName())) {
            for(Node n = first.getFirstChild(); n != null; n = n.getNextSibling()) {
               if ("spawn".equalsIgnoreCase(n.getNodeName())) {
                  int totalCount = 0;
                  NamedNodeMap attrs = n.getAttributes();
                  Node att = attrs.getNamedItem("npcId");
                  if (att == null) {
                     _log.severe("[HellboundManager] Missing npcId in npc List, skipping");
                  } else {
                     int npcId = Integer.parseInt(attrs.getNamedItem("npcId").getNodeValue());
                     Location spawnLoc = null;
                     if (n.getAttributes().getNamedItem("loc") != null) {
                        spawnLoc = Location.parseLoc(n.getAttributes().getNamedItem("loc").getNodeValue());
                     }

                     int count = 1;
                     if (n.getAttributes().getNamedItem("count") != null) {
                        count = Integer.parseInt(n.getAttributes().getNamedItem("count").getNodeValue());
                     }

                     int respawn = 60;
                     if (n.getAttributes().getNamedItem("respawn") != null) {
                        respawn = Integer.parseInt(n.getAttributes().getNamedItem("respawn").getNodeValue());
                     }

                     int respawnRnd = 0;
                     if (n.getAttributes().getNamedItem("respawn_random") != null) {
                        respawnRnd = Integer.parseInt(n.getAttributes().getNamedItem("respawn_random").getNodeValue());
                     }

                     Node attr = n.getAttributes().getNamedItem("stage");
                     StringTokenizer st = new StringTokenizer(attr.getNodeValue(), ";");
                     int tokenCount = st.countTokens();
                     int[] stages = new int[tokenCount];

                     for(int i = 0; i < tokenCount; ++i) {
                        Integer value = Integer.decode(st.nextToken().trim());
                        stages[i] = value;
                     }

                     SpawnTerritory territory = null;

                     for(Node s1 = n.getFirstChild(); s1 != null; s1 = s1.getNextSibling()) {
                        if ("territory".equalsIgnoreCase(s1.getNodeName())) {
                           Polygon poly = new Polygon();

                           for(Node s2 = s1.getFirstChild(); s2 != null; s2 = s2.getNextSibling()) {
                              if ("add".equalsIgnoreCase(s2.getNodeName())) {
                                 int x = Integer.parseInt(s2.getAttributes().getNamedItem("x").getNodeValue());
                                 int y = Integer.parseInt(s2.getAttributes().getNamedItem("y").getNodeValue());
                                 int minZ = Integer.parseInt(s2.getAttributes().getNamedItem("zmin").getNodeValue());
                                 int maxZ = Integer.parseInt(s2.getAttributes().getNamedItem("zmax").getNodeValue());
                                 poly.add(x, y).setZmin(minZ).setZmax(maxZ);
                              }
                           }

                           territory = new SpawnTerritory().add(poly);
                           if (!poly.validate()) {
                              _log.log(Level.WARNING, "HellboundManager: Invalid spawn territory : " + poly + '!');
                           }
                        }
                     }

                     if (spawnLoc == null && territory == null) {
                        _log.warning("HellboundManager: no spawn data for npc id : " + npcId + '!');
                     } else {
                        while(totalCount < count) {
                           NpcTemplate template = NpcsParser.getInstance().getTemplate(npcId);
                           if (template != null) {
                              SpawnTemplate tpl = new SpawnTemplate("none", 1, respawn, respawnRnd);
                              tpl.addSpawnRange((SpawnRange)(spawnLoc != null ? spawnLoc : territory));
                              HellboundManager.HellboundSpawn spawnDat = new HellboundManager.HellboundSpawn(template);
                              spawnDat.setAmount(1);
                              spawnDat.setSpawnTemplate(tpl);
                              spawnDat.setLocation(spawnDat.calcSpawnRangeLoc(0, template));
                              spawnDat.setRespawnDelay(respawn, respawnRnd);
                              spawnDat.setStages(stages);
                              this._population.add(spawnDat);
                              ++totalCount;
                              SpawnParser.getInstance().addNewSpawn(spawnDat);
                           }
                        }
                     }
                  }
               }
            }
         }

         _log.info("HellboundManager: Loaded " + this._population.size() + " spawn entries.");
      } catch (Exception var26) {
         _log.log(Level.WARNING, "HellboundManager: SpawnList could not be initialized.", (Throwable)var26);
      }
   }

   public static final HellboundManager getInstance() {
      return HellboundManager.SingletonHolder._instance;
   }

   public static final class HellboundSpawn extends Spawner {
      private int[] _stages;

      public HellboundSpawn(NpcTemplate mobTemplate) throws SecurityException, ClassNotFoundException, NoSuchMethodException {
         super(mobTemplate);
      }

      public int[] getStages() {
         return this._stages;
      }

      public void setStages(int[] stages) {
         this._stages = stages;
      }
   }

   private static class SingletonHolder {
      protected static final HellboundManager _instance = new HellboundManager();
   }
}
